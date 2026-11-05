package org.yc.learn.services.agent;

import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.supervisor.SupervisorAgent;
import dev.langchain4j.agentic.workflow.HumanInTheLoop;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import org.yc.learn.AppConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * @author zhengyanchuang
 * @version 1.0
 * @description 人工介入（Human-in-the-Loop）示例：智能体缺少信息时阻塞，等待用户输入
 * @date 2026/11/4 21:33
 */
public class DemoSupervisorAgent {

    private static final BufferedReader STDIN =
            new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));

    public static void main(String[] args) {
        QwenChatModel baseModel = QwenChatModel.builder()
                .modelName(AppConfig.modelName())
                .apiKey(AppConfig.apiKey())
                .isMultimodalModel(true)
                .build();

        AstrologyAgent astrologyAgent = AgenticServices
                .agentBuilder(AstrologyAgent.class)
                .chatModel(baseModel)
                .build();

        // HumanInTheLoop 默认 async=false，即同步执行：
        // 执行到该子智能体时整个流程会被阻塞，直到 responseProvider 返回结果。
        HumanInTheLoop askSign = AgenticServices.humanInTheLoopBuilder()
                .description("当无法从用户请求中确定星座时调用：暂停流程，直接向用户询问其星座。")
                .inputKey(String.class, "request")
                .outputKey("sign")
                .responseProvider(scope -> {
                    Object request = scope.readState("request", "");
                    System.out.println();
                    System.out.println("[等待人工输入] 原始请求：" + request);

                    String sign;
                    do {
                        sign = prompt("请输入星座（例如：白羊座）> ");
                        if (sign == null) {
                            throw new IllegalStateException("标准输入已关闭，无法获取用户输入");
                        }
                        sign = sign.trim();
                        if (sign.isEmpty()) {
                            System.out.println("星座不能为空，请重新输入。");
                        }
                    } while (sign.isEmpty());

                    System.out.println("已接收星座：" + sign);
                    return sign;
                })
                .build();

        SupervisorAgent horoscopeAgent = AgenticServices
                .supervisorBuilder()
                .chatModel(baseModel)
                .subAgents(astrologyAgent, askSign)
                .build();

        String result = horoscopeAgent.invoke("为 John 生成运势，我不知道他的星座，请先向我询问。");
        System.out.println();
        System.out.println("=== 生成结果 ===");
        System.out.println(result);
    }

    /**
     * 打印提示并读取一行用户输入；返回 null 表示标准输入已关闭（EOF）。
     */
    private static String prompt(String tip) {
        System.out.print(tip);
        System.out.flush();
        try {
            return STDIN.readLine();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
