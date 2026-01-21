import java.io.OutputStream

class Pipeline(
    val output: OutputStream,
    val error: OutputStream
) {
    fun write(line: String) {
        val writer = output.bufferedWriter()
        CustomLogger.debug("내용을 작성합니다. $line")
        writer.write(line)
        writer.newLine()
        writer.flush()
    }

    fun writeError(line: String) {
        val writer = error.bufferedWriter()
        CustomLogger.debug("에러 내용을 작성합니다. $line")
        writer.write(line)
        writer.newLine()
        writer.flush()
    }
}