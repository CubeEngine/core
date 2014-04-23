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
package de.cubeisland.engine.shout.announce.receiver;

import java.util.Locale;
import java.util.Queue;

import de.cubeisland.engine.core.util.Pair;
import de.cubeisland.engine.shout.announce.Announcement;
import de.cubeisland.engine.shout.announce.MessageOfTheDay;

/**
 * A class that represents a receiver of announcements.
 * This class only handles the order of the announcements, and the delay after each,
 * but does not handle the timing.
 */
public interface Receiver
{
    /**
     * Get the unique name of this receiver
     *
     * @return  the name
     */
    public String getName();

    /**
     * Send a message to this receiver
     *
     * @param message the message to send
     */
    public void sendMessage(String[] message);

    /**
     * Get the next announcement and delay for this receiver
     * NOTE: the delay is not in ms, but in executions of the receivers
     * announcer.
     *
     * @return The next announcement and delay for this receiver
     */
    public Pair<Announcement, Integer> getNextDelayAndAnnouncement();

    /**
     * Get all announcements this receiver could receive
     * This is not effected by the world the receiver is in
     *
     * @return  all the announcements this receiver could receive
     */
    public Queue<Announcement> getAllAnnouncements();

    /**
     * Get the locale code/language of this receiver
     *
     * @return this receivers local code
     */
    public Locale getLocale();

    /**
     * Override and set all this users announcements.
     * This will delete all existing announcements for this receiver
     *
     * @param announcements The new announcements for this receiver
     */
    public void setAllAnnouncements(Queue<Announcement> announcements);

    /**
     * Add a new announcement that this receiver should receive
     * @param announcement
     */
    public void addAnnouncement(Announcement announcement);

    /**
     * If the receiver could receive this announcement now
     * For users this would check if they are in the correct world
     *
     * @param   announcement the announcement to check with
     * @return  if receiver can receive this announcement now
     */
    public boolean canReceive(Announcement announcement);

    /**
     * If the receiver under a specific circumstance could receive an announcement
     * For users this is almost equivalent to hasPermission
     *
     * @param   announcement The announcement to check with
     * @return  if the receiver could receive this announcement
     */
    public boolean couldReceive(Announcement announcement);

    public void setMOTD(MessageOfTheDay motd);
}
