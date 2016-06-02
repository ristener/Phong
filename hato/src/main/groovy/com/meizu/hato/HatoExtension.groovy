package com.meizu.hato

import org.gradle.api.Project

class HatoExtension {
    def targetVersion
    def targetApp
    def askPatch
    def includePackage = []
    def excludeClass = []

    HatoExtension(Project project) {
    }
}
