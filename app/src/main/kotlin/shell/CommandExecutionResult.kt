package shell

enum class CommandExecutionResult {
    EXIT,
    BUILT_IN_EXECUTED,
    COMMAND_EXECUTED,
    COMMAND_NOT_FOUND,
    NOT_BUILT_IN
}