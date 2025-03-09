package jp.s6n.idea.rustowl.settings

import com.intellij.openapi.components.*
import com.intellij.util.application

@Service(Service.Level.APP)
@State(name = "RustOwlSettings", storages = [(Storage("rustowl.xml"))])
class RustOwlSettings :
    SimplePersistentStateComponent<RustOwlSettingsState>(RustOwlSettingsState()) {
    var cargoOwlspPath: String
        get() = state.cargoOwlspPath ?: ""
        set(value) {
            state.cargoOwlspPath = value
        }

    companion object {
        @JvmStatic fun getInstance(): RustOwlSettings = application.service()
    }
}
