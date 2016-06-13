package com.meizu.hato.util

import com.meizu.hato.AppInfo
import org.apache.commons.io.FileUtils
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.DefaultTask
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Project

/**
 * Created by jixin.jia on 15/11/10.
 */
class HatoAndroidUtils {
    private static final def PATCH_NAME = ".jar"
    private static final def PATH_ANDROID_MANIFEST = "src/main/AndroidManifest.xml"

    private static AppInfo mAppInfo;

    public static AppInfo getAppInfo(Project project) {
        if (mAppInfo != null) return mAppInfo
        def androidManifestXml = new File(project.getProjectDir(), PATH_ANDROID_MANIFEST)
        if (androidManifestXml) {
            def androidManifest = new XmlParser().parse(androidManifestXml)
            def androidTag = new groovy.xml.Namespace("http://schemas.android.com/apk/res/android", 'android')
            String applicationName = androidManifest.application[0].attribute(androidTag.name)
            def versionCode = androidManifest.attribute(androidTag.versionCode)
            def versionName = androidManifest.attribute(androidTag.versionName)
            def pkgName = androidManifest.attribute("package")
            mAppInfo = new AppInfo(pkgName, versionCode, versionName, applicationName.replace('.', ''))
        } else {
            return null
        }
    }

   public static void main(String []args){
       def amfxml = new File(System.getProperty("user.home") + "/work/repo/flyme3/meizu/Media/Music/app/src/main/AndroidManifest.xml");
       if (amfxml){
           def amf = new XmlParser().parse(amfxml)

           def androidTag = new groovy.xml.Namespace("http://schemas.android.com/apk/res/android", 'android')
           def versionCode = amf.attribute(androidTag.versionCode)
           def versionName = amf.attribute(androidTag.versionName)

           println(amf.attribute("package"))
           println(versionCode)
           println(versionName)

           String str = 'userhome'
           println(str.replace('.', ''))
       }

   }


    public static dex(Project project, File classDir, String targetApp, String fromVersion) {
        if (classDir.listFiles().size()) {
            def sdkDir
            Properties properties = new Properties()
            File localProps = project.rootProject.file("local.properties")
            if (localProps.exists()) {
                properties.load(localProps.newDataInputStream())
                sdkDir = properties.getProperty("sdk.dir")
            } else {
                sdkDir = System.getenv("ANDROID_HOME")
            }
            if (sdkDir) {
                def cmdExt = Os.isFamily(Os.FAMILY_WINDOWS) ? '.bat' : ''
                def stdout = new ByteArrayOutputStream()
                def patchFile = new File(classDir.getParentFile(), fromVersion + "." + PATCH_NAME)
                project.exec {
                    commandLine "${sdkDir}/build-tools/${project.android.buildToolsVersion}/dx${cmdExt}",
                            '--dex',
                            "--output=${patchFile.absolutePath}",
                            "${classDir.absolutePath}"
                    standardOutput = stdout

                }
                if (patchFile){
                    def newPatchDir = new File(HatoFileUtils.HATO_DIR_PATH + "/${targetApp}/${fromVersion}")
                    if (!newPatchDir.exists()){
                        newPatchDir.mkdir();
                    }
                    FileUtils.copyFile(patchFile, new File(newPatchDir, patchFile.getName()));
                }
                def error = stdout.toString().trim()
                if (error) {
                    println "dex error:" + error
                }
            } else {
                throw new InvalidUserDataException('$ANDROID_HOME is not defined')
            }
        }
    }

    public static applymapping(DefaultTask proguardTask, File mappingFile) {
        if (proguardTask) {
            if (mappingFile.exists()) {
                proguardTask.applymapping(mappingFile)
            } else {
                println "$mappingFile does not exist"
            }
        }
    }
}