package org.yc.learn.services;

import com.alibaba.dashscope.utils.JsonUtils;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.output.structured.Description;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.UserMessage;
import org.yc.learn.AppConfig;

import java.time.LocalDate;

/**
 * @author zhengyanchuang
 * @version 1.0
 * @description TODO
 * @date 2026/11/4 10:40
 */
public class DemoServicesPersonExtractor {

    static class Person {

        @Description("first name of a person") // 你可以添加一个可选的描述来帮助 LLM 更好地理解
        String firstName;
        String lastName;
        LocalDate birthDate;
        Address address;
    }

    @Description("an address") // 你可以添加一个可选的描述来帮助 LLM 更好地理解
    static class Address {
        String street;
        Integer streetNumber;
        String city;
    }

    interface PersonExtractor{
        @UserMessage("Extract information about a person from {{it}}")
        Person extractPersonFrom(String text);
    }

    public static void main(String[] args) {
        ChatModel model = QwenChatModel.builder()
                .modelName(AppConfig.modelName())
                .apiKey(AppConfig.apiKey())
                .isMultimodalModel(true).build();
        PersonExtractor personExtractor = AiServices.create(PersonExtractor.class, model);

        String text = """
             In 1968, amidst the fading echoes of Independence Day,
             a child named John arrived under the calm evening sky.
             This newborn, bearing the surname Doe, marked the start of a new journey.
             He was welcomed into the world at 345 Whispering Pines Avenue
             a quaint street nestled in the heart of Springfield
             an abode that echoed with the gentle hum of suburban dreams and aspirations.
             """;

        Person person = personExtractor.extractPersonFrom(text);

        System.out.println(person.firstName);
    }

}
