package jp.s6n.idea.rustowl.settings

import com.intellij.openapi.components.BaseState

class RustOwlSettingsState : BaseState() {
    var cargoOwlspPath by string()
}
