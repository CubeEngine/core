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
    private MarketSignFactory marketSignFactory;
    private SignMarketConfig config;
    private EditModeListener editModeListener;

    @Override
    public void onEnable()
    {
        this.smblockManager = new SignMarketBlockManager(this);
        this.sminfoManager = new SignMarketInfoManager(this);
        this.marketSignFactory = new MarketSignFactory(this);
        this.editModeListener = new EditModeListener(this);

        this.registerListener(new MarketSignListener(this));

        this.registerPermissions(MarketSignPerm.values());
    }

    public Conomy getConomy()
    {
        return conomy;
    }

    public SignMarketBlockManager getSmblockManager()
    {
        return smblockManager;
    }

    public SignMarketInfoManager getSminfoManager()
    {
        return sminfoManager;
    }

    public MarketSignFactory getMarketSignFactory()
    {
        return marketSignFactory;
    }

    public SignMarketConfig getConfig()
    {
        return config;
    }

    public EditModeListener getEditModeListener()
    {
        return editModeListener;
    }
}
