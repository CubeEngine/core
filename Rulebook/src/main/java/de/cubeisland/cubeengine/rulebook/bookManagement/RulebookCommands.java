package de.cubeisland.cubeengine.rulebook.bookManagement;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.ContainerCommand;
import de.cubeisland.cubeengine.core.command.annotation.Alias;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.annotation.Flag;
import de.cubeisland.cubeengine.core.command.annotation.Param;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.rulebook.Rulebook;

import static de.cubeisland.cubeengine.core.command.exception.IllegalParameterValue.illegalParameter;
import static de.cubeisland.cubeengine.core.command.exception.PermissionDeniedException.denyAccess;
import de.cubeisland.cubeengine.core.i18n.Language;
import de.cubeisland.cubeengine.core.util.log.LogLevel;
import de.cubeisland.cubeengine.rulebook.RulebookPermissions;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionDefault;

public class RulebookCommands extends ContainerCommand
{
    private final RulebookManager rulebookManager;
    
    public RulebookCommands(Rulebook module) 
    {
        super(module, "rulebook", "shows all commands of the rulebook module");
        this.rulebookManager = module.getRuleBookManager();
    }
    
    @Alias(names = {"getrulebook" , "rules"})
    @Command
    (
        desc = "gets the player the rulebook in the inventory",
        usage = "[language] [Player <name>]",
        params = @Param(names = { "player" , "p"}, type = User.class),
        permDefault = PermissionDefault.TRUE,
        max = 1
    )
    public void get(CommandContext context)
    {
        User user = (context.hasNamed("player")) ? context.getNamed("player", User.class, null) : context.getSenderAsUser("rulebook", "&cI can't export a book.");
        
        if(user == null)
        {
            illegalParameter(context, "rulebook", "&cUser not found");
        }
        
        if(!this.rulebookManager.getLanguages().isEmpty())
        {
            String language = context.getIndexed(0, String.class, user.getLanguage());
            
            if( !RulebookPermissions.COMMAND_GET_OTHER.isAuthorized(context.getSenderAsUser()) && context.hasNamed("player") )
            {
                denyAccess(context, "rulebook", "&c You have not the permissions to add the rulebook to the inventory of an other player");
            }
            
            if(!this.rulebookManager.contains(language) && context.hasIndexed(0))
            {
                illegalParameter(context, "rulebook", "&eThe language %s is not supported yet.", language);
            }
            else if( !this.rulebookManager.contains(language) )
            {
                language = this.getModule().getCore().getI18n().getDefaultLanguage();
                if(!this.rulebookManager.contains(language))
                {
                    language = this.rulebookManager.getLanguages().iterator().next();
                }
            }
            
            user.getInventory().addItem( this.rulebookManager.getBook(language) );
        }
        else
        {
            context.sendMessage("rulebook", "&eIt doesn't exist a rulebook yet.");
        }
    }
    
    @Command
    (
        desc = "list all available languages of the rulebooks.",
        flags = {@Flag(longName = "supported", name = "s")},
        permDefault = PermissionDefault.TRUE,
        usage = "[-supported]",
        max = 0
    )
    public void list(CommandContext context)
    {
        if(!context.hasFlag("s"))
        {
            context.sendMessage("rulebook", "&6available Languages:");
            for(String languageName : this.rulebookManager.getLanguages())
            {
                context.sendMessage("&e* " + languageName);
            }   
        }
        else
        {
            context.sendMessage("rulebook", "&6supported Languages:");
            for(Language language : this.getModule().getCore().getI18n().getLanguages())
            {
                context.sendMessage("&e* " + language.getName());
            }
        }
        
    }
    
    @Command
    (
        desc = "removes the declared language and languagefiles!",
        min = 1,
        max = 1,
        usage = "<language>"
    )
    public void remove(CommandContext context)
    {
        String language = context.getIndexed(0, String.class);
        
        Set<Language> languages = this.getModule().getCore().getI18n().searchLanguages(language);
        
        if(languages.size() == 1)
        {
            try 
            {
                this.rulebookManager.removeBook(languages.iterator().next().getName());
                context.sendMessage("rulebook", "&aThe languagefiles of %s was deleted", language);
            } 
            catch (IOException ex) 
            {
                this.getModule().getLogger().log(LogLevel.ERROR, "Error by deleting the files.", ex);
            }
        }
        else
        {
            illegalParameter(context, "rulebook", "&cMore than one language is matched with %s", language);
        }
    }
    
    
    @Command
    (
        desc = "sets the book in hand as rulebook of the declared language",
        min = 1,
        max = 1,
        usage = "<language>"
    )
    public void set(CommandContext context)
    {
        User user = context.getSenderAsUser("rulebook", "&eI thought you are analphabetics?");
        
        ItemStack item = user.getItemInHand();
        if( item.getType().equals( Material.WRITTEN_BOOK ) || item.getType().equals( Material.BOOK_AND_QUILL ) )
        {
            Set<Language> languages = this.getModule().getCore().getI18n().searchLanguages(context.getIndexed(0, String.class));
            if(languages.size() != 1)
            {
                context.sendMessage("rulebook", "I do not know which language you mean with %s exactly", context.getIndexed(0, String.class));
            }
            String language = languages.iterator().next().getName();
            if(!this.rulebookManager.contains(language))
            {
                this.rulebookManager.addBook(item, language);
                context.sendMessage("rulebook", "&aRulebook for the language %s was added succesfully", language);
            }
            else
            {
                context.sendMessage("&eThe ability to modify a book wasn't added yet");
            }
        }
        else
        {
            context.sendMessage("rulebook", "&cI would try it with a book as item in hand");
        }
    }
    
}
