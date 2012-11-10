package de.cubeisland.cubeengine.core.command.args;

import de.cubeisland.cubeengine.core.command.AbstractArgument;
import de.cubeisland.cubeengine.core.command.InvalidArgumentException;

public class IntArg extends AbstractArgument<Integer>
{

    public IntArg()
    {
        super(Integer.class);
    }

    @Override
    public int read(String... args) throws InvalidArgumentException
    {
        String num = args[0].replace(',', '.').replace(".", "");
        
        try
        {
            this.value = Integer.parseInt(num);
            return 1;
        }
        catch (NumberFormatException e)
        {
            return 0;
        }
    }
}
