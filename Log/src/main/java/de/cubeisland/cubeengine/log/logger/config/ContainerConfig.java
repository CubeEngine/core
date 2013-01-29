package de.cubeisland.cubeengine.log.logger.config;

import de.cubeisland.cubeengine.core.config.annotations.Option;
import de.cubeisland.cubeengine.log.LoggerConfig;

public  class ContainerConfig extends LoggerConfig
{
    public ContainerConfig()
    {super(true);
    }
    @Option("log-chest")
    public boolean logChest = true;
    @Option("log-furnace")
    public boolean logFurnace = false;
    @Option("log-brewing")
    public boolean logBrewingstand = false;
    @Option("log-dispenser")
    public boolean logDispenser = true;
    @Option("log-other-block")
    public boolean logOtherBlock = true;
    @Option("log-storage-minecart")
    public boolean logStorageMinecart = false;
    @Option("log-looked-into-chest")
    public boolean logNothing = true;



    @Override
    public String getName()
    {
        return "container";
    }
}