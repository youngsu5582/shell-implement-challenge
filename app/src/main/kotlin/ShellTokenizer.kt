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

        // 초기 시작은 프로세싱
        var state = TokenizerStatus.PROCESSING

        for (char in line) {
            when (state) {
                TokenizerStatus.PROCESSING -> {
                    when (char) {
                        // 따옴표시 시작
                        '\'' -> state = TokenizerStatus.IN_SINGLE_QUOTED
                        '\"' -> state = TokenizerStatus.IN_DOUBLE_QUOTED
                        '\\' -> state = TokenizerStatus.LITERAL
                        // 공백이면 POP
                        ' ' -> flushToken(tokenBuilder, tokens)
                        else -> tokenBuilder.append(char)
                    }
                }

                TokenizerStatus.IN_SINGLE_QUOTED -> {
                    when (char) {
                        // 작은 따옴표시 종료
                        '\'' -> state = TokenizerStatus.PROCESSING
                        else -> tokenBuilder.append(char)
                    }
                }

                TokenizerStatus.IN_DOUBLE_QUOTED -> {
                    when (char) {
                        // 큰 따옴표시 종료
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
                    // 이전상태도, 실행 상태로 변경
                    prevStatus = TokenizerStatus.PROCESSING
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