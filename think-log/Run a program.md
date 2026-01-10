## 코드의 고민

명령어는

```shell
명령어 <args0> <args1> ...
```

와 같이 이루어진다.

근데, 명령어가 빌트인인지 확인을 해야한다.
즉 처음 명령어를 꺼내서 확인한다.

이때 List<String> 상태.
(0번째는 command, 1~n번째는 args)

새로운 객체를 만들고 싶다.

```kotlin
private class ProcessLine(
    val path: Path,
    val args: List<String>
)
```

> args 는 비어있을수 있음

- subList 를 해서 넣을지
- 생성자에서 적절히 첫번째껄 제거할지
- 제일 처음 Built-In 꺼낼때부터 객체를 만들어서 분리할지

고민

---

### 개선방향

일단, ShellCommand 를

```kotlin
enum class ShellCommand(val value: String, val type: ShellCommandType) {
    ECHO("echo", ShellCommandType.BUILT_IN),
    EXIT("exit", ShellCommandType.BUILT_IN),
    TYPE("type", ShellCommandType.BUILT_IN),
    COMMAND("", ShellCommandType.EXECUTABLE);

    companion object {
        fun contains(value: String): Boolean =
            entries.any { entry -> entry.value == value }

        fun from(value: String): ShellCommand = entries.firstOrNull { it.value == value } ?: COMMAND
    }
}
```

를 Built-in 명령어만 넣고 Command 는 따로 처리하도록 변경
그 후, 메인 메소드에서 실행되던 로직도 분리

```kotlin
private fun executeIfBuiltInCommand(processCommand: ProcessCommand) {
    val shellCommand = ShellCommand.from(processCommand.command) ?: return

    when (shellCommand) {
        ShellCommand.EXIT -> return
        ShellCommand.ECHO -> println(processCommand.argsToLine())
        ShellCommand.TYPE -> {
            val args = processCommand.args[0]
            val nextCommand = ShellCommand.from(args)

            if (nextCommand == null) {
                println("$args: not found")
                return
            }

            if (nextCommand.type == ShellCommandType.BUILT_IN) {
                println("$nextCommand is a shell builtin")
                return
            }
            val result = findExecutable(args)

            if (result != null) {
                println("$nextCommand is ${result.pathString}")
                return
            } else {
                println("$args: not found")
            }
        }
    }
}
```

여전히 로직이 조금 마음에 안들긴 하지만...

- 불필요한 ShellCommand 확인 (ProcessCommand 를 만들때, Built-In 인지도 알 수 있을것만 같음)

---

## Stream 으로 변환

기존, `println` 과 `readLine` 구문을 `InputStream`, `OutputStream` 으로 변환했다.
테스트 할때 용이하게 하기 위해서다.

- 단계별로, 요구사항이 심화되고 있는데 현재, 테스트가 누락되어서 코드 수정중 변경사항으로 실패함
- TDD 및 제출전 테스트를 보장하기 위해서
