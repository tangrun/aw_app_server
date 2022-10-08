package cn.wildfirechat.app;

import cn.wildfirechat.app.jpa.ShiroSession;
import cn.wildfirechat.app.shiro.ShiroSessionService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ApplicationTests {

	@Resource
	ShiroSessionService shiroSessionService;

	@Test
	public void contextLoads() {

		shiroSessionService.updateSession("11", "rrrrrrvbdfbvdf".getBytes());
		shiroSessionService.updateSession("12", ",uydmrymtym".getBytes());
		shiroSessionService.updateSession("123", "mmmmmmmmmmm".getBytes());

		ShiroSession sessionById = shiroSessionService.getSessionById("12");


		List<ShiroSession> activeSession1 = shiroSessionService.getActiveSession();

		shiroSessionService.deleteSession("123");

		List<ShiroSession> activeSession2 = shiroSessionService.getActiveSession();

		System.out.println("qqqqqq");
	}

}
