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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.cubeisland.cubeengine.conomy.config.CurrencyConfiguration;
import de.cubeisland.cubeengine.conomy.config.SubCurrencyConfig;

import com.google.common.collect.Lists;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import org.apache.commons.lang.StringUtils;

public class Currency
{
    private LinkedList<SubCurrency> sub = new LinkedList<SubCurrency>();
    private String formatlong;
    private String formatshort;
    private String name;
    private CurrencyManager manager;
    private long defaultBalance;
    private long minMoney = 0;
    private String decimalSeperator;
    private Pattern pattern2 = Pattern.compile("[^a-zA-Z]+");
    private Pattern pattern1;
    private TObjectDoubleHashMap<Currency> conversionRates = new TObjectDoubleHashMap<Currency>();

    public Currency(CurrencyManager manager, String name, CurrencyConfiguration config)
    {
        this.manager = manager;
        this.name = name;
        this.formatlong = config.formatLong;
        this.formatshort = config.formatShort;
        this.defaultBalance = config.defaultBalance;
        this.decimalSeperator = config.decimalSeparator;

        this.pattern1 = Pattern.compile("^-*[\\d,]+$");

        SubCurrency parent = null;
        for (Map.Entry<String, SubCurrencyConfig> entry : config.subcurrencies.entrySet())
        {
            parent = new SubCurrency(entry.getKey(), entry.getValue(), parent);
            this.sub.add(parent);
        }
        long valueInLowest = 1L;
        for (SubCurrency currency : Lists.reverse(sub))
        {
            currency.setvalueInLowest(valueInLowest);
            valueInLowest *= currency.getValueForParent();
        }
    }

    public String getName()
    {
        return name;
    }

    public Long getDefaultBalance()
    {
        return this.defaultBalance;
    }

    private String format(String format, Long balance, boolean isShort)
    {
        boolean neg = false;
        if (balance < 0)
        {
            balance *= -1;
            neg = true;
        }
        for (SubCurrency subcur : Lists.reverse(this.sub))
        {
            Pattern pattern = Pattern.compile("(%" + subcur.getSymbol() + ")(\\d*)");
            Matcher matcher = pattern.matcher(format);
            Long subBalance = balance % subcur.getValueForParent();
            format = format.replace("%-", neg ? "-" : "");
            while (matcher.find())
            {
                if (subcur.equals(this.sub.getFirst()))
                {
                    format = format.replace(matcher.group(), balance.toString());
                }
                else
                {
                    String subBalanceString = subBalance.toString();
                    if (!matcher.group(2).equals(""))
                    {
                        Integer zerofill = Integer.valueOf(matcher.group(2));
                        if (subBalanceString.length() < zerofill)
                        {
                            subBalanceString = de.cubeisland.cubeengine.core.util.StringUtils.repeat('0',zerofill - subBalanceString.length()) + subBalanceString;
                        }
                    }
                    format = format.replace(matcher.group(), subBalanceString);
                }
            }
            balance -= balance % subcur.getValueForParent();
            balance /= subcur.getValueForParent();
            // CurencyNames
            pattern = Pattern.compile("#" + subcur.getSymbol());
            matcher = pattern.matcher(format);
            while (matcher.find())
            {
                if (subBalance == 1)
                {
                    format = format.replace(matcher.group(),isShort ? subcur.getSymbol() : subcur.getName());
                }
                else
                {
                    format = format.replace(matcher.group(),isShort ? subcur.getPluralSymbol() : subcur.getPluralName());
                }
            }
        }
        return format;
    }

    public String formatLong(Long balance)
    {
        return this.format(this.formatlong, balance, false);
    }

    public String formatShort(Long balance)
    {
        return this.format(this.formatshort, balance, true);
    }

