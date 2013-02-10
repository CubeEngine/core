package de.cubeisland.cubeengine.core.command.readers;

import de.cubeisland.cubeengine.core.command.ArgumentReader;
import de.cubeisland.cubeengine.core.command.exception.InvalidArgumentException;

public class IntReader extends ArgumentReader<Integer>
{

    public IntReader()
    {
        super(Integer.class);
    }

    @Override
    public Integer read(String arg) throws InvalidArgumentException
    {
        String num = arg.replace(',', '.').replace(".", "");
        try
        {
            return Integer.parseInt(num);
        }
        catch (NumberFormatException e)
        {
            throw new InvalidArgumentException("Could not parse " + arg + "to Integer!");
        }
    }
}
