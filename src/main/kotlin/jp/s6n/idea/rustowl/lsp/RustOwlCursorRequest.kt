package jp.s6n.idea.rustowl.lsp

data class RustOwlCursorRequest(
    val position: Position,
    val document: Document,
) {
    data class Position(
        val line: Int,
        val character: Int,
    )

    data class Document(
        val uri: String,
    )
}
