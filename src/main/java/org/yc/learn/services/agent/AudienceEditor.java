package org.yc.learn.services.agent;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface AudienceEditor {

    @UserMessage("""
            你是一位专业编辑。 分析并改写以下故事，使其更契合目标受众 {{audience}}。 
            只返回故事本身，不要返回任何其他内容。 故事是："{{style}}"
            ".
        """)
    @Agent("编辑故事，使其更契合给定的受众。")
    String editStory(@V("style") String style, @V("audience") String audience);

}
