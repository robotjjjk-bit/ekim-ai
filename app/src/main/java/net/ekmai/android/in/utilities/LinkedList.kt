package net.ekmai.android.`in`.utilities

import org.json.JSONArray
import org.json.JSONObject

data class Message(
    val text: String,
    val isUser: Boolean,
    val thinking: String? = null
)

class Node(
    val data: Message,
    var next: Node? = null
)

class LinkedList(
    private val limit: Int = 50 // safe for memory
) {
    private var head: Node? = null
    private var tail: Node? = null

    private var size = 0

    fun addMessage(message: Message) {
        val newNode = Node(message)
        if (head == null) {
            head = newNode
            tail = newNode
        } else {
            tail?.next = newNode
            tail = newNode
        }
        size++
        if (size > limit) deleteFirst()
    }

    fun deleteFirst(): Message? {
        val temp: Node? = head
        head = head?.next
        if (head == null) tail = null
        size--
        return temp?.data
    }

    fun deleteLast(): Message? {
        if (head == null) return null
        if(head == tail) {
            val data = head!!.data
            head = null; tail = null; size--
            return data
        }
        var current: Node? = head
        while (current?.next != tail) {
            current = current?.next
        }
        val data = tail!!.data
        tail = current; size--
        return data
    }

    fun toApiMessages(lim: Int = 20): List<Map<String, String>> {
        val temp = mutableListOf<Message>()
        var current: Node? = head
        while (current != null) {
            temp.add(current.data)
            current = current.next
        }
        return temp.takeLast(lim).map {
            mapOf(
                "role" to if (it.isUser) "user" else "assistant",
                "content" to it.text
            )
        }
    }

    fun toGeminiMessages(lim: Int = 20): JSONArray {
        val temp = mutableListOf<Message>()
        var current: Node? = head
        while (current != null) {
            temp.add(current.data)
            current = current.next
        }
        val contents: JSONArray = JSONArray()

        temp.takeLast(lim).forEach { msg ->
            val part = JSONObject().put("text", msg.text)
            val content = JSONObject().put("parts", JSONArray().put(part))
            contents.put(content)
        }
        return contents
    }

    fun toList(): List<Message> {
        val list = mutableListOf<Message>()
        var current = head
        while (current != null) {
            list.add(current.data)
            current = current.next
        }
        return list
    }

    fun getSize(): Int = size
    fun isEmpty(): Boolean = size == 0
    fun clear() {
        head = null; tail = null; size = 0
    }
}