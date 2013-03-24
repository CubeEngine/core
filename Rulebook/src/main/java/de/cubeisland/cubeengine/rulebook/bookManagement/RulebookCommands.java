package de.cubeisland.cubeengine.rulebook.bookManagement;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.ContainerCommand;
import de.cubeisland.cubeengine.core.command.parameterized.Flag;
import de.cubeisland.cubeengine.core.command.parameterized.Param;
import de.cubeisland.cubeengine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.cubeengine.core.command.reflected.Alias;
import de.cubeisland.cubeengine.core.command.reflected.Command;
import de.cubeisland.cubeengine.core.command.sender.CommandSender;
import de.cubeisland.cubeengine.core.i18n.Language;
import de.cubeisland.cubeengine.core.logger.LogLevel;
import de.cubeisland.cubeengine.core.permission.Permission;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.rulebook.Rulebook;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

import static de.cubeisland.cubeengine.core.permission.PermDefault.TRUE;

public class RulebookCommands extends ContainerCommand
{
    private final RulebookManager rulebookManager;

    public RulebookCommands( Rulebook module )
    {
        super( module, "rulebook", "shows all commands of the rulebook module" );
        this.rulebookManager = module.getRuleBookManager();
    }

    @Alias
    ( 
        names = { "getrules", "rules" } 
    )
    @Command
    ( 
        desc = "gets the player the rulebook in the inventory",
        usage = "[language] [Player <name>]",
        params = @Param( names = { "player", "p" }, type = User.class ),
        permDefault = TRUE,
        max = 1 
    )
    public void getRuleBook( ParameterizedContext context )
    {
        CommandSender sender = context.getSender();
        User target = null;
        if( context.hasParam( "player" ) )
        {
            target = context.getUser( "player" );
            if( target == null )
            {
                context.sendMessage( "rulebook", "&cThe given user was not found!" );
            }
        }
        if( target == null )
        {
            if( sender instanceof User )
            {
                target = ( User ) sender;
            }
            else
            {
                context.sendMessage( "rulebook", "&cYou have to specify a user!" );
                return;
            }
        }

        if( sender != target && !context.getSender().hasPermission( Permission.BASE + '.' + context.getCommand().getModule().getId() + ".command.get.other" ) )
        {
            context.sendMessage( "rulebook", "&c You have not the permissions to add the rulebook to the inventory of an other player" );
            return;
        }

        if( this.rulebookManager.getLanguages().isEmpty() )
        {
            context.sendMessage( "rulebook", "&eIt does not exist a rulebook yet" );
            return;
        }

        String language = context.getString( 0, target.getLanguage() );

        if( !this.rulebookManager.contains( language ) && context.hasArg( 0 ) )
        {
            context.sendMessage( "rulebook", "&eThe language %s is not supported yet.", language );
            return;
        }
        if( !this.rulebookManager.contains( language ) )
        {
            language = this.getModule().getCore().getI18n().getDefaultLanguage();
            if( !this.rulebookManager.contains( language ) )
            {
                language = this.rulebookManager.getLanguages().iterator().next();
            }
        }

        TIntSet books = this.inventoryRulebookSearching( target.getInventory(), language );

        TIntIterator iter = books.iterator();
        while( iter.hasNext() )
        {
            target.getInventory().clear( iter.next() );
        }

        target.getInventory().addItem( this.rulebookManager.getBook( language ) );
        target.sendMessage( "rulebook", "&aLot's of fun with your rulebook." );
        if( !books.isEmpty() )
        {
            target.sendMessage( "rulebook", "&aYour old was removed" );
        }
    }

    @Alias( names = "listrules" )
    @Command( desc = "list all available languages of the rulebooks.",
              flags =
    {
        @Flag( longName = "supported",
               name = "s" )
    },
              permDefault = TRUE,
              usage = "[-supported]",
              max = 0 )
    public void list( ParameterizedContext context )
    {
        if( !context.hasFlag( "s" ) )
        {
            if( this.rulebookManager.getLanguages().isEmpty() )
            {
                context.sendMessage( "&eNo rulebook available at the moment" );
            }
            else
            {
                context.sendMessage( "rulebook", "&6available languages:" );
                for( String languageName : this.rulebookManager.getLanguages() )
                {
                    context.sendMessage( "&e* " + languageName );
                }
            }
        }
        else
        {
            context.sendMessage( "rulebook", "&6supported languages:" );
            for( Language language :
                    this.getModule().getCore().getI18n().getLanguages() )
            {
                context.sendMessage( "&e* " + language.getName() );
            }
        }

    }

