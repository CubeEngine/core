package de.cubeisland.cubeengine.core.command;

import de.cubeisland.cubeengine.core.util.RequiresDefaultConstructor;

/**
 * This interface represents a command argument
 */
@RequiresDefaultConstructor
public interface Argument<T>
{
    Class<T> getType();
    
    /**
     * This method reads values from a subset of arguments of a commandline 
     * 
     * @param args an string array of arguments
     * @return the number of arguments that got read from the input array
     * @throws InvalidArgumentException when it wasn't possible to read a proper value from the given arguments
     */
    int read(String... args) throws InvalidArgumentException;

    /**
     * Returns the value read by the read(String[] args) method
     *
     * @return the value or null of none was read
     */
    T value();
}
