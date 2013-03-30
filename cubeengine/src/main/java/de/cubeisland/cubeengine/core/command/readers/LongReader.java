package de.cubeisland.cubeengine.core.command.readers;

import de.cubeisland.cubeengine.core.command.ArgumentReader;
import de.cubeisland.cubeengine.core.command.exception.InvalidArgumentException;

public class LongReader extends ArgumentReader<Long>
{
    public LongReader()
    {
        super(Long.class);
    }

    @Override
    public Long read(String arg) throws InvalidArgumentException
    {
        String num = arg.replace(',', '.').replace(".", "");
        try
        {
            return Long.parseLong(num);
        }
        catch (NumberFormatException e)
        {
            throw new InvalidArgumentException("Could not parse " + arg + " to Long!");
        }
    }
}
