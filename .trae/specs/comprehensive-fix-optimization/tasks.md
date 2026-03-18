# 任务列表

## 阶段一：紧急修复（AI对话功能）

### 任务 1.1: 修复LongCat API调用
- [x] 检查并修复API URL格式
  - [x] 验证正确的endpoint路径
  - [x] 更新AIDefaultConfigInitializer.kt中的DEFAULT_API_URL
- [x] 添加网络错误处理
  - [x] DNS解析失败提示
  - [x] 网络超时处理
  - [x] 添加重试机制

### 任务 1.2: 修复首次启动转圈问题
- [x] 优化MainActivity.kt数据加载逻辑
  - [x] 为settings添加默认值
  - [x] 添加加载超时处理
- [x] 优化DataStore初始化
  - [x] 检查OnboardingDataStore实现
  - [x] 添加异步加载优化

## 阶段二：AI对话功能增强

### 任务 2.1: 添加对话并发控制
- [x] 修改AIChatViewModel.kt
  - [x] 添加isSending状态锁
  - [x] 发送时禁用输入框和发送按钮
  - [x] 完成后解锁
- [x] 修改AIChatWidget.kt
  - [x] 禁用状态UI反馈

### 任务 2.2: 实现流式对话效果
- [x] 修改AIApiClient.kt添加流式支持
  - [x] 添加chatStream方法
  - [x] 解析SSE数据格式
  - [x] 处理流式响应
- [x] 修改AIChatService.kt
  - [x] 添加sendMessageStream方法
  - [x] 返回Flow<String>类型
- [x] 修改AIChatViewModel.kt
  - [x] 处理流式数据更新
  - [x] 实现逐字显示动画

### 任务 2.3: AI回复可读性优化（重要）
- [x] 优化MarkdownText.kt组件
  - [x] 实现标题样式（H1/H2/H3不同字号和粗细）
  - [x] 实现列表样式（有序/无序，层级缩进）
  - [x] 实现代码块样式（深色背景+等宽字体+语法高亮）
  - [x] 实现引用块样式（左侧竖线+斜体+浅色背景）
  - [x] 实现表格样式（表头加粗+斑马纹+边框）
  - [x] 实现链接样式（主题色+下划线+点击跳转）
- [x] 优化文本排版
  - [x] 设置段落间距（1.5倍行高+8dp段落间距）
  - [x] 设置字体大小（正文14sp，标题16sp，小标题15sp）
  - [x] 优化文字颜色对比度
- [x] 添加内容结构优化
  - [x] 自动识别营养数据并格式化
  - [x] 数字和单位之间添加空格
  - [x] 重要数据高亮显示
  - [x] 建议类内容添加图标前缀
- [x] 添加交互功能
  - [x] 长按文本复制
  - [x] 链接点击跳转
  - [x] 代码块一键复制
  - [x] 表格横向滚动

## 阶段三：AI助手UI重构

### 任务 3.1: 重构悬浮球形态
- [x] 优化FloatingButton组件
  - [x] 调整尺寸和动效参数
  - [x] 优化脉冲动画
  - [x] 添加按压缩放反馈

### 任务 3.2: 重构迷你窗口形态
- [x] 优化AIChatMiniWindow组件
  - [x] 调整布局尺寸
  - [x] 优化毛玻璃效果
  - [x] 重构消息列表样式
  - [x] 优化输入框样式

### 任务 3.3: 重构全屏形态
- [x] 优化AIChatScreen.kt
  - [x] 统一视觉风格
  - [x] 添加历史会话侧边栏
  - [x] 优化消息气泡样式

## 阶段四：数据展示修复

### 任务 4.1: 修复热力图数据展示
- [x] 检查OverviewScreen.kt热力图数据源
  - [x] 验证dailyMealRecords数据获取
  - [x] 修复日期计算逻辑
- [x] 优化HeatmapCalendar.kt
  - [x] 修复数据映射
  - [x] 添加悬停详情显示

