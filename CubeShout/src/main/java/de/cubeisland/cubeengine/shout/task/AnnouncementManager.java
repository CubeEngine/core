package de.cubeisland.cubeengine.shout.task;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import org.bukkit.entity.Player;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.filesystem.FileExtentionFilter;
import de.cubeisland.cubeengine.core.filesystem.FileUtil;
import de.cubeisland.cubeengine.core.i18n.I18n;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.shout.Shout;
import de.cubeisland.cubeengine.shout.ShoutException;

/*
 * Class to manage all the announcements and their receivers
 */
public class AnnouncementManager
{
    private Shout module;
    private Announcer taskManager;
    private Map<String, AnnouncementReceiver> receivers;
    private Map<String, Announcement> announcements;

    public AnnouncementManager(Shout module)
    {
        this.module = module;
        this.taskManager = module.getTaskManager();
        this.receivers = new ConcurrentHashMap<String, AnnouncementReceiver>();
        this.announcements = new HashMap<String, Announcement>();
    }

    /**
     * Get all the announcements this user should receive.
     * 
     * @param	user	The user to get announcements of.
     * @return			A list of all announcements that should be displayed to this user.
     */
    public List<Announcement> getAnnouncemets(String user)
    {

        Announcement[] announcementArray = new Announcement[announcements.size()];
        receivers.get(user).messages.toArray(announcementArray);
        return Arrays.asList(announcementArray);
    }

    /**
     * Get all the announcements
     * 
     * @return All announcements
     */
    public Collection<Announcement> getAnnouncemets()
    {
        return this.announcements.values();
    }

    /**
     * Get announcement by name
     * 
     * @param 	name	Name of the announcement
     * @return	The announcements with this name, or null if not exist
     */
    public Announcement getAnnouncement(String name)
    {
        return this.announcements.get(name);
    }

    /**
     * Check if this announcement exist
     * 
     * @param 	name	Name of the announcement to check
     * @return	true or false
     */
    public boolean hasAnnouncement(String name)
    {
        return this.announcements.containsKey(name);
    }

    /**
     * Get the greatest common divisor of the delays form the announcements this user should receive.
     *  
     * @param 	user	The user to get the gcd of their announcements.
     * @return			The gcd of the users announcements.
     */
    public long getGreatestCommonDivisor(String user)
    {
        List<Announcement> announcements = this.getAnnouncemets(user);
        long[] delays = new long[announcements.size()];
        for (int x = 0; x < delays.length; x++)
        {
            delays[x] = announcements.get(x).getDelay();
        }
        return greatestCommonDivisor(delays);
    }

    /**
     * Get the greatest common divisor of a list of integers.
     *  
     * @param	ints	The list to get the gcd from.
     * @return			gcd of all the integers in the list.
     */
    private long greatestCommonDivisor(long[] ints)
    {
        long result = ints[0];

        for (int x = 1; x < ints.length; x++)
        {
            while (ints[x] > 0)
            {
                long t = ints[x];
                ints[x] = result % ints[x];
                result = t;
            }
        }
        return result;
    }

    /**
     * Get next message that should be displayed to this user.
     * 
     * @param	user	User to get the next message of.
     * @return			The next message that should be displayed to the user.
     */
    public String getNextMessage(String user)
    {
        User us = module.getUserManager().getUser(user, false);
        Announcement announcement = null;
        boolean used = false;
        //Skip all announcements that don't apply to this world.
        while (!used)
        {
            if (receivers.get(user).messages.element().hasWorld(receivers.get(user).world))
            {
                announcement = receivers.get(user).messages.poll();
                receivers.get(user).messages.add(announcement);
                used = true;
            }
        }
        if (announcement == null)
        {
            return null;
        }
        return announcement.getMessage(us.getLanguage());
    }

    /**
     * Get the next delay for this users MessageTask
     * @param	user	The user to get the next delay of.
     * @return			The next delay that should be used for this users MessageTask in milliseconds.
     * @see		MessageTask
     */
    public int getNextDelay(String user)
    {
        Announcement announcement = null;
        boolean used = false;

        //Skip all announcements that don't apply to the users current world.
        while (!used)
        {
            if (receivers.get(user).messages.element().hasWorld(receivers.get(user).world))
            {
                announcement = receivers.get(user).messages.poll();
                receivers.get(user).messages.add(announcement);
                used = true;
            }
        }
        if (announcement == null)
        {
            return 0;
        }
        return (int)(announcement.getDelay() / getGreatestCommonDivisor(user));
    }

