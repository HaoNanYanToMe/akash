### Akash Update Project

---

> **2019/11/19 更新日志**
> + **StringKit**中新增了**getUUID**()方法,现在可以更方便的获取UUID了。
 ```
           String uuid = StringKit.getUUID();
 ```
> + 项目中移除了~~静态html页面图片转换功能~~
> + 修复了**sqlEngine**在生成更新和新增语句时存在的bug

---
> **2019/11/20 更新日志**
> + 新增了**ConverterData**类用以初始化逻辑引擎的构造（creator）
> + 优化了**sqlConverter**中的部分细节代码，以解决在多线程情况下数据写入冲突的问题
> + 为保证数据统一性,**BaseApi**中涉及增删改的方法现在已经支持事务操作了
> + **sqlConverter**增加了数据历史日志以提供数据备份管理
> + **excelUpLoadParse**优化了对余量数据的处理，同时提升了其对**海量excel**的解析效能

---
> **2019/11/21 更新日志**
> + 新增了逻辑引擎数据校验器**ConverterValidator**，极大程度上保证了对应数据的安全性
> + **StringKit**新增了**parseLinkedMap**方法，现在可以更便捷的将json数据转化为有序的List集合了
 ```
          LinkedHashMap<String, Object> params = StringKit.parseLinkedMap(data);
 ```
 
 ---
 > **2019/11/29 更新日志**
 > + 为保证数据准确,sqlEngine取消了对生成的sql进行格式化（小写）转换功能
 > + 优化了**ConverterValidator**的验证逻辑
 > + stringKit验证忽略了@_#,四项值
 > + 优化了**sqlConverter**的逻辑，现在已经支持查看生成的sql及其所需的必要入参值了
 > + 新增了**cr_engineparam**以来存储引擎所需的核心字段，并在**sqlConverter**增加了对应的逻辑方法**paramBinding**
