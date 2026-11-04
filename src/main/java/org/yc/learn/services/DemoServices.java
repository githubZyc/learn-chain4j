package org.yc.learn.services;

import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import org.yc.learn.AppConfig;

/**
 * @author zhengyanchuang
 * @version 1.0
 * @description TODO
 * @date 2026/11/3 17:57
 */
public class DemoServices {

    interface Assistant{
        String chat(String userMessage);
    }

    interface Friend{
        //在这个例子中，我们添加了 @SystemMessage 注解，其中包含我们想要使用的系统提示模板。
        //
        //这将在底层被转换为一个 SystemMessage，并与 UserMessage 一起发送给 LLM。
        //
        //@SystemMessage 还可以从资源中加载提示模板：
        //@SystemMessage(fromResource = "my-prompt-template.txt")

        @SystemMessage("你是一个小学老师")
        String chat(String userMessage);
    }

    interface FriendOfUser{
        @UserMessage("你是一个小学老师你姓{{it}}")
        String chat(String userMessage);
    }

    interface FriendOfUser2{
//        @UserMessage("你是一个小学老师你姓{{msg}}")
//        String chat(@V("msg") String userMessage);

        @UserMessage("你是一个： {{something}} 来自城市： {{country}}")
        String chat(@V("something") String something, @V("country") String country);
    }

    public static void main(String[] args) {

        ChatModel model = QwenChatModel.builder()
                .modelName(AppConfig.modelName())
                .apiKey(AppConfig.apiKey())
                .isMultimodalModel(true).build();



        FriendOfUser2 friend = AiServices.builder(FriendOfUser2.class)
                .chatModel(model)
                .build();
        String chat = friend.chat("歌手", "中国");
        System.out.println(chat);

//        FriendOfUser friend = AiServices.builder(FriendOfUser.class)
//                .chatModel(model)
//                .build();
//        String chat = friend.chat("郑");
//        System.out.println(chat);


//        Friend friend = AiServices.builder(Friend.class)
//                .chatModel(model)
//                .systemMessageProvider(chatMemoryId -> "你是一个小学老师")
//                .build();
//        String chat = friend.chat("Hello, 你是叫什么的老师？主要是负责那个阶段的教育?");
//        System.out.println(chat);

//        Friend friend = AiServices.create(Friend.class, chatModel);
//        String chat = friend.chat("Hello, 你是叫什么的老师？主要是负责那个阶段的教育?");
//        System.out.println(chat);

//        Assistant assistant = AiServices.create(Assistant.class,chatModel);
//        String chat = assistant.chat("Hello, how are you?");
//        System.out.println(chat);

    }

}
