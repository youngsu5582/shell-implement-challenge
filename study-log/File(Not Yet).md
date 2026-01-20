## File

### Flow

애플리케이션이 파일이나, 소켓으로부터 데이터를 읽거나 쓸 때의 과정  

1. Java Method 호출 & JNI

- 개발자가 `FileInputStream.read()` 호출
-> 내부적으로 `read0` 같은 Native Method 호출, Java Native Interface 타고 C/C++ 작성된 JVM 내부 코드로 진입

2. System Call ( User → Kernel )

- System Call 발생 + Context Switching 발생 ( User → Kernel )

3. Kernel Buffer & HW I/O

- DMA : CPU 가 직접 디스크에서 읽지 않음. 

커널이 디스크 컨트롤러에게 데이터를 가져오게 시키고, CPU 는 다른 일을 하러 감  
(Blocking I/O 라면 대기)

디스크 컨트롤러는 데이터를 읽어서 Kernel Space 의 버퍼(Page Cache)에 저장

> 디스크 속도가 느리니, 한 번 읽을때 왕창 읽어두고 (Pre-fetching)
> 나중에 또 요청하면 메모리에 바로 줄 수 있게 캐싱

4. Memory Copy ( Kernel → User )

가장 비효율적인 단계

커널 버퍼에 있는 데이터를 JVM 힙 메모리로 복사

- CPU 가 이 복사 작업 수행

5. Return

데이터 복사가 끝나면 System Call 반환, 다시 User Mode 로 돌아와 자바 변수에 데이터가 담김

### Zero-Copy

파일 서버를 만들때 중요하다. (Kafka, Static File Server 등등)  
파일을 읽어서 바로 네트워크로 쏜다면, 굳이 `커널 -> JVM -> 커널` 를 거칠 필요가 없다!

- `FileChannel.transferTo` : 커널 영역에서 데이터를 복사하지 않고, 디스크 버퍼 -> NIC (네트워크 인터페이스 카드) 버퍼로 데이터를 쏘라고 지




