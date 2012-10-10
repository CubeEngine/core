package de.cubeisland.cubeengine.core.command;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.command.annotation.Flag;
import de.cubeisland.cubeengine.core.command.annotation.Param;
import static de.cubeisland.cubeengine.core.command.exception.IllegalParameterValue.illegalParameter;
import static de.cubeisland.cubeengine.core.command.exception.InvalidUsageException.invalidSender;
import static de.cubeisland.cubeengine.core.i18n.I18n._;
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
 * This class holds all the information about a single command call.
 * 
 * TODO extract the command line parsing into its own class
 */
public class CommandContext
{
    private final Core core;
    private final CommandSender sender;
    private final CubeCommand command;
    private final Stack<String> labels;
    private final Map<String, Boolean> flags;
    private final LinkedList<String> indexedParams;
    private final Map<String, Object[]> namedParams;
    private int flagCount;
    private boolean empty;
    private boolean helpCall;

    /**
     * Initializes the CommandContext object with an array of arguments
     *
     * @param baseCommand the base command
     * @param baseLabel the base label
     * @param commandLine the arguments
     * @throws IllegalArgumentException if the args array is empty
     */
    public CommandContext(Core core, CommandSender sender, CubeCommand command, Stack<String> labels)
    {
        this.core = core;
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
            if (commandLine[offset].isEmpty())
            {
                continue; // part is empty, ignoring...
            }
            if (commandLine[offset].charAt(0) == '-') // is flag?
            {
                String flag = commandLine[offset].substring(1);
                if (flag.charAt(0) == '-')
                {
                    flag = flag.substring(1);
                }

                if (flag.isEmpty()) // is there still a name?
                {
                    this.indexedParams.add(commandLine[offset]);
                    continue;
                }

                flag = flag.toLowerCase(Locale.ENGLISH); // lowercase flag

                if (flagLongnameMap.containsKey(flag)) // has longflag?
                {
                    flag = flagLongnameMap.get(flag);
                }
                if (flag != null && this.flags.containsKey(flag)) // has flag ?
                {
                    this.flags.put(flag, true); // added flag
                    this.flagCount++;
                }
                else
                {
                    this.indexedParams.add(commandLine[offset]); // flag not found, adding it as an indexed param
                }
            }
            else //else named param or indexed param
            {   
                String paramName = commandLine[offset].toLowerCase(Locale.ENGLISH);
                // has alias named Param ?
                if (paramAliasMap.containsKey(paramName))
                {
                    paramName = paramAliasMap.get(paramName);
                }
                Param param = paramMap.get(paramName);
                // is named Param?
                if (param != null)
                {
                    Class<?>[] types = param.types();
                    Object[] values = new Object[types.length];
                    for (int typeOffset = 0; typeOffset < types.length && (offset) < commandLine.length; typeOffset++)
                    {
                        if (typeOffset < types.length)
                        {
                            // moving offset +1
                            offset++;
                        }
                        try
                        {
                            // try to apply needed type
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
                            illegalParameter(this, "core", "Invalid Parameter for %s at index %d. %s is not a valid Type of %s",paramName,typeOffset,commandLine[offset],types[typeOffset].toString());
                        }
                    }
                    //added named param
                    this.namedParams.put(paramName, values);
                }
                else // else is indexed param
                {
                    // added indexed param
                    this.indexedParams.add(readString(offset, commandLine));
                }
            }
        }
    }


    /**
     * Reads a string from the splitted command line
     * This version will determine if a quote was used.
     *
     * @param offset the current offset in the command line
     * @param commandLine the command line
     * @return the read string
     */
    private static String readString(Integer offset, String[] commandLine)
    {
        char quote = commandLine[offset].charAt(0);
        if (quote == '"' || quote == '\'')
        {
            return readString(quote, offset, commandLine);
        }
        return commandLine[offset];
    }

    /**
     * Reads a string from the splitted command line
     *
     * @param quoteChar the char used to quote this string
     * @param offset the current offset in the command line
     * @param commandLine the command line
     * @return the read string
     */
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
    
    /**
     * Returns the number of specified flags
     *
     * @return the number of specified flags
     */
    public int flagCount()
    {
        return this.flagCount;
    }

    /**
     * Returns the number of indexed parameters
     *
     * @return the number of indexed parameters
     */
    public int indexedCount()
    {
        return this.indexedParams.size();
    }
    
    /**
     * Returns the number of named parameters
     *
     * @return the number of named parameters
     */
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
    
    /**
     * Returns the Stack of labels used to call the command
     *
     * @return the Stack of labels used to call the command
     */
    public Stack<String> getLabels()
    {
        return this.labels;
    }

    /**
     * Checks whether the given flag exists
     *
     * @param flag the flag name
     * @return true if it exists
     * @throws IllegalArgumentException if the checked flag was not declared
     */
    public boolean hasFlag(String flag)
    {
        Boolean flagState = this.flags.get(flag.toLowerCase(Locale.ENGLISH));
        if (flagState == null)
        {
            throw new IllegalArgumentException("The requested flag was not declared!");
        }
        return flagState.booleanValue();
    }
    
    /**
     * Returns a set of all declared flags
     *
     * @return a set of all declared flags
     */
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

    /**
     * Returns the first value of a named parameters as a String
     *
     * @param name the parameter name
     * @return the String or null if not found
     */
    public String getString(String name)
    {
        return this.getNamed(name, String.class);
    }

    /**
     * Gets a value of a named parameter as a String
     *
     * @param name the parameter name
     * @param i the value index
     * @return the String or null if not found
     */
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
    
    /**
     * Gets a user from a indexed parameter
     * @param i the parameter index
     * @return the user or null if not found
     */
    public User getUser(int i)
    {
        return this.getIndexed(i, User.class, null);
    }
    
    /**
     * Gets the user from the first value of a named parameter
     * 
     * @param name the parameter name
     * @return the user or null of not found
     */
    public User getUser(String name)
    {
        return this.getUser(name, 0);
    }
    
    /**
     * Gets a named parameter as a User
     *
     * @param name the parameter name
     * @param i the value index
     * @return the User or null if not found
     */
    public User getUser(String name, int i)
    {
        return this.getNamed(name, User.class, i);
    }
    
    /**
     * Sends a message to the sender
     *
     * @param message the message
     */
    public void sendMessage(String message)
    {
        this.sender.sendMessage(message);
    }
    
    /**
     * Sends a localized message to the command sender
     *
     * @param category the message category
     * @param message the message
     * @param params the message parameters
     */
    public void sendMessage(String category, String message, Object... params)
    {
        this.sendMessage(_(this.sender, category, message, params));
    }

    /**
     * Returns the CommandSender
     *
     * @return the CommandSender
     */
    public CommandSender getSender()
    {
        return this.sender;
    }
    
    /**
     * Returns the CommandSender as User or null
     *
     * @return the CommandSender as User or null
     */
    public User getSenderAsUser()
    {
        return this.core.getUserManager().getUser(this.sender);
    }

    /**
     * Returns the command sender as a User or throws an IllegalUsageException with the specified message
     *
     * @param category the category of the message
     * @param message the message
     * @param params the message parameters
     * @return the User
     * @throws IllegalUsageException if the CommandSender is not a User
     */
    public User getSenderAsUser(String category, String message, Object... params)
    {
        User user = this.getSenderAsUser();
        if (user == null)
        {
            invalidSender(this.getSender(), category, message, params);
        }
        return user;
    }

    /**
     * Returns the command
     *
     * @return the CubeCommand
     */
    public CubeCommand getCommand()
    {
        return this.command;
    }
    
    /**
     * Checks whether the given index is available
     *
     * @param index the idnex to check
     * @return true if the index is available
     */
    public boolean hasIndexed(int index)
    {
        return (index >= 0 && index < this.indexedCount());
    }

    /**
     * Returns all indexed parameters
     *
     * @return the Params in a List of Strings
     */
    public List<String> getIndexed()
    {
        return Collections.unmodifiableList(this.indexedParams);
    }
    
    /**
     * Returns a indexed parameters or a default value if not found
     *
     * @param <T> the type of the value
     * @param index the index
     * @param type the Class of the value
     * @param def the default value
     * @return returns the requested value or the specified default value if the conversion failt or the parameters was not available
     */
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

    /**
     * Returns a value of a indexed parameter
     *
     * @param <T> the type of the value
     * @param index the index of the value
     * @param type the Class of the value
     * @return the value
     * @throws ConversionException if the value could not be converter to the requested type
     */
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

    /**
     * Checks whether a named parameter was given
     *
     * @param name the name of the parameter
     * @return true if the parameter was specified in the command line
     */
    public boolean hasNamed(String name)
    {
        return this.namedParams.containsKey(name.toLowerCase(Locale.ENGLISH));
    }

    /**
     * Returns all named parameters
     *
     * @return all named parameters
     */
    public Map<String, Object[]> getNamed()
    {
        return Collections.unmodifiableMap(this.namedParams);
    }
    
    /**
     * Returns all values of a named parameter
     * 
     * @param name the name of the parameter
     * @return an array of values or null if the parameter was not found
     */
    public Object[] getNamed(String name)
    {
        if (name == null)
        {
            return null;
        }
        return this.namedParams.get(name);
    }

    /**
     * Returns a value of a named parameter
     *
     * @param <T> the type of the value
     * @param name the name of the parameter
     * @param type the Class of the value
     * @return the value or null if not found
     */
    public <T> T getNamed(String name, Class<T> type)
    {
        return this.getNamed(name, type, 0);
    }

    /**
     * Returns a value of a named parameter
     *
     * @param <T> the type of the value
     * @param name the name of the parameters
     * @param type the Class of the value
     * @param i the index of the value
     * @return the value or null if not available
     */
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

    /**
     * Returns whether the help page was requested
     *
     * @return true if the help page got requested
     */
    public boolean isHelpCall()
    {
        return this.helpCall;
    }
    
    /**
     * Returns the core instance
     *
     * @return the core instance
     */
    public Core getCore()
    {
        return this.core;
    }
}