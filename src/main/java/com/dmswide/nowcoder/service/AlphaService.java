package com.dmswide.nowcoder.service;

import com.dmswide.nowcoder.dao.AlphaDao;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

@Service
//不是创建容器的时候就创建实例了 延迟创建在使用对象的时候再创建对象
//@Scope("prototype")
public class AlphaService {

    @Resource
    //@Qualifier("alphaDaoMybatisImpl")
    private AlphaDao alphaDao;

    public AlphaService() {
        System.out.println("construct");
    }

    /**
     * 构造器之后执行
     */
    @PostConstruct
    public void init(){
        System.out.println("after construct");
    }

    /**
     * 销毁对象之前调用
     */

    @PreDestroy
    public void destory(){
        System.out.println("before destory");
    }

    public String find(){
        return alphaDao.select();
    }
}
