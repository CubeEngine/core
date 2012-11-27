package de.cubeisland.cubeengine.rulebook.bookManagement;

import de.cubeisland.cubeengine.core.bukkit.BookItem;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.ContainerCommand;
import de.cubeisland.cubeengine.core.command.annotation.Alias;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.annotation.Flag;
import de.cubeisland.cubeengine.core.command.annotation.Param;
import static de.cubeisland.cubeengine.core.command.exception.IllegalParameterValue.illegalParameter;
import static de.cubeisland.cubeengine.core.command.exception.InvalidUsageException.invalidUsage;
import static de.cubeisland.cubeengine.core.command.exception.PermissionDeniedException.denyAccess;
import de.cubeisland.cubeengine.core.i18n.Language;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.log.LogLevel;
import de.cubeisland.cubeengine.rulebook.Rulebook;
import de.cubeisland.cubeengine.rulebook.RulebookPermissions;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import java.io.IOException;
import java.util.Set;
import net.minecraft.server.NBTTagCompound;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.permissions.PermissionDefault;

public class RulebookCommands extends ContainerCommand
{
    private final RulebookManager rulebookManager;
    
    public RulebookCommands(Rulebook module) 
    {
        super(module, "rulebook", "shows all commands of the rulebook module");
        this.rulebookManager = module.getRuleBookManager();
        
//        Commands TODO:
//        - modify
//        - get
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
        User user = (context.hasNamed("player")) ? context.getNamed("player", User.class, null) : context.getSenderAsUser("rulebook", "&eI thought you are an analphabet?");
        
        if(context.hasNamed("player") && !RulebookPermissions.COMMAND_GET_OTHER.isAuthorized(context.getSender()))
        {
            denyAccess(context, "rulebook", "&c You have not the permissions to add the rulebook to the inventory of an other player");
        }
        if(user == null)
        {
            illegalParameter(context, "rulebook", "&cUser not found");
        }
        
        if(this.rulebookManager.getLanguages().isEmpty())
        {
            context.sendMessage("rulebook", "&eIt does not exist a rulebook yet");
            return;
        }
        
        String language = context.getIndexed(0, String.class, user.getLanguage());

        if(!this.rulebookManager.contains(language) && context.hasIndexed(0))
        {
            illegalParameter(context, "rulebook", "&eThe language %s is not supported yet.", language);
        }
        if( !this.rulebookManager.contains(language) )
        {
            language = this.getModule().getCore().getI18n().getDefaultLanguage();
            if(!this.rulebookManager.contains(language))
            {
                language = this.rulebookManager.getLanguages().iterator().next();
            }
        }
            
        TIntSet books = this.inventoryRulebookSearching(user.getInventory(), language);
        
        TIntIterator iter = books.iterator();
        while(iter.hasNext())
        {
            user.getInventory().remove(iter.next());            // Doesn't remove the item :/
        }
        
        user.getInventory().addItem( this.rulebookManager.getBook(language) );
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
            if(this.rulebookManager.getLanguages().isEmpty())
            {
                context.sendMessage("&eNo rulebook available at the moment");
            }
            else
            {
                context.sendMessage("rulebook", "&6available languages:");
                for(String languageName : this.rulebookManager.getLanguages())
                {
                    context.sendMessage("&e* " + languageName);
                } 
            }
        }
        else
        {
            context.sendMessage("rulebook", "&6supported languages:");
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
            illegalParameter(context, "rulebook", "&cMore than one or no language is matched with %s", language);
        }
    }
    
    
    @Command
    (
        desc = "adds the book in hand as rulebook of the declared language",
        min = 1,
        max = 1,
        usage = "<language>"
    )
    public void add(CommandContext context)
    {
        User user = context.getSenderAsUser("rulebook", "&eI thought you are an analphabet?");
        
        ItemStack item = user.getItemInHand();
        
        if( !item.getType().equals( Material.WRITTEN_BOOK ) || !item.getType().equals( Material.BOOK_AND_QUILL ))
        {
            invalidUsage(context, "rulebook", "&cI would try it with a book as item in hand");
        }
        
        Set<Language> languages = this.getModule().getCore().getI18n().searchLanguages(context.getIndexed(0, String.class));
        if(languages.size() != 1)
        {
            illegalParameter(context, "rulebook", "&cMore than one or no language is matched with %s", context.getIndexed(0, String.class));
        }
        String language = languages.iterator().next().getName();
        
        if(!this.rulebookManager.contains(language))
        {
            this.rulebookManager.addBook(item, language);
            context.sendMessage("rulebook", "&aRulebook for the language %s was added succesfully", language);
        }
        else
        {
            context.sendMessage("&eThere is already a book with that language.");
        }
    }

    private TIntSet inventoryRulebookSearching(PlayerInventory inventory, String language) 
    {
        TIntSet books = new TIntHashSet();
        
        for(int i = 0; i < inventory.getSize(); i++)
        {
            ItemStack item = inventory.getItem(i);
            
            if(item != null && item.getType().equals(Material.WRITTEN_BOOK))
            {
                BookItem book = new BookItem(item);
                NBTTagCompound tag = book.getTag();
                
                if(tag.getBoolean("rulebook") && language.equalsIgnoreCase( tag.getString("language") ) )
                {
                    books.add(i);
                }
            }
        }
        
        return books;
    }
    
}
