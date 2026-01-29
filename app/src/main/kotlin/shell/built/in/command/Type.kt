package shell.built.`in`.command

import PathFinder
import Pipeline
import ProcessCommand
import shell.built.`in`.BuiltInCommandExecutionResult
import shell.built.`in`.ShellBuiltInCommandType
import shell.built.`in`.ShellCommandType
import kotlin.io.path.pathString

class Type(
    private val pathFinder: PathFinder,
) : BuiltInCommand {
    override fun type(): ShellBuiltInCommandType {
        return ShellBuiltInCommandType.TYPE
    }

    override fun execute(processCommand: ProcessCommand, pipeline: Pipeline): BuiltInCommandExecutionResult {
        val args = processCommand.args[0]
        val nextCommand = ShellBuiltInCommandType.Companion.from(args)

        if (nextCommand?.type == ShellCommandType.BUILT_IN) {
            pipeline.write("${nextCommand.value} is a shell builtin")
            return BuiltInCommandExecutionResult.BUILT_IN_EXECUTED
        }

        val result = pathFinder.findExecutable(args)

        if (result != null) {
            pipeline.write("$args is ${result.pathString}")
        } else {
            pipeline.writeError("$args: not found")
        }
        return BuiltInCommandExecutionResult.BUILT_IN_EXECUTED
    }
}