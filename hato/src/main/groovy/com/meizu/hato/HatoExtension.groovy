package com.meizu.hato

import org.gradle.api.Project

class HatoExtension {
    HashSet<String> includePackage = []
    HashSet<String> excludeClass = []
    String fromVersion
    String toVersion
    boolean askPatch
    HatoExtension(Project project) {
    }
}
