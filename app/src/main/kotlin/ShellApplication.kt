import shell.built.`in`.BuiltInCommandExecutionResult
import shell.built.`in`.ShellBuiltInCommandExecutor
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
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
            val outputStream = toStream(processCommand.stdout, true)
            val errorStream = toStream(processCommand.stderr, true)
            val pipeline = Pipeline(outputStream, errorStream)
            val executionResult = shellBuiltInCommandExecutor.execute(processCommand, pipeline)

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

    private fun toStream(stdout: StandardOutput?, last: Boolean): OutputStream {
        if (!last) {
            CustomLogger.debug("마지막 명령어가 아니므로, 새로운 스트림을 생성합니다")
            return OutputStream.nullOutputStream()
        }
        if (stdout == null) {
            CustomLogger.debug("비어 있으므로, 기본 스트림을 사용합니다.")
            return outputStream
        }
        CustomLogger.debug("경로가 있으므로, 파일 스트림을 사용합니다. $stdout")
        return stdout.openOutputStream()
    }

    fun executePipeline(commandList: List<ProcessCommand>) {
        // BuiltIn 명령어가 포함되어 있는지 확인
        val hasBuiltIn = commandList.any { shellBuiltInCommandExecutor.isBuiltIn(it) }

        if (hasBuiltIn) {
            CustomLogger.debug("BuiltIn 명령어가 포함된 파이프라인 - 순차 실행")
            executeSequentialPipeline(commandList)
        } else {
            CustomLogger.debug("외부 명령어만 있는 파이프라인 - 동시 실행")
            executeConcurrentPipeline(commandList)
        }
    }

    /**
     * BuiltIn 명령어가 포함된 파이프라인: 순차 실행 + 버퍼 방식
     */
    private fun executeSequentialPipeline(commandList: List<ProcessCommand>) {
        var prevOutputBuffer: ByteArrayOutputStream? = null

        for ((index, command) in commandList.withIndex()) {
            val isLastCommand = index == commandList.lastIndex
            CustomLogger.debug("실행할 명령어: $command (마지막: $isLastCommand)")

            val inputStream: InputStream? = prevOutputBuffer?.let {
                CustomLogger.debug("이전 파이프라인의 결과를 사용합니다. 크기: ${it.size()}")
                ByteArrayInputStream(it.toByteArray())
            }

            val currentOutputBuffer = if (isLastCommand) null else ByteArrayOutputStream()
            val outputStream: OutputStream = if (isLastCommand) {
                toStream(command.stdout, true)
            } else {
                currentOutputBuffer!!
            }
            val errorStream: OutputStream = if (isLastCommand) {
                toStream(command.stderr, true)
            } else {
                OutputStream.nullOutputStream()
            }

            val pipeline = Pipeline(outputStream, errorStream)
            val builtInResult = shellBuiltInCommandExecutor.execute(command, pipeline)
            CustomLogger.debug("${command} 실행결과 : $builtInResult")

            if (builtInResult == BuiltInCommandExecutionResult.BUILT_IN_EXECUTED) {
                prevOutputBuffer = currentOutputBuffer
                CustomLogger.debug("BuiltIn 명령어 완료, 출력 버퍼 크기: ${currentOutputBuffer?.size() ?: 0}")
                continue
            }
            if (builtInResult == BuiltInCommandExecutionResult.EXIT) {
                return
            }

            val path = pathFinder.findExecutable(command.command)
            if (path == null) {
                println("${command.command}: not found")
                continue
            }

            val builder = ProcessBuilder(command.command, *command.args.toTypedArray()).apply {
                environment()["PATH"] = path.parent.toString()
                redirectErrorStream(false)
            }

            val process = builder.start()

            if (inputStream != null) {
                inputStream.transferTo(process.outputStream)
                process.outputStream.close()
            }

            process.waitFor()

            if (isLastCommand) {
                if (command.stdout == null) {
                    process.inputStream.bufferedReader().forEachLine { line ->
                        CustomLogger.debug("파이프라인 최종 출력: $line")
                        println(line)
                    }
                }
                if (command.stderr == null) {
                    process.errorStream.bufferedReader().forEachLine { line ->
                        CustomLogger.debug("파이프라인 에러 출력: $line")
                        println(line)
                    }
                }
            } else {
                process.inputStream.transferTo(currentOutputBuffer!!)
                prevOutputBuffer = currentOutputBuffer
                CustomLogger.debug("외부 명령어 완료, 출력 버퍼 크기: ${currentOutputBuffer.size()}")
            }
        }
    }

    /**
     * 외부 명령어만 있는 파이프라인: 모든 프로세스 동시 시작 + 스레드로 스트림 연결
     * 일반적인 쉘의 파이프라인 동작 방식
     */
    private fun executeConcurrentPipeline(commandList: List<ProcessCommand>) {
        // 1. 모든 프로세스 시작
        val processes = mutableListOf<Process>()
        for (command in commandList) {
            val path = pathFinder.findExecutable(command.command)
            if (path == null) {
                println("${command.command}: not found")
                // 이미 시작된 프로세스들 종료
                processes.forEach { it.destroy() }
                return
            }

            val process = ProcessBuilder(command.command, *command.args.toTypedArray()).apply {
                environment()["PATH"] = path.parent.toString()
                redirectErrorStream(false)
            }.start()

            processes.add(process)
            CustomLogger.debug("프로세스 시작: ${command.command}")
        }

        // 2. 스레드로 프로세스 간 스트림 연결 (process[i].stdout → process[i+1].stdin)
        // transferTo는 EOF까지 블로킹되므로, 실시간 전달을 위해 버퍼 단위로 읽고 즉시 flush
        val pumpThreads = mutableListOf<Thread>()
        for (i in 0 until processes.size - 1) {
            val fromProcess = processes[i]
            val toProcess = processes[i + 1]

            val thread = Thread {
                try {
                    val buffer = ByteArray(8192)
                    val input = fromProcess.inputStream
                    val output = toProcess.outputStream

                    while (true) {
                        val bytesRead = input.read(buffer)
                        if (bytesRead == -1) break
                        output.write(buffer, 0, bytesRead)
                        output.flush()  // 즉시 다음 프로세스로 전달
                    }
                    output.close()
                    CustomLogger.debug("스트림 연결 완료: ${commandList[i].command} → ${commandList[i + 1].command}")
                } catch (e: Exception) {
                    CustomLogger.debug("스트림 연결 중 예외: ${e.message}")
                }
            }
            thread.start()
            pumpThreads.add(thread)
        }

        // 3. 마지막 프로세스의 출력 처리
        val lastProcess = processes.last()
        val lastCommand = commandList.last()

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

        // 4. 마지막 프로세스 대기 (head가 종료되면 파이프 끊김 → tail -f도 SIGPIPE로 종료)
        lastProcess.waitFor()
        CustomLogger.debug("마지막 프로세스 종료")

        // 5. 나머지 프로세스 정리
        processes.dropLast(1).forEach { process ->
            if (process.isAlive) {
                process.destroy()
                CustomLogger.debug("프로세스 강제 종료")
            }
        }

        // 6. pump 스레드 대기
        pumpThreads.forEach { it.join(1000) }
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
