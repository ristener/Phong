package com.meizu.hato

import com.meizu.hato.util.*
import org.apache.commons.io.FileUtils
import org.gradle.api.Plugin
import org.gradle.api.Project

class HatoPlugin implements Plugin<Project> {
    public static def debug
    String appName = "someApp"
    def fromVersion
    HashSet<String> includePackage
    //HashSet<String> excludeClass //改造后,没有实际作用了
    def patchList = []
    // def beforeDexTasks = []

    //  private static final String HUATO_PATCHES = "hatoPatches"
    private static final String MAPPING_TXT = "mapping.txt"
    private static final String HASH_TXT = "hash.txt"
    private static final String RELEASE = "release"

    @Override
    void apply(Project project) {
        project.extensions.create("hato", HatoExtension, project)
        project.afterEvaluate {
            //拿到脚本传过来的过滤包信息
            def extension = project.extensions.findByName("hato") as HatoExtension
            debug = extension.debug;
            fromVersion = extension.fromVersion;
            includePackage = extension.includePackage
            //excludeClass = extension.excludeClass

            HatoFileUtils.HATO_DIR_PATH = debug ? HatoDir.DEBUG_PATH : HatoDir.RELEASE_PATH;
            dumpln('debug = ' + debug + ', HATO_DIR_PATH = ' + HatoFileUtils.HATO_DIR_PATH);

            def appInfo = HatoAndroidUtils.getAppInfo(project);
            HatoPlugin.dumpln(appInfo.toString())

            if (appInfo) {
                def pkgName = appInfo.pkgName
                if (pkgName) {
                    includePackage.add(pkgName)
                    appName = pkgName.substring(pkgName.lastIndexOf('.') + 1)
                }
                /*def applicationClass = appInfo.applicationClass
                if (applicationClass) {
                    excludeClass.add(applicationClass)
                }*/

                def versionCode = appInfo.versionCode
                if (versionCode) {
                    if (HatoStringUtils.isNull(fromVersion)) {
                        fromVersion = versionCode
                    }
                }
                dumpln("versionCode=" + versionCode + ",appName=" + appName)
            }


            project.android.applicationVariants.each { variant ->
                if (variant.name.contains(RELEASE) || variant.name.contains(RELEASE.capitalize()) || debug) {
                    Map hashMap
                    File hatoDir
                    File patchDir

                    def variantName = variant.name.capitalize();
                    def preDexTask = project.tasks.findByName("preDex${variantName}")
                    def dexTask = project.tasks.findByName("dex${variantName}")
                    def proguardTask = project.tasks.findByName("proguard${variantName}")
                    def oldHatoDir = HatoFileUtils.getVersionDir(appName, fromVersion)

                    if (oldHatoDir) {
                        def mappingFile = HatoFileUtils.getVariantFile(oldHatoDir, variant, MAPPING_TXT)
                        if (mappingFile) {
                            HatoAndroidUtils.applymapping(proguardTask, mappingFile)
                        }
                        def hashFile = HatoFileUtils.getVariantFile(oldHatoDir, variant, HASH_TXT)
                        if (hashFile) {
                            hashMap = HatoMapUtils.parseMap(hashFile)
                        }
                    }

                    def dirName = variant.dirName
                    hatoDir = new File("${project.buildDir}/outputs/hato")

                    def outputDir = new File("${hatoDir}/${dirName}")
                    def hashFile = new File(outputDir, "hash.txt")

                    Closure hatoPrepareClosure = {
                        outputDir.mkdirs()
                        if (!hashFile.exists()) {
                            hashFile.createNewFile()
                        }

                        if (oldHatoDir) { //need make a patch
                            patchDir = new File("${hatoDir}/${dirName}/patch")
                            patchDir.mkdirs()
                            patchList.add(patchDir)
                        }
                    }


                    def hatoPatch = "hato${variant.name.capitalize()}Patch"
                    project.task(hatoPatch) << {
                        if (patchDir) {
                            HatoAndroidUtils.dex(project, patchDir, appName, fromVersion)
                        }
                    }
                    def hatoPatchTask = project.tasks[hatoPatch]

                    Closure copyMappingClosure = {
                        def targetPath = "/${appName}/${fromVersion}/${dirName}"
                        if (proguardTask) {
                            def mapFile = new File("${project.buildDir}/outputs/mapping/${variant.dirName}/mapping.txt")
                            def newMapFile = new File("${hatoDir}/${variant.dirName}/mapping.txt");
                            if (mapFile) {
                                FileUtils.copyFile(mapFile, newMapFile)
                                HatoFileUtils.copy2SpecialDir(project, targetPath, mapFile)
                            }
                        }

                        def hashText = new File("${project.buildDir}/outputs/hato/${variant.dirName}/hash.txt")
                        if (hashText) {
                            HatoFileUtils.copy2SpecialDir(project, targetPath, hashText)
                        }
                    }

                    if (preDexTask) {//没有开启Multidex
                        def hatoJarBeforePreDex = "hatoJarBeforePreDex${variant.name.capitalize()}"
                        project.task(hatoJarBeforePreDex) << {
                            Set<File> inputFiles = preDexTask.inputs.files.files
                            inputFiles.each { inputFile ->
                                def path = inputFile.absolutePath
                                //  dumpln("hatoJarBeforePreDex:" + path)
                                if (HatoProcessor.shouldProcessPreDexJar(path)) {
                                    HatoProcessor.processJar(hashFile, inputFile, patchDir, hashMap, includePackage)
                                }
                            }
                        }
                        def hatoJarBeforePreDexTask = project.tasks[hatoJarBeforePreDex]
                        hatoJarBeforePreDexTask.dependsOn preDexTask.taskDependencies.getDependencies(preDexTask)
                        preDexTask.dependsOn hatoJarBeforePreDexTask
                        hatoJarBeforePreDexTask.doFirst(hatoPrepareClosure)

                        //before Dex task,
                        def hatoClassBeforeDex = "hatoClassBeforeDex${variant.name.capitalize()}"
                        project.task(hatoClassBeforeDex) << {
                            Set<File> inputFiles = dexTask.inputs.files.files
                            inputFiles.each { inputFile ->
                                //  dumpln("hatoClassBeforeDex:" + inputFile.absolutePath)
                                HatoProcessor.processClass(inputFile, hashFile, hashMap, patchDir, dirName, includePackage)
                            }
                        }
                        def hatoClassBeforeDexTask = project.tasks[hatoClassBeforeDex]
                        hatoClassBeforeDexTask.dependsOn dexTask.taskDependencies.getDependencies(dexTask)
                        dexTask.dependsOn hatoClassBeforeDexTask
                        hatoClassBeforeDexTask.doLast(copyMappingClosure)

                        hatoPatchTask.dependsOn hatoClassBeforeDexTask
                        // beforeDexTasks.add(hatoClassBeforeDexTask)
                    } else {    //开启Multidex
                        def hatoJarBeforeDex = "hatoJarBeforeDex${variant.name.capitalize()}"
                        project.task(hatoJarBeforeDex) << {
                            Set<File> inputFiles = dexTask.inputs.files.files
                            inputFiles.each { inputFile ->
                                def path = inputFile.absolutePath
                                // dumpln("hatoJarBeforeDex:" + path)
                                if (path.endsWith(".jar")) {
                                    HatoProcessor.processJar(hashFile, inputFile, patchDir, hashMap, includePackage)
                                }
                            }
                        }
                        def hatoJarBeforeDexTask = project.tasks[hatoJarBeforeDex]
                        hatoJarBeforeDexTask.dependsOn dexTask.taskDependencies.getDependencies(dexTask)
                        dexTask.dependsOn hatoJarBeforeDexTask

                        hatoJarBeforeDexTask.doFirst(hatoPrepareClosure)
                        hatoJarBeforeDexTask.doLast(copyMappingClosure)

                        hatoPatchTask.dependsOn hatoJarBeforeDexTask
                        // beforeDexTasks.add(hatoJarBeforeDexTask)
                    }

                    def assembleTask = project.tasks.findByName("assemble${variantName}")

                    if (assembleTask) {
                        if (fromVersion) {//配置patch起始版本号
                            assembleTask.dependsOn hatoPatchTask
                        }
                    }


                }
            }

            //generate all patch.jar task
            /*  project.task(HUATO_PATCHES) << {
                  patchList.each { patchDir ->
                      HatoAndroidUtils.dex(project, patchDir, appName, fromVersion)
                  }
              }
              beforeDexTasks.each {
                  project.tasks[HUATO_PATCHES].dependsOn it
              }*/
        }
    }

    public static dumpln(String msg) {
        if (debug) {
            println("--->hatoplugin:" + msg);
        }
    }
}