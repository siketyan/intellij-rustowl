package jp.s6n.idea.rustowl

import com.intellij.execution.configurations.PathEnvironmentVariableUtil
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.util.SystemInfo
import com.intellij.util.application
import java.io.File

@Service(Service.Level.APP)
class RustOwlFinder {
    fun find(): File? = find(PathEnvironmentVariableUtil.getPathVariableValue())

    // PathEnvironmentVariableUtil.findExecutableInPathOnAnyOS and its friends are deprecated since
    // 2026.3, and the replacement (findFirst) is not available on older IDEs. Look up the PATH by
    // ourselves until we drop support for them.
    internal fun find(pathVariable: String?): File? {
        if (pathVariable.isNullOrEmpty()) return null

        val names = if (SystemInfo.isWindows) listOf("$NAME.exe", NAME) else listOf(NAME)

        return PathEnvironmentVariableUtil.getPathDirs(pathVariable)
            .asSequence()
            .flatMap { dir -> names.asSequence().map { File(dir, it) } }
            .firstOrNull { it.isFile && it.canExecute() }
    }

    companion object {
        private const val NAME = "rustowl"

        @JvmStatic fun getInstance(): RustOwlFinder = application.service()
    }
}
