package de.cubeisland.cubeengine.signmarket.storage;

import de.cubeisland.cubeengine.core.storage.SingleKeyStorage;
import de.cubeisland.cubeengine.signmarket.Signmarket;

public class SignMarketInfoManager extends SingleKeyStorage<Long, SignMarketInfoModel>
{
    private static final int REVISION = 1;
    private final Signmarket module;


    public SignMarketInfoManager(Signmarket module)
    {
        super(module.getDatabase(), SignMarketInfoModel.class, REVISION);
        this.initialize();
        this.module = module;
    }
}
