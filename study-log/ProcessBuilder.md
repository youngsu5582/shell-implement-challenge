## ProcessBuilder

JVM 에서 외부 프로세스를 실행하고, 입출력을 제어하는 표준 클래스
즉, O.S 프로세스를 만들어주고 스트림으로 연결해준다.

- `ProcessBuilder(command, args)` : 실행 프로그램 + 인자로 구성
- `start` : 실제 프로세스 생성 -> Process 객체 반환해서 관리 가능 

### Redirect

- PIPE : 스트림을 직접적으로 다룸
- INHERIT : 부모 프로세의 stdin/stdout 그대로 사용
- from(File) : 파일에서 stdin 읽기
- to(File), appendTo(File) : stdout, stderr 에 파일로 쓰기

### 특징

- 프로세의 stdout, stderr 읽지 않으면 버퍼 차서 멈출수 있음
- stdin 에 쓸 때는 무조건 닫아줘야함 - EOF 전달
- PIPE 는 스트림 처리 로직이 필요

### startPipeline

ProcessBuilder 들의 파이프라인을 연결해준다.
이 메소드를 사용하지 않으면?

```kotlin
val process1 = builder1.start()

val builder2 = ProcessBuilder("wc")
    .redirectOutput(ProcessBuilder.Redirect.PIPE)

val process2 = builder2.start()

process1.inputStream.use {
    it.copyTo(process2.outputStream)
    process2.outputStream.close()
}
```

이런식으로, 기존 명령어의 결과를  - 다음 명령어의 입력으로 일일히 전달해줘야 한다.