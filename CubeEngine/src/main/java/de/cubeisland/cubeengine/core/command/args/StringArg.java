package de.cubeisland.cubeengine.core.command.args;

import de.cubeisland.cubeengine.core.command.AbstractArgument;
import de.cubeisland.cubeengine.core.command.InvalidArgumentException;

/**
 *
 * @author Phillip Schichtel
 */
public final class StringArg extends AbstractArgument<String>
{
    public StringArg()
    {
        super(String.class);
    }
    
    @Override
    public int read(String... args) throws InvalidArgumentException
    {
        if (args.length == 0)
        {
            throw new InvalidArgumentException();
        }
        
        if (!args[0].isEmpty())
        {
            char quoteChar = args[0].charAt(0);

            if (quoteChar == '"' || quoteChar == '\'')
            {
                if (args[0].charAt(args[0].length() - 1) == quoteChar)
                {
                    this.value = args[0].substring(1, args[0].length() - 1);
                    return 1;
                }
                int i = 1;
                StringBuilder builder = new StringBuilder(args[0].substring(1));

                for (; i < args.length; ++i)
                {
                    builder.append(' ').append(args[i]);
                }

                this.value = builder.toString();
                return i;
            }
        }
        
        this.value = args[0];
        return 1;
    }
}
