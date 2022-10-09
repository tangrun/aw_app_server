package cn.wildfirechat.app;

import cn.wildfirechat.common.IMExceptionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

@RestController
public class IMExceptionEventController {
    private BlockingDeque<IMExceptionEvent> events = new LinkedBlockingDeque<>();
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${spring.mail.username}")
    private String from;

    @Value("${spring.mail.to_lists}")
    private String toLists;

    @Autowired
    private JavaMailSender mailSender;

    @PostConstruct
    void init() {
        new Thread(()->{
            while (true) {
                try {
                    IMExceptionEvent event = events.take();
                    if (event.event_type == IMExceptionEvent.EventType.HEART_BEAT) {
                        sendTextMail("恭喜您，您的服务已经连续24小时没有异常发生了", "恭喜您，您的服务已经连续24小时没有异常发生了");
                    } else {
                        sendTextMail("IM服务报警通知：节点" + event.node_id + "，发生" + event.count + "次  " + event.msg, "call stack:" + event.call_stack);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @PostMapping("im_exception_event")
    public String onIMException(@RequestBody IMExceptionEvent event)  {
        System.out.println(event);
        events.add(event);
        return "ok";
    }

    /**
     * 文本邮件
     * @param subject 邮件主题
     * @param content 邮件内容
     */
    public void sendTextMail(String subject, String content){
        SimpleMailMessage message = new SimpleMailMessage();
        String[] tos = toLists.split(",");
        message.setTo(tos);
        message.setSubject(subject);
        message.setText(content);
        message.setFrom(from);

        mailSender.send(message);
    }

    //content HTML内容
    public void sendHtmlMail(String subject, String content) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        String[] tos = toLists.split(",");
        helper.setTo(tos);
        helper.setSubject(subject);
        helper.setText(content, true);
        helper.setFrom(from);

        mailSender.send(message);
    }
}
