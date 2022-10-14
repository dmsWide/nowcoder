package com.dmswide.nowcoder.entity;

/**
 * 封装分页信息的组件
 */
public class Page {
    //当前页
    private Integer current = 1;
    //页面显示文章的上限
    private Integer limit = 10;
    //数组总数:用于计算总共分页的数目
    private Integer rows;
    //每个单一页面的路径
    private String path;

    public Integer getCurrent() {
        return current;
    }

    public void setCurrent(Integer current) {
        if(current >= 1){
            this.current = current;
        }
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        if(limit >= 1 && limit <= 100){
            this.limit = limit;
        }
    }

    public Integer getRows() {
        return rows;
    }

    public void setRows(Integer rows) {
        if(rows >= 0){
            this.rows = rows;
        }
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     *
     * @return 获取当前页的起始行
     */
    public int getOffset(){
        return (current - 1) * limit;
    }

    /**
     *
     * @return 获取总的页数
     */
    public int getTotal(){
        if(rows % limit == 0){
            return rows / limit;
        }else{
            return rows / limit + 1;
        }
    }

    /**
     *
     * @return 一段页面的起始页
     */
    public int getFrom(){
        int from = current - 2;
        return Math.max(from, 1);
    }

    /**
     *
     * @return 一段页面的终止页
     */
    public int getTo(){
        int to = current + 2;
        return Math.min(to, getTotal());
    }
}
