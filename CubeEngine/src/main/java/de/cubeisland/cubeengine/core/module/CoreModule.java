package de.cubeisland.cubeengine.core.module;

import java.util.Locale;

public final class CoreModule extends Module
{
    public static final String NAME = "Core";
    public static final String ID = NAME.toLowerCase(Locale.ENGLISH);
    private static CoreModule INSTANCE = null;
    private final ModuleInfo module;

    private CoreModule()
    {
        this.module = new ModuleInfo();
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

    @Override
    public ModuleInfo getInfo()
    {
        return this.module;
    }
}
