package com.dmswide.nowcoder;

import com.dmswide.nowcoder.dao.DiscussPostMapper;
import com.dmswide.nowcoder.dao.elasticsearch.DiscussPostRepository;
import com.dmswide.nowcoder.entity.DiscussPost;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = NowcoderApplication.class)
public class ElasticSearchTest {
    @Resource
    private DiscussPostMapper discussPostMapper;
    @Resource
    private DiscussPostRepository discussPostRepository;
    @Resource
    private ElasticsearchTemplate elasticsearchTemplate;

    //判断某个id的文档是否存在
    @Test
    public void testExist(){
        boolean res = discussPostRepository.existsById(231);
        System.out.println(res);
    }

    //一次插入一条数据 会自动创建不存在的索引
    @Test
    public void testInsert(){
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(241));
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(242));
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(243));
    }

    //一次插入多条数据
    @Test
    public void testInsertAll(){
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(101,0,100,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(102,0,100,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(103,0,100,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(111,0,100,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(112,0,100,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(131,0,100,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(132,0,100,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(133,0,100,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(134,0,100,0));
    }

    //修改一条数据:以覆盖的方式
    @Test
    public void testUpdate(){
        DiscussPost discussPost = discussPostMapper.selectDiscussPostById(231);
        discussPost.setContent("我是新人,打算使劲灌水");
        //title会被设置为null 这里相当于用新的discussPost去覆盖elasticsearch中的discussPost
        //discussPost.setTitle(null);
        discussPostRepository.save(discussPost);
    }

    //修改一条数据:以部分修改的方式
    /*@Test
    public void testUpdateWithModify() throws IOException {
        UpdateRequest request = new UpdateRequest("discusspost", String.valueOf(231));
        request.timeout("1s");
        DiscussPost discussPost = discussPostMapper.selectDiscussPostById(231);
        discussPost.setContent("我是新人,打算使劲灌水,使劲灌水,啊哈哈哈哈");
        //这里es的title不会被修改,保持不变 也不会被设置为null
        discussPost.setTitle(null);
        request.doc(JSON.toJSONString(discussPost), XContentType.JSON);
        UpdateResponse updateResponse = restHighLevelClient.update(request, RequestOptions.DEFAULT);
        //OK
        System.out.println(updateResponse.status());
    }*/

    //删除一条数据
    @Test
    public void testDelete(){
        discussPostRepository.deleteById(231);
    }

    //删除全部数据
    @Test
    public void testDeleteAll(){
        discussPostRepository.deleteAll();
    }

    //无高亮显示
    @Test
    public void testSearchByRepository(){
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
            //matchQuery()是模糊查询 searchSourceBuilder.query(QueryBuilders.matchQuery(key,value));对key进行分词
            //termQuery()是精准查询  searchSourceBuilder.query(QueryBuilders.matchQuery(key,value));对key进行分词
            .withQuery(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content"))
            //定义排序规则 排在前面的字段首先考虑 然后针对每个字段还需要考虑降序还是升序
            .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
            .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
            .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
            .withPageable(PageRequest.of(0, 10))
            .withHighlightFields(
                new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
            ).build();

        //底层调用的是elasticTemplate.queryForPage(searchQuery,class,SearchResultMapper)
        //获取到了高亮的值 但是没有进行返回

        Page<DiscussPost> page = discussPostRepository.search(searchQuery);
        //打印page的属性信息
        System.out.println(page.getTotalElements());
        System.out.println(page.getTotalPages());
        System.out.println(page.getNumber());
        System.out.println(page.getSize());

        for(DiscussPost post : page){
            System.out.println(post);
        }
    }


    @Test
    public void testSearchByTemplate(){
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
            .withQuery(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content"))
            .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
            .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
            .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
            .withPageable(PageRequest.of(0, 10))
            .withHighlightFields(
                new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
            ).build();

        Page<DiscussPost> page = elasticsearchTemplate.queryForPage(searchQuery, DiscussPost.class, new SearchResultMapper() {
            @Override
            public <T> AggregatedPage<T> mapResults(SearchResponse searchResponse, Class<T> aClass, Pageable pageable) {
                SearchHits hits = searchResponse.getHits();
                if(hits.getTotalHits() <= 0){
                    return null;
                }
                List<DiscussPost> list = new ArrayList<>();
                for(SearchHit hit : hits){
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

        //打印page的属性信息
        System.out.println(page.getTotalElements());
        System.out.println(page.getTotalPages());
        System.out.println(page.getNumber());
        System.out.println(page.getSize());

        for(DiscussPost post : page){
            System.out.println(post);
        }
    }

    /*//搜索不高亮显示
    @Test
    public void testNoHighlightQuery() throws IOException {
        SearchRequest searchRequest = new SearchRequest("discusspost");

        //构造搜索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
            //定义查询条件
            .query(QueryBuilders.multiMatchQuery("互联网寒冬","title","content"))
            //定义排序规则 排在前面的字段首先考虑 然后针对每个字段还需要考虑降序还是升序
            .sort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
            .sort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
            .sort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
            //可选项 .timeout(new TimeValue(60, TimeUnit.SECONDS))
            //指定查询的开始行数
            .from(0)
            //需要查询记录的总数
            .size(10);

        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        System.out.println(JSONObject.toJSON(searchResponse));

        List<DiscussPost> list = new LinkedList<>();
        for(SearchHit hit : searchResponse.getHits().getHits()){
            DiscussPost discussPost = JSONObject.parseObject(hit.getSourceAsString(), DiscussPost.class);
            System.out.println(discussPost);
            list.add(discussPost);
        }
    }*/

   /* //搜索并 高亮显示
    @Test
    public void testHighlightQuery() throws IOException {
        //创建对象传入discusspost参数
        SearchRequest searchRequest = new SearchRequest("discusspost");

        //设置查询结果高亮显示
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");
        highlightBuilder.field("content");
        highlightBuilder.requireFieldMatch(false);
        highlightBuilder.preTags("<span style = 'color:red'>");
        highlightBuilder.postTags("</span>");

        //构造搜索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
            //定义查询条件
            .query(QueryBuilders.multiMatchQuery("互联网寒冬","title","content"))
            //定义排序规则 排在前面的字段首先考虑 然后针对每个字段还需要考虑降序还是升序
            .sort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
            .sort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
            .sort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
            //可选项 .timeout(new TimeValue(60, TimeUnit.SECONDS))
            //指定查询的开始行数
            .from(0)
            //需要查询记录的总数
            .size(10)
            //设置高亮
            .highlighter(highlightBuilder);


        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        List<DiscussPost> list = new ArrayList<>();
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
            System.out.println(discussPost);
            list.add(discussPost);
        }
    }*/
}
