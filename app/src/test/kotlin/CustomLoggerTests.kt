import org.junit.jupiter.api.Assertions.*
import java.io.ByteArrayOutputStream
import kotlin.test.Test

class CustomLoggerTests {

    @Test
    fun `outputStream 을 설정할 수 있다`() {
        val stream = ByteArrayOutputStream()
        CustomLogger.setOutputStream(stream)

        CustomLogger.setLevel(LogLevel.DEBUG)
        CustomLogger.debug("확인용 메시지")

        val output = stream.toString()

        assertTrue { output.contains("확인용 메시지") }
    }

    @Test
    fun `설정된 레벨 미만의 메시지는 포함되지 않는다`() {
        val stream = ByteArrayOutputStream()
        CustomLogger.setOutputStream(stream)
        CustomLogger.setLevel(LogLevel.INFO)

        CustomLogger.debug("DEBUG 메시지")
        CustomLogger.info("INFO 메시지")

        val output = stream.toString()

        assertTrue { output.contains("INFO 메시지") }
        assertFalse { output.contains("DEBUG 메시지") }
    }
}