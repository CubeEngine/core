package de.cubeisland.cubeengine.rulebook.bookManagement;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.annotation.Param;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.rulebook.Rulebook;

import static de.cubeisland.cubeengine.core.command.exception.IllegalParameterValue.illegalParameter;

public class RuleBookCommands 
{
    Rulebook module;
    
    public RuleBookCommands(Rulebook module)
    {
        this.module = module;
    }
    
    @Command
    (
        desc = "gets the player the rulebook in the inventory",
        usage = "[language] [Player <name>]",
        params = @Param(names = { "player" , "p"}, type = User.class),
        max = 1
    )
    public void get(CommandContext context)
    {
        User user = 
                (context.hasNamed("player")) ?
                context.getNamed("player", User.class, null) :
                context.getSenderAsUser();
        
        if(user == null)
        {
            illegalParameter(context, "rulebook", "User not found");
        }
        
        String language = user.getLanguage();
        RuleBookManager manager = this.module.getRuleBookManager();
        
        if(!manager.getLanguages().isEmpty())
        {
            if(!manager.contains(language))
            {
                language = this.module.getCore().getI18n().getDefaultLanguage();
                if(!manager.contains(language))
                {
                    for(String lang : manager.getLanguages())
                    {
                        language = lang;
                        break;
                    }
                }
            }
            user.getInventory().addItem( this.module.getRuleBookManager().getBook(language) );
        }
        else
        {
            context.sendMessage("rulebook", "You had not add a book yet");
        }
    }
}
