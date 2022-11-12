package com.dmswide.nowcoder.service;

import java.util.Date;

public interface StatisticService {
    // TODO: 2022/11/12 dmsWide 指定的ip存入UV
    void recordUV(String ip);

    // TODO: 2022/11/12 dmsWide 统计指定范围内的日期的UV
    long calculateUV(Date start,Date end);

    // TODO: 2022/11/12 dmsWide 指定用户计入DAU
    void recordDAU(Integer userId);

    // TODO: 2022/11/12 dmsWide 统计指定日期范围内的DAU
    Long calculateDAU(Date start,Date end);
}
