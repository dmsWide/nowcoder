package com.dmswide.nowcoder.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.UUID;

public class CommunityUtil {

    //UUID生成随机字符串
    public static String generateUUID(){
        return UUID.randomUUID().toString().replaceAll("_","");
    }
    //md5加密
    public static String md5(String key){
        if(StringUtils.isBlank(key)){
            return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }
}
