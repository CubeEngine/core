package de.cubeisland.cubeengine.core.command;

/**
 * This itnerface represents a command argument
 */
public interface Argument<T>
{
    int readValue(String[] args);
    T getValue();
}
