package com.meizu.hato


class AppInfo {
    def pkgName
    def versionCode
    def versionName
    def applicationClass

    AppInfo(pkgName, versionCode, versionName, applicationClass) {
        this.pkgName = pkgName
        this.versionCode = versionCode
        this.versionName = versionName
        this.applicationClass = applicationClass
    }

    @Override
    String toString() {
        return "AppInfo[${pkgName}/${versionCode}/${versionName}/${applicationClass}]"
    }
}


