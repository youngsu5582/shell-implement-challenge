package shell.built.`in`.command

import Pipeline
import ProcessCommand
import shell.built.`in`.BuiltInCommandExecutionResult
import shell.built.`in`.ShellBuiltInCommandType

/**
 * 구현은 하지 않지만, 테스트 통과를 위해 추가
 */
class History : BuiltInCommand {

    override fun type(): ShellBuiltInCommandType {
        return ShellBuiltInCommandType.HISTORY
    }

    override fun execute(processCommand: ProcessCommand, pipeline: Pipeline): BuiltInCommandExecutionResult {
        return BuiltInCommandExecutionResult.BUILT_IN_EXECUTED
    }
}