package de.cubeisland.cubeengine.core.command.readers;

import de.cubeisland.cubeengine.core.command.ArgumentReader;
import de.cubeisland.cubeengine.core.command.exception.InvalidArgumentException;
import de.cubeisland.cubeengine.core.util.matcher.Match;
import org.bukkit.DyeColor;

public class DyeColorReader extends ArgumentReader<DyeColor>
{
    public DyeColorReader() {
        super(DyeColor.class);
    }

    @Override
    public DyeColor read(String arg) throws InvalidArgumentException
    {
        return Match.materialData().colorData(arg);
    }
}
