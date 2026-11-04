package org.yc.learn.services.agent;

import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.supervisor.SupervisorAgent;
import dev.langchain4j.agentic.workflow.HumanInTheLoop;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import org.yc.learn.AppConfig;

/**
 * @author zhengyanchuang
 * @version 1.0
 * @description TODO
 * @date 2026/11/4 21:33
 */
public class DemoSupervisorAgent {

    public static void main(String[] args) {
        QwenChatModel BASE_MODEL = QwenChatModel.builder()
                .modelName(AppConfig.modelName())
                .apiKey(AppConfig.apiKey())
                .isMultimodalModel(true)
                .build();

        AstrologyAgent astrologyAgent = AgenticServices
                .agentBuilder(AstrologyAgent.class)
                .chatModel(BASE_MODEL)
                .build();


        HumanInTheLoop humanInTheLoop = AgenticServices.humanInTheLoopBuilder().description("一个询问用户星座的智能体。\n" +
                        "\n")
                .outputKey("星座,运势")
                .responseProvider(() -> System.console().readLine())
                .build();

        SupervisorAgent horoscopeAgent = AgenticServices
                .supervisorBuilder()
                .chatModel(BASE_MODEL)
                .subAgents(astrologyAgent, humanInTheLoop)
                .build();

        String invoke = horoscopeAgent.invoke("为星座 John 生成运势。");
        System.out.println(invoke);

    }

}
