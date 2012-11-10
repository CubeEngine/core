package de.cubeisland.cubeengine.shout.announce;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.filesystem.FileExtentionFilter;
import de.cubeisland.cubeengine.core.filesystem.FileUtil;
import de.cubeisland.cubeengine.core.i18n.I18n;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.shout.Shout;
import de.cubeisland.cubeengine.shout.ShoutException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import org.bukkit.entity.Player;

/**
 * Class to manage all the announcements and their receivers
 */
public class AnnouncementManager
{
    private Shout module;
    private Announcer taskManager;
    private Map<String, AnnouncementReceiver> receivers;
    private Map<String, Announcement> announcements;
    private File announcementFolder;

    public AnnouncementManager(Shout module, File announcementFolder)
    {
        this.module = module;
        this.taskManager = module.getTaskManager();
        this.receivers = new ConcurrentHashMap<String, AnnouncementReceiver>();
        this.announcements = new HashMap<String, Announcement>();
        this.announcementFolder = announcementFolder;
    }

    /**
     * Get all the announcements this user should receive.
     *
     * @param	user	The user to get announcements of.
     * @return	A list of all announcements that should be displayed to this
     *         user.
     */
    public List<Announcement> getAnnouncemets(String user)
    {
        return new ArrayList<Announcement>(receivers.get(user).messages);
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
     * @param name	Name of the announcement
     * @return	The announcements with this name, or null if not exist
     */
    public Announcement getAnnouncement(String name)
    {
        return this.announcements.get(name);
    }

    /**
     * Check if this announcement exist
     *
     * @param name	Name of the announcement to check
     * @return	true or false
     */
    public boolean hasAnnouncement(String name)
    {
        return this.announcements.containsKey(name);
    }

    /**
     * Get the greatest common divisor of the delays form the announcements this
     * user should receive.
     *
     * @param user	The user to get the gcd of their announcements.
     * @return	The gcd of the users announcements.
     */
    public long getGreatestCommonDivisor(String user)
    {
        List<Announcement> tmpAnnouncements = this.getAnnouncemets(user);
        long[] delays = new long[tmpAnnouncements.size()];
        for (int x = 0; x < delays.length; x++)
        {
            delays[x] = tmpAnnouncements.get(x).getDelay();
        }
        return greatestCommonDivisor(delays);
    }

    /**
     * Get the greatest common divisor of a list of integers.
     *
     * @param	ints	The list to get the gcd from.
     * @return	gcd of all the integers in the list.
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
     * @return	The next message that should be displayed to the user.
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
     *
     * @param	user	The user to get the next delay of.
     * @return	The next delay that should be used for this users MessageTask in
     *         milliseconds.
     * @see	MessageTask
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
     * @param user	 The user
     * @param world	The new world
     */
    public void setWorld(String user, String world)
    {
        receivers.get(user).world = world;
    }

    /**
     * Clean all stored information of that user
     *
     * @param user	the user to clean
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

        this.loadAnnouncements(this.announcementFolder);
        this.initUsers();
    }
    
    public void initUsers()
    {
        for (User user : module.getUserManager().getOnlineUsers())
        {
            this.initializeUser(user);
            taskManager.scheduleTask(user.getName(), new MessageTask(this, module.getTaskManger(), user),
                this.getGreatestCommonDivisor(user.getName()));
        }
    }

    /**
     * Load announcements
     *
     * @param	announcementFolder	The folder to load the announcements from
     */
    public void loadAnnouncements(File announcementFolder)
    {
        List<File> announcementFiles = Arrays.asList(announcementFolder.listFiles());

        for (File f : announcementFiles)
        {
            if (f.isDirectory())
            {
                if (module.getCore().isDebug())
                {
                    module.getLogger().log(Level.INFO, "Loading announcement {0}", f.getName());
                }
                try
                {
                    this.loadAnnouncement(f);
                }
                catch (ShoutException e)
                {
                    module.getLogger().log(Level.WARNING, "There was an error loading the announcement: {0}", f.getName());
                    if (module.getCore().isDebug())
                    {
                        module.getLogger().log(Level.SEVERE, "The error message was: ", e);
                    }
                }
            }
        }

    }

    /**
     * Load an specific announcement
     *
     * @param file the folder to load the announcement from
     * @throws ShoutException if folder is not an folder or don't contain
     *                        required information
     */
    private void loadAnnouncement(File file) throws ShoutException
    {
        if (file.isFile())
        {
            throw new ShoutException("Tried to load an announcement that was a file!");
        }

        File confFile = new File(file, "meta.yml");
        if (!confFile.exists())
        {
            File[] yamlFiles = file.listFiles((FilenameFilter)FileExtentionFilter.YAML);
            if (yamlFiles.length > 0)
            {
                if (!yamlFiles[0].renameTo(confFile))
                {
                    throw new ShoutException("No configfile to announcement: " + file.getName());
                }
            }
            else
            {
                throw new ShoutException("No configfile to announcement: " + file.getName());
            }
        }

        Map<String, String> messages = new HashMap<String, String>();
        String world = "*";
        long delay = 0;
        String permNode = "*";
        String group = "*";

        AnnouncementConfig conf = Configuration.load(AnnouncementConfig.class, confFile);
        world = conf.world == null ? world : conf.world;
        permNode = conf.permNode == null ? permNode : conf.permNode;
        group = conf.group == null ? group : conf.group;
        try
        {
            delay = parseDelay(conf.delay);
        }
        catch (IllegalArgumentException e)
        {
            throw new ShoutException("The delay was not valid", e);
        }

        File[] languageFiles = file.listFiles((FilenameFilter)new FileExtentionFilter("txt"));

        for (File lang : languageFiles)
        {
            StringBuilder message = new StringBuilder();
            for (String line : FileUtil.readStringList(lang))
            {
                message.append(line).append("\n");
            }
            messages.put(I18n.normalizeLanguage(lang.getName().replace(".txt", "")), message.toString());
        }

        if (this.module.getCore().isDebug())
        {
            this.module.getLogger().log(Level.INFO, "Languages: {0}", messages.keySet().toString());
            this.module.getLogger().log(Level.INFO, "World: {0}", world);
            this.module.getLogger().log(Level.INFO, "Delay(in millisecounds): {0}", delay);
            this.module.getLogger().log(Level.INFO, "Permission: {0}", permNode);
            this.module.getLogger().log(Level.INFO, "Group: {0}", group);
        }
        try
        {
            this.addAnnouncement(file.getName(), messages, world, delay, permNode, group);
        }
        catch (IllegalArgumentException e)
        {
            throw new ShoutException("The delay was not valid", e);
        }
    }

    /**
     * parse a delay in this format:
     * 10 minutes
     * to
     * 600 000 ms
     *
     * @param delayText	the text to parse
     * @return the delay in ticks
     * @throws IllegalArgumentException if the delay was not in a valid format
     */
    public long parseDelay(String delayText) throws IllegalArgumentException
    {
        String[] parts = delayText.split(" ", 2);
        if (parts.length < 2) // at least 2 parts, more will be ignored for now
        {
            throw new IllegalArgumentException("Not valid delay string");
        }
        int tmpdelay = Integer.parseInt(parts[0]);
        String unit = parts[1].toLowerCase();
        if (unit.equalsIgnoreCase("seconds") || unit.equalsIgnoreCase("second"))
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
    public void createAnnouncement(String name, String message, String delay, String world, String group, String permNode, String locale) throws IOException, IllegalArgumentException
    {
        locale = I18n.normalizeLanguage(locale);
        Map<String, String> messages = new HashMap<String, String>();
        messages.put(locale, message);
        Announcement.validate(name, locale, permNode, world, messages, parseDelay(delay));

        File folder = new File(this.announcementFolder, name);
        folder.mkdirs();
        File configFile = new File(folder, "meta.yml");
        configFile.createNewFile();
        File language = new File(folder, locale + ".txt");
        language.createNewFile();

        AnnouncementConfig config = new AnnouncementConfig();
        config.setCodec("yml");
        config.setFile(configFile);
        config.delay = delay;
        config.world = world;
        config.permNode = permNode;
        config.group = group;
        config.save();

        BufferedWriter bw = new BufferedWriter(new FileWriter(language));
        try
        {
            bw.write(message);
        }
        catch (IOException e)
        {
            bw.close();
            throw e;
        }
        finally
        {
            bw.close();
        }
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
