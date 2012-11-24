package de.cubeisland.cubeengine.shout.announce;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.filesystem.FileExtentionFilter;
import de.cubeisland.cubeengine.core.filesystem.FileUtil;
import de.cubeisland.cubeengine.core.i18n.I18n;
import de.cubeisland.cubeengine.core.i18n.Language;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.StringUtils;
import de.cubeisland.cubeengine.shout.Shout;
import de.cubeisland.cubeengine.shout.ShoutException;
import de.cubeisland.cubeengine.shout.announce.announcer.Announcer;
import de.cubeisland.cubeengine.shout.announce.announcer.MessageTask;
import de.cubeisland.cubeengine.shout.announce.receiver.Receiver;
import de.cubeisland.cubeengine.shout.announce.receiver.UserReceiver;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import static de.cubeisland.cubeengine.core.filesystem.FileExtentionFilter.TXT;
import static de.cubeisland.cubeengine.core.util.log.LogLevel.*;

/**
 * Class to manage all the announcements and their receivers
 */
public class AnnouncementManager
{
    private static final String MOTD_FOLDER_NAME = "MOTD";
    private static final String META_FILE_NAME = "meta.yml";

    private final Logger logger;
    private final Shout module;
    private final Announcer announcer;
    private final File announcementFolder;
    private Map<String, Receiver> receivers;
    private Map<String, Announcement> announcements;
    private final I18n i18n;
    private MessageOfTheDay motd;

    public AnnouncementManager(Shout module, File announcementFolder)
    {
        this.module = module;
        this.logger = module.getLogger();
        this.i18n = module.getCore().getI18n();
        this.announcer = module.getAnnouncer();
        this.receivers = new ConcurrentHashMap<String, Receiver>();
        this.announcements = new LinkedHashMap<String, Announcement>();
        this.announcementFolder = announcementFolder;
    }

