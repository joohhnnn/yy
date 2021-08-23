package com.txznet.plugin.resolution

import org.gradle.api.Plugin
import org.gradle.api.Project

class ResolutionSupporter implements Plugin<Project> {
    @Override
    void apply(Project project) {
        println "ResolutionSupporter on apply"

        project.extensions.create("resolution", Resolution)
        project.task("generateResolutionResources") {
            doLast {
                Resolution info = project.extensions.getByName("resolution")
                info.generateResources(project.projectDir.path)
            }
        }

        project.tasks.getByName("preBuild").dependsOn project.tasks.getByName("generateResolutionResources")
    }
}