package de.cubeisland.cubeengine.core.command;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.user.User;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

/**
 * This class holds all the arguments that got passed to the command
 *
 * @author Phillip Schichtel
 */
public class CommandContext
{
    private final String baseLabel;
    private final SubCommand subCommand;
    private final String label;
    private final Set<String> flags;
    private final List<String> params;
    private final boolean empty;
    private final int size;
    
    private Core core;

    /**
     * Initializes the CommandContext object with an array of arguments
     *
     * @param baseCommand the base command
     * @param baseLabel  the base label
     * @param subCommand the sub command
     * @param args the arguments
     * @throws IllegalArgumentException if the args array is empty
     */
    public CommandContext(CommandSender sender, String baseLabel, SubCommand subCommand, String[] args, Core core)
    {
        this.core = core;
        
        this.baseLabel = baseLabel;
        this.subCommand = subCommand;
        this.flags = new HashSet<String>();
        this.params = new ArrayList<String>();

        if (args.length > 0)
        {
            this.label = args[0];

            char firstChar;
            char quoteChar = '\0';
            int length;
            StringBuilder quotedArgBuilder = null;
            
            for (int i = 1; i < args.length; ++i)
            {
                firstChar = args[i].charAt(0);
                length = args[i].length();

                if (length < 1)
                {
                    if (quotedArgBuilder != null)
                    {
                        quotedArgBuilder.append(' ');
                    }
                    continue;
                }

                switch (firstChar)
                {
                    case '\'':
                    case '"':
                        if (quotedArgBuilder == null)
                        {
                            if (i + 1 >= args.length)
                            {
                                this.params.add(args[i].substring(1));
                            }
                            else
                            {
                                quoteChar = firstChar;
                                quotedArgBuilder = new StringBuilder(args[i].substring(1));
                            }
                            break;
                        }
                    case '-':
                        if (quotedArgBuilder == null && args[i].matches("^\\-[A-Za-z]+$"))
                        {
                            this.flags.add(args[i].substring(1));
                            break;
                        }
                    default:
                        if (quotedArgBuilder == null)
                        {
                            this.params.add(args[i]);
                        }
                        else
                        {
                            if (quotedArgBuilder == null)
                            {
                                this.params.add(args[i]);
                            }
                            else
                            {
                                int quoteOffset = args[i].indexOf(quoteChar);
                                if (quoteOffset >= 0)
                                {
                                    String before = args[i].substring(0, quoteOffset);
                                    String after = "";
                                    if (quoteOffset + 1 < length)
                                    {
                                        after = args[i].substring(quoteOffset + 1);
                                    }
                                    
                                    if (before.length() > 0)
                                    {
                                        quotedArgBuilder.append(' ').append(before);
                                    }
                                    this.params.add(quotedArgBuilder.toString());
                                    quotedArgBuilder = null;

                                    if (after.length() > 0)
                                    {
                                        this.params.add(after);
                                    }
                                }
                                else
                                {
                                    quotedArgBuilder.append(' ').append(args[i]);
                                    if (i + 1 >= args.length)
                                    {
                                        this.params.add(quotedArgBuilder.toString());
                                        quotedArgBuilder = null;
                                    }
                                }
                            }
                        }
                }
            }
        }
        else
        {
            throw new IllegalArgumentException("There need to be at least 1 argument!");
        }
        this.empty = this.params.isEmpty();
        this.size = this.params.size();
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
     * Returns the executed sub command object
     *
     * @return the label
     */
    public SubCommand getSubCommand()
    {
        return this.subCommand;
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
     * Returns the label of the base command
     *
     * @return the label
     */
    public String getBaseLabel()
    {
        return this.baseLabel;
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
     * enable --> true
     * true --> true
     * yes --> true
     * on --> true
     * 1 --> true
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
     * Returns the value as true, when it equals (case insensitive) any of the given words.
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
            {}            
        }
        ItemStack item = new ItemStack(material, 1, data);
        return item;
    }

    public Set<String> getFlags()
    {
        return Collections.unmodifiableSet(this.flags);
    }

    public List<String> getParams()
    {
        return Collections.unmodifiableList(this.params);
    }
}
