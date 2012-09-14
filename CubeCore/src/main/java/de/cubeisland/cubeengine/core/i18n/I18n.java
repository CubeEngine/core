package de.cubeisland.cubeengine.core.i18n;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.filesystem.FileExtentionFilter;
import de.cubeisland.cubeengine.core.filesystem.FileManager;
import de.cubeisland.cubeengine.core.util.ChatFormat;
import de.cubeisland.cubeengine.core.util.Validate;
import de.cubeisland.cubeengine.core.util.log.CubeLogger;
import de.cubeisland.cubeengine.core.util.log.FileHandler;
import de.cubeisland.cubeengine.core.util.worker.Cleanable;
import gnu.trove.map.hash.THashMap;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Phillip Schichtel
 */
public class I18n implements Cleanable
{
    private static final Logger LOGGER = new CubeLogger("language");
    
    public static final String SOURCE_LANGUAGE = "en_US";
    private final Map<String, Language> languageMap;
    private String defaultLanguage;
    private final Map<String, String> sourceLanguageCache;

    public I18n(FileManager fm, String defaultLanguage)
    {
        this.languageMap = new THashMap<String, Language>();
        this.defaultLanguage = defaultLanguage;
        this.sourceLanguageCache = new ConcurrentHashMap<String, String>();
        this.loadLanguages(fm.getLanguageDir());
        try
        {
            LOGGER.addHandler(new FileHandler(Level.ALL, new File(fm.getLogDir(), "missing-translations.log").getPath()));
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
        if (this.languageMap.containsKey(language))
        {
            this.defaultLanguage = language;
        }
    }

    private void loadLanguages(File languageDir)
    {
        Language language;
        for (File file : languageDir.listFiles((FileFilter)FileExtentionFilter.YAML))
        {
            try
            {
                language = new Language(Configuration.load(LanguageConfiguration.class, file), languageDir);
                this.languageMap.put(language.getCode(), language);
            }
            catch (IllegalArgumentException e)
            {
                CubeEngine.getLogger().log(Level.SEVERE, e.getLocalizedMessage(), e);
            }
        }
    }
    
    public Collection<String> getLanguages()
    {
        return this.languageMap.keySet();
    }

    public String translate(String language, String category, String message, Object... params)
    {
        if (SOURCE_LANGUAGE.equalsIgnoreCase(language))
        {
            return this.parseSourceLanguage(message);
        }
        String translation = null;
        Language lang = this.languageMap.get(language);
        if (lang != null)
        {
            translation = lang.getTranslation(category, message);
        }
        
        if (translation == null)
        {
            this.logMissingTranslation(language, category, message);
            translation = this.parseSourceLanguage(message);
        }
        
        return String.format(translation, params);
    }
    
    private String parseSourceLanguage(String message)
    {
        if (message == null)
        {
            return null;
        }
        String parsed = this.sourceLanguageCache.get(message);
        if (parsed == null)
        {
            this.sourceLanguageCache.put(message, parsed = ChatFormat.parseFormats(message));
            return parsed;
        }
        return parsed;
    }

    @Override
    public void clean()
    {
        for (Language language : this.languageMap.values())
        {
            language.clean();
        }
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