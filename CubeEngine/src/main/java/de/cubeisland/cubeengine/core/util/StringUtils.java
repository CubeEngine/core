package de.cubeisland.cubeengine.core.util;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.log.LogLevel;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private StringUtils()
    {
    }

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
     * @param delim   the delimiter
     * @param strings the strings to implode
     * @return the imploded string
     */
    public static String implode(String delim, String[] strings)
    {
        return implode(delim, Arrays.asList(strings));
    }

    /**
     * This method merges an array of strings to a single string
     *
     * @param delim   the delimiter
     * @param strings the strings to implode
     * @return the imploded string
     */
    public static String implode(String delim, Iterable<String> strings)
    {
        Iterator<String> iterator = strings.iterator();
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
     * Computes the LevenshteinDistance between s and t.
     * (Taken from org.apache.commons.lang.StringUtils)
     *
     * @param s
     * @param t
     * @return the ld between s and t
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
        else
        {
            if (m == 0)
            {
                return n;
            }
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
    static final int wd = 1, wi = 1, wc = 1, ws = 1;

    /**
     * Computes the Demerau-LevenshteinDistance.
     * (Taken from http://qqqqx.blogspot.de/2011/09/dameraulevenshtein-distance.html)
     *
     * @param a
     * @param b
     * @return the DemerauLevenshteinDistance between a and b.
     */
    public static int getDemerauLevenshteinDistance(String a, String b)
    {
        final int inf = a.length() * wd + b.length() * wi + 1;
        int[][] H = new int[a.length() + 2][b.length() + 2];
        for (int i = 0; i <= a.length(); i++)
        {
            H[i + 1][1] = i * wd;
            H[i + 1][0] = inf;
        }
        for (int j = 0; j <= b.length(); j++)
        {
            H[1][j + 1] = j * wi;
            H[0][j + 1] = inf;
        }
        HashMap<Character, Integer> DA = new HashMap<Character, Integer>();
        for (int d = 0; d < a.length(); d++)
        {
            if (!DA.containsKey(a.charAt(d)))
            {
                DA.put(a.charAt(d), 0);
            }
        }
        for (int d = 0; d < b.length(); d++)
        {
            if (!DA.containsKey(b.charAt(d)))
            {
                DA.put(b.charAt(d), 0);
            }
        }
        for (int i = 1; i <= a.length(); i++)
        {
            int DB = 0;
            for (int j = 1; j <= b.length(); j++)
            {
                final int i1 = DA.get(b.charAt(j - 1));
                final int j1 = DB;
                int d = wc;
                if (a.charAt(i - 1) == b.charAt(j - 1))
                {
                    d = 0;
                    DB = j;
                }
                H[i + 1][j + 1] = min(
                    H[i][j] + d,
                    H[i + 1][j] + wi,
                    H[i][j + 1] + wd,
                    H[i1][j1] + ((i - i1 - 1) * wd)
                    + ws + ((j - j1 - 1) * wi));
            }
            DA.put(a.charAt(i - 1), i);
        }
        return H[a.length() + 1][b.length() + 1];
    }

    /**
     * Method used for DemerauLevenshteinDistance.
     */
    private static int min(int a, int b, int c, int d)
    {
        return Math.min(a, Math.min(b, Math.min(c, d)));
    }

    /**
     * Returns the bestMatch for search in strings with maxDistance.
     *
     * @param search      the string to search
     * @param strings     the strings to match to
     * @param maxDistance the max DemerauLevenshteinDistance
     * @return the best match
     */
    public static List<String> getBestMatches(String search, Collection<String> strings, int maxDistance)
    {
        List<String> matches = new LinkedList<String>();
        int searchLength = search.length();
        for (String string : strings)
        {
            if (Math.abs(searchLength - string.length()) > maxDistance)
            {
                continue;
            }
            int distance = getDemerauLevenshteinDistance(search, string);
            if (distance <= maxDistance)
            {
                matches.add(string);
            }
        }
        return matches;
    }

    /**
     * CaseInsensitive StringMatching:
     * First LD-Check 1
     * IndexCheck maxIndex: string.length() maxBehind: 20
     * Second LD-Check 2 maxLength: string.length()
     *
     * @param string     the string to search
     * @param stringlist the possible Strings to find
     * @return a found match or null
     */
    public static String matchString(String string, Collection<String> stringlist)
    {
        return matchString(string, stringlist, true, 1, string.length(), 20, 2, 40, true);
    }

    public static String matchString(String string, String... stringlist)
    {
        return matchString(string, Arrays.asList(stringlist));
    }

    /**
     * CaseInsensitive StringMatching:
     * First LD-Check 1
     * IndexCheck maxIndex: string.length() maxBehind: 20
     * Second LD-Check 2 maxLength: string.length()
     *
     * @param string                 the string to search
     * @param stringlist             the possible Strings to find
     * @param ignoreLdPerLengthOnLD1 whether to ignore ldPerLength on the first LD-Check
     * @return a found match or null
     */
    public static String matchString(String string, Collection<String> stringlist, boolean ignoreLdPerLengthOnLD1)
    {
        return matchString(string, stringlist, true, 1, string.length(), 20, 2, 40, ignoreLdPerLengthOnLD1);
    }

    /**
     * Tries to match a String with - with LD distance.
     * In 3 attemps:
     * First LD-Check: LD-Check with the whole string.
     * Index-Chexk: if searchString is part of a result
     * Second LD-Check: LD-Check with first part of the string
     *
     * @param firstLdCheck        - Index with
     * @param maxIndex            and
     * @param maxbehindIndex      - LD at start with distance
     * @param secondLdCheck       when longer than
     * @param maxLengthforLdCheck
     *
     * @param string
     * @param stringlist
     * @param caseInSensitive
     * @param firstLdCheck
     * @param maxIndex
     * @param maxbehindIndex
     * @param secondLdCheck
     * @param percentLdOfLength
     * @return a matching String
     */
    public static String matchString(String string, Collection<String> stringlist, boolean caseInSensitive, int firstLdCheck, int maxIndex, int maxbehindIndex, int secondLdCheck, int percentLdOfLength, boolean ignoreLdPerLengthOnLD1)
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
                    ld = getDemerauLevenshteinDistance(inList.toLowerCase(Locale.ENGLISH), string);
                }
                else
                {
                    ld = getDemerauLevenshteinDistance(inList, string);
                }
                if (ld <= firstLdCheck) // Match with ld <= 2 and searchString > 4
                {
                    if ((ld * 100 / searchStringLength) <= percentLdOfLength || (ld == 1 && ignoreLdPerLengthOnLD1 && searchStringLength > 1))
                    {
                        CubeEngine.getLogger().log(LogLevel.DEBUG, "LD1: Found " + inList + " for " + searchString + " with LD: " + ld + " and LD/Length: " + (int)ld * 100 / searchStringLength);
                        if (ld < distance)
                        {
                            distance = ld;
                            foundString = inList;
                        }
                    }
                }
            }
        }
        if (foundString == null) // Not Found -> does String contain searchString?
        {
            if (maxIndex >= 0) // Index lower than 0 -> NO CHECK
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
                            CubeEngine.getLogger().log(LogLevel.DEBUG, "Index: Found " + inList + " for " + searchString + " with Index: " + index + " and behindindex: " + (inList.length() - (index + searchStringLength)));
                            indexfound = index;
                            behindindex = inList.length() - (index + searchStringLength);
                            foundString = inList;
                        }
                        if (index == indexfound && inList.length() - (index + searchStringLength) <= behindindex)
                        {
                            CubeEngine.getLogger().log(LogLevel.DEBUG, "Index: Found " + inList + " for " + searchString + " with Index: " + index + " and behindindex: " + (inList.length() - (index + searchStringLength)));
                            behindindex = inList.length() - (index + searchStringLength);
                            foundString = inList;
                        }
                    }
                }
            }
        }
        if (foundString == null) // Not Found -> search for Typo at start
        {
            if (secondLdCheck >= 1) // LD lower than 1 -> NO Check
            {
                distance = secondLdCheck + 1;
                int behindindex = maxbehindIndex;
                for (String inList : stringlist)
                {
                    if (inList.length() >= searchStringLength) // can inList contain searchString?
                    {
                        String subString = inList.substring(0, searchStringLength);
                        if (caseInSensitive)
                        {
                            subString = subString.toLowerCase(Locale.ENGLISH);
                        }
                        ld = getDemerauLevenshteinDistance(subString, searchString);
                        if (ld <= secondLdCheck) // Found light Typo at start of String
                        {
                            if ((ld * 100 / searchStringLength) <= percentLdOfLength || (ld == 1 && ignoreLdPerLengthOnLD1 && searchStringLength > 1))
                            {
                                CubeEngine.getLogger().log(LogLevel.DEBUG, "LD2: Found " + inList + "|" + subString + " for " + searchString + " with LD: " + ld + " and LD/Length: " + (int)ld * 100 / searchStringLength + " and behindindex " + (inList.length() - searchStringLength));
                                if (ld < distance) // Compare to last match
                                {
                                    distance = ld;
                                    foundString = inList;
                                    behindindex = inList.length() - searchStringLength;
                                }
                                if (ld == distance)
                                {
                                    if ((inList.length() - searchStringLength) < behindindex)
                                    {
                                        foundString = inList;
                                        behindindex = inList.length() - searchStringLength;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        CubeEngine.getLogger().log(LogLevel.DEBUG, "Found " + foundString + " for " + searchString);
        return foundString;
    }
}
