package de.cubeisland.cubeengine.core.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class contains some utillities to work with Strings
 *
 * @author Phillip Schichtel
 * @author Anselm Brehme
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

    private StringUtils()
    {
    }

    /**
     * This method splits a string without RegExes
     *
     * @param delim the delimiter
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
     * @param delim the delimiter
     * @param string the string to split
     * @param keepEmptyParts whether to keep empty parts
     * @return an array containing the parts
     */
    public static String[] explode(String delim, String string, boolean keepEmptyParts)
    {
        int pos, offset = 0, delimLen = delim.length();
        List<String> tokens = new ArrayList<String>();
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
     * This method merges an array of strings to a single string
     *
     * @param delim the delimiter
     * @param strings the strings to implode
     * @return the imploded string
     */
    public static String implode(CharSequence delim, CharSequence[] strings)
    {
        return implode(delim, Arrays.asList(strings));
    }

    /**
     * This method merges an array of strings to a single string
     *
     * @param delim the delimiter
     * @param strings the strings to implode
     * @return the imploded string
     */
    public static String implode(CharSequence delim, Iterable<CharSequence> strings)
    {
        Iterator<CharSequence> iterator = strings.iterator();
        if (!iterator.hasNext())
        {
            return "";
        }
        else
        {
            StringBuilder sb = new StringBuilder(iterator.next());

            while (iterator.hasNext())
            {
                sb.append(delim).append(iterator.next());
            }

            return sb.toString();
        }
    }

    /**
     * This method parses a query string
     *
     * @param queryString the query string
     * @param params a map to put the values in
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

    /**
     * Converts Time in d | h | m | s to Milliseconds
     */
    public static long convertTimeToMillis(String str)
    {
        Pattern pattern = Pattern.compile("^(\\d+)([smhd])?$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(str);
        matcher.find();

        long time;
        try
        {
            time = Integer.parseInt(String.valueOf(matcher.group(1)));
        }
        catch (IllegalStateException e)
        {
            return -1;
        }
        catch (Exception e)
        {
            //TODO Throw exception Or Show ErrorLog
            return -1;
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
        switch (unitSuffix.toLowerCase(Locale.ENGLISH).charAt(0))
        {
            case 'd':
                time *= 24;
            case 'h':
                time *= 60;
            case 'm':
                time *= 60;
            case 's':
                time *= 1000;
        }
        return time;
    }

    public static int wordDistance(String source, String target)
    {
        return wordDistance(source, target, new int[source.length() * target.length()]);
    }

    public static int wordDistance(String source, String target, int[] workspace)
    {
        int sourceLength = source.length();
        int targetLength = target.length();
        int lenS1 = sourceLength + 1;
        int lenT1 = targetLength + 1;
        if (lenT1 == 1)
        {
            return lenS1 - 1;
        }
        if (lenS1 == 1)
        {
            return lenT1 - 1;
        }
        int[] dl = workspace;
        int dlIndex = 0;
        int prevSourceIndex = 0, prevTargetIndex = 0, rowBefore = 0, min = 0, cost = 0, tmp = 0;
        int tri = lenS1 + 2;
        // start row with constant
        dlIndex = 0;
        for (tmp = 0; tmp < lenT1; tmp++)
        {
            dl[dlIndex] = tmp;
            dlIndex += lenS1;
        }
        for (int sourceIndex = 0; sourceIndex < sourceLength; sourceIndex++)
        {
            dlIndex = sourceIndex + 1;
            dl[dlIndex] = dlIndex; // start column with constant
            for (int tIndex = 0; tIndex < targetLength; tIndex++)
            {
                rowBefore = dlIndex;
                dlIndex += lenS1;
                //deletion
                min = dl[rowBefore] + 1;
                // insertion
                tmp = dl[dlIndex - 1] + 1;
                if (tmp < min)
                {
                    min = tmp;
                }
                cost = 1;
                if (source.charAt(sourceIndex) == target.charAt(tIndex))
                {
                    cost = 0;
                }
                if (sourceIndex > 0 && tIndex > 0)
                {
                    if (source.charAt(sourceIndex) == target.charAt(prevTargetIndex) && source.charAt(prevSourceIndex) == target.charAt(tIndex))
                    {
                        tmp = dl[rowBefore - tri] + cost;
                        // transposition
                        if (tmp < min)
                        {
                            min = tmp;
                        }
                    }
                }
                // substitution
                tmp = dl[rowBefore - 1] + cost;
                if (tmp < min)
                {
                    min = tmp;
                }
                dl[dlIndex] = min;
                
                prevTargetIndex = tIndex;
            }
            prevSourceIndex = sourceIndex;
        }
        return dl[dlIndex];
    }
}