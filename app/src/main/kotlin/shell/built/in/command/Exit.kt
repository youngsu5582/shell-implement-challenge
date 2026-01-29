package shell.built.`in`.command

import Pipeline
import ProcessCommand
import shell.CommandExecutionResult
import shell.built.`in`.ShellBuiltInCommandType

class Exit : BuiltInCommand {
    override fun type(): ShellBuiltInCommandType {
        return ShellBuiltInCommandType.EXIT
    }

    override fun execute(processCommand: ProcessCommand, pipeline: Pipeline): CommandExecutionResult {
        return CommandExecutionResult.EXIT
    }
}