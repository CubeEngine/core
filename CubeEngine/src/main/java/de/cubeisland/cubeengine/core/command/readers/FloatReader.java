package de.cubeisland.cubeengine.core.command.readers;

import de.cubeisland.cubeengine.core.command.ArgumentReader;
import de.cubeisland.cubeengine.core.command.InvalidArgumentException;
import de.cubeisland.cubeengine.core.util.Pair;

public class FloatReader extends ArgumentReader<Float>
{
    public FloatReader()
    {
        super(Float.class);
    }

    @Override
    public Pair<Integer, Float> read(String... args) throws InvalidArgumentException
    {
        String num = args[0].replaceFirst("\\D", ".").replaceAll("[^\\d\\.]]", "");
        try
        {
            return new Pair<Integer, Float>(1, Float.parseFloat(num));
        }
        catch (NumberFormatException e)
        {
            throw new InvalidArgumentException("Could not parse " + args[0] + " to Float!");
        }
    }
}
