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
package de.cubeisland.cubeengine.shout.announce.receiver;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.ChatFormat;
import de.cubeisland.cubeengine.shout.announce.Announcement;
import de.cubeisland.cubeengine.shout.announce.AnnouncementManager;

import java.util.Locale;
import java.util.Queue;

public class UserReceiver extends AbstractReceiver
{
    private final User user;
    private Queue<Announcement> announcements;

    public UserReceiver(User user, AnnouncementManager announcementManager)
    {
        super(announcementManager);
        this.user = user;
    }

    @Override
    public String getName()
    {
        return this.user.getName();
    }

    @Override
    public void sendMessage(String[] message)
    {
        for (String line : message)
        {
            user.sendMessage(ChatFormat.parseFormats(line));
        }
    }

    @Override
    public Locale getLocale()
    {
        return this.user.getLocale();
    }

    @Override
    public boolean canReceiver(Announcement announcement)
    {
        return announcement.hasWorld(this.user.getWorld().getName());
    }

    @Override
    public boolean couldReceive(Announcement announcement)
    {
        return announcement.getPermNode().equals("*") || this.user.hasPermission(announcement.getPermNode());
    }
}
