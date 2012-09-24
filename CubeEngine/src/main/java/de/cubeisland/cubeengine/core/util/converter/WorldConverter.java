package de.cubeisland.cubeengine.core.util.converter;

import de.cubeisland.cubeengine.core.CubeEngine;
import org.bukkit.World;

/**
 *
 * @author Anselm Brehme
 */
public class WorldConverter implements Converter<World>
{

    @Override
    public Object toObject(World object) throws ConversionException
    {
        return this.toString(object);
    }

    @Override
    public World fromObject(Object object) throws ConversionException
    {
        return this.fromString(object.toString());
    }

    @Override
    public String toString(World object)
    {
        return object.getName();
    }

    @Override
    public World fromString(String string) throws ConversionException
    {
        return CubeEngine.getServer().getWorld(string);
    }
    
}
