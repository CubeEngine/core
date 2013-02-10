package de.cubeisland.cubeengine.core.command.parameterized;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CommandParameter
{
    private static final ConcurrentMap<Class, ParamCompleter> completerMap = new ConcurrentHashMap<Class, ParamCompleter>();

    private final String name;
    private final String[] aliases;

    private final Class type;
    private final boolean required;

    public CommandParameter(String name, String[] aliases, Class type, boolean required)
    {
        this.name = name;
        this.aliases = aliases;
        this.type = type;
        this.required = required;
    }

    public static void registerCompleter(ParamCompleter completer)
    {
        for (Class type : completer.getCompatibleClasses())
        {
            completerMap.put(type, completer);
        }
    }

    // TODO unregister classes of modules
    public static void unregisterCompleterClass(Class type)
    {
        Iterator<Map.Entry<Class, ParamCompleter>> iter = completerMap.entrySet().iterator();
        Map.Entry<Class, ParamCompleter> entry;

        while (iter.hasNext())
        {
            entry = iter.next();
            if (entry.getKey() == type || entry.getValue().getClass() == type)
            {
                iter.remove();
            }
        }
    }

    public static void unregisterCompleter(ParamCompleter completer)
    {
        for (Class type : completer.getCompatibleClasses())
        {
            completerMap.remove(type);
        }
    }

    public static ParamCompleter getCompleter(Class type)
    {
        return completerMap.get(type);
    }

    public static ParamCompleter getCompleter(CommandParameter param)
    {
        return completerMap.get(param.getType());
    }

    public ParamCompleter getCompleter()
    {
        return getCompleter(this);
    }

    public String getName()
    {
        return name;
    }

    public String[] getAliases()
    {
        return aliases;
    }

    public Class getType()
    {
        return type;
    }

    public boolean isRequired()
    {
        return required;
    }
}