package de.cubeisland.cubeengine.core.module;

import de.cubeisland.cubeengine.core.CubeEngine;

import java.util.Locale;

public final class CoreModule extends Module
{
    public static final String NAME = "Core";
    public static final String ID = NAME.toLowerCase(Locale.ENGLISH);
    private static CoreModule INSTANCE = null;

    private CoreModule()
    {
        this.initialize(CubeEngine.getCore(), new ModuleInfo(), CubeEngine.getFileManager().getDataFolder(), null, null, null);
    }

    public static CoreModule get()
    {
        if (INSTANCE == null)
        {
            INSTANCE = new CoreModule();
        }
        return INSTANCE;
    }

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public String getId()
    {
        return ID;
    }
}
