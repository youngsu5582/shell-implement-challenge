## Stream

데이터가 흐르는 통로.

- 바이트 단위로 흐름
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