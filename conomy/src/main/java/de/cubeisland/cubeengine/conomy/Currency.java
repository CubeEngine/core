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

import java.util.Locale;

import de.cubeisland.cubeengine.core.config.node.Node;
import de.cubeisland.cubeengine.core.config.node.StringNode;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.convert.Converter;
import de.cubeisland.cubeengine.conomy.account.AccountManager;

public class Currency
{
    //private Pattern pattern2 = Pattern.compile("[^a-zA-Z]+");
    //private Pattern pattern1;

    private final ConomyConfiguration config;

    private int fractionalDigitsFactor;
    private final AccountManager manager;


    public Currency(AccountManager manager, ConomyConfiguration config)
    {
        this.manager = manager;
        this.config = config;
        //this.pattern1 = Pattern.compile("^-*[\\d,]+$");
        this.fractionalDigitsFactor = (int)Math.pow(10, this.config.fractionalDigits);
    }

    public String getName()
    {
        return this.config.name;
    }

    public double getDefaultBalance()
    {
        return this.config.defaultBalance;
    }

    public String format(double balance)
    {
       return this.format(Locale.ENGLISH, balance);
    }

    public String format(Locale locale, double balance)
    {
        return String.format(locale, "%." + this.config.fractionalDigits
            + "f "+ this.config.symbol, balance);
    }

    public Double parse(String amountString)
    {
        try
        {
            return Double.parseDouble(amountString);
        }
        catch (NumberFormatException ex)
        {}
        // TODO filter currency Names / Symbols
        return null;
    }

    public double getMinMoney()
    {
        return this.config.minimumBalance;
    }

    public int fractionalDigitsFactor()
    {
        return this.fractionalDigitsFactor;
    }

    public CurrencyType getType()
    {
        return this.config.currencyType;
    }

    public double getMinBankMoney()
    {
        return this.config.minimumBankBalance;
    }

    public double getDefaultBankBalance()
    {
        return this.config.defaultBankBalance;
    }

    public enum CurrencyType
    {
        NORMAL, EXP, ITEM;
    }

    public static class CurrencyTypeConverter implements Converter<CurrencyType>
    {
        @Override
        public Node toNode(CurrencyType object) throws ConversionException
        {
            return StringNode.of(object.name());
        }

        @Override
        public CurrencyType fromNode(Node node) throws ConversionException
        {
            try
            {
                return CurrencyType.valueOf(node.unwrap().toUpperCase());
            }
            catch (IllegalArgumentException ex)
            {
                throw new ConversionException("Could not convert " + node.unwrap() + " to a currencyType");
            }
        }
    }
}
