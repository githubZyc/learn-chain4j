package org.yc.learn.chat;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zhengyanchuang
 * @version 1.0
 * @description TODO
 * @date 2026/11/3 16:05
 */
public class PersistentChatMemoryStore implements ChatMemoryStore {

    private final Map<Object, List<ChatMessage>> memoryStore = new HashMap<>();


    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        // ChatMemoryStore 的约定是「没有记录时返回空集合」，不能返回 null
        return memoryStore.getOrDefault(memoryId, List.of());
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        memoryStore.put(memoryId, messages);
    }

    @Override
    public void deleteMessages(Object memoryId) {
        memoryStore.remove(memoryId);
    }
}
