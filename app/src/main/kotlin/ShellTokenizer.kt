object ShellTokenizer {

    private enum class TokenizerStatus {
        PROCESSING,
        IN_QUOTED
    }

    fun tokenized(line: String): List<String> {
        val tokens = mutableListOf<String>()
        val tokenBuilder = StringBuilder()
        // 초기 시작은 프로세싱
        var state = TokenizerStatus.PROCESSING

        for (char in line) {
            when (state) {
                TokenizerStatus.PROCESSING -> {
                    when (char) {
                        // 따옴표시 시작
                        '\'', '\"' -> state = TokenizerStatus.IN_QUOTED
                        // 공백이면 POP
                        ' ' -> flushToken(tokenBuilder, tokens)
                        else -> tokenBuilder.append(char)
                    }
                }

                TokenizerStatus.IN_QUOTED -> {
                    when (char) {
                        // 따옴표시 종료
                        '\'', '\"' -> state = TokenizerStatus.PROCESSING
                        else -> tokenBuilder.append(char)
                    }
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