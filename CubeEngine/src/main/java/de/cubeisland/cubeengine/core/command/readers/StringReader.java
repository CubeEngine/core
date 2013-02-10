package de.cubeisland.cubeengine.core.command.readers;

import de.cubeisland.cubeengine.core.command.ArgumentReader;
import de.cubeisland.cubeengine.core.command.exception.InvalidArgumentException;

public final class StringReader extends ArgumentReader<String>
{

    public StringReader()
    {
        super(String.class);
    }

    @Override
    public String read(String arg) throws InvalidArgumentException
    {
        return arg;
    }
}
