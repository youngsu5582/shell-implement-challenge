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