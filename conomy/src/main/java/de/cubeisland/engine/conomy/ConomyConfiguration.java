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

import de.cubeisland.engine.reflect.ReflectedYaml;
import de.cubeisland.engine.reflect.annotations.Comment;
import de.cubeisland.engine.reflect.annotations.Name;

@SuppressWarnings("all")
public class ConomyConfiguration extends ReflectedYaml
{
    @Name("currency.symbol")
    public String symbol = "€";
    @Name("currency.symbol-plural")
    public String symbolPlural = "€";
    @Name("currency.name")
    public String name = "Euro";
    @Name("currency.name-plural")
    public String namePlural = "Euros";

    @Name("default.user.balance")
    public double defaultBalance = 1000;
    @Name("default.user.minimum-balance")
    public double minimumBalance = 0;
    @Comment("Automatically creates the UserAccount when trying to access it")
    @Name("default.user.auto-create-account")
    public boolean autocreateUserAcc = true;

    @Name("default.bank.balance")
    public double defaultBankBalance = 0;
    @Name("default.bank.minimum-balance")
    public double minimumBankBalance = 0;
    @Name("default.bank.need-invite")
    public boolean bankNeedInvite = false;

    @Comment("The Number of fractional-digits.\n" +
                 "e.g.: 1.00€ -> 2")
    @Name("currency.fractional-digits")
    public int fractionalDigits = 2;

    @Name("enable-logging")
    public boolean enableLogging = true;

    public int fractionalDigitsFactor()
    {
        return (int)Math.pow(10, this.fractionalDigits);
    }
}
