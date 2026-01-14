# Build Your Own Shell - Kotlin

[![progress-banner](https://backend.codecrafters.io/progress/shell/8b82e7be-94fe-4492-9440-7c2b6c915ee6)](https://app.codecrafters.io/users/codecrafters-bot?r=2qF)

[CodeCrafters](https://app.codecrafters.io/courses/shell/overview)의 "Build Your Own Shell" 챌린지를 Kotlin으로 구현한 프로젝트입니다.

POSIX 호환 Shell을 직접 구현하면서 Shell 명령어 파싱, REPL, Built-in 명령어, 외부 프로그램 실행, I/O Redirection 등을 학습했습니다.

## Completed Steps

### Base Stages
- [x] Repository Setup
- [x] Print a prompt
- [x] Handle invalid commands
- [x] Implement a REPL
- [x] Implement exit
- [x] Implement echo
- [x] Implement type
- [x] Locate executable files
- [x] Run a program

### Navigation Extension
- [x] The `pwd` builtin
- [x] The `cd` builtin: Absolute paths
- [x] The `cd` builtin: Relative paths
- [x] The `cd` builtin: Home directory

### Redirection Extension
- [x] Redirect stdout (`>`, `1>`)
- [x] Redirect stderr (`2>`)
- [x] Append stdout (`>>`, `1>>`)
- [x] Append stderr (`2>>`)

## Tech Stack

| Category | Technology |
|----------|------------|
| Language | Kotlin |
| JDK | 24 |
| Build Tool | Gradle 9.1.0 (Kotlin DSL) |
| Test Framework | JUnit 5 |

## Features

### Built-in Commands

| Command | Description |
|---------|-------------|
| `echo <text>` | 텍스트 출력 |
| `exit` | Shell 종료 |
| `type <command>` | 명령어 타입 확인 (builtin / executable 경로) |
| `pwd` | 현재 작업 디렉토리 출력 |
| `cd <path>` | 디렉토리 이동 (절대/상대/`~` 지원) |

### External Program Execution

- `PATH` 환경변수에서 실행 파일 검색
- 재귀적 디렉토리 탐색으로 실행 파일 위치 확인
- `ProcessBuilder`를 통한 외부 프로그램 실행

### I/O Redirection

| Operator | Description |
|----------|-------------|
| `>`, `1>` | stdout을 파일로 덮어쓰기 |
| `>>`, `1>>` | stdout을 파일에 추가 |
| `2>` | stderr를 파일로 덮어쓰기 |
| `2>>` | stderr를 파일에 추가 |

## Usage Examples

```bash
# Shell 실행
./your_program.sh

# Built-in 명령어
$ echo Hello World
Hello World

$ pwd
/home/user/project

$ cd /tmp
$ pwd
/tmp

$ cd ~
$ pwd
/home/user

$ type echo
echo is a shell builtin

$ type cat
cat is /bin/cat

# 외부 프로그램 실행
$ cat file.txt
(file contents)

# I/O Redirection
$ echo "Hello" > output.txt
$ echo "World" >> output.txt
$ cat output.txt
Hello
World

# stderr Redirection
$ cd not_exist 2> error.log
$ cat error.log
cd: not_exist: No such file or directory

# Shell 종료
$ exit
```

## What I Learned

- **REPL 패턴**: Read-Eval-Print Loop의 구조와 구현
- **명령어 파싱**: 공백, 따옴표, Redirection 연산자 처리
- **프로세스 관리**: `ProcessBuilder`를 통한 외부 프로그램 실행
- **I/O Redirection**: stdout/stderr 분리 및 파일 리다이렉션
- **PATH 탐색**: 환경변수에서 실행 파일 검색 로직
- **테스트 설계**: Stream 주입을 통한 테스트 가능한 구조

## License

This project is for educational purposes as part of the CodeCrafters challenge.