    /**
     * Adds an announcement.
     * Most be done before ay player joins!
     * 
     * @param messages
     * @param world
     * @param delay
     * @param permNode
     * @param group
     * @throws ShoutException if there is something wrong with the values
     */
    public void addAnnouncement(String name, Map<String, String> messages, String world, long delay,
        String permNode, String group) throws ShoutException
    {
        try
        {
            Announcement.validate(name, permNode, module.getCore().getConfiguration().defaultLanguage,
                world, messages, delay);
            Announcement announcement = new Announcement(name, module.getCore().getConfiguration().defaultLanguage,
                permNode, world, messages, delay);
            this.addAnnouncement(announcement);
        }
        catch (IllegalArgumentException ex)
        {
            throw new ShoutException("The announcement was not valid", ex);
        }
    }

    public void addAnnouncement(Announcement announcement)
    {
        this.announcements.put(announcement.getName(), announcement);
    }

    /**
     * initialize this users announcements
     * 
     * @param user	The user
     */
    public void initializeUser(User user)
    {
        String name = user.getName();
        Queue<Announcement> messages = new LinkedList<Announcement>();
        String world = user.getWorld().getName();

        // Load what announcements should be displayed to the user
        for (Announcement a : announcements.values())
        {
            if (a.getPermNode().equals("*") || user.hasPermission(a.getPermNode()))// TODO CubeRoles
            {
                messages.add(a);
            }

        }

        this.receivers.put(name, new AnnouncementReceiver(messages, world));
    }

    /**
     * Set the world for the user
     * 
     * @param 	user	The user
     * @param 	world	The new world
     */
    public void setWorld(String user, String world)
    {
        receivers.get(user).world = world;
    }

    /**
     * Clean all stored information of that user
     * 
     * @param 	user	the user to clean
     */
    public void clean(String user)
    {
        this.receivers.remove(user);
        this.taskManager.stopUser(user);
    }

    /**
     * Clean all loaded announcements and users
     */
    public void reload()
    {
        for (String s : receivers.keySet())
        {
            this.clean(s);
        }

        this.receivers = new ConcurrentHashMap<String, AnnouncementReceiver>();
        this.announcements = new HashMap<String, Announcement>();

        this.loadAnnouncements(module.announcementFolder);

        for (Player p : module.getCore().getServer().getOnlinePlayers())
        {
            User u = module.getUserManager().getUser(p.getName(), false);

            this.initializeUser(u);
            taskManager.scheduleTask(u.getName(), new MessageTask(this, taskManager, u),
                this.getGreatestCommonDivisor(u.getName()));
        }
    }

    /**
     * Load announcements
     * 
     * @param	announcementFolder	The folder to load the announcements from
     */
    public void loadAnnouncements(File announcementFolder)
    {
        List<File> announcements = new ArrayList<File>();
        announcements = Arrays.asList(announcementFolder.listFiles());

        for (File f : announcements)
        {
            if (f.isDirectory())
            {
                if (module.getCore().isDebug())
                {
                    module.logger.log(Level.INFO, "Loading announcement " + f.getName());
                }
                try
                {
                    this.loadAnnouncement(f);
                }
                catch (ShoutException e)
                {
                    module.logger.log(Level.WARNING, "There was an error loading the announcement: "
                        + f.getName());
                    module.logger.log(Level.WARNING, "The error message was: " + e.getLocalizedMessage());
                    if (module.getCore().isDebug())
                    {
                        module.logger.log(Level.SEVERE, null, e);
                    }
                }
            }
        }

    }

