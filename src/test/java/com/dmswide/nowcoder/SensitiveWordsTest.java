package com.dmswide.nowcoder;

import com.dmswide.nowcoder.util.SensitiveWordsFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = NowcoderApplication.class)
public class SensitiveWordsTest {
    @Resource
    private SensitiveWordsFilter sensitiveWordsFilter;

    /**
     * 过滤敏感词测试方法
     */
    @Test
    public void testSensitiveWordsFilter(){
        String text = "这里可以赌博,** ++ 吸毒 **/嫖娼 练习法轮功 等你到来";
        String processedText = sensitiveWordsFilter.filter(text);
        System.out.println(processedText);
        text = "这里可以⭐赌⭐⭐博⭐,** ++ 吸⭐毒 **/嫖⭐娼 练习⭐法⭐轮⭐⭐功 等你到来";
        processedText = sensitiveWordsFilter.filter(text);
        System.out.println(processedText);
    }
}
