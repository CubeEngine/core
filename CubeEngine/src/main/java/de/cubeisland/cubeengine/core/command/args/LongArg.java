package de.cubeisland.cubeengine.core.command.args;

import de.cubeisland.cubeengine.core.command.ArgumentReader;
import de.cubeisland.cubeengine.core.command.InvalidArgumentException;
import de.cubeisland.cubeengine.core.util.Pair;

public class LongArg extends ArgumentReader<Long>
{
    public LongArg()
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
            return new Pair<Integer, Long>(0, value);
        }
        catch (NumberFormatException e)
        {
            return new Pair<Integer, Long>(0, null);
        }
    }
}
