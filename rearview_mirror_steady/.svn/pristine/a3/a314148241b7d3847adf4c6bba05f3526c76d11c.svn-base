package com.txznet.webchat.plugin

import com.tonicsystems.jarjar.Main
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Exec

/**
 * 插件task注入工具
 *
 * 微信插件Plugin会注入用于编译和调试插件的相关方法, 比较重要的几个:
 * wxpAssembleRelease/wxpAssembleDebug:
 *     插件打包task, 输出release/debug模式的插件包
 * wxpTestDebug/wxoTestRelease:
 *     插件调试task, 打包插件+清空设备插件调试目录+push插件至设备调试目录+重启微信进程
 */
class WxPluginTaskInjector {
    static final def GROUP_WXP_BUILD = "wxp_build"
    static final def TASK_ASSEMBLE_JAR_DEBUG = 'wxpAssembleJarDebug'
    static final def TASK_ASSEMBLE_JAR_RELEASE = 'wxpAssembleJarRelease'
    static final def TASK_GENERATE_RULES = 'generateModPackageRules'
    static final def TASK_MOD_PACKAGE = 'wxpModPackageName'
    static final def TASK_ASSEMBLE_PLUGIN_DEBUG = 'wxpAssembleDebug'
    static final def TASK_ASSEMBLE_PLUGIN_RELEASE = 'wxpAssembleRelease'
    // const def for plugin testing
    static final def GROUP_WXP_TEST = "wxp_test"
    static final def TASK_TEST_CLEAN_DIR = "wxpCleanPluginTestDir"
    static final def TASK_TEST_DEPLOY_PLUGIN = "wxpDeployTestPlugin"
    static final def TASK_TEST_KILL_PROCESS = "wxpKillWechatProcess"
    static final def TASK_TEST_RESTART_PROCESS = "wxpRestartWechatProcess"
    static final def TASK_TEST_START_DEBUG = "wxpTestDebug"
    static final def TASK_TEST_START_RELEASE = "wxpTestRelease"

