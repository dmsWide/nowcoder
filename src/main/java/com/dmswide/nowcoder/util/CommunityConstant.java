package com.dmswide.nowcoder.util;

/**
 * 激活账户的一些状态
 * 激活成功 激活失败 重复激活
 */
public interface CommunityConstant {
    /**
     * 账户激活的状态
     */
    int ACTIVATION_SUCCESS = 0;

    int ACTIVATION_REPEAT = 1;

    int ACTIVATION_FAILURE = 2;
}
