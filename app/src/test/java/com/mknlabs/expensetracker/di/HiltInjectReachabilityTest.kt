package com.mknlabs.expensetracker.di

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Guards the one gap Hilt leaves open: Dagger's binding graph is **demand-driven**.
 *
 * An `@Inject` constructor that nothing ever asks for is never added to the graph, so
 * Dagger never checks it. That is not theoretical — `BillingManager` carried an
 * unsatisfiable dependency (`@ApplicationContext Application` plus a Kotlin `object` as a
 * constructor parameter) for months while the project compiled cleanly, because no code
 * injected it. The moment it was injected, the build failed.
 *
 * So "the project builds" does *not* mean the injectable classes are sound, and this test
 * closes that gap: it fails if any class with an `@Inject` constructor is not referenced
 * from main sources, because such a class is exactly the case Hilt silently skips.
 *
 * Reachability is checked against stripped, non-comment source. A class mentioned only in a
 * KDoc link or a comment does not count as demanded.
 *
 * Known limitation: this proves a class is *referenced*, not that it is referenced in an
 * injection position. A class named only in an unused function signature would still pass.
 * Tightening that would mean parsing the Hilt graph itself, which a JVM test cannot do —
 * the component is generated code, and it offers no reflective lookup by type.
 */
class HiltInjectReachabilityTest {

    @Test
    fun `every injectable class is referenced so Hilt actually validates it`() {
        val files = mainSourceRoot()
            .walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .toList()

        assertTrue(
            "Found no Kotlin sources to scan. The source root was resolved incorrectly, " +
                "which would make this test vacuously pass.",
            files.isNotEmpty()
        )

        // Read the tree once: the reference check below is O(candidates * files).
        val strippedSources = files.map { file ->
            file to file.readLines()
                .filterNot { isCommentLine(it.trimStart()) }
                .joinToString("\n")
        }

        val injectableClasses = files.flatMap { file ->
            injectedClassNames(file).map { it to file }
        }

        assertTrue(
            "Found no @Inject constructors. Either the scan logic broke or the project " +
                "stopped using constructor injection.",
            injectableClasses.isNotEmpty()
        )

        val unreferenced = injectableClasses
            .filter { (className, declaringFile) ->
                val pattern = Regex("""\b${Regex.escape(className)}\b""")
                strippedSources.none { (otherFile, text) ->
                    otherFile != declaringFile && pattern.containsMatchIn(text)
                }
            }
            .map { (className, declaringFile) ->
                "$className (${declaringFile.name})"
            }
            .toSet()

        assertEquals(
            "An injectable class nothing references is a class Hilt never validates, so an " +
                "unsatisfiable dependency can hide there indefinitely. Inject it where it " +
                "belongs, delete it if it is dead code, or — only when it is genuinely reached " +
                "another way — add it below with a reason. Do not add to this set to silence " +
                "a real failure.",
            KNOWN_UNREFERENCED,
            unreferenced
        )
    }

    companion object {
        /**
         * Injectable classes that nothing in main sources references, verified one by one on
         * 2026-09-23. Each is either dead code to remove or a class whose dependency graph
         * Hilt has never validated — decide per class rather than leaving it here forever.
         *
         * These are pinned rather than ignored so that both directions are deliberate:
         * removing one from the code fails this test until the entry is removed too, and
         * introducing a new one fails immediately.
         *
         * Adding an entry is an explicit statement that Hilt will not validate that class.
         */
        private val KNOWN_UNREFERENCED = setOf(
            "ConnectivityHelper (ConnectivityHelper.kt)",
            "GeminiVoiceParser (GeminiVoiceParser.kt)",
            "GetSmsInboxPageUseCase (SmsInboxReadUseCases.kt)",
            "SetActiveFontUseCase (SetActiveFontUseCase.kt)",
            "VoiceTransactionViewModel (VoiceTransactionViewModel.kt)",
        )

        private val CLASS_DECLARATION = Regex("""class\s+([A-Za-z0-9_]+)""")

        /** How far back to look for the class declaration that owns an `@Inject constructor`. */
        private const val CLASS_LOOKBEHIND_LINES = 15

        /** How close `@Inject` must sit to `constructor(` to count as annotating it. */
        private const val INJECT_LOOKAHEAD_LINES = 3

        private fun mainSourceRoot(): File {
            val workingDirectory = File(System.getProperty("user.dir") ?: ".")
            val candidates = listOf(
                File(workingDirectory, "src/main/java"),
                File(workingDirectory, "app/src/main/java"),
                File(workingDirectory.parentFile ?: workingDirectory, "app/src/main/java")
            )

            val root = candidates.firstOrNull { it.isDirectory }
            assertTrue(
                "Could not find src/main/java from ${workingDirectory.absolutePath}. " +
                    "Candidates tried: ${candidates.joinToString { it.path }}",
                root != null
            )
            return root!!
        }

        /**
         * The class name attached to each `@Inject constructor` in [file].
         *
         * The declaration is found by walking back from `constructor(`, not by taking the
         * first `class` in the file — a file may declare data classes above the injectable one.
         */
        private fun injectedClassNames(file: File): List<String> {
            val lines = file.readLines()

            return lines.indices
                .filter { index ->
                    lines[index].contains("constructor(") &&
                        (index - INJECT_LOOKAHEAD_LINES..index).any { line ->
                            line >= 0 && lines[line].contains("@Inject")
                        }
                }
                .mapNotNull { index -> classDeclarationAbove(lines, index) }
                .distinct()
        }

        private fun classDeclarationAbove(lines: List<String>, fromIndex: Int): String? {
            val start = (fromIndex - CLASS_LOOKBEHIND_LINES).coerceAtLeast(0)

            for (index in fromIndex downTo start) {
                CLASS_DECLARATION.find(lines[index])?.let { return it.groupValues[1] }
            }
            return null
        }

        private fun isCommentLine(trimmedLine: String): Boolean =
            trimmedLine.startsWith("//") ||
                trimmedLine.startsWith("/*") ||
                trimmedLine.startsWith("*")
    }
}
