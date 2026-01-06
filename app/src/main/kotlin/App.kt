fun main() {
    // TODO: Uncomment the code below to pass the first stage
    while (true) {
        print("$ ")
        val command = readlnOrNull() ?: return
        if (command.startsWith("exit")) break

        val commands = parseCommand(command)
        executeCommand(commands)
    }
}

private fun parseCommand(line: String): List<String> = line.split(" ")
private fun executeCommand(commands: List<String>) {
    val command = commands[0];
    when (command) {
        "echo" -> {
            println(
                commands.subList(1, commands.size)
                    .joinToString(" ")
            )
        }

        else -> println("$command: command not found")
    }
}