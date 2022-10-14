package com.dmswide.nowcoder.dao;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Repository("alphaDaoMybatisImpl")
@Primary
public class AlphaDaoMybatisImpl implements AlphaDao {
    @Override
    public String select() {
        return "mybatis";
    }
}
