package jp.s6n.idea.rustowl.lsp

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspServerSupportProvider
import java.io.File
import jp.s6n.idea.rustowl.CargoOwlspFinder
import jp.s6n.idea.rustowl.settings.RustOwlSettings

class RustOwlLspServerSupportProvider : LspServerSupportProvider {
    override fun fileOpened(
        project: Project,
        file: VirtualFile,
        serverStarter: LspServerSupportProvider.LspServerStarter,
    ) {
        if (file.extension != "rs") return

        val owlspFile =
            RustOwlSettings.getInstance().cargoOwlspPath.ifEmpty { null }?.let { File(it) }
                ?: CargoOwlspFinder.getInstance().find()
                ?: return Notifications.Bus.notify(
                    Notification(
                        "RustOwl",
                        "RustOwl is not installed",
                        "Could not find cargo-owlsp in the PATH.",
                        NotificationType.ERROR,
                    )
                )

        serverStarter.ensureServerStarted(RustOwlLspServerDescriptor(project, owlspFile))
    }
}
