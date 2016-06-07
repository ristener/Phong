package com.meizu.hato

import org.gradle.api.Project

class HatoExtension {
    def debug
    def enable
    def patch = [] //patch的配置信息
    def includePackage = []
    def excludeClass = []

    HatoExtension(Project project) {}
}
