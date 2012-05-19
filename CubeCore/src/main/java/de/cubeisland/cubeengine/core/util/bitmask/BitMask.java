package de.cubeisland.cubeengine.core.util.bitmask;

/**
 * Represents a bitmask with 32 bits
 *
 * @author Phillip Schichtel
 */
public class BitMask
{
    private int mask;

    public BitMask()
    {
        this(0);
    }

    public BitMask(int mask)
    {
        this.mask = mask;
    }

    public int get()
    {
        return this.mask;
    }

    public int set(int bits)
    {
        return this.mask |= bits;
    }

    public int reset()
    {
        return this.reset(0);
    }

    public int reset(int mask)
    {
        return this.mask = mask;
    }

    public int unset(int bits)
    {
        return this.mask &= ~bits;
    }

    public int toggle(int bits)
    {
        return this.mask ^= bits;
    }

    public boolean isset(int bits)
    {
        return ((this.mask & bits) == bits);
    }
}
