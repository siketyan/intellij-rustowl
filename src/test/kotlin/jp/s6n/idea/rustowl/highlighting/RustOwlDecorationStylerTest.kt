package jp.s6n.idea.rustowl.highlighting

import com.intellij.openapi.editor.markup.EffectType
import java.awt.Color
import jp.s6n.idea.rustowl.lsp.RustOwlCursorResponse
import org.junit.Assert.assertEquals
import org.junit.Test

class RustOwlDecorationStylerTest {
    private val palette =
        RustOwlPalette(
            lifetime = Color(1, 0, 0),
            maybeInitialized = Color(2, 0, 0),
            immutableBorrowing = Color(3, 0, 0),
            mutableBorrowing = Color(4, 0, 0),
            valueMoved = Color(5, 0, 0),
            lifetimeError = Color(6, 0, 0),
        )

    private val styler = RustOwlDecorationStyler(palette)

    private fun deco(type: String, line: Int = 0, overlapped: Boolean = false) =
        RustOwlCursorResponse.Decoration(
            type = type,
            range =
                RustOwlCursorResponse.Range(
                    RustOwlCursorResponse.Location(line, 0),
                    RustOwlCursorResponse.Location(line, 1),
                ),
            hover_text = "",
            overlapped = overlapped,
        )

    private fun List<StyledDecoration>.summary() = map {
        Triple(it.decoration.type, it.color, it.effectType)
    }

    @Test
    fun `styles every known decoration type`() {
        val result =
            styler.style(
                listOf(
                    deco("definitely_live"),
                    deco("maybe_initialized"),
                    deco("imm_borrow"),
                    deco("mut_borrow"),
                    deco("move"),
                    deco("call"),
                    deco("shared_mut"),
                    deco("outlive"),
                ),
            )

        assertEquals(
            listOf(
                Triple("definitely_live", palette.lifetime, EffectType.BOLD_LINE_UNDERSCORE),
                Triple("maybe_initialized", palette.maybeInitialized, EffectType.WAVE_UNDERSCORE),
                Triple("imm_borrow", palette.immutableBorrowing, EffectType.BOLD_LINE_UNDERSCORE),
                Triple("mut_borrow", palette.mutableBorrowing, EffectType.BOLD_LINE_UNDERSCORE),
                Triple("move", palette.valueMoved, EffectType.BOLD_LINE_UNDERSCORE),
                Triple("call", palette.valueMoved, EffectType.BOLD_LINE_UNDERSCORE),
                Triple("shared_mut", palette.lifetimeError, EffectType.WAVE_UNDERSCORE),
                Triple("outlive", palette.lifetimeError, EffectType.WAVE_UNDERSCORE),
            ),
            result.summary(),
        )
    }

    @Test
    fun `ignores legacy lifetime when precise lifetimes are reported`() {
        val result =
            styler.style(listOf(deco("lifetime"), deco("definitely_live"), deco("imm_borrow")))

        assertEquals(listOf("definitely_live", "imm_borrow"), result.map { it.decoration.type })
    }

    @Test
    fun `ignores legacy lifetime when only maybe initialized is reported`() {
        val result = styler.style(listOf(deco("lifetime"), deco("maybe_initialized")))

        assertEquals(listOf("maybe_initialized"), result.map { it.decoration.type })
    }

    @Test
    fun `falls back to legacy lifetime for older servers`() {
        val result = styler.style(listOf(deco("lifetime"), deco("mut_borrow")))

        assertEquals(
            listOf(
                Triple("lifetime", palette.lifetime, EffectType.BOLD_LINE_UNDERSCORE),
                Triple("mut_borrow", palette.mutableBorrowing, EffectType.BOLD_LINE_UNDERSCORE),
            ),
            result.summary(),
        )
    }

    @Test
    fun `skips overlapped decorations`() {
        val result =
            styler.style(listOf(deco("definitely_live", overlapped = true), deco("move", line = 1)))

        assertEquals(listOf("move"), result.map { it.decoration.type })
    }

    @Test
    fun `skips unknown types without dropping the remaining decorations`() {
        val unknown = mutableListOf<String>()

        val result =
            styler.style(listOf(deco("something_new"), deco("move"), deco("outlive"))) {
                unknown.add(it)
            }

        assertEquals(listOf("something_new"), unknown)
        assertEquals(listOf("move", "outlive"), result.map { it.decoration.type })
    }
}
