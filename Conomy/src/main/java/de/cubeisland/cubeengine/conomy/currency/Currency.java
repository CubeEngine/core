package de.cubeisland.cubeengine.conomy.currency;

import com.google.common.collect.Lists;
import de.cubeisland.cubeengine.conomy.config.CurrencyConfiguration;
import de.cubeisland.cubeengine.conomy.config.SubCurrencyConfig;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.lang.StringUtils;

public class Currency
{
    private LinkedList<SubCurrency> sub = new LinkedList<SubCurrency>();
    private String formatlong;
    private String formatshort;
    private String name;
    private CurrencyManager manager;

    public Currency(CurrencyManager manager, String name, CurrencyConfiguration config)
    {
        this.manager = manager;
        this.name = name;
        this.formatlong = config.formatLong;
        this.formatshort = config.formatShort;
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

    public boolean canConvert(Currency currency)
    {
        return this.manager.canConvert(this, currency);
    }

    public Long getDefaultValue()
    {//Default value in config
        return 42L; //TODO
    }

    public String formatLong(Long balance)
    {
        boolean neg = false;
        if (balance < 0)
        {
            balance *= -1;
            neg = true;
        }
        String format = this.formatlong;
        for (SubCurrency subcur : Lists.reverse(this.sub))
        {
            Long subBalance = balance % subcur.getValueForParent();
            if (subcur.equals(this.sub.getFirst()))
            {
                format = format.replace("%" + subcur.getSymbol(), balance.toString());
            }
            else
            {
                format = format.replace("%" + subcur.getSymbol(), subBalance.toString());
            }
            balance -= balance % subcur.getValueForParent();
            balance /= subcur.getValueForParent();
        }
        format = format.replace("%-", neg ? "-" : "");
        return format;
    }
    String NUMBERSEPARATOR = ",";

    public Long parse(String amountString)
    {
        long result;
        String tempString = amountString;
        // Without CurrencySymbols:
        if (!tempString.matches("[^\\d" + NUMBERSEPARATOR + "]"))
        {
            int separators = StringUtils.countMatches(tempString, NUMBERSEPARATOR);
            try
            {
                if (separators == 0) // No separator try if its long
                {
                    result = Long.parseLong(tempString);
                    for (SubCurrency subCur : sub)
                    {
                        result *= subCur.getValueForParent();
                    }
                    return result / 100;
                }
                else if (separators <= this.sub.size() - 1)
                {
                    boolean first = true;
                    result = 0;
                    for (SubCurrency subCur : sub)
                    {
                        result *= subCur.getValueForParent();
                        int nextSeparator = tempString.indexOf(NUMBERSEPARATOR);
                        String read;//TODO if read is 1 parse to 10
                        if (nextSeparator == -1)
                        {
                            read = tempString;
                        }
                        else
                        {
                            read = tempString.substring(0, nextSeparator);
                            tempString = tempString.substring(nextSeparator + 1);
                        }
                        if (!first && read.length() > 2)
                        {
                            return null;
                        }
                        if (read.length() == 1 && !first)
                        {
                            read += "0";
                        }
                        result += Long.parseLong(read) * subCur.getValueForParent() / 100;
                        first = false;
                    }
                    return result;
                }
            }
            catch (NumberFormatException ignore)
            {
            }
        }
        tempString = amountString;
        //TODO parse known patterns formatLong/short
        final int stringLen = tempString.length();
        char current;
        StringBuilder token = new StringBuilder();
        ArrayList<String> tokens = new ArrayList<String>();
        boolean isNumber = false;
        for (int i = 0; i < stringLen; ++i)
        {
            current = tempString.charAt(i);
            if (Character.isDigit(current))
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
        HashMap<String, SubCurrency> symbols = new HashMap<String, SubCurrency>();
        for (SubCurrency currency : sub)
        {
            symbols.put(currency.getName().toLowerCase(Locale.ENGLISH), currency);
            symbols.put(currency.getSymbol().toLowerCase(Locale.ENGLISH), currency);
        }
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
}
