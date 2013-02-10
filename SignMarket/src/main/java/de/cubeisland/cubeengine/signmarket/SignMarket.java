package de.cubeisland.cubeengine.signmarket;

import de.cubeisland.cubeengine.conomy.Conomy;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.signmarket.storage.SignMarketBlockManager;
import de.cubeisland.cubeengine.signmarket.storage.SignMarketInfoManager;


public class Signmarket extends Module
{
    private Conomy conomy;
    private SignMarketBlockManager smblockManager;
    private SignMarketInfoManager sminfoManager;

    @Override
    public void onEnable()
    {
        this.smblockManager = new SignMarketBlockManager(this);
        this.sminfoManager = new SignMarketInfoManager(this);
    }
}
