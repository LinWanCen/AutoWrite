# `AutoWrite`
自动编写或修改代码文件

[TOC]


# 使用说明

## 直接使用
1. 清空 files 文件夹
2. 把要修改的源文件丢进去双击对应 .bat 文件

## 拖放使用
2. 把要修改的源文件拖到对应的 .bat 文件

## 直接修改剪切板
1. 清空 files 文件夹
2. 直接双击对应 .bat 文件

## 作为 IntelliJ IDEA 工具
1. Ctrl + Alt + S 进入设置
2. 选择 **Tools** 的 External Tools 添加进去，参数设置`$FilePath$ $FileEncoding$`即可

![](img/IntelliJ%20IDEA%20External%20Tools.jpg)

## 作为 IntelliJ IDEA 选择内容工具
1. Ctrl + Alt + S 进入设置
2. 选择 **Tools** 的 External Tools 添加进去，参数设置：
```
$FilePath$
$FileEncoding$
$FilePath$
$SelectionStartLine$
$SelectionEndLine$
$SelectionStartColumn$
$SelectionEndColumn$
```
一般程序都是行处理，所以最后两个参数可以不设置，bat 文件也不会传入


# 基本程序介绍
运行参数：`[输入文件] [编码] [输出文件] [起始行] [结束行] [起始列] [结束列]`

## `AttributeAlign`
对齐 XML 属性  
推荐设置为 IntelliJ IDEA 选择内容工具
```xml
    <id property="id" column="id"/>
    <result property="userName" column="user_name"/>
    <result property="class" column="class"/>
```
替换为
```xml
    <id     property="id"       column="id"       />
    <result property="userName" column="user_name"/>
    <result property="class"    column="class"    />
```

## `POJODoc`
把行末注释变为文档注释
```java
    private String user; // 用户名
```
替换为
```java
    /** 用户名 */
    private String user;
```

## `GetSetDoc`
get set 方法添加字段上的文档注释
```java
    /** 用户名 */
    private String user;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
```
替换为
```java
    /** 用户名 */
    private String user;

    /** 获取用户名 */
    public String getUser() {
        return user;
    }

    /** 设置用户名 */
    public void setUser(String user) {
        this.user = user;
    }
```


## `ReplaceMethod`
替换重写方法的`return null;`为带方法本身的代码
```java
   public String abc(String key) {
       return null;
   }
```
替换为
```java
   public String abc(String key) {
       return abc(key);
   }
```


# 连续正则替换程序介绍
运行参数：`正则编码 正则文件 [输入文件] [编码] [输出文件] [起始行] [结束行] [起始列] [结束列]`

## `ReplaceAllMultiple`
在正则文件中写好要替换的正则表达式和正则替换式列表，
程序会读取整个文件并按顺序做正则替换。

正则文件前两行会被直接忽略，后面每四行为一组，分别是
1. 说明，若//开头则不执行，可以包含参数，参数可以放在任何位置，需匹配大小写
2. 正则表达式
3. 正则替换式，主要 Tab 等要用 \t 等表达，就像在 Java 代码中一样
4. 横线分割，无用

目前参数有五个，分别是：  
get:    正则获取  
loop:   替换到替换前后一致为止  
title:  在文档开始添加 正则替换式 解码后的文本，换行符自行添加  
footer: 在文档结尾添加 正则替换式 解码后的文本，换行符自行添加  
case:   忽略大小写  

案例：

find2tsv.txt
```
IntelliJ IDEA 搜索结果替换成 tsv 格式
-------------------------------------------
1.把原有 Tab 替换成四个空格
\t

-------------------------------------------
2.循环替换在行号代码前加文件名等信息【参数：loop】
(\W+)(\w*)(\.java.*\n)( +)(\d+) *(.*)
$1$2$3$4\t$2\t$5\t$2.java:$5\t$6
-------------------------------------------
3.替换掉非行号代码的部分
^ *[^\t]*

-------------------------------------------
4.替换掉开头的 Tab
^\t

-------------------------------------------
5.插入标题行【参数：title】

程序名\t行\t文件:行\t代码\n
-------------------------------------------
```