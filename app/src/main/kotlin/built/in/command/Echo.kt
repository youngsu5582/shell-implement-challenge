package built.`in`.command

import Pipeline
import ProcessCommand
import built.`in`.BuiltInCommandExecutionResult
import built.`in`.ShellBuiltInCommandType

class Echo : BuiltInCommand {
    override fun type(): ShellBuiltInCommandType {
        return ShellBuiltInCommandType.ECHO
    }

    override fun execute(processCommand: ProcessCommand, pipeline: Pipeline): BuiltInCommandExecutionResult {
        pipeline.write(processCommand.argsToLine())
        return BuiltInCommandExecutionResult.BUILT_IN_EXECUTED
    }
}