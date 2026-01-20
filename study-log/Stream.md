## Stream

데이터가 흐르는 통로.

- 바이트 단위로 흐름
- FIFO : 먼저 들어간 데이터가 먼저 나옴
- Sequential Access : Random Access 가 불가능, 지나간 데이터는 다시 읽으려면 다시 생성 or 버퍼링 기능을 써야 함
- 읽고, 쓰는 쪽은 서로 분리되어 책임을 가진다. (InputStream, OutputStream)
-> 둘다 가장 기초적인 초식을 제공해준다.

```
[데이터 소스] ---> (InputStream) ---> [프로그램] ---> (OutputStream) ---> [데이터 목적지]
```

> 흔히, 우리가 하는 JSON 상하차도 이 수많은 단계를 거치는 것이다.

- 파일을 읽어서 화면에 출력, 연산에 사용
- 네트워크에서 받은 데이터를 저장, 연산에 사용


### InputStream

바이트 단위로 입력을 읽는 추상 클래스

- 파일, 네트워크, 메모리 등 다양한 입력 소스에서 데이터를 읽음
- 모든 입력 스트림의 최상위 클래스

```java
public abstract class InputStream implements Closeable {
    ...
}
```

- FileInputStream : 파일에서 읽음
- ByteArrayInputStream : 메모리 배열에서 읽음
- BufferedInputStream : 버퍼를 사용해 읽기 성능 향상해서 읽음

```java
int read();
// 1바이트 읽기, 없으면 -1 반환

void close();
// 자원 해제
```

### OutputStream

바이트 단위로 출력을 쓰는 추상 클래스

- 파일, 네트워크, 메모리 등 다양한 출력 대상에 데이터를 쓴다
- 모든 출력 스트림의 최상위 클래스

```kotlin
public abstract class OutputStream implements Closeable, Flushable {
    ...
}
```

```java
void write(int b);
// 1바이트 쓰기

void write(byte[] b);
// 배열 전체 쓰기

void flush();
// 버퍼에 남은 데이터 강제 출력

void close();
// 자원 해제
```

### 왜 나눴는가

클래스 레벨에서 읽기, 쓰기를 엄격히 분리했다. - SRP
(C언어는 FILE 클래스가 읽기, 쓰기 모두 제공)

- 하드웨어적 특성 반영 : 키보드는 입력, 프린터는 출력 - 모든 장치가 읽기/쓰기 동시 가능한게 아님
- 단방향성 : 양방향으로 만들면 내부적으로 `커서 관리`, `버퍼 동기화`, `읽기/쓰기 모드 전환` 등등 복잡도가 기하급수적으로 증가!
- 최소 권한 원칙 : 데이터 읽기만 하는 메소드에 `OutputStream` 객체 넘기는 건 위험

### EOF

End Of File

더 이상 읽을 데이터가 없다는 신호
InputStream 은 read 가 -1 을 반환하는 것으로 표현

- 스트림은 "데이터가 얼마나 남았는지" 알려주지 않는 경우가 많음

> 알려줄 수 없을수도 있고.

-> EOF 를 기준으로 읽기 루프를 종료

```java
int b;
while ((b = in.read()) != -1) {
  // b 사용
}
```