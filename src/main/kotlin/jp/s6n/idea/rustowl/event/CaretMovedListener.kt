package jp.s6n.idea.rustowl.event

import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import jp.s6n.idea.rustowl.highlighting.RustOwlHighlighter
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class CaretMovedListener(
    private val highlighter: RustOwlHighlighter,
) : CaretListener {
    private val debounceDelay = 2000L;
    private val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(1)
    private var scheduledFuture: ScheduledFuture<*>? = null

    override fun caretPositionChanged(event: CaretEvent) {
        val fut = scheduledFuture
        if (fut != null && !fut.isCancelled) {
            fut.cancel(false)
        }

        scheduledFuture = scheduler.schedule(
            { highlighter.highlight(event.newPosition) },
            debounceDelay,
            TimeUnit.MILLISECONDS,
        )
    }
}
