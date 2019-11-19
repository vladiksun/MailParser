import com.email.EmailFolder
import com.email.EmailParser
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class TestMail {

    @Test
    fun `The email must contain the text`() {
        val subjectToFilter = "TEST SUBJECT"

        val contentToCheck = "TEST CONTENT"
        val message = parser.getMessagesBySubject(subjectToFilter, false, 5)[0]

        Assertions.assertTrue(parser.isTextInMessage(message, contentToCheck))
    }



    companion object {

        lateinit var parser: EmailParser

        @BeforeAll
        @JvmStatic
        internal fun beforeAll() {
            try {
                parser = EmailParser(EmailFolder.INBOX)
                parser.openFolder()
            } catch (e: Exception) {
                e.printStackTrace()
                Assertions.fail<Exception>(e)
            }
        }

        @AfterAll
        @JvmStatic
        internal fun afterAll() {
            parser.closeFolder()
        }
    }
}