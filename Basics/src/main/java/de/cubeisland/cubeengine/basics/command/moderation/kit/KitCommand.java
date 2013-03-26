package de.cubeisland.cubeengine.basics.command.moderation.kit;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.inventory.ItemStack;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.CommandResult;
import de.cubeisland.cubeengine.core.command.ContainerCommand;
import de.cubeisland.cubeengine.core.command.parameterized.Flag;
import de.cubeisland.cubeengine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.cubeengine.core.command.reflected.Alias;
import de.cubeisland.cubeengine.core.command.reflected.Command;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.FileUtil;
import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.basics.BasicsPerm;

public class KitCommand extends ContainerCommand
{
    public KitCommand(Basics module)
    {
        super(module, "kit", "Manages item-kits");
        this.module = module;
    }
    private Basics module;

    @Override
    public CommandResult run(CommandContext context) throws Exception
    {
        if (context.hasArg(0))
        {
            this.give((ParameterizedContext)context);//TODO this works but not used as intended!?
            return null;
        }
        else
        {
            return super.run(context);
        }
    }

    @Command(desc = "Creates a new kit with the items in your inventory.",
            flags = @Flag(longName = "toolbar", name = "t"),
            usage = "<kitName> [-toolbar]", min = 1, max = 1)
    public void create(ParameterizedContext context)
    {
        User sender = null;
        if (context.getSender() instanceof User)
        {
            sender = (User)context.getSender();
        }
        if (sender == null)
        {
            context.sendMessage("basics", "&cJust log in or use the config!");
            return;
        }
        List<KitItem> itemList = new ArrayList<KitItem>();
        if (context.hasFlag("t"))
        {
            ItemStack[] items = sender.getInventory().getContents();
            for (int i = 0; i <= 8; ++i)
            {
                if (items[i] == null || items[i].getTypeId() == 0)
                {
                    break;
                }
                itemList.add(
                        new KitItem(items[i].getType(),
                            items[i].getDurability(),
                            items[i].getAmount(),
                            items[i].getItemMeta().getDisplayName()));
            }
        }
        else
        {
            for (ItemStack item : sender.getInventory().getContents())
            {
                if (item == null || item.getTypeId() == 0)
                {
                    break;
                }
                itemList.add(
                        new KitItem(item.getType(),
                            item.getDurability(),
                            item.getAmount(),
                            item.getItemMeta().getDisplayName()));
            }
        }
        Kit kit = new Kit(this.module,context.getString(0), false, 0, -1, true, "", new ArrayList<String>(), itemList);
        if (!FileUtil.isValidFileName(kit.getKitName()))
        {
            context.sendMessage("basics", "&6%s &cis is not a valid name! Do not use characters like *, | or ?", kit.getKitName());
            return;
        }
        module.getKitManager().saveKit(kit);
        if (kit.getPermission() != null)
        {
            module.getCore().getPermissionManager().registerPermission(module,kit.getPermission());
        }
        context.sendMessage("basics", "&aCreated the &6%s &akit!", kit.getKitName());
    }

    @Alias(names = "kit")
    @Command(desc = "Gives a kit of items.", usage = "<kitname> [player]", min = 1, max = 2, flags = {
        @Flag(longName = "all", name = "a"),
        @Flag(longName = "force", name = "f")
    })
    public void give(ParameterizedContext context)
    {
        String kitname = context.getString(0);
        User user = null;
        Kit kit = module.getKitManager().getKit(kitname);
        boolean force = false;
        if (context.hasFlag("f") && BasicsPerm.COMMAND_KIT_GIVE_FORCE.isAuthorized(context.getSender()))
        {
            force = true;
        }
        if (kit == null)
        {
            context.sendMessage("basics", "&cKit &6%s &cnot found!", kitname);
            return;
        }
        if (context.hasFlag("a"))
        {
            boolean gaveKit = false;
            int kitNotreceived = 0;
            for (User receiver : module.getCore().getUserManager().getOnlineUsers())
            {
                try
                {
                    if (kit.give(context.getSender(), receiver, force))
                    {
                        if (receiver.getName().equals(context.getSender().getName()))
                        {
                            context.sendMessage("basics", "&aReceived the &6%s &akit!", kit.getKitName());
                        }
                        else
                        {
                            context.sendMessage("basics", "&aYou gave &2%s &athe &6%s &akit!", receiver.getName(), kit.getKitName());
                            receiver.sendMessage("basics", "&aReceived the &6%s &akit. Enjoy it!", kit.getKitName());
                        }
                        gaveKit = true;
                    }
                }
                catch (Exception ex)
                {
                    kitNotreceived++;
                }
            }
            if (!gaveKit)
            {
                context.sendMessage("basics", "&cNo one received the kit!");
            }
            else if (kitNotreceived > 0)
            {
                context.sendMessage("basics", "&c%d players did not receive a kit!");
            }
        }
        else
        {
            boolean other = false;
            if (context.hasArg(1))
            {
                user = context.getUser(1);
                other = true;
            }
            else if (context.getSender() instanceof User)
            {
                user = (User)context.getSender();
            }
            if (user == null)
            {
                context.sendMessage("basics", "&cUser %s &cnot found!", context.getString(0));
                return;
            }
            if (kit.give(context.getSender(), user, force))
            {
                if (!other)
                {
                    if (kit.getCustomMessage().equals(""))
                    {
                        context.sendMessage("basics", "&aReceived the &6%s &akit. Enjoy it!", kit.getKitName());
                    }
                    else
                    {
                        context.sendMessage(kit.getCustomMessage());
                    }
                }
                else
                {
                    context.sendMessage("basics", "&aYou gave &2%s &athe &6%s &akit!", user.getName(), kit.getKitName());
                    if (kit.getCustomMessage().equals(""))
                    {
                        user.sendMessage("basics", "&aReceived the &6%s &akit. Enjoy it!", kit.getKitName());
                    }
                    else
                    {
                        user.sendMessage(kit.getCustomMessage());
                    }
                }
            }
            else
            {
                if (other)
                {
                    context.sendMessage("basics", "&2%s &ehas not enough inventory-space for this kit!", user.getName());
                }
                else
                {
                    context.sendMessage("basics", "&eYou don't have enough inventory-space for this kit!");
                }
            }
        }
    }
}
