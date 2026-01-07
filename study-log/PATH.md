
Unix 계열 운영체제, DOS, Windows 에서 사용되는 환경 변수
실행 가능한 프로그램이 위치한 디렉토리들의 집합을 지정

### 가벼운 역사

Multics 가 검색 경로 개념 도입

- 초기 Unix Shell 은 `/bin` 에서만 프로그램 이름 찾았다고 함
- `/bin` 디렉토리가 커지며, `/usr/bin` 과 검색경로도 운영체제 일부로 추가

### PATH 가 없다면.

- 매번 전체 경로를 입력해야 한다.

```
/usr/bin/python3 script.py
/usr/local/bin/git status
```

이 얼마나 불편할 것인가..

PATH 를 통해 명령어 이름만으로 실행이 가능하다.

```
python3 script.py
git status
```

### 검색 원리

1. 사용자가 명령어 입력하거나, 프로그램에서 `exec` 호출
2. 시스템은 PATH 디렉토리 목록을 왼쪽 -> 오른쪽 순으로 검색
3. 명령 이름과 일치하는 파일 발견하면 실행
4. 모든 디렉토리 검색해도 없으면, `command not found` 오류 발생

### 구조

```sh
echo $PATH
```

를 실행하면

`/usr/local/bin:/usr/bin:/bin` 이런식으로 나온다.

- `:` 를 기반으로 구분
- 디렉토리 이름에 `:` 사용 불가능

### 설정

- 현재 터미널 세션에만 임시 설정

```
- PATH 앞에 추가 ( 우선순위 가장 높게 설정 )
export PATH="new/directory:$PATH"

- PATH 뒤에 추가 ( 우선순위 가장 낮게 설정 )
export PATH="$PATH:/new/directory"
```

- 영구 설정

rc 에 편집

```
echo 'export PATH="$HOME/bin:$PATH"' >> ~/.bashrc
source ~/.bashrc
```

bashrc 파일에 넣고, 다시 터미널 로딩

> zshrc 등도 가능

우리가 흔히하는 JVM 세팅도

```
export JAVA_HOME="/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"
```

1. JAVA_HOME 이라는 변수에 경로를 지정하고
2. 그 경로를 PATH 에 넣는다

가 끝인 간단한 명령어다.

---

### The PATH Environment Variable

1. check if the command is a builtin command, just report `<command> is a shell builtin`
2. If the command is not a builtin, shell must go through every directory in PATH
    - Check if a file with the command name exists.
    - Check if the file has execute permissions.
   > 앞에서 file 이 명시되었으므로, 그 다음은 the!
    - If the file exists and have the permissions, print `<command> is <full_path>` and stop
    - If the file exists but don't have the permissions, skip and continue to the next directory
3. If no executable is found in any directory, print `<command>: not found`


주의할 것!

Kotlin 의 Path 는
`java.nio.file.NoSuchFileException` 를 던진다!
`kotlin.io.NoSuchFileException` 을 던지지 않는다.

