package jp.s6n.idea.rustowl.event

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileDocumentManagerListener
import com.intellij.openapi.project.ProjectLocator
import com.intellij.platform.lsp.api.LspServerManager
import jp.s6n.idea.rustowl.lsp.RustOwlAnalyzeRequest
import jp.s6n.idea.rustowl.lsp.RustOwlLsp4jServer
import jp.s6n.idea.rustowl.lsp.RustOwlLspServerSupportProvider

/**
 * Asks RustOwl to re-analyze the workspace when a Rust file is saved.
 *
 * RustOwl discards its analysis results on every `textDocument/didChange`, and does not re-analyze
 * by itself on `textDocument/didSave`, so we need to request it explicitly as the official
 * extensions do.
 */
class DocumentSavedListener : FileDocumentManagerListener {
    private val logger = Logger.getInstance(this.javaClass)

    override fun afterDocumentSaved(document: Document) {
        val file = FileDocumentManager.getInstance().getFile(document) ?: return
        if (file.extension != "rs") return

        val project = ProjectLocator.getInstance().guessProjectForFile(file) ?: return

        LspServerManager.getInstance(project)
            .getServersForProvider(RustOwlLspServerSupportProvider::class.java)
            .forEach { server ->
                server.sendNotification { lsp4jServer ->
                    (lsp4jServer as RustOwlLsp4jServer).analyze(RustOwlAnalyzeRequest())
                        .exceptionally {
                            logger.warn("rustowl/analyze request failed", it)
                            null
                        }
                }
            }
    }
}
