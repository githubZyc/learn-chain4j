package org.yc.learn.chat;

import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;
import org.yc.learn.AppConfig;

/**
 * @author zhengyanchuang
 * @version 1.0
 * @description TODO
 * @date 2026/11/3 15:53
 */
public class ServiceWithMemoryForEachUserExample {
    /**
     * See also {@link ServiceWithPersistentMemoryForEachUserExample}.
     */

    interface Assistant {

        String chat(@MemoryId int memoryId, @UserMessage String userMessage);
    }

    public static void main(String[] args) {
        String qwApiKey = AppConfig.apiKey();
        String modelName = AppConfig.modelName();
        System.out.println("Model Name: " + modelName);

        ChatModel model = QwenChatModel.builder()
                .apiKey(qwApiKey)
                .modelName(modelName)
                .isMultimodalModel(true)
                .build();

        ChatMemory chatMemory = MessageWindowChatMemory.builder().maxMessages(10).build();

        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(model)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(2))
                .build();
        System.out.println(assistant.chat(1, "Hello, my name is Klaus"));
        // Hi Klaus! How can I assist you today?

        System.out.println(assistant.chat(2, "Hello, my name is Francine"));
        // Hello Francine! How can I assist you today?

        System.out.println(assistant.chat(1, "What is my name?"));
        // Your name is Klaus.

        System.out.println(assistant.chat(2, "What is my name?"));
        // Your name is Francine.

        System.out.println(assistant.chat(1, "我的英文名字的首字母是什么？而且我告诉你我叫郑彦闯"));
        System.out.println(assistant.chat(1, "所以我的中文名字是?"));
        System.out.println(assistant.chat(1, "所以我的中文名字开头是?"));
    }
}
