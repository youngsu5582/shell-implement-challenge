import built.`in`.BuiltInCommandExecutionResult
import built.`in`.ShellBuiltInCommandExecutor
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.TimeUnit
import kotlin.io.bufferedReader


class ShellApplication(
    private val inputStream: InputStream = System.`in`,
    private val outputStream: OutputStream = System.out,
    pathList: List<String>
) {
    private val reader = inputStream.bufferedReader()
    private val writer = outputStream.bufferedWriter()
    private val pathFinder = PathFinder(pathList)
    private val shellBuiltInCommandExecutor = ShellBuiltInCommandExecutor(inputStream, outputStream, pathFinder)

    fun start() {
        while (true) {
            print("$ ")
            val line = readLine()

            val processCommandLine = ProcessCommandLine.from(line)

            // 파이프라인이 있는 경우 (커맨드가 2개 이상)
            if (processCommandLine.commandLine.size > 1) {
                CustomLogger.debug("파이프라인 실행: ${processCommandLine.commandLine}")
                executePipeline(processCommandLine.commandLine)
                continue
            }

            val processCommand = processCommandLine.commandLine[0]
            val executionResult = shellBuiltInCommandExecutor.execute(processCommand)

            when (executionResult) {
                BuiltInCommandExecutionResult.EXIT -> return
                BuiltInCommandExecutionResult.BUILT_IN_EXECUTED -> continue
                BuiltInCommandExecutionResult.NOT_BUILT_IN -> {
                    CustomLogger.debug("명령어로 실행합니다. 명령어: $processCommand")
                    val result = executeShellCommand(processCommand)
                    CustomLogger.debug("명령어 실행결과: $result")
                }
            }
        }
    }

    private fun executePipeline(commandList: List<ProcessCommand>) {
        // ProcessBuilder 리스트 생성
        val builders = commandList.mapIndexed { index, command ->
            val path = pathFinder.findExecutable(command.command)
            if (path == null) {
                println("${command.command}: not found")
                return
            }

            ProcessBuilder(command.command, *command.args.toTypedArray()).apply {
                environment()["PATH"] = path.parent.toString()
                redirectErrorStream(false)

                // 마지막 프로세스에만 출력 리다이렉션 적용 (중간 프로세스는 파이프로 연결)
                if (index == commandList.lastIndex) {
                    applyRedirection(command = command)
                }
            }
        }

        val processList = ProcessBuilder.startPipeline(builders)

        // 마지막 프로세스의 출력 처리
        val lastCommand = commandList.last()
        val lastProcess = processList.last()

        if (lastCommand.stdout == null) {
            lastProcess.inputStream.bufferedReader().forEachLine { line ->
                CustomLogger.debug("파이프라인 최종 출력: $line")
                println(line)
            }
        }

        if (lastCommand.stderr == null) {
            lastProcess.errorStream.bufferedReader().forEachLine { line ->
                CustomLogger.debug("파이프라인 에러 출력: $line")
                println(line)
            }
        }

        // 모든 프로세스 완료 대기
        processList.forEach { it.waitFor(1, TimeUnit.MINUTES) }
    }

    private fun readLine() = reader.readLine()
    private fun print(message: String) {
        writer.write(message)
        writer.flush()
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

    private fun executeShellCommand(processCommand: ProcessCommand): Boolean {
        val path = pathFinder.findExecutable(processCommand.command)
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

        return process
            .waitFor(1, TimeUnit.MINUTES)
    }
}