# LicensePlateDemo
# 号牌号码识别，支持新能源车牌，离线识别
号牌识别，支持新能源车，离线版
## 使用方式：
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
LPalte.openScanPlate(this, 666);//第一个参数就是activity的上线文，第二个参数是requestCode 我填写的666
```
**4**：重写onActivityResult 方法
```java
//识别后的回调
    @Override
    protected void onActivityResult(int requestCode, int resultCode,  Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (resultCode == RESULT_OK && requestCode == LICESECODE && data != null) {
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


###### 号牌颜色表：








