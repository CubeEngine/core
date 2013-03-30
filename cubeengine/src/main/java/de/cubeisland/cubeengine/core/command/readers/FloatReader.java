package de.cubeisland.cubeengine.core.command.readers;

import de.cubeisland.cubeengine.core.command.ArgumentReader;
import de.cubeisland.cubeengine.core.command.exception.InvalidArgumentException;

public class FloatReader extends ArgumentReader<Float>
{
    public FloatReader()
    {
        super(Float.class);
    }

    @Override
    public Float read(String arg) throws InvalidArgumentException
    {
        String num = arg.replaceFirst("\\D", ".").replaceAll("[^\\d\\.]]", "");
        try
        {
            return Float.parseFloat(num);
        }
        catch (NumberFormatException e)
        {
            throw new InvalidArgumentException("Could not parse " + arg + " to Float!");
        }
    }
}
