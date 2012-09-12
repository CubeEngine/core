package de.cubeisland.cubeengine.core.command;

import de.cubeisland.cubeengine.core.BukkitDependend;
import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.command.annotation.Flag;
import de.cubeisland.cubeengine.core.command.annotation.Param;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.Validate;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * This class holds all the arguments that got passed to the command
 *
 * @author Phillip Schichtel
 */
@BukkitDependend("Uses Bukkit's CommandSender")
public class CommandContext
{
    private Core core;
    private final CommandSender sender;
    private final CubeCommand command;
    private String label;
    private final Map<String, Boolean> flags;
    private Map<String, String> flagLongnameMap;
    private final LinkedList<String> indexedParams;
    private final Map<String, String> namedParamAliases;
    private final Map<String, Object> namedParams;
    private boolean empty;
    private int size;
    private boolean result;

    /**
     * Initializes the CommandContext object with an array of arguments
     *
     * @param baseCommand the base command
     * @param baseLabel the base label
     * @param commandLine the arguments
     * @throws IllegalArgumentException if the args array is empty
     */
    public CommandContext(Core core, CommandSender sender, CubeCommand command, String label)
    {
        this.core = core;
        if (sender instanceof Player)
        {
            sender = core.getUserManager().getUser(sender);
        }
        this.sender = sender;
        this.command = command;
        this.flags = new THashMap<String, Boolean>(0);
        this.flagLongnameMap = new THashMap<String, String>(0);
        this.indexedParams = new LinkedList<String>();
        this.namedParams = new THashMap<String, Object>(0);
        this.namedParamAliases = new THashMap<String, String>(0);
        this.result = true;
    }
    
    public void parseCommandArgs(String[] commandLine)
    {
        this.parseCommandArgs(commandLine, new Flag[0]);
    }
    
    public void parseCommandArgs(String[] commandLine, Flag[] flags)
    {
        this.parseCommandArgs(commandLine, flags, new Param[0]);
    }

    public void parseCommandArgs(String[] commandLine, Flag[] flags, Param[] params)
    {
        Validate.notEmpty(commandLine, "There needs to be at least 1 argument!");

        this.label = commandLine[0];
        
        this.flagLongnameMap = new THashMap<String, String>(flags.length);
        for (Flag flag : flags)
        {
            this.flags.put(flag.name(), false);
            if (!"".equals(flag.longName()))
            {
                this.flagLongnameMap.put(flag.longName(), flag.name());
            }
        }
        
        Map<String, Param> paramMap = new THashMap<String, Param>(params.length);
        Map<String, String> paramAliasMap = new THashMap<String, String>();
        String[] names;
        
        for (Param param : params)
        {
            names = param.names();
            if (names.length == 0)
            {
                throw new IllegalArgumentException("One of the declared parameters does not specify a name!");
            }
            paramMap.put(names[0], param);
            for (int i = 1; i < names.length; ++i)
            {
                paramAliasMap.put(names[i], names[0]);
            }
        }

        char firstChar;
        char quoteChar = '\0';
        StringBuilder quotedArgBuilder = null; // null => not constructing a string
        
        Param currentParam = null;
        Class[] types = null;
        int typeOffset = 0;

        for (int i = 1; i < commandLine.length; ++i)
        {
            if (commandLine[i].isEmpty())
            {
                if (quotedArgBuilder != null)
                {
                    quotedArgBuilder.append(' ');
                }
                continue;
            }
            firstChar = commandLine[i].charAt(0);
            
            if (quotedArgBuilder == null)
            {
                if (firstChar == '"' || firstChar == '\'')
                {
                    quoteChar = firstChar;

                    if (commandLine[i].charAt(commandLine[i].length() - 1) == quoteChar)
                    {
                        quotedArgBuilder = new StringBuilder(commandLine[i].substring(1));
                    }
                    else
                    {
                        this.indexedParams.add(commandLine[i].substring(1, commandLine[i].length() - 1));
                    }
                }
                else if (firstChar == '-')
                {
                    String flag = commandLine[i].substring(1);
                    if (flag.charAt(0) == '-')
                    {
                        flag = flag.substring(1);
                    }
                    if (this.flagLongnameMap.containsKey(flag))
                    {
                        flag = this.flagLongnameMap.get(flag);
                    }
                    if (flag != null)
                    {
                        this.flags.put(flag, true);
                    }
                }
                else
                {
                    String paramName = commandLine[i];
                    if (paramAliasMap.containsKey(paramName))
                    {
                        paramName = paramAliasMap.get(paramName);
                    }
                    Param param = paramMap.get(paramName);
                    if (param != null)
                    {
                        currentParam = param;
                    }
                    else
                    {
                        this.indexedParams.add(commandLine[0]);
                    }
                }
            }
            else if (quotedArgBuilder != null)
            {
                if (commandLine[i].charAt(commandLine[i].length() - 1) == quoteChar)
                {
                    quotedArgBuilder.append(' ').append(commandLine[i].substring(0, commandLine[i].length() - 1));
                    this.indexedParams.add(quotedArgBuilder.toString());
                    quotedArgBuilder = null;
                }
                else
                {
                    quotedArgBuilder.append(commandLine[i]);
                }
            }
            else if (currentParam != null)
            {
                
            }
        }
    }

