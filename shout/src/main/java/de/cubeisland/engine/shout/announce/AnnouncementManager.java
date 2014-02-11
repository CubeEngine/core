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
package de.cubeisland.engine.shout.announce;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.filesystem.FileUtil;
import de.cubeisland.engine.core.i18n.I18n;
import de.cubeisland.engine.core.i18n.Language;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.StringUtils;
import de.cubeisland.engine.core.util.matcher.Match;
import de.cubeisland.engine.logging.Log;
import de.cubeisland.engine.shout.Shout;
import de.cubeisland.engine.shout.ShoutException;
import de.cubeisland.engine.shout.announce.announcer.Announcer;
import de.cubeisland.engine.shout.announce.announcer.FixedCycleTask;
import de.cubeisland.engine.shout.announce.announcer.MessageTask;
import de.cubeisland.engine.shout.announce.receiver.Receiver;
import de.cubeisland.engine.shout.announce.receiver.UserReceiver;
import org.apache.commons.lang.Validate;

import static de.cubeisland.engine.core.filesystem.FileExtensionFilter.TXT;
import static de.cubeisland.engine.core.filesystem.FileExtensionFilter.YAML;

/**
 * Class to manage all the announcements and their receivers
 */
public class AnnouncementManager
{
    private static final String MOTD_FOLDER_NAME = "MOTD";
    private static final String META_FILE_NAME = "meta.yml";

    private final Log logger;
    private final Shout module;
    private final Announcer announcer;
    private final Path announcementFolder;
    private final Map<String, Receiver> receivers;
    private final Map<String, Announcement> dynamicAnnouncements;
    private final Map<String, Announcement> fixedCycleAnnouncements;
    private final I18n i18n;
    private MessageOfTheDay motd;

    public AnnouncementManager(Shout module, Path announcementFolder)
    {
        this.module = module;
        this.logger = module.getLog();
        this.i18n = module.getCore().getI18n();
        this.announcer = module.getAnnouncer();
        this.receivers = new ConcurrentHashMap<>();
        this.dynamicAnnouncements = new HashMap<>();
        this.fixedCycleAnnouncements = new LinkedHashMap<>();
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
        return new ArrayList<>(this.receivers.get(receiver).getAllAnnouncements());
    }

    /**
     * Get all the announcements registered
     *
     * @return All announcements currently registered
     */
    public Collection<Announcement> getAllAnnouncements()
    {
        Collection<Announcement> announcements = new HashSet<>();
        announcements.addAll(this.dynamicAnnouncements.values());
        announcements.addAll(this.fixedCycleAnnouncements.values());
        return announcements;
    }

    /**
     * Get announcement by name
     *
     * @param   name    Name of the announcement
     * @return  The announcement with this name, or null if not exist
     */
    public Announcement getAnnouncement(String name)
    {
        Map<String, Announcement> announcements = new HashMap<>();
        announcements.putAll(this.dynamicAnnouncements);
        announcements.putAll(this.fixedCycleAnnouncements);
        name = name.toLowerCase(Locale.ENGLISH);
        Announcement announcement = announcements.get(name);
        if (announcement == null)
        {
            Set<String> matches = Match.string().getBestMatches(name, announcements.keySet(), 3);

            if (matches.size() > 0)
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
        return this.dynamicAnnouncements.containsKey(name.toLowerCase(Locale.ENGLISH))
            || this.fixedCycleAnnouncements.containsKey(name.toLowerCase(Locale.ENGLISH));
    }

    /**
     * Get the greatest common divisor of the delays from the dynamicAnnouncements this
     * receiver should receive.
     *
     * @param   receiver	The user to get the gcd of their dynamicAnnouncements.
     * @return	The gcd of the users dynamicAnnouncements.
     */
    public long getGCD(Receiver receiver)
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
        return this.gcd(delays);
    }

