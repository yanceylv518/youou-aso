package com.youou.aso.modules.pricing.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ServiceTerminologyTest {
    @Test void builtInVariantsUseTheGlossaryWithoutAProviderRequest() {
        var service = new OrderModuleTranslationService(new ObjectMapper());
        assertEquals("Keyword installs (Standard)", service.canonicalName("zh-CN", "en-US", "关键词安装（普通）"));
        assertEquals("Keyword installs (Advanced)", service.canonicalName("zh-CN", "en-US", "关键词安装(高级）"));
        assertEquals("关键词安装（高级）", service.canonicalName("en-US", "zh-CN", "Key installation (advanced)"));
        for (String locale : new String[]{"en-US", "ru-RU", "pt-PT", "es-ES"}) {
            assertNotNull(service.canonicalName("zh-CN", locale, "关键词保排名"));
        }
        assertNull(service.canonicalName("zh-CN", "en-US", "专属推广套餐"));
    }
}
