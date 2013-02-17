package de.cubeisland.cubeengine.basics.command.moderation.kit;

import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.config.annotations.Codec;
import de.cubeisland.cubeengine.core.config.annotations.Comment;
import de.cubeisland.cubeengine.core.config.annotations.Option;
import de.cubeisland.cubeengine.core.util.time.Duration;

import java.util.LinkedList;
import java.util.List;

@Codec("yml")
public class KitConfiguration extends Configuration
{
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

    public Kit getKit(Basics module)
    {
        Kit kit = new Kit(module, this.kitName, this.giveOnFirstJoin, this.limitUsage, this.limitUsageDelay.toMillis(), this.usePerm, this.customReceiveMsg, this.kitCommands, this.kitItems);
        return kit;
    }


}
