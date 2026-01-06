fun main() {
    // TODO: Uncomment the code below to pass the first stage
    while (true) {
        print("$ ")
        val command = readlnOrNull() ?: return
        println("$command: command not found")
    }
}
