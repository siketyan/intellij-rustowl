package jp.s6n.idea.rustowl.lsp

import com.intellij.openapi.progress.util.ProgressIndicatorBase
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking

class RustOwlProgressTest : BasePlatformTestCase() {
    /** Records whether the indicator was indeterminate whenever a fraction was set. */
    private class RecordingIndicator : ProgressIndicatorBase() {
        val indeterminateOnSetFraction = mutableListOf<Boolean>()

        override fun setFraction(fraction: Double) {
            indeterminateOnSetFraction.add(isIndeterminate)
            super.setFraction(fraction)
        }
    }

    private fun mirror(vararg progresses: Progress): RecordingIndicator {
        val indicator = RecordingIndicator().apply { isIndeterminate = true }
        val channel = Channel<Progress>(Channel.UNLIMITED)
        progresses.forEach { channel.trySend(it) }

        runBlocking { mirrorProgress(channel, indicator) }

        return indicator
    }

    fun testStaysIndeterminateUntilFirstReport() {
        val indicator = mirror(Progress.Done)

        assertTrue(indicator.isIndeterminate)
        assertEmpty(indicator.indeterminateOnSetFraction)
    }

    fun testBecomesDeterminateBeforeSettingFraction() {
        val indicator =
            mirror(
                Progress.InProgress("foo analyzed", 50),
                Progress.InProgress("bar analyzed", 100),
                Progress.Done,
            )

        assertFalse(indicator.isIndeterminate)
        assertEquals(listOf(false, false), indicator.indeterminateOnSetFraction)
        assertEquals("bar analyzed", indicator.text)
        assertEquals(1.0, indicator.fraction)
    }

    fun testStopsOnDone() {
        val indicator = mirror(Progress.Done, Progress.InProgress("ignored", 50))

        assertTrue(indicator.isIndeterminate)
        assertNull(indicator.text)
    }
}
