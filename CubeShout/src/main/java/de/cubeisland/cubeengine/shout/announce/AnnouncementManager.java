package de.cubeisland.cubeengine.shout.announce;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.filesystem.FileExtentionFilter;
import de.cubeisland.cubeengine.core.filesystem.FileUtil;
import de.cubeisland.cubeengine.core.i18n.I18n;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.StringUtils;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import static de.cubeisland.cubeengine.core.util.log.LogLevel.DEBUG;
import static de.cubeisland.cubeengine.core.util.log.LogLevel.WARNING;

/**
 * Class to manage all the announcements and their receivers
 */
public class AnnouncementManager
{
    private final Logger logger;
    private final Shout module;
    private final Announcer announcer;
    private final File announcementFolder;
    private Map<String, AnnouncementReceiver> receivers;
    private Map<String, Announcement> announcements;

    public AnnouncementManager(Shout module, File announcementFolder)
    {
        this.module = module;
        this.logger = module.getLogger();
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
        if (this.announcements.containsKey(name.toLowerCase()))
        {
            return this.announcements.get(name.toLowerCase());
        }
        else
        {
            List<String> matches = StringUtils.getBestMatches(name, this.announcements.keySet(), 3);

            if (matches.size() == 1)
            {
                return this.announcements.get(matches.get(0));
            }
        }
        return null;
    }

    /**
     * Check if this announcement exist
     *
     * @param   name	Name of the announcement to check
     * @return	if this announcement exist
     */
    public boolean hasAnnouncement(String name)
    {
        // TODO correct typos?
        return this.announcements.containsKey(name.toLowerCase());
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
        if (this.announcements.containsKey("motd"))
        {
            messages.add(this.announcements.get("motd"));
        }
        for (Announcement a : announcements.values())
        {
            if (receiver.couldReceive(a) && !a.getName().equalsIgnoreCase("motd"))
            {
                messages.add(a);
            }
        }

        if (messages.isEmpty())
            return;

        receiver.setAllAnnouncements(messages);

        this.receivers.put(receiver.getName(), receiver);
        announcer.scheduleTask(receiver.getName(), new MessageTask(module.getTaskManger(), receiver),
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
            this.initializeUser(user);

        }
    }

    /**
     * Adds an announcement.
     * Most be done before any player joins!
     *
     * @throws ShoutException if there is something wrong with the values
     */
    public void addAnnouncement(String name, Map<String, String> messages, List<String> worlds, long delay, String permNode, boolean motd) throws ShoutException
    {
        try
        {
            Announcement.validate(name, permNode, worlds, messages, delay);
            Announcement announcement = new Announcement(name.toLowerCase(), permNode, worlds, messages, delay, motd);
            this.addAnnouncement(announcement);
        }
        catch (IllegalArgumentException ex)
        {
            throw new ShoutException("The announcement was not valid", ex);
        }
    }

    public void addAnnouncement(Announcement announcement)
    {
        this.announcements.put(announcement.getName().toLowerCase(), announcement);
    }

    /**
     * Load announcements
     *
     * @param	announcementFolder	The folder to load the announcements from
     */
    public void loadAnnouncements(File announcementFolder)
    {
        List<File> announcementFiles = Arrays.asList(announcementFolder.listFiles());
        File motd = new File(announcementFolder, "motd");
        if (!motd.exists())
        {
            motd = new File(announcementFolder, "MOTD");
        }
        if (motd.exists())
        {
            try
            {
                this.loadAnnouncement(motd, true);
            }
            catch (ShoutException ex)
            {
                this.logger.log(WARNING, "Could not load the MOTD");
                this.logger.log(DEBUG, "The error message was: ", ex);
            }
        }

        for (File f : announcementFiles)
        {
            if (f.getName().equalsIgnoreCase("motd"))
            {
                continue;
            }
            if (f.isDirectory())
            {
                this.logger.log(DEBUG, "Loading announcement {0}", f.getName());
                try
                {
                    this.loadAnnouncement(f, false);
                }
                catch (ShoutException ex)
                {
                    this.logger.log(WARNING, "There was an error loading the announcement: {0}", f.getName());
                    this.logger.log(DEBUG, "The error message was: ", ex);
                }
            }
        }

    }

    /**
     * Load an specific announcement
     *
     * @param announcementDir the folder to load the announcement from
     * @throws ShoutException if folder is not an folder or don't contain
     *                        required information
     */
    private void loadAnnouncement(File announcementDir, boolean motd) throws ShoutException
    {
        if (announcementDir.isFile())
        {
            throw new ShoutException("Tried to load an announcement that was a file!");
        }

        File metaFile = new File(announcementDir, "meta.yml");
        if (!metaFile.exists())
        {
            File[] potentialMetaFiles = announcementDir.listFiles((FilenameFilter)FileExtentionFilter.YAML);
            if (potentialMetaFiles.length > 0)
            {
                if (!potentialMetaFiles[0].renameTo(metaFile))
                {
                    throw new ShoutException("No meta file to announcement: " + announcementDir.getName());
                }
            }
            else
            {
                throw new ShoutException("No meta file to announcement: " + announcementDir.getName());
            }
        }

        Map<String, String> messages = new HashMap<String, String>();
        List<String> worlds = Arrays.asList("*");
        long delay;
        String permNode = "*";
        String group = "*";

        AnnouncementConfig config = Configuration.load(AnnouncementConfig.class, metaFile);
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

        File[] languageFiles = announcementDir.listFiles((FilenameFilter)new FileExtentionFilter("txt"));

        for (File langFile : languageFiles)
        {
            StringBuilder message = new StringBuilder();
            for (String line : FileUtil.readStringList(langFile))
            {
                message.append(StringUtils.rtrim(line)).append('\n');
            }
            // TODO convert language names to codes. british -> en_GB
            messages.put(I18n.normalizeLanguage(langFile.getName().replace(".txt", "")), message.toString());
        }

        this.logger.log(DEBUG, "Languages: {0}", messages.keySet().toString());
        this.logger.log(DEBUG, "Worlds: {0}", worlds);
        this.logger.log(DEBUG, "Delay(in millisecounds): {0}", delay);
        this.logger.log(DEBUG, "Permission: {0}", permNode);
        this.logger.log(DEBUG, "Group: {0}", group);

        try
        {
            this.addAnnouncement(announcementDir.getName(), messages, worlds, delay, permNode, motd);
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
    public void createAnnouncement(String name, String locale, String message, String delay, String world, String group, String permNode) throws IOException, IllegalArgumentException
    {
        locale = I18n.normalizeLanguage(locale);
        Map<String, String> messages = new HashMap<String, String>();
        messages.put(locale, message);
        Announcement.validate(name, permNode, world, messages, parseDelay(delay));

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
