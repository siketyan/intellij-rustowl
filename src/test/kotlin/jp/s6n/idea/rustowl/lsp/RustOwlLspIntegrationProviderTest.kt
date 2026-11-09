package jp.s6n.idea.rustowl.lsp

import com.intellij.notification.Notification
import com.intellij.notification.Notifications
import com.intellij.openapi.application.ApplicationManager
import com.intellij.platform.lsp.api.LspClientDescriptor
import com.intellij.platform.lsp.api.LspIntegrationProvider
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import jp.s6n.idea.rustowl.RustOwlFinder
import jp.s6n.idea.rustowl.settings.RustOwlSettings

class RustOwlLspIntegrationProviderTest : BasePlatformTestCase() {
    private val startedDescriptors = mutableListOf<LspClientDescriptor>()
    private val notifications = mutableListOf<Notification>()

    private val clientStarter =
        object : LspIntegrationProvider.LspClientStarter {
            override fun ensureClientStarted(descriptor: LspClientDescriptor) {
                startedDescriptors.add(descriptor)
            }
        }

    override fun setUp() {
        super.setUp()

        // Notifications without a project are published to the application message bus.
        ApplicationManager.getApplication()
            .messageBus
            .connect(testRootDisposable)
            .subscribe(
                Notifications.TOPIC,
                object : Notifications {
                    override fun notify(notification: Notification) {
                        notifications.add(notification)
                    }
                },
            )
    }

    override fun tearDown() {
        try {
            RustOwlSettings.getInstance().rustowlPath = ""
        } finally {
            super.tearDown()
        }
    }

    fun testStartsClientWithConfiguredPath() {
        RustOwlSettings.getInstance().rustowlPath = "/path/to/rustowl"
        val file = myFixture.addFileToProject("src/main.rs", "").virtualFile

        RustOwlLspIntegrationProvider().fileOpened(project, file, clientStarter)

        val descriptor = startedDescriptors.single()
        assertInstanceOf(descriptor, RustOwlLspClientDescriptor::class.java)
        assertEquals("/path/to/rustowl", descriptor.createCommandLine().exePath)
        assertEmpty(notifications)
    }

    fun testIgnoresNonRustFiles() {
        RustOwlSettings.getInstance().rustowlPath = "/path/to/rustowl"
        val file = myFixture.addFileToProject("Cargo.toml", "").virtualFile

        RustOwlLspIntegrationProvider().fileOpened(project, file, clientStarter)

        assertEmpty(startedDescriptors)
        assertEmpty(notifications)
    }

    fun testNotifiesWhenRustOwlIsNotFound() {
        // Depends on the environment: skip if rustowl is installed on the PATH.
        if (RustOwlFinder.getInstance().find() != null) return

        val file = myFixture.addFileToProject("src/main.rs", "").virtualFile

        RustOwlLspIntegrationProvider().fileOpened(project, file, clientStarter)

        assertEmpty(startedDescriptors)
        assertEquals("RustOwl is not installed", notifications.single().title)
    }
}
