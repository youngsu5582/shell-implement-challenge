## Quote in Shell

### Single Quote (`'`)

- 모든 문자를 리터럴로 처리
- 백슬래시(`\`) 도 이스케이프 없이 그대로 유지
- 공백, 특수 문자, 따옴표 등등 모두 리터럴

> 리터럴 : 변수, 식으로 변환되지 않고 그대로 처리되는 값

- 공백 보존 : `echo 'hello    world'` -> `echo 'hello    world`
- 따옴표 연결 : `echo 'hello''world'` -> `helloworld`
- 백슬래시 리터럴 처리 : `echo 'shell\\\nscript` -> `shell\\\nscript`
- 따옴표 리터럴 처리 : `echo "shell's test"` -> `shell's test`

### Double Quote (`"`)

- 대부분 문자는 리터럴 처리
- 백슬래시가 특수 문자만 이스케이프 처리해줌 - ", \, $
- 공백도 보존

- 