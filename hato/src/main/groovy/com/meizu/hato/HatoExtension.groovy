package com.meizu.hato

import org.gradle.api.Project

class HatoExtension {
    HashSet<String> includePackage = []
    HashSet<String> excludeClass = []
    boolean debugOn = true

    HatoExtension(Project project) {
    }
}
