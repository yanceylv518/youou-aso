package com.youou.aso;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable(named = "YOUOU_TEST_MYSQL_URL", matches = "jdbc:mysql://.+")
@SpringBootTest
class YououAsoApplicationTests extends com.youou.aso.support.IsolatedMysqlTest {
    @Test
    void contextLoads() {
    }
}
