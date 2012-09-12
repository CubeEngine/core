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
import java.util.regex.Pattern;
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
    private static final Pattern COMMAND_VALIDATE_PATTERN = Pattern.compile("^[\\w\\d\\.]+$", Pattern.CASE_INSENSITIVE);
    
    private Core core;
    private final CommandSender sender;
    private final CubeCommand command;
    private String label;
    private final Set<String> flags;
    private final Map<String, String> flagAliases;
    private final LinkedList<String> params;
    private final Map<String, String> namedParams;
    private final Map<String, String> namedParamAliases;
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
        this.flags = new THashSet<String>();
        this.flagAliases = new THashMap<String, String>();
        this.params = new LinkedList<String>();
        this.namedParams = new THashMap<String, String>();
        this.namedParamAliases = new THashMap<String, String>();
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

        char firstChar;
        char quoteChar = '\0';
        int length;
        StringBuilder quotedArgBuilder = null;

        for (int i = 1; i < commandLine.length; ++i)
        {
            if (commandLine[i].isEmpty())
            {
                continue;
            }
            firstChar = commandLine[i].charAt(0);
            
            if (quotedArgBuilder == null)
            {
                if (firstChar == '-')
                {

                }
                else if (COMMAND_VALIDATE_PATTERN.matcher(commandLine[i]).matches())
                {

                }
                else
                {

                }
            }
            else if (quotedArgBuilder == null && (firstChar == '"' || firstChar == '\''))
            {
                quoteChar = firstChar;
                quotedArgBuilder = new StringBuilder();
                
                if (commandLine[i].charAt(commandLine[i].length() - 1) == quoteChar)
                {
                    
                }
            }
            else
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
        return this.flags.contains(flag);
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
        return this.params.get(i);
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
            return this.params.get(i);
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
        return Collections.unmodifiableSet(this.flags);
    }

    /**
     * Returns the list of Params
     * 
     * @return the Params in a List of Strings
     */
    public List<String> getParams()
    {
        return Collections.unmodifiableList(this.params);
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