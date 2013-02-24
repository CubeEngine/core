package de.cubeisland.cubeengine.core.command;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.command.sender.CommandSender;
import de.cubeisland.cubeengine.core.user.User;

import java.util.LinkedList;
import java.util.Stack;

public class BasicContext implements CommandContext
{
    private final Core core;
    private final CubeCommand command;
    private final CommandSender sender;
    private final Stack<String> labels;
    private final LinkedList<String> args;
    private final int argCount;

    public BasicContext(CubeCommand command, CommandSender sender, Stack<String> labels, LinkedList<String> args)
    {
        this.core = command.getModule().getCore();
        this.command = command;
        this.sender = sender;
        this.labels = labels;
        this.args = args;
        this.argCount = args.size();
    }

    @Override
    public Core getCore()
    {
        return this.core;
    }

    @Override
    public CubeCommand getCommand()
    {
        return this.command;
    }

    @Override
    public boolean isSender(Class<? extends CommandSender> type)
    {
        return type.isAssignableFrom(this.sender.getClass());
    }

    @Override
    public CommandSender getSender()
    {
        return this.sender;
    }

    @Override
    public String getLabel()
    {
        return this.labels.peek();
    }

    @Override
    public Stack<String> getLabels()
    {
        Stack<String> newStack = new Stack<String>();
        newStack.addAll(this.labels);
        return newStack;
    }

    @Override
    public void sendMessage(String message)
    {
        this.sender.sendMessage(message);
    }

    @Override
    public void sendMessage(String category, String message, Object... args)
    {
        this.sender.sendMessage(category, message, args);
    }

    @Override
    public boolean hasArgs()
    {
        return this.argCount > 0;
    }

    public LinkedList<String> getArgs()
    {
        return new LinkedList<String>(this.args);
    }

    @Override
    public boolean hasArg(int i)
    {
        return i >= 0 && i < this.argCount;
    }

    @Override
    public int getArgCount()
    {
        return this.argCount;
    }

    @Override
    public <T> T getArg(int index, Class<T> type)
    {
        try
        {
            return ArgumentReader.read(type, this.args.get(index));
        }
        catch (Exception e)
        {
            return null;
        }
    }

    public <T> T getArg(int index, Class<T> type, T def)
    {
        final T value = this.getArg(index, type);
        return value == null ? def : value;
    }

    @Override
    public String getString(int i)
    {
        return this.getArg(i, String.class);
    }

    @Override
    public String getStrings(int from)
    {
        if (!this.hasArg(from))
        {
            return null;
        }
        StringBuilder sb = new StringBuilder(this.getString(from));
        while (this.hasArg(++from))
        {
            sb.append(" ").append(this.getString(from));
        }
        return sb.toString();
    }

    @Override
    public String getString(int i, String def)
    {
        return this.getArg(i, String.class, def);
    }

    @Override
    public User getUser(int i)
    {
        return this.getArg(i, User.class);
    }
}
