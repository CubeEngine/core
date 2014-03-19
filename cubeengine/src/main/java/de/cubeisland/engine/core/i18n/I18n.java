package de.cubeisland.engine.core.i18n;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.logging.LoggingUtil;
import de.cubeisland.engine.i18n.I18nService;
import de.cubeisland.engine.i18n.language.ClonedLanguage;
import de.cubeisland.engine.i18n.language.Language;
import de.cubeisland.engine.i18n.language.SourceLanguage;
import de.cubeisland.engine.i18n.loader.GettextLoader;
import de.cubeisland.engine.logging.Log;
import de.cubeisland.engine.logging.target.file.AsyncFileTarget;

public class I18n extends I18nService
{
    final Core core;
    private final Log logger;
    private final I18nLanguageLoader languageLoader;

    private final Map<Locale, Language> languages = new HashMap<>();

    public I18n(Core core)
    {
        super(SourceLanguage.EN_US, new GettextLoader(core.getFileManager().getTranslationPath().toFile()));
        this.core = core;
        this.logger = core.getLogFactory().getLog(Core.class, "Language");
        this.logger.addTarget(new AsyncFileTarget(LoggingUtil.getLogFile(core, "Language"),
                                                  LoggingUtil.getFileFormat(false, false),
                                                  true, LoggingUtil.getCycler(),
                                                  core.getTaskManager().getThreadFactory()));
        this.languages.put(this.getSourceLanguage().getLocale(), this.getSourceLanguage());
        this.languageLoader = new I18nLanguageLoader(this, this.core.getFileManager().getLanguagePath());
    }

    public Language getDefaultLanguage()
    {
        Language language = this.getLanguage(this.core.getConfiguration().defaultLocale);
        if (language == null)
        {
            language = this.getSourceLanguage();
        }
        return language;
    }

    public Language getLanguage(Locale locale)
    {
        if (locale == null)
        {
            throw new NullPointerException("The locale must not be null!");
        }
        Language result = this.languages.get(locale);
        if (result == null && this.languageLoader.hasConfiguration(locale))
        {
            result = this.languageLoader.loadLanguage(locale);
            if (result instanceof ClonedLanguage)
            {
                Language original = ((ClonedLanguage)result).getOriginal();
                this.languages.put(original.getLocale(), original);
            }
            this.languages.put(locale, result);
        }
        return result;
    }

    public String translate(String message)
    {
        return this.translate(this.core.getConfiguration().defaultLocale, message);
    }

    public String translate(Locale locale, String message)
    {
        if (locale == null)
        {
            throw new NullPointerException("The language must not be null!");
        }
        if (message == null)
        {
            return null;
        }

        String translation = null;
        Language language = this.getLanguage(locale);
        if (language != null)
        {
            translation = language.getTranslation(message);
        }
        if (translation == null)
        {
            // TODO this.logMissingTranslation(locale, message); still necessary?
            Language defLang = this.getLanguage(Locale.getDefault());
            if (defLang != null)
            {
                translation = defLang.getTranslation(message);
            }
            else
            {
                this.logger.warn("The configured default language {} was not found! Falling back to the source language...", this.core.getConfiguration().defaultLocale.getDisplayName());
            }
            if (translation == null)
            {
                translation = this.getSourceLanguage().getTranslation(message); // TODO why not just return the message?
            }
        }
        return translation;
    }

}
