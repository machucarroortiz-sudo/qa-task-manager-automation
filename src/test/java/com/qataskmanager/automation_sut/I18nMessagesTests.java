package com.qataskmanager.automation_sut;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import org.junit.jupiter.api.Test;

class I18nMessagesTests {
    private static final Path RESOURCES = Path.of("src", "main", "resources");
    private static final Path STYLESHEET = RESOURCES.resolve(Path.of("static", "css", "styles.css"));

    @Test
    void guiMessageBundlesHaveTheSameKeys() throws IOException {
        Properties english = load("messages.properties");
        Properties spanish = load("messages_es.properties");

        assertThat(spanish.stringPropertyNames()).containsExactlyInAnyOrderElementsOf(english.stringPropertyNames());
    }

    @Test
    void validationMessageBundlesHaveTheSameKeys() throws IOException {
        Properties english = load("ValidationMessages.properties");
        Properties spanish = load("ValidationMessages_es.properties");

        assertThat(spanish.stringPropertyNames()).containsExactlyInAnyOrderElementsOf(english.stringPropertyNames());
    }

    @Test
    void contentTranslationTooltipWarningIsHiddenUntilHoverOrFocus() throws IOException {
        String styles = Files.readString(STYLESHEET, StandardCharsets.UTF_8);

        int tooltipBlock = styles.indexOf(".tooltip-bubble {");
        int hiddenRule = styles.indexOf("visibility: hidden;", tooltipBlock);
        int hoverRule = styles.indexOf(".tooltip-wrapper:hover .tooltip-bubble", tooltipBlock);
        int focusRule = styles.indexOf(".tooltip-wrapper:focus-within .tooltip-bubble", hoverRule);
        int visibleRule = styles.indexOf("visibility: visible;", focusRule);

        assertThat(tooltipBlock).isGreaterThanOrEqualTo(0);
        assertThat(hiddenRule).isGreaterThan(tooltipBlock).isLessThan(hoverRule);
        assertThat(focusRule).isGreaterThan(hoverRule);
        assertThat(visibleRule).isGreaterThan(focusRule);
    }

    private Properties load(String fileName) throws IOException {
        Properties properties = new Properties();
        try (var reader = Files.newBufferedReader(RESOURCES.resolve(fileName), StandardCharsets.UTF_8)) {
            properties.load(reader);
        }
        return properties;
    }
}
