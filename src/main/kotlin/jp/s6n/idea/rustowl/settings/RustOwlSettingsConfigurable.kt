package jp.s6n.idea.rustowl.settings

import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.emptyText
import com.intellij.ui.dsl.builder.*
import jp.s6n.idea.rustowl.CargoOwlspFinder

class RustOwlSettingsConfigurable :
    BoundSearchableConfigurable("RustOwl", "RustOwl", "Settings.RustOwl") {
    private val settings = RustOwlSettings.getInstance()

    override fun createPanel(): DialogPanel = panel {
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
}
