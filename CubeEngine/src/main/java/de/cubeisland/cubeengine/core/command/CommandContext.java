package de.cubeisland.cubeengine.core.command;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.command.sender.CommandSender;
import de.cubeisland.cubeengine.core.user.User;

import java.util.LinkedList;
import java.util.Stack;

public interface CommandContext
{
    Core getCore();
    CubeCommand getCommand();
    boolean isSender(Class<? extends CommandSender> type);
    CommandSender getSender(); // type inference is our friend!
    String getLabel();
    Stack<String> getLabels();
    void sendMessage(String message);
    void sendMessage(String category, String message, Object... args);

    int getArgCount();
    boolean hasArgs();
    LinkedList<String> getArgs();
    boolean hasArg(int i);
    <T> T getArg(int i, Class<T> type);
    <T> T getArg(int i, Class<T> type, T def);
    String getString(int i);
    String getStrings(int from);
    String getString(int i, String def);
    User getUser(int i);
}
