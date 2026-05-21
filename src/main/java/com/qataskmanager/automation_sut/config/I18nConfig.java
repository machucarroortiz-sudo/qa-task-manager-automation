package com.qataskmanager.automation_sut.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.Enumeration;
import java.util.Locale;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

@Configuration
public class I18nConfig implements WebMvcConfigurer {
    private static final String LOCALE_COOKIE_NAME = "sut_locale";
    private static final String LOCALE_PARAMETER_NAME = "lang";
    private static final Locale ENGLISH = Locale.ENGLISH;
    private static final Locale SPANISH = Locale.forLanguageTag("es");

    private final AppMetadataProperties metadata;

    public I18nConfig(AppMetadataProperties metadata) {
        this.metadata = metadata;
    }

    @Bean
    public LocaleResolver localeResolver() {
        return new BrowserAwareCookieLocaleResolver(Locale.forLanguageTag(metadata.getDefaultLocale()));
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName(LOCALE_PARAMETER_NAME);
        return interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }

    private static class BrowserAwareCookieLocaleResolver implements LocaleResolver {
        private static final String REQUEST_LOCALE_ATTRIBUTE = BrowserAwareCookieLocaleResolver.class.getName() + ".LOCALE";
        private static final int COOKIE_MAX_AGE_SECONDS = (int) Duration.ofDays(365).toSeconds();

        private final Locale defaultLocale;

        BrowserAwareCookieLocaleResolver(Locale defaultLocale) {
            Locale supportedDefault = supportedLocaleOrNull(defaultLocale);
            this.defaultLocale = supportedDefault == null ? ENGLISH : supportedDefault;
        }

        @Override
        public Locale resolveLocale(HttpServletRequest request) {
            Object requestLocale = request.getAttribute(REQUEST_LOCALE_ATTRIBUTE);
            if (requestLocale instanceof Locale locale) {
                return locale;
            }

            Locale cookieLocale = resolveCookieLocale(request);
            if (cookieLocale != null) {
                return cookieLocale;
            }

            Locale browserLocale = resolveBrowserLocale(request);
            return browserLocale == null ? defaultLocale : browserLocale;
        }

        @Override
        public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
            if (locale == null) {
                request.removeAttribute(REQUEST_LOCALE_ATTRIBUTE);
                response.addCookie(localeCookie("", 0));
                return;
            }

            Locale supportedLocale = supportedLocaleOrNull(locale);
            Locale selectedLocale = supportedLocale == null ? defaultLocale : supportedLocale;
            request.setAttribute(REQUEST_LOCALE_ATTRIBUTE, selectedLocale);
            response.addCookie(localeCookie(selectedLocale.toLanguageTag(), COOKIE_MAX_AGE_SECONDS));
        }

        private Locale resolveCookieLocale(HttpServletRequest request) {
            Cookie[] cookies = request.getCookies();
            if (cookies == null) {
                return null;
            }
            for (Cookie cookie : cookies) {
                if (LOCALE_COOKIE_NAME.equals(cookie.getName()) && hasText(cookie.getValue())) {
                    Locale supportedLocale = supportedLocaleOrNull(Locale.forLanguageTag(cookie.getValue()));
                    if (supportedLocale != null) {
                        return supportedLocale;
                    }
                }
            }
            return null;
        }

        private Locale resolveBrowserLocale(HttpServletRequest request) {
            Enumeration<Locale> locales = request.getLocales();
            while (locales.hasMoreElements()) {
                Locale supportedLocale = supportedLocaleOrNull(locales.nextElement());
                if (supportedLocale != null) {
                    return supportedLocale;
                }
            }
            return null;
        }

        private Cookie localeCookie(String value, int maxAge) {
            Cookie cookie = new Cookie(LOCALE_COOKIE_NAME, value);
            cookie.setPath("/");
            cookie.setMaxAge(maxAge);
            return cookie;
        }
    }

    private static Locale supportedLocaleOrNull(Locale locale) {
        if (locale == null) {
            return null;
        }
        String language = locale.getLanguage();
        if (SPANISH.getLanguage().equalsIgnoreCase(language)) {
            return SPANISH;
        }
        if (ENGLISH.getLanguage().equalsIgnoreCase(language)) {
            return ENGLISH;
        }
        return null;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
