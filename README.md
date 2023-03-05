# gateway-Authority-control
使用springcloud gateway集成jwt进行登录验证，并使用RBAC模型进行用户的权限控制的一个demo
数据库分为

本项目基于SpringCloud下进行开发，使用nacos 1.14作为服务注册中心，使用redis6.0作为缓存存储

cloud-api-commons中存放了一些实体类和公共返回类
cloud-user-manage微服务中进行用户的注册(controller中仅写了普通用户注册，管理员同理)
此处为了简便，数据库设计将用户角色字段也存入了user_account表中
cloud-auth-server微服务为用户登录鉴权微服务
cloud-jwt-manage中存放生成jwt token的相关工具
cloud-roles-manage中是用户权限相关微服务，用来存储角色对于某个api的权限信息
cloud-address-manage是一个应用微服务实例，用来验证登录后进行查询是否有效(包含了部分redission布隆过滤器相关内容)
cloud-gateway是统一鉴权网关，对项目内所有api对外开放进行统一管理

相关博客：
springcloud gateway集成JWT——实现登录鉴权 
https://blog.csdn.net/tang_seven/article/details/127498184?spm=1001.2014.3001.5501
springcloud gateway集成RBAC模型——实现用户权限判断
https://blog.csdn.net/tang_seven/article/details/127536027?spm=1001.2014.3001.5501

