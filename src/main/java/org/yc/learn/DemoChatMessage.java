package org.yc.learn;

import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;

/**
 * @author zhengyanchuang
 * @version 1.0
 * @description TODO
 * @date 2026/11/3 15:16
 */
public class DemoChatMessage {

    public static void main(String[] args) {
        String qwApiKey = System.getenv("api_key");
        String modelName = System.getenv("model_name");
        System.out.println("Model Name: " + modelName);

        ChatModel qwenModel = QwenChatModel.builder()
                .apiKey(qwApiKey)
                .modelName(modelName)
                .isMultimodalModel(true)
                .build();

        UserMessage firstUserMessage = UserMessage.from("Hello, my name is Klaus");

        AiMessage firstAiMessage = qwenModel.chat(firstUserMessage).aiMessage(); // Hi Klaus, how can I help you?

        UserMessage secondUserMessage = UserMessage.from("What is my name?");

        AiMessage secondAiMessage = qwenModel.chat(firstUserMessage, firstAiMessage, secondUserMessage).aiMessage(); // Klaus
        System.out.println("Second AI Message: " + secondAiMessage.text());
    }
}
