package net.hax.niatool.updater

import android.util.Log

class Version(val major: Int, val minor: Int, val patch: Int) {

    companion object {
        val TAG = "Version"

        // Note, we are using the same regex to match both our generated version and the remote tag version,
        // in detail our local version will never have a 'v' prefix if using our gradle script
        val VERSION_REGEX = """^v?(\d+)\.(\d+)(?:\.(\d+))?.*$""".toRegex()

        fun fromString(versionName: String): Version {
            val match = VERSION_REGEX.matchEntire(versionName)?.groupValues ?:
                    throw IllegalArgumentException("Cannot recognize version string $versionName")

            return Version(match[1].toInt(), match[2].toInt(), if (match[3].isNotEmpty()) match[3].toInt() else 0)
        }

        fun fromStringOrNull(versionName: String): Version? {
            return try {
                Version.fromString(versionName)
            } catch (e: IllegalArgumentException) {
                Log.e(TAG, "Cannot recognize release version format for \"$versionName\"")
                null
            }
        }
    }

    val pretty: String
        get() = if (patch == 0) "$major.$minor" else "$major.$minor.$patch"

    val canonical: String
        get() = "v$major.$minor.$patch"

    operator fun compareTo(that: Version): Int {
        if (this.major > that.major) return 100
        else if (this.major < that.major) return -100

        if (this.minor > that.minor) return 10
        else if (this.minor < that.minor) return -10

        if (this.patch > that.patch) return 1
        else if (this.patch < that.patch) return -1
        return 0
    }

    override fun toString(): String = "[$canonical]"

}
