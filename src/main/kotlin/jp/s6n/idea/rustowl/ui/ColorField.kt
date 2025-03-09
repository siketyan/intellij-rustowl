package jp.s6n.idea.rustowl.ui

import com.intellij.ui.ColorPanel
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.Row
import com.intellij.ui.dsl.builder.toMutableProperty
import java.awt.Color
import kotlin.reflect.KMutableProperty0

fun Row.colorField() = cell(ColorPanel())

fun Cell<ColorPanel>.withTransparency() = also { component.setSupportTransparency(true) }

fun Cell<ColorPanel>.bindColor(property: KMutableProperty0<Color>) = also {
    bind(
        { component -> component.selectedColor!! },
        { component, value -> component.selectedColor = value },
        property.toMutableProperty(),
    )
}
