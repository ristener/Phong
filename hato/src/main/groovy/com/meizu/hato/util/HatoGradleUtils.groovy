package com.meizu.hato.util

import com.meizu.hato.HatoExctption
import org.gradle.api.Project

/**
 * Created by jixin.jia on 15/11/10.
 */
class HatoGradleUtils {

    public static void generatePatch(Project project) {
        def gradlew = new File("../gradlew")
        if (gradlew.exists()) {
            def stdout = new ByteArrayOutputStream()
            project.exec {
                commandLine "../gradlew clean hatoPatches -P HatoDir=hato";
                standardOutput = stdout
            }
            def error = stdout.toString().trim()
            if (error) {
                println "dex error:" + error
            }
        } else {
            throw new HatoExctption('gradlew is not found')
        }
    }
}
