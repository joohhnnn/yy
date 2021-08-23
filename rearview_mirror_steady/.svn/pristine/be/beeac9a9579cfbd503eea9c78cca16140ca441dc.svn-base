package com.txznet.rxflux;

import android.support.annotation.MainThread;

/**
 * 在Flux架构上，进行一定的调整，明确约束了每个模块之间的角色定位
 * 推荐以现实中物流(伪)的形式去理解RxFlux
 * 用户通过终端寄件，样件最终送到物流中转站点，经过一定工序处理(包装、检视)/经过某个部门处理，然后再转发到收件方。
 * <p>
 * View-> Action ->(n..1) Dispatcher ->(1..n) Workflow ->
 * View: 负责通过ActionCreator产生事件，通过订阅Store刷新界面(推荐使用LiveData)
 * Dispatcher: 事件分发器
 * Workflow: 工作流中的处理模块，负责异步处理业务，并响应结果
 * Store: 系统中，关于某一块特定逻辑的数据临时存放中心。
 * <p>
 * View产生的Action会流到各个Workflow中，
 * Workflow允许丢弃Action，丢弃后则不会传达到各个Store，
 * 若所有Workflow对该Action都没有丢弃行为，则Action默认会流到Store中
 *
 * @author https://facebook.github.io/flux/ & zakchou & telnewbie
 */
public class RxFlux {

    private RxFlux() {

    }

    /**
     * 初始化工作流模块，推荐在Application执行
     */
    @MainThread
    public static void initWorkflow(RxWorkflow... workflows) {
        Dispatcher.get().register(workflows);
    }
}
