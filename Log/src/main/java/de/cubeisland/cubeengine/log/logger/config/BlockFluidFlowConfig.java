package de.cubeisland.cubeengine.log.logger.config;

import de.cubeisland.cubeengine.core.config.annotations.Comment;
import de.cubeisland.cubeengine.core.config.annotations.Option;
import de.cubeisland.cubeengine.log.SubLogConfig;

public  class BlockFluidFlowConfig extends SubLogConfig
{
    public BlockFluidFlowConfig()
    {
        super(false);
    }

    @Comment("Logging water flowing normally and replacing air or water")
    @Option("log-water-flow")
    public boolean logWaterFlow = false;
    @Comment("Logging lava flowing normally and replacing air or lava")
    @Option("log-lava-flow")
    public boolean logLavaFlow = false;
    @Comment("Logging water destroying blocks like redstone etc.")
    @Option("log-water-destruction")
    public boolean logWaterDestruct = true;
    @Comment("Logging lava destroying blocks like redstone etc.")
    @Option("log-lava-destruction")
    public boolean logLavaDestruct = true;
    @Comment("Logging lava or water creating stone, cobblestone or obsidian")
    @Option("log-water-lava-creation")
    public boolean logLavaWaterCreation = true;
    @Comment("Logging obsidian creation with redstone")
    @Option("log-redstone-obsidian-creation")
    public boolean logRedsObsiCreation = true;

    @Override
    public String getName()
    {
        return "block-fluids";
    }
}