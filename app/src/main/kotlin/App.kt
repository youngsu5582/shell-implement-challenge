object Constant {
    const val PATH = "PATH"
}

fun main() {
    CustomLogger.setLevel(LogLevel.INFO)
    val pathList = System.getenv(Constant.PATH).split(":")
    val application = ShellApplication(pathList = pathList)
    application.start()
}