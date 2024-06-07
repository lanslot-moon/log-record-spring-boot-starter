package com.tuya.solution.record.service;


import com.tuya.solution.record.beans.Operator;

/**
 * @author Violet（wangli.liu@tuya.com）
 * @describe 用户获取Service
 * @since 2024/6/5 14:02
 */
public interface IOperatorGetService {

    /**
     * 可以在里面外部的获取当前登陆的用户，比如UserContext.getCurrentUser()
     *
     * @return 转换成Operator返回
     */
    Operator getUser();
}
