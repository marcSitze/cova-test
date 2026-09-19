package com.taskmanager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class TaskManagerApplicationTests {

    @Test
    void contextLoads() {
        // Test que le contexte Spring Boot démarre correctement
    }
}
