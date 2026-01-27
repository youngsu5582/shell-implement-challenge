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