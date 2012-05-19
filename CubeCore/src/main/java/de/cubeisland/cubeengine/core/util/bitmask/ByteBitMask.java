package de.cubeisland.cubeengine.core.util.bitmask;

/**
 * Represents a bitmask with 8 bits
 *
 * @author Phillip Schichtel
 */
public class ByteBitMask
{
    private byte mask;

    public ByteBitMask()
    {
        this((byte)0);
    }

    public ByteBitMask(byte mask)
    {
        this.mask = mask;
    }

    public byte get()
    {
        return this.mask;
    }

    public byte set(byte bits)
    {
        return this.mask |= bits;
    }

    public byte reset()
    {
        return this.reset((byte)0);
    }

    public byte reset(byte mask)
    {
        return this.mask = mask;
    }

    public byte unset(byte bits)
    {
        return this.mask &= ~bits;
    }

    public byte toggle(byte bits)
    {
        return this.mask ^= bits;
    }

    public boolean isset(byte bits)
    {
        return ((this.mask & bits) == bits);
    }
}
