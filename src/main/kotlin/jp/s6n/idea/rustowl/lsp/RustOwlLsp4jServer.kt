package jp.s6n.idea.rustowl.lsp

import org.eclipse.lsp4j.jsonrpc.services.JsonRequest
import org.eclipse.lsp4j.services.LanguageServer
import java.util.concurrent.CompletableFuture

interface RustOwlLsp4jServer : LanguageServer {
    @JsonRequest("rustowl/cursor")
    fun cursor(request: RustOwlCursorRequest): CompletableFuture<RustOwlCursorResponse>
}
