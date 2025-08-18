package com.github.not.n0w.lifepilot.aiEngine.chain.modules;

import com.github.not.n0w.lifepilot.aiEngine.AiTextClient;
import com.github.not.n0w.lifepilot.aiEngine.chain.AiModule;
import com.github.not.n0w.lifepilot.aiEngine.model.AiResponse;
import com.github.not.n0w.lifepilot.aiEngine.model.ChainRequest;
import com.github.not.n0w.lifepilot.aiEngine.model.UserSession;
import com.github.not.n0w.lifepilot.aiEngine.model.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@Slf4j
public class TalkAiModule implements AiModule {
    private AiModule nextAiModule;
    private boolean isTerminal = true;
    private AiTextClient aiTextClient;
    @Autowired
    public void setWebClient(AiTextClient aiTextClient) {
        this.aiTextClient = aiTextClient;
    }


    @Override
    public void setNextAiModule(AiModule aiModule) {
        nextAiModule = aiModule;
    }

    @Override
    public String getName() {
        return "TalkAiModule";
    }

    @Override
    public AiResponse passThrough(ChainRequest request) {
        UserSession userSession = request.getUserSession();
        AiResponse aiResponse = aiTextClient.ask(userSession);
        if(isTerminal) {
            return aiResponse;
        }

        userSession.addUserMessage(
                new Message("assistant", aiResponse.getAnswerToUser())
        );
        request.setUserSession(userSession);
        return nextAiModule.passThrough(request);
    }

    @Override
    public boolean isTerminal() {
        return isTerminal;
    }
}
