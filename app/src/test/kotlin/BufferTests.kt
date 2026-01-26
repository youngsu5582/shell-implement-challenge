import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import kotlin.test.Test

class BufferTests {
    @Test
    fun `d`() {
        val byteBuffer = ByteBuffer.allocate(12)
        byteBuffer.putChar('A')
        byteBuffer.putChar('B')
        byteBuffer.putChar('C')


        val outputStream = ByteArrayOutputStream(12)
        outputStream.write(byteBuffer.array())
        outputStream.close()


    }
}