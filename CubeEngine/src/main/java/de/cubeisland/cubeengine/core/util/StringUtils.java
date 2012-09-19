package de.cubeisland.cubeengine.core.util;

import de.cubeisland.cubeengine.core.util.converter.ConversionException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
    private static final long DAY = (long)24 * 60 * 60 * 1000;

    /**
     * Converts Time in y | M | w | d | h | m | s to Long default is m
     */
    public static long convertTimeToMillis(String str) throws ConversionException
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
            throw new ConversionException("Error while Converting String to time in millis");
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

    /**
     * Taken from org.apache.commons.lang.StringUtils
     */
    public static int getLevenshteinDistance(String s, String t)
    {
        if (s == null || t == null)
        {
            throw new IllegalArgumentException("Strings must not be null");
        }

        int n = s.length(); // length of s
        int m = t.length(); // length of t

        if (n == 0)
        {
            return m;
        }
        else if (m == 0)
        {
            return n;
        }

        if (n > m)
        {
            // swap the input strings to consume less memory
            String tmp = s;
            s = t;
            t = tmp;
            n = m;
            m = t.length();
        }

        int p[] = new int[n + 1]; //'previous' cost array, horizontally
        int d[] = new int[n + 1]; // cost array, horizontally
        int _d[]; //placeholder to assist in swapping p and d

        // indexes into strings s and t
        int i; // iterates through s
        int j; // iterates through t

        char t_j; // jth character of t

        int cost; // cost

        for (i = 0; i <= n; i++)
        {
            p[i] = i;
        }

        for (j = 1; j <= m; j++)
        {
            t_j = t.charAt(j - 1);
            d[0] = j;

            for (i = 1; i <= n; i++)
            {
                cost = s.charAt(i - 1) == t_j ? 0 : 1;
                // minimum of cell to the left+1, to the top+1, diagonally left and up +cost
                d[i] = Math.min(Math.min(d[i - 1] + 1, p[i] + 1), p[i - 1] + cost);
            }

            // copy current distance counts to 'previous row' distance counts
            _d = p;
            p = d;
            d = _d;
        }

        // our last action in the above loop was to switch d and p, so p now 
        // actually has the most recent cost counts
        return p[n];
    }

    /**
     * CaseInsensitive StringMatching First LD-Check 1 IndexCheck maxIndex:
     * string.length() maxBehind: 20 Second LD-Check 2 maxLength:
     * string.length()
     *
     * @param string the string to search
     * @param stringlist the possible Strings to find
     * @return a found match or null
     */
    public static String matchString(String string, Collection<String> stringlist)
    {
        return matchString(string, stringlist, true, 1, string.length(), 20, 2, string.length());
    }

    /**
     * Tries to match a String with - LD with distance
     *
     * @param firstLdCheck - Index with
     * @param maxIndex and
     * @param maxbehindIndex - LD at start with distance
     * @param secondLdCheck when longer than
     * @param maxLengthforLdCheck
     *
     * @param string
     * @param stringlist
     * @param caseInSensitive
     * @param firstLdCheck
     * @param maxIndex
     * @param maxbehindIndex
     * @param secondLdCheck
     * @param minLengthforLdCheck
     * @return a matching String
     */
    public static String matchString(String string, Collection<String> stringlist, boolean caseInSensitive, int firstLdCheck, int maxIndex, int maxbehindIndex, int secondLdCheck, int minLengthforLdCheck)
    {
        if (stringlist == null || string == null || stringlist.isEmpty())
        {
            return null;
        }
        int distance;
        int ld;
        String searchString = string;
        if (caseInSensitive)
        {
            searchString = searchString.toLowerCase(Locale.ENGLISH);
        }
        int searchStringLength = searchString.length();
        String foundString = null;
        if (stringlist.contains(string))
        {
            return string; // Direct Match
        }
        if (firstLdCheck >= 1) // LD lower than 1 -> NO Check
        {
            distance = firstLdCheck + 1;
            for (String inList : stringlist)
            {
                if ((searchStringLength < (inList.length() - 3)) || (searchStringLength > (inList.length() + 3)))
                {
                    continue; // length differ by more than 3
                }
                if (caseInSensitive)
                {
                    ld = getLevenshteinDistance(inList.toLowerCase(Locale.ENGLISH), string);
                }
                else
                {
                    ld = getLevenshteinDistance(inList, string);
                }
                if (ld <= firstLdCheck) // Match with ld <= 2 and searchString > 4
                {
                    if (ld < distance)
                    {
                        distance = ld;
                        foundString = inList;
                    }
                }
            }
        }
        if (maxIndex >= 0) // Index lower than 0 -> NO CHECK
        {
            if (foundString == null) // Not Found -> does String contain searchString?
            {
                int indexfound = maxIndex;
                int index;
                int behindindex = maxbehindIndex;
                for (String inList : stringlist)
                {
                    if (caseInSensitive)
                    {
                        index = inList.toLowerCase(Locale.ENGLISH).indexOf(searchString);
                    }
                    else
                    {
                        index = inList.indexOf(searchString);
                    }
                    if (index != -1) // Found seachString in inList
                    {
                        if (index < indexfound) // Compare to last match
                        {
                            indexfound = index;
                            behindindex = inList.length() - (index + searchStringLength);
                            foundString = inList;
                        }
                        if (index == indexfound && inList.length() - (index + searchStringLength) <= behindindex)
                        {
                            behindindex = inList.length() - (index + searchStringLength);
                            foundString = inList;
                        }
                    }
                }
            }
        }
        if (secondLdCheck >= 1) // LD lower than 1 -> NO Check
        {
            if (foundString == null) // Not Found -> search for Typo at start
            {
                if (searchStringLength > minLengthforLdCheck) // Only search if long enough
                {
                    distance = secondLdCheck + 1;
                    for (String inList : stringlist)
                    {
                        if (inList.length() >= searchStringLength) // can inList contain searchString?
                        {
                            String subString = inList.substring(0, searchStringLength);
                            if (caseInSensitive)
                            {
                                subString = subString.toLowerCase(Locale.ENGLISH);
                            }
                            ld = getLevenshteinDistance(subString, searchString);
                            if (ld <= secondLdCheck || ld == 1) // Found light Typo at start of String
                            {
                                if (ld < distance) // Compare to last match
                                {
                                    if (ld == 1)
                                    {
                                        return inList; // ld 1 Typo in start of String
                                    }
                                    distance = ld;
                                    foundString = inList;
                                }
                            }
                        }
                    }
                }
            }
        }
        return foundString;
    }
}
