package com.github.not.n0w.lifepilot.aiEngine.task.tasks;

import com.github.not.n0w.lifepilot.aiEngine.model.UserSession;
import com.github.not.n0w.lifepilot.aiEngine.prompt.PromptLoader;
import com.github.not.n0w.lifepilot.aiEngine.task.AiTask;
import com.github.not.n0w.lifepilot.model.AiTaskType;
import com.github.not.n0w.lifepilot.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TalkTask implements AiTask {
    private final PromptLoader promptLoader;

    @Override
    public AiTaskType getType() {
        return AiTaskType.TALK;
    }

    @Override
    public UserSession execute(UserSession userSession, User user) {
        String talkPrompt = promptLoader.loadPromptText("taskPrompts/TalkPrompt.txt");
        userSession.addSystemMessage(talkPrompt);
        return userSession;
    }
}
