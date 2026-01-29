package shell.built.`in`.command

import Pipeline
import ProcessCommand
import shell.built.`in`.BuiltInCommandExecutionResult
import shell.built.`in`.ShellBuiltInCommandType

class Echo : BuiltInCommand {
    override fun type(): ShellBuiltInCommandType {
        return ShellBuiltInCommandType.ECHO
    }

    override fun execute(processCommand: ProcessCommand, pipeline: Pipeline): BuiltInCommandExecutionResult {
        pipeline.write(processCommand.argsToLine())
        return BuiltInCommandExecutionResult.BUILT_IN_EXECUTED
    }
}