package de.cubeisland.cubeengine.core.command;

import de.cubeisland.cubeengine.core.BukkitDependend;
import de.cubeisland.cubeengine.core.command.annotation.Flag;
import de.cubeisland.cubeengine.core.command.annotation.Param;
import static de.cubeisland.cubeengine.core.command.exception.IllegalParameterValue.illegalParameter;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.converter.ConversionException;
import de.cubeisland.cubeengine.core.util.converter.Convert;
import gnu.trove.map.hash.THashMap;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * This class holds all the arguments that got passed to the command
 *
 * @author Phillip Schichtel
 */
@BukkitDependend("Uses Bukkit's CommandSender")
public class CommandContext
{
    private final CommandSender sender;
    private final CubeCommand command;
    private final Stack<String> labels;
    private final Map<String, Boolean> flags;
    private final LinkedList<String> indexedParams;
    private final Map<String, Object[]> namedParams;
    private int flagCount;
    private boolean empty;
    private boolean result;
    private boolean helpCall;

    /**
     * Initializes the CommandContext object with an array of arguments
     *
     * @param baseCommand the base command
     * @param baseLabel the base label
     * @param commandLine the arguments
     * @throws IllegalArgumentException if the args array is empty
     */
    public CommandContext(CommandSender sender, CubeCommand command, Stack<String> labels)
    {
        if (sender instanceof Player)
        {
            sender = command.getModule().getUserManager().getUser(sender);
        }
        this.sender = sender;
        this.command = command;
        this.labels = labels;
        
        this.flags = new THashMap<String, Boolean>(0);
        this.flagCount = 0;
        this.indexedParams = new LinkedList<String>();
        this.namedParams = new THashMap<String, Object[]>(0);
        this.result = true;
        this.helpCall = false;
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
        Map<String, String> flagLongnameMap = new THashMap<String, String>(flags.length);
        for (Flag flag : flags)
        {
            this.flags.put(flag.name().toLowerCase(Locale.ENGLISH), false);
            if (!"".equals(flag.longName()))
            {
                flagLongnameMap.put(flag.longName().toLowerCase(Locale.ENGLISH), flag.name().toLowerCase(Locale.ENGLISH));
            }
        }
        
        if (commandLine.length == 0)
        {
            return;
        }
        
        this.helpCall = ("?".equals(commandLine[0]));
        if (this.isHelpCall())
        {
            return;
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
            String name = names[0].toLowerCase(Locale.ENGLISH);
            paramMap.put(name, param);
            for (int i = 1; i < names.length; ++i)
            {
                paramAliasMap.put(names[i].toLowerCase(Locale.ENGLISH), name);
            }
        }

        Integer offset = new Integer(0);
        for (;offset < commandLine.length; ++offset)
        {
            if (commandLine[offset].charAt(0) == '-')
            {
                String flag = commandLine[offset];
                while (!flag.isEmpty() && flag.charAt(0) == '-')
                {
                    flag = flag.substring(1);
                }

                if (flag.isEmpty())
                {
                    this.indexedParams.add(commandLine[offset]);
                }

                flag = flag.toLowerCase(Locale.ENGLISH);

                if (flagLongnameMap.containsKey(flag))
                {
                    flag = flagLongnameMap.get(flag);
                }
                if (flag != null && this.flags.containsKey(flag))
                {
                    this.flags.put(flag, true);
                    this.flagCount++;
                }
                else
                {
                    this.indexedParams.add(commandLine[offset]);
                }
            }
            else
            {
                String paramName = commandLine[offset].toLowerCase(Locale.ENGLISH);
                if (paramAliasMap.containsKey(paramName))
                {
                    paramName = paramAliasMap.get(paramName);
                }
                Param param = paramMap.get(paramName);

                if (param != null)
                {
                    Class<?>[] types = param.types();
                    Object[] values = new Object[types.length];

                    offset++;
                    int typeOffset = 0;
                    for (; typeOffset < types.length && offset < commandLine.length; typeOffset++)
                    {
                        try
                        {
                            if (String.class.isAssignableFrom(types[typeOffset]))
                            {
                                values[typeOffset] = readString(offset, commandLine);
                            }
                            else
                            {
                                values[typeOffset] = Convert.fromString(types[typeOffset], commandLine[offset]);
                            }
                        }
                        catch (ConversionException e)
                        {
                            illegalParameter(this.getSender(), "core", "Invalid Parameter for %s at index %d. %s is not a valid Type of %s",paramName,typeOffset,commandLine[offset],types[typeOffset].toString());
                        }
                        if (typeOffset < types.length)
                        {
                            offset++;
                        }
                    }

                    this.namedParams.put(paramName, values);
                }
                else
                {
                    this.indexedParams.add(readString(offset, commandLine));
                }
            }
        }
    }

