# LicensePlateDemo 号牌号码识别，支持新能源车牌，离线识别
号牌识别，支持新能源车，离线版
# 使用方式：
<br/>
 1：下载aar 包
[licenseplatelib-v1.0.aar](https://github.com/ccc920123/LicensePlateDemo/blob/master/downloads/licenseplatelib-v1.0.aar?raw=true)
<br/>
2：由于library包含so 需要在 app 的build.gradle中添加<br/>
ndk的支持

     defaultConfig {
       .....
        ndk {
            //设置支持的SO库架构
            abiFilters 'armeabi'
        }
    }
