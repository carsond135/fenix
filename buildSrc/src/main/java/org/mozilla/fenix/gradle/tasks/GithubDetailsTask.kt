package org.mozilla.fenix.gradle.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File

open class GithubDetailsTask : DefaultTask() {
    @Input
    var text: String = ""

    private val detailsFile = File("/builds/worker/github/customCheckRunText.md")
    private val suffix = "\n\n_(404 if compilation failed)_"

    @TaskAction
    fun writeFile() {
        val taskId = System.getenv("TASK_ID")
        val reportsUrl = "https://firefoxci.taskcluster-artifacts.net/$taskId/0/public/reports"
        val replaced = text.replace("{reportsUrl}", reportsUrl)

        project.mkdir("/builds/worker/github")
        detailsFile.writeText(replaced + suffix)
    }
}