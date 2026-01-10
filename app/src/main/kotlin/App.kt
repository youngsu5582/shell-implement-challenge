import java.nio.file.Path
import java.util.concurrent.TimeUnit
import kotlin.io.path.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.isExecutable
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.pathString
import kotlin.system.exitProcess

object Constant {
    const val PATH = "PATH"
}

fun main() {
    CustomLogger.setLevel(LogLevel.DEBUG)
    while (true) {
        print("$ ")
        val line = readlnOrNull() ?: return

        val processCommand = ProcessCommand.from(line)
        val executeResultInBuiltInCommand = executeIfBuiltInCommand(processCommand)
        if (executeResultInBuiltInCommand) {
            CustomLogger.debug("Built-In 명령어로 실행했습니다. 명령어: $processCommand")
            continue
        }
    }
}

/**
 * Built In 커맨드이고,
 * 함수가 제대로 실행이 되었다면 true 를 리턴
 * 그렇지 않다면, false 를 리턴
 */
private fun executeIfBuiltInCommand(processCommand: ProcessCommand): Boolean {
    val shellBuiltInCommand = ShellBuiltInCommand.from(processCommand.command) ?: return false

    when (shellBuiltInCommand) {
        ShellBuiltInCommand.EXIT -> exitProcess(0)
        ShellBuiltInCommand.ECHO -> {
            println(processCommand.argsToLine())
            return true
        }

        ShellBuiltInCommand.TYPE -> {
            val args = processCommand.args[0]

            val nextCommand = ShellBuiltInCommand.from(args)

            if (nextCommand?.type == ShellCommandType.BUILT_IN) {
                println("${nextCommand.value} is a shell builtin")
                return true
            }

            val result = findExecutable(args)

            if (result != null) {
                println("$args is ${result.pathString}")
                return true
            } else {
                println("$args: not found")
            }
        }
    }
    return false
}

private fun findExecutable(command: String): Path? {
    val pathList = System.getenv(Constant.PATH).split(":")
    CustomLogger.debug("PATH 경로 목록: $pathList")
    for (path in pathList) {
        val path = Path(path)
        val found = recursiveSearch(path, command)
        if (found != null) {
            return found
        }
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