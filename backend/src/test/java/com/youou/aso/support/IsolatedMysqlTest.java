package com.youou.aso.support;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.boot.test.mock.mockito.MockBean;
import java.sql.DriverManager;
import java.util.UUID;

/** Opt-in MySQL tests always create their own schema; never read application-local.yml. */
@ActiveProfiles("test")
public abstract class IsolatedMysqlTest {
    @MockBean protected com.youou.aso.modules.order.job.OrderCompletionScheduler scheduler;
    @MockBean protected com.youou.aso.modules.order.service.OrderNotificationSender notifications;
    @MockBean protected com.youou.aso.modules.account.service.PasswordResetMailSender resetMail;
    private static String schema;
    private static String testUrl;

    @DynamicPropertySource
    static synchronized void database(DynamicPropertyRegistry properties) throws Exception {
        if (schema == null) {
            String server = System.getenv("YOUOU_TEST_MYSQL_URL");
            // Require a server URL with no database name. A business schema cannot be selected accidentally.
            if (server == null || !server.matches("jdbc:mysql://[^/]+/([?].*)?")) {
                throw new IllegalStateException("YOUOU_TEST_MYSQL_URL must be a MySQL server URL ending in /, without a database name");
            }
            schema = "youou_test_" + UUID.randomUUID().toString().replace("-", "");
            try (var connection = DriverManager.getConnection(server, user(), password()); var statement = connection.createStatement()) {
                statement.execute("CREATE DATABASE `" + schema + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            }
            int query = server.indexOf('?');
            testUrl = (query < 0 ? server : server.substring(0, query)) + schema + (query < 0 ? "" : server.substring(query));
            final String createdSchema = schema;
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (!createdSchema.matches("youou_test_[a-f0-9]{32}")) return;
                try (var connection = DriverManager.getConnection(server, user(), password()); var statement = connection.createStatement()) {
                    statement.execute("DROP DATABASE `" + createdSchema + "`");
                } catch (Exception exception) {
                    System.err.println("Could not remove isolated test schema: " + createdSchema);
                }
            }));
        }
        properties.add("spring.datasource.url", () -> testUrl);
        properties.add("spring.datasource.username", IsolatedMysqlTest::user);
        properties.add("spring.datasource.password", IsolatedMysqlTest::password);
    }
    private static String user() { return System.getenv("YOUOU_TEST_MYSQL_USER"); }
    private static String password() { return System.getenv("YOUOU_TEST_MYSQL_PASSWORD"); }
}
