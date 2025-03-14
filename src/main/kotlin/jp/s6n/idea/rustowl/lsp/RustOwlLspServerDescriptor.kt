package jp.s6n.idea.rustowl.lsp

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.ProjectWideLspServerDescriptor
import java.io.File

class RustOwlLspServerDescriptor(project: Project, private val owlspFile: File) :
    ProjectWideLspServerDescriptor(project, "RustOwl") {
    override val lsp4jServerClass = RustOwlLsp4jServer::class.java

    override fun isSupportedFile(file: VirtualFile) = file.extension == "rs"

    override fun getLanguageId(file: VirtualFile) = "rust"

    override fun createCommandLine() = GeneralCommandLine(owlspFile.path)
}
