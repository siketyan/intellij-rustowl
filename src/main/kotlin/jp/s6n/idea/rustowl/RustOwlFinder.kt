package jp.s6n.idea.rustowl

import com.intellij.execution.configurations.PathEnvironmentVariableUtil
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.util.application
import java.io.File

@Service(Service.Level.APP)
class RustOwlFinder {
    fun find(): File? = PathEnvironmentVariableUtil.findExecutableInPathOnAnyOS("rustowl")

    companion object {
        @JvmStatic fun getInstance(): RustOwlFinder = application.service()
    }
}
