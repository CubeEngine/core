package de.cubeisland.cubeengine.core.bukkit;

import de.cubeisland.cubeengine.core.CoreConfiguration;
import de.cubeisland.cubeengine.core.config.annotations.Comment;
import de.cubeisland.cubeengine.core.config.annotations.Option;
import de.cubeisland.cubeengine.core.config.annotations.Revision;

@Revision(1)
public class BukkitCoreConfiguration extends CoreConfiguration
{
    @Option("prevent-spam-kick")
    @Comment("Whether to prevent Bukkit from kicking players for spamming")
    public boolean preventSpamKick = false;

    @Option("commands.improve-vanilla")
    @Comment("Whether to replace the vanilla standard commands with improved ones")
    public boolean improveVanillaCommands = true;
}
