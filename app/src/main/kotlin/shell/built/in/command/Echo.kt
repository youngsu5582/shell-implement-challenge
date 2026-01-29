package shell.built.`in`.command

import Pipeline
import ProcessCommand
import shell.CommandExecutionResult
import shell.built.`in`.ShellBuiltInCommandType

class Echo : BuiltInCommand {
    override fun type(): ShellBuiltInCommandType {
        return ShellBuiltInCommandType.ECHO
    }

    override fun execute(processCommand: ProcessCommand, pipeline: Pipeline): CommandExecutionResult {
        pipeline.write(processCommand.argsToLine())
        return CommandExecutionResult.BUILT_IN_EXECUTED
    }
}