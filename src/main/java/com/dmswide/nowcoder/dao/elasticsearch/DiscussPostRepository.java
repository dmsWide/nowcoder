package com.dmswide.nowcoder.dao.elasticsearch;

import com.dmswide.nowcoder.entity.DiscussPost;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * 这里的注解只能使用@Repository
 * 继承接口 ElasticsearchRepository<DiscussPost,Integer>前者为处理的实体类 后者为实体类的主键类型
 * 接口内部不需要有任何方法 父接口ElasticsearchRepository已经定义好了对es服务器的增删改查操作
 * 加了注解后 spring会自动实现 直接调用  @Mapper是mybatis专有的注解，所以这里用@Repository
 */
@Repository
public interface DiscussPostRepository extends ElasticsearchRepository<DiscussPost,Integer> {

}
