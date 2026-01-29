import Constant.USER_DIRECTORY_PROPERTY
import shell.built.`in`.ShellBuiltInCommandType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively
import kotlin.io.path.exists
import kotlin.io.path.readLines
import kotlin.io.path.readText
import kotlin.test.Test
import kotlin.test.assertFalse
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
        @EnumSource(value = ShellBuiltInCommandType::class)
        fun `type 명령어는 Built-In 명령어면 Built-In 이라고 출력해준다`(command: ShellBuiltInCommandType) {
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
            println(result)

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
            println(result)

            assertTrue(result.contains("not_exist_command: not found"))
        }
    }

    @Nested
    inner class NavigationTests {

        private lateinit var originalUserDir: String
        private lateinit var originalHomeDir: String

        @BeforeEach
        fun setUp() {
            // 1. 테스트 시작 전 원래 시스템 속성 저장
            originalUserDir = System.getProperty(Constant.USER_DIRECTORY_PROPERTY)
            originalHomeDir = System.getenv(Constant.HOME_DIRECTORY_PROPERTY) // 혹은 HOME_DIRECTORY_PROPERTY
        }

        @AfterEach
        fun tearDown() {
            System.setProperty(USER_DIRECTORY_PROPERTY, originalUserDir)
        }

        @Test
        fun `pwd 는 현재 경로를 반환하다`() {
            val command = buildCommand {
                appendLine("pwd")
            }
            val result = execute(command)
            assertTrue { result.contains("codecrafters-shell-kotlin/app") }
        }

        @Test
        fun `cd 는 입력한 디렉토리로 이동한다`() {
            val command = buildCommand {
                appendLine("cd directory")
                appendLine("pwd")
            }

            val result = execute(command)
            assertTrue { result.contains("codecrafters-shell-kotlin/app/directory") }
        }

        @Test
        fun `cd ~ 는 홈 디렉토리로 이동한다`() {
            val command = buildCommand {
                appendLine("cd ~")
                appendLine("pwd")
            }

            val result = execute(command)
            // env 의 홈 디렉토리를 통해 이동
            assertTrue { result.contains(System.getenv(Constant.HOME_DIRECTORY_PROPERTY)) }
        }

        @Test
        fun `cd 는 상위 디렉토리로 이동한다`() {
            val command = buildCommand {
                appendLine("cd directory")
                appendLine("cd inner-directory")
                appendLine("cd ../")
                appendLine("pwd")
            }

            val result = execute(command)
            assertTrue { result.contains("codecrafters-shell-kotlin/app/directory") }
        }
    }

    @Nested
    inner class PipelineTests {

        val filePath = "./temp.txt"
        val errorPath = "./error.txt"
        val directoryPath = "./temp"

        @OptIn(ExperimentalPathApi::class)
        @BeforeEach
        fun deleteFile() {
            Files.deleteIfExists(Paths.get(filePath))
            Files.deleteIfExists(Paths.get(errorPath))
            val directory = Paths.get(directoryPath)
            directory.deleteRecursively()
            Files.deleteIfExists(directory)
        }

        @Test
        fun `파이프라인을 입력하면, 파이프라인에서 지정한 파일에 결과가 저장된다`() {
            val command = buildCommand {
                appendLine("type type > $filePath")
            }

            // when
            val result = execute(command)
            println(result)

            // then
            assertFalse { result.contains("type is a shell builtin") }

            val file = Paths.get(filePath)
            assertTrue { file.exists() }
            assertTrue { file.readText().contains("type is a shell builtin") }
        }

        @Test
        fun `디렉토리와 파일이 없으면 생성한다`() {
            val directoryFile = "$directoryPath/$filePath"
            // given
            val command = buildCommand {
                appendLine("type type > $directoryFile")
            }

            // 시작 전에 없는지 확인
            assertFalse { Paths.get(directoryPath).exists() }
            assertFalse { Paths.get(directoryFile).exists() }

            // when
            val result = execute(command)

            // then
            assertFalse { result.contains("type is a shell builtin") }

            assertTrue { Paths.get(directoryPath).exists() }
            assertTrue { Paths.get(directoryFile).exists() }
        }

        @Test
        fun `에러는 기존 터미널에 출력된다`() {
            // given
            val command = buildCommand {
                appendLine("cd not-exist")
            }

            // when
            val result = execute(command)

            // then
            assertTrue { result.contains("cd: not-exist: No such file or directory") }
        }

        @Test
        fun `에러 파이프라인도 지정 가능하다`() {
            // given
            val command = buildCommand {
                appendLine("cd not-exist 2> $errorPath")
            }

            // when
            val result = execute(command)

            assertFalse { result.contains("cd: not-exist: No such file or directory") }

            val file = Paths.get(errorPath)
            assertTrue { file.exists() }
            assertTrue { file.readText().contains("cd: not-exist: No such file or directory") }
        }

        @Test
        fun `에러 결과가 없어도 먼저 파일을 생성한다`() {
            // given
            val command = buildCommand {
                appendLine("type type 2> $errorPath")
            }

            // when
            val result = execute(command)

            assertTrue { result.contains("type is a shell builtin") }
            val file = Paths.get(errorPath)
            assertTrue { file.exists() }
            assertTrue { file.readText().isEmpty() }
        }

        @Test
        fun `Overwrite 는 결과를 덮어쓴다`() {
            // given
            val command = buildCommand {
                appendLine("type type > $filePath")
                appendLine("type type > $filePath")
            }

            // when
            execute(command)

            val file = Paths.get(filePath)
            assertTrue { file.exists() }
            val lines = file.readLines()
            assertTrue { lines.size == 1 }
            assertTrue { lines.all { it == "type is a shell builtin" } }
        }

        @Test
        fun `Append 는 결과를 추가한다`() {
            // given
            val command = buildCommand {
                appendLine("type type >> $filePath")
                appendLine("type type >> $filePath")
            }

            // when
            execute(command)

            val file = Paths.get(filePath)
            assertTrue { file.exists() }
            val lines = file.readLines()
            assertTrue { lines.size == 2 }
            assertTrue { lines.all { it == "type is a shell builtin" } }
        }

        @Test
        fun `Append 는 순서를 유지해야한다`() {
            // given
            val command = buildCommand {
                appendLine("echo 123 > $filePath")
                appendLine("echo 456 >> $filePath")
            }

            // when
            execute(command)

            val file = Paths.get(filePath)
            assertTrue { file.exists() }
            val lines = file.readLines()
            assertTrue { lines.size == 2 }
            assertTrue { lines[0] == "123" }
            assertTrue { lines[1] == "456" }
        }
    }

    @Nested
    inner class PipeOperatorTests {

        private val testFilePath = "./pipe_test.txt"

        // 시스템 PATH를 사용하여 cat, wc, head, tail 등을 찾을 수 있도록 설정
        private val systemPath = System.getenv("PATH")?.split(":") ?: emptyList()

        @OptIn(ExperimentalPathApi::class)
        @BeforeEach
        fun setup() {
            Files.deleteIfExists(Paths.get(testFilePath))
        }

        @OptIn(ExperimentalPathApi::class)
        @AfterEach
        fun cleanup() {
            Files.deleteIfExists(Paths.get(testFilePath))
        }

        @Test
        fun `cat file | wc 파이프라인이 동작한다`() {
            // given: 테스트 파일 생성 (wc -l은 줄바꿈 문자 수를 센다)
            val content = "line1\nline2\nline3\nline4\nline5\n"
            Files.write(Paths.get(testFilePath), content.toByteArray())

            val command = buildCommand {
                appendLine("cat $testFilePath | wc -l")
            }

            // when
            val result = execute(command, pathList = systemPath)
            println("Result: $result")

            // then: wc -l 은 라인 수를 출력 (5줄)
            assertTrue { result.contains("5") }
        }

        @Test
        fun `echo | cat 파이프라인이 동작한다`() {
            val command = buildCommand {
                appendLine("echo hello world | cat")
            }

            // when
            val result = execute(command, pathList = systemPath)
            println("Result: $result")

            // then
            assertTrue { result.contains("hello world") }
        }

        @Test
        fun `cat file | head 파이프라인이 동작한다`() {
            // given: 테스트 파일 생성 (10줄)
            val content = (1..10).joinToString("\n") { "line$it" }
            Files.write(Paths.get(testFilePath), content.toByteArray())

            val command = buildCommand {
                appendLine("cat $testFilePath | head -n 3")
            }

            // when
            val result = execute(command, pathList = systemPath)
            println("Result: $result")

            // then: head -n 3 은 처음 3줄만 출력
            assertTrue { result.contains("line1") }
            assertTrue { result.contains("line2") }
            assertTrue { result.contains("line3") }
            assertFalse { result.contains("line4") }
        }

        @Test
        fun `cat file | tail 파이프라인이 동작한다`() {
            // given: 테스트 파일 생성 (10줄)
            val content = (1..10).joinToString("\n") { "line$it" }
            Files.write(Paths.get(testFilePath), content.toByteArray())

            val command = buildCommand {
                appendLine("cat $testFilePath | tail -n 3")
            }

            // when
            val result = execute(command, pathList = systemPath)
            println("Result: $result")

            // then: tail -n 3 은 마지막 3줄만 출력
            assertTrue { result.contains("line8") }
            assertTrue { result.contains("line9") }
            assertTrue { result.contains("line10") }
            assertFalse { result.contains("line7") }
        }

        @Test
        fun `echo | wc 파이프라인이 동작한다`() {
            // given BUILT_IN + SHELL COMMAND
            val command = buildCommand {
                appendLine("echo raspberry\\\\nblueberry | wc")
            }

            // when
            val result = execute(command, pathList = systemPath)
            println("Result: $result")

            // then 1 1 22 포함
            assertTrue { result.contains("        1       1      21") }
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