package com.github.not.n0w.lifepilot.aiEngine.chain.modules;

import com.github.not.n0w.lifepilot.aiEngine.chain.AiModule;
import com.github.not.n0w.lifepilot.aiEngine.model.AiResponse;
import com.github.not.n0w.lifepilot.aiEngine.model.ChainRequest;
import com.github.not.n0w.lifepilot.aiEngine.model.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class ParseAiModule implements AiModule {
    private final boolean isTerminal = true;
    private AiModule nextAiModule;

    private String parseByTag(String tag, String text) {
        String found = null;

        Pattern pattern = Pattern.compile("<" + tag + ">" + ".*</" + tag + ">");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            found = matcher.group();
        }

        if(found == null) {
            return "";
        }
        return found.substring(
                2 + tag.length(),
                found.length() - tag.length() - 3
        );
    }

    @Override
    public AiResponse passThrough(ChainRequest request) {
        var userMessages = request.getUserSession().getUserMessages();
        Message message = userMessages.get(
                userMessages.size() - 1
        );
        if(!message.getRole().equals("assistant")) {
            log.warn("Parsing NOT assistant message");
        }
        if(!isTerminal) {
            log.error("Parser must be terminal");
            return null;
        }

        return new AiResponse(
                parseByTag("answer", message.getContent()),
                parseByTag("advice", message.getContent()),
                parseByTag("analysis", message.getContent())
        );
    }

    @Override
    public boolean isTerminal() {
        return isTerminal;
    }

    @Override
    public void setNextAiModule(AiModule aiModule) {
        this.nextAiModule = aiModule;
    }

    @Override
    public String getName() {
        return "ParseAiModule";
    }
}

// <answer></answer>
// <advice></advice>
// <analysis></analysis>