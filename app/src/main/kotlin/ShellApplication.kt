import Constant.HOME_DIRECTORY_PROPERTY
import Constant.USER_DIRECTORY_PROPERTY
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.FileAlreadyExistsException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import kotlin.io.path.*


class ShellApplication(
    inputStream: InputStream = System.`in`,
    outputStream: OutputStream = System.out,
    private val pathList: List<String>
) {
    private val reader = inputStream.bufferedReader()
    private val writer = outputStream.bufferedWriter()

    fun start() {
        while (true) {
            print("$ ")
            val line = readLine()

            val processCommand = ProcessCommand.from(line)
            val executionResult = executeIfBuiltInCommand(processCommand)
            when (executionResult) {
                CommandExecutionResult.EXIT -> return
                CommandExecutionResult.BUILT_IN_EXECUTED -> continue
                CommandExecutionResult.NOT_BUILT_IN -> {
                    CustomLogger.debug("Built-In 명령어로 실행했습니다. 명령어: $processCommand")
                    val result = executeShellCommand(processCommand)
                    CustomLogger.debug("명령어 실행결과: $result")
                }
            }
        }
    }

    private fun readLine() = reader.readLine()
    private fun print(message: String) {
        writer.write(message)
        writer.flush()
    }

    private fun println(message: String, stdout: String? = null) {
        if (stdout.isNullOrBlank()) {
            writer.write(message)
            writer.newLine()
            writer.flush()
            return
        }
        val path = Paths.get(stdout.trim())
        val directory = path.parent.toFile()
        val result = directory.mkdirs()
        CustomLogger.debug("$directory 디렉토리가 있는지 확인 및 생성합니다. $result")

        try {
            CustomLogger.debug("$path 에 파일 생성을 시도합니다.")
            Files.createFile(path)
        } catch (e: FileAlreadyExistsException) {
            CustomLogger.debug("이미 파일이 존재합니다.")
        }
        path.writeText(message)
    }

    /**
     * Built In 커맨드이고,
     * 함수가 제대로 실행이 되었다면 true 를 리턴
     * 그렇지 않다면, false 를 리턴
     */
    private fun executeIfBuiltInCommand(processCommand: ProcessCommand): CommandExecutionResult {
        val shellBuiltInCommand =
            ShellBuiltInCommand.from(processCommand.command) ?: return CommandExecutionResult.NOT_BUILT_IN

        when (shellBuiltInCommand) {
            ShellBuiltInCommand.EXIT -> return CommandExecutionResult.EXIT
            ShellBuiltInCommand.ECHO -> {
                println(processCommand.argsToLine(), processCommand.stdout)
            }

            ShellBuiltInCommand.PWD -> {
                println(
                    Paths.get(System.getProperty(USER_DIRECTORY_PROPERTY)).toAbsolutePath().toString(),
                    processCommand.stdout
                )
            }

            ShellBuiltInCommand.CD -> {
                CustomLogger.debug("현재 디렉토리: ${System.getProperty(USER_DIRECTORY_PROPERTY)}")
                val currentDirectory = Paths.get(System.getProperty(USER_DIRECTORY_PROPERTY)).toAbsolutePath()

                if (processCommand.argsToLine() == Constant.HOME_DIRECTORY_SYMBOL) {
                    CustomLogger.debug("홈 디렉토리로 이동합니다. 디렉토리: ${System.getenv(HOME_DIRECTORY_PROPERTY)}")
                    val homeDirectory = System.getenv(HOME_DIRECTORY_PROPERTY)
                    System.setProperty(USER_DIRECTORY_PROPERTY, Paths.get(homeDirectory).toAbsolutePath().toString())
                    return CommandExecutionResult.BUILT_IN_EXECUTED
                }

                val toDirectory = currentDirectory.resolve(processCommand.argsToLine()).normalize()
                CustomLogger.debug("이동할 디렉토리. 디렉토리: $toDirectory")
                try {
                    if (!Files.exists(toDirectory) || !Files.isDirectory(toDirectory)) {
                        throw IOException("Not a directory: $toDirectory")
                    }
                    System.setProperty(USER_DIRECTORY_PROPERTY, toDirectory.toString())
                } catch (e: IOException) {
                    println("${processCommand.formatToLine()} No such file or directory", processCommand.stdout)
                }
            }

            ShellBuiltInCommand.TYPE -> {
                val args = processCommand.args[0]
                val nextCommand = ShellBuiltInCommand.from(args)

                if (nextCommand?.type == ShellCommandType.BUILT_IN) {
                    println("${nextCommand.value} is a shell builtin", processCommand.stdout)
                    return CommandExecutionResult.BUILT_IN_EXECUTED
                }

                val result = findExecutable(args)

                if (result != null) {
                    println("$args is ${result.pathString}", processCommand.stdout)
                } else {
                    println("$args: not found", processCommand.stdout)
                }
            }
        }
        return CommandExecutionResult.BUILT_IN_EXECUTED
    }

    private fun executeShellCommand(processCommand: ProcessCommand): Boolean {
        val path = findExecutable(processCommand.command)
        if (path == null) {
            println("${processCommand.command}: not found", processCommand.stdout)
            return false
        }
        CustomLogger.debug("${processCommand.command} 명령어의 경로를 찾았습니다. $path")

        val builder = ProcessBuilder(processCommand.command, *processCommand.args.toTypedArray())
            // 있으면 실행한 현재 디렉토리가 아닌, 명령어를 찾은 디렉토리로 이동해서 실행함
            // .directory(path.parent.toFile())
            // 있으면, stdout 과 stderr 를 하나의 스트림으로 합침
            .redirectErrorStream(false)

        builder.environment()["PATH"] = path.parent.toString()

        val process = builder.start()

        // 프로세스 출력 → 우리 outputStream으로 복사

        process.inputStream.bufferedReader().forEachLine { line ->
            CustomLogger.debug("프로세스 실행 출력 스트림: $line")
            println(line, processCommand.stdout)
        }

        process.errorStream.bufferedReader().forEachLine { line ->
            CustomLogger.debug("프로세스 실행 에러 스트림: $line")
            println(line)
        }

        return process
            .waitFor(1, TimeUnit.MINUTES)
    }


    private fun findExecutable(command: String): Path? {
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
        } catch (e: Exception) {
            return null
        }
        return null
    }
}