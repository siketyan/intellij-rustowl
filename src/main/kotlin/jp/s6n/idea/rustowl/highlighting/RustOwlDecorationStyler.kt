package jp.s6n.idea.rustowl.highlighting

import com.intellij.openapi.editor.markup.EffectType
import java.awt.Color
import jp.s6n.idea.rustowl.lsp.RustOwlCursorResponse

/** Colors used to render each kind of decoration. */
data class RustOwlPalette(
    val lifetime: Color,
    val maybeInitialized: Color,
    val immutableBorrowing: Color,
    val mutableBorrowing: Color,
    val valueMoved: Color,
    val lifetimeError: Color,
)

/** Resolved style of a decoration to render. */
data class StyledDecoration(
    val decoration: RustOwlCursorResponse.Decoration,
    val color: Color,
    val effectType: EffectType,
)

/** Decides which decorations to render and how. */
class RustOwlDecorationStyler(private val palette: RustOwlPalette) {
    /**
     * Returns the decorations to render with their styles. Overlapped and unknown decorations are
     * skipped; unknown types are passed to [onUnknown].
     *
     * RustOwl 0.4.0 or later reports `definitely_live` and `maybe_initialized` computed by the
     * CFG-based analysis, and keeps reporting `lifetime` only for backward compatibility. Prefer
     * the new ones if available, falling back to `lifetime` for older servers.
     */
    fun style(
        decorations: List<RustOwlCursorResponse.Decoration>,
        onUnknown: (String) -> Unit = {},
    ): List<StyledDecoration> {
        val hasPreciseLifetimes = decorations.any {
            it.type == DEFINITELY_LIVE || it.type == MAYBE_INITIALIZED
        }

        return decorations.mapNotNull { decoration ->
            if (decoration.overlapped) return@mapNotNull null

            val (color, effectType) =
                when (decoration.type) {
                    LIFETIME -> {
                        if (hasPreciseLifetimes) return@mapNotNull null
                        palette.lifetime to EffectType.BOLD_LINE_UNDERSCORE
                    }
                    DEFINITELY_LIVE -> palette.lifetime to EffectType.BOLD_LINE_UNDERSCORE
                    MAYBE_INITIALIZED -> palette.maybeInitialized to EffectType.WAVE_UNDERSCORE
                    "imm_borrow" -> palette.immutableBorrowing to EffectType.BOLD_LINE_UNDERSCORE
                    "mut_borrow" -> palette.mutableBorrowing to EffectType.BOLD_LINE_UNDERSCORE
                    "call",
                    "move" -> palette.valueMoved to EffectType.BOLD_LINE_UNDERSCORE
                    "shared_mut",
                    "outlive" -> palette.lifetimeError to EffectType.WAVE_UNDERSCORE
                    else -> {
                        onUnknown(decoration.type)
                        return@mapNotNull null
                    }
                }

            StyledDecoration(decoration, color, effectType)
        }
    }

    private companion object {
        const val LIFETIME = "lifetime"
        const val DEFINITELY_LIVE = "definitely_live"
        const val MAYBE_INITIALIZED = "maybe_initialized"
    }
}
