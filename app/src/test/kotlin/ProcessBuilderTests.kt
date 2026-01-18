import org.junit.jupiter.api.Assertions.assertTrue
import java.util.concurrent.TimeUnit
import kotlin.collections.set
import kotlin.test.Test
import kotlin.test.assertFalse

class ProcessBuilderTests {

    // Process 기준 입출력 스트림 방향 정리:
    // inputStream: 프로세스 stdout (애플리케이션이 프로세스의 결과값을 읽음)
    // outputStream: 프로세스 stdin (애플리케이션이 프로세스의 입력값을 씀)
    @Test
    fun `echo 출력은 process inputStream 으로 읽는다`() {
        val builder = ProcessBuilder("echo", "hello")
            .redirectInput(ProcessBuilder.Redirect.PIPE)

        val process = builder.start()

        val result = process.inputStream.bufferedReader().readText()

        val exitCode = process.waitFor()
        assertTrue(exitCode == 0)
        assertTrue { result.contains("hello") }
    }

    @Test
    fun `stdin 을 닫아야 wc 가 종료된다`() {
        val builder = ProcessBuilder("wc")
            .redirectOutput(ProcessBuilder.Redirect.PIPE)

        builder.environment()["PATH"] = "/usr/bin:/bin:/usr/sbin:/sbin"

        val process = builder.start()
        process.outputStream.bufferedWriter().use { writer ->
            writer.write("hihi")
            writer.flush()
        }

        val result = process.inputStream.bufferedReader().readText()

        val exitCode = process.waitFor()
        assertTrue(exitCode == 0)
        assertTrue { result.contains("0       1       4") }
    }


    @Test
    fun `stdin 을 닫지 않으면 wc 는 종료하지 않는다`() {
        val builder = ProcessBuilder("wc")
            .redirectOutput(ProcessBuilder.Redirect.PIPE)

        val process = builder.start()

        val writer = process.outputStream.bufferedWriter()
        writer.write("hihi")

        val result = process.waitFor(2, TimeUnit.SECONDS)
        assertFalse { result }
    }

    @Test
    fun `외부 프로세스 간 출력을 직접 연결할 수 있다`() {
        val builder1 = ProcessBuilder("echo", "hello")
            .redirectInput(ProcessBuilder.Redirect.PIPE)

        val process1 = builder1.start()

        val builder2 = ProcessBuilder("wc")
            .redirectOutput(ProcessBuilder.Redirect.PIPE)

        val process2 = builder2.start()

        process1.inputStream.use {
            it.copyTo(process2.outputStream)
            process2.outputStream.close()
        }

        val result = process2.inputStream.bufferedReader().readText()

        process1.waitFor()
        process2.waitFor()

        assertTrue { result.contains("1       1       6") }
    }

    @Test
    fun `startPipeline 은 프로세스 간 파이프를 자동으로 연결한다`() {
        val builder1 = ProcessBuilder("echo", "hello")
        val builder2 = ProcessBuilder("wc")

        // startPipeline 을 사용하면, 자동으로 Process 간 파이프라인이 연결된다.
        val pipeline = ProcessBuilder.startPipeline(listOf(builder1, builder2))

        val result = pipeline.last().inputStream.bufferedReader().readText()

        pipeline.forEach { it.waitFor() }
        assertTrue { result.contains("1       1       6") }
    }
}
