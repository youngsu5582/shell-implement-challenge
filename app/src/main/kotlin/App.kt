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
                val nextCommand = ShellCommand.from(commands[1])
                if (nextCommand == ShellCommand.NONE) println(
                    "${commands.subList(1, commands.size).joinToString(" ")}: not found"
                )
                else println("${nextCommand.value} is a shell builtin")
            }

            ShellCommand.NONE -> println("$line: command not found")
        }
    }
}

private fun parseCommand(line: String): List<String> = line.split(" ")

enum class ShellCommand(val value: String) {
    ECHO("echo"),
    EXIT("exit"),
    TYPE("type"),
    NONE("none");

    companion object {
        fun contains(value: String): Boolean =
            entries.any { entry -> entry.value == value }

        fun from(value: String): ShellCommand = entries.firstOrNull { it.value == value } ?: NONE
    }
}