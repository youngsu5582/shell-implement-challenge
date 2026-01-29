package shell.built.`in`.command

import Pipeline
import ProcessCommand
import shell.built.`in`.BuiltInCommandExecutionResult
import shell.built.`in`.ShellBuiltInCommandType

interface BuiltInCommand {

    fun type(): ShellBuiltInCommandType
    fun execute(processCommand: ProcessCommand, pipeline: Pipeline): BuiltInCommandExecutionResult
}