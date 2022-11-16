package com.dmswide.nowcoder.controller;

import com.dmswide.nowcoder.entity.Event;
import com.dmswide.nowcoder.event.EventProducer;
import com.dmswide.nowcoder.util.CommunityConstant;
import com.dmswide.nowcoder.util.CommunityUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

@Controller
public class ShareController implements CommunityConstant{
    private static final Logger logger = LoggerFactory.getLogger(ShareController.class);
    @Value("${nowcoder.path.domain}")
    private String domain;
    @Value("${server.servlet.context-path}")
    private String contextPath;
    @Value("${wk.image.storage}")
    private String wkImageStorage;
    @Resource
    private EventProducer eventProducer;

    /** 长图接口的访问需要手动填写url来访问
     *  http://localhost:8080/community/share?htmlUrl=https://www.nowcoder.com/
     * @param htmlUrl url地址
     * @return
     */
    @GetMapping("/share")
    @ResponseBody
    public String share(String htmlUrl){
        String fileName = CommunityUtil.generateUUID();

        //异步生成长图
        Event event = new Event()
            .setTopic(TOPIC_SHARE)
            .setData("htmlUrl",htmlUrl)
            .setData("fileName",fileName)
            .setData("suffix",".png");

        eventProducer.fireEvent(event);
        //返回访问呢路径
        Map<String,Object> map = new HashMap<>();
        map.put("shareUrl",domain + contextPath + "/share/image/" + fileName);
        return CommunityUtil.getJSONString(0,null,map);
    }

    //获取长途，通过response向浏览器输出长图
    @GetMapping("/share/image/{fileName}")
    public void getShareImage(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        if (StringUtils.isBlank(fileName)) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        response.setContentType("image/png");
        File file = new File(wkImageStorage + "/" + fileName + ".png");
        try {
            OutputStream outputStream = response.getOutputStream();
            FileInputStream fileInputStream = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            int b = 0;
            while((b = fileInputStream.read(buffer)) != -1){
                outputStream.write(buffer,0,b);
            }
        } catch (IOException e) {
            logger.error("获取长图失败: " + e.getMessage());
        }
    }
}
