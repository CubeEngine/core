package de.cubeisland.cubeengine.core.command.readers;

import de.cubeisland.cubeengine.core.command.ArgumentReader;
import de.cubeisland.cubeengine.core.command.exception.InvalidArgumentException;
import de.cubeisland.cubeengine.core.util.matcher.Match;
import org.bukkit.entity.Villager;

public class ProfessionReader extends ArgumentReader<Villager.Profession>
{
    public ProfessionReader()
    {
        super(Villager.Profession.class);
    }

    @Override
    public Villager.Profession read(String arg) throws InvalidArgumentException
    {
        return Match.profession().profession(arg);
    }
}
