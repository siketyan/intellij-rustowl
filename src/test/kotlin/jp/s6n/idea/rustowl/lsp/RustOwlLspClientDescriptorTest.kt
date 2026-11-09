package jp.s6n.idea.rustowl.lsp

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.io.File

class RustOwlLspClientDescriptorTest : BasePlatformTestCase() {
    private val rustowl = File("/path/to/rustowl")

    private fun createDescriptor() = RustOwlLspClientDescriptor(project, rustowl)

    fun testSupportedFiles() {
        val descriptor = createDescriptor()
        val rs = myFixture.addFileToProject("src/main.rs", "").virtualFile
        val toml = myFixture.addFileToProject("Cargo.toml", "").virtualFile

        assertTrue(descriptor.isSupportedFile(rs))
        assertFalse(descriptor.isSupportedFile(toml))
    }

    fun testLanguageId() {
        val rs = myFixture.addFileToProject("src/main.rs", "").virtualFile

        assertEquals("rust", createDescriptor().getLanguageId(rs))
    }

    fun testCommandLineRunsRustOwlViaStdio() {
        val commandLine = createDescriptor().createCommandLine()

        assertEquals(rustowl.path, commandLine.exePath)
        assertEquals(listOf("--stdio"), commandLine.parametersList.parameters)
    }

    fun testWorkDoneProgressIsEnabled() {
        assertTrue(createDescriptor().clientCapabilities.window.workDoneProgress)
    }

    fun testLsp4jServerClass() {
        assertEquals(RustOwlLsp4jServer::class.java, createDescriptor().lsp4jServerClass)
    }
}
