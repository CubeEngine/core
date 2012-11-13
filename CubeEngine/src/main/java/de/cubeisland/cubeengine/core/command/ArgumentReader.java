package de.cubeisland.cubeengine.core.command;

import de.cubeisland.cubeengine.core.util.Pair;

public abstract class ArgumentReader<T>
{
    private final Class<T> type;

    public ArgumentReader(Class<T> type)
    {
        this.type = type;
    }

    /**
     *
     * @param args an string array of arguments
     * @return the number of arguments paired with the value that got read from the input array
     * @throws InvalidArgumentException
     */
    public abstract Pair<Integer, T> read(String... args) throws InvalidArgumentException;

    public Class<T> getType()
    {
        return this.type;
    }
}
