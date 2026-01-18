## Fork, Exec

한 프로세스가 다른 프로세스를 실행시키기 위해 사용

- fork : 새로운 프로세스를 위한 메모리 할당

새로운 공간으로 전부 복사, fork 수행된 다음 라인부터 실행

- exec : 새로운 프로세스를 위한 메모리를 할당하지 않고, exec 에 의해 호출된 프로세스만 메모리에 남게 된다.

exec 실행 결과로, 생성되는 새로운 프로세스는 없음
exec 호출한 프로세스의 PID 가 그대로 새로운 프로세스에 적용

=> fork 는 프로세스가 하나 더 생기는 것 (PID 가 완전히 다른 또 하나의 프로세스가 생기는 것)
=> exec 은 새로운 프로세스는 없고, 호출 프로세스의 PID 가 그대로 새로운 프로세스에 적용 (덮여 쓰여짐)

### System Call 에서의 fork, exec

System Call 은 작업영역을 3가지로 구분한다.

1. File I/O
2. Process Control
3. InterProcess Communication - IPC

fork, exec 은 Process Control 영역이다.

![process vs thread](https://i.imgur.com/BkSGMGH.png)

독립된 메모리 공간을 할당 받냐, 안받냐의 차이다.

> 물론, 조금 더 복잡한 내용들이 있다. Context Switching 라던가

하나의 프로세스는 운영체제의 가상 메모리 공간에 독립적 할당 공간에서 로딩이 된다.
스레드는 프로세스에 종속되기 때문에 할당된 메모리 공간에서 움직인다.

