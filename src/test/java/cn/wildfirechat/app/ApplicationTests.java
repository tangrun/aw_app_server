package cn.wildfirechat.app;

import cn.wildfirechat.app.jpa.ShiroSession;
import cn.wildfirechat.app.shiro.ShiroSessionService;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.net.URI;
import java.net.URLEncoder;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ApplicationTests {

	@Resource
	ShiroSessionService shiroSessionService;


	@Test
	public void contextLoads() {

	}
	private RestResult.RestCode sendLinKaiCode(String mobile, String code) {
		String msgContent = "您好！您本次的验证码为:" + code + ",如非本人操作请忽略.";
		try {
			String send_content = URLEncoder.encode(msgContent, "GBK");

			String path ="http://sdk2.028lk.com:9880/sdk2/BatchSend2.aspx";
			String user ="CDXLKJ010847";
			String pwd ="123456789";

			URI url = new URI(path + "?CorpID="
					+ user + "&Pwd=" + pwd + "&Mobile=" + mobile + "&Content="
					+ send_content + "&Cell=&SendTime=" + "");

			RestTemplate restTemplate = new RestTemplate();

			String response = restTemplate.getForObject(url, String.class);
			//System.out.println(response);
			if (StringUtils.isNotBlank(response) && Integer.parseInt(response) > 1)
				return RestResult.RestCode.SUCCESS;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return RestResult.RestCode.ERROR_SERVER_ERROR;
	}
}
