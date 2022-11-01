package com.dmswide.nowcoder.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

/** aop示例:
 * aop概念理解困难但是使用很方便
 */

//@Component
//@Aspect
public class AlphaAspect {
    //定义切点的表达式
    @Pointcut("execution(* com.dmswide.nowcoder.service.impl.*.*(..))")
    public void pointCut(){

    }

    //定义advice
    @Before("pointCut()")
    public void before(){
        System.out.println("before");
    }

    //定义advice
    @After("pointCut()")
    public void after(){
        System.out.println("after");
    }

    //定义advice
    @AfterReturning("pointCut()")
    public void afterReturning(){
        System.out.println("after returning");
    }

    //定义advice
    @AfterThrowing("pointCut()")
    public void afterThrow(){
        System.out.println("after Throw");
    }

    //定义advice

    /**
     *
     * @param joinPoint ProceedingJointPoint
     * @return 返回值是必须有的 类型是Object 非环绕类型的方法也可以有连接点 参数类型是JoinPoint
     * @throws Throwable
     */
    @Around("pointCut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        //在目标方法之前执行某些功能
        System.out.println("around before target method");
        //调用target对象的 选定的目标方法
        Object proceed = joinPoint.proceed();
        //在目标方法之后执行某些功能
        System.out.println("around after target method");

        return proceed;
    }
}
