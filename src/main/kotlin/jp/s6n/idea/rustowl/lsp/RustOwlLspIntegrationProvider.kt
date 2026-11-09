package jp.s6n.idea.rustowl.lsp

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspClient
import com.intellij.platform.lsp.api.LspIntegrationProvider
import com.intellij.platform.lsp.api.lsWidget.LspClientWidgetItem
import java.io.File
import jp.s6n.idea.rustowl.RustOwlFinder
import jp.s6n.idea.rustowl.icons.RustOwlIcons
import jp.s6n.idea.rustowl.settings.RustOwlSettings
import jp.s6n.idea.rustowl.settings.RustOwlSettingsConfigurable

class RustOwlLspIntegrationProvider : LspIntegrationProvider {
    override fun fileOpened(
        project: Project,
        file: VirtualFile,
        clientStarter: LspIntegrationProvider.LspClientStarter,
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

        clientStarter.ensureClientStarted(RustOwlLspClientDescriptor(project, owlspFile))
    }

    override fun createWidgetItem(lspClient: LspClient, currentFile: VirtualFile?) =
        LspClientWidgetItem(
            lspClient,
            currentFile,
            RustOwlIcons.RustOwl,
            RustOwlSettingsConfigurable::class.java,
        )
}
