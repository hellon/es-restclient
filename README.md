# es-restclient
对elasticsearch restClinet的封装使用

# 使用介绍

1. entity需要继承ElasticsearchDocument,并且需要在entity上使用@Document注解，标注entity对应index名称和需要唯一索引的列
2. 需要在类路径下建立mappings文件夹,创建对应entity的mapping文件，命名规则为:@Document注解内标注的index名称+".json"。
3. 操作es需要继承AbstractESRepository，构造函数中传递一个Es的RestHighLevelClient对象即可。


---
 完成以上三步之后就可以轻松操作es了，对应的es版本为7.1X
 
 ## 高级用法
 1. 创建entity对应index的setting和mapping都可以自定义。
 2. setting自定义需要实现IndexSetting接口，然后将实现类指定到@Document注解的settingGenerator属性上即可。
 3. mapping自定义需要实现IndexMapping接口，然后将实现类指定到@Document注解的mappingGenerator属性上即可。
 4. 自定义的setting和mapping可以参考源码DefaultIndexSetting和DefaultIndexMapping的实现
 
 ## example 开始
 1. 创建一个user entity做为基本用法，创建一个role entity做为高级用法


