package me.ele.napos.evalon.demo.descriptor.service;

import me.ele.napos.vine.descriptor.annotation.soa.SoaService;

/**
 * 这是一个新创建的服务
 */
@SoaService
public interface SimpleService {
    /**
     * 这是一个新创建的方法
     * @param param_1 param1
     * @param param_2 param2
     * @return A String Response
     */
    String simpleMethod_1(String param_1, int param_2);
}