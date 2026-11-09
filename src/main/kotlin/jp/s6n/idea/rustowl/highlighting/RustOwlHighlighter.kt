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
            server.sendRequestSync(10_000) {
                (it as RustOwlLsp4jServer).cursor(
                    RustOwlCursorRequest(
                        RustOwlCursorRequest.Position(position.line, position.column),
                        RustOwlCursorRequest.Document(file.url),
                    )
                )
            } ?: return

        logger.debug("RustOwl response: $response")

        // RustOwl 0.4.0 or later reports `definitely_live` and `maybe_initialized` computed by the
        // CFG-based analysis, and keeps reporting `lifetime` only for backward compatibility. Prefer
        // the new ones if available, falling back to `lifetime` for older servers.
        val hasPreciseLifetimes =
            response.decorations.any {
                it.type == "definitely_live" || it.type == "maybe_initialized"
            }

        response.decorations.forEach { decoration ->
            if (decoration.overlapped) {
                return@forEach
            }

            val (color, effectType) =
                when (decoration.type) {
                    "lifetime" -> {
                        if (hasPreciseLifetimes) return@forEach
                        settings.lifetimeColor to EffectType.BOLD_LINE_UNDERSCORE
                    }
                    "definitely_live" -> settings.lifetimeColor to EffectType.BOLD_LINE_UNDERSCORE
                    "maybe_initialized" ->
                        settings.maybeInitializedColor to EffectType.WAVE_UNDERSCORE
                    "imm_borrow" ->
                        settings.immutableBorrowingColor to EffectType.BOLD_LINE_UNDERSCORE
                    "mut_borrow" ->
                        settings.mutableBorrowingColor to EffectType.BOLD_LINE_UNDERSCORE
                    "call",
                    "move" -> settings.valueMovedColor to EffectType.BOLD_LINE_UNDERSCORE
                    "shared_mut",
                    "outlive" -> settings.lifetimeErrorColor to EffectType.WAVE_UNDERSCORE
                    else -> {
                        logger.warn("Unexpected decoration type: ${decoration.type}")
                        return@forEach
                    }
                }

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
