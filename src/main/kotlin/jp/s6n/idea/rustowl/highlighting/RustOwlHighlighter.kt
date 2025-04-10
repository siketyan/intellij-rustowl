package jp.s6n.idea.rustowl.highlighting

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.LogicalPosition
import com.intellij.openapi.editor.markup.*
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.platform.lsp.api.LspServerManager
import com.intellij.ui.JBColor
import jp.s6n.idea.rustowl.lsp.RustOwlCursorRequest
import jp.s6n.idea.rustowl.lsp.RustOwlLsp4jServer
import jp.s6n.idea.rustowl.lsp.RustOwlLspServerSupportProvider
import jp.s6n.idea.rustowl.settings.RustOwlSettings

class RustOwlHighlighter(private val editor: Editor) {
    private val logger = Logger.getInstance(this.javaClass)
    private val settings = RustOwlSettings.getInstance()
    private val lastHighlighters = mutableListOf<RangeHighlighter>()

    fun highlight(position: LogicalPosition) {
        logger.debug("Highlighting lifetimes at $position")

        lastHighlighters.forEach { editor.markupModel.removeHighlighter(it) }

        val project = editor.project ?: return
        val file = FileDocumentManager.getInstance().getFile(editor.document) ?: return
        val server =
            LspServerManager.getInstance(project)
                .getServersForProvider(RustOwlLspServerSupportProvider::class.java)
                .firstOrNull() ?: return

        val response =
            server.sendRequestSync {
                (it as RustOwlLsp4jServer).cursor(
                    RustOwlCursorRequest(
                        RustOwlCursorRequest.Position(position.line, position.column),
                        RustOwlCursorRequest.Document(file.url),
                    )
                )
            } ?: return

        logger.debug("RustOwl response: $response")

        response.decorations.forEach { decoration ->
            if (decoration.overlapped) {
                return@forEach
            }

            val color =
                when (decoration.type) {
                    "lifetime" -> settings.lifetimeColor
                    "imm_borrow" -> settings.immutableBorrowingColor
                    "mut_borrow" -> settings.mutableBorrowingColor
                    "call",
                    "move" -> settings.valueMovedColor
                    "shared_mut",
                    "outlive" -> settings.lifetimeErrorColor
                    else -> {
                        logger.warn("Unexpected decoration type: ${decoration.type}")
                        return
                    }
                }.let { JBColor(it, it) }

            val textAttributes =
                TextAttributes().also {
                    it.effectType = EffectType.BOLD_LINE_UNDERSCORE
                    it.effectColor = color
                }

            val highlighter =
                editor.markupModel.addRangeHighlighter(
                    decoration.range.start.toOffset(editor),
                    decoration.range.end.toOffset(editor),
                    HighlighterLayer.LAST,
                    textAttributes,
                    HighlighterTargetArea.EXACT_RANGE,
                )

            lastHighlighters.add(highlighter)
        }
    }
}
