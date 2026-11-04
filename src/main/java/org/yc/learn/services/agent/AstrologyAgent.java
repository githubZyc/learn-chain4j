package org.yc.learn.services.agent;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface AstrologyAgent {

    @SystemMessage("""
            你是一位占星师，根据用户的姓名和星座生成运势。
            
            """)
    @UserMessage("""
            为星座是 {{sign}} 的 {{name}} 生成运势。
            
            
    """)
    @Agent("一位根据用户姓名和星座生成运势的占星师。")
    String horoscope(@V("name") String name, @V("sign") String sign);

}
