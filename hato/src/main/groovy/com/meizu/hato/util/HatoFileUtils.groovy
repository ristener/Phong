package com.meizu.hato.util


import org.apache.commons.io.FileUtils
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Project

/**
 * Created by jixin.jia on 15/11/10.
 */
class HatoFileUtils {

    public static final String SERVER_HATO_DIR = /*System.getProperty("user.home") + */"/media/mzf/DailyBuild4Test/hato"

    public static void copy2TargetDir(Project project, String targetApp, String targetVersion, String dirName, File file){
        def patchDir = new File(SERVER_HATO_DIR +  "/${targetApp}/${targetVersion}/${dirName}")
        if (!patchDir.exists()){
            patchDir.mkdir()
        }
        if (file.exists()) {
            def newFile = new File(patchDir.getAbsolutePath() + "/" + file.getName())
            FileUtils.copyFile(file, newFile)
        }
    }


    public static File touchFile(File dir, String path) {
        def file = new File("${dir}/${path}")
        file.getParentFile().mkdirs()
        return file
    }

    public static copyBytesToFile(byte[] bytes, File file) {
        if (!file.exists()) {
            file.createNewFile()
        }
        FileUtils.writeByteArrayToFile(file, bytes)
    }

    public static File getFileFromProperty(Project project, String property) {
        def file
        if (project.hasProperty(property)) {
            file = new File(project.getProperties()[property])
            if (!file.exists()) {
                throw new InvalidUserDataException("${project.getProperties()[property]} does not exist")
            }
            if (!file.isDirectory()) {
                throw new InvalidUserDataException("${project.getProperties()[property]} is not directory")
            }
        }
        return file
    }

    public static File getVersionDir(String appName, String version) {
        def dir = SERVER_HATO_DIR + "/${appName}/${version}"
        def file = new File(dir);

        if (!file.exists()) {
            //throw new InvalidUserDataException("extras/${version} dir does not exist")
            return null;
        }
        if (!file.isDirectory()) {
            throw new InvalidUserDataException("${dir} dir is not directory")
        }
        return file
    }


    public static File getVariantFile(File dir, def variant, String fileName) {
        return new File("${dir}/${variant.dirName}/${fileName}")
    }

}
