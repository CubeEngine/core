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

import java.util.LinkedHashMap;

public class CurrencyConfiguration
{
    public LinkedHashMap<String, SubCurrencyConfig> subcurrencies;
    public String formatLong;
    public String formatShort;
    public long defaultBalance;
    public long minimumBalance;
    public String decimalSeparator;
    public String thousandSeparator;

    public CurrencyConfiguration(LinkedHashMap<String, SubCurrencyConfig> subcurrencies, String formatLong, String formatShort,
                                 long defaultBalance, long minimumBalance, String decimalSeparator, String thousandSeparator) {
        this.subcurrencies = subcurrencies;
        this.formatLong = formatLong;
        this.formatShort = formatShort;
        this.defaultBalance = defaultBalance;
        this.minimumBalance = minimumBalance;
        this.decimalSeparator = decimalSeparator;
        this.thousandSeparator = thousandSeparator;
    }
}
