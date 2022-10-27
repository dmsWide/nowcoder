package com.dmswide.nowcoder;

import com.dmswide.nowcoder.service.AlphaService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = NowcoderApplication.class)
public class TransactionTest {
    @Resource
    private AlphaService alphaService;

    @Test
    public void testSave(){
        Object obj = alphaService.save();
        System.out.println(obj);
    }

     @Test
    public void testSave1(){
        Object obj = alphaService.save1();
        System.out.println(obj);
    }
}
