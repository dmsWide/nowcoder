package com.dmswide.nowcoder.service.impl;

import com.dmswide.nowcoder.dao.elasticsearch.DiscussPostRepository;
import com.dmswide.nowcoder.entity.DiscussPost;
import com.dmswide.nowcoder.service.ElasticsearchService;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.naming.directory.SearchResult;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
@Service
public class ElasticsearchServiceImpl implements ElasticsearchService {
    @Resource
    private DiscussPostRepository discussPostRepository;
    @Resource
    private ElasticsearchTemplate elasticsearchTemplate;

    //发布帖子时往es中增加帖子 对帖子评论时更新es服务器中帖子的commentcount 可以用saveDiscussPost实现
    @Override
    public void saveDiscussPost(DiscussPost discussPost) {
        discussPostRepository.save(discussPost);
    }

    //删除帖子
    @Override
    public void deleteDiscussPost(int id) {
        discussPostRepository.deleteById(id);
    }

    //搜索帖子，传入关键词，分页条件
    //current是当前页从0开始，limit是每页显示多少条数据
    //我们自己写的page的current是从1开始
    @Override
    public Page<DiscussPost> searchDiscussPost(String keyword, int current, int limit) {
        //构造查询条件DML
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
            .withQuery(QueryBuilders.multiMatchQuery(keyword, "title", "content"))
            .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
            .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
            .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
            .withPageable(PageRequest.of(current, limit))
            .withHighlightFields(
                new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
            ).build();

        return elasticsearchTemplate.queryForPage(searchQuery, DiscussPost.class, new SearchResultMapper() {
            @Override
            public <T> AggregatedPage<T> mapResults(SearchResponse searchResponse, Class<T> aClass, Pageable pageable) {
                SearchHits hits = searchResponse.getHits();
                if(hits.getTotalHits() <= 0){
                    return null;
                }
                List<DiscussPost> list = new ArrayList<>();
                for(SearchHit hit : hits){
                    //实例化实体 放到集合中
                    DiscussPost post = new DiscussPost();

                    String id = hit.getSourceAsMap().get("id").toString();
                    post.setId(Integer.parseInt(id));

                    String userId = hit.getSourceAsMap().get("userId").toString();
                    post.setUserId(Integer.parseInt(userId));

                    String title = hit.getSourceAsMap().get("title").toString();
                    post.setTitle(title);

                    String content = hit.getSourceAsMap().get("content").toString();
                    post.setContent(content);

                    String status = hit.getSourceAsMap().get("status").toString();
                    post.setStatus(Integer.parseInt(status));

                    String createTime = hit.getSourceAsMap().get("createTime").toString();
                    post.setCreateTime(new Date(Long.parseLong(createTime)));

                    String commentCount = hit.getSourceAsMap().get("commentCount").toString();
                    post.setCommentCount(Integer.parseInt(commentCount));

                    //处理高亮显示的结果
                    HighlightField titleField = hit.getHighlightFields().get("title");
                    if(titleField != null){
                        post.setTitle(titleField.getFragments()[0].toString());
                    }

                    HighlightField contentField = hit.getHighlightFields().get("content");
                    if(contentField != null){
                        post.setContent(contentField.getFragments()[0].toString());
                    }
                    list.add(post);
                }
                return new AggregatedPageImpl(list,pageable,hits.getTotalHits(),searchResponse.getAggregations(),
                    searchResponse.getScrollId(),hits.getMaxScore());
            }
        });
    }

    //方法实现的有问题 但是不知道怎么修改
   /* @Override
    public SearchResult searchDiscussPost(String keyword, int current, int limit) throws Exception {
        SearchRequest searchRequest = new SearchRequest("discusspost");

        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");
        highlightBuilder.field("content");
        highlightBuilder.requireFieldMatch(false);
        highlightBuilder.preTags("<em>");
        highlightBuilder.postTags("</em>");

        //构造搜索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
            //定义查询条件
            .query(QueryBuilders.multiMatchQuery(keyword,"title","content"))
            //定义排序规则 排在前面的字段首先考虑 然后针对每个字段还需要考虑降序还是升序
            .sort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
            .sort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
            .sort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
            //指定查询的开始行数
            .from(current)
            //需要查询记录的总数
            .size(limit)
            //设置高亮
            .highlighter(highlightBuilder);

        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        //获取查询到的总个数
        long count = searchResponse.getHits().getTotalHits().value;

        List<DiscussPost> list = new LinkedList<>();
        for(SearchHit hit : searchResponse.getHits().getHits()){
            DiscussPost discussPost = JSONObject.parseObject(hit.getSourceAsString(), DiscussPost.class);

            //处理高亮显示的结果
            HighlightField titleField = hit.getHighlightFields().get("title");
            if(titleField != null){
                discussPost.setTitle(titleField.getFragments()[0].toString());
            }
            HighlightField contentField = hit.getHighlightFields().get("content");
            if(contentField != null){
                discussPost.setContent(contentField.getFragments()[0].toString());
            }
            //System.out.println(discussPost);
            list.add(discussPost);
        }
        return new SearchResult(list,count);
    }*/
}
