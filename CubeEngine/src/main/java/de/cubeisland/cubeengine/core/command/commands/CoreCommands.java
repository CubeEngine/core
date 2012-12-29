package de.cubeisland.cubeengine.core.command.commands;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.CoreConfiguration;
import de.cubeisland.cubeengine.core.bukkit.BukkitCore;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.ContainerCommand;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.annotation.Flag;
import de.cubeisland.cubeengine.core.i18n.Language;
import de.cubeisland.cubeengine.core.user.User;

import java.util.Set;

import static de.cubeisland.cubeengine.core.command.exception.IllegalParameterValue.illegalParameter;
import static de.cubeisland.cubeengine.core.command.exception.InvalidUsageException.blockCommand;
import static de.cubeisland.cubeengine.core.command.exception.InvalidUsageException.paramNotFound;
import static de.cubeisland.cubeengine.core.command.exception.PermissionDeniedException.denyAccess;
import java.util.HashMap;
import java.util.Map;

public class CoreCommands extends ContainerCommand
{
    private final BukkitCore core;

    public CoreCommands(Core core)
    {
        super(core.getModuleManager().getCoreModule(), "cubeengine", "These are the basic commands of the CubeEngine.", "ce");
        this.core = (BukkitCore) core;
    }

    @Command(desc = "Disables the CubeEngine")
    public void disable(CommandContext context)
    {
        this.core.getServer().getPluginManager().disablePlugin(this.core);
    }

    @Command(names =
    {
        "setpassword", "setpw"
    }, desc = "Sets your password.", min = 1, max = 2, usage = "<password> [player]")
    public void setPassword(CommandContext context)
    {
        User sender = context.getSenderAsUser();
        if (sender == null && !context.hasIndexed(1))
        {
            illegalParameter(context, "core", "&cPlayer missing! Can not set password.");
        }
        User user = sender;
        if (context.hasIndexed(1))
        {
            if (!CommandPermissions.COMMAND_SETPASSWORD_OTHER.isAuthorized(context.getSender()))
            {
                denyAccess(context, "basics", "&cYou are not allowed to change the password of an other user!");
            }
            user = context.getUser(1);
            if (user == null)
            {
                blockCommand(context, "core", "&cUser %s not found!", context.getString(1));
            }
        }
        int strength = this.computePasswordStrength(context.getString(0));
        if (strength < 28)
        {
            context.sendMessage("core", "&cYour passwords strength is too weak! \n&eTry using at least 8 letters and numbers!");
            return;
        }
        else if (strength < 36)
        {
            context.sendMessage("core", "&eYour passwords strength is weak!");
        }
        else if (strength < 60)
        {
            context.sendMessage("core", "&aYour passwords strength is ok!");
        }
        else if (strength < 128)
        {
            context.sendMessage("core", "&aYour passwords strength is good!");
        }
        else
        {
            context.sendMessage("core", "&aYour passwords strength is very good!");
        }
        user.setPassword(context.getString(0));
        context.sendMessage("core", "&aPassword set!");
    }

    @Command(names =
    {
        "clearpassword", "clearpw"
    }, desc = "Clears your password.", max = 1, usage = "[<player>|-a]", flags =
    @Flag(longName = "all", name = "a"))
    public void clearPassword(CommandContext context)
    {
        if (context.hasFlag("a"))
        {
            if (CommandPermissions.COMMAND_CLEARPASSWORD_ALL.isAuthorized(context.getSender()))
            {
                this.getModule().getUserManager().resetAllPasswords();
                for (User user1 : this.getModule().getUserManager().getLoadedUsers())
                {
                    user1.passwd = null; //update loaded users
                }
                context.sendMessage("core", "&all passwords resetted!");
            }
            else
            {
                denyAccess(context, "core", "&cYou are not allowed to clear all passwords!");
            }
            return;
        }
        else
        {
            User user;
            if (context.hasIndexed(0))
            {
                if (!CommandPermissions.COMMAND_CLEARPASSWORD_OTHER.isAuthorized(context.getSender()))
                {
                    denyAccess(context, "core", "&cYou are not allowed to clear the password of other users!");
                }
                user = context.getUser(0);
            }
            else
            {
                user = context.getSenderAsUser("core", "&cYou do not need an ingame password as console!");
            }
            if (user == null)
            {
                paramNotFound(context, "core", "&cUser &c not found!");
                return;
            }
            user.resetPassword();
        }
        context.sendMessage("core", "&aPassword reset!");
    }

    @Command(desc = "Logs you in with your password!", usage = "<password>", min = 1, max = 1)
    public void login(CommandContext context)
    {
        User sender = context.getSenderAsUser("core", "&eYou don't have to login to yourself!");
        if (sender.isLoggedIn())
        {
            blockCommand(context, "core", "&aYou are already logged in!");
        }
        boolean isLoggedIn = sender.login(context.getString(0));
        if (isLoggedIn)
        {
            context.sendMessage("core", "&aYou logged in successfully!");
        }
        else
        {
            context.sendMessage("core", "&cWrong password!");
        }
    }

