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
package de.cubeisland.engine.core.util.matcher;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.google.common.base.Functions;
import com.google.common.collect.Ordering;
import de.cubeisland.engine.core.CubeEngine;

public class StringMatcher
{
    private final DamerauLevenshteinAlgorithm editDistance = new DamerauLevenshteinAlgorithm(1, 1, 1, 1);

    /**
     * Returns all matches with their editDistance, having an editDistance <= maxDistance
     *
     * @param search the String to search for
     * @param in the Strings to match in
     * @param maxDistance the maximum editDistance
     * @param ignoreCase
     * @return a map of all matches sorted by their editDistance
     */
    public TreeMap<String, Integer> getMatches(String search, Collection<String> in, int maxDistance, boolean ignoreCase)
    {
        if (maxDistance < 1)
        {
            CubeEngine.getLog().warn(new Throwable(), "Checking EditDistance lower than 1!");
            return new TreeMap<>();
        }
        Map<String, Integer> matches = new HashMap<>();
        Ordering<String> comparator = Ordering.natural().onResultOf(Functions.forMap(matches)).compound(Ordering.natural());
        for (String target : in)
        {
            int distance = target.length() - search.length();
            if (distance > maxDistance || -distance > maxDistance) // too long/short to match
            {
                continue;
            }
            if (ignoreCase)
            {
                distance = this.editDistance.executeIgnoreCase(search, target);
            }
            else
            {
                distance = this.editDistance.execute(search, target);
            }
            if (distance <= maxDistance)
            {
                matches.put(target, distance);
            }
        }
        TreeMap<String, Integer> result = new TreeMap<>(comparator);
        result.putAll(matches);
        return result;
    }

    public String matchString(String search, Collection<String> in, boolean ignoreCase,
                              int firstEditDistance, double firstMinPercentCorrect,
                              int maxIndex, double indexMinPercentCorrect,
                              int secondEditDistance, double secondMinPercentCorrect)
    {
        if (search == null || in == null || in.isEmpty())
        {
            return null;
        }
        if (in.contains(search))
        {
            return search; // Direct Match
        }
        Map<String, Integer> firstEditDistanceCheck = this.getMatches(search, in, firstEditDistance, ignoreCase);
        int searchStringLength = search.length();
        if (!firstEditDistanceCheck.isEmpty()) // Nothing found with EditDistance -> Check for starting strings
        {
            for (Map.Entry<String, Integer> entry : firstEditDistanceCheck.entrySet())
            {
                double curPercentage = (entry.getKey().length() - entry.getValue()) * 100 / entry.getKey().length();
                if (curPercentage >= firstMinPercentCorrect)
                {
                    CubeEngine.getLog().debug("1stDist FOUND: {} for {} (D:{}|{}%)",
                                              firstEditDistanceCheck.keySet().iterator().next(), search,
                                              firstEditDistanceCheck.values().iterator().next(), curPercentage);
                    return entry.getKey();
                }
                CubeEngine.getLog().debug("1stDist TO WEAK: {} for {} (D:{}|{}%)",
                                          firstEditDistanceCheck.keySet().iterator().next(), search,
                                          firstEditDistanceCheck.values().iterator().next(), curPercentage);
            }
        }
        String bestMatch = null;

        if (ignoreCase)
        {
            search = search.toLowerCase();
        }
        if (maxIndex >= 0) // Index lower than 0 -> NO CHECK
        {
            int bestIndex = maxIndex;
            int currentIndex;
            double bestPercentCorrect = indexMinPercentCorrect;
            for (String inList : in)
            {
                if (ignoreCase)
                {
                    currentIndex = inList.toLowerCase(Locale.ENGLISH).indexOf(search);
                }
                else
                {
                    currentIndex = inList.indexOf(search);
                }
                if (currentIndex != -1) // Found search in inList
                {
                    double curPercentCorrect = searchStringLength * 100 / inList.length();
                    if (currentIndex < bestIndex && curPercentCorrect >= indexMinPercentCorrect) // Compare to last match
                    {
                        CubeEngine.getLog().debug("Index: FOUND {} for {} (I:{}|{}%)", inList, search, currentIndex,
                                                  curPercentCorrect);
                        bestIndex = currentIndex;
                        bestPercentCorrect = curPercentCorrect;
                        bestMatch = inList;
                    }
                    else if (currentIndex == bestIndex && curPercentCorrect >= bestPercentCorrect)
                    {
                        CubeEngine.getLog().debug("Index: FOUND {} for {} (I:{}|{}%)", inList, search, currentIndex,
                                                  curPercentCorrect);
                        bestPercentCorrect = curPercentCorrect;
                        bestMatch = inList;
                    }
                }
            }
        }
        if (bestMatch != null || secondEditDistance < 1 || searchStringLength < 3) // found OR do not check secondEditDistance OR searchString shorter than 3
        {
            return bestMatch;
        }
        // Still not found -> search for starting strings with typo
        int bestDistance = secondEditDistance + 1;
        double bestPercentCorrect = secondMinPercentCorrect;
        for (String inList : in)
        {
            if (inList.length() >= searchStringLength + secondEditDistance) // can inList contain searchString?
            {
                String subString = inList.substring(0, searchStringLength + secondEditDistance);
                if (ignoreCase)
                {
                    subString = subString.toLowerCase(Locale.ENGLISH);
                }
                int currentDistance = this.editDistance.execute(search, subString);
                double curPercentCorrect = (searchStringLength - currentDistance) * 100 / inList.length();
                if (currentDistance < bestDistance && curPercentCorrect >= secondMinPercentCorrect) // Found light Typo at start of String
                {
                    CubeEngine.getLog().debug("2nDist: Found {}|{} for {} (D:{}|{}%)", inList, subString, search,
                                              currentDistance, (int)curPercentCorrect);
                    bestDistance = currentDistance;
                    bestMatch = inList;
                    bestPercentCorrect = curPercentCorrect;
                }
                else if (currentDistance == bestDistance && bestPercentCorrect <= curPercentCorrect)
                {
                    CubeEngine.getLog().debug("2nDist: Found {}|{} for {} (D:{}|{}%)", inList, subString, search,
                                              currentDistance, (int)curPercentCorrect);
                    bestMatch = inList;
                    bestPercentCorrect = curPercentCorrect;
                }
            }
        }
        return bestMatch;
    }

    /**
     * Gets the best matches with given maxEditDistance ignoring case
     *
     * @param search the string to match
     * @param in the collection to match in
     * @param maxEditDistance the maximum editDistance
     * @return a sorted set of the best matches
     */
    public Set<String> getBestMatches(String search, Collection<String> in, int maxEditDistance)
    {
        return this.getMatches(search, in, maxEditDistance, true).keySet();
    }

    public String matchString(String search, Collection<String> in)
    {
        return this.matchString(search, in, true, 1, 40, 3, 10, 1, 40);
    }

    public String matchString(String search, String... in)
    {
        return this.matchString(search, Arrays.asList(in));
    }
}
