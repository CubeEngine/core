package de.cubeisland.cubeengine.basics.moderation.kit;

import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.config.annotations.Codec;
import de.cubeisland.cubeengine.core.config.annotations.Comment;
import de.cubeisland.cubeengine.core.config.annotations.Option;
import de.cubeisland.cubeengine.core.util.time.Duration;
import gnu.trove.map.hash.THashMap;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

@Codec("yml")
public class KitConfiguration extends Configuration
{
    private static Basics basics = Basics.getInstance();
    @Comment("The name to access this kit.")
    @Option("kit-name")
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

    public Kit getKit()
    {
        Kit kit = new Kit(this.kitName, this.giveOnFirstJoin, this.limitUsage, this.limitUsageDelay.toMillis(), this.usePerm, this.customReceiveMsg, this.kitCommands, this.kitItems);
        return kit;
    }

    public static Kit getKit(String name) throws InvalidKitException
    {
        String lname = name.toLowerCase(Locale.ENGLISH);
        Kit kit = kitMap.get(lname);
        if (kit == null)
        {
            File file = new File(basics.getFolder(), "kits");
            file.mkdir();
            file = new File(file, lname + ".yml");
            if (file.exists())
            {
                try
                {
                    KitConfiguration config = Configuration.load(KitConfiguration.class, file);
                    kit = config.getKit();
                    kitConfigMap.put(kit, config);
                    kitMap.put(lname, kit);
                }
                catch (Exception ex)
                {
                    throw new InvalidKitException("Could not load the kit " + name, ex);
                }
            }
        }
        return kit;
    }

    public static void saveKit(Kit kit)
    {
        KitConfiguration config = kitConfigMap.get(kit);
        if (config == null)
        {
            config = new KitConfiguration();
            //TODO assign values
        }
        config.save(new File(basics.getFolder(), config.kitName + ".yml"));
    }
}
