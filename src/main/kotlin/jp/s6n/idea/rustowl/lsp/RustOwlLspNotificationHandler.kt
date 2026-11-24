package jp.s6n.idea.rustowl.lsp

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.platform.lsp.api.LspServerNotificationsHandler
import jp.s6n.idea.rustowl.highlighting.RustOwlHighlighter
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.runBlocking
import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.jsonrpc.messages.Either

sealed interface Progress {
    data class InProgress(val text: String, val percentage: Int) : Progress

    data object Done : Progress
}

/** Mirrors the progress received from [channel] to [indicator] until [Progress.Done] arrives. */
internal suspend fun mirrorProgress(
    channel: ReceiveChannel<Progress>,
    indicator: ProgressIndicator,
) {
    while (true) {
        when (val progress = channel.receive()) {
            is Progress.InProgress -> {
                // Stay indeterminate until the server reports the actual progress. The platform
                // logs an IllegalStateException on setFraction unless we opt out beforehand.
                indicator.isIndeterminate = false
                indicator.text = progress.text
                indicator.fraction = progress.percentage / 100.0
            }

            is Progress.Done -> break
        }
    }
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

                // I know this method is deprecated now, but the IntelliJ API doesn't allow us to
                // set the custom fraction to the progress indicator. We don't do anything in our
                // thread, just mirroring the progress reported by the LSP server. Using
                // reportRawProgress was an option, but it's an internal API.
                val task =
                    object : Task.Backgroundable(project, payload.title) {
                        override fun run(indicator: ProgressIndicator) {
                            runBlocking { mirrorProgress(channel, indicator) }
                        }
                    }

                ProgressManager.getInstance().run(task)
            }

            WorkDoneProgressKind.report -> {
                val payload = value as WorkDoneProgressReport
                val channel = progress[params.token] ?: return

                channel.trySendBlocking(Progress.InProgress(payload.message, payload.percentage))
            }

            WorkDoneProgressKind.end -> {
                progress.remove(params.token)?.trySendBlocking(Progress.Done)

                rehighlight()
            }
        }
    }

    /**
     * Highlights the selected editors again at their current caret positions.
     *
     * The cursor request sent while the analysis is running returns no decorations, and nothing
     * triggers another request until the caret moves, so we request it again once the analysis is
     * finished.
     */
    private fun rehighlight() {
        val application = ApplicationManager.getApplication()

        application.invokeLater(
            {
                val targets =
                    FileEditorManager.getInstance(project)
                        .selectedEditors
                        .filterIsInstance<TextEditor>()
                        .mapNotNull { textEditor ->
                            val editor = textEditor.editor
                            editor.getUserData(RustOwlHighlighter.KEY)?.let {
                                it to editor.caretModel.logicalPosition
                            }
                        }

                if (targets.isEmpty()) return@invokeLater

                // Highlighting sends a request synchronously, so avoid blocking the EDT.
                application.executeOnPooledThread {
                    targets.forEach { (highlighter, position) -> highlighter.highlight(position) }
                }
            },
            project.disposed,
        )
    }
}
