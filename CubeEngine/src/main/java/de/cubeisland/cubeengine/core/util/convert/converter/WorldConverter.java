package de.cubeisland.cubeengine.core.util.convert.converter;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.convert.Converter;
import java.util.UUID;
import org.bukkit.World;

public class WorldConverter implements Converter<World>
{
    @Override
    public Object toObject(World object) throws ConversionException
    {
        return object.getName() + "(" + object.getUID().toString() + ")";
    }

    @Override
    public World fromObject(Object object) throws ConversionException
    {

        if (object instanceof String)
        {
            String string = (String)object;
            World world = null;
            if (string.contains("(") && string.contains(")"))
            {
                UUID uid = UUID.fromString(string.substring(string.indexOf('('), string.indexOf(')') - 1));
                world = CubeEngine.getServer().getWorld(uid);
                string = string.substring(0,string.indexOf('('));
            }
            if (world == null)
            {
                world = CubeEngine.getServer().getWorld(string);
            }
            if (world != null)
            {
                return world;
            }
        }
        throw new ConversionException("Could not \"" + object + "\" convert to World!");
    }
}