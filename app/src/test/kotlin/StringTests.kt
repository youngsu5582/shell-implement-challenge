import kotlin.test.Test

class StringTests {
    @Test
    fun `작음 따옴표로 split한다`() {
        val line = "'hello''world'"
        val result = line.split("'").map { it.trim() }.filter { it.isNotEmpty() }
        println(result)
    }
}