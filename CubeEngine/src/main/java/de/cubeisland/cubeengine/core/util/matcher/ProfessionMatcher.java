package de.cubeisland.cubeengine.core.util.matcher;

import de.cubeisland.cubeengine.core.util.StringUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.bukkit.entity.Villager.Profession;

public class ProfessionMatcher
{
    private List<String>             professions;
    private static ProfessionMatcher instance = null;

    public static ProfessionMatcher get()
    {
        if (instance == null)
        {
            instance = new ProfessionMatcher();
            instance.professions = new ArrayList<String>();
            for (Profession profession : Profession.values())
            {
                instance.professions.add(profession.toString());
            }
        }
        return instance;
    }

    public Profession matchProfession(String name)
    {
        String match = StringUtils.matchString(name.toUpperCase(Locale.ENGLISH), this.professions);
        return Profession.valueOf(match);
    }
}
