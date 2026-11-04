package org.yc.learn.services.agent;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface StyleEditor {

    @UserMessage("""
            你要把故事做的比较喜剧性："{{style}}"
            ".
        """)
    @Agent("加工故事，使其更符合喜剧风格。")
    String editStory(@V("style") String style, @V("audience") String audience);

}
