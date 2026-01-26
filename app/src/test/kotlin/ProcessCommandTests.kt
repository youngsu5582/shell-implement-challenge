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
        assertTrue { command.stdout?.path == "file.txt" }
    }

    @Test
    fun `에러 파이프라인은 stderr 을 지정한다`() {
        val command = ProcessCommand.from("echo hello 2> file.txt")
        assertTrue { command.command == "echo" }
        assertTrue { command.args.contains("hello") }
        assertTrue { command.stderr?.path == "file.txt" }
    }

    @Test
    fun `파이프라인과 에러 파이프라인 둘다 지정 가능하다`() {
        val command = ProcessCommand.from("cd not-exist > file.txt 2> error.txt")
        assertTrue { command.command == "cd" }
        assertTrue { command.args.contains("not-exist") }
        assertTrue { command.stdout?.path == "file.txt" }
        assertTrue { command.stderr?.path == "error.txt" }
    }

    @Test
    fun `순서가 바뀌어도 제대로 지정된다`() {
        val command = ProcessCommand.from("cd not-exist 2> error.txt > file.txt")
        assertTrue { command.command == "cd" }
        assertTrue { command.args.contains("not-exist") }
        assertTrue { command.stdout?.path == "file.txt" }
        assertTrue { command.stderr?.path == "error.txt" }
    }

    @Test
    fun `'hello    world'	는 그대로 hello    world	로 인식된다`() {
        val command = ProcessCommand.from("echo 'hello    world'")
        assertTrue { command.command == "echo" }
        assertTrue { command.args.contains("hello    world") }
    }

    @Test
    fun `echo hello    world 는 hello world 로 인식된다`() {
        val command = ProcessCommand.from("echo hello    world")
        assertTrue { command.command == "echo" }
        assertTrue { command.args.contains("hello") }
        assertTrue { command.args.contains("world") }
    }

    @Test
    fun `echo 'hello''world' 는 helloworld 로 인식된다`() {
        val command = ProcessCommand.from("echo 'hello''world'")
        assertTrue { command.command == "echo" }
        assertTrue { command.args.contains("helloworld") }
    }

    @Test
    fun `echo hello''world 는 helloworld 로 인식된다`() {
        val command = ProcessCommand.from("echo hello''world")
        assertTrue { command.command == "echo" }
        assertTrue { command.args.contains("helloworld") }
    }

    @Test
    fun `echo hello 'hello_world' 는 hello hello_world 로 인식된다`() {
        val command = ProcessCommand.from("echo hello 'hello_world'")
        println(command)
        assertTrue { command.command == "echo" }
        assertTrue { command.args.contains("hello") }
        assertTrue { command.args.contains("hello_world") }
    }

    @Test
    fun `echo "hello    world" 는 hello    world 로 인식된다`() {
        val command = ProcessCommand.from("echo \"hello    world\"")
        assertTrue { command.command == "echo" }
        assertTrue { command.args.contains("hello    world") }
    }

    @Test
    fun `echo "hello""world" 는 helloworld 로 인식된다`() {
        val command = ProcessCommand.from("echo \"hello\"\"world\"")
        assertTrue { command.command == "echo" }
        assertTrue { command.args.contains("helloworld") }
    }

    @Test
    fun `echo "hello" "world" 는 hello world 로 인식된다`() {
        val command = ProcessCommand.from("echo \"hello\" \"world\"")
        assertTrue { command.command == "echo" }
        assertTrue { command.args.contains("hello") }
        assertTrue { command.args.contains("world") }
    }

    @Test
    fun `echo "shell's test" 는 shell's test 로 인식된다`() {
        val command = ProcessCommand.from("echo \"shell's test\"")
        assertTrue { command.command == "echo" }
        assertTrue { command.args.contains("shell's test") }
    }

    @Test
    fun `echo "quz  hello"  "bar" 는 quz  hello bar 로 인식된다`() {
        val command = ProcessCommand.from("echo \"quz  hello\"  \"bar\"")
        assertTrue { command.command == "echo" }
        assertTrue { command.args.contains("quz  hello") }
        assertTrue { command.args.contains("bar") }
    }

    @Test
    fun `echo "bar"  "shell's"  "foo" 는 bar shell's foo 로 인식된다`() {
        val command = ProcessCommand.from("echo \"bar\"  \"shell's\"  \"foo\"")
        assertTrue { command.command == "echo" }
        assertTrue { command.args.contains("bar") }
        assertTrue { command.args.contains("shell's") }
        assertTrue { command.args.contains("foo") }
    }
}