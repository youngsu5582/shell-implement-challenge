/**
 * Shell 명령어를 토큰으로 분리해주는 객체
 *
 * 상태 머신 개념으로 동작
 *
 * - PROCESSING : 일반 텍스트 처리
 * - IN_SINGLE_QUOTED : 작은 따옴표(') 내부, 이스케이프 처리 X
 * - IN_DOUBLE_QUOTED : 쌍 따옴표(") 내부, 백슬래시(\) 이스케이프 지원
 * - LITERAL : 다음 문자를 리터럴로 처리
 */
object ShellTokenizer {

    private enum class TokenizerStatus {
        PROCESSING,
        IN_SINGLE_QUOTED,
        IN_DOUBLE_QUOTED,
        LITERAL
    }

    fun tokenized(line: String): List<String> {
        val tokens = mutableListOf<String>()
        val tokenBuilder = StringBuilder()
        var prevStatus = TokenizerStatus.PROCESSING

        var state = TokenizerStatus.PROCESSING

        for (char in line) {
            when (state) {
                TokenizerStatus.PROCESSING -> {
                    when (char) {
                        '\'' -> state = TokenizerStatus.IN_SINGLE_QUOTED
                        '\"' -> state = TokenizerStatus.IN_DOUBLE_QUOTED
                        '\\' -> {
                            prevStatus = TokenizerStatus.PROCESSING
                            state = TokenizerStatus.LITERAL
                        }
                        ' ' -> flushToken(tokenBuilder, tokens)
                        else -> tokenBuilder.append(char)
                    }
                }

                TokenizerStatus.IN_SINGLE_QUOTED -> {
                    when (char) {
                        '\'' -> state = TokenizerStatus.PROCESSING
                        else -> tokenBuilder.append(char)
                    }
                }

                TokenizerStatus.IN_DOUBLE_QUOTED -> {
                    when (char) {
                        '\"' -> state = TokenizerStatus.PROCESSING
                        '\\' -> {
                            prevStatus = TokenizerStatus.IN_DOUBLE_QUOTED
                            state = TokenizerStatus.LITERAL
                        }

                        else -> tokenBuilder.append(char)
                    }
                }

                TokenizerStatus.LITERAL -> {
                    tokenBuilder.append(char)
                    state = prevStatus
                }
            }
        }

        flushToken(tokenBuilder, tokens)
        return tokens
    }

    private fun flushToken(tokenBuilder: StringBuilder, tokens: MutableList<String>) {
        if (tokenBuilder.isNotEmpty()) {
            tokens.add(tokenBuilder.toString())
            tokenBuilder.clear()
        }
    }
}