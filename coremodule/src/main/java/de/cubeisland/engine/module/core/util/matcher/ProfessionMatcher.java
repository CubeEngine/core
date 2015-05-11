package de.cubeisland.engine.module.core.util.matcher;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import de.cubeisland.engine.module.core.sponge.SpongeCore;
import org.spongepowered.api.Game;
import org.spongepowered.api.data.types.Profession;

public class ProfessionMatcher
{
    private final Map<String, Profession> professions = new HashMap<>();
    private SpongeCore core;

    public ProfessionMatcher(SpongeCore core, Game game)
    {
        this.core = core;
        for (Profession profession : game.getRegistry().getAllOf(Profession.class))
        {
            this.professions.put(profession.getName().toLowerCase(), profession);
        }
    }

    public Profession profession(String name)
    {
        return professions.get(core.getModularity().start(StringMatcher.class).matchString(name.toLowerCase(Locale.ENGLISH),
                                                                                     this.professions.keySet()));
    }

    public String[] professions()
    {
        return professions.keySet().toArray(new String[professions.size()]);
    }
}
