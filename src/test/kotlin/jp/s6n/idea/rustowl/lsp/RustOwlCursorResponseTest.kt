package jp.s6n.idea.rustowl.lsp

import org.eclipse.lsp4j.jsonrpc.json.MessageJsonHandler
import org.junit.Assert.assertEquals
import org.junit.Test

class RustOwlCursorResponseTest {
    private val gson = MessageJsonHandler(emptyMap()).gson

    @Test
    fun `deserializes a response from RustOwl 0_4_0`() {
        // Captured from the shape produced by RustOwl 0.4.0
        // (`crates/rustowl/src/lsp/decoration.rs`).
        val json =
            """
            {
              "is_analyzed": true,
              "status": "finished",
              "path": "/tmp/project/src/main.rs",
              "decorations": [
                {
                  "type": "lifetime",
                  "local": { "id": 1, "fn_id": 2 },
                  "range": { "start": { "line": 1, "character": 8 }, "end": { "line": 4, "character": 1 } },
                  "hover_text": "lifetime of variable `v`",
                  "overlapped": false
                },
                {
                  "type": "definitely_live",
                  "local": { "id": 1, "fn_id": 2 },
                  "range": { "start": { "line": 1, "character": 8 }, "end": { "line": 2, "character": 4 } },
                  "hover_text": "variable `v` definitely lives here",
                  "overlapped": false
                },
                {
                  "type": "maybe_initialized",
                  "local": { "id": 1, "fn_id": 2 },
                  "range": { "start": { "line": 3, "character": 4 }, "end": { "line": 4, "character": 1 } },
                  "hover_text": "variable `v` maybe lives here",
                  "overlapped": true
                }
              ]
            }
            """
                .trimIndent()

        val response = gson.fromJson(json, RustOwlCursorResponse::class.java)

        assertEquals(
            listOf("lifetime", "definitely_live", "maybe_initialized"),
            response.decorations.map { it.type },
        )

        val maybe = response.decorations[2]
        assertEquals(
            RustOwlCursorResponse.Range(
                RustOwlCursorResponse.Location(3, 4),
                RustOwlCursorResponse.Location(4, 1),
            ),
            maybe.range,
        )
        assertEquals("variable `v` maybe lives here", maybe.hover_text)
        assertEquals(true, maybe.overlapped)
    }

    @Test
    fun `serializes the cursor request as RustOwl expects`() {
        val request =
            RustOwlCursorRequest(
                RustOwlCursorRequest.Position(3, 7),
                RustOwlCursorRequest.Document("file:///tmp/project/src/main.rs"),
            )

        assertEquals(
            """{"position":{"line":3,"character":7},"document":{"uri":"file:///tmp/project/src/main.rs"}}""",
            gson.toJson(request),
        )
    }

    @Test
    fun `serializes the analyze request as an empty object`() {
        assertEquals("{}", gson.toJson(RustOwlAnalyzeRequest()))
    }
}
