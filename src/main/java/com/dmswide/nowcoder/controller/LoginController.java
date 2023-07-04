package com.dmswide.nowcoder.controller;

import com.dmswide.nowcoder.entity.User;
import com.dmswide.nowcoder.service.impl.UserServiceImpl;
import com.dmswide.nowcoder.util.CommunityConstant;
import com.google.code.kaptcha.Producer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class LoginController implements CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Resource
    private UserServiceImpl userService;
    @Resource
    private Producer kaptchaProducer;
    @GetMapping("/register")
    public String getRegistryPage(){
        return "/site/register";
    }

     @GetMapping("/login")
    public String getLoginPage(){
        return "/site/login";
    }

    //注册
    @PostMapping("/register")
    public String register(Model model,User user){
        Map<String, Object> map = userService.register(user);
        //注册成功
        if(map.isEmpty()){
            model.addAttribute("msg","注册成功,已经向您的邮箱发送了激活邮件,请尽快激活账号!");
            model.addAttribute("target","/index");
            return "/site/operate-result";
        }else{
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            model.addAttribute("emailMsg",map.get("emailMsg"));
            //重新注册
            return "/site/register";
        }
    }

    /**
     * 注册完成之后 跳转到激活页面激活账号
     * http://localhost/8080/community/activation/userId/activationCode
     * @return 返回网页
     */
    @GetMapping("/activation/{id}/{code}")
    public String activation(Model model,
                             @PathVariable("id") Integer userId,
                             @PathVariable("code") String activationCode){

        int result = userService.activation(userId, activationCode);
        if(result == ACTIVATION_SUCCESS){
            model.addAttribute("msg","激活成功,赶快去登陆吧!");
            model.addAttribute("target","/login");
        }else if(result == ACTIVATION_REPEAT){
            model.addAttribute("msg","无效操作,重复激活!");
            model.addAttribute("target","/index");
        }else if(result == ACTIVATION_FAILURE){
            model.addAttribute("msg","激活失败,激活码有误!");
            model.addAttribute("target","/index");
        }
        return "/site/operate-result";
    }

    //验证码的生成
    @GetMapping("/kaptcha")
    public void getKaptcha(/*HttpSession session,*/ HttpServletResponse response){
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);
        //验证码文本内容存入session
        /*session.setAttribute("kaptcha",text);*/

        // TODO: 2022/11/2 dmsWide 使用redis来代替session存储验证码
        //验证码归属
        String kaptchaOwner = CommunityUtil.generateUUID();
        Cookie cookie = new Cookie("kaptchaOwner", kaptchaOwner);
        cookie.setMaxAge(60);
        cookie.setPath(contextPath);
        response.addCookie(cookie);

        //验证码文本存入redis
        String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(redisKey,text,60, TimeUnit.SECONDS);

        //验证码图片输出到浏览器
        response.setContentType("image/png");
        try {
            //将验证码图片输出到网页的相应位置上
            ServletOutputStream outputStream = response.getOutputStream();
            ImageIO.write(image,"png",outputStream);
        } catch (IOException e) {
            logger.error("响应验证码失败:" + e.getMessage());
        }

    }

    @GetMapping("/logout")
    public String logout(@CookieValue("ticket") String ticket){
        userService.logout(ticket);

        // TODO: 2022/11/10 dmsWide 退出的时候清理数据
        SecurityContextHolder.clearContext();
        return "redirect:/login";
    }
}
