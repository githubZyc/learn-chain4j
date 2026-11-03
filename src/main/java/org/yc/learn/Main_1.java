package org.yc.learn;

import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.model.chat.ChatModel;

/**
 * @author zhengyanchuang
 * @version 1.0
 * @description TODO
 * @date 2026/11/3 13:53
 */
public class Main_1 {


    public static void main(String[] args) {
        String qwApiKey = AppConfig.apiKey();
        String modelName = AppConfig.modelName();
        System.out.println("Model Name: " + modelName);

        ChatModel qwenModel = QwenChatModel.builder()
                .apiKey(qwApiKey)
                .modelName(modelName)
                .isMultimodalModel(AppConfig.isMultimodal())
                .build();

        String response = qwenModel.chat("你是什么模型");
        System.out.println("Response: " + response);
    }
}
