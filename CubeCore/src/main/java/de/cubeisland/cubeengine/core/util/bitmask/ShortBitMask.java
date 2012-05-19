package de.cubeisland.cubeengine.core.util.bitmask;

/**
 * Represents a bitmask with 16 bits
 *
 * @author Phillip Schichtel
 */
public class ShortBitMask
{
    private short mask;

    public ShortBitMask()
    {
        this((short)0);
    }

    public ShortBitMask(short mask)
    {
        this.mask = mask;
    }

    public short get()
    {
        return this.mask;
    }

    public short set(short bits)
    {
        return this.mask |= bits;
    }

    public short reset()
    {
        return this.reset((short)0);
    }

    public short reset(short mask)
    {
        return this.mask = mask;
    }

    public short unset(short bits)
    {
        return this.mask &= ~bits;
    }

    public short toggle(short bits)
    {
        return this.mask ^= bits;
    }

    public boolean isset(short bits)
    {
        return ((this.mask & bits) == bits);
    }
}
