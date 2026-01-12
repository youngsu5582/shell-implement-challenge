## 코드의 놓침

아무생각 없이, 당연히 파이프라인으로 파일 생성하고 거기에 쓰면 된다고 생각했다.

-> 라인마다 새로운 파일을 작성해서 덮어쓰게 된다!!!!!

즉, 제일 처음 생성해서 파이프라인을 열고
그 다음에는 거기에 결과가 저장되게 해야한다...

```kotlin
private fun println(message: String, stdout: String? = null) {
    if (stdout.isNullOrBlank()) {
        writer.write(message)
        writer.newLine()
        writer.flush()
        return
    }
    val path = Paths.get(stdout.trim())
    val directory = path.parent.toFile()
    val result = directory.mkdirs()
    CustomLogger.debug("$directory 디렉토리가 있는지 확인 및 생성합니다. $result")

    try {
        CustomLogger.debug("$path 에 파일 생성을 시도합니다.")
        Files.createFile(path)
    } catch (e: FileAlreadyExistsException) {
        CustomLogger.debug("이미 파일이 존재합니다.")
    }
    path.writeText(message)
}
```

해당 구조부터 조금 아쉬운거 같다.
애초에 ProcessBuilder 에서 바로 경로로 리다이렉션 되게하는게 베스트인듯

---

```kotlin
// stdout 이 있으면 지정
if (processCommand.stdout != null) {
    val outputFile = File(processCommand.stdout)
    outputFile.parentFile.mkdirs()

    // 파일 없으면 자동 생성
    builder.redirectOutput(ProcessBuilder.Redirect.to(outputFile))
} else {
    builder.redirectOutput(ProcessBuilder.Redirect.PIPE)
    builder.redirectError(ProcessBuilder.Redirect.PIPE)
}

val process = builder.start()

// 프로세스 출력 → 우리 outputStream으로 복사
if (processCommand.stdout.isNullOrEmpty()) {
    process.inputStream.bufferedReader().forEachLine { line ->
        CustomLogger.debug("프로세스 실행 출력 스트림: $line")
        println(line, processCommand.stdout)
    }

    process.errorStream.bufferedReader().forEachLine { line ->
        CustomLogger.debug("프로세스 실행 에러 스트림: $line")
        println(line)
    }
}
```

일단, stdout 의 존재 유무에 따라 다르게 다이렉팅 되게 설정

-> 차차, 기존 Built-in 부분들도 개선되어야 할듯..