    /**
     * Load an specific announcement
     * 
     * @param 	f				the folder to load the announcement from
     * @throws 	ShoutException	if folder is not an folder or don't contain required information
     */
    private void loadAnnouncement(File f) throws ShoutException
    {
        if (f.isFile())
        {
            throw new ShoutException("Tried to load an announcement that was a file!");
        }

        File confFile = new File(f, "meta.yml");
        if (!confFile.exists())
        {
            File config = f.listFiles((FilenameFilter)FileExtentionFilter.YAML)[0];
            if (config != null)
            {
                if (!config.renameTo(confFile))
                {
                    throw new ShoutException("No configfile to announcement: " + f.getName());
                }
            }
            else
            {
                throw new ShoutException("No configfile to announcement: " + f.getName());   
            }
        }

        Map<String, String> messages = new HashMap<String, String>();
        String world = "*";
        long delay = 0;
        String permNode = "*";
        String group = "*";

        AnnouncementConfiguration conf = Configuration.load(AnnouncementConfiguration.class, confFile);
        world = conf.world == null ? world : conf.world;
        permNode = conf.permNode == null ? permNode : conf.permNode;
        group = conf.group == null ? group : conf.group;
        try
        {
            delay = parseDelay(conf.delay);
        }
        catch (IllegalArgumentException ex)
        {
            throw new ShoutException("The delay was not valid", ex);
        }

        List<File> languages = new ArrayList<File>();
        languages = Arrays.asList(f.listFiles((FilenameFilter)new FileExtentionFilter("txt")));

        for (File lang : languages)
        {
            StringBuilder message = new StringBuilder();
            for (String line : FileUtil.readStringList(lang))
            {
                message.append(line).append("\n");
            }
            messages.put(I18n.normalizeLanguage(lang.getName().replace(".txt", "")), message.toString());
        }

        if (module.getCore().isDebug())
        {
            module.logger.log(Level.INFO, "Languages: " + messages.keySet().toString());
            module.logger.log(Level.INFO, "World: " + world);
            module.logger.log(Level.INFO, "Delay(in millisecounds): " + delay);
            module.logger.log(Level.INFO, "Permission: " + permNode);
            module.logger.log(Level.INFO, "Group: " + group);
        }
        try
        {
            this.addAnnouncement(f.getName(), messages, world, delay, permNode, group);
        }
        catch (IllegalArgumentException ex)
        {
            throw new ShoutException("The delay was not valid", ex);
        }
    }

    /**
     * parse a delay in this format:
     * 	10 minutes
     * to
     * 	600 000 ms
     * 
     * @param delayText	the text to parse
     * @return the delay in ticks
     * @throws IllegalArgumentException if the delay was not in a valid format
     */
    public long parseDelay(String delayText) throws IllegalArgumentException
    {
        String[] parts = delayText.split(" ", 2);
        if (parts.length != 2)
        {
            throw new IllegalArgumentException("Not valid delay string");
        }
        int tmpdelay = Integer.parseInt(parts[0]);
        String unit = parts[1].toLowerCase();
        if (unit.equalsIgnoreCase("secounds") || unit.equalsIgnoreCase("secound"))
        {
            return tmpdelay * 1000;
        }
        else if (unit.equalsIgnoreCase("minutes") || unit.equalsIgnoreCase("minute"))
        {
            return tmpdelay * 60 * 1000;
        }
        else if (unit.equalsIgnoreCase("hours") || unit.equalsIgnoreCase("hour"))
        {
            return tmpdelay * 60 * 60 * 1000;
        }
        else if (unit.equalsIgnoreCase("days") || unit.equalsIgnoreCase("day"))
        {
            return tmpdelay * 24 * 60 * 60 * 1000;
        }
        return 0;
    }

    /**
     * Create an announcement folder structure with the params specified.
     * This will not load the announcement into the plugin
     * 
     * @param name
     * @param message
     * @param delay
     * @param world
     * @param group
     * @param permNode
     * @param locale 
     */
    public void createAnnouncement(String name, String message, String delay, String world, String group,
        String permNode, String locale) throws IOException, IllegalArgumentException
    {
        Map<String, String> messages = new HashMap<String, String>();
        messages.put(locale, message);
        Announcement.validate(name, locale, permNode, world, messages, parseDelay(delay));

        File folder = new File(module.announcementFolder, name);
        folder.mkdirs();
        File configFile = new File(folder, "meta.yml");
        configFile.createNewFile();
        File language = new File(folder, locale + ".txt");
        language.createNewFile();

        AnnouncementConfiguration config = new AnnouncementConfiguration();
        config.setCodec("yml");
        config.setFile(configFile);
        config.delay = delay;
        config.world = world;
        config.permNode = permNode;
        config.group = group;
        config.save();

        BufferedWriter bw = new BufferedWriter(new FileWriter(language));
        bw.write(message);
        bw.close();
    }
    
    /**
     * Class to reperesent someone receiving announcements
     */
    private class AnnouncementReceiver
    {
        public Queue<Announcement> messages;
        public String world;

        public AnnouncementReceiver(Queue<Announcement> messages, String world)
        {
            this.messages = messages;
            this.world = world;
        }
    }
}
