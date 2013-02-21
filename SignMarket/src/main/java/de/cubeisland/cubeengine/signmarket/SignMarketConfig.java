package de.cubeisland.cubeengine.signmarket;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.config.annotations.Codec;
import de.cubeisland.cubeengine.core.config.annotations.Comment;
import de.cubeisland.cubeengine.core.config.annotations.DefaultConfig;
import de.cubeisland.cubeengine.core.config.annotations.Option;

@Codec("yml")
@DefaultConfig
public class SignMarketConfig extends Configuration
{
    @Option("sign.overstack.in-sign")
    @Comment("Allows items in signs to be overstacked up to 64.")
    public boolean allowOverStackedInSign = false;

    @Option("sign.overstack.out-sign")
    @Comment("Allows items taken out of signs to be overstacked up to 64.")
    public boolean allowOverStackedOutOfSign = false;

    @Option("sign.admin.stock.allow")
    public boolean allowAdminStock = true;
    @Option("sign.admin.no-stock.allow")
    public boolean allowAdminNoStock = true;

    @Comment("The maximum amount of inventory-lines a admin-sign can have.\n" +
            "Use -1 for infinite stock-size OR values from 1-6!")
    @Option("sign.admin.stock.max")
    public int maxAdminStock = -1;
    @Comment("The maximum amount of inventory-lines a user-sign can have.\n" +
            "Use -1 for infinite stock-size OR values from 1-6!")
    @Option("sign.user.stock.max")
    public int maxUserStock = 6;
}
