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
package de.cubeisland.engine.core.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.cubeisland.engine.core.contract.Contract.expect;


/**
 * This class contains some utillities to work with Strings.
 */
public final class StringUtils
{
    public static String repeat(String string, int i)
    {
        StringBuilder sb = new StringBuilder();
        while (i > 0)
        {
            --i;
            sb.append(string);
        }
        return sb.toString();
    }

    public static String repeat(char string, int i)
    {
        StringBuilder sb = new StringBuilder();
        while (i > 0)
        {
            --i;
            sb.append(string);
        }
        return sb.toString();
    }

    private StringUtils()
    {}

    /**
     * This method splits a string without RegExes
     *
     * @param delim  the delimiter
     * @param string the string to split
     * @return an array containing the parts
     */
    public static String[] explode(String delim, String string)
    {
        return explode(delim, string, true);
    }

    /**
     * This method splits a string without RegExes
     *
     * @param delim          the delimiter
     * @param string         the string to split
     * @param keepEmptyParts whether to keep empty parts
     * @return an array containing the parts
     */
    public static String[] explode(String delim, String string, boolean keepEmptyParts)
    {
        int pos, offset = 0, delimLen = delim.length();
        List<String> tokens = new ArrayList<>();
        String part;

        while ((pos = string.indexOf(delim, offset)) > -1)
        {
            part = string.substring(offset, pos);
            if (part.length() > 0 || keepEmptyParts)
            {
                tokens.add(part);
            }
            offset = pos + delimLen;
        }
        part = string.substring(offset);
        if (part.length() > 0 || keepEmptyParts)
        {
            tokens.add(part);
        }

        return tokens.toArray(new String[tokens.size()]);
    }

    /**
     * This method merges an array of objects to a single string
     *
     * @param delim   the delimiter
     * @param objects the objects to implode
     * @return the imploded string
     */
    public static String implode(String delim, Object[] objects)
    {
        return implode(delim, Arrays.asList(objects));
    }

    /**
     * This method merges an array of objects to a single string
     *
     * @param delimiter   the delimiter
     * @param objects the objects to implode
     * @return the imploded string
     */
    public static String implode(String delimiter, Iterable objects)
    {
        Iterator<?> iterator = objects.iterator();
        if (!iterator.hasNext())
        {
            return "";
        }
        else
        {
            StringBuilder sb = new StringBuilder(iterator.next().toString());

            while (iterator.hasNext())
            {
                sb.append(delimiter).append(iterator.next());
            }

            return sb.toString();
        }
    }

    /**
     * This method parses a query string
     *
     * @param queryString the query string
     * @param params      a map to put the values in
     */
    public static void parseQueryString(String queryString, Map<String, String> params)
    {
        if (queryString == null || params == null)
        {
            return;
        }
        if (queryString.length() > 0)
        {
            String token;
            int offset;
            StringTokenizer tokenizer = new StringTokenizer(queryString, "&");
            while (tokenizer.hasMoreTokens())
            {
                token = tokenizer.nextToken();
                if ((offset = token.indexOf('=')) > 0)
                {
                    params.put(urlDecode(token.substring(0, offset)), urlDecode(token.substring(offset + 1)));
                }
                else
                {
                    params.put(urlDecode(token), null);
                }
            }
        }
    }