    private static String readString(Integer offset, String[] commandLine)
    {
        char quote = commandLine[offset].charAt(0);
        if (quote == '"' || quote == '\'')
        {
            return readString(quote, offset, commandLine);
        }
        return commandLine[offset];
    }

    private static String readString(char quoteChar, Integer offset, String[] commandLine)
    {
        String message = commandLine[offset++].substring(1);
        if (message.charAt(message.length() - 1) == quoteChar)
        {
            return message.substring(0, message.length() - 1);
        }
            
        StringBuilder builder = new StringBuilder(message);
        
        while (offset < commandLine.length)
        {
            builder.append(' ');
            message = commandLine[offset];
            if (message.charAt(message.length() - 1) == quoteChar)
            {
                return builder.append(message.substring(0, message.length() - 1)).toString();
            }
            builder.append(message);
            ++offset;
        }
        
        return builder.toString();
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
    
    public int flagCount()
    {
        return this.flagCount;
    }

    /**
     * Returns the number of parameters
     *
     * @return the numbers of parameters
     */
    public int indexedCount()
    {
        return this.indexedParams.size();
    }
    
    public int namedCount()
    {
        return this.namedParams.size();
    }

    /**
     * Returns the label of the command
     *
     * @return the label
     */
    public String getLabel()
    {
        return this.labels.peek();
    }
    
    public Stack<String> getLabels()
    {
        return this.labels;
    }

    /**
     * Checks whether the given flag exists
     *
     * @param flag the flag name
     * @return true if it exists
     */
    public boolean hasFlag(String flag)
    {
        Boolean flagState = this.flags.get(flag);
        if (flagState == null)
        {
            throw new IllegalArgumentException("The requested flag was not declared!");
        }
        return flagState.booleanValue();
    }
    
    public Set<String> getDeclaredFlags()
    {
        return this.flags.keySet();
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
        return this.getIndexed(i, String.class, null);
    }

    public String getString(String name)
    {
        return this.getNamed(name, String.class);
    }

    public String getString(String name, int i)
    {
        return this.getNamed(name, String.class, i);
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
        return this.getIndexed(i, String.class, def);
    }
    
    public User getUser(int i)
    {
        return this.getIndexed(i, User.class, null);
    }
    
    public User getUser(String name)
    {
        return this.getUser(name, 0);
    }
    
    public User getUser(String name, int i)
    {
        return this.getNamed(name, User.class, i);
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
    
    public boolean hasIndexed(int index)
    {
        return (index >= 0 && index < this.indexedCount());
    }

    /**
     * Returns the list of Params
     *
     * @return the Params in a List of Strings
     */
    public List<String> getIndexed()
    {
        return Collections.unmodifiableList(this.indexedParams);
    }
    
    public <T> T getIndexed(int index, Class<T> type, T def)
    {
        try
        {
            return this.getIndexed(index, type);
        }
        catch (ConversionException e)
        {
            return def;
        }
    }

    public <T> T getIndexed(int index, Class<T> type) throws ConversionException
    {
        try
        {
            return Convert.fromString(type, this.indexedParams.get(index));
        }
        catch (IndexOutOfBoundsException e)
        {
            if (index < this.command.getMinimumParams() - 1 || (this.command.getMaximumParams() > -1 && index >= this.command.getMaximumParams()))
            {
                throw e;
            }
            return null;
        }
    }

    public boolean hasNamed(String name)
    {
        return this.namedParams.containsKey(name.toLowerCase(Locale.ENGLISH));
    }

    public Map<String, Object[]> getNamed()
    {
        return Collections.unmodifiableMap(this.namedParams);
    }

    public <T> T getNamed(String name, Class<T> type)
    {
        return this.getNamed(name, type, 0);
    }

    public <T> T getNamed(String name, Class<T> type, int i)
    {
        Object[] values = this.namedParams.get(name.toLowerCase(Locale.ENGLISH));
        if (values == null)
        {
            return null;
        }

        if (i >= values.length)
        {
            throw new IndexOutOfBoundsException("The named parameter you requested has only " + values.length + " values.");
        }
        if (type.isAssignableFrom(values[i].getClass()))
        {
            return type.cast(values[i]);
        }
        return null;
    }
    
    public boolean isHelpCall()
    {
        return this.helpCall;
    }
}