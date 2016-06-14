 # **Hato**
## Hato is a gradle plugin for auto building a patch named versionCode.jar which can be used in Android app for hotfixing some bugs. It target to java class. 

## Requirements
hato can be apply in gradle version gradle:1.3.+

## Using Hato in your project
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
## Configure
```
hato {
    fromVersion = '5310' //要做补丁包的版本
}
```
## Validate

> * 1.配置要做补丁包的版本 hato {fromVersion = '' }
> * 2.修改一些代码
> * 3.重新编译
> * 4.查看产物目录下生成的补丁包
> * 5.push补丁包到/sdcard根目录
> * 6.杀进程重启App查看结果

## Products
产物目录
> * 服务器编译
smb://dailybuild/firmware/DailyBuild4Test/hato
> * 本地编译
 用户目录/hato/
```