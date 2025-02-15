package jp.s6n.idea.rustowl.highlighting

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.LogicalPosition
import com.intellij.openapi.editor.markup.*
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.platform.lsp.api.LspServerManager
import com.intellij.ui.JBColor
import java.awt.Color
import jp.s6n.idea.rustowl.lsp.RustOwlCursorRequest
import jp.s6n.idea.rustowl.lsp.RustOwlLsp4jServer
import jp.s6n.idea.rustowl.lsp.RustOwlLspServerSupportProvider

@Suppress("UnstableApiUsage")
class RustOwlHighlighter(private val editor: Editor) {
    private val logger = Logger.getInstance(this.javaClass)
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
            @Suppress("UseJBColor")
            val color =
                when (decoration.type) {
                    // TODO: Ability to change colours in settings
                    "lifetime" -> Color(0xCC47EA54_U.toInt(), true)
                    "imm_borrow" -> Color(0xCC4762EA_U.toInt(), true)
                    "mut_borrow" -> Color(0xCCEA47EA_U.toInt(), true)
                    "call",
                    "move" -> Color(0xCCEAA647_U.toInt(), true)
                    "outlive" -> Color(0xCCEA4747_U.toInt(), true)
                    else -> {
                        logger.warn("Unexpected decoration type: ${decoration.type}")
                        return
                    }
                }.let { JBColor(it, it) }

            val textAttributes =
                TextAttributes().also {
                    it.withAdditionalEffect(EffectType.BOLD_LINE_UNDERSCORE, color)
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
