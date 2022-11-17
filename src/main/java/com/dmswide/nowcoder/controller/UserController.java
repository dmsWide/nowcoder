package com.dmswide.nowcoder.controller;

import com.dmswide.nowcoder.annotation.LoginRequired;
import com.dmswide.nowcoder.entity.User;
import com.dmswide.nowcoder.service.FollowService;
import com.dmswide.nowcoder.service.LikeService;
import com.dmswide.nowcoder.service.impl.UserServiceImpl;
import com.dmswide.nowcoder.util.CommunityConstant;
import com.dmswide.nowcoder.util.CommunityUtil;
import com.dmswide.nowcoder.util.HostHolder;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${nowcoder.path.upload}")
    private String uploadPath;
    @Value("${nowcoder.path.domain}")
    private String domain;
    @Value("${server.servlet.context-path}")
    private String contextPath;
    @Resource
    private UserServiceImpl userService;
    @Resource
    private HostHolder hostHolder;
    @Resource
    private LikeService likeService;
    @Resource
    private FollowService followService;
    @Value("${qiniu.key.access}")
    private String accessKey;
    @Value("${qiniu.key.secret}")
    private String secretKey;
    @Value("${qiniu.bucket.header.name}")
    private String headerBucketName;
    @Value("${qiniu.bucket.header.url}")
    private String headerBucketUrl;

    @LoginRequired
    @GetMapping("/setting")
    public String getSettingPage(Model model){
        // TODO: 2022/11/16 dmsWide 打开头像上传表单的页面 生成上传凭证 并且将凭证写入到表单里
        //上传文件的名称
        String fileName = CommunityUtil.generateUUID();
        //设置响应信息
        StringMap policy = new StringMap();
        policy.put("returnBody",CommunityUtil.getJSONString(0));
        //生成上传凭证
        Auth auth = Auth.create(accessKey, secretKey);
        String uploadToken = auth.uploadToken(headerBucketName, fileName, 60 * 60, policy);
        model.addAttribute("uploadToken",uploadToken);
        model.addAttribute("fileName",fileName);
        return "/site/setting";
    }

    @PostMapping("/header/url")
    @ResponseBody
    public String updateHeaderUrl(String fileName){
        if(StringUtils.isBlank(fileName)){
            return CommunityUtil.getJSONString(1,"文件名为空");
        }
        //七牛云上头像的访问路径
        String url = headerBucketUrl + "/" + fileName;
        userService.updateHeader(hostHolder.getUser().getId(),url);
        return CommunityUtil.getJSONString(0);
    }

    // TODO: 2022/11/16 dmsWide 将头像上传到七牛云上方法废弃
    /**
     * 上传头像 上传到本地文件夹 D:/work/data/upload
     * @param headerImage
     * @param model
     * @return 返回首页
     */
    @LoginRequired
    @PostMapping("/upload")
    public String uploadHeader(MultipartFile headerImage, Model model){
        if(headerImage == null){
            model.addAttribute("error","请选择要上传的图片");
            return "/site/setting";
        }
        String originalFilename = headerImage.getOriginalFilename();

        //System.out.println("originalFilename :" + originalFilename);

        if(originalFilename != null){
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            if(StringUtils.isBlank(suffix)){
                model.addAttribute("error","文件格式不对");
                return "/site/setting";
            }
            //生成随机文件名
            originalFilename = CommunityUtil.generateUUID() + suffix;
            File file = new File(uploadPath + "/" + originalFilename);
            try {
                //存储文件
                headerImage.transferTo(file);
            } catch (IOException e) {
                logger.error("上传文件失败" + e.getMessage());
                throw new RuntimeException("上传文件失败,服务器发生异常",e);
            }
            //更新当前用户头像的路径,web访问路径
            //http://localhost:8080/community/user/header/xxx.png
            User user = hostHolder.getUser();
            String headerUrl = domain + contextPath + "/user/header/" + originalFilename;
            userService.updateHeader(user.getId(),headerUrl);
        }
        return "redirect:/index";
    }


    // TODO: 2022/11/16 dmsWide 头像上传到七牛云上 查看头像的方法也废弃
    /**
     * 向浏览器响应图片
     * @param fileName 文件名
     * @param response 响应对象
     */
    @GetMapping("/header/{fileName}")
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response){
        //服务器存放路径
        fileName = uploadPath + "/" + fileName;

        //System.out.println("fileName = " + fileName);

        //获取文件格式
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
        //设置相应的内容
        response.setContentType("image/" + suffix);
        //获取输出流
        try (
            //实现Closeable接口 执行结束自动调用close()方法
            FileInputStream fileInputStream = new FileInputStream(fileName);
            OutputStream outputStream = response.getOutputStream()
        ){
            int b = 0;
            byte[] buffer = new byte[1024];
            while((b = fileInputStream.read(buffer)) != -1){
                outputStream.write(buffer,0,b);
            }
        } catch (IOException e) {
            logger.error("读取头像失败" + e.getMessage());
        }
    }

    /**
     * 如果处理成功跳转到operate-result.html页面 否则还留在setting.html
     * @param originalPassword 原始密码
     * @param newPassword 新密码
     * @param confirmPassword 确认密码
     * @param model 域对象
     * @return 根据处理结果返回特定网页
     */
    @PostMapping("/updatePassword")
    public String updatePassword(String originalPassword, String newPassword, String confirmPassword, Model model){

        Map<String, Object> map = userService.changePassword(hostHolder.getUser().getId(), originalPassword, newPassword, confirmPassword);
        if(map.isEmpty()){
            model.addAttribute("msg","成功修改密码");
            //修改密码后 重定向到退出功能 强制用户退出重新登录
            model.addAttribute("target","/logout");
            return "/site/operate-result.html";
        }else{
            model.addAttribute("originalPasswordMsg",map.get("originalPasswordMsg"));
            model.addAttribute("newPasswordMsg",map.get("newPasswordMsg"));
            model.addAttribute("confirmPasswordMsg",map.get("confirmPasswordMsg"));
            return "/site/setting";
        }
    }

    /**
     * 查看个人主页 不只是自己的主页 通过点击头像可以查看任何人的主页
     * @param userId 根据用户id来查询 拼成url
     * @param model 给网页返回数据
     * @return 返回页面
     */
    @GetMapping("/profile/{userId}")
    public String getProfilePage(@PathVariable("userId") Integer userId,Model model){
        User user = userService.findUserById(userId);
        if(user == null){
            throw new RuntimeException("该用户不存在");
        }
        //添加用户
        model.addAttribute("user",user);
        //点赞数量
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount",likeCount);

        // TODO: 2022/11/1 dmsWide 查询关注数量
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount",followeeCount);
        // TODO: 2022/11/1 dmsWide 粉丝的数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount",followerCount);
        // TODO: 2022/11/1 dmsWide 当前登录用户是否已关注某个用户
        boolean hasFollowed = false;
        if(hostHolder.getUser() != null){
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(),ENTITY_TYPE_USER,userId);
        }
        model.addAttribute("hasFollowed",hasFollowed);

        return "/site/profile";
    }
}
