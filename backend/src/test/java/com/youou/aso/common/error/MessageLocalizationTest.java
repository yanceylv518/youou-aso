package com.youou.aso.common.error;

import org.junit.jupiter.api.Test;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BindException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Properties;
import static org.junit.jupiter.api.Assertions.*;

class MessageLocalizationTest {
    @Test void everyBusinessErrorHasAnExplicitTranslation() throws Exception {
        for (String language : new String[]{"zh_CN", "en_US", "ru_RU", "pt_PT", "es_ES"}) {
            Properties values = new Properties();
            try (var stream = getClass().getResourceAsStream("/i18n/messages_" + language + ".properties")) {
                assertNotNull(stream);
                values.load(new InputStreamReader(stream, StandardCharsets.UTF_8));
            }
            for (ErrorCode code : ErrorCode.values()) {
                assertNotNull(values.getProperty("error." + code), language + ": " + code);
                assertFalse(values.getProperty("error." + code).isBlank());
            }
            assertTrue(values.getProperty("error.EXECUTION_HOURS_INVALID").contains("24"));
        }
    }

    @Test void validationDoesNotExposeEnglishAnnotationMessagesInOtherLanguages() {
        var messages = new ResourceBundleMessageSource();
        messages.setBasename("i18n/messages");
        messages.setDefaultEncoding("UTF-8");
        var binding = new BindException(new Object(), "request");
        binding.reject("invalid", "must not be blank");
        var handler = new GlobalExceptionHandler(messages);
        for (String language : new String[]{"zh-CN", "en-US", "ru-RU", "pt-PT", "es-ES"}) {
            Locale locale = Locale.forLanguageTag(language);
            var response = handler.handleValidation(binding, locale, new MockHttpServletRequest());
            assertEquals(messages.getMessage("error.VALIDATION_ERROR", null, locale), response.getBody().message());
        }
    }
}
