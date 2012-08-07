package de.cubeisland.cubeengine.core.util.converter;

/**
 *
 * @author Anselm Brehme
 */
public class IntegerConverter extends BasicConverter<Integer>
{
    public Integer fromString(String string)
    {
        return Integer.parseInt(string);
    }
}
