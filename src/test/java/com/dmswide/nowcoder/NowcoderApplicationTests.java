package com.dmswide.nowcoder;

import com.dmswide.nowcoder.dao.AlphaDao;
import com.dmswide.nowcoder.service.AlphaService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.SimpleDateFormat;
import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest
/*测试类也使用配置类需要加入这个注解*/
@ContextConfiguration(classes = NowcoderApplication.class)
public class NowcoderApplicationTests implements ApplicationContextAware {

    /**
     * 要获取spring容器需要继承ApplicationContextAware接口实现setApplicationContext方法
     * 在项目启动的时候spring容器会检测到，然后将自身传入进来然后这个类就可以获取到spring容器了
     * @param applicationContext
     * @throws BeansException
     */
    private ApplicationContext applicationContext;

    @Autowired
    private SimpleDateFormat simpleDateFormat;

    @Autowired
    @Qualifier("alphaDaoHibernateImpl")
    private AlphaDao alphaDao;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Test
    public void testApplicationContext(){
        //GenericWebApplicationContext
        System.out.println(applicationContext);

        //AlphaDao bean = new AlphaDaoHibernateImpl();两者效果是一致的
        AlphaDao bean = applicationContext.getBean(AlphaDao.class);
        System.out.println(bean.select());
        //bean = (AlphaDao) applicationContext.getBean("alphaDaoHibernateImpl");
        bean = applicationContext.getBean("alphaDaoHibernateImpl",AlphaDao.class);
        System.out.println(bean.select());
    }

    @Test
    public void testBeanManagement(){
        AlphaService bean = applicationContext.getBean(AlphaService.class);
        System.out.println(bean);
        //spring容器管理的实例默认bean是单例的
        AlphaService bean1 = applicationContext.getBean(AlphaService.class);
        System.out.println(bean1);
        System.out.println(bean == bean1);//true
    }

    @Test
    public void testBeanConfig(){
        //这是主动获取bean更常用的是依赖注入
        SimpleDateFormat bean = applicationContext.getBean(SimpleDateFormat.class);
        System.out.println(bean.format(new Date()));
        System.out.println(simpleDateFormat.format(new Date()));;
    }

    @Test
    public void testAlphaDaoDI(){
        System.out.println(alphaDao.select());;
    }


}