    public Long parse(String amountString)
    {
        if (amountString == null)
        {
            return null;
        }
        amountString = amountString.toLowerCase();
        long result;
        String tempString = amountString;
        if (tempString.endsWith(this.name.toLowerCase())) // remove currency name at the end
        //allows parsing e.g.: 1,4Euro
        {
            tempString = tempString.substring(0, tempString.length() - this.name.length());
        }
        // Without CurrencySymbols:
        if (pattern1.matcher(tempString).find() && pattern2.matcher(tempString).find())
        {
            int separators = StringUtils.countMatches(tempString, decimalSeperator);
            try
            {
                if (separators == 0) // No separator try if its long
                {
                    result = Long.parseLong(tempString);
                    result *= this.sub.get(0).getValueInLowest();
                    return result;
                }
                else if (separators <= this.sub.size() - 1)
                {
                    boolean first = true;
                    boolean end = false;
                    result = 0;
                    for (SubCurrency subCur : sub)
                    {
                        result *= subCur.getValueForParent();
                        if (end)
                        {
                            break;
                        }
                        int subCurLen = String.valueOf(subCur.getValueForParent() - 1).length();
                        int nextSeparator = tempString.indexOf(decimalSeperator);
                        String read;
                        if (nextSeparator == -1)
                        {
                            read = tempString;
                            end = true;
                        }
                        else
                        {
                            read = tempString.substring(0, nextSeparator);
                            tempString = tempString.substring(nextSeparator + 1);
                        }

                        if (!first && read.length() > subCurLen)
                        {
                            return null;
                        }
                        while (read.length() < subCurLen && !first)
                        {
                            read += "0";
                        }
                        result += Long.parseLong(read);
                        first = false;
                    }
                    return result;
                }
            }
            catch (NumberFormatException ignore)
            {}
        }
        tempString = amountString;
        //TODO parse known patterns formatLong/short
        //e.g.: 1,4Euro
        final int stringLen = tempString.length();
        Character current;
        StringBuilder token = new StringBuilder();
        ArrayList<String> tokens = new ArrayList<String>();
        boolean isNumber = false;
        for (int i = 0; i < stringLen; ++i)
        {
            current = tempString.charAt(i);
            if (Character.isDigit(current) || current == '-')
            {
                if (isNumber)
                {
                    token.append(current);
                }
                else
                {
                    if (!token.toString().isEmpty())
                    {
                        tokens.add(token.toString());
                    }
                    token = new StringBuilder();
                    token.append(current);
                }
                isNumber = true;
            }
            else
            {
                if (Character.isWhitespace(current) || isNumber)
                {
                    if (!token.toString().isEmpty())
                    {
                        tokens.add(token.toString());
                    }
                    token = new StringBuilder();
                    if (!Character.isWhitespace(current))
                    {
                        token.append(current);
                        if (!this.decimalSeperator.equals("") && current.toString().equalsIgnoreCase(this.decimalSeperator))
                        {
                            tokens.add(token.toString());
                            token = new StringBuilder();
                        }
                    }
                }
                else
                {
                    token.append(current);
                }
                isNumber = false;
            }
        }
        if (!token.toString().isEmpty())
        {
            tokens.add(token.toString());
        }
        for (String s : tokens)
        {
            System.out.println(s);
        }
        HashMap<String, SubCurrency> symbols = this.getAllSymbolsAndNames();
        try
        {
            result = 0;
            long amount = 0;
            for (int i = 0; i < tokens.size(); ++i)
            {
                String readToken = tokens.get(i).toLowerCase(Locale.ENGLISH);
                if (i % 2 == 0)
                {
                    amount = Long.parseLong(readToken);
                }
                else
                {
                    if (symbols.keySet().contains(readToken))
                    {
                        SubCurrency currency = symbols.get(readToken);
                        amount *= currency.getValueInLowest();
                        result += amount;
                        amount = 0;
                    }
                    else
                    {
                        return null;
                    }
                }
            }
        }
        catch (NumberFormatException ingored)
        {
            return null;
        }
        return result;
    }

    public HashMap<String, SubCurrency> getAllSymbolsAndNames()
    {
        HashMap<String, SubCurrency> symbols = new HashMap<String, SubCurrency>();
        symbols.putAll(this.getAllNames());
        symbols.putAll(this.getAllSymbols());
        return symbols;
    }

    public HashMap<String, SubCurrency> getAllSymbols()
    {

        HashMap<String, SubCurrency> symbols = new HashMap<String, SubCurrency>();
        for (SubCurrency currency : sub)
        {
            symbols.put(currency.getSymbol().toLowerCase(Locale.ENGLISH), currency);
        }
        return symbols;
    }

    public HashMap<String, SubCurrency> getAllNames()
    {
        HashMap<String, SubCurrency> symbols = new HashMap<String, SubCurrency>();
        for (SubCurrency currency : sub)
        {
            symbols.put(currency.getName().toLowerCase(Locale.ENGLISH), currency);
        }
        return symbols;
    }

    public long getMinMoney()
    {
        return minMoney;
    }

    public void addConversionRate(Currency cur, double rate)
    {
        this.conversionRates.put(cur, rate);
    }

    public boolean canConvert(Currency currency)
    {
        if (this.equals(currency))
        {
            return true;
        }
        return this.conversionRates.containsKey(currency);
    }

    /**
     * Converts the amount in given currency into this currency.
     *
     * @param currency
     * @param amount
     * @return
     */
    public long convert(Currency currency, long amount)
    {
        Double rate = currency.conversionRates.get(currency);
        if (rate == null)
        {
            throw new IllegalArgumentException("Currency not conversible! " + currency.getName() + " & " + this.getName());
        }
        return amount *= rate;
    }
}
