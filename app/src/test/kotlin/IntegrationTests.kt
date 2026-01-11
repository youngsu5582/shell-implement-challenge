import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

class IntegrationTests {

    @Nested
    inner class BuiltInCommand {
        @Test
        fun `exit 명령어는 그대로 종료한다`() {
            val command = buildCommand { }

            val result = execute(command)

            assertTrue { result.contains("$") }
        }

        @ParameterizedTest
        @EnumSource(value = ShellBuiltInCommand::class)
        fun `type 명령어는 Built-In 명령어면 Built-In 이라고 출력해준다`(command: ShellBuiltInCommand) {
            val request = buildCommand {
                appendLine("type ${command.value}")
            }

            val result = execute(request)

            assertTrue { result.contains("${command.value} is a shell builtin") }
        }

        @Test
        fun `echo 는 명령어를 출력한다`() {
            val command = buildCommand {
                appendLine("echo 123")
            }

            val result = execute(command)

            assertTrue { result.contains("123") }
        }
    }

    @Nested
    inner class ExecutableCommand {

        private val pathList = listOf(File(javaClass.getResource("/my-command").path).parent)

        @BeforeEach
        fun setup() {
            pathList.forEach { path ->
                File(path).listFiles()?.forEach {
                    it.setExecutable(true)
                }
            }
        }

        @Test
        fun `type {command} 는 command 의 경로를 찾아서 반환한다`() {
            val command = buildCommand {
                appendLine("type my-command")
            }

            val result = execute(command, pathList = pathList)
            assertTrue { result.contains("resources/test/my-command") }
        }

        @Test
        fun `type {command} 를 찾지 못한다면, command not found 를 출력한다`() {
            val command = buildCommand {
                appendLine("type not-exist-command")
            }

            val result = execute(command, pathList = pathList)
            assertTrue { result.contains("not-exist-command: not found") }
        }

        @Test
        fun `함수를 실행한다`() {
            val command = buildCommand {
                appendLine("custom_exe alice")
            }

            val result = execute(command, pathList = pathList)

            assertTrue(result.contains("Program was passed 2 args"))
            assertTrue(result.contains("Arg #0 (program name):"))
            assertTrue(result.contains("src/test/resources/custom_exe"))
            assertTrue(result.contains("Arg #1: alice"))
        }

        @Test
        fun `함수가 없으면 실행하지 않는다`() {
            val command = buildCommand {
                appendLine("not_exist_command")
            }

            val result = execute(command, pathList = pathList)

            assertTrue(result.contains("not_exist_command: not found"))
        }
    }


    private fun execute(command: String, pathList: List<String> = emptyList()): String {
        val input = ByteArrayInputStream(command.toByteArray())
        val output = ByteArrayOutputStream()

        val app = ShellApplication(input, output, pathList)
        app.start()

        return output.toString()
    }

    /**
     * exit 를 입력해야만, 종료가 되므로 맨 마지막에 무조건 종료가 되게 커맨드 구성
     */
    private fun buildCommand(builder: StringBuilder.() -> Unit): String {
        return StringBuilder().apply {
            builder()
            append("exit")
            append(System.lineSeparator())
        }.toString()
    }


}