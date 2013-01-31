package de.cubeisland.cubeengine.core.util.matcher;

import org.bukkit.entity.Villager.Profession;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProfessionMatcher
{
    private List<String> professions;

    public ProfessionMatcher() {
        this.professions = new ArrayList<String>();
        for (Profession profession : Profession.values())
        {
            this.professions.add(profession.toString());
        }
    }

    public Profession profession(String name)
    {
        String match = Match.string().matchString(name.toUpperCase(Locale.ENGLISH), this.professions);
        return Profession.valueOf(match);
    }
}
