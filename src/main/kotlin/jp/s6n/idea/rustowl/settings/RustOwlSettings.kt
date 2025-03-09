package jp.s6n.idea.rustowl.settings

import com.intellij.openapi.components.*
import com.intellij.ui.ColorUtil
import com.intellij.util.application
import java.awt.Color

@Service(Service.Level.APP)
@State(name = "RustOwlSettings", storages = [(Storage("rustowl.xml"))])
class RustOwlSettings :
    SimplePersistentStateComponent<RustOwlSettingsState>(RustOwlSettingsState()) {
    var cargoOwlspPath: String
        get() = state.cargoOwlspPath ?: ""
        set(value) {
            state.cargoOwlspPath = value
        }

    var lifetimeColor: Color
        get() = ColorUtil.fromHex(state.lifetimeColor, null) ?: ColorUtil.fromHex("47EA54CC")
        set(value) {
            state.lifetimeColor = ColorUtil.toHex(value, true)
        }

    var immutableBorrowingColor: Color
        get() =
            ColorUtil.fromHex(state.immutableBorrowingColor, null) ?: ColorUtil.fromHex("4762EACC")
        set(value) {
            state.immutableBorrowingColor = ColorUtil.toHex(value, true)
        }

    var mutableBorrowingColor: Color
        get() =
            ColorUtil.fromHex(state.mutableBorrowingColor, null) ?: ColorUtil.fromHex("EA47EACC")
        set(value) {
            state.mutableBorrowingColor = ColorUtil.toHex(value, true)
        }

    var valueMovedColor: Color
        get() = ColorUtil.fromHex(state.valueMovedColor, null) ?: ColorUtil.fromHex("EAA647CC")
        set(value) {
            state.valueMovedColor = ColorUtil.toHex(value, true)
        }

    var lifetimeErrorColor: Color
        get() = ColorUtil.fromHex(state.lifetimeErrorColor, null) ?: ColorUtil.fromHex("EA4747CC")
        set(value) {
            state.lifetimeErrorColor = ColorUtil.toHex(value, true)
        }

    companion object {
        @JvmStatic fun getInstance(): RustOwlSettings = application.service()
    }
}
