package built.`in`.command

import Pipeline
import ProcessCommand
import built.`in`.BuiltInCommandExecutionResult
import built.`in`.ShellBuiltInCommandType

class Exit : BuiltInCommand {
    override fun type(): ShellBuiltInCommandType {
        return ShellBuiltInCommandType.EXIT
    }

    override fun execute(processCommand: ProcessCommand, pipeline: Pipeline): BuiltInCommandExecutionResult {
        return BuiltInCommandExecutionResult.EXIT
    }
}