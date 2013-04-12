/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.cubeengine.conomy.config;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.config.annotations.Codec;
import de.cubeisland.cubeengine.core.config.annotations.Comment;
import de.cubeisland.cubeengine.core.config.annotations.DefaultConfig;
import de.cubeisland.cubeengine.core.config.annotations.Option;

@Codec("yml")
@DefaultConfig
public class ConomyConfiguration extends Configuration
{
    @Comment("Keep in mind subcurrencies have to be ordered in order of their value beginning with the highest.")
    @Option("currencies")
    public LinkedHashMap<String, CurrencyConfiguration> currencies = new LinkedHashMap<String, CurrencyConfiguration>();
    @Option("relations")
    public LinkedHashMap<String, Map<String, Double>> relations = new LinkedHashMap<String, Map<String, Double>>();
    @Option("enable-logging")
    public boolean enableLogging = true;

    @Override
    public void onLoaded(File loadFrom)
    {
        // Create a default currency when none given
        if (currencies == null || currencies.isEmpty())
        {
            this.currencies = new LinkedHashMap<String, CurrencyConfiguration>();
            LinkedHashMap<String, SubCurrencyConfig> subcurrencies = new LinkedHashMap<String, SubCurrencyConfig>();
            subcurrencies.put("Euro", new SubCurrencyConfig("Euros","€","€", 1));
            subcurrencies.put("Cent", new SubCurrencyConfig("Cents","c","c", 100));
            CurrencyConfiguration subConfig = new CurrencyConfiguration(subcurrencies, "%-%€ #€ %c #c", "%-%€,%c2#€", 10000,0,",",".");
            this.currencies.put("Euro", subConfig);
        }
    }
}
