package com.moo.authenticationservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;

@SpringBootTest
@ActiveProfiles("test")
class AuthenticationServiceApplicationTests {

	@Autowired
	private Environment env;

	@Test
	void testProfileIsActive() {
		var active = Arrays.asList(env.getActiveProfiles());
		System.out.println("Active profiles: " + active);
		assert active.contains("test");
	}

	@Test
	void contextLoads() {
	}

}
