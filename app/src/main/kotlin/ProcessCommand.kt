import kotlin.math.max

data class ProcessCommand(
    val command: String,
    val args: List<String>,
    val stdout: String? = null,
) {
    companion object {
        fun from(line: String): ProcessCommand {
            val commandLine = parseCommand(line)
            if (commandLine.size == 1) {
                return ProcessCommand(commandLine[0], emptyList())
            }
            val args = commandLine.subList(1, commandLine.size)

            val pipelineIndex = findPipeline(args)
            CustomLogger.debug("$args 파이프라인 검색 결과: $pipelineIndex")
            // 리다이렉션이 없음
            if (pipelineIndex == -1) {
                return ProcessCommand(commandLine[0], args)
            }

            return ProcessCommand(commandLine[0], args.subList(0, pipelineIndex), args[pipelineIndex + 1])
        }

        private fun findPipeline(args: List<String>): Int {
            val index1 = args.indexOf("1>")
            val index2 = args.indexOf(">")
            if (index1 == -1 && index2 == -1) {
                return -1
            }
            // 둘중 무조건 양수 존재
            return max(index1, index2)
        }

        private fun parseCommand(line: String): List<String> =
            line.split(" ").map { it.trim().removePrefix("\"").removePrefix("\'").removeSuffix("\"").removeSuffix("\'") }
    }

    fun argsToLine(): String = args.joinToString(" ")

    // cd: /does_not_exist: No such file or directory
    fun formatToLine(): String = "$command: ${argsToLine()}:"
}