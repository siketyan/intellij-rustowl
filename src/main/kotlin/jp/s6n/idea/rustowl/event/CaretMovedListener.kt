package jp.s6n.idea.rustowl.event

import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.editor.event.EditorFactoryEvent
import com.intellij.openapi.editor.event.EditorFactoryListener
import jp.s6n.idea.rustowl.highlighting.RustOwlHighlighter
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class CaretMovedListener : EditorFactoryListener, CaretListener {
    private val debounceDelay = 2000L;
    private val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(1)
    private var scheduledFuture: ScheduledFuture<*>? = null

    override fun editorCreated(event: EditorFactoryEvent) {
        event.editor.caretModel.addCaretListener(this)
    }

    override fun editorReleased(event: EditorFactoryEvent) {
        event.editor.caretModel.removeCaretListener(this)
    }

    override fun caretPositionChanged(event: CaretEvent) {
        val fut = scheduledFuture
        if (fut != null && !fut.isCancelled) {
            fut.cancel(false)
        }

        scheduledFuture = scheduler.schedule(
            { RustOwlHighlighter.highlight(event.editor, event.newPosition) },
            debounceDelay,
            TimeUnit.MILLISECONDS,
        )
    }
}
