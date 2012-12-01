package de.cubeisland.cubeengine.shout.announce.receiver;

import de.cubeisland.cubeengine.core.util.Pair;
import de.cubeisland.cubeengine.shout.announce.Announcement;
import de.cubeisland.cubeengine.shout.announce.MessageOfTheDay;

import java.util.Queue;

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
     * If the receiver could receive this announcement now
     * For users this would check if they are in the correct world
     *
     * @param   announcement the announcement to check with
     * @return  if receiver can receive this announcement now
     */
    public boolean canReceiver(Announcement announcement);

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
