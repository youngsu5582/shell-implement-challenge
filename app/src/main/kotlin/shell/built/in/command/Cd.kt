package shell.built.`in`.command

import Constant.HOME_DIRECTORY_PROPERTY
import Constant.USER_DIRECTORY_PROPERTY
import Pipeline
import ProcessCommand
import shell.CommandExecutionResult
import shell.built.`in`.ShellBuiltInCommandType
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths

class Cd : BuiltInCommand {
    override fun type(): ShellBuiltInCommandType {
        return ShellBuiltInCommandType.CD
    }

    override fun execute(processCommand: ProcessCommand, pipeline: Pipeline): CommandExecutionResult {
        CustomLogger.debug("현재 디렉토리: ${System.getProperty(USER_DIRECTORY_PROPERTY)}")
        val currentDirectory = Paths.get(System.getProperty(USER_DIRECTORY_PROPERTY)).toAbsolutePath()

        if (processCommand.argsToLine() == Constant.HOME_DIRECTORY_SYMBOL) {
            CustomLogger.debug("홈 디렉토리로 이동합니다. 디렉토리: ${System.getenv(HOME_DIRECTORY_PROPERTY)}")
            val homeDirectory = System.getenv(HOME_DIRECTORY_PROPERTY)
            System.setProperty(
                USER_DIRECTORY_PROPERTY,
                Paths.get(homeDirectory).toAbsolutePath().toString()
            )
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
            pipeline.writeError("${processCommand.formatToLine()} No such file or directory")
        }
        return CommandExecutionResult.BUILT_IN_EXECUTED
    }
}