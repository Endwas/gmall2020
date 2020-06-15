# gmall2020
****本SSM项目使用Springboot来生成多个web、service服务，通过dubbo和zookeeper来做服务管理和监控，后台模块前端分离运行在nodejs上
后续使用了elasticsearch,activemq,redisson,weibo单点登录、支付宝付费接口等技术
同时引用了很多工具包，StringUtils,fastjson等
FastDfs没有引入，因为不仅依赖的东西多配置的麻烦，只有图片服务器使用到了。所以暂时只添加了依赖包，工具类和使用代码还没写****



###### 各服务端口号如下：

gmall-user-web 服务的端口为8080
gmall-user-service 服务的端口为8070

gmall-manage-web 服务的端口为8081
gmall-manage-service 服务的端口为8071

gmall-item-web 服务的端口为8082

gmall-search-web 服务的端口为8083
gmall-search-service 服务的端口为8073

gmall-search-web 服务的端口为8084
gmall-search-service 服务的端口为8074

gmall-passport-web 服务的端口为8085
gmall-user-service 服务的端口为8075

gmall-order-web 服务的端口为8086
gmall-order-service 服务的端口为8076

gmall-redisson-test 服务的端口为8081|8082|8083
使用nginx进行服务的分发，所以开启三个服务做测试


------------------------------------------------------------------------------------

**Coding 遇到的问题**
1.新版的springboot已经废弃了1.5的版本，所以在创建的时候，无法选择该版本了，而使用2.0以上的配合该zookeeper会报错

`解决办法：因为我们使用parent来做版本控制，在gmall-parent pom中声明为1.5的版本 依然运行的是1.5的版本spring boot`

2.zookeeper无法启动，提示Error contacting service. It is probably not running.

`解决办法：首先查看zookeeper/conf中是否配置了zoo.cfg文件，若没配置直接复制一份zoosample.cfg命名为zoo.cfg即可，然后修改
里面的dataDir，可创建一个data文件夹在zookeeper中
若上述配置配了，那么就要确定自己的java环境配置了吗，如果配了那么一定要执行 source /etc/profile才可以成功运行，否则一直运行失败`

3.dubbo-admin监控的Tomcat无法启动

`解决办法：查看conf中的catalina.out文件，一般是配置映射链接符号有问题，或者是Q2的java环境没加载。`

4.IDEA无法连接上自己的redis服务器

`解决办法：将自己redis的conf中port修改为0.0.0.0重新运行即可，或其内网ip地址，localhost是无法被连接的`

5.nginx启动失败或无法分辨自己是否启动成功

`解决办法：使用的是windows版的nginx，在start nginx启动的时候就只要闪框，也没有告知启动状态，代理又失败了，
那么可以在nginx文件夹中使用nginx.exe -s stop尝试关闭，如果nginx配置有问题，会命令行提示字符或参数错误
如果是端口被占用，也会提示80端口被占用，那么此时去任务管理器kill就好
但有种特别情况，sql server reporting占用了，显示服务名称叫system，无法kill。那么自行关闭：
开始菜单--microsoft sql server 2016--Sql Server 配置管理器 -- Sql Server 服务 -- Sql Server reporting services(右键停止)
`
6.数据库连接失败或提示时区错误

`解决办法：安装自己数据库版本选择对应的连接驱动，如我使用的是8.0的mysql需要将版本号更改为8.0.11否则会报错
如果版本没问题，可能是时区的问题，需要在mysql配置后面加上&serverTimezone=Asia/Shanghai`

7.拦截器无法拦截部分方法
`解决办法：要将springboot启动类放在gmall下，和web-util中的config，annotation同级这样才能被扫描到`

