package org.yc.learn.chat;

import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import org.yc.learn.AppConfig;


/**
 * @author zhengyanchuang
 * @version 1.0
 * @description TODO
 * @date 2026/11/3 15:37
 */
public class DemoChatMemory {

    interface Assistant {

        String chat(String message);
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
        System.out.println(chatMemory.messages());

        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(model)
                .chatMemory(chatMemory)
                .build();
        String answer = assistant.chat("Hello! My name is Klaus.");
        System.out.println(answer); // Hello Klaus! How can I assist you today?

        String answerWithName = assistant.chat("What is my name?");
        System.out.println(answerWithName); // Your name is Klaus.
    }
}

