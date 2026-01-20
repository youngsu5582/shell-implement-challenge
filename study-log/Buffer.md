## Buffer

데이터를 잠시 모아두는 메모리 공간

I/O 장치와 CPU/메모리 간 속도 차이를 완화하기 위해 사용된다.

### 왜 필요한가

I/O 는 System Call 을 동반한다.
(OS 의 파일을 건드리니까)

-> 매번 1바이트 씩 읽고, 호출 횟수가 너무 많으면 성능이 급격히 떨어진다!
(System Call -> Context Switch -> Disk I/O)

버퍼를 사용해서

- 작은 조각들을 모아서 한 번에 처리
- system call 횟수 감소
- 전체 I/O 성능 개선

의 이점을 가진다.

### Buffer with Stream

- InputStream + Buffer
  - 장치에서 Chunk 로 읽어 메모리에 저장
  - 프로그램은 메모리에서 빠르게 소비

- OutputStream + Buffer
  - 프로그램이 쓴 메모리 저장
  - 한 번에 디스크/네트워크로 전송

`BufferedInputStream` : `read` 호출 시 여러 바이트를 미리 읽음
`BufferedOutputStream` : `write` 호출 시 버퍼에 누적

---

일반적으로

- 파일 I/O 는 큰 버퍼 유리
- 네트워크는 지연/패킷 등을 고려해야 함

```java
InputStream in =
      new DataInputStream(
          new BufferedInputStream(
              new FileInputStream("data.bin")
          )
      );
```

안쪽 부터 실행이 된다.

```
  파일 data.bin
     │
     ▼
  FileInputStream      (파일에서 바이트 읽기)
     │
     ▼
  BufferedInputStream  (버퍼링으로 성능 개선)
     │
     ▼
  DataInputStream      (기본 타입 단위로 읽기)
     │
     ▼
   프로그램
```

파일을 지정하고 -> 파일에서 바이트를 읽고 -> 바이트를 버퍼로 읽고 -> 기본 타입 단위로 읽기

```java
OutputStream out =
    new DataOutputStream(
        new BufferedOutputStream(
            new FileOutputStream("data.bin")
        )
    );
```

```
  프로그램
     │
     ▼
  DataOutputStream     (기본 타입 단위로 쓰기)
     │
     ▼
  BufferedOutputStream (버퍼링으로 성능 개선)
     │
     ▼
  FileOutputStream     (파일에 실제 기록)
     │
     ▼
  파일 data.bin
```

기본 타입 단위로 쓰기 -> 버퍼 단위로 써서 성능 개선 -> 파일에 실제 기록

=> 모든 필터들은 다른 스트림을 감싸는 래퍼 역할

