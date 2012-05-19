package de.cubeisland.cubeengine.core.util;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * This class contains some utillities to work with Strings
 *
 * @author Phillip Schichtel
 * @author Faithcaio
 */
public final class StringUtils
{
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
     * @param inputArray the array to merge
     * @return a String containing the inputArray delimited by delim
     */
    public static String implode(String delim, String[] inputArray)
    {
        return implode(delim, inputArray, true);
    }
    
    /**
     * This method merges an array of strings to a single string
     * 
     * @param delim the delimiter
     * @param inputArray the array to merge
     * @param keepEmptyParts whether to keep empty parts
     * @return a String containing the inputArray delimited by delim
     */
    public static String implode(String delim, String[] inputArray, boolean keepEmptyParts)
    {
        int max = inputArray.length;
        if (max == 0) return "";
        String output = inputArray[0];
        for (int i=1; i<max; ++i)
        {
            if ((inputArray[i].length() > 0) || (keepEmptyParts))
            {
                output += delim;
                output += inputArray[i];
            }
        }
        return output;
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
                if ((offset = token.indexOf("=")) > 0)
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
     * For example:
     * "an+example%20string" -> "an example string"
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
        catch (Exception e)
        {
            return string;
        }
    }
}
