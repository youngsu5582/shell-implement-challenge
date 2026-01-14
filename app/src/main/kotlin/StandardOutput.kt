import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.appendText
import kotlin.io.path.writeText
import kotlin.io.writeText

data class StandardOutput(val path: String, val option: StandardOption) {
    companion object {
        fun from(pipeline: String, path: String): StandardOutput {
            val option = if (pipeline.contains(">>")) StandardOption.APPEND else StandardOption.OVERWRITE
            // 디렉토리 생성
            val file = File(path)
            file.parentFile?.mkdirs()

            if (option == StandardOption.OVERWRITE) {
                CustomLogger.debug("덮어쓰기 옵션이므로, 파일을 빈 문자열로 변경합니다. $file")
                file.writeText("");
            }
            if (option == StandardOption.APPEND && Files.notExists(file.toPath())) {
                CustomLogger.debug("APPEND 옵션이므로, 파일을 생성만 합니다. $file")
                Files.createFile(file.toPath())
            }

            return StandardOutput(path, option)
        }
    }

    fun printText(message: String) {
        val path = Paths.get(path.trim())

        when (option) {
            StandardOption.OVERWRITE -> path.writeText(message)
            StandardOption.APPEND -> path.appendText(message + System.lineSeparator())
        }
    }
}

enum class StandardOption {
    OVERWRITE,
    APPEND,
}