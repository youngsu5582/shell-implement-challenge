## Pipelines with built-ins

로직이 또 깨졌다.  물론, 이건 학습이 의도해서 깨뜨렸다.

```kotlin
ProcessBuilder.startPipeline(builders)
```

를 사용했는데 `built-in commands 도 파이프라인에 넣어야 한다.` 라는 요구사항이 생겼다.
(당연히, path 순회해서 직접 터미널이 실행하는게 안된다.)

즉, 이전단계의 결과를 현재단계의 stdin 으로 보내야한다.

```kotlin
private fun pump(input: InputStream, output: OutputStream) {
  input.copyTo(output)
  output.flush()
  output.close()
}
```

일종의 펌핑
이거 꽤나 어려운게

JVM 의 Stream 을 완벽히 이해하지 못하면 코드를 작성할 수 조차 없다.
close 를 제대로 하지 않으면 종료 자체가 되지 않기 때문.

---

결국, Claude Code 의 도움을 받았다...  

input, output 의 이름이 반대인게 몹시 헷갈리기도 하고  
파이프라인 처리가 너무 어려웠다.

우리가 해야하는건, 여러가지이다.  

- 이전 명령어의 출력은 현재 명령어의 입력으로 전환

```kotlin
val inputStream: InputStream? = prevOutputBuffer?.let {
    CustomLogger.debug("이전 파이프라인의 결과를 사용합니다. 크기: ${it.size()}")
    ByteArrayInputStream(it.toByteArray())
}
```

- 중간 명령어는 스트림에 저장
    - 마지막 명령이면, 불필요하니 null <-> 아니라면, 버퍼 생성
    - 마지막 명령이면, stdout 처리  <-> 아니라면, 버퍼 사용

```kotlin
val currentOutputBuffer = if (isLastCommand) null else ByteArrayOutputStream()
val outputStream: OutputStream = if (isLastCommand) {
    toStream(command.stdout, true)
} else {
    currentOutputBuffer!!
}
```

- BUILT_IN 명령어면 버퍼에 결과 저장

```kotlin
if (builtInResult == BuiltInCommandExecutionResult.BUILT_IN_EXECUTED) {
    // 중간 명령어의 출력 버퍼를 다음 명령어를 위해 저장
    prevOutputBuffer = currentOutputBuffer
    CustomLogger.debug("BuiltIn 명령어 완료, 출력 버퍼 크기: ${currentOutputBuffer?.size() ?: 0}")
    continue
}
```

- 이전 명령어의 출력을 현재 프로세스 입력으로 전달

```kotlin
if (inputStream != null) {
    inputStream.transferTo(process.outputStream)
    process.outputStream.close()
}
```

- 프로세스 출력은
  - 마지막 명령어면, 파일 처리 or 출력
  - 중간 명령어면, 출력을 버퍼에 저장

```kotlin
if (isLastCommand) {
    if (command.stdout == null) {
        process.inputStream.bufferedReader().forEachLine { line ->
            println(line)
        }
    }
    if (command.stderr == null) {
        process.errorStream.bufferedReader().forEachLine { line ->
            println(line)
        }
    }
} else {
    process.inputStream.transferTo(currentOutputBuffer!!)
    prevOutputBuffer = currentOutputBuffer
}
```

너무 어렵다..    
이거...  

나중에 다시 시도해봐야지.    