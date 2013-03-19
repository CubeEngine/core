package de.cubeisland.cubeengine.rulebook.bookManagement;

import de.cubeisland.cubeengine.core.i18n.Language;
import de.cubeisland.cubeengine.core.logger.LogLevel;
import de.cubeisland.cubeengine.core.util.StringUtils;
import de.cubeisland.cubeengine.rulebook.Rulebook;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class RulebookManager
{
    private final Rulebook module;
    private Map<Locale, String[]> rulebooks;

    public RulebookManager( Rulebook module )
    {
        this.module = module;

        this.rulebooks = new HashMap<Locale, String[]>();

        for( File book : RuleBookFile.getLanguageFiles( this.module.getFolder() ) )
        {
            Language language = this.module.getCore().getI18n().searchLanguages( StringUtils.stripFileExtension( book.getName() ) ).iterator().next();
            try
            {
                rulebooks.put( language.getLocale(), RuleBookFile.convertToPages( book ) );
            }
            catch ( IOException ex )
            {
                this.module.getLogger().log( LogLevel.ERROR, "Can't read the file {0}", book.getName() );
            }
        }
    }

    public Collection<Locale> getLocales()
    {
        return this.rulebooks.keySet();
    }

    public boolean contains( Locale locale )
    {
        return this.contains( locale, 2 );
    }

    public boolean contains( Locale locale, int editDistance )
    {
        Set<Language> languages = this.module.getCore().getI18n().searchLanguages( locale.toString(), editDistance );
        return languages.size() == 1 && this.rulebooks.containsKey( languages.iterator().next().getLocale() );
    }

    public String[] getPages( String language )
    {
        return this.getPages( language, 2 );
    }

    public String[] getPages( String language, int editDistance )
    {
        Set<Language> languages = this.module.getCore().getI18n().searchLanguages( language, editDistance );
        if( languages.size() == 1 )
        {
            return this.rulebooks.get( languages.iterator().next().getLocale() );
        }
        return null;
    }

    public ItemStack getBook( String language )
    {
        Set<Language> languages = this.module.getCore().getI18n().searchLanguages( language );
        if( languages.size() != 1 )
        {
            return null;
        }
        Locale locale = languages.iterator().next().getLocale();

        if( this.contains( locale ) )
        {
            ItemStack ruleBook = new ItemStack( Material.WRITTEN_BOOK );
            BookMeta meta = (( BookMeta ) ruleBook.getItemMeta());
            meta.setAuthor( Bukkit.getServerName() );
            meta.setTitle( this.module.getCore().getI18n().translate(language, "rulebook", "Rulebook") );
            meta.setPages( this.getPages( language ) );

            List<String> lore = new ArrayList<String>();
            lore.add( locale.getLanguage() );

            meta.setLore( lore );
            ruleBook.setItemMeta( meta );

            return ruleBook;
        }
        return null;
    }

    public boolean removeBook( String language ) throws IOException
    {
        Set<Language> languages = this.module.getCore().getI18n().searchLanguages( language );
        boolean value = false;

        if( languages.size() == 1 )
        {
            Locale locale = languages.iterator().next().getLocale();

            for( File file : RuleBookFile.getLanguageFiles( this.module.getFolder() ) )
            {
                Locale fileLocale = this.module.getCore().getI18n().searchLanguages( StringUtils.stripFileExtension( file.getName() ) ).iterator().next().getLocale();

                if( fileLocale.equals( locale ) )
                {
                    value = file.delete();
                    if( !value )
                    {
                        throw new IOException( "Can't delete the file " + file.getName() );
                    }
                }
            }
            if( value )
            {
                this.rulebooks.remove( locale );
            }
        }
        return value;
    }

    public void addBook( ItemStack book, Locale language )
    {
        Set<Language> languages = this.module.getCore().getI18n().searchLanguages(language.toString());
        if( !this.contains( language ) && languages.size() == 1 )
        {
            Language lang = languages.iterator().next();
            try
            {
                File file = new File( this.module.getFolder().getAbsoluteFile(), lang.getName() + ".txt" );
                List<String> pages = (( BookMeta ) book.getItemMeta()).getPages();
                RuleBookFile.createFile( file, pages.toArray( new String[pages.size()] ) );

                this.rulebooks.put( lang.getLocale(), RuleBookFile.convertToPages( file ) );
            }
            catch ( IOException ex )
            {
                this.module.getLogger().log( LogLevel.ERROR, "Error by creating the book" );
            }
        }
    }
}
