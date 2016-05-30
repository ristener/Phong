package com.meizu.hato.util

import org.apache.commons.io.FileUtils
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Project

/**
 * Created by jixin.jia on 15/11/10.
 */
class HatoFileUtils {

    public static void copy2Extras(Project project, String version, String dirName, File file){
        def patchDir = new File(project.buildDir.getParentFile(), "/extras/${version}/${dirName}")
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

    public static File getVersionDir(Project project, String version) {

        def file = new File(project.buildDir.getParentFile(), "extras/${version}");

            if (!file.exists()) {
                //throw new InvalidUserDataException("extras/${version} dir does not exist")
                return null;
            }
            if (!file.isDirectory()) {
                throw new InvalidUserDataException("extras/${version} dir is not directory")
            }
        return file
    }

    public static File getFileFromExt(String path) {
        def file
            file = new File(path)
            if (!file.exists()) {
                throw new InvalidUserDataException("${path} does not exist")
            }
            if (!file.isDirectory()) {
                throw new InvalidUserDataException("${path} is not directory")
            }
        return file
    }

    public static File getVariantFile(File dir, def variant, String fileName) {
        return new File("${dir}/${variant.dirName}/${fileName}")
    }

}
