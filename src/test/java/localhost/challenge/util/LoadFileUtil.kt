package localhost.challenge.util

import org.springframework.util.ResourceUtils
import org.testcontainers.shaded.org.apache.commons.io.FileUtils
import java.io.IOException
import java.nio.charset.StandardCharsets

object LoadFileUtil {
    @JvmStatic
    fun loadJsonFileAsString(path: String): String {
        return try {
            val file = ResourceUtils.getFile(path)
            FileUtils.readFileToString(file, StandardCharsets.UTF_8)
        } catch (e: IOException) {
            throw RuntimeException("Unable to load resource $path", e)
        }
    }
}
