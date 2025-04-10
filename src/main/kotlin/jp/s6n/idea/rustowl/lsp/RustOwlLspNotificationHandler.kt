package jp.s6n.idea.rustowl.lsp

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.platform.lsp.api.LspServerNotificationsHandler
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.runBlocking
import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.jsonrpc.messages.Either

sealed interface Progress {
    data class InProgress(val text: String, val percentage: Int) : Progress

    data object Done : Progress
}

class RustOwlLspNotificationHandler(
    private val project: Project,
    private val delegate: LspServerNotificationsHandler,
    private val progress: MutableMap<Either<String, Int>, Channel<Progress>> = mutableMapOf(),
) : LspServerNotificationsHandler {
    override fun applyEdit(params: ApplyWorkspaceEditParams) = delegate.applyEdit(params)

    override fun configuration(params: ConfigurationParams) = delegate.configuration(params)

    override fun createProgress(params: WorkDoneProgressCreateParams) =
        delegate.createProgress(params)

    override fun logMessage(params: MessageParams) = delegate.logMessage(params)

    override fun logTrace(params: LogTraceParams) = delegate.logTrace(params)

    override fun publishDiagnostics(params: PublishDiagnosticsParams) =
        delegate.publishDiagnostics(params)

    override fun refreshCodeLenses() = delegate.refreshCodeLenses()

    override fun refreshDiagnostics() = delegate.refreshDiagnostics()

    override fun refreshInlayHints() = delegate.refreshInlayHints()

    override fun refreshInlineValues() = delegate.refreshInlineValues()

    override fun refreshSemanticTokens() = delegate.refreshSemanticTokens()

    override fun registerCapability(params: RegistrationParams) =
        delegate.registerCapability(params)

    override fun showDocument(params: ShowDocumentParams) = delegate.showDocument(params)

    override fun showMessage(params: MessageParams) = delegate.showMessage(params)

    override fun showMessageRequest(params: ShowMessageRequestParams) =
        delegate.showMessageRequest(params)

    override fun telemetryEvent(`object`: Any) = delegate.telemetryEvent(`object`)

    override fun unregisterCapability(params: UnregistrationParams) =
        delegate.unregisterCapability(params)

    override fun workspaceFolders() = delegate.workspaceFolders()

    override fun notifyProgress(params: ProgressParams) {
        if (!params.value.isLeft) {
            return
        }

        val value = params.value.left
        when (value.kind!!) {
            WorkDoneProgressKind.begin -> {
                val channel = Channel<Progress>()
                val payload = value as WorkDoneProgressBegin

                progress[params.token] = channel

                // I know this method is deprecated now, but the IntelliJ API doesn't allow us to set the custom
                // fraction to the progress indicator. We don't do anything in our thread, just mirroring the progress
                // reported by the LSP server. Using reportRawProgress was an option, but it's an internal API.
                ProgressManager.getInstance().run(object : Task.Backgroundable(project, payload.title) {
                    override fun run(indicator: ProgressIndicator) {
                        runBlocking {
                            while (true) {
                                when (val progress = channel.receive()) {
                                    is Progress.InProgress -> {
                                        indicator.text = progress.text
                                        indicator.fraction = progress.percentage / 100.0
                                    }

                                    is Progress.Done -> {
                                        break
                                    }
                                }
                            }
                        }
                    }
                })
            }

            WorkDoneProgressKind.report -> {
                val payload = value as WorkDoneProgressReport
                val channel = progress[params.token] ?: return

                channel.trySendBlocking(Progress.InProgress(payload.message, payload.percentage))
            }

            WorkDoneProgressKind.end -> {
                val channel = progress[params.token] ?: return

                channel.trySendBlocking(Progress.Done)
            }
        }
    }
}
