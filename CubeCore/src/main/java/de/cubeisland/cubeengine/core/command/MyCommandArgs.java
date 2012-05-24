package de.cubeisland.cubeengine.core.command;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * This class holds all the arguments that got passed to the command
 *
 * @author Faithcaio
 * Kannste auch wieder löschen wenns nicht gefällt...
 */
public class MyCommandArgs
{
    private final BaseCommand baseCommand;
    private final String label;
    private final Set<String> flags;
    private final HashMap<Integer,String> params;
    private final HashMap<String,Integer> namedparams;
    private final boolean empty;
    private final int size;

    /**
     * Initializes the CommandArgs object with an array of arguments
     *
     * @param baseCommand the base command
     * @param baseLabel  the base label
     * @param subCommand the sub command
     * @param args the arguments
     * @param paramKeys the possible keys of named params
     * @throws IllegalArgumentException if the args array is empty
     */
    public MyCommandArgs(BaseCommand baseCommand, String[] args, String... paramKeys)
    {
        this.baseCommand = baseCommand;
        this.flags = new HashSet<String>();
        this.params = new HashMap<Integer,String>();
        this.namedparams = new HashMap<String,Integer>();
        HashSet<String> paramKeySet = new HashSet<String>(Arrays.asList(paramKeys));
        
        if (args.length > 0)
        {
            this.label = args[0];
            int pos = 0;
            
            for (int i = 1; i < args.length; ++i)
            {
                if (paramKeySet.contains(args[i]))
                {
                    this.namedparams.put(args[i], i-pos);
                    this.params.put(i-pos, args[++i]);
                    pos++;
                    continue;
                }
                if (args[i].charAt(0) == '-')
                    if (args[i].matches("^\\-[A-Za-z]+$"))
                    {
                        this.flags.add(args[i].substring(1));
                        pos++;
                        continue;
                    }
                this.params.put(i-pos, args[i]);
            }
        }
        else
        {
            throw new IllegalArgumentException("There need to be at least 1 argument!");
        }
        this.empty = this.params.isEmpty();
        this.size = this.params.size();
    }

    public boolean isEmpty()
    {
        return this.empty;
    }

    public int size()
    {
        return this.size;
    }

    public BaseCommand getBaseCommand()
    {
        return this.baseCommand;
    }

    public String getLabel()
    {
        return this.label;
    }

    public boolean hasFlag(String flag)
    {
        return this.flags.contains(flag);
    }

    public boolean hasFlags(String... flags)
    {
        for (String flag : flags)
        {
            if (!this.hasFlag(flag))
            {
                return false;
            }
        }
        return true;
    }

    public String getString(int i)
    {
        return this.params.get(i);
    }
    
    public String getString(String s)
    {
        return this.params.get(this.namedparams.get(s));
    }
    
    public Integer getNamedParamIndex(String s)
    {
        return this.namedparams.get(s);
    }

    public Set<String> getFlags()
    {
        return Collections.unmodifiableSet(this.flags);
    }

    public Collection<String> getParams()
    {
        return Collections.unmodifiableCollection(this.params.values());
    }
    
    public Collection<Integer> getNamedParamsIndexes()
    {
        return Collections.unmodifiableCollection(this.namedparams.values());
    }
}
