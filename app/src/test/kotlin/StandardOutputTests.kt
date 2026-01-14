import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import java.io.File
import java.nio.file.Files
import kotlin.io.path.Path
import kotlin.test.Test

class StandardOutputTests {

    private val path = "temp.txt"
    private val overwritePath = "temp-overwrite.txt"
    private val appendPath = "temp-append.txt"

    @AfterEach
    fun setup() {
        Files.deleteIfExists(Path(path))
        Files.deleteIfExists(Path(overwritePath))
        Files.deleteIfExists(Path(appendPath))
    }

    @Test
    fun `파이프라인 1개는 overwrite 로 인식한다`() {
        val output = StandardOutput.from(">", path)
        assertTrue { output.path == path }
        assertTrue { output.option == StandardOption.OVERWRITE }
    }

    @Test
    fun `파이프라인 2개는 append 로 인식한다`() {
        val output = StandardOutput.from(">>", path)
        assertTrue { output.path == path }
        assertTrue { output.option == StandardOption.APPEND }
    }

    @Test
    fun `overwrite 는 파일을 초기화한다`() {
        val file = File(overwritePath)
        file.writeText("overwrite content")
        assertTrue { File(overwritePath).readText().isNotEmpty() }

        val output = StandardOutput.from(">", overwritePath)

        assertTrue { output.path == overwritePath }
        assertTrue { output.option == StandardOption.OVERWRITE }
    }

    @Test
    fun `append 는 파일이 없으면 빈 파일을 생성한다`() {
        val output = StandardOutput.from(">>", appendPath)

        assertTrue { output.path == appendPath }
        assertTrue { File(appendPath).readText().isEmpty() }
    }

    @Test
    fun `append 는 존재하는 파일을 그대로 유지한다`() {
        val file = File(appendPath)
        file.writeText("append content")

        val output = StandardOutput.from(">>", appendPath)

        assertTrue { output.path == appendPath }
        assertTrue { output.option == StandardOption.APPEND }
        assertTrue { file.readText() == "append content" }
    }
}