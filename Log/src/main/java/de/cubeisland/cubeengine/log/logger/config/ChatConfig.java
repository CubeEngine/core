package de.cubeisland.cubeengine.log.logger.config;

import de.cubeisland.cubeengine.core.config.annotations.Comment;
import de.cubeisland.cubeengine.core.config.annotations.Option;
import de.cubeisland.cubeengine.log.SubLogConfig;

import java.util.ArrayList;
import java.util.List;

public class ChatConfig extends SubLogConfig
{
    public ChatConfig() {
        super(false);
    }

    @Option("log-console-command")
    public boolean logConsoleCommand = true;
    @Option("log-player-command")
    public boolean logPlayerCommand = true;
    @Option("log-player-chat")
    public boolean logPlayerChat = true;
    @Comment("Regex of commands to ignore when logging player commands.")
    @Option("ignore-commands")
    public List<String> ignoreRegex = new ArrayList<String>();//TODO add default CE pw setting

    @Override
    public String getName()
    {
        return "chat";
    }
}