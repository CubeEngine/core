package de.cubeisland.cubeengine.core.util.bitmask;

/**
 * Represents a bitmask with 8 bits
 *
 * @author Phillip Schichtel
 */
public class ByteBitMask
{
    private byte mask;

    /**
     * Creates a new ByteBitMask
     * 
     */
    public ByteBitMask()
    {
        this((byte)0);
    }

    /**
     * Creates a new BitMask and set its value to mask
     * 
     * @param mask the value to set the BitMask at the beginning
     */
    public ByteBitMask(byte mask)
    {
        this.mask = mask;
    }

    /**
     * Gets the value of this BitMask
     * 
     * @return the value saved by this BitMask
     */
    public byte get()
    {
        return this.mask;
    }

    /**
     * Sets the specified Bits
     * 
     * @param bits The bits to set
     * @return the new value of this BitMask
     */
    public byte set(byte bits)
    {
        return this.mask |= bits;
    }

    /**
     * Resets this BitMask to 0
     * 
     * @return 0
     */
    public byte reset()
    {
        return this.reset((byte)0);
    }

    /**
     * Resets this BitMask to mask
     * 
     * @param mask The value to reset the BitMask to
     * @return the new value of this BitMask
     */
    public byte reset(byte mask)
    {
        return this.mask = mask;
    }

    /**
     * Unsets the specified Bits
     * 
     * @param bits The bits to unset
     * @return the new value of this BitMask
     */
    public byte unset(byte bits)
    {
        return this.mask &= ~bits;
    }

    /**
     * Toggles the specified Bits
     * 
     * @param bits The bits to toggle
     * @return the new value of this BitMask
     */
    public byte toggle(byte bits)
    {
        return this.mask ^= bits;
    }

    /**
     * Checks whether the specified Bits are set.
     * 
     * @param bits The bits to check.
     * @return whether the specified Bits are set or not
     */
    public boolean isset(byte bits)
    {
        return ((this.mask & bits) == bits);
    }
}
