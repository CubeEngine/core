package de.cubeisland.cubeengine.core.util.matcher;

import com.google.common.base.Functions;
import com.google.common.collect.Ordering;
import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.util.log.LogLevel;
import gnu.trove.map.hash.THashMap;

import java.util.*;

public class StringMatcher
{
    private DamerauLevenshteinAlgorithm editDistance;

    public StringMatcher() {
        this.editDistance = new DamerauLevenshteinAlgorithm(1,1,1,1);
    }

    /**
     * Returns all matches with their editDistance, having an editDistance <= maxDistance
     *
     * @param search the String to search for
     * @param in the Strings to match in
     * @param maxDistance the maximum editDistance
     * @param ignoreCase
     * @return a map of all matches sorted by their editDistance
     */
    public TreeMap<String,Integer> getMatches(String search, Collection<String> in, int maxDistance, boolean ignoreCase)
    {
        if (maxDistance < 1)
        {
            CubeEngine.getLogger().log(LogLevel.WARNING,"Checking EditDistance lower than 1!",new Throwable());
            return new TreeMap<String, Integer>();
        }
        THashMap<String,Integer> matches = new THashMap<String, Integer>();
        Ordering comparator = Ordering.natural().onResultOf(Functions.forMap(matches)).compound(Ordering.natural());
        for (String target : in)
        {
            int distance = target.length() - search.length();
            if (distance > maxDistance || -distance > maxDistance) // too long/short to match
            {
                continue;
            }
            if (ignoreCase)
            {
                distance = this.editDistance.executeIgnoreCase(search,target);
            }
            else
            {
                distance = this.editDistance.execute(search,target);
            }
            if (distance <= maxDistance)
            {
                matches.put(target,distance);
            }
        }
        TreeMap<String, Integer> result = new TreeMap<String, Integer>(comparator);
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
            CubeEngine.getLogger().log(LogLevel.DEBUG,"Direct Match FOUND: "+ search);
            return search; // Direct Match
        }
        Map<String,Integer> firstEditDistanceCheck = this.getMatches(search,in,firstEditDistance,ignoreCase);
        int searchStringLength = search.length();
        if (!firstEditDistanceCheck.isEmpty()) // Nothing found with EditDistance -> Check for starting strings
        {
            for (Map.Entry<String,Integer> entry : firstEditDistanceCheck.entrySet())
            {
                double curPercentage = (entry.getKey().length() - entry.getValue()) * 100 / entry.getKey().length();
                if (curPercentage >= firstMinPercentCorrect)
                {
                    CubeEngine.getLogger().log(LogLevel.DEBUG,"1stDist FOUND: "+firstEditDistanceCheck.keySet().iterator().next()+
                            " for "+ search +" (D:"+firstEditDistanceCheck.values().iterator().next()+"|"+ curPercentage+"%)");
                    return entry.getKey();
                }
                CubeEngine.getLogger().log(LogLevel.DEBUG,"1stDist TO WEAK: "+firstEditDistanceCheck.keySet().iterator().next()+
                        " for "+ search +" (D:"+firstEditDistanceCheck.values().iterator().next()+"|"+ curPercentage+"%)");
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
                        CubeEngine.getLogger().log(LogLevel.DEBUG, "Index: FOUND " + inList + " for " + search + " (I:" + currentIndex + "|" + (int)curPercentCorrect+"%)");
                        bestIndex = currentIndex;
                        bestPercentCorrect = curPercentCorrect;
                        bestMatch = inList;
                    }
                    else if (currentIndex == bestIndex && curPercentCorrect >= bestPercentCorrect)
                    {
                        CubeEngine.getLogger().log(LogLevel.DEBUG, "Index: FOUND " + inList + " for " + search + " (I:" + currentIndex + "|" + (int)curPercentCorrect+"%)");
                        bestPercentCorrect = curPercentCorrect;
                        bestMatch = inList;
                    }
                }
            }
        }
        if (bestMatch != null || secondEditDistance < 1|| searchStringLength < 3) // found OR do not check secondEditDistance OR searchString shorter than 3
        {
            return bestMatch;
        }
        // Still not found -> search for starting strings with typo
        int bestDistance = secondEditDistance + 1;
        double bestPercentCorrect = secondMinPercentCorrect;
        for (String inList : in)
        {
            if (inList.length() >= searchStringLength+secondEditDistance) // can inList contain searchString?
            {
                String subString = inList.substring(0, searchStringLength+secondEditDistance);
                if (ignoreCase)
                {
                    subString = subString.toLowerCase(Locale.ENGLISH);
                }
                int currentDistance = this.editDistance.execute(search, subString);
                double curPercentCorrect = (searchStringLength-currentDistance) * 100 / inList.length();
                if (currentDistance < bestDistance && curPercentCorrect>=secondMinPercentCorrect) // Found light Typo at start of String
                {
                    CubeEngine.getLogger().log(LogLevel.DEBUG, "2nDist: Found " + inList + "|" + subString + " for " + search
                            + " (D:" + currentDistance + "|" + (int)curPercentCorrect+"%)");
                    bestDistance = currentDistance;
                    bestMatch = inList;
                    bestPercentCorrect = curPercentCorrect;
                }
                else if (currentDistance == bestDistance && bestPercentCorrect <= curPercentCorrect)
                {
                    CubeEngine.getLogger().log(LogLevel.DEBUG, "2nDist: Found " + inList + "|" + subString + " for " + search
                            + " (D:" + currentDistance + "|" + (int)curPercentCorrect+"%)");
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
        return this.matchString(search,in,true,1,40,3,10,1,40);
    }

    public String matchString(String search, String... in)
    {
        return this.matchString(search, Arrays.asList(in));
    }
}
