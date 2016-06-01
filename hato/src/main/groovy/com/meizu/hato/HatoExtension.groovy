package com.meizu.hato

import org.gradle.api.Project

class HatoExtension {
    boolean askPatch
    String targetVersion
    HashSet<String> includePackage = []
    HashSet<String> excludeClass = []
    HatoExtension(Project project) {
    }
}
