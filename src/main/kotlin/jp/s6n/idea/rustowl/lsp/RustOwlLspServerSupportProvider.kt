package jp.s6n.idea.rustowl.lsp

import com.intellij.execution.configurations.PathEnvironmentVariableUtil
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspServerSupportProvider

@Suppress("UnstableApiUsage")
class RustOwlLspServerSupportProvider : LspServerSupportProvider {
    override fun fileOpened(
        project: Project,
        file: VirtualFile,
        serverStarter: LspServerSupportProvider.LspServerStarter
    ) {
        if (file.extension != "rs") return

        // TODO: Customise location of cargo-owlsp
        val owlspFile = PathEnvironmentVariableUtil.findExecutableInPathOnAnyOS("cargo-owlsp")
            ?: return Notifications.Bus.notify(
                Notification(
                    "RustOwl",
                    "RustOwl is not installed",
                    "Could not find cargo-owlsp in the PATH.",
                    NotificationType.ERROR
                )
            )

        serverStarter.ensureServerStarted(RustOwlLspServerDescriptor(project, owlspFile))
    }
}