### 任务 4.2: 修复月度总结默认显示
- [x] 修改OverviewScreen.kt
  - [x] 获取当前月份数据
  - [x] 计算汇总统计
  - [x] 无数据时显示占位符

## 阶段五：设置界面优化

### 任务 5.1: 删除模拟配置
- [x] 修改AISettingsScreen.kt
  - [x] 过滤isPreset=true的配置
  - [x] 仅显示用户自定义配置
- [x] 修改AIDefaultConfigInitializer.kt
  - [x] 调整默认配置逻辑

### 任务 5.2: 添加模型调用记录功能
- [x] 创建数据模型
  - [x] 创建APICallRecord.kt
  - [x] 创建APICallRecordDao.kt
  - [x] 创建APICallRecordRepository.kt
- [x] 更新数据库
  - [x] 修改AppDatabase.kt添加新表
  - [x] 添加数据库迁移

## 阶段六：启动性能优化

### 任务 6.1: 优化应用启动时间
- [x] 分析启动瓶颈
  - [x] 检查主线程阻塞
  - [x] 检查数据库初始化
- [x] 实施优化
  - [x] 延迟非关键初始化
  - [x] 异步加载资源
  - [x] 优化主题加载

## 阶段七：备份功能审查

### 任务 7.1: 审查备份与恢复功能
- [x] 检查BackupService.kt
  - [x] 验证数据完整性
  - [x] 检查版本兼容性
  - [x] 测试恢复流程
- [x] 修复发现的问题
  - [x] 添加缺失字段（饮水记录备份）
  - [x] 优化错误处理

## 阶段八：文件统计与分析

### 任务 8.1: 统计项目文件
- [x] 统计所有Kotlin文件数量（约140个文件）
- [x] 按模块分类统计（data/service/ui/utils/di）

### 任务 8.2: 分析每个文件详情
- [x] 分析data层文件
- [x] 分析service层文件
- [x] 分析ui层文件
- [x] 分析utils层文件
- [x] 分析di层文件

### 任务 8.3: 生成详细分析报告
- [x] 创建 file-analysis-report.md 文档
- [x] 记录每个文件的类、函数、变量
- [x] 生成模块依赖关系图
- [x] 提出优化建议

## 已完成总结

所有主要任务均已完成。项目包含约140个Kotlin文件，分为以下模块：
- data层：数据模型、Dao、Repository（28个文件）
- service层：AI服务、备份服务、通知服务等（22个文件）
- ui层：Screen、Component、Theme、Animation（75个文件）
- utils层：工具类（4个文件）
- di层：依赖注入模块（3个文件）
- 其他：Application、MainActivity等（8个文件）

详细分析报告见：[file-analysis-report.md](file-analysis-report.md)

## 阶段九：安全审查与修复

### 任务 9.1: 安全漏洞修复
- [x] 识别API密钥硬编码问题
- [x] 使用EncryptedSharedPreferences存储密钥
- [x] 移除源代码中的硬编码密钥

### 任务 9.2: 生成安全审查报告
- [x] 创建 security-audit-report.md 文档
- [x] 记录所有安全问题
- [x] 提供修复建议和优先级

### 任务 9.3: 日志安全修复
- [x] 创建SecureLogger工具类
- [x] 在AIApiClient中使用SecureLogger
- [x] 生产环境隐藏敏感信息

### 任务 9.4: 数据加密修复
- [x] UserSettingsRepository使用EncryptedSharedPreferences
- [x] 移除明文SharedPreferences使用

### 任务 9.3: 备份安全修复
- [x] 修改AIConfigBackup不备份敏感信息
- [x] 添加hasApiKey标记替代实际密钥

### 任务 9.4: 日志安全修复
- [x] 创建SecureLogger工具类
- [x] 在AIApiClient中使用SecureLogger
- [x] 生产环境过滤敏感信息

### 任务 9.5: 数据加密
- [x] UserSettingsRepository使用EncryptedSharedPreferences
- [x] 替换普通SharedPreferences为加密版本
