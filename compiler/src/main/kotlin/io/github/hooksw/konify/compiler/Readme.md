## 设计思路

0. 使用限制

   1. @Component函数只能被@Component函数调用
   2. 不同于Compose,konify注解不支持property
1. 函数参数

   1. 对于函数类型的参数如：`lambda`、~~fun interface~~,
      如果没有特殊注解（暂定为@Stateful)，直接跳过，
      否则类型和默认参数从`Type`都改为`()->Type`类型
   2. 对于`vararg`参数，不允许默认值的存在，因为默认值可能已经提前初始化好了，
      而konify无法对其进行干预。
      同时，将`vararg`的类型从`vararg p:Type`改为`vararg p:()->Type`。
   3. 对于普通参数，如果是(`int` `long` `float` `double`),
      类型转为konify内置的primitive supplier，以避免额外的拆箱和装箱。
      如果有`@Stateless`注解，则跳过
      否则，则转为`()->Type`的形式
   4. 考虑到`value class`的存在，可能需要在未来支持动态生成`()->ValueType`的`fun interface`,或者将其与上一条合并，全部动态生成
   5. 如果是带`@Component`注解的函数类型参数，则更改签名，将形参按上述原则转换为lazy的形式
   6. 对于可空类型，如`Int?`，转为`()->Int?`,而不是`IntSupplier?`
2. 方法体

   1. 对于`@Component`注解的函数，统一在函数前后默认生成`createNode()`和`prepare()`,
      对于`@ReadOnly`注解则不生成
   2. 对于函数调用，则将实参按前述原则，对必要参数进行lambda包装和fun interface调用
   3. 全局方法体用`try...finally`包裹以确保`createNode()`和`prepare()`的执行
3. 特殊注解

   1. `@UnTrack`.由于effect需要手动指定响应的参数，需要取消自动追踪，
      那么，需要对带该注解的函数类型实参进行处理，对所有signal和形参调用自动进行`untrack()`包装
      （可能需要考虑local function的存在）
   2. `@StyleBuilder`.用于类型css的函数构造，只允许赋值操作和函数调用
      对于如下style设定

      ```kotlin
      Style {
        background=s1
        border[bottom]{
            width=s2.dp
        }
      }
      ```
      转换为：

      ```kotlin
      Style {
        bind{
            background=s1
        }
        bind{
            border[bottom]{
                width=s2.dp
            }
        }
      }
      ```
      那么编译器插件需要确保原始Style里面没有bind函数调用，以避免干扰
