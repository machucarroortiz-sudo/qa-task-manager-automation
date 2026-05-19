package com.qataskmanager.automation_sut.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.Locale;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

@Configuration
public class I18nConfig implements WebMvcConfigurer {
    private static final String LOCALE_COOKIE_NAME = "sut_locale";
    private static final String LOCALE_PARAMETER_NAME = "lang";

    private final AppMetadataProperties metadata;

    public I18nConfig(AppMetadataProperties metadata) {
        this.metadata = metadata;
    }

    @Bean
    public LocaleResolver localeResolver() {
        CookieLocaleResolver resolver = new CookieLocaleResolver(LOCALE_COOKIE_NAME);
        resolver.setDefaultLocale(Locale.forLanguageTag(metadata.getDefaultLocale()));
        resolver.setCookieMaxAge(Duration.ofDays(365));
        return resolver;
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName(LOCALE_PARAMETER_NAME);
        return interceptor;
    }

    @Bean
    public HandlerInterceptor localeContextInterceptor() {
        return new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
                LocaleContextHolder.setLocale(resolveLocale(request));
                return true;
            }

            @Override
            public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
                LocaleContextHolder.resetLocaleContext();
            }
        };
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
        registry.addInterceptor(localeContextInterceptor());
    }

    private Locale resolveLocale(HttpServletRequest request) {
        String localeParameter = request.getParameter(LOCALE_PARAMETER_NAME);
        if (hasText(localeParameter)) {
            return Locale.forLanguageTag(localeParameter);
        }
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (LOCALE_COOKIE_NAME.equals(cookie.getName()) && hasText(cookie.getValue())) {
                    return Locale.forLanguageTag(cookie.getValue());
                }
            }
        }
        return Locale.forLanguageTag(metadata.getDefaultLocale());
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
