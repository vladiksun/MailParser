package com.email

import java.io.BufferedReader
import java.io.InputStreamReader
import javax.mail.*
import javax.mail.Folder.READ_ONLY
import javax.mail.search.SubjectTerm
import kotlin.system.exitProcess

class EmailParser(val emailFolder: EmailFolder) {

    private val folder: Folder

    init {
        System.getProperties()
        val props = System.getProperties()

        try {
            props.load(this::class.java.getResourceAsStream("/email.properties"))
        } catch (e: Exception) {
            e.printStackTrace()
            exitProcess(-1)
        }

        val emailServer = getEmailServerHost()
        val emailAddress = getEmailAddress()
        val emailPassword = getEmailPassword()
        val emailPort = getEmailServerPort().toInt()

        val session = Session.getInstance(props)
        session.debug = true

        val store: Store = session.getStore("imaps")
        store.connect(emailServer, emailPort, emailAddress, emailPassword)

        folder = store.getFolder(emailFolder.text)
    }

    private fun getEmailAddress(): String {
        return System.getProperty("email.username")
    }

    private fun getEmailPassword(): String {
        return System.getProperty("email.password")
    }

    private fun getEmailServerHost(): String {
        return System.getProperty("email.server.host")
    }

    private fun getEmailServerPort(): String {
        return System.getProperty("email.server.port")
    }

    @Throws(Exception::class)
    fun getMessagesBySubject(subject: String, unreadOnly: Boolean, maxToSearch: Int): List<Message> {
        val indices = getStartAndEndIndices(maxToSearch)

        var messages = folder.search(SubjectTerm(subject),
                folder.getMessages(indices["startIndex"]!!, indices["endIndex"]!!))
                .toList()

        if (unreadOnly) {
            val unreadMessages = arrayListOf<Message>()

            for (message in messages) {
                if (isMessageUnread(message)) {
                    unreadMessages.add(message)
                }
            }
            messages = unreadMessages
        }
        return messages
    }

    @Throws(Exception::class)
    fun isMessageInFolder(subject: String, unreadOnly: Boolean): Boolean {
        val messagesFound = getMessagesBySubject(subject, unreadOnly, getNumberOfMessages()).size
        return messagesFound > 0
    }

    fun openFolder() {
        if (!folder.isOpen) {
            folder.open(READ_ONLY)
        }
    }

    fun closeFolder() {
        if (folder.isOpen) {
            folder.close(false);
        }
    }

    @Throws(Exception::class)
    fun isMessageUnread(message: Message): Boolean {
        return !message.isSet(Flags.Flag.SEEN)
    }

    @Throws(Exception::class)
    fun isTextInMessage(message: Message, text: String): Boolean {
        var content = getMessageContent(message)

        content = content.replace("&nbsp;", " ")

        return content.contains(text)
    }

    /**
     * Returns HTML of the email's content
     */
    @Throws(Exception::class)
    fun getMessageContent(message: Message): String {
        val text = message.inputStream.bufferedReader().use(BufferedReader::readText)

//        val br = BufferedReader(InputStreamReader(message.inputStream, "UTF8"))
//        val text = br.readText();

        return text
    }

    @Throws(MessagingException::class)
    private fun getStartAndEndIndices(max: Int): Map<String, Int> {
        val endIndex = getNumberOfMessages()
        var startIndex = endIndex - max

        if (startIndex < 1) {
            startIndex = 1
        }

        return hashMapOf("startIndex" to startIndex, "endIndex" to endIndex)
    }

    @Throws(MessagingException::class)
    fun getNumberOfMessages(): Int {
        return folder.messageCount
    }
}