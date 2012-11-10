package de.cubeisland.cubeengine.core.command;

/**
 *
 * @author Phillip Schichtel
 */
public abstract class AbstractArgument<T> implements Argument<T>
{
    private final Class<T> type;
    protected T value;
    
    public AbstractArgument(Class<T> type)
    {
        this(type, null);
    }
    
    public AbstractArgument(Class<T> type, T value)
    {
        this.type = type;
        this.value = value;
    }

    @Override
    public Class<T> getType()
    {
        return this.type;
    }

    @Override
    public T value()
    {
        return this.value;
    }
}
