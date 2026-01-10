data class ProcessCommand(
    val command: String,
    val args: List<String>
) {
    companion object {

        fun from(line: String): ProcessCommand {
            val commandLine = parseCommand(line)
            if (commandLine.size == 1) {
                return ProcessCommand(commandLine[0], emptyList())
            }
            return ProcessCommand(commandLine[0], commandLine.subList(1, commandLine.size))
        }

        private fun parseCommand(line: String): List<String> = line.split(" ")
    }

    fun argsToLine(): String = args.joinToString(" ")
}