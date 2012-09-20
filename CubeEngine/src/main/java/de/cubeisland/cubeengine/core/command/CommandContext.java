package de.cubeisland.cubeengine.core.command;

import de.cubeisland.cubeengine.core.BukkitDependend;
import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.command.annotation.Flag;
import de.cubeisland.cubeengine.core.command.annotation.Param;
import de.cubeisland.cubeengine.core.command.exception.IllegalParameterValue;
import de.cubeisland.cubeengine.core.util.converter.ConversionException;
import de.cubeisland.cubeengine.core.util.converter.Convert;
import gnu.trove.map.hash.THashMap;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.lang.Validate;
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
    private Core core;
    private final CommandSender sender;
    private final CubeCommand command;
    private String label;
    private final Map<String, Boolean> flags;
    private Map<String, String> flagLongnameMap;
    private final LinkedList<String> indexedParams;
    private final Map<String, String> namedParamAliases;
    private final Map<String, Object[]> namedParams;
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
        this.namedParams = new THashMap<String, Object[]>(0);
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
            this.flags.put(flag.name().toLowerCase(Locale.ENGLISH), false);
            if (!"".equals(flag.longName()))
            {
                this.flagLongnameMap.put(flag.longName().toLowerCase(Locale.ENGLISH), flag.name().toLowerCase(Locale.ENGLISH));
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
            String name = names[0].toLowerCase(Locale.ENGLISH);
            paramMap.put(name, param);
            for (int i = 1; i < names.length; ++i)
            {
                paramAliasMap.put(names[i].toLowerCase(Locale.ENGLISH), name);
            }
        }

        // same vars to hold states from cycle to cycle
        char firstChar;
        char quoteChar = '\0';
        StringBuilder quotedArgBuilder = null; // null => not constructing a string
        String lastQuotedParam;


        for (int i = 1; i < commandLine.length; ++i)
        {
            firstChar = commandLine[i].charAt(0);

            // is string?
            if (firstChar == '"' || firstChar == '\'')
            {
                readString(firstChar, i, commandLine);
            }
            else if (firstChar == '-')
            {
                // get the name of the flag
                String flag = commandLine[i];
                // was a second dash used?
                while (!flag.isEmpty() && flag.charAt(0) == '-')
                {
                    // strip that as well
                    flag = flag.substring(1);
                }

                if (flag.isEmpty())
                {
                    this.indexedParams.add(commandLine[i]);
                }

                flag = flag.toLowerCase(Locale.ENGLISH);

                // is this a long name mapped to a short name?
                if (this.flagLongnameMap.containsKey(flag))
                {
                    // replace the name
                    flag = this.flagLongnameMap.get(flag);
                }
                // is there flag now?
                if (flag != null)
                {
                    // set the flag state to true
                    this.flags.put(flag, true);
                }
            }
            else
            {
                String paramName = commandLine[i].toLowerCase(Locale.ENGLISH);
                // is this an alias?
                if (paramAliasMap.containsKey(paramName))
                {
                    // replace the name by the mapped one
                    paramName = paramAliasMap.get(paramName);
                }
                // get the param
                Param param = paramMap.get(paramName);

                if (param != null)
                {
                    Class<?>[] types = param.types();
                    Object[] values = new Object[types.length];

                    int j = 0;
                    for (; j < types.length && i < commandLine.length; j++, i++)
                    {
                        try
                        {
                            if (String.class.isAssignableFrom(types[j]))
                            {
                                values[i] = readString(i, commandLine);
                            }
                            values[i] = Convert.fromString(types[j], commandLine[i]);
                        }
                        catch (ConversionException e)
                        {
                            throw new IllegalParameterValue(param.names()[0], j, commandLine[i], types[j]);
                        }
                    }

                    this.namedParams.put(label, values);
                }
                else
                {
                    this.indexedParams.add(commandLine[0]);
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
        return readString(quoteChar, offset, commandLine, new StringBuilder());
    }

    private static String readString(char quoteChar, Integer offset, String[] commandLine, StringBuilder builder)
    {
        return "";
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
    public String getString(int i) throws ConversionException
    {
        return this.getIndexed(i, String.class);
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
        if (i >= 0 && this.size > i)
        {
            return this.indexedParams.get(i);
        }
        return def;
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

    public Map<String, Object[]> getNamed()
    {
        return Collections.unmodifiableMap(this.namedParams);
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

    public <T> T getIndexed(int index, Class<T> type) throws ConversionException
    {
        // TODO bounds checks: IndexOutOfBoundsException of > max or < min, otherwise null
        return Convert.fromString(type, this.indexedParams.get(index));
    }

    public boolean hasNamed(String name)
    {
        return this.namedParams.containsKey(name.toLowerCase(Locale.ENGLISH));
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
}