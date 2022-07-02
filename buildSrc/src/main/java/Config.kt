import org.gradle.api.Project
import org.mozilla.fenix.gradle.ext.execReadStandardOutOrThrow
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.Date
import java.util.Locale

object Config {
    const val compileSdkVersion = 32
    const val minSdkVersion = 21
    const val targetSdkVersion = 32

    @JvmStatic
    private fun generateDebugVersionName(): String {
        val today = Date()
        return SimpleDateFormat("1.0.yyww", Locale.US).format(today)
    }

    @JvmStatic
    fun releaseVersionName(project: Project): String {
        return if (project.hasProperty("versionName")) project.property("versionName") as String else ""
    }

    @JvmStatic
    fun nightlyVersionName(): String {
        val majorVersion = AndroidComponents.VERSION.split(".")[0]
        return "$majorVersion.0a1"
    }

    @JvmStatic
    fun majorVersion(project: Project): String {
        val releaseVersion = releaseVersionName(project)
        val version = if (releaseVersion.isBlank()) {
            nightlyVersionName()
        } else {
            releaseVersion
        }

        return version.split(".")[0]
    }

    @JvmStatic
    fun generateBuildDate(): String {
        return LocalDateTime.now().toString()
    }

    private val fennecBaseVersionCode by lazy {
        val format = SimpleDateFormat("yyyyMMddHHmmss", Locale.US)
        val cutoff = format.parse("20141228000000")
        val build = Date()

        Math.floor((build.time - cutoff.time) / (1000.0 * 60.0 * 60.0)).toInt()
    }

    @JvmStatic
    fun generateFennecVersionCode(abi: String): Int {
        val base = fennecBaseVersionCode

        when {
            base < 0 -> throw RuntimeException("Cannot calculate versionCode. Hours underflow.")
            base > 0x20000 /* 2^17 */ -> throw RuntimeException("Cannot calculate versionCode. Hours overflow.")
            base > 0x20000 - (366 * 24) ->
                throw RuntimeException("Running out of low order bits calculating versionCode.")
        }

        var version = 0x78200000 // 1111000001000000000000000000000
        version = version or (base shl 3)

        if (abi == "x86_64" || abi == "x86") {
            version = version or (1 shl 2)
        }

        if (abi == "arm64-v8a" || abi == "x86_64") {
            version = version or (1 shl 1)
        }

        version = version or (1 shl 0)

        return version
    }

    @JvmStatic
    fun getGitHash(): String {
        val revisionCmd = arrayOf("git", "rev-parse", "--short", "HEAD")
        val revision = Runtime.getRuntime().execReadStandardOutOrThrow(revisionCmd)

        val statusCmd = arrayOf("git", "status", "--porcelain=v2")
        val status = Runtime.getRuntime().execReadStandardOutOrThrow(statusCmd)
        val hasUnstagedChanges = status.isNotBlank()
        val statusSuffix = if (hasUnstagedChanges) "+" else ""

        return "${revision}${statusSuffix}"
    }
}
