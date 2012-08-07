package de.cubeisland.cubeengine.core.util.bitmask;

/**
 * Represents a bitmask with 16 bits
 *
 * @author Phillip Schichtel
 */
public class ShortBitMask
{
    private short mask;

    /**
     * Creates a new ByteBitMask
     *
     */
    public ShortBitMask()
    {
        this((short)0);
    }

    /**
     * Creates a new BitMask and set its value to mask
     *
     * @param mask the value to set the BitMask at the beginning
     */
    public ShortBitMask(short mask)
    {
        this.mask = mask;
    }

    /**
     * Gets the value of this BitMask
     *
     * @return the value saved by this BitMask
     */
    public short get()
    {
        return this.mask;
    }

    /**
     * Sets the specified Bits
     *
     * @param bits The bits to set
     * @return the new value of this BitMask
     */
    public short set(short bits)
    {
        return this.mask |= bits;
    }

    /**
     * Resets this BitMask to 0
     *
     * @return 0
     */
    public short reset()
    {
        return this.reset((short)0);
    }

    /**
     * Resets this BitMask to mask
     *
     * @param mask The value to reset the BitMask to
     * @return the new value of this BitMask
     */
    public short reset(short mask)
    {
        return this.mask = mask;
    }

    /**
     * Unsets the specified Bits
     *
     * @param bits The bits to unset
     * @return the new value of this BitMask
     */
    public short unset(short bits)
    {
        return this.mask &= ~bits;
    }

    /**
     * Toggles the specified Bits
     *
     * @param bits The bits to toggle
     * @return the new value of this BitMask
     */
    public short toggle(short bits)
    {
        return this.mask ^= bits;
    }

    /**
     * Checks whether the specified Bits are set.
     *
     * @param bits The bits to check.
     * @return whether the specified Bits are set or not
     */
    public boolean isset(short bits)
    {
        return ((this.mask & bits) == bits);
    }
}