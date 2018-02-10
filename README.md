# `AutoWrite`
自动编写或修改代码文件

自动将`src/main/resources/类名`中`in`目录下的文件处理后输出到`out`目录

每个类功能如下。

## `POJODoc`
把行末注释变为文档注释

## `GetSetDoc`
为`get/set`方法添加字段上的文档注释

## `ReplaceMethod`
把重写方法的`return null;`替换为带方法本身的代码 例如：
```
   public String abc(String key) {
       return abc(key);
   }
```
