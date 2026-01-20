## Blocking VS Non-Blocking

기본적으로 `InputStream` 은 Blocking 이다.
데이터를 다 읽을 때까지 스레드는 아무것도 못 하고 멈춰 있는다. - `WAIT`

- 동시 접속자가 1만명이면, 스레드도 1만개 필요!!
-> Context Switching 도 폭발, OOM 도 발생할 수 있다

=> NIO (New I/O) 를 사용하면 Selector 를 통해 스레드 하나로 여러 연결(채널) 을 관리할 수 있다.