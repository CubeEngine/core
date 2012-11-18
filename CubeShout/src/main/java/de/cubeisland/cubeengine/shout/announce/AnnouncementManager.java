package de.cubeisland.cubeengine.shout.announce;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.filesystem.FileExtentionFilter;
import de.cubeisland.cubeengine.core.filesystem.FileUtil;
import de.cubeisland.cubeengine.core.i18n.I18n;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.log.LogLevel;
import de.cubeisland.cubeengine.shout.Shout;
import de.cubeisland.cubeengine.shout.ShoutException;
import de.cubeisland.cubeengine.shout.announce.announcer.Announcer;
import de.cubeisland.cubeengine.shout.announce.announcer.MessageTask;
import de.cubeisland.cubeengine.shout.announce.receiver.AnnouncementReceiver;
import de.cubeisland.cubeengine.shout.announce.receiver.UserReceiver;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class to manage all the announcements and their receivers
 */
public class AnnouncementManager
{
    private Shout module;
    private Announcer announcer;
    private Map<String, AnnouncementReceiver> receivers;
    private Map<String, Announcement> announcements;
    private File announcementFolder;

    public AnnouncementManager(Shout module, File announcementFolder)
    {
        this.module = module;
        this.announcer = module.getAnnouncer();
        this.receivers = new ConcurrentHashMap<String, AnnouncementReceiver>();
        this.announcements = new LinkedHashMap<String, Announcement>();
        this.announcementFolder = announcementFolder;
    }

    /**
     * Get all the announcements this receiver should receive.
     *
     * @param	receiver	The receiver to get announcements of.
     * @return	A list of all announcements that should be displayed to this
     *         receiver.
     */
    public List<Announcement> getAnnouncements(String receiver)
    {
        return new ArrayList<Announcement>(receivers.get(receiver).getAllAnnouncements());
    }

    /**
     * Get all the announcements
     *
     * @return All announcements
     */
    public Collection<Announcement> getAnnouncements()
    {
        return this.announcements.values();
    }

    /**
     * Get announcement by name
     *
     * @param   name	Name of the announcement
     * @return	The announcement with this name, or null if not exist
     */
    public Announcement getAnnouncement(String name)
    {
        return this.announcements.get(name);
    }

    /**
     * Check if this announcement exist
     *
     * @param   name	Name of the announcement to check
     * @return	if this announcement exist
     */
    public boolean hasAnnouncement(String name)
    {
        return this.announcements.containsKey(name);
    }

    /**
     * Get the greatest common divisor of the delays from the announcements this
     * receiver should receive.
     *
     * @param   receiver	The user to get the gcd of their announcements.
     * @return	The gcd of the users announcements.
     */
    public long getGreatestCommonDivisor(AnnouncementReceiver receiver)
    {
        List<Announcement> tmpAnnouncements = this.getAnnouncements(receiver.getName());
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
     * @param	integers	The list to get the gcd from.
     * @return	gcd of all the integers in the list.
     */
    private long greatestCommonDivisor(long[] integers)
    {
        long result = integers[0];

        for (int x = 1; x < integers.length; x++)
        {
            while (integers[x] > 0)
            {
                long t = integers[x];
                integers[x] = result % integers[x];
                result = t;
            }
        }
        return result;
    }

    /**
     * Load the announcements of a user
     * this will create an AnnouncementReceiver and call initializeReceiver
     *
     * @param user the user to load
     */
    public void initializeUser(User user)
    {
        AnnouncementReceiver receiver = new UserReceiver(user, this);
        this.initializeReceiver(receiver);
    }

    /**
     * initialize this receivers announcements
     *
     * @param receiver	The receiver
     */
    public void initializeReceiver(AnnouncementReceiver receiver)
    {
        Queue<Announcement> messages = new LinkedList<Announcement>();

        // Load what announcements should be displayed to the user
        for (Announcement a : announcements.values())
        {
            if (receiver.couldReceive(a))
            {
                messages.add(a);
            }
        }

        if (messages.isEmpty())
            return;

        receiver.setAllAnnouncements(messages);

        this.receivers.put(receiver.getName(), receiver);
        announcer.scheduleTask(receiver.getName(), new MessageTask(this, module.getTaskManger(), receiver),
                this.getGreatestCommonDivisor(receiver));
    }

    /**
     * Clean all stored information of that user
     *
     * @param receiver	the receiver to clean
     */
    public void clean(String receiver)
    {
        this.receivers.remove(receiver);
        this.announcer.stopTask(receiver);
    }

    /**
     * Reload all loaded announcements and users
     */
    public void reload()
    {
        for (AnnouncementReceiver receiver : receivers.values())
        {
            this.clean(receiver.getName());
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
            this.initializeReceiver(new UserReceiver(user, this));

        }
    }

    /**
     * Adds an announcement.
     * Most be done before ay player joins!
     *
     * @throws ShoutException if there is something wrong with the values
     */
    public void addAnnouncement(String name, Map<String, String> messages, List<String> worlds, long delay,
                                String permNode) throws ShoutException
    {
        try
        {
            Announcement.validate(name, permNode, module.getCore().getConfiguration().defaultLanguage,
                    worlds, messages, delay);
            Announcement announcement = new Announcement(name, module.getCore().getConfiguration().defaultLanguage,
                    permNode, worlds, messages, delay);
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
                    module.getLogger().log(LogLevel.DEBUG, "Loading announcement {0}", f.getName());
                }
                try
                {
                    this.loadAnnouncement(f);
                }
                catch (ShoutException e)
                {
                    module.getLogger().log(LogLevel.WARNING, "There was an error loading the announcement: {0}", f.getName());
                    if (module.getCore().isDebug())
                    {
                        module.getLogger().log(LogLevel.ERROR, "The error message was: ", e);
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
                    throw new ShoutException("No config file to announcement: " + file.getName());
                }
            }
            else
            {
                throw new ShoutException("No config file to announcement: " + file.getName());
            }
        }

        Map<String, String> messages = new HashMap<String, String>();
        List<String> worlds = Arrays.asList("*");
        long delay;
        String permNode = "*";
        String group = "*";

        AnnouncementConfig config = Configuration.load(AnnouncementConfig.class, confFile);
        worlds = config.worlds == null ? worlds : config.worlds;
        permNode = config.permNode == null ? permNode : config.permNode;
        group = config.group == null ? group : config.group;
        try
        {
            delay = parseDelay(config.delay);
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
            this.module.getLogger().log(LogLevel.DEBUG, "Languages: {0}", messages.keySet().toString());
            this.module.getLogger().log(LogLevel.DEBUG, "Worlds: {0}", worlds);
            this.module.getLogger().log(LogLevel.DEBUG, "Delay(in millisecounds): {0}", delay);
            this.module.getLogger().log(LogLevel.DEBUG, "Permission: {0}", permNode);
            this.module.getLogger().log(LogLevel.DEBUG, "Group: {0}", group);
        }
        try
        {
            this.addAnnouncement(file.getName(), messages, worlds, delay, permNode);
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
    public static long parseDelay(String delayText) throws IllegalArgumentException
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
        config.worlds = Arrays.asList(world);
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
}
