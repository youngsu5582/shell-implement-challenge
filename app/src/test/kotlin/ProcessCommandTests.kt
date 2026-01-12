import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.Test

class ProcessCommandTests {

    @Test
    fun `문자열을 받아 명령어를 조립한다`() {
        val command = ProcessCommand.from("ls -al")
        assertTrue { command.command == "ls" }
        assertTrue { command.args.contains("-al") }
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "echo \"banana\"",
            "echo \'banana\'"
        ]
    )
    fun `앞, 뒤 문자열이 있다면 잘라서 args 를 만든다`(command: String) {
        val command = ProcessCommand.from(command)
        assertTrue { command.command == "echo" }
        assertTrue { command.args.contains("banana") }
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "echo hello 1> file.txt",
            "echo hello > file.txt"
        ]
    )
    fun `파이프라인은 stdout 을 지정한다`(command: String) {
        val command = ProcessCommand.from(command)
        assertTrue { command.command == "echo" }
        assertTrue { command.args.contains("hello") }
        assertTrue { command.stdout == "file.txt" }
    }

}