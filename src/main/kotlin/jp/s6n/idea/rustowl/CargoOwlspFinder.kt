package jp.s6n.idea.rustowl

import com.intellij.execution.configurations.PathEnvironmentVariableUtil
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.util.application
import java.io.File

@Service(Service.Level.APP)
class CargoOwlspFinder {
    fun find(): File? = PathEnvironmentVariableUtil.findExecutableInPathOnAnyOS("cargo-owlsp")

    companion object {
        @JvmStatic fun getInstance(): CargoOwlspFinder = application.service()
    }
}