    /**
     * Checks whether there are parameters
     *
     * @return true if empty
     */
    public boolean isEmpty()
    {
        return this.empty;
    }

    /**
     * Returns the number of parameters
     *
     * @return the numbers of parameters
     */
    public int size()
    {
        return this.size;
    }

    /**
     * Returns the label of the command
     *
     * @return the label
     */
    public String getLabel()
    {
        return this.label;
    }

    /**
     * Checks whether the given flag exists
     *
     * @param flag the flag name
     * @return true if it exists
     */
    public boolean hasFlag(String flag)
    {
        if (this.flagLongnameMap.containsKey(flag))
        {
            flag = this.flagLongnameMap.get(flag);
        }
        Boolean flagState = this.flags.get(flag);
        if (flagState == null)
        {
            throw new IllegalArgumentException("The requested flag was not declared!");
        }
        return flagState.booleanValue();
    }

    /**
     * Checks whether all the given flags exist
     *
     * @param flags the flags to check
     * @return true if all flags exist
     */
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

    /**
     * Returns the requested value as a String
     *
     * @param i the index of the flag
     * @return the value as String
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    public String getString(int i)
    {
        return this.indexedParams.get(i);
    }

    /**
     * Returns the requested value as a String
     *
     * @param i the index of the flag
     * @param def the default value
     * @return the value as String or the given default value
     */
    public String getString(int i, String def)
    {
        if (i >= 0 && this.size > i)
        {
            return this.indexedParams.get(i);
        }
        return def;
    }

    /**
     * Returns the requested value as an int
     *
     * @param index the index
     * @return the value as int
     */
    public int getInt(int index) throws NumberFormatException
    {
        return Integer.parseInt(this.getString(index));
    }

    /**
     * Returns the requested value as a double
     *
     * @param index the index
     * @return the value as double
     */
    public double getDouble(int index) throws NumberFormatException
    {
        return Double.parseDouble(this.getString(index));
    }

    /**
     * Returns the requested value as a long
     *
     * @param i the index
     * @return the value as long
     */
    public long getLong(int index) throws NumberFormatException
    {
        return Long.parseLong(this.getString(index));
    }

    /**
     * Returns the requested value as a boolean
     *
     * enable --> true true --> true yes --> true on --> true 1 --> true
     *
     * everything else --> false
     *
     * @param i the index
     * @return true or false
     */
    public boolean getBoolean(int index)
    {
        return this.getBoolean(index, "true", "yes", "on", "1", "enable");
    }

    /**
     * Returns the value as true, when it equals (case insensitive) any of the
     * given words.
     *
     * @param index the index
     * @param trueWords the words that indicate true
     * @return true if any of the words match
     */
    public boolean getBoolean(int index, String... trueWords)
    {
        String string = this.getString(index);
        for (String word : trueWords)
        {
            if (string.equalsIgnoreCase(word))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the requested value as a User
     *
     * @param index the index
     * @return the value as User
     */
    public User getUser(int index)
    {
        return this.core.getUserManager().getUser(this.getString(index));
    }

    /**
     * Returns the requested value as a Material
     *
     * @param index the index
     * @return the value as Material
     */
    public Material getMaterial(int index)
    {
        //TODO eigene items.csv pder ähnlich
        return Material.matchMaterial(this.getString(index));
    }

    /**
     * Returns the requested value as a ItemStack
     *
     * @param index the index
     * @return the value as ItemStack
     */
    public ItemStack getItemStack(int index)
    {
        String value = this.getString(index);
        String[] values = value.split(":");
        //TODO eigene items.csv pder ähnlich
        Material material = Material.matchMaterial(values[0]);
        short data = 0;
        if (values.length > 1)
        {
            try
            {
                data = Short.parseShort(values[1]);
            }
            catch (NumberFormatException ex)
            {
                //TODO do something?
            }
        }
        ItemStack item = new ItemStack(material, 1, data);
        return item;
    }

    /**
     * Returns a Set of the Flags
     * 
     * @return the Flags as Set of String
     */
    public Set<String> getFlags()
    {
        // TODO cache this
        Set<String> availableFlags = new THashSet<String>();
        for (Map.Entry<String, Boolean> entry : flags.entrySet())
        {
            if (entry.getValue())
            {
                availableFlags.add(entry.getKey());
            }
        }
        return availableFlags;
    }

    /**
     * Returns the list of Params
     * 
     * @return the Params in a List of Strings
     */
    public List<String> getParams()
    {
        return Collections.unmodifiableList(this.indexedParams);
    }

    /**
     * Returns the result
     * 
     * @return the result
     */
    public boolean getResult()
    {
        return this.result;
    }

    /**
     * Sets the result
     * 
     * @param result the result to set
     */
    public void setResult(boolean result)
    {
        this.result = result;
    }

    /**
     * Returns the CommandSender
     * 
     * @return the CommandSender
     */
    @BukkitDependend("This returns the CommandSender")
    public CommandSender getSender()
    {
        return this.sender;
    }

    /**
     * Returns the Command
     * 
     * @return the CubeCommand
     */
    public CubeCommand getCommand()
    {
        return this.command;
    }

    public <T> T getParam(String name, Class<T> type)
    {
        return type.cast(name);
    }
}