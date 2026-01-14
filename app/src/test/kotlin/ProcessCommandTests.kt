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

    @Test
    fun `에러 파이프라인은 stderr 을 지정한다`() {
        val command = ProcessCommand.from("echo hello 2> file.txt")
        assertTrue { command.command == "echo" }
        assertTrue { command.args.contains("hello") }
        assertTrue { command.stderr == "file.txt" }
    }

    @Test
    fun `파이프라인과 에러 파이프라인 둘다 지정 가능하다`() {
        val command = ProcessCommand.from("cd not-exist > file.txt 2> error.txt")
        assertTrue { command.command == "cd" }
        assertTrue { command.args.contains("not-exist") }
        assertTrue { command.stdout == "file.txt" }
        assertTrue { command.stderr == "error.txt" }
    }

    @Test
    fun `순서가 바뀌어도 제대로 지정된다`() {
        val command = ProcessCommand.from("cd not-exist 2> error.txt > file.txt")
        assertTrue { command.command == "cd" }
        assertTrue { command.args.contains("not-exist") }
        assertTrue { command.stdout == "file.txt" }
        assertTrue { command.stderr == "error.txt" }
    }
}