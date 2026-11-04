package org.yc.learn.services.agent;

import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.model.chat.ChatModel;
import org.yc.learn.AppConfig;

import java.util.Map;

/**
 * @author zhengyanchuang
 * @version 1.0
 * @description TODO
 * @date 2026/11/4 21:04
 */
public class DemoAgentServices {

    public static void main(String[] args) {
        ChatModel model = QwenChatModel.builder()
                .modelName(AppConfig.modelName())
                .apiKey(AppConfig.apiKey())
                .isMultimodalModel(true).build();


        CreativeWriter creativeWriter = AgenticServices
                .agentBuilder(CreativeWriter.class)
                .chatModel(model)
                .build();


        AudienceEditor audienceEditor = AgenticServices
                .agentBuilder(AudienceEditor.class)
                .chatModel(model)
                .outputKey("乌鸦,水")
                .build();


        StyleEditor styleEditor = AgenticServices
                .agentBuilder(StyleEditor.class)
                .chatModel(model)
                .outputKey("乌鸦,水")
                .build();

        UntypedAgent novelCreator = AgenticServices
                .sequenceBuilder()
                .subAgents(creativeWriter, audienceEditor, styleEditor)
                .outputKey("乌鸦,水")
                .build();


        Map<String, Object> input = Map.of(
                "topic", "乌鸦,水",
                "style", "喜剧",
                "audience", "年轻人"
        );

        String story = (String) novelCreator.invoke(input);
        System.out.println(story);



    }
}
