class ProcessCommandLine(
    val commandLine: List<ProcessCommand>,
) {
    companion object {
        fun from(line: String): ProcessCommandLine {
            return ProcessCommandLine(
                line.split("|")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .map { ProcessCommand.from(it) })
        }
    }
}