    @Command(desc = "Logs you out!", max = 0)
    public void logout(CommandContext context)
    {
        User sender = context.getSenderAsUser("core", "&eJust close the console!");
        if (!sender.isLoggedIn())
        {
            blockCommand(context, "core", "&aYou were not logged in!");
        }
        sender.logout();
        context.sendMessage("core", "&aYou are now logged out!");
    }

    @Command(desc = "Displays or changes your language!", usage = "[<language>|reset]", max = 1)
    public void language(CommandContext context)
    {
        User sender = context.getSenderAsUser();
        if (context.hasIndexed(0))
        {
            if (context.getString(0).equalsIgnoreCase("reset"))
            {
                if (sender != null)
                {
                    sender.setLanguage(null);
                    this.core.getUserManager().update(sender);
                    context.sendMessage("core", "&aYour language is now reset to the one selected in your client!");
                }
                else
                {
                    context.sendMessage("core", "&c");
                    return;
                }
            }

            Language language = null;
            Set<Language> results = context.getCore().getI18n().searchLanguages(context.getString(0), 2);
            if (results.size() == 1)
            {
                language = results.iterator().next();
            }
            else if (results.size() > 1)
            {
                context.sendMessage("core", "&eThe given language name matched more than one language!");
                context.sendMessage("core", "The following languages matched:");
                for (Language result : results)
                {
                    context.sendMessage(" - " + result.getName() + " (" + result.getLocalName() + ")");
                }
                return;
            }

            if (language == null)
            {
                context.sendMessage("core", "&cUnknown language!");
                return;
            }

            if (sender == null)
            {
                CoreConfiguration config = this.core.getConfiguration();
                config.defaultLanguage = language.getCode();
                config.save();

                this.core.getI18n().setDefaultLanguage(language.getCode());
                context.sendMessage("core", "&aDefault language is now set to &e%s&a (&e%s&a)!", language.getName(), language.getLocalName());
            }
            else
            {
                sender.setLanguage(language);
                this.core.getUserManager().update(sender);
                context.sendMessage("core", "&aYou language is now set to &e%s&a (&e%s&a)!", language.getName(), language.getLocalName());
            }
        }
        else
        {
            if (sender == null)
            {
                Language lang = core.getI18n().getLanguage(core.getI18n().getDefaultLanguage());
                context.sendMessage("basics", "&eThe default language is &e%s&a (&e%s&a).", lang.getName(), lang.getLocalName());
            }
            else
            {
                Language lang = core.getI18n().getLanguage(sender.getLanguage());
                context.getSenderAsUser("basics", "&eYour language is &e%s&a (&e%s&a).", lang.getName(), lang.getLocalName());
            }
        }
    }

    private int computePasswordStrength(String pass)
    {
        double strengthPerChar = Math.log(getCharSetSize(pass) / Math.log(2));
        double strength = 0;
        Map<Character, Double> chars = new HashMap<Character, Double>();
        for (char c : pass.toCharArray())
        {
            if (chars.containsKey(c))
            {
                double loweredStrengthPerChar = chars.get(c);
                loweredStrengthPerChar *= 0.75;
                strength += loweredStrengthPerChar;
                chars.put(c, loweredStrengthPerChar);
            }
            else
            {
                chars.put(c, strengthPerChar);
                strength += strengthPerChar;
            }
        }

        return (int) strength;
    }

    private int getCharSetSize(String pass)
    {
        boolean lower = true, upper = true, number = true, symbols1 = true, symbols2 = true, space = true, symbols3 = true;
        int chars = 0;
        for (char c : pass.toCharArray())
        {
            if (lower && "abcdefghijklmnopqrstuvwxyz".indexOf(c) >= 0)
            {
                chars += 26;
                lower = false;
            }
            else if (upper && "ABCDEFGHIJKLMNOPQRSTUVWXYZ".indexOf(c) >= 0)
            {
                chars += 26;
                upper = false;
            }
            else if (number && "0123456789".indexOf(c) >= 0)
            {
                chars += 10;
                number = false;
            }
            else if (symbols1 && "!@#$%^&*()".indexOf(c) >= 0)
            {
                chars += 10;
                symbols1 = false;
            }
            else if (symbols2 && "`~-_=+[{]}\\|;:'\",<.>/?".indexOf(c) >= 0)
            {
                chars += 20;
                symbols2 = false;
            }
            else if (space && c == ' ')
            {
                chars += 1;
                space = false;
            }
            else if (symbols3 && (c < ' ' || c > '~'))
            {
                chars += 32 + 128;
                symbols3 = false;
            }
        }
        return chars;
    }
}
