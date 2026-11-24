package jp.s6n.idea.rustowl.lsp

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.LogicalPosition
import com.intellij.platform.lsp.api.LspServerNotificationsHandler
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.lang.reflect.Proxy
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import jp.s6n.idea.rustowl.highlighting.RustOwlHighlighter
import org.eclipse.lsp4j.ProgressParams
import org.eclipse.lsp4j.WorkDoneProgressEnd
import org.eclipse.lsp4j.WorkDoneProgressNotification
import org.eclipse.lsp4j.WorkDoneProgressReport
import org.eclipse.lsp4j.jsonrpc.messages.Either

class RustOwlLspNotificationHandlerTest : BasePlatformTestCase() {
    /** Records the positions to highlight instead of sending requests to the server. */
    private class RecordingHighlighter(editor: Editor) : RustOwlHighlighter(editor) {
        val positions = LinkedBlockingQueue<LogicalPosition>()

        override fun highlight(position: LogicalPosition) {
            positions.add(position)
        }
    }

    private val delegate =
        Proxy.newProxyInstance(
            javaClass.classLoader,
            arrayOf(LspServerNotificationsHandler::class.java),
        ) { _, _, _ ->
            null
        } as LspServerNotificationsHandler

    private fun createHandler() = RustOwlLspNotificationHandler(project, delegate)

    private fun notification(value: WorkDoneProgressNotification) =
        ProgressParams(Either.forLeft("token"), Either.forLeft(value))

    private fun openEditor(): RecordingHighlighter {
        myFixture.configureByText("main.rs", "fn main() {\n    let s = String::new();\n}\n")
        myFixture.editor.caretModel.moveToLogicalPosition(LogicalPosition(1, 8))

        return RecordingHighlighter(myFixture.editor).also {
            myFixture.editor.putUserData(RustOwlHighlighter.KEY, it)
        }
    }

    fun testHighlightsAgainAtCaretWhenAnalysisIsFinished() {
        val highlighter = openEditor()

        createHandler().notifyProgress(notification(WorkDoneProgressEnd()))
        PlatformTestUtil.dispatchAllInvocationEventsInIdeEventQueue()

        assertEquals(LogicalPosition(1, 8), highlighter.positions.poll(10, TimeUnit.SECONDS))
    }

    fun testDoesNotHighlightOnReport() {
        val highlighter = openEditor()

        createHandler()
            .notifyProgress(
                notification(
                    WorkDoneProgressReport().apply {
                        message = "foo analyzed"
                        percentage = 50
                    },
                ),
            )
        PlatformTestUtil.dispatchAllInvocationEventsInIdeEventQueue()

        assertNull(highlighter.positions.poll(1, TimeUnit.SECONDS))
    }
}
