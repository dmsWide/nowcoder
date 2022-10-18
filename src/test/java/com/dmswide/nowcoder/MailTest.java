package com.dmswide.nowcoder;

import com.dmswide.nowcoder.util.MailClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.annotation.Resource;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = NowcoderApplication.class)
public class MailTest {
    @Resource
    private MailClient mailClient;
    @Resource
    private TemplateEngine templateEngine;

    @Test
    public void testMailClient(){
        mailClient.sendMail("dmswide@mail.ustc.edu.cn","test","this is a test mail");
    }

    @Test
    public void testSendHtmlMail(){
        //模板的数据
        Context context = new Context();
        context.setVariable("username","cindy");
        //数据context放入到模板引擎当中 使用模板引擎生成邮件内容
        String text = templateEngine.process("/mail/demo", context);
        System.out.println(text);
        mailClient.sendMail("dmswide@mail.ustc.edu.cn","html email",text);
    }
}
