package built.`in`.command

import Pipeline
import ProcessCommand
import built.`in`.BuiltInCommandExecutionResult
import built.`in`.ShellBuiltInCommandType

interface BuiltInCommand {

    fun type(): ShellBuiltInCommandType
    fun execute(processCommand: ProcessCommand, pipeline: Pipeline): BuiltInCommandExecutionResult
}