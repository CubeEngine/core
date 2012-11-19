package de.cubeisland.cubeengine.core.command.readers;

import de.cubeisland.cubeengine.core.command.ArgumentReader;
import de.cubeisland.cubeengine.core.command.InvalidArgumentException;
import de.cubeisland.cubeengine.core.util.Pair;

public class LongReader extends ArgumentReader<Long>
{
    public LongReader()
    {
        super(Long.class);
    }

    @Override
    public Pair<Integer, Long> read(String... args) throws InvalidArgumentException
    {
        String num = args[0].replace(',', '.').replace(".", "");
        try
        {
            Long value = Long.parseLong(num);
            return new Pair<Integer, Long>(1, value);
        }
        catch (NumberFormatException e)
        {
            throw new InvalidArgumentException("Could not parse " + args[0] + " to Long!");
        }
    }
}
