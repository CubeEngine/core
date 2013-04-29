package de.cubeisland.cubeengine.itemrepair.repair.blocks;

import java.io.File;

import org.bukkit.Material;

import de.cubeisland.cubeengine.core.config.InvalidConfigurationException;
import de.cubeisland.cubeengine.core.config.YamlConfiguration;
import de.cubeisland.cubeengine.core.config.annotations.Option;

public class RepairBlockConfig extends YamlConfiguration
{
    @Option("title")
    public String title;
    @Option("block")
    public Material blockType;
    @Option("break-percentage")
    public float breakPercentage;
    @Option("fail-percentage")
    public float failPercentage;
    @Option("cost-percentage")
    public float costPercentage;
    @Option("loose-enchantments-percentage")
    public float looseEnchPercentage;

    public static RepairBlockConfig defaultNormal()
    {
        RepairBlockConfig config = new RepairBlockConfig();
        config.title = "Normal Repair";
        config.blockType = Material.IRON_BLOCK;
        config.breakPercentage = 0;
        config.costPercentage = 100;
        config.looseEnchPercentage = 0;
        config.failPercentage = 0;
        return config;
    }

    public static RepairBlockConfig defaultCheap()
    {
        RepairBlockConfig config = new RepairBlockConfig();
        config.title = "Cheap Repair";
        config.blockType = Material.DIAMOND_BLOCK;
        config.breakPercentage = 0;
        config.costPercentage = 70;
        config.looseEnchPercentage = 5;
        config.failPercentage = 15;
        return config;
    }

    @Override
    public void onLoaded(File loadFrom)
    {
        if (!this.blockType.isBlock())
        {
            throw new InvalidConfigurationException("blockType must be a block!");
        }
    }
}
