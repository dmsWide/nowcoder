package com.dmswide.nowcoder.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveWordsFilter {
    private static final Logger logger = LoggerFactory.getLogger(SensitiveWordsFilter.class);
    private static final String REPLACEMENT = "***";
    private final TrieNode root = new TrieNode();

    @PostConstruct
    public void init(){
        try(
            InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))
        ){
            String keyword;
            while ((keyword = bufferedReader.readLine()) != null){
                this.addKeyWord(keyword);
            }
        }catch (IOException e){
            logger.error("读取敏感词文件失败"+e.getMessage());
        }

    }

    private void addKeyWord(String keyword){
        TrieNode node = root;
        for(int i = 0;i < keyword.length();i++){
            char c = keyword.charAt(i);
            TrieNode subNode = node.getSubNode(c);
            if(subNode == null){
                subNode = new TrieNode();
                node.addSubNode(c,subNode);
            }
            node = subNode;
            if(i == keyword.length() - 1){
                node.setKeywordEnd(true);
            }
        }
    }


    public String filter(String text){
        if(StringUtils.isBlank(text)){
            return null;
        }
        //指针1
        TrieNode node = root;
        //指针2、3
        int begin= 0,position = 0;
        //执行结果
        StringBuilder stringBuilder = new StringBuilder();

        while(begin < text.length()){
            if(position < text.length()){
                char c = text.charAt(position);

                //跳过符号
                if(isSymbol(c)){
                    //若指针1处于根节点 将符号计入根节点 指针2向下走一步
                    if(node == root){
                        stringBuilder.append(c);
                        begin++;
                    }
                    //无论符号在开头还是中间 指针三都往下走
                    position++;
                    continue;
                }

                //检查下级节点
                node = node.getSubNode(c);
                if(node == null){
                    //以begin开头的字符串不是敏感词
                    stringBuilder.append(text.charAt(begin));
                    begin++;
                    position = begin;
                    //重新指向根节点
                    node = root;
                }else if(node.isKeywordEnd()){
                    //发现了敏感词 [begin,position]进行替换
                    stringBuilder.append(REPLACEMENT);
                    position++;
                    begin = position;
                    //重新指向根节点
                    node = root;

                }else{
                    position++;
                }

            }else{
                //position 遍历越界仍未发现敏感词
                stringBuilder.append(text.charAt(begin));
                begin++;
                position = begin;
                node = root;
            }
        }
        return stringBuilder.toString();
    }

    private boolean isSymbol(Character character){
        //[0x2E80,0x9FFF]东亚文字
        return !CharUtils.isAsciiAlphanumeric(character) && (character < 0x2E80 || character > 0x9FFF);
    }
    private static class TrieNode{
        private boolean isKeywordEnd = false;
        //key和value分别表示:下级字符和下级子节点
        private final Map<Character,TrieNode> subNodes = new HashMap<>();

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        public void addSubNode(Character character,TrieNode trieNode){
            subNodes.put(character,trieNode);
        }

        public TrieNode getSubNode(Character character){
            return subNodes.get(character);
        }
    }
}
