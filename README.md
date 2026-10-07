# Youou-ASO

Youou-ASO 是面向 ASO 推广业务的下单与运营管理系统。

## 第一版范围

- 用户注册、登录、找回密码、首次登录强制改密
- 用户应用管理与应用真实性校验
- 线下充值引导、管理员入账、余额流水
- 推广服务下单：关键词安装、下载量、星级评分、用户评价
- 审核服务：关键词保排名、关键词覆盖
- 管理员确认订单、拒绝退款、执行、批量执行、自动完成
- 中英文多语言
- 订单导出、余额流水导出、操作日志

## 工程结构

- `backend/`：Spring Boot 模块化单体后端
- `frontend/`：Vue 3 + TypeScript 单前端项目

## 生产原则

- 不在源码中保存密码、Token、API Key 或生产连接信息
- 金额统一使用数据库 `DECIMAL(18,2)` 和 Java `BigDecimal`
- 所有余额变动必须生成流水
- 所有订单状态变更必须写状态历史
- 所有关键后台操作必须写审计日志

## 测试与安全约束

- 后端单元测试：在 `backend` 执行 `mvn test`。测试默认启用 `test` profile，禁止回落到本地业务库；未配置测试 MySQL 时，数据库集成测试会明确跳过。
- 完整数据库回归：设置 `YOUOU_TEST_MYSQL_URL`（例如 `jdbc:mysql://127.0.0.1:3306/?serverTimezone=Asia/Shanghai`，不能包含库名）、`YOUOU_TEST_MYSQL_USER`、`YOUOU_TEST_MYSQL_PASSWORD` 后执行 `mvn test`。使用专用测试服务/账号，需有创建和删除测试库的权限。测试为每次运行生成 `youou_test_<随机 UUID>` 库，运行所有 Flyway 迁移，结束时仅删除该随机库。强制终止进程可能留下测试库，清理时必须核对名称。
- 前端回归：在 `frontend` 执行 `node order-detail-export.test.mjs`、`npm run validate:locales`、`npm run build` 和 `npm audit`。
- JWT 会话绑定密码凭据，并逐请求核对账号状态及当前管理员角色。部署此更新后，旧会话需要重新登录；修改密码后主动返回登录页。
- 远程图标只下载 Apple/Google 图片 CDN（`mzstatic.com`、`googleusercontent.com`、`ggpht.com`）的 HTTPS 地址，禁用重定向并在接收过程中限制大小。其他自定义图标通过文件上传。
- 地区覆盖单价必须大于零，最多四位小数；订单按分结算，结算总额必须大于零。已完成订单的数量退款按累计应退金额计算，避免重复调整产生舍入差额。
- V58 增加跨账号注册锁；V59 对特殊审核单来源增加唯一索引。升级前如存在历史重复 `source_audit_id`，应核实重复订单和扣款后处理，不能直接删除业务数据以通过迁移。
- ExcelJS 保持 4.x；只覆盖其 `uuid` 到兼容 CommonJS 的 11.x，避免自动安全修复将 ExcelJS 降级。导出测试验证工作簿写入和回读。
