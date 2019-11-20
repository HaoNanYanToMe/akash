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
