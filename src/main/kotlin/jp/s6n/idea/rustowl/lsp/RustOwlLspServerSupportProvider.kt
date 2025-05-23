package jp.s6n.idea.rustowl.lsp

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspServer
import com.intellij.platform.lsp.api.LspServerSupportProvider
import com.intellij.platform.lsp.api.lsWidget.LspServerWidgetItem
import java.io.File
import jp.s6n.idea.rustowl.RustOwlFinder
import jp.s6n.idea.rustowl.icons.RustOwlIcons
import jp.s6n.idea.rustowl.settings.RustOwlSettings
import jp.s6n.idea.rustowl.settings.RustOwlSettingsConfigurable

class RustOwlLspServerSupportProvider : LspServerSupportProvider {
    override fun fileOpened(
        project: Project,
        file: VirtualFile,
        serverStarter: LspServerSupportProvider.LspServerStarter,
    ) {
        if (file.extension != "rs") return

        val owlspFile =
            RustOwlSettings.getInstance().rustowlPath.ifEmpty { null }?.let { File(it) }
                ?: RustOwlFinder.getInstance().find()
                ?: return Notifications.Bus.notify(
                    Notification(
                        "RustOwl",
                        "RustOwl is not installed",
                        "Could not find rustowl in the PATH.",
                        NotificationType.ERROR,
                    )
                )

        serverStarter.ensureServerStarted(RustOwlLspServerDescriptor(project, owlspFile))
    }

    override fun createLspServerWidgetItem(lspServer: LspServer, currentFile: VirtualFile?) =
        LspServerWidgetItem(
            lspServer,
            currentFile,
            RustOwlIcons.RustOwl,
            RustOwlSettingsConfigurable::class.java,
        )
}
