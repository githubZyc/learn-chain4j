package org.yc.learn.services;

import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.memory.ChatMemoryAccess;
import org.yc.learn.AppConfig;

import java.util.List;

/**
 * @author zhengyanchuang
 * @version 1.0
 * @description TODO
 * @date 2026/11/4 19:42
 */
public class DemoMessageWindowChatMemory {

    interface Assistant {
        String chat(@MemoryId int memoryId, @UserMessage String message);
    }


    interface AssistantChatMemoryAccess extends ChatMemoryAccess {
        String chat(@MemoryId int memoryId, @UserMessage String message);
    }

    public static void main(String[] args) {
        QwenChatModel model = QwenChatModel.builder().modelName(AppConfig.modelName()).apiKey(AppConfig.apiKey())
                .isMultimodalModel(true).build();


//        Assistant assistant = AiServices.builder(Assistant.class)
//                .chatModel(model)
//                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(10))
//                .build();
//
//
//        String answerToKlaus = assistant.chat(1, "Hello, 我的名字是张三");
//        System.out.println(answerToKlaus);
//        String answerToFrancine = assistant.chat(2, "Hello, 我的名字是李四");
//        System.out.println(answerToFrancine);


                AssistantChatMemoryAccess assistant = AiServices.builder(AssistantChatMemoryAccess.class)
                .chatModel(model)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(10))
                .build();
        List<ChatMessage> messagesWithKlaus = assistant.getChatMemory(1).messages();
        System.out.println(messagesWithKlaus);
        boolean chatMemoryWithFrancineEvicted = assistant.evictChatMemory(2);
        System.out.println(chatMemoryWithFrancineEvicted);
    }
}
