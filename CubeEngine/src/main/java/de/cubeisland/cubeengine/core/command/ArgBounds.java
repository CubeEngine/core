package de.cubeisland.cubeengine.core.command;

public class ArgBounds
{
    public static final int NO_MAX = -1;
    private final int min;
    private final int max;

    public ArgBounds(int min)
    {
        this(min, min);
    }

    public ArgBounds(int min, int max)
    {
        if (max > NO_MAX && min > max)
        {
            throw new IllegalArgumentException("The arg limit must not be greater than the minimum!");
        }
        this.min = min;
        this.max = max;
    }

    public int getMin()
    {
        return this.min;
    }

    public int getMax()
    {
        return max;
    }

    public boolean inBounds(int n)
    {
        if (n < this.min)
        {
            return false;
        }
        if (this.max > NO_MAX && n > this.max)
        {
            return false;
        }
        return true;
    }
}
