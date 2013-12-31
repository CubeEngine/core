package de.cubeisland.engine.kits;

import de.cubeisland.engine.core.module.Module;

public class Kits extends Module
{
    private KitManager kitManager;
    
    @Override
    public void onEnable()
    {
        getCore().getDB().registerTable(TableKitsGiven.class);
        this.getCore().getConfigFactory().getDefaultConverterManager().
            registerConverter(KitItem.class, new KitItemConverter());

        this.kitManager = new KitManager(this);
        this.kitManager.loadKits();
        this.getCore().getUserManager().addDefaultAttachment(KitsAttachment.class, this);
        getCore().getCommandManager().registerCommand(new KitCommand(this));
    }

    public KitManager getKitManager()
    {
        return this.kitManager;
    }
}