    /**
     * Calculate the greatest common divisor of a list of integers.
     *
     * @param	integers	The list to get the gcd from.
     * @return	gcd of all the integers in the list.
     */
    private long gcd(long[] integers)
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
        Queue<Announcement> messages = new LinkedList<>();

        if (this.motd != null)
        {
            receiver.setMOTD(this.motd);
        }

        // Load what dynamic time announcements should be displayed to the user
        for (Announcement announcement : this.dynamicAnnouncements.values())
        {
            if (receiver.couldReceive(announcement))
            {
                messages.add(announcement);
            }
        }

        if (messages.isEmpty())
        {
            return;
        }

        receiver.setAllAnnouncements(messages);

        this.receivers.put(receiver.getName(), receiver);
        this.announcer.scheduleDynamicTask(receiver.getName(),
            new MessageTask(this.module.getCore().getTaskManager(), receiver), this.getGCD(receiver));
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
            this.announcer.scheduleFixedTask(announcement.getName().toLowerCase(Locale.ENGLISH),
                                             new FixedCycleTask(this.module.getCore().getUserManager(),
                                                                this.module.getCore().getTaskManager(), announcement),
                                             announcement.getDelay());
        }
        else
        {
            this.dynamicAnnouncements.put(announcement.getName().toLowerCase(Locale.ENGLISH), announcement);
            for (Receiver receiver : this.receivers.values())
            {
                if (receiver.couldReceive(announcement))
                {
                    this.announcer.cancelDynamicTask(receiver.getName());
                    receiver.addAnnouncement(announcement);
                    this.announcer.scheduleDynamicTask(receiver.getName(),
                                                       new MessageTask(this.module.getCore().getTaskManager(), receiver),
                                                       this.getGCD(receiver));
                }
            }
        }
    }

    /**
     * Load announcements
     *
     * @param	announcementFolder	The folder to load the announcements from
     */
    public void loadAnnouncements(Path announcementFolder)
    {
        try (DirectoryStream<Path> directory = Files.newDirectoryStream(announcementFolder))
        {
            boolean motdLoaded = false;
            for (Path path : directory)
            {
                if (!Files.isDirectory(path))
                {
                    continue;
                }
                if (!motdLoaded && path.getFileName().toString().equalsIgnoreCase(MOTD_FOLDER_NAME))
                {
                    // this might be the message of the day
                    try
                    {
                        this.logger.debug("Loading the MOTD");
                        this.motd = this.loadMotd(path);
                        motdLoaded = true;
                        continue;
                    }
                    catch (ShoutException ex)
                    {
                        this.logger.info(ex, "An announcement that looked like the MOTD failed to load.");
                    }
                }
                this.logger.debug("Loading announcement {}", path);
                try
                {
                    this.addAnnouncement(this.loadAnnouncement(path));
                }
                catch (ShoutException ex)
                {
                    this.logger.warn(ex, "There was an error loading the announcement: {}", path);
                }
            }
        }
        catch (IOException ex)
        {
            this.logger.warn(ex, "An error occured while loading announcements.");
        }
    }

    public MessageOfTheDay loadMotd(Path announcementFolder) throws ShoutException
    {
        return new MessageOfTheDay(this.loadAnnouncement(announcementFolder));
    }

    /**
     * Load a specific announcement
     *
     * @param announcementFolder the folder to load the announcement from
     * @throws ShoutException if folder is not an folder or don't contain
     *                        required information
     */
    public Announcement loadAnnouncement(Path announcementFolder) throws ShoutException
    {
        if (Files.isRegularFile(announcementFolder))
        {
            throw new ShoutException("Tried to load an announcement that was a file!");
        }

        Path metaFile = announcementFolder.resolve(META_FILE_NAME);
        if (!Files.exists(metaFile))
        {
            try (DirectoryStream<Path> directory = Files.newDirectoryStream(announcementFolder, YAML))
            {
                Iterator<Path> directoryIterator = directory.iterator();
                if (directoryIterator.hasNext())
                {
                    Path alternative = directoryIterator.next();
                    try
                    {
                        Files.move(alternative, metaFile);
                    }
                    catch (IOException ex)
                    {
                        this.module.getLog().info(ex, "Failed to rename the meta file, using it anyway: {}", alternative.getFileName());
                        metaFile = alternative;
                    }
                }
                else
                {
                    throw new ShoutException("No meta file to announcement: " + announcementFolder);
                }
            }
            catch (IOException e)
            {
                throw new ShoutException("Failed to search for alternative meta files in the the announcement folder " + announcementFolder, e);
            }
        }

        AnnouncementConfig config = this.module.getCore().getConfigFactory().load(AnnouncementConfig.class, metaFile.toFile());

        long delay;
        try
        {
            delay = parseDelay(config.delay);
        }
        catch (IllegalArgumentException e)
        {
            throw new ShoutException("The delay was not valid", e);
        }

        Map<Locale, String[]> messages = new HashMap<>();

        try (DirectoryStream<Path> directory = Files.newDirectoryStream(announcementFolder, TXT))
        {
            for (Path langFile : directory)
            {
                String name = StringUtils.stripFileExtension(langFile.getFileName().toString());
                if (name.isEmpty())
                {
                    continue;
                }
                Language language;
                Set<Language> langs = this.i18n.searchLanguages(name);
                if (langs.size() < 1)
                {
                    this.module.getLog().info("Tried to load a lang-file with an invalid locale: {}", name);
                    continue;
                }

                language = langs.iterator().next();
                try (FileChannel in = FileChannel.open(langFile))
                {
                    String content = FileUtil.readToString(in, Core.CHARSET);
                    if (content != null)
                    {
                        content = content.replace("\r\n", "\n").replace('\r', '\n').trim();
                        messages.put(language.getLocale(), StringUtils.explode("\n", content));
                    }
                }
                catch (IOException ex)
                {
                    this.module.getLog().info(ex, "Failed to load an announcement file: {}", langFile);
                }
            }
        }
        catch (IOException ex)
        {
            this.logger.warn(ex, "Failed to read an announcement folder: {}", announcementFolder);
        }


        this.logger.trace("Languages: {}", messages.keySet().toString());
        this.logger.trace("Worlds: {}", config.worlds);
        this.logger.trace("Delay(in milliseconds): {}", delay);
        this.logger.trace("Permission: {}", config.permName);
        this.logger.trace("FixedCycle: {}", config.fixedCycle);

        try
        {
            return new Announcement(this.module,
                announcementFolder.getFileName().toString().toLowerCase(Locale.US),
                config.permName,
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
    public Announcement createAnnouncement(String name, Locale locale, String message, String delay, String world, String permName, boolean fc) throws IOException, IllegalArgumentException
    {
        Validate.notEmpty(name);
        Validate.notNull(locale);
        Validate.notEmpty(message);
        Validate.notEmpty(delay);
        Validate.notEmpty(world);
        Validate.notEmpty(permName);

        Path folder = this.announcementFolder.resolve(name);

        Files.createDirectories(folder);

        AnnouncementConfig config = this.module.getCore().getConfigFactory().create(AnnouncementConfig.class);
        config.setFile(folder.resolve(META_FILE_NAME).toFile());
        config.delay = delay;
        config.worlds = Arrays.asList(world);
        config.permName = permName;
        config.fixedCycle = fc;
        config.save();

        try (BufferedWriter writer = Files.newBufferedWriter(folder.resolve(locale.toString() + ".txt"), Core.CHARSET))
        {
            writer.write(message);
        }
        Map<Locale, String[]> messages = new HashMap<>();
        messages.put(locale, StringUtils.explode("\n", message));
        return new Announcement(this.module, name,permName, Arrays.asList(world), messages, parseDelay(delay), fc);
    }

    public Receiver getReceiver(String name)
    {
        return this.receivers.get(name);
    }
}
