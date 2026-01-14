data class ProcessCommand(
    val command: String,
    val args: List<String>,
    val stdout: String? = null,
    val stderr: String? = null
) {
    companion object {
        fun from(line: String): ProcessCommand {
            val commandLine = parseCommand(line)
            if (commandLine.size == 1) {
                return ProcessCommand(commandLine[0], emptyList())
            }

            val pipelineIndex = findExistPipeline(commandLine)

            // 파이프라인이 없으면 그대로 반환
            if (pipelineIndex == -1) {
                return ProcessCommand(commandLine[0], commandLine.subList(1, commandLine.size))
            }

            val outputPipelineIndex = findOutputPipeline(commandLine)
            val errorPipelineIndex = findErrorPipeline(commandLine)
            CustomLogger.debug("$line 파이프라인 검색 결과: $outputPipelineIndex")
            CustomLogger.debug("$line 에러 파이프라인 검색 결과: $errorPipelineIndex")
            return ProcessCommand(
                command = commandLine[0],
                args = commandLine.subList(1, pipelineIndex),
                stdout = if (outputPipelineIndex != -1) commandLine.getOrNull(outputPipelineIndex + 1) else null,
                stderr = if (errorPipelineIndex != -1) commandLine.getOrNull(errorPipelineIndex + 1) else null,
            )
        }

        private fun findExistPipeline(args: List<String>): Int = args.indexOfFirst { it.contains(">") }
        private fun findOutputPipeline(args: List<String>): Int =
            args.indexOfFirst { it == "1>" || it == ">" }

        private fun findErrorPipeline(args: List<String>): Int = args.indexOfFirst { it == "2>" }

        private fun parseCommand(line: String): List<String> =
            line.split(" ")
                .map { it.trim().removePrefix("\"").removePrefix("\'").removeSuffix("\"").removeSuffix("\'") }
    }

    fun argsToLine(): String = args.joinToString(" ")

    // cd: /does_not_exist: No such file or directory
    fun formatToLine(): String = "$command: ${argsToLine()}:"
}