package com.meizu.hato

import org.gradle.api.Project

class HatoExtension {
    def debug
    def fromVersion
    def includePackage = []
    //def excludeClass = []
    HatoExtension(Project project) {}
}
