package com.qataskmanager.automation_sut;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.qataskmanager.automation_sut.repository.IssueRepository;
import com.qataskmanager.automation_sut.repository.LabelRepository;
import com.qataskmanager.automation_sut.repository.TaskRepository;
import com.qataskmanager.automation_sut.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@ActiveProfiles("test")
class TestDataManagementTests {
    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private IssueRepository issueRepository;

    @Autowired
    private LabelRepository labelRepository;

    @BeforeEach
    void restoreDefaultSeedData() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
        mockMvc.perform(post("/api/test-data/reset")
                        .with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void adminCanResetData() throws Exception {
        mockMvc.perform(post("/api/test-data/reset")
                        .with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users").value(3))
                .andExpect(jsonPath("$.tasks").value(5))
                .andExpect(jsonPath("$.issues").value(3))
                .andExpect(jsonPath("$.labels").value(4));
    }

    @Test
    void adminCanClearAllDataExceptDefaultAdmin() throws Exception {
        mockMvc.perform(post("/api/test-data/clear")
                        .with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users").value(1))
                .andExpect(jsonPath("$.tasks").value(0))
                .andExpect(jsonPath("$.issues").value(0))
                .andExpect(jsonPath("$.labels").value(0))
                .andExpect(jsonPath("$.comments").value(0));

        org.assertj.core.api.Assertions.assertThat(userRepository.findByEmail("admin@example.com")).isPresent();
        org.assertj.core.api.Assertions.assertThat(taskRepository.count()).isZero();
        org.assertj.core.api.Assertions.assertThat(issueRepository.count()).isZero();
        org.assertj.core.api.Assertions.assertThat(labelRepository.count()).isZero();
    }

    @Test
    void nonAdminCannotClearOrResetData() throws Exception {
        mockMvc.perform(post("/api/test-data/clear")
                        .with(user("user1@example.com").roles("USER")))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/test-data/reset")
                        .with(user("user1@example.com").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void invalidImportReturnsClearErrorResponse() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "unsafe.sql", "text/plain", "drop table tasks".getBytes());

        mockMvc.perform(multipart("/api/test-data/import")
                        .file(file)
                        .with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(containsString("Only JSON import files are supported")));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void settingsPageRequiresExplicitClearDataConfirmationText() throws Exception {
        mockMvc.perform(get("/settings"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("data-testid=\"settings-clear-data-button\"")))
                .andExpect(content().string(containsString("data-confirm-required-text=\"CLEAR DATA\"")))
                .andExpect(content().string(containsString("data-testid=\"clear-data-confirmation-input\"")));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void settingsClearActionRedirectsWithCsrf() throws Exception {
        mockMvc.perform(post("/settings/clear").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/settings"));
    }

    @Test
    void defaultLocaleRendersEnglishLoginUi() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Welcome back")))
                .andExpect(content().string(containsString("Language")))
                .andExpect(content().string(containsString("name=\"_csrf\"")));
    }

    @Test
    void seededUserCanLogInThroughUiWithCsrf() throws Exception {
        mockMvc.perform(post("/login")
                        .param("email", "admin@example.com")
                        .param("password", "password123")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));
    }

    @Test
    void spanishLocaleRendersSpanishLoginUi() throws Exception {
        mockMvc.perform(get("/login").param("lang", "es"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Bienvenido de nuevo")))
                .andExpect(content().string(containsString("Idioma")));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void taskDetailsPageExposesSeparateContentTranslationControl() throws Exception {
        mockMvc.perform(get("/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("data-content-translation-toggle")))
                .andExpect(content().string(containsString("tooltip-bubble")))
                .andExpect(content().string(containsString("data-content-translatable=\"true\"")));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void issueDetailsPageExposesSeparateContentTranslationControl() throws Exception {
        mockMvc.perform(get("/issues/1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("data-content-translation-toggle")))
                .andExpect(content().string(containsString("tooltip-bubble")))
                .andExpect(content().string(containsString("data-content-translatable=\"true\"")));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void spanishLocaleRendersTranslatedTaskListUi() throws Exception {
        mockMvc.perform(get("/tasks").param("lang", "es"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Mis tareas")))
                .andExpect(content().string(containsString("Traducir contenido")));
    }
}
