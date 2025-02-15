package jp.s6n.idea.rustowl.event

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.event.EditorFactoryEvent
import com.intellij.openapi.editor.event.EditorFactoryListener
import jp.s6n.idea.rustowl.highlighting.RustOwlHighlighter

class EditorUpdatedListener : EditorFactoryListener {
    private val caretListenerMap = mutableMapOf<Editor, CaretMovedListener>()

    override fun editorCreated(event: EditorFactoryEvent) {
        val highlighter = RustOwlHighlighter(event.editor)
        val listener = CaretMovedListener(highlighter)

        event.editor.caretModel.addCaretListener(listener)
        caretListenerMap[event.editor] = listener
    }

    override fun editorReleased(event: EditorFactoryEvent) {
        caretListenerMap[event.editor]?.let {
            event.editor.caretModel.removeCaretListener(it)
        }
    }
}
