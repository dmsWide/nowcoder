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

    @Override
    public void saveDiscussPost(DiscussPost discussPost) {
        discussPostRepository.save(discussPost);
    }

    @Override
    public void deleteDiscussPost(int id) {
        discussPostRepository.deleteById(id);
    }

    @Override
    public Page<DiscussPost> searchDiscussPost(String keyword, int current, int limit) {
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
                    //??????????????? ???????????????
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

                    //???????????????????????????
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

    //???????????????????????? ???????????????????????????
   /* @Override
    public SearchResult searchDiscussPost(String keyword, int current, int limit) throws Exception {
        SearchRequest searchRequest = new SearchRequest("discusspost");

        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");
        highlightBuilder.field("content");
        highlightBuilder.requireFieldMatch(false);
        highlightBuilder.preTags("<em>");
        highlightBuilder.postTags("</em>");

        //??????????????????
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
            //??????????????????
            .query(QueryBuilders.multiMatchQuery(keyword,"title","content"))
            //?????????????????? ????????????????????????????????? ?????????????????????????????????????????????????????????
            .sort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
            .sort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
            .sort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
            //???????????????????????????
            .from(current)
            //???????????????????????????
            .size(limit)
            //????????????
            .highlighter(highlightBuilder);

        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        //???????????????????????????
        long count = searchResponse.getHits().getTotalHits().value;

        List<DiscussPost> list = new LinkedList<>();
        for(SearchHit hit : searchResponse.getHits().getHits()){
            DiscussPost discussPost = JSONObject.parseObject(hit.getSourceAsString(), DiscussPost.class);

            //???????????????????????????
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
