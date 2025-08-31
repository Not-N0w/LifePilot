package com.github.not.n0w.lifepilot.config;

import com.github.not.n0w.lifepilot.aiEngine.chain.AiModuleChain;
import com.github.not.n0w.lifepilot.aiEngine.chain.modules.ParseAiModule;
import com.github.not.n0w.lifepilot.aiEngine.chain.modules.TalkAiModule;
import com.github.not.n0w.lifepilot.aiEngine.chain.modules.TaskAiModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiModuleConfig {

    @Bean
    public AiModuleChain aiModuleChain(TalkAiModule talkAiModule, TaskAiModule taskAiModule, ParseAiModule parseAiModule) {
        AiModuleChain chain = new AiModuleChain();

        chain.setPreviousAiModule(parseAiModule);
        chain.setPreviousAiModule(talkAiModule);
        chain.setPreviousAiModule(taskAiModule);

        return chain;
    }
}
