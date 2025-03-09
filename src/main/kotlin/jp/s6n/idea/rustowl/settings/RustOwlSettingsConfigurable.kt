package jp.s6n.idea.rustowl.settings

import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.emptyText
import com.intellij.ui.dsl.builder.*
import jp.s6n.idea.rustowl.CargoOwlspFinder
import jp.s6n.idea.rustowl.ui.*

class RustOwlSettingsConfigurable :
    BoundSearchableConfigurable("RustOwl", "RustOwl", "Settings.RustOwl") {
    private val settings = RustOwlSettings.getInstance()

    override fun createPanel(): DialogPanel = panel {
        group("RustOwl Settings") {
            row("Path to cargo-owlsp:") {
                textFieldWithBrowseButton("Choose cargo-owlsp executable")
                    .align(Align.FILL)
                    .bindText(settings::cargoOwlspPath)
                    .run {
                        component.emptyText.setText(
                            CargoOwlspFinder.getInstance().find()?.let { "Auto-detected: $it" }
                                ?: "Could not auto-detect cargo-owlsp from the PATH"
                        )
                    }
            }
        }
        group("Color Settings") {
            row("Variable's Actual Lifetime:") {
                colorField().withTransparency().bindColor(settings::lifetimeColor)
            }
            row("Immutable Borrowing:") {
                colorField().withTransparency().bindColor(settings::immutableBorrowingColor)
            }
            row("Mutable Borrowing:") {
                colorField().withTransparency().bindColor(settings::mutableBorrowingColor)
            }
            row("Value Moved / Function Call:") {
                colorField().withTransparency().bindColor(settings::valueMovedColor)
            }
            row("Lifetime Error:") {
                colorField().withTransparency().bindColor(settings::lifetimeErrorColor)
            }
        }
    }
}
