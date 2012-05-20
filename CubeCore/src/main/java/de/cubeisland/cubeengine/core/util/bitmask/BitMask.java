package de.cubeisland.cubeengine.core.util.bitmask;

/**
 * Represents a bitmask with 32 bits
 *
 * @author Phillip Schichtel
 */
public class BitMask
{
    private int mask;

    /**
     * Creates a new BitMask
     * 
     */
    public BitMask()
    {
        this(0);
    }

    /**
     * Creates a new BitMask and set its value to mask
     * 
     * @param mask the value to set the BitMask at the beginning
     */
    public BitMask(int mask)
    {
        this.mask = mask;
    }

    /**
     * Gets the value of this BitMask
     * 
     * @return the value saved by this BitMask
     */
    public int get()
    {
        return this.mask;
    }

    /**
     * Sets the specified Bit
     * 
     * @param bits The bit to set
     * @return the new value of this BitMask
     */
    public int set(int bits)
    {
        return this.mask |= bits;
    }

    /**
     * Resets this BitMask to 0
     * 
     * @return 0
     */
    public int reset()
    {
        return this.reset(0);
    }

    /**
     * Resets this BitMask to mask
     * 
     * @param mask The value to reset the BitMask to
     * @return the new value of this BitMask
     */
    public int reset(int mask)
    {
        return this.mask = mask;
    }

    /**
     * Unsets the specified Bit
     * 
     * @param bits The bit to unset
     * @return the new value of this BitMask
     */
    public int unset(int bits)
    {
        return this.mask &= ~bits;
    }

    /**
     * Toggles the specified Bit
     * 
     * @param bits The bit to toggle
     * @return the new value of this BitMask
     */
    public int toggle(int bits)
    {
        return this.mask ^= bits;
    }

    /**
     * Checks whether the specified Bit is set.
     * 
     * @param bits The bit to check.
     * @return whether the specified Bit is set or not
     */
    public boolean isset(int bits)
    {
        return ((this.mask & bits) == bits);
    }
}
