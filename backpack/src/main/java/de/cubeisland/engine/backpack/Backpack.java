package de.cubeisland.engine.backpack;

import java.io.File;

import de.cubeisland.engine.core.module.Module;

public class Backpack extends Module
{
    private BackpackConfig config;

    protected File singleDir;
    protected File groupedDir;
    protected File globalDir;

    @Override
    public void onEnable()
    {
        this.config = this.loadConfig(BackpackConfig.class);
        this.singleDir = this.getFolder().resolve("single").toFile();
        this.groupedDir = this.getFolder().resolve("grouped").toFile();
        this.globalDir = this.getFolder().resolve("global").toFile();
    }
}
