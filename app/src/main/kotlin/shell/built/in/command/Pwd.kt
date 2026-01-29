package shell.built.`in`.command

import Constant.USER_DIRECTORY_PROPERTY
import Pipeline
import ProcessCommand
import shell.built.`in`.BuiltInCommandExecutionResult
import shell.built.`in`.ShellBuiltInCommandType
import java.nio.file.Paths

class Pwd : BuiltInCommand {
    override fun type(): ShellBuiltInCommandType {
        return ShellBuiltInCommandType.PWD
    }

    override fun execute(processCommand: ProcessCommand, pipeline: Pipeline): BuiltInCommandExecutionResult {
        val path = Paths.get(System.getProperty(USER_DIRECTORY_PROPERTY)).toAbsolutePath().toString()
        pipeline.write(path)
        return BuiltInCommandExecutionResult.BUILT_IN_EXECUTED
    }
}