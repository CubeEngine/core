package de.cubeisland.cubeengine.core.command.args;

import de.cubeisland.cubeengine.core.command.ArgumentReader;
import de.cubeisland.cubeengine.core.command.InvalidArgumentException;
import de.cubeisland.cubeengine.core.util.Pair;

public final class StringArg extends ArgumentReader<String>
{
    public StringArg()
    {
        super(String.class);
    }

    @Override
    public Pair<Integer, String> read(String... args) throws InvalidArgumentException
    {
        if (args.length == 0)
        {
            throw new InvalidArgumentException();
        }

        if (args[0] != null && !args[0].isEmpty())
        {
            char quoteChar = args[0].charAt(0);

            if (quoteChar == '"' || quoteChar == '\'')
            {
                if (args[0].charAt(args[0].length() - 1) == quoteChar)
                {
                    return new Pair<Integer, String>(1, args[0].substring(1, args[0].length() - 1));
                }
                int i = 1;
                StringBuilder builder = new StringBuilder(args[0].substring(1));

                for (; i < args.length; ++i)
                {
                    builder.append(' ').append(args[i]);
                }
                return new Pair<Integer, String>(i, builder.toString());
            }
        }
        return new Pair<Integer, String>(1, args[0]);
    }
}
