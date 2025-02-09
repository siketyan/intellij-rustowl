package jp.s6n.idea.rustowl.lsp

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.LogicalPosition
import com.intellij.openapi.util.TextRange

data class RustOwlCursorResponse (
    var decorations: List<Decoration>,
) {
    data class Decoration(
        var type: String,
        var range: Range,
        var hover_text: String,
    )

    data class Range(
        var start: Location,
        var end: Location,
    )

    data class Location(
        var line: Int,
        var character: Int,
    ) {
        fun toLogicalPosition() = LogicalPosition(line, character)

        fun toOffset(editor: Editor) = editor.logicalPositionToOffset(toLogicalPosition())
    }
}
