package de.cubeisland.cubeengine.core.command.args;

import de.cubeisland.cubeengine.core.command.AbstractArgument;
import de.cubeisland.cubeengine.core.command.InvalidArgumentException;

public class FloatArg extends AbstractArgument<Float>
{

    public FloatArg()
    {
        super(Float.class);
    }

    @Override
    public int read(String... args) throws InvalidArgumentException
    {
        String num = args[0].replace(',', '.');
        int lastDot = num.lastIndexOf('.');
        num = num.substring(0, lastDot).replace(".", "") + num.substring(lastDot);
        
        try
        {
            this.value = Float.parseFloat(num);
            return 1;
        }
        catch (NumberFormatException e)
        {
            return 0;
        }
    }
}
