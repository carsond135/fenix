package org.mozilla.fenix.gradle.ext

import java.util.concurrent.TimeUnit

fun Runtime.execReadStandardOutOrThrow(cmd: Array<String>, timeoutSeconds: Long = 30): String {
    val process = Runtime.getRuntime().exec(cmd)

    check(process.waitFor(timeoutSeconds, TimeUnit.SECONDS)) { "command unexpectedly timed out: `$cmd`" }
    check(process.exitValue() == 0) {
        val stderr = process.errorStream.bufferedReader().readText().trim()
        """command exited with non-zero exit value: ${process.exitValue()}.
           |cmd: ${cmd.joinToString(separator = " ")}
           |stderr:
           |${stderr}""".trimMargin()
    }

    return process.inputStream.bufferedReader().readText().trim()
}