    /**
     * Decodes the percent encoding scheme.
     *
     * For example: "an+example%20string" -> "an example string"
     */
    public static String urlDecode(String string)
    {
        if (string == null)
        {
            return null;
        }
        try
        {
            return URLDecoder.decode(string, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            return string;
        }
    }

    private static final long DAY = (long)24 * 60 * 60 * 1000;

    /**
     * Converts Time in y | M | w | d | h | m | s to Long default is m.
     */
    public static long convertTimeToMillis(String str) throws TimeConversionException
    {
        Pattern pattern = Pattern.compile("^(\\d+)([sSmhHdDwWMyY])?$");
        Matcher matcher = pattern.matcher(str);
        matcher.find();

        long time;
        try
        {
            time = Integer.parseInt(String.valueOf(matcher.group(1)));
        }
        catch (Exception e)
        {
            throw new TimeConversionException("Error while Converting String to time in millis");
        }
        if (time < 0)
        {
            return -1;
        }
        String unitSuffix = matcher.group(2);
        if (unitSuffix == null)
        {
            unitSuffix = "m";
        }
        switch (unitSuffix.charAt(0))
        {
            case 'y':
            case 'Y':
                time *= 365;
            case 'd':
            case 'D':
                time *= 24;
            case 'h':
            case 'H':
                time *= 60;
            case 'm':
                time *= 60;
            case 's':
            case 'S':
                time *= 1000;
                break;
            case 'W':
            case 'w':
                time *= 7 * DAY;
                break;
            case 'M':
                time *= 30 * DAY;
                break;
        }
        return time;
    }

    public static String trimRight(String string)
    {
        if (string == null)
        {
            return null;
        }
        if (string.isEmpty())
        {
            return string;
        }

        int lastPos = string.length();
        for (int i = string.length() - 1; i >= 0 && Character.isWhitespace(string.charAt(i)); --i)
        {
            --lastPos;
        }
        return string.substring(0, lastPos);
    }

    public static String trimLeft(String string)
    {
        if (string == null)
        {
            return null;
        }
        if (string.isEmpty())
        {
            return string;
        }

        int i = 0;
        while (i < string.length() && Character.isWhitespace(string.charAt(i)))
        {
            ++i;
        }
        return string.substring(i);
    }

    public static String trim(String string)
    {
        return trimRight(trimLeft(string));
    }

    public static String stripFileExtension(String filename)
    {
        if (filename == null)
        {
            return null;
        }
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > -1)
        {
            return filename.substring(0, lastDot);
        }
        return filename;
    }

    public static String padLeft(String string, int length)
    {
        return padLeft(string, ' ', length);
    }

    public static String padLeft(String string, char padChar, int length)
    {
        if (string == null)
        {
            return null;
        }
        final int num = length - string.length();
        if (num <= 0)
        {
            return string;
        }
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < num; ++i)
        {
            builder.append(padChar);
        }

        return builder.append(string).toString();
    }

    public static String padRight(String string, int length)
    {
        return padRight(string, ' ', length);
    }

    public static String padRight(String string, char padChar, int length)
    {
        if (string == null)
        {
            return null;
        }
        final int num = length - string.length();
        if (num <= 0)
        {
            return string;
        }
        StringBuilder builder = new StringBuilder(string);

        for (int i = 0; i < num; ++i)
        {
            builder.append(padChar);
        }

        return builder.toString();
    }

    public static String padCenter(String string, int length)
    {
        return padCenter(string, ' ', length);
    }

    public static String padCenter(String string, char padChar, int length)
    {
        if (string == null)
        {
            return null;
        }
        final int num = length - string.length();
        if (num <= 0)
        {
            return string;
        }

        final int numLeft = (int)Math.floor((double)num / 2.0);
        final int numRight = (int)Math.ceil((double)num / 2.0);

        return padLeft(string, padChar, numLeft) + string + padRight(string, padChar, numRight);
    }

    public static String ucFirst(String string)
    {
        return string.substring(0, 1) + string.substring(1);
    }

    public static boolean startsWithIgnoreCase(String string, String token)
    {
        if (string.length() < token.length())
        {
            return false;
        }

        return string.substring(0, token.length()).equalsIgnoreCase(token);
    }

    public static boolean endsWithIgnoreCase(String string, String token)
    {
        if (string.length() < token.length())
        {
            return false;
        }

        return string.substring(string.length() - token.length()).equalsIgnoreCase(token);
    }

    public static String replaceWithCallback(Pattern pattern, String string, ReplaceCallback callback)
    {
        final Matcher matcher = pattern.matcher(string);
        while(matcher.find())
        {
            final MatchResult matchResult = matcher.toMatchResult();
            final String replacement = callback.replace(matchResult);
            string = string.substring(0, matchResult.start()) + replacement + string.substring(matchResult.end());
            matcher.reset(string);
        }
        return string;
    }

    public static String getLastPart(String string, String separator)
    {
        int lastSepPos = string.indexOf(separator);
        if (lastSepPos == -1)
        {
            return string;
        }
        return string.substring(lastSepPos + 1);
    }

    public static String randomString(Random random, int length)
    {
        return randomString(random, length, "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQESRUVWXYZ0123456789");
    }

    public static String randomString(Random random, int length, String charset)
    {
        expect(length > 0, "The length must be greater than zero!");

        int upperLimit = charset.length();
        StringBuilder sb = new StringBuilder();

        for (; length > 0; --length)
        {
            sb.append(charset.charAt(random.nextInt(upperLimit)));
        }

        return sb.toString();
    }

    public static interface ReplaceCallback
    {
        String replace(MatchResult result);
    }
}
