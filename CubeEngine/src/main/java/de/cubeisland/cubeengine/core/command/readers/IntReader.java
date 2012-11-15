package de.cubeisland.cubeengine.core.command.readers;

import de.cubeisland.cubeengine.core.command.ArgumentReader;
import de.cubeisland.cubeengine.core.command.InvalidArgumentException;
import de.cubeisland.cubeengine.core.util.Pair;

public class IntReader extends ArgumentReader<Integer>
{
    public IntReader()
    {
        super(Integer.class);
    }

    @Override
    public Pair<Integer, Integer> read(String... args) throws InvalidArgumentException
    {
        String num = args[0].replace(',', '.').replace(".", "");
        try
        {
            Integer value = Integer.parseInt(num);
            return new Pair<Integer, Integer>(0, value);
        }
        catch (NumberFormatException e)
        {
            return new Pair<Integer, Integer>(0, null);
        }
    }
}