    /**
     * Get all the announcements this receiver should receive.
     *
     * @param    receiver    The receiver to get announcements of.
     * @return A list of all announcements that should be displayed to this
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
     * @param   name    Name of the announcement
     * @return The announcement with this name, or null if not exist
     */
    public Announcement getAnnouncement(String name)
    {
        name = name.toLowerCase(Locale.ENGLISH);
        if (this.announcements.containsKey(name))
        {
            return this.announcements.get(name);
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
    public long getGreatestCommonDivisor(Receiver receiver)
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
     * this will create an Receiver and call initializeReceiver
     *
     * @param user the user to load
     */
    public void initializeUser(User user)
    {
        this.initializeReceiver(new UserReceiver(user, this));
    }

    /**
     * initialize this receivers announcements
     *
     * @param receiver	The receiver
     */
    public void initializeReceiver(Receiver receiver)
    {
        Queue<Announcement> messages = new LinkedList<Announcement>();

        // Load what announcements should be displayed to the user
        if (this.announcements.containsKey("motd"))
        {
            messages.add(this.announcements.get("motd"));
        }
        for (Announcement announcement : announcements.values())
        {
            if (receiver.couldReceive(announcement) && !announcement.getName().equalsIgnoreCase("motd"))
            {
                messages.add(announcement);
            }
        }

        if (messages.isEmpty())
            return;

        receiver.setAllAnnouncements(messages);

        this.receivers.put(receiver.getName(), receiver);
        this.announcer.scheduleTask(receiver.getName(), new MessageTask(module.getTaskManger(), receiver), this.getGreatestCommonDivisor(receiver));
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
        for (Receiver receiver : receivers.values())
        {
            this.clean(receiver.getName());
        }

        this.receivers = new ConcurrentHashMap<String, Receiver>();
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

    public MessageOfTheDay getMotd()
    {
        return this.motd;
    }

    public void setMotd(MessageOfTheDay motd)
    {
        this.motd = motd;
    }

    public void addAnnouncement(Announcement announcement)
    {
        this.announcements.put(announcement.getName().toLowerCase(Locale.ENGLISH), announcement);
    }

    /**
     * Load announcements
     *
     * @param	announcementFolder	The folder to load the announcements from
     */
    public void loadAnnouncements(File announcementFolder)
    {
        File[] files = announcementFolder.listFiles();
        if (files == null)
        {
            this.logger.log(ERROR, "Reading the announcement folder failed!");
            return;
        }

        List<File> announcementFolders = new ArrayList<File>();
        // filter out files
        for (File file : files)
        {
            if (file.isDirectory())
            {
                announcementFolders.add(file);
            }
        }

        boolean motdLoaded = false;
        for (File folder : announcementFolders)
        {
            if (!motdLoaded && folder.getName().equalsIgnoreCase(MOTD_FOLDER_NAME))
            {
                // this might be the message of the day
                try
                {
                    this.motd = this.loadMotd(folder);
                    motdLoaded = true;
                    continue;
                }
                catch (ShoutException e)
                {
                    this.logger.log(DEBUG, "An announcement that looked like the MOTD failed to load.", e);
                }
            }
            if (folder.isDirectory())
            {
                this.logger.log(DEBUG, "Loading announcement {0}", folder.getName());
                try
                {
                    this.addAnnouncement(this.loadAnnouncement(folder));
                }
                catch (ShoutException ex)
                {
                    this.logger.log(WARNING, "There was an error loading the announcement: {0}", folder.getName());
                    this.logger.log(DEBUG, "The error message was: ", ex);
                }
            }
        }

    }

    public MessageOfTheDay loadMotd(File announcementFolder) throws ShoutException
    {
        Announcement motd = this.loadAnnouncement(announcementFolder);
        return new MessageOfTheDay(motd);
    }

    /**
     * Load an specific announcement
     *
     * @param announcementFolder the folder to load the announcement from
     * @throws ShoutException if folder is not an folder or don't contain
     *                        required information
     */
    public Announcement loadAnnouncement(File announcementFolder) throws ShoutException
    {
        if (announcementFolder.isFile())
        {
            throw new ShoutException("Tried to load an announcement that was a file!");
        }

        File metaFile = new File(announcementFolder, META_FILE_NAME);
        if (!metaFile.exists())
        {
            File[] potentialMetaFiles = announcementFolder.listFiles((FilenameFilter)FileExtentionFilter.YAML);
            if (potentialMetaFiles.length > 0)
            {
                if (!potentialMetaFiles[0].renameTo(metaFile))
                {
                    throw new ShoutException("No meta file to announcement: " + announcementFolder.getName());
                }
            }
            else
            {
                throw new ShoutException("No meta file to announcement: " + announcementFolder.getName());
            }
        }

        AnnouncementConfig config = Configuration.load(AnnouncementConfig.class, metaFile);

        long delay;
        try
        {
            delay = parseDelay(config.delay);
        }
        catch (IllegalArgumentException e)
        {
            throw new ShoutException("The delay was not valid", e);
        }

        Map<String, String[]> messages = new HashMap<String, String[]>();

        File[] messageFiles = announcementFolder.listFiles((FilenameFilter)TXT);
        if (messageFiles != null)
        {
            for (File langFile : messageFiles)
            {
                String name = StringUtils.stripFileExtention(langFile.getName());
                if (name.isEmpty())
                {
                    continue;
                }
                Language language;
                Set<Language> langs = this.i18n.searchLanguages(name);
                if (langs.size() != 1)
                {
                    continue;
                }

                language = langs.iterator().next();
                try
                {
                    String content = FileUtil.readToString(new FileInputStream(langFile), Charset.forName("UTF-8"));
                    if (content != null)
                    {
                        content = content.replace("\r\n", "\n").replace('\r', '\n');
                        messages.put(language.getCode(), StringUtils.explode("\n", content));
                    }
                }
                catch (FileNotFoundException ignore)
                {}
            }
        }

        this.logger.log(DEBUG, "Languages: {0}", messages.keySet().toString());
        this.logger.log(DEBUG, "Worlds: {0}", config.worlds);
        this.logger.log(DEBUG, "Delay(in milliseconds): {0}", delay);
        this.logger.log(DEBUG, "Permission: {0}", config.permNode);
        this.logger.log(DEBUG, "Group: {0}", config.group);

        try
        {
            return new Announcement(
                announcementFolder.getName().toLowerCase(Locale.ENGLISH),
                config.permNode,
                config.worlds,
                messages,
                delay);
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

        File folder = new File(this.announcementFolder, name);
        if (!folder.mkdirs())
        {
            throw new IOException("Failed to create the announcement folder for '" + name + "'");
        }

        AnnouncementConfig config = new AnnouncementConfig();
        config.setFile(new File(folder, META_FILE_NAME));
        config.delay = delay;
        config.worlds = Arrays.asList(world);
        config.permNode = permNode;
        config.group = group;
        config.save();

        locale = I18n.normalizeLanguage(locale);
        BufferedWriter bw = new BufferedWriter(new FileWriter(new File(folder, locale + ".txt")));
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
