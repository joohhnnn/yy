package com.txznet.webchat.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * 微信插件使用的gradle plugin
 *
 * 负责处理微信插件工程的task注入和发布模式下的打包流程控制
 *
 * 微信插件分预置和发布两种模式
 * 预置模式: 直接采用library依赖方式, 微信主工程直接依赖插件module
 * 发布模式: 插件module打包为插件jar包, 由微信主工程控制装载
 *
 * 为避免发布模式下的插件与对应的预置插件发生包名冲突, 约定两种模式下插件采用不同的包名前缀
 * 预置 -> com.txznet.webchat.plugin.preset
 * 发布 -> com.txznet.webchat.plugin
 *
 * 微信插件发布模式打包流程
 *    1. assemble生成jar包
 *    2. 生成的jar包修改包名(com.txznet.webchat.plugin.preset -> com.txznet.webchat.plugin)
 *    3. 对修改包名后的jar包进行dex打包
 *
 * Created by J on 2018/8/20.
 */

public class WxPlugin implements Plugin<Project> {
    static final def EXTENSION_NAME = "WxPlugin"

    @Override
    void apply(final Project project) {
        // create plugin extension
        project.extensions.create(EXTENSION_NAME, WxPluginInfo)
        // create plugin tasks
        project.afterEvaluate {
            WxPluginInfo pluginInfo = project.extensions.getByName(EXTENSION_NAME)
            WxPluginTaskInjector.injectPluginTasks(project, pluginInfo)
        }
    }
}
