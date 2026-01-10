import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.isExecutable
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.pathString

object Constant {
    const val PATH = "PATH"
}

fun main() {
    while (true) {
        print("$ ")
        val line = readlnOrNull() ?: return

        val processCommand = ProcessCommand.from(line)
        executeIfBuiltInCommand(processCommand)

    }
}

private fun executeIfBuiltInCommand(processCommand: ProcessCommand) {
    val shellBuiltInCommand = ShellBuiltInCommand.from(processCommand.command) ?: return

    when (shellBuiltInCommand) {
        ShellBuiltInCommand.EXIT -> return
        ShellBuiltInCommand.ECHO -> println(processCommand.argsToLine())
        ShellBuiltInCommand.TYPE -> {
            val args = processCommand.args[0]
            val nextCommand = ShellBuiltInCommand.from(args)

            if (nextCommand == null) {
                println("$args: not found")
                return
            }

            if (nextCommand.type == ShellCommandType.BUILT_IN) {
                println("$nextCommand is a shell builtin")
                return
            }
            val result = findExecutable(args)

            if (result != null) {
                println("$nextCommand is ${result.pathString}")
                return
            } else {
                println("$args: not found")
            }
        }
    }
}

private fun findExecutable(command: String): Path? {
    val pathList = System.getenv(Constant.PATH).split(":")
    for (path in pathList) {
        val path = Path(path)
        val found = recursiveSearch(path, command)
        if (found != null) return found
    }
    return null
}

private fun recursiveSearch(path: Path, to: String): Path? {
    try {
        val entries = path.listDirectoryEntries()
        for (entry in entries) {
            if (entry.isDirectory()) {
                val recursiveFound = recursiveSearch(entry, to)
                if (recursiveFound != null) return recursiveFound
            }
            if (entry.name == to && entry.isExecutable()) return entry.toAbsolutePath()
        }
    } catch (e: Exception) {
        return null
    }
    return null
}