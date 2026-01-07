import java.io.File
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.Path
import kotlin.io.path.absolute
import kotlin.io.path.absolutePathString
import kotlin.io.path.isDirectory
import kotlin.io.path.isExecutable
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.pathString

object Constant {
    const val PATH = "PATH"
}

fun main() {
    // TODO: Uncomment the code below to pass the first stage
    while (true) {
        print("$ ")
        val line = readlnOrNull() ?: return
        if (line.startsWith("exit")) break

        val commands = parseCommand(line)
        when (val command = ShellCommand.from(commands[0])) {
            ShellCommand.EXIT -> return
            ShellCommand.ECHO -> println(commands.subList(1, commands.size).joinToString(" "))
            ShellCommand.TYPE -> {
                // type ls
                // ls
                val nextCommand = commands[1]
                if (ShellCommand.from(nextCommand).type == ShellCommandType.BUILT_IN) {
                    println("$nextCommand is a shell builtin")
                    continue
                }
                val result = findExecutable(nextCommand)
                if (result != null) {
                    println("$nextCommand is ${result.pathString}")
                    continue
                } else {
                    println("${commands.subList(1, commands.size).joinToString(" ")}: not found")
                }
            }

            else -> println("$line: command not found")
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

private fun parseCommand(line: String): List<String> = line.split(" ")

enum class ShellCommand(val value: String, val type: ShellCommandType) {
    ECHO("echo", ShellCommandType.BUILT_IN),
    EXIT("exit", ShellCommandType.BUILT_IN),
    TYPE("type", ShellCommandType.BUILT_IN),
    LS("ls", ShellCommandType.EXECUTABLE),
    NONE("none", ShellCommandType.EXECUTABLE);

    companion object {
        fun contains(value: String): Boolean =
            entries.any { entry -> entry.value == value }

        fun from(value: String): ShellCommand = entries.firstOrNull { it.value == value } ?: NONE
    }
}

enum class ShellCommandType {
    BUILT_IN,
    EXECUTABLE
}