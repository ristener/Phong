# **Hato**
## Hato is a gradle plugin for auto building a patch named versionCode.jar which can be used in Android app for hotfixing some bugs. It target to java class. 

## Requirements
hato can be apply in gradle version gradle:1.3.+

## Using Hato in your application
```
buildscript {
    repositories {
        jcenter()
    }

    dependencies {
        classpath 'com.meizu.media:hato:1.1.+'
    }
}

apply plugin: 'com.meizu.hato'
```
## configure
```
hato {
    debug = true; //debug switch
    fromVersion = '5310' //version you wanna make a patch
    includePackage = [''] //package name used to make a patch default is just the app's
}
```
