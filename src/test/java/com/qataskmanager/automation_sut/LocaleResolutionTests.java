package com.qataskmanager.automation_sut;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@ActiveProfiles("test")
class LocaleResolutionTests {
    private static final String LOCALE_COOKIE_NAME = "sut_locale";

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setupMockMvc() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    void loginUsesSpanishFromAcceptLanguageWhenNoLocaleCookieExists() throws Exception {
        mockMvc.perform(get("/login").header(HttpHeaders.ACCEPT_LANGUAGE, "es-ES,es;q=0.9,en;q=0.5"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Bienvenido de nuevo")))
                .andExpect(content().string(containsString("Idioma")));
    }

    @Test
    void loginUsesEnglishFromAcceptLanguageWhenNoLocaleCookieExists() throws Exception {
        mockMvc.perform(get("/login").header(HttpHeaders.ACCEPT_LANGUAGE, "en-US,en;q=0.9,es;q=0.5"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Welcome back")))
                .andExpect(content().string(containsString("Language")));
    }

    @Test
    void loginFallsBackToEnglishWhenAcceptLanguageIsUnsupported() throws Exception {
        mockMvc.perform(get("/login").header(HttpHeaders.ACCEPT_LANGUAGE, "fr-FR,fr;q=0.9"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Welcome back")))
                .andExpect(content().string(containsString("Language")));
    }

    @Test
    void languageParameterStoresSpanishPreference() throws Exception {
        MvcResult result = mockMvc.perform(get("/login").param("lang", "es"))
                .andExpect(status().isOk())
                .andExpect(cookie().value(LOCALE_COOKIE_NAME, "es"))
                .andExpect(content().string(containsString("Bienvenido de nuevo")))
                .andReturn();

        Cookie localeCookie = result.getResponse().getCookie(LOCALE_COOKIE_NAME);

        assertThat(localeCookie).isNotNull();
        mockMvc.perform(get("/login")
                        .cookie(localeCookie)
                        .header(HttpHeaders.ACCEPT_LANGUAGE, "en-US,en;q=0.9"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Bienvenido de nuevo")));
    }

    @Test
    void savedSpanishPreferenceWinsOverEnglishAcceptLanguage() throws Exception {
        mockMvc.perform(get("/login")
                        .cookie(new Cookie(LOCALE_COOKIE_NAME, "es"))
                        .header(HttpHeaders.ACCEPT_LANGUAGE, "en-US,en;q=0.9"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Bienvenido de nuevo")))
                .andExpect(content().string(containsString("Idioma")));
    }

    @Test
    void savedEnglishPreferenceWinsOverSpanishAcceptLanguage() throws Exception {
        mockMvc.perform(get("/login")
                        .cookie(new Cookie(LOCALE_COOKIE_NAME, "en"))
                        .header(HttpHeaders.ACCEPT_LANGUAGE, "es-ES,es;q=0.9"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Welcome back")))
                .andExpect(content().string(containsString("Language")));
    }
}
