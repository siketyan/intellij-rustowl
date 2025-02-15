package jp.s6n.idea.rustowl.lsp

import java.util.concurrent.CompletableFuture
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest
import org.eclipse.lsp4j.services.LanguageServer

interface RustOwlLsp4jServer : LanguageServer {
    @JsonRequest("rustowl/cursor")
    fun cursor(request: RustOwlCursorRequest): CompletableFuture<RustOwlCursorResponse>
}
