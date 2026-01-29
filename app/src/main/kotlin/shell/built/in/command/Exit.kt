package shell.built.`in`.command

import Pipeline
import ProcessCommand
import shell.built.`in`.BuiltInCommandExecutionResult
import shell.built.`in`.ShellBuiltInCommandType

class Exit : BuiltInCommand {
    override fun type(): ShellBuiltInCommandType {
        return ShellBuiltInCommandType.EXIT
    }

    override fun execute(processCommand: ProcessCommand, pipeline: Pipeline): BuiltInCommandExecutionResult {
        return BuiltInCommandExecutionResult.EXIT
    }
}