    static void injectPluginTasks(Project project, WxPluginInfo pluginInfo) {
        println "checking sdk environments..."
        def extAndroid = project.extensions.getByName("android")
        def dxExe = "${extAndroid.sdkDirectory}\\build-tools\\${extAndroid.buildToolsVersion}\\dx.bat"
        def adbExe = extAndroid.adbExe
        println "dx executable path = ${dxExe}"
        println "adb executable path = ${adbExe}"

        project.ext.wxpDebugBuild = true
        // 生成debug插件jar包task
        project.task([type: Copy, group: GROUP_WXP_BUILD], TASK_ASSEMBLE_JAR_DEBUG) {
            from(getInputPath(project, pluginInfo, true)) {
                include "classes.jar"
            }
            into getOutputPath(project, pluginInfo, true)
            rename { String fileName ->
                "classes_raw#${pluginInfo.pluginVersionName}.jar"
            }

            doFirst {
                project.ext.wxpDebugBuild = true
                println "wxpAssembleJarDebug current debug = ${project.ext.wxpDebugBuild}"
            }
        }

        // 生成release插件jar包的task
        project.task([type: Copy, group: GROUP_WXP_BUILD], TASK_ASSEMBLE_JAR_RELEASE) {
            from(getInputPath(project, pluginInfo, false)) {
                include "classes.jar"
            }
            from("${project.projectDir.path}/build/outputs/mapping/${pluginInfo.releaseBuildType}") {
                include "mapping.txt"
            }
            into getOutputPath(project, pluginInfo, false)
            rename { String fileName ->
                if (fileName.equals("classes.jar")) {
                    "classes_raw#${pluginInfo.pluginVersionName}.jar"
                } else if (fileName.equals("mapping.txt")) {
                    "mapping#${pluginInfo.pluginVersionName}.txt"
                }
            }

            doFirst {
                project.ext.wxpDebugBuild = false
                println "wxpAssembleJarRelease current debug = ${project.ext.wxpDebugBuild}"
            }
        }

        // 生成修改jar包包名的rules文件
        project.task(group: GROUP_WXP_BUILD, TASK_GENERATE_RULES) {
            def rule = "rule ${pluginInfo.presetPackageName}.** " +
                    "${project.ext.wxpDebugBuild ? pluginInfo.debugPackageName : pluginInfo.releasePackageName}.@1"
            def rulePath = "${project.buildDir}" + "/wxp"
            new File(rulePath).mkdirs()
            new FileOutputStream(rulePath + "/mod_package_rule.txt").write(rule.getBytes("UTF-8"))
        }

        // 修改包名task
        project.task([dependsOn: project.tasks.getByName(TASK_GENERATE_RULES),
                      group    : GROUP_WXP_BUILD],
                TASK_MOD_PACKAGE) {
            doLast {
                new Main().process(new File("${project.buildDir}/wxp/mod_package_rule.txt "),
                new File("${getOutputPath(project, pluginInfo, project.ext.wxpDebugBuild)}/" +
                        "classes_raw#${pluginInfo.pluginVersionName}.jar"),
                new File("${getOutputPath(project, pluginInfo, project.ext.wxpDebugBuild)}/" +
                        "classes_raw_mod#${pluginInfo.pluginVersionName}.jar"))
            }
        }

        // 生成debug插件包的task
        project.task([type     : Exec,
                      dependsOn: [project.tasks.getByName(TASK_ASSEMBLE_JAR_DEBUG),
                                  project.tasks.getByName(TASK_MOD_PACKAGE)],
                      group    : GROUP_WXP_BUILD],
                TASK_ASSEMBLE_PLUGIN_DEBUG) {
            doFirst {
                commandLine 'cmd', '/c', "${dxExe} --dex --output " +
                        "${getOutputPath(project, pluginInfo, project.ext.wxpDebugBuild)}" +
                        "/${pluginInfo.pluginName}#${pluginInfo.pluginVersionName}.jar " +
                        "${getOutputPath(project, pluginInfo, project.ext.wxpDebugBuild)}" +
                        "/classes_raw_mod#${pluginInfo.pluginVersionName}.jar"
            }
        }

        // 生成release插件包的task
        project.task([type     : Exec,
                      dependsOn: [project.tasks.getByName(TASK_ASSEMBLE_JAR_RELEASE),
                                  project.tasks.getByName(TASK_MOD_PACKAGE)],
                      group    : GROUP_WXP_BUILD],
                TASK_ASSEMBLE_PLUGIN_RELEASE) {
            doFirst {
                commandLine 'cmd', '/c', "${dxExe} --dex --output " +
                        "${getOutputPath(project, pluginInfo, project.ext.wxpDebugBuild)}" +
                        "/${pluginInfo.pluginName}#${pluginInfo.pluginVersionName}.jar " +
                        "${getOutputPath(project, pluginInfo, project.ext.wxpDebugBuild)}" +
                        "/classes_raw_mod#${pluginInfo.pluginVersionName}.jar"
            }
        }

        // ------------------------- 插件debug相关task ------------------------------

        // 清除插件测试目录
        project.task([type: Exec, group: GROUP_WXP_TEST], TASK_TEST_CLEAN_DIR) {
            doFirst {
                println("cleaning plugin test dir..")
            }

            commandLine 'cmd', '/c', "${adbExe} shell rm -rf /sdcard/txz/webchat/plugin"
        }

        // 部署测试插件
        project.task([type     : Exec,
                      dependsOn: project.tasks.getByName(TASK_TEST_CLEAN_DIR),
                      group    : GROUP_WXP_TEST],
                TASK_TEST_DEPLOY_PLUGIN) {
            doFirst {
                def pluginName = "${pluginInfo.pluginName}#${pluginInfo.pluginVersionName}.jar"
                def outDir = "${getOutputPath(project, pluginInfo, project.ext.wxpDebugBuild)}/" +
                        "$pluginName"
                println("deploying plugin: [$pluginName] to test dir..")
                commandLine 'cmd', '/c',
                        "${adbExe} -d push $outDir /sdcard/txz/webchat/plugin/$pluginName"
            }
        }

        // 关闭微信进程
        project.task([type: Exec, group: GROUP_WXP_TEST], TASK_TEST_KILL_PROCESS) {
            commandLine 'cmd', '/c', "${adbExe} shell am force-stop com.txznet.webchat"
        }

        // 重启微信进程
        project.task([type     : Exec,
                      dependsOn: project.tasks.getByName(TASK_TEST_KILL_PROCESS)],
                TASK_TEST_RESTART_PROCESS) {
            doFirst {
                println("restarting wechat process..")
                commandLine 'cmd', '/c',
                        "${adbExe} shell am start -n com.txznet.webchat/.ui.AppStartActivity"
            }
        }

        project.task([dependsOn: [project.tasks.getByName(TASK_ASSEMBLE_PLUGIN_DEBUG),
                                  project.tasks.getByName(TASK_TEST_DEPLOY_PLUGIN),
                                  project.tasks.getByName(TASK_TEST_RESTART_PROCESS)],
                      group    : GROUP_WXP_TEST],
                TASK_TEST_START_DEBUG) {
            project.ext.wxpDebugBuild = true
        }

        project.task([dependsOn: [project.tasks.getByName(TASK_ASSEMBLE_PLUGIN_RELEASE),
                                  project.tasks.getByName(TASK_TEST_DEPLOY_PLUGIN),
                                  project.tasks.getByName(TASK_TEST_RESTART_PROCESS)],
                      group    : GROUP_WXP_TEST],
                TASK_TEST_START_RELEASE) {
            project.ext.wxpDebugBuild = false
        }

        // 处理task依赖关系
        project.tasks.getByName(TASK_ASSEMBLE_JAR_DEBUG)
                .dependsOn getAssembleTaskByBuildType(project, pluginInfo.debugBuildType)
        project.tasks.getByName(TASK_ASSEMBLE_JAR_RELEASE)
                .dependsOn getAssembleTaskByBuildType(project, pluginInfo.releaseBuildType)
    }

    static getInputPath(Project project, WxPluginInfo pluginInfo, boolean isDebugMode) {
        if (isDebugMode) {
            "${project.projectDir.path}/build/intermediates/" +
                    "packaged-classes/${pluginInfo.debugBuildType}"
        } else {
            "${project.projectDir.path}/build/intermediates/" +
                    "packaged-classes/${pluginInfo.releaseBuildType}"
        }
    }

    static getOutputPath(Project project, WxPluginInfo info, boolean isDebugMode) {
        if (isDebugMode) {
            "${project.projectDir.path}${info.debugOutPath}"
        } else {
            "${project.projectDir.path}${info.releaseOutPath}"
        }
    }

    static Task getAssembleTaskByBuildType(Project project, String buildType) {
        String taskName = "assemble" + buildType.replaceFirst(buildType.charAt(0).toString(),
                buildType.charAt(0).toUpperCase().toString())
        return project.tasks.getByName(taskName)
    }
}