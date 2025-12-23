package com.kobe.pokekernle;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.cloud.aws.secretsmanager.enabled=false",
        "spring.cloud.aws.region.static=us-east-1"
})
class PokekernelApplicationTests {

    @Test
    void contextLoads() {
    }

}
