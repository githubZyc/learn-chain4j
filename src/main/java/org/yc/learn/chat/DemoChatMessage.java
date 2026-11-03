package org.yc.learn.chat;

import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import org.yc.learn.AppConfig;

/**
 * @author zhengyanchuang
 * @version 1.0
 * @description TODO
 * @date 2026/11/3 15:16
 */
public class DemoChatMessage {

    public static void main(String[] args) {
        String qwApiKey = AppConfig.apiKey();
        String modelName = AppConfig.modelName();
        System.out.println("Model Name: " + modelName);

        ChatModel model = QwenChatModel.builder()
                .apiKey(qwApiKey)
                .modelName(modelName)
                .isMultimodalModel(true)
                .build();

//        UserMessage firstUserMessage = UserMessage.from("Hello, my name is Klaus");
//
//        AiMessage firstAiMessage = qwenModel.chat(firstUserMessage).aiMessage(); // Hi Klaus, how can I help you?
//
//        UserMessage secondUserMessage = UserMessage.from("What is my name?");
//
//        AiMessage secondAiMessage = qwenModel.chat(firstUserMessage, firstAiMessage, secondUserMessage).aiMessage(); // Klaus
//        System.out.println("Second AI Message: " + secondAiMessage.text());

        UserMessage userMessage =  UserMessage.from(TextContent.from("描述一下图片的内容"),
                ImageContent.from("/Users/zhengyanchuang/fashion-studio/output/20261102_165939/images/4.png"));

        ChatResponse response = model.chat(userMessage);
        System.out.println("Response: " + response.aiMessage().text());


    }
}