    @Alias( names = "removerules" )
    @Command( desc = "removes the declared language and languagefiles!",
              min = 1,
              max = 1,
              usage = "<language>" )
    public void remove( CommandContext context )
    {
        String language = context.getString( 0 );

        Set<Language> languages = this.getModule().getCore().getI18n().searchLanguages( language );

        if( languages.size() == 1 )
        {
            try
            {
                this.rulebookManager.removeBook( languages.iterator().next().getName() );
                context.sendMessage( "rulebook", "&aThe languagefiles of %s was deleted", language );
            }
            catch ( IOException ex )
            {
                this.getModule().getLogger().log( LogLevel.ERROR, "Error by deleting the files.", ex );
            }
        }
        else
        {
            context.sendMessage( "rulebook", "&cMore than one or no language is matched with %s", language );
        }
    }

    @Alias( names = "modifyrules" )
    @Command(
    desc = "modified the rulebook of the declared language with the book in hand",
              usage = "<language>",
              min = 1,
              max = 1 )
    public void modify( CommandContext context )
    {
        if( !(context.getSender() instanceof User) )
        {
            context.sendMessage( "rulebook", "&eYou are not able to write, aren't you?" );
        }
        User user = ( User ) context.getSender();

        ItemStack item = user.getItemInHand();

        if( !item.getType().equals( Material.WRITTEN_BOOK ) && !item.getType().equals( Material.BOOK_AND_QUILL ) )
        {
            context.sendMessage( "rulebook", "&cI would try it with a book as item in hand" );
            return;
        }

        final String langArg = context.getString( 0 );
        Set<Language> languages = this.getModule().getCore().getI18n().searchLanguages( langArg );
        if( languages.size() != 1 )
        {
            context.sendMessage( "rulebook", "&cMore than one or no language is matched with %s", langArg );
            return;
        }
        String language = languages.iterator().next().getName();

        if( this.rulebookManager.contains( language ) )
        {
            try
            {
                if( this.rulebookManager.removeBook( language ) )
                {
                    this.rulebookManager.addBook( item, language );
                    context.sendMessage( "rulebook", "&aThe rulebook %s was succesful modified.", language );
                }
                else
                {
                    context.sendMessage( "rulebook", "&eAn error ocurred by deleting the old rulebook" );
                }
            }
            catch ( IOException ex )
            {
                this.getModule().getLogger().log( LogLevel.ERROR, "Error by deleting the files.", ex );
            }
        }
        else
        {
            context.sendMessage( "rulebook", "&cYou can't modify a book which does not exist." );
        }
    }

    @Alias( names = "addrules" )
    @Command(
    desc = "adds the book in hand as rulebook of the declared language",
              min = 1,
              max = 1,
              usage = "<language>" )
    public void add( CommandContext context )
    {
        if( !(context.getSender() instanceof User) )
        {
            context.sendMessage( "rulebook", "&eI thought you are an analphabet?" );
        }
        User user = ( User ) context.getSender();

        ItemStack item = user.getItemInHand();

        if( !item.getType().equals( Material.WRITTEN_BOOK ) && !item.getType().equals( Material.BOOK_AND_QUILL ) )
        {
            context.sendMessage( "rulebook", "&cI would try it with a book as item in hand" );
            return;
        }

        Set<Language> languages = this.getModule().getCore().getI18n().searchLanguages( context.getString( 0 ) );
        if( languages.size() != 1 )
        {
            context.sendMessage( "rulebook", "&cMore than one or no language is matched with %s", context.getString( 0 ) );
            return;
        }
        String language = languages.iterator().next().getName();

        if( !this.rulebookManager.contains( language ) )
        {
            this.rulebookManager.addBook( item, language );
            context.sendMessage( "rulebook", "&aRulebook for the language %s was added succesfully", language );
        }
        else
        {
            context.sendMessage( "&eThere is already a book with that language." );
        }
    }

    private TIntSet inventoryRulebookSearching( PlayerInventory inventory, String language )
    {
        TIntSet books = new TIntHashSet();

        Set<Language> languages = this.getModule().getCore().getI18n().searchLanguages( language );
        if( languages.size() != 1 )
        {
            return books;
        }
        language = languages.iterator().next().getName();

        for( int i = 0; i < inventory.getSize(); i++ )
        {
            ItemStack item = inventory.getItem( i );

            if( item != null && item.getType().equals( Material.WRITTEN_BOOK ) )
            {
                List<String> lore = item.getItemMeta().getLore();
                if( lore != null )
                {
                    if( lore.size() > 0 && language.equalsIgnoreCase( lore.get( 0 ) ) )
                    {
                        books.add( i );
                    }
                }
            }
        }

        return books;
    }
}
