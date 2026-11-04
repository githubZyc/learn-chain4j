package org.yc.learn.services;

import dev.langchain4j.community.model.dashscope.QwenStreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;
import org.yc.learn.AppConfig;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * @author zhengyanchuang
 * @version 1.0
 * @description TODO
 * @date 2026/11/4 10:54
 */
public class DemoServiceWithStreaming {
    interface Assistant {

        TokenStream chat(String message);
    }

    public static void main(String[] args) {
        QwenStreamingChatModel model = QwenStreamingChatModel.builder()
                .apiKey(AppConfig.apiKey())
                .modelName(AppConfig.modelName())
                .isMultimodalModel(AppConfig.isMultimodal())
                .build();


        Assistant assistant = AiServices.create(Assistant.class, model);
        TokenStream tokenStream = assistant.chat("你好啊，很高兴见到你");

        CompletableFuture<ChatResponse> futureResponse = new CompletableFuture<>();


        tokenStream.onPartialResponse(System.out::print)
                .onCompleteResponse(futureResponse::complete)
                .onError(futureResponse::completeExceptionally)
                .start();

        try {
            futureResponse.get(30, java.util.concurrent.TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
        System.out.println("\n" + futureResponse);


    }

}
