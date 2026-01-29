package shell

import PathFinder
import Pipeline
import ProcessCommand
import StandardOutput
import applyRedirection
import shell.built.`in`.ShellBuiltInCommandExecutor
import shell.built.`in`.ShellBuiltInCommandType
import java.io.OutputStream
import java.util.concurrent.TimeUnit

class ProcessRunner(
    val pathFinder: PathFinder,
    val outputStream: OutputStream,
    val shellBuiltInCommandExecutor: ShellBuiltInCommandExecutor,
) {
    private val writer = outputStream.bufferedWriter()

    fun execute(processCommand: ProcessCommand): CommandExecutionResult {
        val outputStream = toStream(processCommand.stdout)
        val errorStream = toStream(processCommand.stderr)
        val pipeline = Pipeline(outputStream, errorStream)
        if (ShellBuiltInCommandType.from(processCommand.command) == null) {
            return executeShellCommand(processCommand)
        }

        return shellBuiltInCommandExecutor.execute(processCommand, pipeline)
    }

    private fun executeShellCommand(processCommand: ProcessCommand): CommandExecutionResult {
        val path = pathFinder.findExecutable(processCommand.command)
        if (path == null) {
            println("${processCommand.command}: not found", processCommand.stdout)
            return CommandExecutionResult.COMMAND_NOT_FOUND
        }
        CustomLogger.debug("${processCommand.command} 명령어의 경로를 찾았습니다. $path")

        val builder = ProcessBuilder(processCommand.command, *processCommand.args.toTypedArray())
            // 있으면 실행한 현재 디렉토리가 아닌, 명령어를 찾은 디렉토리로 이동해서 실행함
            // .directory(path.parent.toFile())
            // 있으면, stdout 과 stderr 를 하나의 스트림으로 합침
            .redirectErrorStream(false)

        builder.environment()["PATH"] = path.parent.toString()

        builder.applyRedirection(command = processCommand)

        val process = builder.start()

        // 프로세스 출력 → 우리 stream 으로 복사
        // 테스트 때문에 PIPE + inputStream 조합을 사용. INHERIT 만 해도, 자바 프로세스 콘솔 출력에 같이 포함된다.
        if (processCommand.stdout == null) {
            process.inputStream.bufferedReader().forEachLine { line ->
                CustomLogger.debug("프로세스 실행 출력 스트림: $line")
                println(line, processCommand.stdout)
            }
        }

        if (processCommand.stderr == null) {
            process.errorStream.bufferedReader().forEachLine { line ->
                CustomLogger.debug("프로세스 실행 에러 스트림: $line")
                println(line, processCommand.stderr)
            }
        }

        process.waitFor(1, TimeUnit.MINUTES)

        return CommandExecutionResult.COMMAND_EXECUTED
    }

    private fun toStream(stdout: StandardOutput?): OutputStream {
        if (stdout == null) {
            CustomLogger.debug("비어 있으므로, 기본 스트림을 사용합니다.")
            return outputStream
        }
        CustomLogger.debug("경로가 있으므로, 파일 스트림을 사용합니다. $stdout")
        return stdout.openOutputStream()
    }

    private fun println(message: String, stdout: StandardOutput? = null) {
        if (stdout == null) {
            writer.write(message)
            writer.newLine()
            writer.flush()
            return
        }
        stdout.printText(message)
    }
}