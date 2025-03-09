package jp.s6n.idea.rustowl.settings

import com.intellij.openapi.components.BaseState

class RustOwlSettingsState : BaseState() {
    var cargoOwlspPath by string()

    var lifetimeColor by string()
    var immutableBorrowingColor by string()
    var mutableBorrowingColor by string()
    var valueMovedColor by string()
    var lifetimeErrorColor by string()
}
