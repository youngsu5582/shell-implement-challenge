import java.io.File

fun ProcessBuilder.applyRedirection(command: ProcessCommand) {

    if (command.stdout != null) {
        val outputFile = File(command.stdout.path)
        val redirect = when (command.stdout.option) {
            StandardOption.APPEND -> ProcessBuilder.Redirect.appendTo(outputFile)
            StandardOption.OVERWRITE -> ProcessBuilder.Redirect.to(outputFile)
        }
        this.redirectOutput(redirect)
    } else {
        this.redirectOutput(ProcessBuilder.Redirect.PIPE)
    }

    if (command.stderr != null) {
        val outputFile = File(command.stderr.path)
        val redirect = when (command.stderr.option) {
            StandardOption.APPEND -> ProcessBuilder.Redirect.appendTo(outputFile)
            StandardOption.OVERWRITE -> ProcessBuilder.Redirect.to(outputFile)
        }
        this.redirectError(redirect)
    } else {
        this.redirectError(ProcessBuilder.Redirect.PIPE)
    }
}