package de.cubeisland.cubeengine.basics.command.moderation.spawnmob;

import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.CommandResult;
import de.cubeisland.cubeengine.core.command.chatcommand.ChatCommand;
import de.cubeisland.cubeengine.core.command.chatcommand.ChatCommandContext;
import de.cubeisland.cubeengine.core.command.chatcommand.ChatCommandContextFactory;
import de.cubeisland.cubeengine.core.command.parameterized.CommandFlag;
import de.cubeisland.cubeengine.core.command.parameterized.CommandParameter;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.matcher.Match;
import gnu.trove.map.hash.TLongObjectHashMap;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class AdvancedSpawnMob extends ChatCommand<Basics>
{
    public AdvancedSpawnMob(Basics module)
    {
        super(module, new ChatCommandContextFactory());
        this.getContextFactory()
                .addFlag(new CommandFlag("exit", "exit"))
                .addFlag(new CommandFlag("spawn", "spawnmob"))
                .addParameter(new CommandParameter("mob", EntityType.class))
                .addParameter(new CommandParameter("amount", Integer.class))
                .addFlag(new CommandFlag("charge", "charged"))
                .addFlag(new CommandFlag("baby", "baby"))
                .addFlag(new CommandFlag("saddle", "saddled"))
                .addFlag(new CommandFlag("angry", "angry"))
                .addFlag(new CommandFlag("sitting", "sitting"))
                .addFlag(new CommandFlag("tame", "tame"))
                .addFlag(new CommandFlag("villager", "villagerzombie"))
                .addParameter(new CommandParameter("color", new String[]{"sheepcolor"}, DyeColor.class))
                .addParameter(new CommandParameter("size", new String[]{"slimesize"}, String.class))
                .addParameter(new CommandParameter("prof", new String[]{"profession"}, Villager.Profession.class))
                .addParameter(new CommandParameter("endermanitem", ItemStack.class))
                .addParameter(new CommandParameter("equip-hand", ItemStack.class))
                .addParameter(new CommandParameter("equip-helmet", ItemStack.class))
                .addParameter(new CommandParameter("equip-boots", ItemStack.class))
                .addParameter(new CommandParameter("equip-chestplate", ItemStack.class))
                .addParameter(new CommandParameter("equip-leggings", ItemStack.class))
                .addParameter(new CommandParameter("hp", Integer.class))
                .addFlag(new CommandFlag("here", "here"))
                .addParameter(new CommandParameter("nearby", User.class))
                .addFlag(new CommandFlag("info", "info"))
                .addFlag(new CommandFlag("clear", "clear"))//TODO
        //TODO riding mobs
        ;

    }

    private TLongObjectHashMap<SpawningData> spawningDatas = new TLongObjectHashMap<SpawningData>();

    @Override
    public void addUser(User user) {
        super.addUser(user);
        SpawningData spawningData;
        this.spawningDatas.put(user.key,spawningData= new SpawningData());
        spawningData.location = user.getTargetBlock(null, 200).getLocation().add(new Vector(0, 1, 0));
    }

    @Override
    public void removeUser(User user) {
        super.removeUser(user);
        this.spawningDatas.remove(user.key);
    }

    @Override
    public CommandResult run(CommandContext runContext) throws Exception
    {
        ChatCommandContext context = (ChatCommandContext)runContext;
        User user = (User) context.getSender();
        SpawningData spawningData = this.spawningDatas.get(user.key);
        if (context.hasParam("mob"))
        {
            EntityType entityType = context.getParam("mob",EntityType.ARROW);
            if (entityType.isAlive())
            {
                spawningData.entityType = entityType;
            }
            else
            {
                context.sendMessage("basics","&6%s &cis not a living Entity!", Match.entity().getNameFor(entityType));
            }
        }
        if (context.hasFlag("baby"))
        {
            spawningData.add(EntityDataChanger.BABY, true);
        }
        if (context.hasFlag("charged"))
        {
            spawningData.add(EntityDataChanger.POWERED, true);
        }
        if (context.hasFlag("saddle"))
        {
            spawningData.add(EntityDataChanger.PIGSADDLE, true);
        }
        if (context.hasFlag("angry"))
        {
            spawningData.add(EntityDataChanger.ANGRY, true);
        }
        if (context.hasFlag("sitting"))
        {
            spawningData.add(EntityDataChanger.SITTING, true);
        }
        if (context.hasFlag("tame"))
        {
            spawningData.add(EntityDataChanger.TAME,user);
        }
        if (context.hasFlag("villager"))
        {
            spawningData.add(EntityDataChanger.VILLAGER_ZOMBIE, true);
        }
        if (context.hasParam("color"))
        {
            spawningData.add(EntityDataChanger.SHEEP_COLOR,context.getParam("color",DyeColor.WHITE));
        }
        if (context.hasParam("size"))
        {
            String match = Match.string().matchString(context.getString("size"), "tiny", "small", "big");
            try
            {
                int size = "tiny".equals(match) ? 0
                        : "small".equals(match) ? 2
                        : "big".equals(match) ? 4
                        : Integer.parseInt(context.getString("size"));
                spawningData.add(EntityDataChanger.SLIME_SIZE,size);
            }
            catch (NumberFormatException e)
            {
                context.sendMessage("basics", "&eThe slime-size has to be a number or tiny, small or big!");
            }
        }
        if (context.hasParam("prof"))
        {
            spawningData.add(EntityDataChanger.VILLAGER_PROFESSION,context.getParam("prof", Villager.Profession.FARMER));
        }
        if (context.hasParam("endermanitem"))
        {
            spawningData.add(EntityDataChanger.ENDERMAN_ITEM,context.getParam("endermanitem",AIR));
        }
        if (context.hasParam("equip-hand"))
        {
            spawningData.add(EntityDataChanger.EQUIP_ITEMINHAND,context.getParam("equip-hand",AIR));
        }
        if (context.hasParam("equip-helmet"))
        {
            spawningData.add(EntityDataChanger.EQUIP_HELMET,context.getParam("equip-helmet",AIR));
        }
        if (context.hasParam("equip-boots"))
        {
            spawningData.add(EntityDataChanger.EQUIP_BOOTS,context.getParam("equip-boots",AIR));
        }
        if (context.hasParam("equip-chestplate"))
        {
            spawningData.add(EntityDataChanger.EQUIP_CHESTPLATE,context.getParam("equip-chestplate",AIR));
        }
        if (context.hasParam("equip-leggings"))
        {
            spawningData.add(EntityDataChanger.EQUIP_LEGGINGS,context.getParam("equip-leggings",AIR));
        }
        if (context.hasParam("hp"))
        {
            spawningData.add(EntityDataChanger.HP,(Integer)context.getParam("hp",null));
        }
        if (context.hasFlag("here"))
        {
            user.getLocation(spawningData.location);
        }
        if (context.hasParam("nearby"))
        {
            User nearby = context.getParam("nearby",null);
            if (nearby == null || !nearby.isOnline())
            {
                context.sendMessage("basics","&cUser %s not found!",context.getString("nearby"));
            }
            else
            {
                nearby.getLocation(spawningData.location);
            }
        }
        if (context.hasParam("amount"))
        {
            Integer amount = context.getParam("amount",0);
            if (amount == 0)
            {
                context.sendMessage("basics","&cInvalid amount!");
                amount = 1;
            }
            spawningData.amount = amount;
        }
        if (context.hasFlag("info"))
        {
            spawningData.showInfo(user);
        }
        if (context.hasFlag("spawn"))
        {
            spawningData.doSpawn();
        }
        if (context.hasFlag("exit"))
        {
            this.removeUser(user);
            return null;
        }
        return null;
    }

    private static final ItemStack AIR = new ItemStack(Material.AIR);
}
