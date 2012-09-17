package de.cubeisland.cubeengine.core.command.exception;

/**
 *
 * @author CodeInfection
 */
public class InvalidUsageException extends Exception
{
    private final int min;
    private final int max;

    public InvalidUsageException(int min, int max)
    {
        this.min = min;
        this.max = max;
    }
    
    public int getMin()
    {
        return this.min;
    }
    
    public int getMax()
    {
        return this.max;
    }
}
