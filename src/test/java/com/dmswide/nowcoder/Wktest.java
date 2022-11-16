package com.dmswide.nowcoder;

import java.io.IOException;

public class Wktest {
    public static void main(String[] args) {
        String cmd = "E:/Wkhtmltopdf/installation/wkhtmltopdf/bin/wkhtmltoimage --quality 75 https://www.nowcoder.com/ E:/Wkhtmltopdf/wk-images/1.png";
        try {
            //java主线程和操作系统调用命令生成图片是异步的
            Runtime.getRuntime().exec(cmd);
            System.out.println("ok");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
