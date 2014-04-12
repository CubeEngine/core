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

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.cubeisland.engine.core.CubeEngine;

public class McUUID
{
    private final static ObjectMapper mapper = new ObjectMapper();
    private static final String MOJANG_API_URL = "https://api.mojang.com/profiles/page/";
    private static final String AGENT = "minecraft";

    static {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static final Pattern UUID_PATTERN = Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$");

    public static Map<String, UUID> getUUIDForNames(Collection<String> playerNames)
    {
        Map<String, UUID> map = new HashMap<>();
        for (Profile profile : getUUIDForNames0(playerNames))
        {
            try
            {
                map.put(profile.name, getUUIDFromString(profile.id));
            }
            catch (Exception e)
            {
                CubeEngine.getLog().error("Could not convert UUID of: {} ({})", profile.name, profile.id);
            }
        }
        playerNames.removeAll(map.keySet());
        for (String playerName : playerNames)
        {
            CubeEngine.getLog().error("Missing UUID for {}", playerName);
            map.put(playerName, null);
        }
        return map;
    }

    private static UUID getUUIDFromString(String id)
    {
        return UUID.fromString(id.substring(0, 8) + "-" + id.substring(8, 12) + "-" + id.substring(12, 16) + "-" + id.substring(16, 20) + "-" +id.substring(20, 32));
    }

    private static List<Profile> getUUIDForNames0(Collection<String> playernames)
    {
        LinkedList<String> players = new LinkedList<>();
        List<Profile> profiles = new ArrayList<>();
        for (String playername : new HashSet<>(playernames))
        {
            players.add(playername);
            if (players.size() >= 20)
            {
                getProfiles(profiles, players);
                CubeEngine.getLog().info(profiles.size() + "/" + playernames.size());
            }
        }
        getProfiles(profiles, players);
        CubeEngine.getLog().info("Getting UUIDs done!", playernames.size());
        return profiles;
    }

    private static void getProfiles(List<Profile> profiles, LinkedList<String> players)
    {
        int amount = players.size();
        CubeEngine.getLog().debug("Query UUID for: " + StringUtils.implode(",", players));
        ArrayNode node = mapper.createArrayNode();
        while (!players.isEmpty())
        {
            ObjectNode criteria = mapper.createObjectNode();
            criteria.put("name", players.poll());
            criteria.put("agent", AGENT);
            node.add(criteria);
        }
        int page = 1;
        try
        {
            CubeEngine.getLog().info("Query Mojang for {} UUIDs", amount);
            while (amount > 0)
            {
                int read = readProfilesFromInputStream(postQuery(node, page++).getInputStream(), profiles);
                if (read == 0)
                {
                    CubeEngine.getLog().info("No Answer for {} players", amount);
                }
                else if (read != amount)
                {
                    amount -= read;
                    continue;
                }
                return;
            }
        }
        catch (IOException e)
        {
            CubeEngine.getLog().error(e, "Could not retrieve UUID for given names!");
        }
    }

    private static HttpURLConnection postQuery(ArrayNode node, int page) throws IOException
    {
        HttpURLConnection con = (HttpURLConnection)new URL(MOJANG_API_URL + page).openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setUseCaches(false);
        con.setDoInput(true);
        con.setDoOutput(true);
        DataOutputStream writer = new DataOutputStream(con.getOutputStream());
        writer.write(node.toString().getBytes());
        writer.close();
        return con;
    }

    private static int readProfilesFromInputStream(InputStream is, List<Profile> profiles) throws IOException
    {
        ProfileSearchResult results = mapper.readValue(is, ProfileSearchResult.class);
        profiles.addAll(Arrays.asList(results.profiles));
        return results.size;
    }

    public static UUID getUUIDForName(String player)
    {
        Map<String, UUID> uuidForNames = getUUIDForNames(Arrays.asList(player));
        return uuidForNames.get(player);
    }

    public static class Profile
    {
        public String id;
        public String name;
    }

    public static class ProfileSearchResult
    {
        public Profile[] profiles;
        public int size;
    }
}
