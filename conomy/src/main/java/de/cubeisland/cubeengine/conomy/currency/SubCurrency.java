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
package de.cubeisland.cubeengine.conomy.currency;

import de.cubeisland.cubeengine.conomy.config.SubCurrencyConfig;

public class SubCurrency
{
    private String name;
    private String symbol;
    private SubCurrency parent = null;
    private long valueForParent;
    private long valueInLowest;
    private String pluralSymbol;
    private String pluralName;

    public SubCurrency(String name, SubCurrencyConfig config, SubCurrency parent)
    {
        this.name = name;
        this.pluralName = config.longNamePlural;
        this.symbol = config.shortName;
        this.pluralSymbol = config.shortNamePlural;
        this.valueForParent = config.value;
        this.parent = parent;
    }

    public void setvalueInLowest(long valueInLowest)
    {
        this.valueInLowest = valueInLowest;
    }

    public String getSymbol()
    {
        return this.symbol;
    }

    public long getValueForParent()
    {
        return this.valueForParent;
    }

    public long getValueInLowest()
    {
        return valueInLowest;
    }

    public SubCurrency getParent()
    {
        return parent;
    }

    public String getName()
    {
        return name;
    }

    public String getPluralSymbol()
    {
        return pluralSymbol;
    }

    public String getPluralName()
    {
        return pluralName;
    }
}
