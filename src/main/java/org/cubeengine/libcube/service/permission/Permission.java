/*
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
package org.cubeengine.libcube.service.permission;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import org.cubeengine.libcube.service.i18n.I18n;
import org.cubeengine.libcube.service.i18n.formatter.MessageType;
import org.spongepowered.api.service.permission.Subject;

import java.util.Collections;
import java.util.Set;

public class Permission
{
    private final String id;
    private final String desc;
    private final Set<String> explicitParents;

    public Permission(String id, String desc, Set<String> explicitParents)
    {
        this.id = id;
        this.desc = desc;
        this.explicitParents = Collections.unmodifiableSet(explicitParents);
    }

    public String getId()
    {
        return id;
    }

    public String getDesc()
    {
        return desc;
    }

    public Set<String> getExplicitParents()
    {
        return explicitParents;
    }

    /**
     * Checks a permission
     *
     * @param subject the subject
     *
     * @return true if the subject has this permission.
     */
    public boolean check(Subject subject)
    {
        return subject.hasPermission(this.getId());
    }

    /**
     * Checks a permission and informs if the permission is not set.
     *
     * @param checkOn the Subject to check the permission and send message to
     * @param i18n the I18n Service
     * @param <T> MessageReceiver and Subject Type
     *
     * @return true if the subject has this permission.
     */
    public <T extends Audience & Subject> boolean check(T checkOn, I18n i18n)
    {
        boolean hasPerm = checkOn.hasPermission(this.getId());
        if (!hasPerm)
        {
            Component perm = TextComponent.of(this.id).hoverEvent(HoverEvent.showText(TextComponent.of(this.desc)));
            i18n.send(checkOn, MessageType.NEGATIVE, "You are missing the permission {txt}.", perm);
        }
        return hasPerm;
    }

    @Override
    public String toString()
    {
        return getId() + ": " + getDesc();
    }
}
