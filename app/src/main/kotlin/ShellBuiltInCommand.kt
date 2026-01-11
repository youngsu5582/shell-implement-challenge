enum class ShellBuiltInCommand(val value: String, val type: ShellCommandType) {
    ECHO("echo", ShellCommandType.BUILT_IN),
    EXIT("exit", ShellCommandType.BUILT_IN),
    TYPE("type", ShellCommandType.BUILT_IN),
    PWD("pwd", ShellCommandType.BUILT_IN),
    CD("cd", ShellCommandType.BUILT_IN),
    ;

    companion object {
        fun contains(value: String): Boolean =
            entries.any { entry -> entry.value == value }

        fun from(value: String): ShellBuiltInCommand? = entries.firstOrNull { it.value == value }
    }
}

enum class ShellCommandType {
    BUILT_IN,
    EXECUTABLE
}