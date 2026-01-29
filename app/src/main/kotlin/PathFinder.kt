import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.isExecutable
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name

class PathFinder(
    private val pathList: List<String>
) {
    fun findExecutable(command: String): Path? {
        for (path in pathList) {
            val path = Path(path)
            val found = recursiveSearch(path, command)
            CustomLogger.debug("$path 에서 검색결과: $found(실행권환: ${found?.isExecutable()})")
            if (found != null && found.isExecutable()) {
                return found
            }
        }
        return null
    }

    private fun recursiveSearch(path: Path, to: String): Path? {
        try {
            val entries = path.listDirectoryEntries()
            for (entry in entries) {
                if (entry.isDirectory()) {
                    val recursiveFound = recursiveSearch(entry, to)
                    if (recursiveFound != null) return recursiveFound
                }
                if (entry.name == to) return entry.toAbsolutePath()
            }
        } catch (e: SecurityException) {
            CustomLogger.error("권한 없음: ${e.message}")
            return null
        } catch (e: Exception) {
            CustomLogger.error("IO 오류: ${e.message}")
            return null
        }
        return null
    }
}