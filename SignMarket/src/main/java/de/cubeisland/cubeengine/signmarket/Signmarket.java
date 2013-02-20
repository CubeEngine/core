package de.cubeisland.cubeengine.signmarket;

import de.cubeisland.cubeengine.conomy.Conomy;
import de.cubeisland.cubeengine.core.module.Module;

public class Signmarket extends Module
{
    private Conomy conomy;
    private MarketSignFactory marketSignFactory;
    private SignMarketConfig config;
    private EditModeListener editModeListener;

    @Override
    public void onEnable()
    {
        this.marketSignFactory = new MarketSignFactory(this);
        this.editModeListener = new EditModeListener(this);

        this.registerListener(new MarketSignListener(this));

        this.registerPermissions(MarketSignPerm.values());

        this.registerCommand(new SignMarketCommands(this));
    }

    public Conomy getConomy()
    {
        return conomy;
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
