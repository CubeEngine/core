package de.cubeisland.cubeengine.core.util.matcher;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.util.log.LogLevel;

import java.util.*;

public class StringMatcher {


    // TODO what is this used for??
    static final int wd = 1, wi = 1, wc = 1, ws = 1;

    /**
     * Computes the Demerau-LevenshteinDistance.
     * (Taken from http://qqqqx.blogspot.de/2011/09/dameraulevenshtein-distance.html)
     *
     * @param a the first string
     * @param b the second string
     * @return the Demerau-Levenshtein-Distance between a and b.
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
     * Method used for Demerau-Levenshtein-Distance.
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
     * @param maxDistance the max Demerau-Levenshtein-Distance
     * @return the best match
     */
    public List<String> getBestMatches(String search, Collection<String> strings, int maxDistance)
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
    public String matchString(String string, Collection<String> stringlist)
    {
        return matchString(string, stringlist, true, 1, string.length(), 20, 2, 40, true);
    }

    public String matchString(String string, String... stringlist)
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
    public String matchString(String string, Collection<String> stringlist, boolean ignoreLdPerLengthOnLD1)
    {
        return matchString(string, stringlist, true, 1, string.length(), 20, 2, 40, ignoreLdPerLengthOnLD1);
    }

    /**
     * Tries to match a String with - with LD distance.
     * In 3 attempts:
     * First LD-Check: LD-Check with the whole string.
     * Index-Check: if searchString is part of a result
     * Second LD-Check: LD-Check with first part of the string
     *
     * @param firstLdCheck        - Index with
     * @param maxIndex            and
     * @param maxBehindIndex      - LD at start with distance
     * @param secondLdCheck       when longer than
     * @return a matching String
     */
    public String matchString(String string, Collection<String> stringList, boolean caseInSensitive, int firstLdCheck, int maxIndex, int maxBehindIndex, int secondLdCheck, int percentLdOfLength, boolean ignoreLdPerLengthOnLD1)
    {
        if (stringList == null || string == null || stringList.isEmpty())
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
        if (stringList.contains(string))
        {
            return string; // Direct Match
        }
        if (firstLdCheck >= 1) // LD lower than 1 -> NO Check
        {
            distance = firstLdCheck + 1;
            for (String inList : stringList)
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
                int indexFound = maxIndex;
                int index;
                int behindIndex = maxBehindIndex;
                for (String inList : stringList)
                {
                    if (caseInSensitive)
                    {
                        index = inList.toLowerCase(Locale.ENGLISH).indexOf(searchString);
                    }
                    else
                    {
                        index = inList.indexOf(searchString);
                    }
                    if (index != -1) // Found searchString in inList
                    {
                        if (index < indexFound) // Compare to last match
                        {
                            CubeEngine.getLogger().log(LogLevel.DEBUG, "Index: Found " + inList + " for " + searchString + " with Index: " + index + " and behindindex: " + (inList.length() - (index + searchStringLength)));
                            indexFound = index;
                            behindIndex = inList.length() - (index + searchStringLength);
                            foundString = inList;
                        }
                        if (index == indexFound && inList.length() - (index + searchStringLength) <= behindIndex)
                        {
                            CubeEngine.getLogger().log(LogLevel.DEBUG, "Index: Found " + inList + " for " + searchString + " with Index: " + index + " and behindindex: " + (inList.length() - (index + searchStringLength)));
                            behindIndex = inList.length() - (index + searchStringLength);
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
                int behindIndex = maxBehindIndex;
                for (String inList : stringList)
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
                                    behindIndex = inList.length() - searchStringLength;
                                }
                                if (ld == distance)
                                {
                                    if ((inList.length() - searchStringLength) < behindIndex)
                                    {
                                        foundString = inList;
                                        behindIndex = inList.length() - searchStringLength;
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
