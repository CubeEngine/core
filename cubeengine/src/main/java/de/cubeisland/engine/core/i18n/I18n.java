package de.cubeisland.engine.core.i18n;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.i18n.DefinitionLoadingException;
import de.cubeisland.engine.i18n.I18nService;
import de.cubeisland.engine.i18n.TranslationLoadingException;
import de.cubeisland.engine.i18n.language.Language;
import de.cubeisland.engine.i18n.language.SourceLanguage;
import de.cubeisland.engine.i18n.loader.GettextLoader;

public class I18n
{
    final Core core;
    private final I18nService service;
    private List<URI> translationFolders = new LinkedList<>();

    public I18n(Core core)
    {
        this.core = core;
        // TODO fill translationFolders
        GettextLoader translationLoader = new GettextLoader(Charset.forName("UTF-8"), this.translationFolders);
        this.service = new I18nService(SourceLanguage.EN_US, translationLoader, new I18nLanguageLoader(core), core.getConfiguration().defaultLocale);
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
            Language defLang = this.getDefaultLanguage();
            if (defLang != null)
            {
                translation = defLang.getTranslation(message);
            }
            else
            {
                this.core.getLog().warn("The configured default language {} was not found! Falling back to the source language...", this.core
                    .getConfiguration().defaultLocale.getDisplayName());
            }
            if (translation == null)
            {
                translation = service.getSourceLanguage().getTranslation(message); // TODO why not just return the message?
            }
        }
        return translation;
    }

    private Language getLanguage(Locale locale)
    {
        try
        {
            return this.service.getLanguage(locale);
        }
        catch (TranslationLoadingException | DefinitionLoadingException e)
        {
            this.core.getLog().error(e, "Error while getting Language!");
            return null;
        }
    }

    public Language getDefaultLanguage()
    {
        return this.getLanguage(service.getDefaultLocale());
    }
}
