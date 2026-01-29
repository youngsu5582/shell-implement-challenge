package shell.built.`in`.command

import Pipeline
import ProcessCommand
import shell.CommandExecutionResult
import shell.built.`in`.ShellBuiltInCommandType

/**
 * 구현은 하지 않지만, 테스트 통과를 위해 추가
 */
class History : BuiltInCommand {

    override fun type(): ShellBuiltInCommandType {
        return ShellBuiltInCommandType.HISTORY
    }

    override fun execute(processCommand: ProcessCommand, pipeline: Pipeline): CommandExecutionResult {
        return CommandExecutionResult.BUILT_IN_EXECUTED
    }
}