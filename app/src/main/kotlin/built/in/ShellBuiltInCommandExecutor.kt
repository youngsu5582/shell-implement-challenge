package built.`in`

import PathFinder
import Pipeline
import ProcessCommand
import StandardOutput
import built.`in`.command.BuiltInCommand
import built.`in`.command.Cd
import built.`in`.command.Echo
import built.`in`.command.Exit
import built.`in`.command.History
import built.`in`.command.Pwd
import built.`in`.command.Type
import java.io.InputStream
import java.io.OutputStream

class ShellBuiltInCommandExecutor(
    private val inputStream: InputStream,
    private val outputStream: OutputStream,
    pathFinder: PathFinder
) {
    private val commandList: List<BuiltInCommand> = listOf(
        Cd(),
        Echo(),
        Exit(),
        Pwd(),
        History(),
        Type(pathFinder)
    )

    fun execute(processCommand: ProcessCommand, pipeline: Pipeline): BuiltInCommandExecutionResult {
        val commandType = ShellBuiltInCommandType.from(processCommand.command)
        if (commandType == null) {
            return BuiltInCommandExecutionResult.NOT_BUILT_IN
        }
        CustomLogger.debug("Executing command: $commandType")
        val command = getCommand(commandType)
        return command.execute(processCommand, pipeline)
    }

    fun isBuiltIn(processCommand: ProcessCommand): Boolean {
        return ShellBuiltInCommandType.from(processCommand.command) != null
    }

    private fun getCommand(builtInCommand: ShellBuiltInCommandType): BuiltInCommand =
        commandList.first { it.type() == builtInCommand }

    private fun toStream(stdout: StandardOutput?) = stdout?.openOutputStream() ?: outputStream
}