package com.github.not.n0w.lifepilot.aiEngine.chain.modules;

import com.github.not.n0w.lifepilot.aiEngine.chain.AiModule;
import com.github.not.n0w.lifepilot.aiEngine.model.AiResponse;
import com.github.not.n0w.lifepilot.aiEngine.model.ChainRequest;
import com.github.not.n0w.lifepilot.aiEngine.model.UserSession;
import com.github.not.n0w.lifepilot.aiEngine.task.AiTask;
import com.github.not.n0w.lifepilot.aiEngine.task.TaskManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@RequiredArgsConstructor
@Component
@Slf4j
public class TaskAiModule implements AiModule  {
    private AiModule nextAiModule;
    private final TaskManager taskManager;

    @Override
    public AiResponse passThrough(ChainRequest request) {

        for(var task : new ArrayList<>(request.getUser().getTasks())) {
            AiTask aiTask = taskManager.getTask(task.getName());
            UserSession userSession = aiTask.execute(request.getUserSession(), request.getUser());
            request.setUserSession(userSession);
        }
        return nextAiModule.passThrough(request);
    }

    @Override
    public boolean isTerminal() {
        return false;
    }

    @Override
    public void setNextAiModule(AiModule aiModule) {
        nextAiModule = aiModule;
    }


    @Override
    public String getName() {
        return "TaskAiModule";
    }
}
