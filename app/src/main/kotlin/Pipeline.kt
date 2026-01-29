import java.io.OutputStream

class Pipeline(
    val output: OutputStream,
    val error: OutputStream
) {
    private val outputWriter = output.bufferedWriter()
    private val errorWriter = error.bufferedWriter()

    fun write(line: String) {
        CustomLogger.debug("내용을 작성합니다. $line")
        outputWriter.write(line)
        outputWriter.newLine()
        outputWriter.flush()
    }

    fun writeError(line: String) {
        CustomLogger.debug("에러 내용을 작성합니다. $line")
        errorWriter.write(line)
        errorWriter.newLine()
        errorWriter.flush()
    }
}