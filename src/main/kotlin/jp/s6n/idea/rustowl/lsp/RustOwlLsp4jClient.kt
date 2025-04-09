package jp.s6n.idea.rustowl.lsp

import com.intellij.openapi.project.Project
import com.intellij.platform.lsp.api.Lsp4jClient
import com.intellij.platform.lsp.api.LspServerNotificationsHandler

class RustOwlLsp4jClient(
    project: Project,
    serverNotificationsHandler: LspServerNotificationsHandler,
) : Lsp4jClient(RustOwlLspNotificationHandler(project, serverNotificationsHandler))
