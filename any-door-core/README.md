# AnyDoor 任意门
## 发布版本
### 0.0.2
- 支持Bean私有方法
- 支持空字符串要解析成对象，不再是null
- 支持没有注册Spring对象进行执行
- 打印响应结果
- 调用的方法是否会走代理？会走
- 修复null参数
- 修复String类型
- 修复List类型的泛型映射

### 0.0.3
- 支持同步或异步执行，默认异步

### 0.0.4
- 修复代理Bean的私有方法调用
- 字符串类型传入null存在null字符串

### 0.0.5
- 修改支持jdk8

### 0.0.7
- 支持通过Attach进行调度运行项目

### 0.0.8
- 当同步执行时时不使用CompletableFuture以简化调用栈

### 0.0.9
- 支持lambda表达式入参

### 0.0.10
- 修复Attach传递参数过长情况（通过文件传递）
- 调整依赖，autoconfigure需要提供
- 修复直接通过接口调用方法
- 修复执行重载的私有方法
- 修复时间支持：LocalDateTime传yyyy-MM-dd'T'HH:mm:ss
- 修复Json序列化支持泛型

### 1.0.0 && 1.0.1 重大更新
- 加入Arthas依赖，支持获取到运行时的对象信息
- 调整打包依赖
- 移除mvc依赖、移除spring boot依赖