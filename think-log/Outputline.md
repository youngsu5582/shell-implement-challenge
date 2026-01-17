## 현재의 문제점

StandardOutput 은 모호한 객체이다.

- 문제
  - path 라는 String 을 가지고 있는데, Path & File 을 만들어 사용도 한다.
  - 객체가, 파일도 추가로 생성을 한다.

> 완벽히 Shell 을 따르기 위해, `>` 와 같이 파일이 파이프라인으로 지정되면 파일이 자동으로 생성되게 해야했다.
> 이때, StandardOutput 이 해당 기능까지 하는게 최선의 선택이라고 생각했다.

---

즉, 처음 만들때는 String 과 Option 이 있으니 이를 기반으로 생성까지 담당을 해도 되겠지라 생각했으나,
어느 순간 SRP 가 깨져버렸다.

-> 내가 생각하는 리팩토링 방향

- StandardOutput 은 path, option 까지만 가지고 있게 할 예정
- 이 StandardOutput 을 받아서 파일 생성 로직을 담당할 예정

`cat /tmp/foo/file | wc` 이와 같은 명령어가 있다면

1. 명령어를 파싱한다.

`cat /tmp/foo/file`
`wc`

2. ProcessCommand 를 읽어서, 명령어를 수행한다.

오른쪽 wc 는 왼쪽 커맨드의 출력을 입력으로 파이프라인을 연결한다.

그후, 명령어들을 수행한다.

=> 실행 부분을 분리한다.

- 추가로, Built-in Command 를 전략패턴으로 분리한다.
지금 구조상 명령어가 늘어날 때마다 수행하는 코드가 한 메소드에 늘어난다.

=> 각 명령어에 맞게 interface 를 구현해서 수행한다.

---

구현할 때, LLM 의 도움을 받았다...

왜 어려웠는가?

일단
- 코틀린에 대한 지식 부족
- 그리고, Java 에서 Process 를 처리하는 방법에 대한 지식 부족

어떻게, Stdout -> Stdin 을 연결시키는지 막혔는데

```kotlin
val processList = ProcessBuilder.startPipeline(builders)
```

startPipeline 명령어를 입력하면, 자동으로 입력과 출력이 같이 연결이 된다.
- O.S 단에서 관리를 하고, Zero-Copy 를 사용할 수 있다.
- pipe, fork, exec 같은 시스템 콜을 사용해 커널 레벨에서 프로세스 연결.

처음 stdin 과 & 마지막 stdout 만 외부에서 제어가 가능해진다.

추가로, 이렇게 직접 구현을 하면서 프로세스 실행 로직이 Built-In Command 와 살짝 달라졌다.

-> 이는, 차차 개선할 수 있으면 개선할 예정