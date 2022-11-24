package com.dmswide.nowcoder.actuator;

import com.dmswide.nowcoder.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

//自定义端点:监视数据库连接是否正常,还需要在security中进行权限管理即可
//使用管理员身份登录之后 访问路径 http://localhost:8080/community/actuator/database 即可访问该端点
@Component
@Endpoint(id = "database")
public class DatabaseEndpoint {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseEndpoint.class);
    //思路就是获取一次连接，尝试访问一次数据库，如果可以访问到就说明数据库连接正确
    @Resource
    private DataSource dataSource;

    @ReadOperation
    public String checkConnection(){
        try(
            Connection connection = dataSource.getConnection()
        ){
            return CommunityUtil.getJSONString(0,"获取连接成功!");
        }catch (SQLException e){
            logger.error("获取连接失败" + e.getMessage());
            return CommunityUtil.getJSONString(1,"获取连接失败！");
        }
    }
}
