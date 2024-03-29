# 栗鼠 Android

>深挖洞，广积粮，做一只快乐的栗鼠。

栗鼠是一个免费的漫画库管理系统。你可以浏览各种来源里的漫画，将感兴趣的添加到库中。栗鼠会自动把漫画内容同步到你的磁盘上。

这个仓库包含栗鼠 Android 客户端的代码。

- Server：https://github.com/FishHawk/lisu
- Android：https://github.com/FishHawk/lisu-android（你在这）



![Screenshot_20201110-105619_c](.github/readme_image/Screenshot_20201110-105619.png)

## 特点

- 利用[ComicImageView](https://github.com/FishHawk/ComicImageView)减轻了漫画网点缩放后产生的摩尔纹。
- 支持在多个服务器之间切换。
- 支持深色主题。

## 高级用法

### mDNS自动发现

对于linux系统，创建`/etc/avahi/services/lisu.service`文件。内容如下：

```xml
<?xml version="1.0" standalone='no'?><!--*-nxml-*-->
<!DOCTYPE service-group SYSTEM "avahi-service.dtd">
<service-group>
  <name replace-wildcards="yes">lisu %h</name>
  <service>
    <type>_lisu._tcp</type>
    <port>8080</port>
  </service>
</service-group>
```

创建后，服务端会启动mDNS服务，从而让客户端能够自动发现lisu服务。服务名称和端口号可以自行修改。
