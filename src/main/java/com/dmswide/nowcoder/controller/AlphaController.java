package com.dmswide.nowcoder.controller;

import com.dmswide.nowcoder.service.AlphaService;
import com.dmswide.nowcoder.util.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;


import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.PrintWriter;
import java.util.*;

@Controller
@RequestMapping("/alpha")
public class AlphaController {

    //注入的是mybatis的实现类
    @Autowired
    private AlphaService alphaService;

    @GetMapping("/hello")
    @ResponseBody
    public String sayHello(){
        return "hello springboot!";
    }

    @GetMapping("/find")
    @ResponseBody
    public String find(){
        return alphaService.find();
    }

    @GetMapping("/http")
    public void param(HttpServletRequest request, HttpServletResponse response){
        String method = request.getMethod();
        String contextPath = request.getContextPath();
        String servletPath = request.getServletPath();
        System.out.println("method = " + method);
        System.out.println("contextPath = " + contextPath);
        System.out.println("servletPath = " + servletPath);

        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()){
            String name = headerNames.nextElement();
            String value = request.getHeader(name);
            System.out.println(name + " : " + value);
        }
        String code = request.getParameter("code");
        System.out.println(code);

        response.setContentType("text/html;charset=utf-8");
        try(
            PrintWriter writer = response.getWriter();
        ){
            writer.write("<h1>牛客网</h1>");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @GetMapping("/getStudent")
    @ResponseBody
    public String getStudent(@RequestParam(value = "name",required = false,defaultValue = "james") String name,
                             @RequestParam(value = "age",required = false,defaultValue = "25") Integer age){
        return name + " " + age;
    }

    @GetMapping("/getById/{id}")
    @ResponseBody
    public String getStudentById(@PathVariable(value = "id") Integer id){
        return String.valueOf(id);
    }

    /**
     * post请求提交参数也可以使用@RequestParam来获取提交的参数
     */
    @PostMapping("/student")
    @ResponseBody
    public String addStudent(@RequestParam(value = "studentName") String name,
                             @RequestParam(value = "studentAge") Integer age){
        System.out.println(name + " " + age);
        return "add student";
    }

    @GetMapping("/teacher")
    public ModelAndView getTeacher(){
        ModelAndView mv = new ModelAndView();
        mv.addObject("name","mike");
        mv.addObject("age","20");
        /*设置模板名称放在template目录下 需要些templates下一级目录而且thymeleaf默认文件是html文件
        所以不需要写后缀名html 具体如下*/
        //view指的是/demo/view.html 这个html并不是静态html而是一个模板需要额外声明内容
        mv.setViewName("/demo/view");
        return mv;
    }

    @GetMapping("/school")
    public String getSchool(Model model){
        model.addAttribute("name","ustc");
        model.addAttribute("age","70");
        return "/demo/view";
    }

    //响应json数据，跨语言常用的方式 java obj -> json(字符串格式) - js obj
    @GetMapping("/emp")
    @ResponseBody
    public Map<String,Object> getEmp(){
        Map<String,Object> emp  = new HashMap<>();
        emp.put("name","lucy");
        emp.put("age","22");
        return emp;
    }

    @GetMapping("/emps")
    @ResponseBody
    public List<Map<String,Object>> getEmps(){
        List<Map<String,Object>> list = new ArrayList<>();

        Map<String,Object> lucy  = new HashMap<>();
        lucy.put("name","lucy");
        lucy.put("age","22");
        list.add(lucy);

        Map<String,Object> cindy  = new HashMap<>();
        cindy.put("name","cindy");
        cindy.put("age","22");
        list.add(cindy);
        return list;
    }

    /**
     * 服务端设置cookie 使用response来设置
     * @param response
     * @return
     */
    @GetMapping("/cookie/set")
    @ResponseBody
    public String setCookie(HttpServletResponse response){
        //下面三个值都会以key=value的形式存在cookie中
        Cookie cookie = new Cookie("code", CommunityUtil.generateUUID());
        //设置cookie的生效范围 也就是设置了在下次哪些路径中才携带这个cookie
        cookie.setPath("/community/alpha");
        //设置生存的时间 也就是在磁盘上的生存时间
        cookie.setMaxAge(2 * 60);
        //发送cookie就是将cookie设置在response的header中
        response.addCookie(cookie);

        return "set cookie";
    }

    /**
     * request带有cookie那么就可以从中获取cookie
     * @param code
     * @return
     */
    @GetMapping("/cookie/get")
    @ResponseBody
    public String getCookie(@CookieValue("code") String code){
        return code;
    }

    /**
     * JSESSIONID来指定cookie和session的对应关系
     * @param session 不需要自己创建 直接声明即可 是一个域对象
     * @return 返回字符串
     */
    @GetMapping("/session/set")
    @ResponseBody
    public String setSession(HttpSession session){
        session.setAttribute("id",1);
        session.setAttribute("name","session");
        return "set session";
    }

    @GetMapping("/session/get")
    @ResponseBody
    public String getSession(HttpSession session, @CookieValue("JSESSIONID") String sessionId){
        Integer id = (Integer) session.getAttribute("id");
        String name = (String) session.getAttribute("name");
        return id + " " + name + " " + sessionId;
    }

    /**
     * ajax返回json数据 不是网页
     * @param name
     * @param age
     * @return json字符串
     */
    @PostMapping("/ajax")
    @ResponseBody
    public String testAjax(String name,int age){
        System.out.println(name + " " + age);
        return CommunityUtil.getJSONString(0,"ok");
    }
}
