## System Call

운영체제는 시스템 안정성을 위해 메모리 공간을 두 가지로 분리한다.

- User Mode(Ring 3) : 작성한 애플리케이션 코드가 실행되는 곳, HW 에 직접 접근할 권한이 없고 제한된 명령 및 연산만 수행
- Kernel Mode(Ring 0) : OS 커널이 실행되는 곳, 모든 HW 제어 및 메모리 접근 권한을 가짐

> Ring 은 하드웨어 차원에서 정의한 `계층적 보호 도메인` 레이어를 의미한다.

그렇기에, Kernel Mode 를 사용하기 위해선 요청을 해야만 한다.
-> System Call

User Mode 프로세스가 `파일 읽기`, `네트워크 패킷 수신/전송`, `프로세스 생성` 등  
커널의 권한이 필요한 작업 요청할 때 사용하는 인터페이스(소프트웨어 인터럽트)

### User -> Kernel Mode 전환

Context Switching 이 발생한다.

- CPU 의 실행 모드 변경

1. 준비

애플리케이션이 표준 라이브러리 함수 (`read` 등) 호출하면, 래퍼 함수가 두가지를 수행한다.

- System Call Number 로드 : 요청 기능의 고유 번호 CPU 레지스터(주로 RAX라고 한다..?)에 저장
- 인자 저장 : 함수 매개변수들 CPU 레지스터에 저장

2. 전환

`SYSCALL`, `SYSENTER` 같은 명령어를 사용한다.
실행 순간 CPU 하드웨어 레벨에서 아래 일들이 벌어진다.

- Privilege Level 변경 : CPU 권한 비트가 Ring 3 -> 0 으로 변환
- User Stack -> Kernel Stack : CPU 가 스택 포인터를 Kernel Stack 주소로 변경

> Kernel Stack? : 커널 주소 공간에 위치, 각 스레드마다 하나씩 할당
> Java 에서 스레드 1개는 OS 레벨의 `task_struct` 생성 - 8KB ~ 16KB Kernel Stack 짝지어 생성

> 스레드 생성 비용이 비싼 이유다. - `new Thread().start()`
> 단순 JVM 객체가 아니라, OS 커널에 요청해 `Kernel Stack` 용 물리 메모리 할당 + 페이지 테이블 세팅
> (Virtual Thread 는 커널 스택을 1:1 로 만들지 않는다)

- Context 저장 : 돌아올 주소, 현재 상태 플래그 등 커널 스택 or 특정 레지스터에 저장시켜놓는다.

3. 실행

이제, 제어권은 커널에게 있다.

- System Call Handelr : 커널이 RAX 레지스터에 있는 번호를 보고 System Call Table 참조해 해당 커널 함수로 jump
- 작업 수행 : 권한 검사 수행한 뒤, 요청된 하드웨어 작업 처리

4. 복귀

커널은

1. 결과값(or 에러코드) 를 RAX 에 담는다.
2. `SYSRET` 명령어 실행. - Ring 3로 낮추고, Context 를 복구

를 통해 멈춘 곳 다음부터 실행이 가능해진다.

### System Call Cost

결국 System Call 은 느리다.

- CPU Pipeline Flush & Cache Pollution

모드 전환 일어나면, CPU 파이프라인이 깨질 수 있다.
