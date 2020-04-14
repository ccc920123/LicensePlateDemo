# LicensePlateDemo
# 号牌号码识别，支持新能源车牌，离线识别
号牌识别，支持新能源车，离线版
## 使用方式：

项目build.gradle添加

    allprojects {
        repositories {
            .....
            maven { url 'https://jitpack.io' }   
        }
    }
app build.gradle 添加

    implementation 'com.github.ccc920123:LicensePlateDemo:v1.0'

**或者**

 **1**：下载aar 包

[licenseplatelib-v1.0.aar](https://github.com/ccc920123/LicensePlateDemo/blob/master/downloads/licenseplatelib-v1.0.aar?raw=true)

**2**：由于library包含so 需要在 app 的build.gradle中添加<br/>
ndk的支持

     defaultConfig {
       .....
        ndk {
            //设置支持的SO库架构
            abiFilters 'armeabi'
        }
    }
**3**：调用方法：
<br/>
```java
LPalte.openScanPlate(this, 666);//第一个参数就是activity的上下文，第二个参数是requestCode 我填写的666
```
**4**：重写onActivityResult 方法
```java
//识别后的回调
    @Override
    protected void onActivityResult(int requestCode, int resultCode,  Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (resultCode == RESULT_OK && requestCode == 666 && data != null) {
            String hphm = data.getCharSequenceExtra("number").toString();
            String hpzl = data.getCharSequenceExtra("hpzl").toString();
            String color = data.getCharSequenceExtra("color").toString();

            result.setText(hphm+" 号牌种类："+hpzl+" 号牌颜色："+color);
        }
    }
```
好了号牌识别就这么简单。
### 注意：
如果你需要修改识别界面，请自行下载demo，然后导入licenseplatelib 这个library，修改里面的UI


------------
###### 号牌种类表：
| code  |  值 |
| ------------ | ------------ |
| 01  | 大型汽车（黄牌车）  |
| 02  | 小型汽车（蓝牌）  |
| 52  |  新能源汽车（绿牌 小型新能源） |
| 51  |  新能源汽车（黄绿 大型新能源） |
| 03  |  使馆车牌 |
| 04  |  领事馆车牌 |
| 15  |  挂车 |
| 26  |  香港车牌 |
| 16  |  教练车牌 |
| 27  | 澳门车牌  |
|  32 | 军车车牌  |
|  23 | 警用车牌  |
|  31 |  武警号牌 |
|  25 |  原农机号牌 |
| 99  |  其他号牌 |

###### 号牌颜色表：
| code  |  值 |
| ------------ | ------------ |
| 0  | 黄牌  |
| 1  | 蓝牌 |
| 2 |  原农机号牌 |
| 3  |  使馆汽车号/香港澳门入出境 |
| 4  |  军队，警，武警号牌 |
| 5  |  新能源号牌 |
| -1  |  其他号牌 |







