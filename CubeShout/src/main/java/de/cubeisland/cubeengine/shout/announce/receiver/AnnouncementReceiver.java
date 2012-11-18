package de.cubeisland.cubeengine.shout.announce.receiver;

import de.cubeisland.cubeengine.core.util.Pair;
import de.cubeisland.cubeengine.shout.announce.Announcement;

import java.util.Queue;

public interface AnnouncementReceiver
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
    public void sendMessage(String message);

    /**
     * Get the world this receiver is in.
     * If this receiver is some kind of bot(IRC) this should be the channel or server.
     *
     * @return The world the receiver is in or channel/server if it is a bot
     */
    public String getWorld();

    /**
     * Get the next announcement and delay for this receiver
     * NOTE: the delay is not in ms, but in this receivers execution format
     *
     * @return The next announcement and delay for this user
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
    public String getLanguage();

    /**
     * Override and set all this users announcements.
     * This will delete all existing announcements for this receiver
     *
     * @param announcements The new announcements for this receiver
     */
    public void setAllAnnouncements(Queue<Announcement> announcements);

    /**
     * Set this receivers current world
     * UserReceivers are not affected by this.
     * @param world
     */
    public void setWorld(String world);

    /**
     * Check if this receiver has this announcement
     *
     * @param   permission to check for
     * @return  if receiver has this announcement
     */
    public boolean hasPermission(String permission);
}
