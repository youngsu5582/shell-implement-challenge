## flush, close

Stream 은 OS 자원(파일 디스크립터/ 소켓) 과 버퍼를 함께 사용함

즉, `버퍼에 남은 데이터를 실제로 내보내는 행위` 와 `스트림을 종료하고 OS 자원을 반환하는 행위` 를 해야한다.

### flush

메모리 버퍼에 쌓인 데이터를 즉시 목적지에 전달

- 버퍼에 남아있는 데이터는 프로그램 종료/예외 시 사라질 수 있음
- 네트워크, 파일에 '당장!' 반영해야 하는 상황이 존재할 수 있음

OutputStream 에서 제공

### close

스트림을 완전히 종료하고, OS 자원 반환

#### 왜 필요한가.

- 파일 핸들 누수

파일 열 때마다, OS 는 File Descriptor 를 잡아둔다.    
close 를 하지 않으면, 반환되지 않는다.  

-> `열 수 있는 파일 개수` 의 한계는 정해져있다. - 모든 파일/소켓 open 이 실패할 수 있음  

- 다른 프로세스가 파일에 접근 못할 가능성 존재

OS/파일시스템 마다 다르지만, 일부 환경은  
열린 파일에 대해 잠금을 걸거나, 쓰기 접근을 막을 수 있다.  

- 데이터 유실 가능

OutputStream 은 성능 때문에 내부 버퍼에 데이터를 모아둔다.  

close/flush 를 안하면 메모리 버퍼에 남아있는 데이터가 전송되지 않고 소멸될 수 있음.  

> 그래서, close 를 할 때도 flush 를 해준다. 관례적으로 

- 누적되어 장애 유발

웹 서버, 데몬같은 오래 도는 프로그램은 핸들이 계속 누수되어서 점진적으로 자원이 고갈

-> 지속적 장애로 진행, 파일 저장 실패 & 로그 작성 실패 & 네트워크 연결 실패

### try-with-resources

try 블록이 끝날 때 자동으로 `close` 호출해준다.

- 예외가 발생해도, 자원 해제 누락을 막기 위한 패턴

```java
try (InputStream in = new FileInputStream("a.txt");
       OutputStream out = new FileOutputStream("b.txt")) {

      // read/write
}
```

코드가 끝나면 자동으로 close

- 예외 처리 경로에서 누락되지 않고 보장
- close 도중 예외는 `suppressed exception` 으로 기록

`AutoCloseable`, `Closeable` 구현 클래스만 가능