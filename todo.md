1. 执行流程
    1. 创建Node并初始化
    2. 构造Node树
    3. 接入Node树到宿主系统
    4. 代理测量和布局
       需要注意几个方面：

    + 支持自定义节点处理：
      那么是否包括控制节点（If,For等）——否定
      创建节点并统一处理：
      如createNode("row",object:RowParams{val modifier:Modifier get()=modifier //...})
      RowParams可以用ksp生成
      自定义一个处理器
      然后据其返回Node，感觉这样会比expect actual灵活？
    + host和reactiveSystem相关：
      androidHost(reactive:()->ReactiveSystem,config,content:@Component()->Unit)
    