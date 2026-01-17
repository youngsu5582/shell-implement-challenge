object Constant {
    const val PATH = "PATH"
    const val HOME_DIRECTORY_SYMBOL = "~"
    const val HOME_DIRECTORY_PROPERTY = "HOME"
    const val USER_DIRECTORY_PROPERTY = "user.dir"
}

fun main() {
    CustomLogger.setLevel(LogLevel.INFO)
    val pathList = System.getenv(Constant.PATH).split(":")
    val application = ShellApplication(pathList = pathList)
    application.start()
}