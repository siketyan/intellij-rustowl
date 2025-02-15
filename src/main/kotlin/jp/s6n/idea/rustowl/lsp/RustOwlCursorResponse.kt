package jp.s6n.idea.rustowl.lsp

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.LogicalPosition

@Suppress("PropertyName")
data class RustOwlCursorResponse(var decorations: List<Decoration>) {
    data class Decoration(var type: String, var range: Range, var hover_text: String)

    data class Range(var start: Location, var end: Location)

    data class Location(var line: Int, var character: Int) {
        fun toOffset(editor: Editor) =
            editor.logicalPositionToOffset(LogicalPosition(line, character))
    }
}
