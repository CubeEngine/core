package de.cubeisland.cubeengine.core.i18n;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.filesystem.FileExtentionFilter;
import de.cubeisland.cubeengine.core.filesystem.FileManager;
import de.cubeisland.cubeengine.core.util.Cleanable;
import de.cubeisland.cubeengine.core.util.Validate;
import de.cubeisland.cubeengine.core.util.log.CubeLogger;
import de.cubeisland.cubeengine.core.util.log.FileHandler;
import gnu.trove.map.hash.THashMap;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Phillip Schichtel
 */
public class I18n implements Cleanable
{
    private static final Logger LOGGER = new CubeLogger("language",false);
    
    public static final SourceLanguage SOURCE_LANGUAGE = SourceLanguage.getInstance();
    private final Map<String, Language> languageMap;
    private String defaultLanguage;

    public I18n(FileManager fm)
    {
        this(fm, SOURCE_LANGUAGE.getCode());
    }

    public I18n(FileManager fm, String defaultLanguage)
    {
        this.languageMap = new THashMap<String, Language>();
        this.defaultLanguage = defaultLanguage;
        this.loadLanguages(fm.getLanguageDir());
        try
        {
            LOGGER.addHandler(new FileHandler(Level.ALL, new File(fm.getLogDir(), "missing-translations").getPath()));
        }
        catch (IOException e)
        {
            Logger.getLogger(I18n.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public String getDefaultLanguage()
    {
        return this.defaultLanguage;
    }

    public void setDefaultLanguage(String language)
    {
        Validate.notNull(language, "The language must not be null!");

        language = normalizeLanguage(language);
        if (SOURCE_LANGUAGE.equals(language) || this.languageMap.containsKey(language))
        {
            this.defaultLanguage = language;
        }
    }

    private void loadLanguages(File languageDir)
    {
        Map<String, LanguageConfiguration> languages = new HashMap<String, LanguageConfiguration>();
        LanguageConfiguration config;
        for (File file : languageDir.listFiles((FileFilter)FileExtentionFilter.YAML))
        {
            config = Configuration.load(LanguageConfiguration.class, file, false);
            config.code = normalizeLanguage(config.code);
            if (config.code != null)
            {
                languages.put(config.code, config);
            }
            else
            {
                LOGGER.log(Level.SEVERE, "The language ''{0}'' has an invalid configation!", file.getName());
            }
        }
        
        Stack<String> loadStack = new Stack<String>();
        for (LanguageConfiguration entry : languages.values())
        {
            this.loadLanguage(languageDir, entry, languages, loadStack);
        }
    }
    
    private Language loadLanguage(File languageDir, LanguageConfiguration config, Map<String, LanguageConfiguration> languages, Stack<String> loadStack)
    {
        if (this.languageMap.containsKey(config.code))
        {
            return this.languageMap.get(config.code);
        }
        if (loadStack.contains(config.code))
        {
            LOGGER.log(Level.SEVERE, "The language ''{0}'' caused a circular dependency!", loadStack.peek());
            return null;
        }
        Language language = null;
        config.parent = normalizeLanguage(config.parent);
        if (SOURCE_LANGUAGE.equals(config.parent))
        {
            language = SOURCE_LANGUAGE;
        }
        else
        {
            LanguageConfiguration parent = languages.get(config.parent);
            if (parent != null)
            {
                loadStack.add(config.code);
                language = this.loadLanguage(languageDir, parent, languages, loadStack);
                loadStack.pop();
            }
        }

        try
        {
            language = new NormalLanguage(config, languageDir, language);
            this.languageMap.put(config.code, language);
            if (config.clones != null)
            {
                Language clonedLanguage;
                for (String clone : config.clones)
                {
                    clonedLanguage = ClonedLanguage.clone(language, clone);
                    if (clonedLanguage != null && !SOURCE_LANGUAGE.equals(clonedLanguage.getCode()))
                    {
                        this.languageMap.put(clonedLanguage.getCode(), clonedLanguage);
                    }
                }
            }
            
            return language;
        }
        catch (IllegalArgumentException e)
        {
            LOGGER.log(Level.SEVERE, "Failed to load the language ''{0}'': {1}", new Object[] {config.code, e.getLocalizedMessage()});
        }
        return null;
    }
    
    public Collection<String> getLanguages()
    {
        return this.languageMap.keySet();
    }
    
    public Language getLanguage(String name)
    {
        return this.languageMap.get(name);
    }

    public String translate(String language, String category, String message, Object... params)
    {
        String translation = null;
        if (!SOURCE_LANGUAGE.equals(language))
        {
            Language lang = this.languageMap.get(language);
            if (lang != null)
            {
                translation = lang.getTranslation(category, message);
            }
        }
        
        if (translation == null)
        {
            this.logMissingTranslation(language, category, message);
            translation = SOURCE_LANGUAGE.getTranslation(category, message);
        }
        
        return String.format(translation, params);
    }

    @Override
    public void clean()
    {
        for (Language language : this.languageMap.values())
        {
            language.clean();
        }
        SOURCE_LANGUAGE.clean();
        this.languageMap.clear();
    }

    public static String normalizeLanguage(String name)
    {
        if (name == null)
        {
            return null;
        }
        final int length = name.length();
        if (length >= 2 && length <= 5)
        {
            final int delimPos = name.indexOf('_');
            if (delimPos < 0 && length == 2)
            {
                return name.toLowerCase(Locale.ENGLISH) + "_" + name.toUpperCase(Locale.ENGLISH);
            }
            else if (delimPos == 2 && length == 5)
            {
                return name.substring(0, 2).toLowerCase(Locale.ENGLISH) + '_' + name.substring(3).replace("_", "").toUpperCase(Locale.ENGLISH);
            }
        }
        return null;
    }
    
    private void logMissingTranslation(String language, String category, String message)
    {
        LOGGER.log(Level.INFO, "\"{0}\" - \"{1}\" - \"{2}\"", new Object[] {language, category, message});
    }
}