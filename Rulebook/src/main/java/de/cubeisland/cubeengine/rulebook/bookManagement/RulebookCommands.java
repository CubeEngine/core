package de.cubeisland.cubeengine.rulebook.bookManagement;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.ContainerCommand;
import de.cubeisland.cubeengine.core.command.annotation.Alias;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.annotation.Param;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.rulebook.Rulebook;

import static de.cubeisland.cubeengine.core.command.exception.IllegalParameterValue.illegalParameter;
import static de.cubeisland.cubeengine.core.command.exception.PermissionDeniedException.denyAccess;
import de.cubeisland.cubeengine.rulebook.RulebookPermissions;
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
        permDefault = PermissionDefault.TRUE,
        max = 0
    )
    public void list(CommandContext context)
    {
        context.sendMessage("rulebook", "&6available Languages:");
        for(String languageName : this.rulebookManager.getLanguages())
        {
            context.sendMessage("&e\t" + languageName);
        }
    }
    
}
