package jp.s6n.idea.rustowl.lsp

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspServerNotificationsHandler
import com.intellij.platform.lsp.api.ProjectWideLspServerDescriptor
import java.io.File
import org.eclipse.lsp4j.ClientCapabilities

class RustOwlLspServerDescriptor(project: Project, private val owlspFile: File) :
    ProjectWideLspServerDescriptor(project, "RustOwl") {
    override val lsp4jServerClass = RustOwlLsp4jServer::class.java

    override val clientCapabilities: ClientCapabilities
        get() = super.clientCapabilities.apply { window.workDoneProgress = true }

    override fun isSupportedFile(file: VirtualFile) = file.extension == "rs"

    override fun getLanguageId(file: VirtualFile) = "rust"

    override fun createCommandLine() =
        GeneralCommandLine(owlspFile.path).apply { addParameter("--stdio") }

    override fun createLsp4jClient(handler: LspServerNotificationsHandler) =
        RustOwlLsp4jClient(project, handler)
}
