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
package de.cubeisland.cubeengine.shout.announce;

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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.filesystem.FileExtentionFilter;
import de.cubeisland.cubeengine.core.filesystem.FileUtil;
import de.cubeisland.cubeengine.core.i18n.I18n;
import de.cubeisland.cubeengine.core.i18n.Language;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.StringUtils;
import de.cubeisland.cubeengine.core.util.matcher.Match;
import de.cubeisland.cubeengine.shout.Shout;
import de.cubeisland.cubeengine.shout.ShoutException;
import de.cubeisland.cubeengine.shout.announce.announcer.Announcer;
import de.cubeisland.cubeengine.shout.announce.announcer.FixedCycleTask;
import de.cubeisland.cubeengine.shout.announce.announcer.MessageTask;
import de.cubeisland.cubeengine.shout.announce.receiver.Receiver;
import de.cubeisland.cubeengine.shout.announce.receiver.UserReceiver;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;

import static de.cubeisland.cubeengine.core.filesystem.FileExtentionFilter.TXT;
import static de.cubeisland.cubeengine.core.logger.LogLevel.*;

/**
 * Class to manage all the dynamicAnnouncements and their receivers
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
    private Map<String, Announcement> dynamicAnnouncements;
    private Map<String, Announcement> fixedCycleAnnouncements;
    private final I18n i18n;
    private MessageOfTheDay motd;

    public AnnouncementManager(Shout module, File announcementFolder)
    {
        this.module = module;
        this.logger = module.getLog();
        this.i18n = module.getCore().getI18n();
        this.announcer = module.getAnnouncer();
        this.receivers = new ConcurrentHashMap<String, Receiver>();
        this.dynamicAnnouncements = new HashMap<String, Announcement>();
        this.fixedCycleAnnouncements = new LinkedHashMap<String, Announcement>();
        this.announcementFolder = announcementFolder;
    }

    /**
     * Get all the dynamicAnnouncements this receiver should receive.
     *
     * @param    receiver    The receiver to get dynamicAnnouncements of.
     * @return A list of all dynamicAnnouncements that should be displayed to this
     *         receiver.
     */
    public List<Announcement> getAnnouncements(String receiver)
    {
        return new ArrayList<Announcement>(this.receivers.get(receiver).getAllAnnouncements());
    }

    /**
     * Get all the dynamicAnnouncements
     *
     * @return All dynamicAnnouncements
     */
    public Collection<Announcement> getDynamicAnnouncements()
    {
        Collection<Announcement> announcements = new HashSet<Announcement>();
        announcements.addAll(this.dynamicAnnouncements.values());
        announcements.addAll(this.fixedCycleAnnouncements.values());
        return announcements;
    }

    /**
     * Get announcement by name
     *
     * @param   name    Name of the announcement
     * @return The announcement with this name, or null if not exist
     */
    public Announcement getAnnouncement(String name)
    {
        Map<String, Announcement> announcements = new HashMap<String, Announcement>();
        announcements.putAll(this.dynamicAnnouncements);
        announcements.putAll(this.fixedCycleAnnouncements);
        name = name.toLowerCase(Locale.ENGLISH);
        Announcement announcement = announcements.get(name);
        if (announcement == null)
        {
            Set<String> matches = Match.string().getBestMatches(name, announcements.keySet(), 3);

            if (matches.size() == 1)
            {
                announcement = announcements.get(matches.iterator().next());
            }
        }
        return announcement;
    }

    /**
     * Check if this announcement exist
     *
     * @param   name	Name of the announcement to check
     * @return	if this announcement exist
     */
    public boolean hasAnnouncement(String name)
    {
        return this.dynamicAnnouncements.containsKey(name.toLowerCase(Locale.ENGLISH)) || this.fixedCycleAnnouncements.containsKey(name.toLowerCase(Locale.ENGLISH));
    }

    /**
     * Get the greatest common divisor of the delays from the dynamicAnnouncements this
     * receiver should receive.
     *
     * @param   receiver	The user to get the gcd of their dynamicAnnouncements.
     * @return	The gcd of the users dynamicAnnouncements.
     */
    public long getGreatestCommonDivisor(Receiver receiver)
    {
        List<Announcement> tmpAnnouncements = this.getAnnouncements(receiver.getName());
        long[] delays;
        if (this.motd != null)
        {
            delays = new long[tmpAnnouncements.size() + 1];
            delays[tmpAnnouncements.size()] = this.motd.getDelay();
        }
        else
        {
            delays = new long[tmpAnnouncements.size() + 1];
        }
        for (int x = 0; x < tmpAnnouncements.size(); x++)
        {
            delays[x] = tmpAnnouncements.get(x).getDelay();
        }
        return this.greatestCommonDivisor(delays);
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
     * Load the dynamicAnnouncements of a user
     * this will create an Receiver and call initializeReceiver
     *
     * @param user the user to load
     */
    public void initializeUser(User user)
    {
        this.initializeReceiver(new UserReceiver(user, this));
    }

    /**
     * initialize this receivers dynamicAnnouncements
     *
     * @param receiver	The receiver
     */
    public void initializeReceiver(Receiver receiver)
    {
        Queue<Announcement> messages = new LinkedList<Announcement>();

        if (this.motd != null)
        {
            receiver.setMOTD(this.motd);
        }

        // Load what dynamicAnnouncements should be displayed to the user
        for (Announcement announcement : this.dynamicAnnouncements.values())
        {
            if (receiver.couldReceive(announcement))
            {
                messages.add(announcement);
            }
        }

        if (messages.isEmpty())
            return;

        receiver.setAllAnnouncements(messages);

        this.receivers.put(receiver.getName(), receiver);
        this.announcer.scheduleDynamicTask(receiver.getName(),
            new MessageTask(this.module.getCore().getTaskManager(), receiver), this.getGreatestCommonDivisor(receiver));
    }

    /**
     * Clean all stored information of that user
     *
     * @param receiver	the receiver to clean
     */
    public void clean(String receiver)
    {
        this.receivers.remove(receiver);
        this.announcer.cancelDynamicTask(receiver);
    }

    /**
     * Reload all loaded announcements and users
     */
    public void reload()
    {
        for (Receiver receiver : this.receivers.values())
        {
            this.clean(receiver.getName());
        }
        module.getCore().getTaskManager().cancelTasks(this.module);

        this.announcer.restart();

        this.receivers.clear();
        this.dynamicAnnouncements.clear();
        this.fixedCycleAnnouncements.clear();

        this.loadAnnouncements(this.announcementFolder);
        this.initUsers();
    }

    public void initUsers()
    {
        for (User user : this.module.getCore().getUserManager().getOnlineUsers())
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

    public void addAnnouncement(final Announcement announcement)
    {
        if (announcement.hasFixedCycle())
        {
            this.fixedCycleAnnouncements.put(announcement.getName().toLowerCase(Locale.ENGLISH), announcement);
            this.announcer.scheduleFixedTask(announcement.getName().toLowerCase(Locale.ENGLISH), new FixedCycleTask(this.module, announcement), announcement.getDelay());
        }
        else
        {
            this.dynamicAnnouncements.put(announcement.getName().toLowerCase(Locale.ENGLISH), announcement);
        }
    }

    /**
     * Load dynamicAnnouncements
     *
     * @param	announcementFolder	The folder to load the dynamicAnnouncements from
     */
    public void loadAnnouncements(File announcementFolder)
    {
        File[] files = announcementFolder.listFiles();
        if (files == null)
        {
            this.logger.error("Reading the announcement folder failed! No dynamicAnnouncements will be loaded");
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
                    this.logger.debug("An announcement that looked like the MOTD failed to load.", e);
                }
            }
            if (folder.isDirectory())
            {
                this.logger.debug("Loading announcement {0}", folder.getName());
                try
                {
                    this.addAnnouncement(this.loadAnnouncement(folder));
                }
                catch (ShoutException ex)
                {
                    this.logger.warn("There was an error loading the announcement: {0}", folder.getName());
                    this.logger.debug("The error message was: ", ex);
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

        Map<Locale, String[]> messages = new HashMap<Locale, String[]>();

        File[] messageFiles = announcementFolder.listFiles((FilenameFilter)TXT);
        if (messageFiles != null)
        {
            for (File langFile : messageFiles)
            {
                String name = StringUtils.stripFileExtension(langFile.getName());
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
                        content = content.replace("\r\n", "\n").replace('\r', '\n').trim();
                        messages.put(language.getLocale(), StringUtils.explode("\n", content));
                    }
                }
                catch (FileNotFoundException ignore)
                {}
            }
        }

        this.logger.debug("Languages: {0}", messages.keySet().toString());
        this.logger.debug("Worlds: {0}", config.worlds);
        this.logger.debug("Delay(in milliseconds): {0}", delay);
        this.logger.debug("Permission: {0}", config.permNode);
        this.logger.debug("Group: {0}", config.group);
        this.logger.debug("FixedCycle: {0}", config.fixedCycle);

        try
        {
            return new Announcement(
                announcementFolder.getName().toLowerCase(Locale.US),
                config.permNode,
                config.worlds,
                messages,
                delay,
                config.fixedCycle);
        }
        catch (IllegalArgumentException e)
        {
            throw new ShoutException(e);
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
    public void createAnnouncement(String name, Locale locale, String message, String delay, String world, String group, String permNode, boolean fc) throws IOException, IllegalArgumentException
    {
        Validate.notEmpty(name);
        Validate.notNull(locale);
        Validate.notEmpty(message);
        Validate.notEmpty(delay);
        Validate.notEmpty(world);
        Validate.notEmpty(group);
        Validate.notEmpty(permNode);

        File folder = new File(this.announcementFolder, name);
        if (!folder.mkdirs())
        {
            throw new IOException("Failed to create the announcement folder for '" + name + "'");
        }

        AnnouncementConfig config = Configuration.createInstance(AnnouncementConfig.class);
        config.setFile(new File(folder, META_FILE_NAME));
        config.delay = delay;
        config.worlds = Arrays.asList(world);
        config.permNode = permNode;
        config.group = group;
        config.fixedCycle = fc;
        config.save();

        BufferedWriter bw = new BufferedWriter(new FileWriter(new File(folder, locale.toString() + ".txt")));
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

    public Shout getModule()
    {
        return module;
    }
}
