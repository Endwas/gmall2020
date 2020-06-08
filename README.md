# gmall2020
**本SSM项目使用Springboot来生成多个web、service服务，通过dubbo和zookeeper来做服务管理和监控，后台模块前端分离运行在nodejs上
后续使用了elasticsearch,activemq,redisson,weibo单点登录、支付宝付费接口等技术
同时引用了很多工具包，StringUtils,fastjson等**

各服务端口号如下：

gmall-user-web 服务的端口为8080
gmall-user-service 服务的端口为8070

gmall-manage-web 服务的端口为8081
gmall-manage-service 服务的端口为8071

gmall-item-web 服务的端口为8082
gmall-item-service 服务的端口为8072

gmall-redisson-test 服务的端口为8081|8082|8083
使用nginx进行服务的分发，所以开启三个服务做测试


------------------------------------------------------------------------------------
Coding 遇到的问题
1.新版的springboot已经废弃了1.5的版本，所以在创建的时候，无法选择该版本了，而使用2.0以上的配合该zookeeper会报错

解决办法：因为我们使用parent来做版本控制，在gmall-parent pom中声明为1.5的版本 依然运行的是1.5的版本spring boot

2.zookeeper无法启动，提示Error contacting service. It is probably not running.

解决办法：首先查看zookeeper/conf中是否配置了zoo.cfg文件，若没配置直接复制一份zoosample.cfg命名为zoo.cfg即可，然后修改
里面的dataDir，可创建一个data文件夹在zookeeper中
若上述配置配了，那么就要确定自己的java环境配置了吗，如果配了那么一定要执行 source /etc/profile才可以成功运行，否则一直运行失败

3.IDEA无法连接上自己的redis服务器

解决办法：将自己redis的conf中port修改为0.0.0.0重新运行即可，127.0.0.1似乎是无法被连接的，暂不清楚原因

4.





