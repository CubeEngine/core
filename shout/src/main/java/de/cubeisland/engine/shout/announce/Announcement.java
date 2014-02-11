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
package de.cubeisland.engine.shout.announce;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.bukkit.permissions.Permissible;

import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.i18n.ClonedLanguage;
import de.cubeisland.engine.core.i18n.I18n;
import de.cubeisland.engine.core.i18n.Language;
import de.cubeisland.engine.core.i18n.NormalLanguage;

import de.cubeisland.engine.core.permission.PermDefault;
import de.cubeisland.engine.core.permission.Permission;
import de.cubeisland.engine.shout.Shout;
import gnu.trove.map.hash.THashMap;

/**
 * Class to represent an announcement.
 */
public class Announcement
{
    private final String name;
    private final Permission permission;
    private final List<String> worlds;
    private final Map<Locale, String[]> messages;
    private final long delay;
    private final boolean fixedCycle;

    public Announcement(Announcement acm)
    {
        if (acm == null) throw new IllegalArgumentException("The announcement must not be null!");

        this.name = acm.name;
        this.permission = acm.permission;
        this.worlds = new ArrayList<>(acm.getWorlds());
        this.messages = new THashMap<>(acm.messages);
        this.delay = acm.delay;
        this.fixedCycle = acm.fixedCycle;
    }

    /**
     * Constructor of announcement
     *
     * @param name          This announcements unique name
     * @param permName      This permission node for this announcement
     * @param worlds        The worlds this announcement should be displayed in. Can be just a * to indicate all worlds
     * @param messages      The messages of this announcement
     * @param delay         The delay of the announcement
     */
    public Announcement(Shout module, String name, String permName, List<String> worlds, Map<Locale, String[]> messages, long delay, boolean fixedCycle)
    {
        if (name == null || name.isEmpty()) throw new IllegalArgumentException("The announcement must have a name");
        if (permName == null || permName.isEmpty()) throw new IllegalArgumentException("The announcement must have a permission name");
        if (worlds == null || worlds.isEmpty()) throw new IllegalArgumentException("The announcement must have a world");
        if (messages == null || messages.isEmpty()) throw new IllegalArgumentException("The announcement must have one or more messages");
        if (delay < 0) throw new IllegalArgumentException("The announcement must have a valid delay");

        this.name = name;
        if (!permName.equalsIgnoreCase("*"))
        {
            this.permission = module.getAnnouncePerm().child(permName, PermDefault.FALSE);
            module.getCore().getPermissionManager().registerPermission(module, permission);
        }
        else
        {
            this.permission = null;
        }
        this.worlds = worlds;
        this.messages = messages;
        this.delay = delay;
        this.fixedCycle = fixedCycle;
    }

    /**
     * Get the message from this announcement in a specified language
     *
     * @param   locale  The language to get the message in
     * @return	The message in that language if exist, else the message in the default locale will be returned
     */
    public String[] getMessage(Locale locale)
    {
        if (this.messages.containsKey(locale))
        {
            return messages.get(locale);
        }

        final I18n i18n = CubeEngine.getI18n();
        Language lang = i18n.getLanguage(locale);
        if (lang != null)
        {
            if (lang instanceof NormalLanguage)
            {
                lang = ((NormalLanguage)lang).getParent();
                if (lang != null)
                {
                    return this.messages.get(lang.getLocale());
                }
            }

            if (lang instanceof ClonedLanguage)
            {
                return this.messages.get(((ClonedLanguage)lang).getOriginal().getLocale());
            }
        }

        return this.messages.get(Locale.getDefault());
    }

    /**
     * Get the delay
     *
     * @return The delay in milliseconds
     */
    public long getDelay()
    {
        return this.delay;
    }

    /**
     * Get the permission node for this announcement
     *
     * @return	the permission node for this announcement
     */
    public Permission getPermission()
    {
        return this.permission;
    }

    /**
     * Get the worlds this announcement should be displayed in
     *
     * @return	The worlds this announcement should be displayed in.
     */
    public List<String> getWorlds()
    {
        return this.worlds;
    }

    /**
     * Get this announcements first world
     *
     * @return the first world
     */
    public String getFirstWorld()
    {
        return worlds.get(0);
    }

    public boolean hasWorld(String world)
    {
        return (this.worlds.get(0).equals("*") || this.worlds.contains(world));
    }

    public String getName()
    {
        return this.name;
    }

    public boolean hasFixedCycle()
    {
        return this.fixedCycle;
    }

    public boolean canAccess(Permissible permissible)
    {
        // TODO check the group
        return this.getPermission() == null || this.getPermission().isAuthorized(permissible);
    }
}
