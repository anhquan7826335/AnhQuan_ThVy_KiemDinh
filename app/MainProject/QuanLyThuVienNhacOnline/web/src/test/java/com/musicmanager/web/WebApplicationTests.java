package com.musicmanager.web;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class WebApplicationTests {

	@Disabled("Context đầy đủ cần cấu hình Security/DB thật; các test khác đã dùng slice test. Bật lại khi cấu hình test đầy đủ.")
	@Test
	void contextLoads() {
	}

}
