package de.cubeisland.cubeengine.conomy.config;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.config.annotations.Codec;
import de.cubeisland.cubeengine.core.config.annotations.Comment;
import de.cubeisland.cubeengine.core.config.annotations.DefaultConfig;
import de.cubeisland.cubeengine.core.config.annotations.Option;
import java.util.LinkedHashMap;
import java.util.Map;

@Codec("yml")
@DefaultConfig
public class ConomyConfiguration extends Configuration
{
    @Comment("Keep in mind subcurrencies have to be ordered in order of their value beginning with the highest.")
    @Option("currencies")
    public LinkedHashMap<String, CurrencyConfiguration> currencies = new LinkedHashMap<String, CurrencyConfiguration>();
    @Comment("0.5 means that 1 of currency1 is equal to 2 of currency2 (always using highest/first subcurrency)")
    @Option("relations")
    public LinkedHashMap<String, Map<String, Double>> relations;

    @Override
    public void onLoaded()
    {
        // Create a default currency when none given
        if (currencies == null || currencies.isEmpty())
        {
            this.currencies = new LinkedHashMap<String, CurrencyConfiguration>();
            LinkedHashMap<String, SubCurrencyConfig> subcurrencies = new LinkedHashMap<String, SubCurrencyConfig>();
            subcurrencies.put("Euro", new SubCurrencyConfig("€", 1));
            subcurrencies.put("Cent", new SubCurrencyConfig("c", 100));
            CurrencyConfiguration subConfig = new CurrencyConfiguration(subcurrencies, "%€ Euro %c Cent", "%€,%c€");
            this.currencies.put("Euro", subConfig);
        }
    }
}
