package com.mknlabs.expensetracker.core.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Pins where the system bar icon colour is declared.
 *
 * The icons read dark in light mode only if the window is *built* from a theme that says so:
 * the framework applies `android:windowLightStatusBar` when it creates the window, and it can
 * land after the runtime call in `ApplySystemBarStyle`, which is why a runtime-only fix showed
 * up as white icons on a light screen while dark mode looked fine. The splash theme needs the
 * same value because the splash window is created from it before the activity theme is applied.
 *
 * A screenshot cannot say which layer set the icons; this says both theme variants declare it,
 * in both styles, so the light window is right even before Compose draws.
 */
class SystemBarIconThemeTest {

    @Test
    fun `the light and dark themes declare the system bar icon colours`() {
        assertDeclared("values", lightStatusBar = "true")
        assertDeclared("values-night", lightStatusBar = "false")
    }

    private fun assertDeclared(qualifier: String, lightStatusBar: String) {
        val file = themeFile(qualifier)
        assumeTrue("$qualifier/themes.xml not reachable from ${File("").absolutePath}", file != null)

        val styles = styleElements(file!!)
        listOf("Theme.ExpenseTracker", "Theme.ExpenseTracker.Splash").forEach { styleName ->
            val items = styles[styleName]
                ?: throw AssertionError("$styleName is missing from ${file.path}")
            assertEquals(
                "$styleName must declare android:windowLightStatusBar",
                lightStatusBar,
                items["android:windowLightStatusBar"]
            )
            assertEquals(
                "$styleName must declare android:windowLightNavigationBar",
                lightStatusBar,
                items["android:windowLightNavigationBar"]
            )
        }
    }

    private fun themeFile(qualifier: String): File? {
        // Unit tests run with the module as the working directory, but a search upward keeps
        // this working if that ever changes.
        var dir: File? = File("").absoluteFile
        while (dir != null) {
            listOf("src/main/res", "app/src/main/res")
                .map { File(dir, "$it/$qualifier/themes.xml") }
                .firstOrNull { it.isFile }
                ?.let { return it }
            dir = dir.parentFile
        }
        return null
    }

    private fun styleElements(file: File): Map<String, Map<String, String>> {
        val document = DocumentBuilderFactory.newInstance()
            .apply { isNamespaceAware = false }
            .newDocumentBuilder()
            .parse(file)
        val styles = document.getElementsByTagName("style")
        val result = mutableMapOf<String, Map<String, String>>()
        for (index in 0 until styles.length) {
            val style = styles.item(index) as Element
            val items = mutableMapOf<String, String>()
            val children = style.getElementsByTagName("item")
            for (child in 0 until children.length) {
                val item = children.item(child) as Element
                items[item.getAttribute("name")] = item.textContent.trim()
            }
            result[style.getAttribute("name")] = items
        }
        assertTrue("no <style> entries in ${file.path}", result.isNotEmpty())
        return result
    }
}
