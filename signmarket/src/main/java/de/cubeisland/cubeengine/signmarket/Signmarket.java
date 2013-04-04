package de.cubeisland.cubeengine.signmarket;

import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.conomy.Conomy;

public class Signmarket extends Module
{
    private Conomy conomy;
    private MarketSignFactory marketSignFactory;
    private SignMarketConfig config;
    private EditModeListener editModeListener;
    private MarketSignPerm perm;

    @Override
    public void onEnable()
    {
        this.marketSignFactory = new MarketSignFactory(this, this.conomy);
        this.marketSignFactory.loadInAllSigns();
        this.editModeListener = new EditModeListener(this, this.conomy);

        this.getCore().getEventManager().registerListener(this, new MarketSignListener(this));

        this.perm = new MarketSignPerm(this);

        this.getCore().getCommandManager().registerCommand(new SignMarketCommands(this));
    }

    @Override
    public void onDisable()
    {
        this.perm.cleanup();
    }

    public MarketSignFactory getMarketSignFactory()
    {
        return this.marketSignFactory;
    }

    public SignMarketConfig getConfig()
    {
        return this.config;
    }

    public EditModeListener getEditModeListener()
    {
        return this.editModeListener;
    }
}
