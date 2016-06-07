package com.meizu.hato.util

import com.meizu.hato.HatoDir
import org.apache.commons.io.FileUtils
import org.gradle.api.Project

/**
 * Created by jixin.jia on 15/11/10.
 */
class HatoFileUtils {

    public static String HATO_DIR_PATH = HatoDir.RELEASE_PATH

    public static void copy2SpecialDir(Project project, String targetPath, File file){
        def patchDir = new File("${HATO_DIR_PATH}/${targetPath}")
        if (!patchDir.exists()){
            patchDir.mkdir()
        }
        if (file.exists()) {
            def newFile = new File(patchDir.getAbsolutePath() + "/" + file.getName())
            if (!newFile.exists()){
                FileUtils.copyFile(file, newFile)
            }
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

    public static File getVersionDir(String appName, String version) {
        def file = new File(HATO_DIR_PATH + "/${appName}/${version}");
        if (!file.exists()) {
            return null;
        }
        if (!file.isDirectory()) {
            return null;
        }
        return file
    }


    public static File getVariantFile(File dir, def variant, String fileName) {
        return new File("${dir}/${variant.dirName}/${fileName}")
    }

}
