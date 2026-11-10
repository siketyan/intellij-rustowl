package jp.s6n.idea.rustowl.highlighting

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.LogicalPosition
import com.intellij.openapi.editor.markup.*
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.platform.lsp.api.LspClientManager
import com.intellij.ui.JBColor
import jp.s6n.idea.rustowl.lsp.RustOwlCursorRequest
import jp.s6n.idea.rustowl.lsp.RustOwlLsp4jServer
import jp.s6n.idea.rustowl.lsp.RustOwlLspIntegrationProvider
import jp.s6n.idea.rustowl.settings.RustOwlSettings

class RustOwlHighlighter(private val editor: Editor) {
    private val logger = Logger.getInstance(this.javaClass)
    private val settings = RustOwlSettings.getInstance()
    private val lastHighlighters = mutableListOf<RangeHighlighter>()

    fun highlight(position: LogicalPosition) {
        logger.debug("Highlighting lifetimes at $position")

        lastHighlighters.forEach { editor.markupModel.removeHighlighter(it) }

        val project = editor.project?.takeUnless { it.isDefault } ?: return
        val file = FileDocumentManager.getInstance().getFile(editor.document) ?: return
        val client =
            LspClientManager.getInstance(project)
                .getClients(RustOwlLspIntegrationProvider::class.java)
                .firstOrNull() ?: return

        val response =
            client.sendRequestSync(10_000) {
                (it as RustOwlLsp4jServer).cursor(
                    RustOwlCursorRequest(
                        RustOwlCursorRequest.Position(position.line, position.column),
                        RustOwlCursorRequest.Document(file.url),
                    ),
                )
            } ?: return

        logger.debug("RustOwl response: $response")

        val palette =
            RustOwlPalette(
                lifetime = settings.lifetimeColor,
                maybeInitialized = settings.maybeInitializedColor,
                immutableBorrowing = settings.immutableBorrowingColor,
                mutableBorrowing = settings.mutableBorrowingColor,
                valueMoved = settings.valueMovedColor,
                lifetimeError = settings.lifetimeErrorColor,
            )

        RustOwlDecorationStyler(palette)
            .style(response.decorations) { logger.warn("Unexpected decoration type: $it") }
            .forEach { (decoration, color, effectType) ->
                val textAttributes =
                    TextAttributes().also {
                        it.effectType = effectType
                        it.effectColor = JBColor(color, color)
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
