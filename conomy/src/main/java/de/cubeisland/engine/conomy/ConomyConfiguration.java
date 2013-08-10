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
package de.cubeisland.engine.conomy;

import java.nio.file.Path;

import de.cubeisland.engine.core.config.Configuration;
import de.cubeisland.engine.core.config.annotations.Codec;
import de.cubeisland.engine.core.config.annotations.Comment;
import de.cubeisland.engine.core.config.annotations.DefaultConfig;
import de.cubeisland.engine.core.config.annotations.Option;

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
    @Comment("Automatically creates the UserAccount when trying to access it")
    @Option("default.user.auto-create-account")
    public boolean autocreateUserAcc = true;

    @Option("default.bank.balance")
    public double defaultBankBalance = 0;
    @Option("default.bank.minimum-balance")
    public double minimumBankBalance = 0;
    @Option("default.bank.need-invite")
    public boolean bankNeedInvite = false;

    @Comment("The Number of fractional-digits.\n" +
                 "e.g.: 1.00€ -> 2")
    @Option("currency.fractional-digits")
    public int fractionalDigits = 2;

    @Option("enable-logging")
    public boolean enableLogging = true;

    private int fractionalDigitsFactor;


    @Override
    public void onLoaded(Path loadFrom)
    {
        this.fractionalDigitsFactor = (int)Math.pow(10, this.fractionalDigits);
    }

    public int fractionalDigitsFactor()
    {
        return this.fractionalDigitsFactor;
    }

}
