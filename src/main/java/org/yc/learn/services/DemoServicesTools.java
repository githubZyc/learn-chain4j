package org.yc.learn.services;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.output.FinishReason;
import dev.langchain4j.model.output.TokenUsage;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.Result;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.tool.ToolExecution;
import org.yc.learn.AppConfig;

import java.util.List;

/**
 * @author zhengyanchuang
 * @version 1.0
 * @description TODO
 * @date 2026/11/3 17:57
 */
public class DemoServicesTools {

    static class Tools{
        @Tool
        int add(int a, int b) {
            return a + b;
        }

        @Tool
        int multiply(int a, int b) {
            return a * b;
        }
    }

    interface Assistant {
        @UserMessage("2乘以3是多少")
        Result<List<String>> generateOutlineFor(String topic);
    }
    public static void main(String[] args) {

        ChatModel model = QwenChatModel.builder()
                .modelName(AppConfig.modelName())
                .apiKey(AppConfig.apiKey())
                .isMultimodalModel(true).build();

        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(model)
                .tools(new Tools())
                .build();

        Result<List<String>> result = assistant.generateOutlineFor("帮我做个计算");

        List<String> outline = result.content();
        TokenUsage tokenUsage = result.tokenUsage();
        List<Content> sources = result.sources();
        List<ToolExecution> toolExecutions = result.toolExecutions();
        FinishReason finishReason = result.finishReason();

        System.out.println("========== Result 详情 ==========");

        // 1. content：模型返回的结构化结果
        System.out.println("【content】");
        if (outline == null) {
            System.out.println("  null");
        } else {
            System.out.println("  size = " + outline.size());
            for (int i = 0; i < outline.size(); i++) {
                System.out.printf("  [%d] %s%n", i, outline.get(i));
            }
        }

        // 2. tokenUsage：token 消耗
        System.out.println("【tokenUsage】");
        if (tokenUsage == null) {
            System.out.println("  null");
        } else {
            System.out.printf("  input = %s%n", tokenUsage.inputTokenCount());
            System.out.printf("  output = %s%n", tokenUsage.outputTokenCount());
            System.out.printf("  total = %s%n", tokenUsage.totalTokenCount());
        }

        // 3. sources：RAG 检索到的内容来源
        System.out.println("【sources】");
        if (sources == null || sources.isEmpty()) {
            System.out.println("  无（未接入 RAG）");
        } else {
            System.out.println("  size = " + sources.size());
            for (int i = 0; i < sources.size(); i++) {
                Content content = sources.get(i);
                String text = content.textSegment() == null ? null : content.textSegment().text();
                System.out.printf("  [%d] text = %s%n", i, text);
                System.out.printf("      metadata = %s%n", content.metadata());
            }
        }

        // 4. toolExecutions：工具调用记录
        System.out.println("【toolExecutions】");
        if (toolExecutions == null || toolExecutions.isEmpty()) {
            System.out.println("  无（本次未调用工具）");
        } else {
            System.out.println("  size = " + toolExecutions.size());
            for (int i = 0; i < toolExecutions.size(); i++) {
                ToolExecution execution = toolExecutions.get(i);
                System.out.printf("  [%d] id      = %s%n", i, execution.request().id());
                System.out.printf("      name    = %s%n", execution.request().name());
                System.out.printf("      args    = %s%n", execution.request().arguments());
                System.out.printf("      result  = %s%n", execution.result());
                System.out.printf("      failed  = %s%n", execution.hasFailed());
                System.out.printf("      start   = %s%n", execution.startTime());
                System.out.printf("      finish  = %s%n", execution.finishTime());
                System.out.printf("      duration= %s%n", execution.duration());
            }
        }

        // 5. finishReason：结束原因
        System.out.println("【finishReason】");
        System.out.printf("  %s (%s)%n", finishReason, finishReason == null ? "null" : finishReason.name());

        // 6. 额外的原始响应信息
        System.out.println("【finalResponse】");
        var finalResponse = result.finalResponse();
        if (finalResponse == null) {
            System.out.println("  null");
        } else {
            System.out.printf("  id = %s%n", finalResponse.id());
            System.out.printf("  model = %s%n", finalResponse.modelName());
            System.out.printf("  aiMessage = %s%n", finalResponse.aiMessage() == null ? null : finalResponse.aiMessage().text());
        }

        System.out.println("【intermediateResponses】");
        var intermediateResponses = result.intermediateResponses();
        System.out.printf("  size = %s%n", intermediateResponses == null ? "null" : intermediateResponses.size());

        System.out.println("================================");
    }
}
