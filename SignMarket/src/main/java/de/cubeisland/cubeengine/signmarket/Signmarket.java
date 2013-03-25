package de.cubeisland.cubeengine.signmarket;

import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.conomy.Conomy;

public class Signmarket extends Module
{
    private Conomy conomy;
    private MarketSignFactory marketSignFactory;
    private SignMarketConfig config;
    private EditModeListener editModeListener;

    @Override
    public void onEnable()
    {
        this.marketSignFactory = new MarketSignFactory(this, this.conomy);
        this.editModeListener = new EditModeListener(this, this.conomy);

        this.getCore().getEventManager().registerListener(this, new MarketSignListener(this));

        this.getCore().getPermissionManager().registerPermissions(this, MarketSignPerm.values());

        this.getCore().getCommandManager().registerCommand(new SignMarketCommands(this));
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
