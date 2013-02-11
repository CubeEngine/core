package de.cubeisland.cubeengine.basics.moderation.kit;

import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.config.annotations.Codec;
import de.cubeisland.cubeengine.core.config.annotations.Comment;
import de.cubeisland.cubeengine.core.config.annotations.Option;
import de.cubeisland.cubeengine.core.util.StringUtils;
import de.cubeisland.cubeengine.core.logger.LogLevel;
import de.cubeisland.cubeengine.core.util.matcher.Match;
import de.cubeisland.cubeengine.core.util.time.Duration;
import gnu.trove.map.hash.THashMap;
import java.io.File;
import java.io.FileFilter;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Codec("yml")
public class KitConfiguration extends Configuration
{
    private static Basics basics = Basics.getInstance();
    public String kitName;
    @Comment("Players that join your server the first time will receive this kit if set on true.")
    @Option("give-on-first-join")
    public boolean giveOnFirstJoin = false;
    @Comment("If not empty this message will be displayed when receiving this kit.")
    @Option("custom-receive-message")
    public String customReceiveMsg = "";
    @Comment("amount*itemName/Id:Data customName\n"
        + "example: 64*1:0 MyFirstStoneBlocks")
    @Option("items")
    public List<KitItem> kitItems = new LinkedList<KitItem>();
    @Option("commands")
    public List<String> kitCommands = new LinkedList<String>();
    @Comment("If a permission is generated the user needs the permission to bew able to receive this kit")
    @Option("generate-permission")
    public boolean usePerm = false;
    @Comment("The delay between each usage of this kit.")
    @Option("limit-usage-delay")
    public Duration limitUsageDelay = new Duration("-1");
    @Comment("Limits the usage to x amount. Use 0 for infinite.")
    @Option("limit-usage")
    public int limitUsage = 0;
    private static THashMap<String, Kit> kitMap = new THashMap<String, Kit>();
    private static THashMap<Kit, KitConfiguration> kitConfigMap = new THashMap<Kit, KitConfiguration>();

    @Override
    public void onLoaded()
    {
        String fileName = this.getFile().getName();
        this.kitName = fileName.substring(0, fileName.indexOf(".yml"));
        if (this.kitName.length() > 50)
        {
            this.kitName = this.kitName.substring(0, 50); // limit for db
        }
    }

    public Kit getKit()
    {
        Kit kit = new Kit(this.kitName, this.giveOnFirstJoin, this.limitUsage, this.limitUsageDelay.toMillis(), this.usePerm, this.customReceiveMsg, this.kitCommands, this.kitItems);
        return kit;
    }

    public static Kit getKit(String name)
    {
        Set<String> match = Match.string().getBestMatches(name.toLowerCase(Locale.ENGLISH), kitMap.keySet(), 2);
        if (match.isEmpty())
        {
            return null;
        }
        return kitMap.get(match.iterator().next());
    }

    public static void saveKit(Kit kit)
    {
        KitConfiguration config = kitConfigMap.get(kit);
        if (config == null)
        {
            config = new KitConfiguration();
            kitConfigMap.put(kit, config);
            kitMap.put(kit.getKitName(), kit);
        }
        config.setCodec("yml");
        kit.applyToConfig(config);
        config.save(new File(basics.getFolder(), File.separator + "kits" + File.separator + config.kitName + ".yml"));
    }

    public static void loadKit(File file)
    {
        try
        {
            KitConfiguration config = Configuration.load(KitConfiguration.class, file);
            config.kitName = StringUtils.stripFileExtension(file.getName());
            Kit kit = config.getKit();

            kitConfigMap.put(kit, config);
            kitMap.put(config.kitName.toLowerCase(Locale.ENGLISH), kit);
        }
        catch (Exception ex)
        {
            Basics.getInstance().getLogger().log(LogLevel.WARNING, "Could not load the kit configuration!", ex);
        }
    }

    public static void loadKits()
    {
        File folder = new File(basics.getFolder(), "kits");
        folder.mkdir();
        for (File file : folder.listFiles(new FileFilter()
        {
            @Override
            public boolean accept(File pathname)
            {
                if (pathname.getName().endsWith(".yml"))
                {
                    return true;
                }
                return false;
            }
        }))
        {
            loadKit(file);
        }
    }
}
