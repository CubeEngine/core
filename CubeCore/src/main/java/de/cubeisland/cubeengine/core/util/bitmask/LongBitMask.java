package de.cubeisland.cubeengine.core.util.bitmask;

/**
 * Represents a bitmask with 64 bits
 *
 * @author Phillip Schichtel
 */
public class LongBitMask
{
    private long mask;

    public LongBitMask()
    {
        this(0);
    }

    public LongBitMask(long mask)
    {
        this.mask = mask;
    }

    public long get()
    {
        return this.mask;
    }

    public long set(long bits)
    {
        return this.mask |= bits;
    }

    public long reset()
    {
        return this.reset(0);
    }

    public long reset(long mask)
    {
        return this.mask = mask;
    }

    public long unset(long bits)
    {
        return this.mask &= ~bits;
    }

    public long toggle(long bits)
    {
        return this.mask ^= bits;
    }

    public boolean isset(long bits)
    {
        return ((this.mask & bits) == bits);
    }
}
