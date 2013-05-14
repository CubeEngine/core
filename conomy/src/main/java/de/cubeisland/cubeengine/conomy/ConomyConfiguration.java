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
package de.cubeisland.cubeengine.conomy;

import java.io.File;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.config.annotations.Codec;
import de.cubeisland.cubeengine.core.config.annotations.Comment;
import de.cubeisland.cubeengine.core.config.annotations.DefaultConfig;
import de.cubeisland.cubeengine.core.config.annotations.Option;
import de.cubeisland.cubeengine.conomy.Currency.CurrencyType;

@Codec("yml")
@DefaultConfig
public class ConomyConfiguration extends Configuration
{
    @Option("currency.symbol")
    public String symbol = "€";
    @Option("currency.symbol-plural")
    public String symbolPlural = "€";
    @Option("currency.name")
    public String name = "Euro";
    @Option("currency.name-plural")
    public String namePlural = "Euros";

    @Option("default.user.balance")
    public double defaultBalance = 1000;
    @Option("default.user.minimum-balance")
    public double minimumBalance = 0;

    @Option("default.bank.balance")
    public double defaultBankBalance = 0;
    @Option("default.bank.minimum-balance")
    public double minimumBankBalance = 0;
    @Comment("The Number of fractional-digits.\n" +
                 "e.g.: 1.00€ -> 2")
    @Option("currency.fractional-digits")
    public int fractionalDigits = 2;

    @Option("enable-logging")
    public boolean enableLogging = true;
    @Comment("Possible currency Types are: NORMAL, ITEM, EXP")
    @Option("currency.type")
    public CurrencyType currencyType = CurrencyType.NORMAL;

    @Override
    public void onLoaded(File loadFrom)
    {
        // TODO enforce options
        if (currencyType == CurrencyType.ITEM)
        {
            this.minimumBalance = 0;
        }
    }
}
