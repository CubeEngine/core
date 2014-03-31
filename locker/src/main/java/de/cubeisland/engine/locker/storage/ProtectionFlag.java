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
package de.cubeisland.engine.locker.storage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.cubeisland.engine.core.util.StringUtils;
import de.cubeisland.engine.core.util.matcher.Match;

import static de.cubeisland.engine.core.util.StringUtils.startsWithIgnoreCase;

/**
 * Flags that can be given to a protection.
 * <p>Flags may not be supported by all {@link ProtectedType}
 */
public enum ProtectionFlag
{
    /**
     * Ignore Redstone changes to protected block
     */
    BLOCK_REDSTONE("blockredstone", 1 << 0),
    /**
     * Autoclose doors etc. after a configured time
     */
    AUTOCLOSE("autoclose", 1 << 1),
    /**
     * Allow items to get moved into a chest by an other block
     */
    HOPPER_IN("hopperIn", 1 << 2),
    /**
     * Allow items to get moved out of a chest by a hopper-block
     */
    HOPPER_OUT("hopperOut", 1 << 3),
    /**
     * Allow items to get moved out of a chest by a hopper-minecart
     */
    HOPPER_MINECART_OUT("minecartOut", 1 << 4),
    /**
     * Allows items to get moved into a chest by an entity
     */
    HOPPER_MINECART_IN("minecartIn", 1 << 5),
    /**
     * Notify the owner when accessing
     */
    NOTIFY_ACCESS("notify", 1 << 6)
    ;
    public static final short NONE = 0;

    public final short flagValue;
    public final String flagname;

    private ProtectionFlag(String flagname, int flag)
    {
        this.flagname = flagname;
        this.flagValue = (short)flag;
    }

    private final static Map<String, ProtectionFlag> flags;

    static
    {
        flags = new HashMap<>();
        for (ProtectionFlag protectionFlag : ProtectionFlag.values())
        {
            if (flags.put(protectionFlag.flagname, protectionFlag) != null)
            {
                throw new IllegalArgumentException("Duplicate protection flag!");
            }
        }
    }

    public static List<String> getTabCompleteList(String token, String subToken)
    {
        List<String> previousTokens = Arrays.asList(StringUtils.explode(",", token));
        List<String> matchedFlags = new ArrayList<>();
        for (String flag : flags.keySet())
        {
            if (startsWithIgnoreCase(flag, subToken))
            {
                matchedFlags.add(flag);
            }
        }
        matchedFlags.removeAll(previousTokens); // do not duplicate!
        List<String> result = new ArrayList<>();
        for (String flag : matchedFlags)
        {
            result.add(token + flag.replaceFirst(subToken, ""));
        }
        return result;
    }

    public static ProtectionFlag match(String toMatch)
    {
        String match = Match.string().matchString(toMatch, flags.keySet());
        return flags.get(match);
    }

    public static Set<ProtectionFlag> matchFlags(String param)
    {
        HashSet<ProtectionFlag> result = new HashSet<>();
        if (param == null)
        {
            return result;
        }
        String[] flags = StringUtils.explode(",", param);
        for (String flag : flags)
        {
            ProtectionFlag match = ProtectionFlag.match(flag);
            if (match != null)
            {
                result.add(match);
            }
        }
        return result;
    }

    public static Set<String> getNames()
    {
        return flags.keySet();
    }
}
