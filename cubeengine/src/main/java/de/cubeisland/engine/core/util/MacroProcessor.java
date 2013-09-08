package de.cubeisland.engine.core.util;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

public class MacroProcessor
{
    private final LinkedList<String> keys;
    private final int macroCount;

    public MacroProcessor(Collection<String> keys)
    {
        this.keys = new LinkedList<>(keys);
        this.macroCount = this.keys.size();
    }

    public MacroProcessor(String... keys)
    {
        this.keys = new LinkedList<>();
        Collections.addAll(this.keys, keys);
        this.macroCount = keys.length;
    }

    public String process(String message, String... values)
    {
        if (values.length != keys.size())
        {
            throw new IllegalArgumentException("The number of values does not match the number of macros!");
        }

        return message;
    }
}
