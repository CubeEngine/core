package de.cubeisland.CubeWar.Commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 *
 * @author Phillip Schichtel
 * @author Faithcaio
 */
public class CommandArgs
{
    private final String label;
    private final List<String> flags;
    private final Map<String, String> params;
    private final boolean empty;
    private final int size;

/**
 * load in CommandArguments
 */     
    public CommandArgs(String[] args)
    {
        this.flags = new ArrayList<String>();
        this.params = new HashMap<String, String>();

        if (args.length > 0)
        {
            this.label = args[0];
            String name;
            for (int i = 1; i < args.length; ++i)
            {
                if (args[i].charAt(0) == '-')
                {
                    name = args[i].substring(1);
                    if (i + 1 < args.length)
                    {
                        this.params.put(name, args[++i]);
                    }
                    else
                    {
                        this.flags.add(name);
                    }
                }
                else
                {
                    this.flags.add(args[i]);
                }
            }
        }
        else
        {
            throw new IllegalArgumentException("There need to be at least 1 argument!");
        }
        this.empty = (this.flags.isEmpty() && this.params.isEmpty());
        this.size = this.flags.size() + this.params.size();
    }

/**
 * @return true when no arguments given
 */ 
    public boolean isEmpty()
    {
        return this.empty;
    }
    
/**
 * @return amount of arguments saved
 */ 
    public int size()
    {
        return this.size;
    }

    public String getLabel()
    {
        return this.label;
    }

 /**
 * @return true if flag is set
 */ 
    public boolean hasFlag(String flag)
    {
        return this.flags.contains(flag);
    }
    
/**
 * @return true if param is set
 */ 
    public boolean hasParam(String param)
    {
        return this.params.containsKey(param);
    }
    
/**
 * @return flag as String
 */ 
    public String getString(int i)
    {
        try { return this.flags.get(i); }
        catch (IndexOutOfBoundsException ex) {return null;}
    }

/**
 * @return param as String
 */ 
    public String getString(String param)
    {
        return params.get(param);
    }
    
/**
 * @return flag as Integer
 */ 
    public Integer getInt(int flag)
    {
        return this.getInt(flag, null);
    }

/**
 * @return flag as Integer OR def
 */ 
    public Integer getInt(int flag, Integer def)
    {
        try
        {
            return Integer.parseInt(this.getString(flag));
        }
        catch (NumberFormatException e)
        {
            return def;
        }
    }
    
/**
 * @return param as Integer
 */ 
    public Integer getInt(String param)
    {
        return this.getInt(param, null);
    }
    
/**
 * @return param as Integer OR def
 */ 
    public Integer getInt(String param, Integer def)
    {
        try
        {
            return Integer.parseInt(this.getString(param));
        }
        catch (NumberFormatException e)
        {
            return def;
        }
    }

/**
 * @return flag as Integer
 */ 
    public Double getDouble(int flag)
    {
        return this.getDouble(flag, null);
    }

/**
 * @return flag i as Double OR def
 */ 
    public Double getDouble(int flag, Double def)
    {
        try
        {
            return Double.parseDouble(this.getString(flag));
        }
        catch (NumberFormatException e)
        {
            return def;
        }
    }
    
/**
 * @return param as Double
 */ 
    public Double getDouble(String param)
    {
        return this.getDouble(param, null);
    }
    
/**
 * @return Parameter param as Double OR def
 */ 
    public Double getDouble(String param, Double def)
    {
        try
        {
            return Double.parseDouble(this.getString(param));
        }
        catch (NumberFormatException e)
        {
            return def;
        }
    }
    
/**
 * @return flag as Player
 */ 
    public Player getPlayer(int flag)
    {
        return Bukkit.getPlayer(this.getString(flag));
    }
    
/**
 * @return param as Player
 */ 
    public Player getPlayer(String param)
    {
        return Bukkit.getPlayer(this.getString(param));
    }
}
