package com.meizu.hato.util

/**
 * Created by jixin.jia on 15/11/10.
 */
class HatoSetUtils {
   /* public static boolean isExcluded(String path, Set<String> excludeClass) {
        def isExcluded = false;
        excludeClass.each { exclude ->
            if (path.contains(exclude)) {
                isExcluded = true
            }
        }
        return isExcluded
    }
*/
    public static boolean isIncluded(String path, Set<String> includePackage) {
        if (includePackage.size() == 0) {
            return true
        }
        def isIncluded = false;
        includePackage.each { include ->
            include = include.replace('.', '/')
            if (path.contains(include)) {
                isIncluded = true
            }
        }
        return isIncluded
    }
}
