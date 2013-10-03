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
package de.cubeisland.engine.core.service;

import java.util.List;
import java.util.Locale;

public interface Economy
{
    boolean isEnabled();
    String getName();
    boolean hasBankSupport();
    int fractionalDigits();
    long fractionalDigitsFactor();

    /**
     * Divides the long value by fractionalDigitsFactor()
     * <p>100 (cents) => 1.00 (Euro)
     *
     * @param value the value to convert
     * @return the converted value
     */
    double convertLongToDouble(long value);
    String format(double amount);
    String format(Locale locale, double amount);
    String currencyNamePlural();
    String currencyName();
    boolean hasAccount(String player);
    double getBalance(String playerName);
    boolean has(String playerName, double amount);
    boolean withdraw(String playerName, double amount);
    boolean deposit(String playerName, double amount);
    boolean createBank(String name, String ownerName);
    boolean deleteBank(String name);
    double getBankBalance(String name);
    boolean bankHas(String name, double amount);
    boolean bankWithdraw(String playerName, double amount);
    boolean bankDeposit(String playerName, double amount);
    boolean isBankOwner(String name, String playerName);
    boolean isBankMember(String name, String playerName);
    List<String> getBanks();
    boolean createPlayerAccount(String playerName);
    Double parse(String price);
    Double parseFor(String price, Locale locale);
}
