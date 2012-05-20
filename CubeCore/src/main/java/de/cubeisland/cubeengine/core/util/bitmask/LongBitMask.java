package de.cubeisland.cubeengine.core.util.bitmask;

/**
 * Represents a bitmask with 64 bits
 *
 * @author Phillip Schichtel
 */
public class LongBitMask
{
    private long mask;

    /**
     * Creates a new BitMask
     * 
     */
    public LongBitMask()
    {
        this(0);
    }

    /**
     * Creates a new BitMask and set its value to mask
     * 
     * @param mask the value to set the BitMask at the beginning
     */
    public LongBitMask(long mask)
    {
        this.mask = mask;
    }

    /**
     * Gets the value of this BitMask
     * 
     * @return the value saved by this BitMask
     */
    public long get()
    {
        return this.mask;
    }

    /**
     * Sets the specified Bits
     * 
     * @param bits The bits to set
     * @return the new value of this BitMask
     */
    public long set(long bits)
    {
        return this.mask |= bits;
    }

    /**
     * Resets this BitMask to 0
     * 
     * @return 0
     */
    public long reset()
    {
        return this.reset(0);
    }

    /**
     * Resets this BitMask to mask
     * 
     * @param mask The value to reset the BitMask to
     * @return the new value of this BitMask
     */
    public long reset(long mask)
    {
        return this.mask = mask;
    }

    /**
     * Unsets the specified Bits
     * 
     * @param bits The bits to unset
     * @return the new value of this BitMask
     */
    public long unset(long bits)
    {
        return this.mask &= ~bits;
    }

    /**
     * Toggles the specified Bits
     * 
     * @param bits The bits to toggle
     * @return the new value of this BitMask
     */
    public long toggle(long bits)
    {
        return this.mask ^= bits;
    }

    /**
     * Checks whether the specified Bits are set.
     * 
     * @param bits The bits to check.
     * @return whether the specified Bit are set or not
     */
    public boolean isset(long bits)
    {
        return ((this.mask & bits) == bits);
    }
}
