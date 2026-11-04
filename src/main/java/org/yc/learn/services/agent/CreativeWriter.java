package org.yc.learn.services.agent;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface CreativeWriter {
    @UserMessage("""
            你是一位创意作家。 
            围绕给定主题生成一篇不超过 3 句话的故事草稿。 
            只返回故事本身，不要返回任何其他内容。 主题是 {{topic}}.
            """)

    @Agent(name = "故事生成机",outputKey = "根据给定主题生成一个故事")
    String generateStory(@V("topic") String topic);

}
