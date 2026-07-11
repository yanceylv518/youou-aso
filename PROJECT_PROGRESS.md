# 项目进度记录

## 2026-06-25 更新：前端生产包构建与部署包生成

- 状态：已完成前端生产构建，并生成可上传到宝塔站点目录的部署压缩包。
- 当前任务：对近期前端页面调整进行打包，准备更新线上部署。
- 实现原则：只执行前端生产构建和部署包生成；不修改后端 jar、数据库、路由、接口和任何秘钥配置。

### 实施内容

1. 在 `frontend` 目录执行生产构建。
2. 使用构建产物 `frontend/dist` 生成前端部署压缩包。
3. 压缩包顶层直接包含 `index.html` 和 `assets`，适合上传到宝塔前端站点根目录后解压覆盖。
4. 未执行远程服务器文件删除、服务重启或数据库操作。

### 生成文件

1. `deploy/youou-aso-frontend-20260625-113923.zip`

### 验证结果

1. `cmd /c npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. `Compress-Archive`：成功生成前端部署压缩包。
3. 压缩包内容检查：确认顶层包含 `index.html`，并包含 `assets` 目录。
4. 前端构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本次未处理。

### 下一步任务清单

1. 将 `deploy/youou-aso-frontend-20260625-113923.zip` 上传到宝塔前端站点目录。
2. 在站点根目录解压覆盖，确保 `index.html` 和 `assets` 位于站点根目录。
3. 清理浏览器缓存或强制刷新后验证线上页面。
4. 检查创建订单页、订单列表筛选区、推广服务页和应用商店图标显示。

### 风险点

1. 本次只生成前端包，没有直接连接服务器执行覆盖；线上更新仍需要在宝塔中上传并解压。
2. 浏览器或 Nginx 静态缓存可能导致短时间看到旧页面，需要强制刷新或清理缓存。

## 2026-06-25 更新：按商店拆分的订单列表去掉应用商店筛选

- 状态：已完成用户端和管理员端订单列表筛选区精简，并通过前端生产构建验证。
- 当前任务：苹果订单、谷歌订单、iPad订单已经按 store 拆分菜单，筛选区不再显示“应用商店”筛选。
- 实现原则：只移除重复 UI 筛选项；保留路由 `storeType` 内部过滤逻辑；不修改接口、路由、数据库和订单数据结构。

### 实施内容

1. 用户端订单列表移除筛选区“应用商店”下拉框。
2. 管理员端订单列表移除筛选区“应用商店”下拉框。
3. 保留 `filters.storeType`，继续由当前路由菜单决定请求参数。
4. 保留应用列表、地区列表和导出文件名中的 store 限定逻辑。
5. 清理仅用于商店筛选下拉框的 `StoreIcon` 引用、`storeOptions` 和相关 CSS。

### 修改文件

1. `frontend/src/views/user/StoreOrdersView.vue`
2. `frontend/src/views/admin/StoreOrdersView.vue`
3. `PROJECT_PROGRESS.md`

### 验证结果

1. `cmd /c npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. `rg` 检查订单列表组件中已无 `order-store-select-dropdown`、`storeOptions`、`StoreIcon` 等商店筛选 UI 残留。
3. 前端构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本次未处理。

### 下一步任务清单

1. 部署新的 `frontend/dist` 后检查用户端苹果订单、谷歌订单、iPad订单筛选区。
2. 检查管理员端苹果订单、谷歌订单、iPad订单筛选区。
3. 确认订单列表仍按当前菜单对应的 store 查询数据。
4. 检查导出文件名和列表应用商店标签是否仍显示正确。

### 风险点

1. 本次仅移除重复筛选项，内部仍依赖路由 `meta.storeType` 过滤；如果后续新增不按 store 拆分的通用订单列表，需要单独提供商店筛选入口。
2. 线上静态资源更新后，浏览器缓存可能短时间继续显示旧筛选区，需要强制刷新或清理缓存。

## 2026-06-25 更新：添加关键词按钮移入关键词表格最后一列

- 状态：已完成用户端和管理员端创建订单页地区关键词表格按钮位置调整，并通过前端生产构建验证。
- 当前任务：将地区关键词区域中表格下方的“添加关键词”按钮移动到上方关键词表格的最后一列。
- 实现原则：只调整前端模板和局部样式；不修改订单数据结构、提交逻辑、接口、路由和数据库。

### 实施内容

1. 用户端创建订单页：移除表格下方独立的“添加关键词”按钮。
2. 用户端创建订单页：将“添加关键词”按钮放入关键词表格最后一列，仅在第一行显示。
3. 管理员端创建订单页：同步完成相同布局调整。
4. 为最后一列补充按钮对齐样式，使按钮在单元格中垂直居中并保持表格列宽稳定。
5. 保持 `addKeywordItem`、`removeKeywordItem` 和提交参数逻辑不变。

### 修改文件

1. `frontend/src/views/user/OrderCreateView.vue`
2. `frontend/src/views/admin/OrderCreateView.vue`
3. `PROJECT_PROGRESS.md`

### 验证结果

1. `cmd /c npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. 前端构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本次未处理。

### 下一步任务清单

1. 部署新的 `frontend/dist` 后检查用户端关键词安装订单创建页。
2. 检查管理员端推广服务创建订单页同一区域。
3. 确认多关键词、多地区情况下“添加关键词”只在每个地区配置表格第一行最后一列出现。
4. 检查窄屏下关键词表格是否需要进一步优化。

### 风险点

1. 本次仅为视觉与布局调整，业务逻辑无变化。
2. 窄屏下表格已有响应式规则，若后续要求移动端也必须完整显示最后一列，可能需要单独做移动端表格布局优化。

## 2026-06-25 更新：store 类型图标替换为老系统图标

- 状态：已完成 App Store、Google Play、iPad Store 图标替换，并通过前端生产构建验证。
- 当前任务：将新系统中 store 类型相关图标替换为老系统图标，避免字母、CSS 图形和线框图标风格不一致。
- 实现原则：复用老系统已有图片资源；新增轻量 `StoreIcon` 组件统一映射；不修改业务逻辑、接口、路由和数据库。

### 实施内容

1. 从老系统复制 `as.png` 和 `gp.png` 到新系统静态资源目录。
2. 新增 `StoreIcon` 组件，统一处理 `APP_STORE`、`GOOGLE_PLAY`、`IPAD_STORE` 图标映射。
3. 用户端和管理员端创建订单页的应用商店 tab 改为老系统图标。
4. 用户端和管理员端应用管理页的商店筛选、下拉选项、列表列和用户端新增应用弹窗改为老系统图标。
5. 用户端订单列表、管理员端订单列表、管理员端待确认订单列表的商店筛选下拉改为老系统图标。
6. 首页平台展示改为同一套 store 图标。
7. 清理旧的字母图标、CSS 三角图标和线框图标相关函数与样式。

### 修改文件

1. `frontend/src/assets/logo/as.png`
2. `frontend/src/assets/logo/gp.png`
3. `frontend/src/components/StoreIcon.vue`
4. `frontend/src/views/public/HomeView.vue`
5. `frontend/src/views/user/OrderCreateView.vue`
6. `frontend/src/views/admin/OrderCreateView.vue`
7. `frontend/src/views/user/ApplicationManagementView.vue`
8. `frontend/src/views/admin/ApplicationManagementView.vue`
9. `frontend/src/views/user/StoreOrdersView.vue`
10. `frontend/src/views/admin/StoreOrdersView.vue`
11. `frontend/src/views/admin/PendingConfirmOrdersView.vue`
12. `PROJECT_PROGRESS.md`

### 验证结果

1. `cmd /c npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. 前端构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本次未处理。

### 下一步任务清单

1. 将新的 `frontend/dist` 上传覆盖到服务器 `/www/wwwroot/youou-aso/frontend`。
2. 清理浏览器缓存后检查创建订单页应用商店 tab 图标。
3. 检查应用管理页筛选下拉、列表商店列和新增应用弹窗。
4. 检查订单列表和待确认订单列表的商店筛选下拉。
5. 确认 iPad Store 复用老系统 App Store 图标符合预期。

### 风险点

1. 老系统没有独立 iPad 图标，本次按老系统逻辑复用 App Store 图标。
2. 线上发布后旧静态资源可能被浏览器缓存，需要强制刷新或清理缓存。

## 2026-06-25 更新：关键词安装订单改为单日期选择

- 状态：已完成用户端和管理员端订单创建页关键词安装订单日期选择调整，并通过前端生产构建验证。
- 当前任务：关键词安装类型的订单时间不能选择时间区间，只能选择一个具体日期。
- 实现原则：不修改接口、路由或数据库；前端提交时继续使用已有 `startDate`/`endDate` 字段，关键词安装将两者设为同一天，保持后端兼容。

### 实施内容

1. 用户端订单创建页：关键词安装订单时间控件由日期范围切换为单日期。
2. 管理员端订单创建页：关键词安装订单时间控件由日期范围切换为单日期。
3. 下载量、评分、评论等其他普通订单类型继续使用日期范围。
4. 关键词安装提交时自动将 `startDate` 和 `endDate` 设置为所选日期。
5. 补充中英文单日期占位文案，并将必填提示从“订单时间”调整为“订单日期”。

### 修改文件

1. `frontend/src/views/user/OrderCreateView.vue`
2. `frontend/src/views/admin/OrderCreateView.vue`
3. `frontend/src/i18n/locales/zh-CN.ts`
4. `frontend/src/i18n/locales/en-US.ts`
5. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. 前端构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本次未处理。

### 下一步任务清单

1. 将新的 `frontend/dist` 上传覆盖到服务器 `/www/wwwroot/youou-aso/frontend`。
2. 在用户端创建关键词安装订单，确认订单时间只显示单日期选择器。
3. 在管理员端创建关键词安装订单，确认订单时间只显示单日期选择器。
4. 创建关键词安装订单后查看订单详情，确认订单开始日期和结束日期相同。
5. 检查下载量、评分、评论订单仍保留日期范围选择。

### 风险点

1. 本次为前端兼容调整，后端仍接收 `startDate`/`endDate`，关键词安装订单两者相同。
2. 线上更新后需要清理浏览器缓存或强制刷新，避免旧静态资源继续显示日期范围控件。

## 2026-06-25 更新：推广服务卡片按钮底部对齐

- 状态：已完成用户端和管理员端推广服务卡片按钮对齐调整，并通过前端生产构建验证。
- 当前任务：用户反馈推广服务页面第二行特殊服务卡片按钮高度与左右卡片不在同一水平线，需要对齐。
- 实现原则：仅调整卡片内部 CSS 布局，不修改业务逻辑、接口、路由和数据库。

### 实施内容

1. 将推广服务卡片设置为纵向 flex 容器。
2. 将卡片正文区域设置为可撑满剩余高度的纵向 flex 布局。
3. 将卡片按钮设置为 `margin-top: auto`，固定在卡片底部位置。
4. 将描述区域从固定高度调整为最小高度，避免标题换行时挤压按钮对齐。
5. 用户端和管理员端推广服务页面保持一致的卡片对齐策略。

### 修改文件

1. `frontend/src/views/user/PromotionServicesView.vue`
2. `frontend/src/views/admin/PromotionServicesView.vue`
3. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. 前端构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本次未处理。

### 下一步任务清单

1. 将新的 `frontend/dist` 上传到服务器 `/www/wwwroot/youou-aso/frontend`。
2. 浏览器强制刷新或清理站点缓存后检查推广服务页面按钮是否水平对齐。
3. 如需进一步优化，可统一卡片标题与徽标在中英文环境下的换行策略。

### 风险点

1. 本次只调整卡片内部布局，不改变服务入口和下单流程。
2. 线上更新前端静态资源后，旧浏览器缓存可能短时间显示旧样式，需要强制刷新。

## 2026-06-24 更新：新增生产环境配置文件

- 状态：已完成 `prod` profile 配置文件新增，并通过后端测试验证。
- 当前任务：项目准备使用宝塔部署，需要补充生产环境 yml，避免生产环境直接复用本地开发配置。
- 实现原则：不写入生产数据库密码、Redis 密码、JWT 密钥或其他秘钥；所有敏感项均通过环境变量或服务器文件注入。

### 实施内容

1. 新增 `backend/src/main/resources/application-prod.yml`。
2. 数据库连接、账号、密码通过 `YOUOU_DATASOURCE_*` 环境变量配置。
3. Redis 连接通过 `YOUOU_REDIS_*` 环境变量配置。
4. JWT 密钥通过 `YOUOU_JWT_SECRET` 或 `YOUOU_JWT_SECRET_FILE` 配置。
5. 生产端口、Tomcat 临时目录、日志文件路径和日志级别支持环境变量覆盖。
6. Flyway 在生产 profile 下继续启用，确保首次启动自动执行数据库迁移。
7. Spring SQL 初始化设置为 `never`，避免与 Flyway 迁移职责冲突。
8. 未修改 `application-local.yml`、`.env` 或任何秘钥配置文件。

### 修改文件

1. `backend/src/main/resources/application-prod.yml`
2. `PROJECT_PROGRESS.md`

### 验证结果

1. `mvn.cmd test`：通过，后端 88 个测试全部成功。

### 下一步任务清单

1. 宝塔 Java 项目启动参数使用 `--spring.profiles.active=prod`。
2. 在宝塔 Java 项目环境变量中配置 `YOUOU_DATASOURCE_URL`、`YOUOU_DATASOURCE_USERNAME`、`YOUOU_DATASOURCE_PASSWORD`。
3. 根据生产 Redis 情况配置 `YOUOU_REDIS_HOST`、`YOUOU_REDIS_PORT`、`YOUOU_REDIS_PASSWORD`、`YOUOU_REDIS_DATABASE`。
4. 配置 `YOUOU_JWT_SECRET`，或在服务器 `/opt/youou-aso/secrets/jwt-secret` 放置 JWT 密钥文件并配置 `YOUOU_JWT_SECRET_FILE`。
5. 确认 `/opt/youou-aso/logs` 和 `/opt/youou-aso/secrets` 目录存在，且运行用户具备对应写入或读取权限。
6. 首次启动后检查 Flyway 日志，确认数据库迁移已执行到最新版本。

### 风险点

1. `application.yml` 默认仍激活 `local`，生产部署必须显式传入 `--spring.profiles.active=prod`。
2. 如果服务器未配置 JWT 密钥，生产环境会因为密钥缺失存在启动或登录风险，部署前必须配置。
3. `application-local.yml` 仍包含本地开发配置，不应用于生产部署。

## 2026-06-22 更新：登录页记住密码改为记住账号

- 状态：已完成登录页记住项文案与实际行为对齐，并通过前端生产构建验证。
- 当前任务：用户反馈勾选“记住密码”后密码输入框仍为空。
- 根因：登录页原实现只在 `localStorage` 保存账号和勾选状态，没有保存密码；但文案写成“记住密码”，造成预期不一致。
- 安全决策：生产系统不在应用代码中把明文密码保存到 `localStorage`，避免 XSS 或本机泄露时直接暴露用户密码；密码保存与回填交给浏览器密码管理器处理。

### 实施内容

1. 登录页勾选项从“记住密码 / Remember password”改为“记住账号 / Remember account”。
2. 前端变量从 `rememberPassword` 调整为 `rememberAccount`，语义与实际行为一致。
3. 保持本地存储 key 不变，兼容历史 `rememberPassword` 字段读取，避免旧用户丢失已记住账号。
4. 不保存、不回填用户密码；保留密码输入框 `autocomplete="current-password"`，由浏览器密码管理器负责密码回填。

### 修改文件

1. `frontend/src/views/public/LoginView.vue`
2. `frontend/src/i18n/locales/zh-CN.ts`
3. `frontend/src/i18n/locales/en-US.ts`
4. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. 前端构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本轮未处理。

### 下一步任务清单

1. 勾选“记住账号”登录后退出，确认重新进入登录页会回填账号。
2. 如需密码自动回填，请使用浏览器自带保存密码功能确认提示和回填效果。

### 风险点

1. 本次不会在应用本地存储中保存密码，因此密码框仍可能为空；这是出于生产安全要求的刻意行为。

## 2026-06-22 更新：修复下载量输入步进过大

- 状态：已完成下载量订单每日下载量输入步进修复，并通过前端生产构建验证。
- 当前任务：用户反馈创建下载量订单时点击加号一次会从 1 变成 11，即每次增加 10。
- 根因：用户端和管理员端订单创建页的 `dailyDownloadCount` 输入框均配置了 `:step="10"`，导致 Element Plus 数字输入框加减按钮按 10 递增/递减。

### 实施内容

1. 用户端下载量订单“每日下载量”输入框步进从 10 调整为 1。
2. 管理员端下载量订单“每日下载量”输入框步进从 10 调整为 1。
3. 未修改下载量订单计费公式、提交参数和后端逻辑。

### 修改文件

1. `frontend/src/views/user/OrderCreateView.vue`
2. `frontend/src/views/admin/OrderCreateView.vue`
3. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. 前端构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本轮未处理。

### 下一步任务清单

1. 打开用户端下载量订单创建页，确认点击加号每次只增加 1。
2. 打开管理员端下载量订单创建页，确认点击加号每次只增加 1。

### 风险点

1. 本次只调整输入控件步进，不限制用户手动输入更大的下载量。

## 2026-06-22 更新：用户特殊订单列表补充应用商店列

- 状态：已完成用户端特殊订单列表应用商店信息补充，并通过前端生产构建验证。
- 当前任务：用户反馈特殊订单列表看不出订单属于苹果、谷歌还是 iPad。
- 实现原则：复用特殊订单审核记录已有 `storeType` 字段，仅补充前端展示，不改接口和数据结构。

### 实施内容

1. 用户端“订单 / 特殊订单”列表新增“应用商店”列。
2. 根据 `storeType` 显示 `App Store`、`Google Play` 或 `iPad Store`。
3. 保持应用列只展示应用图标、应用名和应用标识，避免信息过度堆叠。

### 修改文件

1. `frontend/src/views/user/SpecialOrdersView.vue`
2. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. 前端构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本轮未处理。

### 下一步任务清单

1. 打开用户端“订单 / 特殊订单”，确认每条特殊订单显示对应应用商店。
2. 使用 App Store、Google Play、iPad Store 的特殊订单数据分别验证显示是否正确。

### 风险点

1. 本次只新增前端列展示；如后端返回的 `storeType` 为空或异常，会默认显示为 `App Store`，后续可按接口约束再增强兜底。

## 2026-06-22 更新：用户端特殊订单从推广服务页拆出

- 状态：已完成用户端特殊订单需求列表独立页面拆分，并通过前端生产构建验证。
- 当前任务：用户反馈“用户的特殊订单不能放在推广服务里面”，推广服务应作为订购入口，特殊订单跟进应归入订单体系。
- 实现原则：不改特殊订单接口、状态流转和支付逻辑，只调整用户端信息架构与页面承载位置。

### 实施内容

1. 新增用户端 `SpecialOrdersView.vue`，用于展示“我的特殊订单需求”列表。
2. 用户端“推广服务”页移除特殊订单需求列表，仅保留服务卡片和下单入口。
3. 用户端“订单”二级菜单新增“特殊订单”。
4. 新增路由 `/user/orders/special`，顶部标题显示“订单 / 特殊订单”。
5. 用户提交关键词保排名、关键词覆盖需求成功后，跳转到“特殊订单”页面。
6. 保留特殊订单已审核待支付时的“支付并提交”逻辑，支付成功后仍跳转到对应商店订单列表。

### 修改文件

1. `frontend/src/views/user/PromotionServicesView.vue`
2. `frontend/src/views/user/SpecialOrdersView.vue`
3. `frontend/src/layouts/UserLayout.vue`
4. `frontend/src/router/index.ts`
5. `frontend/src/views/user/OrderCreateView.vue`
6. `frontend/src/i18n/locales/zh-CN.ts`
7. `frontend/src/i18n/locales/en-US.ts`
8. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. 前端构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本轮未处理。

### 下一步任务清单

1. 登录用户端，确认“推广服务”页只显示服务订购入口。
2. 展开“订单”菜单，确认新增“特殊订单”入口。
3. 提交特殊订单需求后，确认跳转到“订单 / 特殊订单”页面。
4. 在特殊订单页确认已审核待支付订单仍可支付并生成正式订单。

### 风险点

1. 本次只调整前端信息架构；后端特殊订单数据和接口未改。

## 2026-06-22 更新：修正特殊需求取消弹窗确认按钮文案

- 状态：已完成特殊需求审核页取消弹窗按钮文案修正，并通过前端生产构建验证。
- 当前任务：用户反馈特殊订单审核管理中的取消弹窗，右下角红色主按钮应显示“确认”，不应显示“取消”。
- 根因：`AuditManagementView.vue` 的取消弹窗标题、列表动作和主提交按钮共用了 `specialAudit.cancel` 文案，导致弹窗内主按钮显示为“取消”。

### 实施内容

1. 保留弹窗标题“取消”和列表操作“取消”不变。
2. 将取消弹窗右下角主提交按钮改为复用通用“确认”文案。
3. 不改取消接口、校验逻辑、状态流转和业务语义。

### 修改文件

1. `frontend/src/views/admin/AuditManagementView.vue`
2. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. 前端构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本轮未处理。

### 下一步任务清单

1. 在特殊订单审核管理页打开取消弹窗，确认右下角红色按钮显示为“确认”。
2. 点击确认后仍按原逻辑提交取消原因并取消需求。

### 风险点

1. 本次只调整按钮显示文案，不改变取消操作的危险性颜色和业务行为。

## 2026-06-22 更新：修正充值与划扣流水类型显示名

- 状态：已完成“充值 / 划扣”显示名纠偏，并通过后端测试与前端生产构建验证。
- 当前任务：用户反馈“管理员充值改为默认充值”不是显示“默认充值”，而是去掉管理员语义后显示“充值”；“默认划扣”同理应显示为“划扣”。
- 根因：上一轮实现把 `ADMIN_RECHARGE` 和 `ADMIN_ADJUSTMENT` 的配置显示名理解成“默认充值 / 默认划扣”，实际产品文案应为更简洁的业务类型“充值 / 划扣”。

### 实施内容

1. `ADMIN_RECHARGE` 默认显示名调整为“充值 / Recharge”。
2. `ADMIN_ADJUSTMENT` 默认显示名调整为“划扣 / Deduction”。
3. 新增 `V15__wallet_transaction_type_short_names.sql`，用于修正已执行过 v14 的数据库配置值。
4. 前端中英文语言包同步更新兜底文案。
5. 未修改流水编码、资金方向、充值接口和历史流水业务逻辑。

### 修改文件

1. `backend/src/main/java/com/youou/aso/modules/wallet/domain/WalletTransactionTypeConfig.java`
2. `backend/src/main/resources/db/migration/V15__wallet_transaction_type_short_names.sql`
3. `frontend/src/i18n/locales/zh-CN.ts`
4. `frontend/src/i18n/locales/en-US.ts`
5. `PROJECT_PROGRESS.md`

### 验证结果

1. `mvn.cmd test`：通过，后端 88 个测试全部成功；本地 schema 已从 v14 迁移到 v15。
2. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
3. 前端构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本轮未处理。

### 下一步任务清单

1. 重启后端后进入“系统管理 / 流水类型配置”，确认显示“充值”“划扣”“配送”。
2. 切换英文环境，确认对应显示为 “Recharge”“Deduction”“Delivery”。

### 风险点

1. 已执行过的 migration 不应再修改，如需继续改展示名应通过新的 migration 或配置页面保存。

## 2026-06-22 更新：调整默认流水类型名称并新增配送类型

- 状态：已完成流水类型默认显示名调整和“配送”类型新增，并通过后端测试与前端生产构建验证。
- 当前任务：用户要求将“管理员充值”改为“默认充值”，“管理员调整”改为“默认划扣”，并新增“配送”流水类型。
- 实现原则：保留既有资金流水编码不变，通过新增 migration 更新配置显示名；新增 `DELIVERY` 作为真实流水类型编码，但当前仅提供配置和展示能力，不自动产生配送流水。

### 实施内容

1. `ADMIN_RECHARGE` 默认显示名调整为“默认充值 / Default recharge”。
2. `ADMIN_ADJUSTMENT` 默认显示名调整为“默认划扣 / Default deduction”。
3. 新增流水类型编码 `DELIVERY`，默认显示名为“配送 / Delivery”。
4. 新增 `V14__wallet_transaction_type_delivery.sql`，用于升级已执行过 v13 的数据库。
5. 恢复已执行过的 `V13__wallet_transaction_type_config.sql`，避免 Flyway checksum 变化导致启动失败。
6. 同步后端枚举、配置默认值、查询排序、单元测试和前端类型定义。
7. 用户端收支明细筛选项和前端默认语言包增加“配送”类型。

### 修改文件

1. `backend/src/main/java/com/youou/aso/modules/wallet/domain/WalletTransactionType.java`
2. `backend/src/main/java/com/youou/aso/modules/wallet/domain/WalletTransactionTypeConfig.java`
3. `backend/src/main/java/com/youou/aso/modules/wallet/repository/JdbcWalletTransactionTypeConfigRepository.java`
4. `backend/src/main/resources/db/migration/V14__wallet_transaction_type_delivery.sql`
5. `backend/src/test/java/com/youou/aso/modules/wallet/service/WalletTransactionTypeConfigServiceTest.java`
6. `frontend/src/api/wallet.ts`
7. `frontend/src/views/user/TransactionsView.vue`
8. `frontend/src/i18n/locales/zh-CN.ts`
9. `frontend/src/i18n/locales/en-US.ts`
10. `PROJECT_PROGRESS.md`

### 验证结果

1. `mvn.cmd test`：通过，后端 88 个测试全部成功。
2. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
3. 后端测试启动时 Flyway 已成功将本地 schema 从 v13 迁移到 v14。
4. 首次验证曾因修改已执行过的 v13 migration 触发 Flyway checksum mismatch，已通过恢复 v13 并保留 v14 增量迁移修复。
5. v14 migration 执行时 MySQL 提示 `VALUES()` 在未来版本会弃用，当前为非阻断警告；本轮不再修改已执行 migration。

### 下一步任务清单

1. 重启后端后进入“系统管理 / 流水类型配置”，确认显示“默认充值”“默认划扣”“配送”。
2. 切换英文环境，确认对应显示为 “Default recharge”“Default deduction”“Delivery”。
3. 如后续需要实际产生“配送”流水，需要单独设计配送业务触发点、资金方向和金额来源。

### 风险点

1. `DELIVERY` 当前只是可配置、可展示的流水类型；没有业务流程写入该类型流水。
2. 已执行过的 migration 不应再修改，否则会触发 Flyway checksum 校验失败。

## 2026-06-22 更新：新增流水类型显示名称配置

- 状态：已完成“流水类型配置”系统配置能力，并通过后端测试与前端生产构建验证。
- 当前任务：用户要求新增系统配置，系统已有流水类型只能修改显示名字，不能新增或改变真实流水类型。
- 实现原则：保留资金流水的固定枚举编码用于审计，只允许维护中英文显示名；不改变历史流水数据，不允许页面新增、删除或禁用流水类型。

### 实施内容

1. 新增 `wallet_transaction_type_config` 配置表，初始化四个固定流水类型的中英文默认显示名。
2. 后端新增流水类型显示配置 domain、DTO、repository、service。
3. 管理员端新增读取和保存接口，用户端新增只读接口。
4. 管理员端“系统管理”新增“流水类型配置”菜单和页面。
5. 流水类型配置页面只读展示编码，允许编辑中文显示名和英文显示名。
6. 管理员财务流水、用户收支明细、用户首页最近收支改为优先使用配置显示名。
7. 配置加载失败时，前端自动回退到语言包默认文案，避免影响资金流水页面可用性。
8. 补充后端单元测试，覆盖默认配置、自定义显示名和空显示名拒绝。

### 修改文件

1. `backend/src/main/resources/db/migration/V13__wallet_transaction_type_config.sql`
2. `backend/src/main/java/com/youou/aso/modules/wallet/domain/WalletTransactionTypeConfig.java`
3. `backend/src/main/java/com/youou/aso/modules/wallet/dto/WalletTransactionTypeConfigResult.java`
4. `backend/src/main/java/com/youou/aso/modules/wallet/dto/UpdateWalletTransactionTypeConfigCommand.java`
5. `backend/src/main/java/com/youou/aso/modules/wallet/repository/WalletTransactionTypeConfigRepository.java`
6. `backend/src/main/java/com/youou/aso/modules/wallet/repository/JdbcWalletTransactionTypeConfigRepository.java`
7. `backend/src/main/java/com/youou/aso/modules/wallet/service/WalletTransactionTypeConfigService.java`
8. `backend/src/main/java/com/youou/aso/modules/wallet/api/AdminWalletController.java`
9. `backend/src/main/java/com/youou/aso/modules/wallet/api/CustomerWalletController.java`
10. `backend/src/test/java/com/youou/aso/modules/wallet/service/WalletTransactionTypeConfigServiceTest.java`
11. `frontend/src/api/wallet.ts`
12. `frontend/src/composables/useWalletTransactionTypeLabels.ts`
13. `frontend/src/views/admin/WalletTransactionTypeConfigView.vue`
14. `frontend/src/views/admin/FinanceManagementView.vue`
15. `frontend/src/views/user/TransactionsView.vue`
16. `frontend/src/views/user/DashboardView.vue`
17. `frontend/src/router/index.ts`
18. `frontend/src/layouts/AdminLayout.vue`
19. `frontend/src/i18n/locales/zh-CN.ts`
20. `frontend/src/i18n/locales/en-US.ts`
21. `PROJECT_PROGRESS.md`

### 验证结果

1. `mvn.cmd test`：通过，后端 88 个测试全部成功。
2. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
3. 后端测试启动时 Flyway 已成功将本地 schema 从 v12 迁移到 v13。
4. 前端构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本轮未处理。

### 下一步任务清单

1. 重启后端后进入“系统管理 / 流水类型配置”，确认四个固定流水类型显示正常。
2. 修改任一流水类型中文名后保存，再刷新“财务流水”和用户端“收支明细”，确认列表显示新名称。
3. 切换英文环境，确认英文显示名生效。

### 风险点

1. 本次新增数据库 migration，生产或本地重启后会自动建表并初始化默认值。
2. 流水类型编码仍为固定枚举，不支持业务人员新增类型；如未来确需新增真实流水类型，需要单独做资金业务设计和审计评估。
3. 如果配置接口异常，前端会回退语言包默认文案，不会阻断流水列表查看。

## 2026-06-22 更新：修复消费记录订单类型翻译与订单信息关联

- 状态：已完成消费记录页面订单类型文案和订单信息关联兜底修复，并通过后端测试与前端生产构建验证。
- 当前任务：用户反馈“消费记录”页面订单类型筛选显示 `ordersPage.all`，且列表中订单编号、应用名称、订单类型为空。
- 问题定位：前端引用了不存在的 `ordersPage.all` 翻译键；钱包流水查询只通过扣款/退款流水 ID 关联订单，对历史或兜底数据未使用 `related_order_id`；消费记录页也没有在结构化订单类型缺失时解析已有流水备注。

### 实施内容

1. 消费记录页订单类型筛选占位改用现有 `ordersPage.allTaskTypes` 文案。
2. 中英文语言包补充通用 `ordersPage.all`，避免其它页面继续裸显示翻译 key。
3. 钱包流水查询关联订单时新增 `wt.related_order_id = o.id` 兜底条件。
4. 消费记录页在后端未返回结构化 `orderType` 时，从 `ORDER_DEDUCT:<订单类型>` 备注中解析订单类型，用于列表显示和筛选。

### 修改文件

1. `backend/src/main/java/com/youou/aso/modules/wallet/repository/JdbcWalletQueryRepository.java`
2. `frontend/src/views/user/ConsumptionRecordsView.vue`
3. `frontend/src/i18n/locales/zh-CN.ts`
4. `frontend/src/i18n/locales/en-US.ts`
5. `PROJECT_PROGRESS.md`

### 验证结果

1. `mvn.cmd test`：通过，后端 85 个测试全部成功。
2. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
3. 前端构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本轮未处理。

### 下一步任务清单

1. 重启后端并刷新用户端“消费记录”，确认订单类型筛选显示“全部”而不是翻译 key。
2. 使用已有消费流水确认订单编号、应用名称、订单类型能从关联订单或备注兜底中展示。
3. 如仍存在历史流水无法展示订单编号/应用名称，需要进一步检查该流水是否确实没有关联订单记录。

### 风险点

1. 如果历史流水没有 `deducted_transaction_id/refund_transaction_id`，也没有 `related_order_id`，则订单编号和应用名称无法可靠反查，只能继续显示空占位。
2. 备注解析只作为历史兜底使用，新数据仍应优先依赖结构化订单字段。

## 2026-06-22 更新：用户端新增消费记录菜单

- 状态：已完成用户端“消费记录”独立菜单和页面，并通过后端测试与前端生产构建验证。
- 当前任务：用户要求在用户端新增“消费记录”菜单，页面参考截图展示订单消费记录筛选区和列表区。
- 实现原则：不修改数据库结构，不新增依赖，不修改 `.env` 或秘钥配置；复用现有钱包流水与订单扣款关系，避免重复存储消费数据。

### 实施内容

1. 用户端侧边栏新增“消费记录”菜单，路由为 `/user/consumption-records`。
2. 新增用户端消费记录页面，仅展示订单扣款流水 `ORDER_DEDUCT`。
3. 页面提供订单类型、开始日期、结束日期筛选，以及搜索、重置操作。
4. 列表展示订单编号、应用名称、订单类型、消费前金额、订单金额、消费后金额、支付时间。
5. 根据此前“备注字段去掉”的要求，消费记录列表不显示备注列。
6. 后端钱包流水查询补充关联订单的应用名称，前端可直接展示应用名称，减少业务人员理解成本。
7. 修复用户端“收支明细”页面筛选后列表仍使用原始数据的问题，改为使用筛选结果。

### 修改文件

1. `backend/src/main/java/com/youou/aso/modules/wallet/domain/WalletTransaction.java`
2. `backend/src/main/java/com/youou/aso/modules/wallet/dto/WalletTransactionResult.java`
3. `backend/src/main/java/com/youou/aso/modules/wallet/repository/JdbcWalletQueryRepository.java`
4. `frontend/src/api/wallet.ts`
5. `frontend/src/router/index.ts`
6. `frontend/src/layouts/UserLayout.vue`
7. `frontend/src/views/user/ConsumptionRecordsView.vue`
8. `frontend/src/views/user/TransactionsView.vue`
9. `frontend/src/i18n/locales/zh-CN.ts`
10. `frontend/src/i18n/locales/en-US.ts`
11. `PROJECT_PROGRESS.md`

### 验证结果

1. `mvn.cmd test`：通过，后端 85 个测试全部成功。
2. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
3. 前端构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本轮未处理。

### 下一步任务清单

1. 重启后端后刷新用户端，确认“消费记录”菜单可见且列表能显示应用名称。
2. 用真实扣款订单验证消费前金额、订单金额、消费后金额和支付时间是否符合业务预期。
3. 如后续消费记录数据量增大，可再扩展用户端钱包流水接口的后端分页与筛选参数。

### 风险点

1. 当前消费记录基于现有钱包流水接口最多拉取最近 100 条后在前端筛选；数据量增大后需要后端分页筛选。
2. 历史扣款流水如果无法关联到订单，应用名称会显示为空占位，不会影响现有收支明细。

## 2026-06-22 更新：用户收支明细改为筛选区加列表区

- 状态：已完成用户端收支明细页面布局调整，并通过前端生产构建验证。
- 当前任务：用户要求用户页面的收支明细不显示统计区域，改为查询筛选区和列表区。
- 实现原则：仅调整用户端收支明细页面展示；不修改后端接口、数据库结构或用户余额逻辑；账户余额仍由用户端顶部余额入口展示。

### 实施内容

1. 移除用户端收支明细页面顶部统计卡片区域，包括可用余额、冻结余额和用户 ID。
2. 页面加载时不再请求钱包概览接口，只请求流水列表接口。
3. 新增查询筛选区，支持按流水类型和收支方向筛选。
4. 查询筛选区提供搜索、清空和刷新操作。
5. 保留原流水列表区及订单编号、订单类型、方向、金额、变动后余额、创建时间等列。

### 修改文件

1. `frontend/src/views/user/TransactionsView.vue`
2. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. 构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本轮未处理。

### 下一步任务清单

1. 刷新用户端“收支明细”，确认页面只包含筛选区和列表区。
2. 分别筛选订单扣款、订单退款、管理员充值，以及收入/支出方向，确认列表过滤符合预期。

### 风险点

1. 当前筛选为前端本地过滤最近 50 条流水；如未来流水量变大，需要扩展用户端流水接口支持后端分页和筛选参数。
2. 统计区域移除后，用户查看余额依赖顶部全局余额入口。

## 2026-06-22 更新：财务流水列表移除备注列

- 状态：已完成财务流水列表和用户收支明细列表的备注列移除，并通过前端生产构建验证。
- 当前任务：用户要求去掉备注字段。
- 实现原则：仅移除列表展示列和无备注文案；保留后端接口字段、数据库字段和充值录入表单中的“充值备注”，避免影响历史数据、接口兼容性和后台登记充值说明。

### 实施内容

1. 管理员端“财务管理 / 财务流水”表格移除备注列。
2. 用户端“收支明细”表格移除备注列。
3. 删除两个页面中已不再使用的备注格式化函数。
4. 移除钱包语言包中列表备注和无备注文案。

### 修改文件

1. `frontend/src/views/admin/FinanceManagementView.vue`
2. `frontend/src/views/user/TransactionsView.vue`
3. `frontend/src/i18n/locales/zh-CN.ts`
4. `frontend/src/i18n/locales/en-US.ts`
5. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. 构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本轮未处理。

### 下一步任务清单

1. 刷新管理员端财务流水和用户端收支明细，确认表格不再显示备注列。
2. 如后续不再需要充值备注录入，可单独确认后再移除充值弹窗的“充值备注”字段和后端 remark 入参。

### 风险点

1. 后端仍返回 `remark` 字段以保持接口兼容；本次仅做页面展示收敛。
2. 充值记录中已录入的备注不会丢失，只是不在流水列表中展示。

## 2026-06-22 更新：财务流水订单信息结构化展示

- 状态：已完成财务流水和用户收支明细中的订单信息结构化展示，并通过后端全量测试和前端生产构建验证。
- 当前任务：用户反馈财务流水列表用备注表达订单类型不合适，并且语言不一致。
- 问题定位：订单扣款/退款流水的备注中写入了 `ORDER_DEDUCT:*`、`ORDER_REFUND:*` 等内部标记，前端直接展示备注，导致业务页面暴露技术字段且无法多语言统一。
- 实现原则：不修改数据库结构，不迁移历史数据；利用订单表中已有的 `deducted_transaction_id/refund_transaction_id` 与钱包流水反向关联，按结构化字段返回订单编号和订单类型。

### 实施内容

1. 后端 `WalletTransaction` 和 `WalletTransactionResult` 增加非持久化字段 `orderNo`、`orderType`。
2. 钱包流水查询 SQL 左连接 `aso_order`，通过扣款/退款交易 ID 反查关联订单。
3. 对历史特殊扣款中仍只有内部备注标记的记录，按 `ORDER_DEDUCT:<ORDER_TYPE>` 做兜底解析订单类型。
4. 管理员财务流水和用户收支明细新增“关联订单”“订单类型”列。
5. 备注列过滤内部 `ORDER_DEDUCT:*`、`ORDER_REFUND:*` 标记，只展示真正业务备注；无备注时显示本地化的“无备注”。
6. 订单类型展示复用现有 `ordersPage.types` 多语言文案，避免财务页和订单页翻译不一致。

### 修改文件

1. `backend/src/main/java/com/youou/aso/modules/wallet/domain/WalletTransaction.java`
2. `backend/src/main/java/com/youou/aso/modules/wallet/dto/WalletTransactionResult.java`
3. `backend/src/main/java/com/youou/aso/modules/wallet/repository/JdbcWalletQueryRepository.java`
4. `frontend/src/api/wallet.ts`
5. `frontend/src/views/admin/FinanceManagementView.vue`
6. `frontend/src/views/user/TransactionsView.vue`
7. `frontend/src/i18n/locales/zh-CN.ts`
8. `frontend/src/i18n/locales/en-US.ts`
9. `PROJECT_PROGRESS.md`

### 验证结果

1. `mvn.cmd test`：通过，后端 85 个测试全部成功。
2. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
3. 构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本轮未处理。

### 下一步任务清单

1. 重启后端后刷新“财务管理 / 财务流水”，确认订单扣款流水单独显示订单编号和订单类型。
2. 刷新用户端“收支明细”，确认不再在备注列看到 `ORDER_DEDUCT:*`、`ORDER_REFUND:*` 等内部标记。
3. 如后续需要点击订单编号跳转订单详情，可单独增加基于 `relatedOrderId` 的详情入口。

### 风险点

1. 本次没有修改历史流水备注，只是在查询和展示层做结构化补齐；如果历史数据既没有关联订单也没有可解析备注，订单类型会显示为空。
2. 当前订单编号只做展示，不做跳转；后续若增加跳转，需要区分管理员端和用户端详情路由权限。

## 2026-06-22 更新：管理员管理支持创建普通管理员

- 状态：已完成管理员创建能力补齐，并通过后端账号相关测试和前端生产构建验证。
- 当前任务：用户反馈“管理员管理”页面不能创建管理员。
- 问题定位：当前页面和接口只支持管理员列表查看与状态调整，前端还有“只支持查看和状态控制”的提示；后端缺少 `POST /api/admin/admin-accounts` 创建接口，因此不是页面按钮隐藏，而是功能未实现。
- 实现原则：只新增超级管理员创建普通管理员能力；不修改 `.env` 或秘钥配置；不安装依赖；不删除文件；不修改数据库结构；不允许通过页面直接创建超级管理员。

### 实施内容

1. 后端新增 `CreateAdminAccountCommand`，并新增超级管理员创建普通管理员接口。
2. 创建管理员时复用现有 `PasswordEncoder`，初始密码保存为 BCrypt 哈希。
3. 创建前校验用户名和邮箱在管理员账户、用户账户中均不能重复，避免登录账号冲突。
4. 新管理员默认角色为 `ADMIN`，默认状态为 `ENABLED`，默认语言为 `zh-CN`。
5. 管理员管理页新增“创建管理员”按钮和弹窗表单，支持填写用户名、邮箱和初始密码。
6. 管理员管理页移除“只支持查看”的提示，并收敛状态操作为启用/禁用，不再展示锁定按钮。
7. 补充中英文多语言文案和前端 API。

### 修改文件

1. `backend/src/main/java/com/youou/aso/modules/account/api/AdminAccountController.java`
2. `backend/src/main/java/com/youou/aso/modules/account/service/AdminAccountService.java`
3. `backend/src/main/java/com/youou/aso/modules/account/repository/AdminAccountRepository.java`
4. `backend/src/main/java/com/youou/aso/modules/account/repository/JdbcAdminAccountRepository.java`
5. `backend/src/main/java/com/youou/aso/modules/account/dto/CreateAdminAccountCommand.java`
6. `backend/src/test/java/com/youou/aso/modules/account/api/AdminAccountControllerTest.java`
7. `backend/src/test/java/com/youou/aso/modules/account/service/AuthServiceTest.java`
8. `frontend/src/api/adminAccounts.ts`
9. `frontend/src/views/admin/AdminAccountsView.vue`
10. `frontend/src/i18n/locales/zh-CN.ts`
11. `frontend/src/i18n/locales/en-US.ts`
12. `PROJECT_PROGRESS.md`

### 验证结果

1. `mvn.cmd "-Dtest=AdminAccountControllerTest,AuthServiceTest" test`：通过，9 个测试全部成功。
2. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
3. 构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本轮未处理。

### 下一步任务清单

1. 重启后端后进入“系统管理 / 管理员管理”，确认超级管理员可看到“创建管理员”按钮。
2. 使用弹窗创建普通管理员，确认列表刷新后出现新账号。
3. 使用新管理员账号登录，确认其只能访问普通管理员权限范围，不能进入超级管理员专属页面。

### 风险点

1. 当前创建的是普通管理员，不支持页面创建超级管理员；如需超级管理员授权流程，需要单独设计审批和审计策略。
2. 当前创建时由超级管理员设置初始密码，后续如需更严格的安全流程，可增加一次性重置链接或强制首次改密，但需要单独确认产品规则。

## 2026-06-22 更新：客服配置默认内容按当前语言显示

- 状态：已完成客服配置页和用户充值弹窗的默认客服文案本地化，并通过前端生产构建验证。
- 当前任务：用户反馈当前语言为中文时，“系统管理 / 客服配置”页面仍显示英文默认值 `Youou-ASO Support` 和英文联系说明。
- 问题定位：页面静态文案已走中文多语言，错误来自后端初始化配置和 Service 默认值中的英文默认数据；这些值作为“配置数据”返回后，前端原样展示，导致中文环境下看起来语言错误。
- 实现原则：不直接修改已有数据库记录，不新增数据库迁移；仅在前端展示层识别系统默认英文值并按当前语言映射，避免覆盖管理员真实自定义内容。

### 实施内容

1. 在中英文语言包中补充客服配置默认客服名称和默认联系说明。
2. 管理员客服配置页加载配置后，如果命中系统默认英文值或中文默认值，则按当前语言展示。
3. 用户端点击“充值”弹出的客服二维码弹窗同步使用本地化默认客服名称和说明。
4. 保持管理员手动配置的自定义客服名称、二维码 URL 和联系说明原样展示，不做自动覆盖。

### 修改文件

1. `frontend/src/i18n/locales/zh-CN.ts`
2. `frontend/src/i18n/locales/en-US.ts`
3. `frontend/src/views/admin/CustomerServiceConfigView.vue`
4. `frontend/src/layouts/UserLayout.vue`
5. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. 构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本轮未处理。

### 下一步任务清单

1. 刷新“系统管理 / 客服配置”页面，确认中文环境下默认客服名称显示为“Youou-ASO 客服”，联系说明显示中文。
2. 切换英文后确认默认客服名称和联系说明显示英文。
3. 如果管理员保存了自定义客服文案，应确认不会被本地化规则替换。

### 风险点

1. 本次仅映射系统默认值；如果数据库中已经被手工保存为其他英文内容，系统会视为管理员自定义内容并原样展示。
2. 后续若新增更多可配置默认文案，建议统一建立配置默认值本地化规范，避免配置数据和界面语言混用。

## 2026-06-22 更新：修复客服配置接口 500

- 状态：已完成客服配置加载失败的后端根因修复，并通过客服配置 Service 与 Controller 单元测试。
- 当前任务：用户反馈退出登录并重新登录后，系统管理 / 客服配置页面仍提示“客服配置加载失败”，Network 显示 `GET /api/admin/support/customer-service` 返回 `500 Internal Server Error`。
- 问题定位：登录态正常，数据库迁移和 `customer_service_config` 配置记录正常，Service 直接读取也正常；实际 500 发生在接口返回阶段，原因是 `CustomerServiceConfigResult.updatedAt` 使用 `LocalDateTime`，当前 HTTP JSON 序列化链路未正确处理该类型。
- 实现原则：仅修复客服配置返回 DTO 和 Service 转换逻辑；不修改 `.env` 或秘钥配置；不安装依赖；不删除文件；不修改路由、数据库结构或项目架构。

### 实施内容

1. 将 `CustomerServiceConfigResult.updatedAt` 从 `LocalDateTime` 改为 `String`，与前端已声明的 `string | null` 契约保持一致。
2. 在 `CustomerServiceConfigService` 中统一把 `LocalDateTime` 转为 ISO 字符串返回，避免接口序列化时抛出 500。
3. 保持客服配置的启用校验、二维码 URL 校验、默认配置逻辑不变。

### 修改文件

1. `backend/src/main/java/com/youou/aso/modules/support/dto/CustomerServiceConfigResult.java`
2. `backend/src/main/java/com/youou/aso/modules/support/service/CustomerServiceConfigService.java`
3. `PROJECT_PROGRESS.md`

### 验证结果

1. `mvn.cmd "-Dtest=CustomerServiceConfigServiceTest,CustomerServiceConfigControllerTest" test`：通过，7 个测试全部成功。
2. 本次未重启正在运行的后端服务；用户已说明由用户自行重启。因此当前浏览器里的旧后端进程仍可能继续返回旧错误，需重启后端后再刷新客服配置页面验证。

### 下一步任务清单

1. 用户重启后端后，重新进入“系统管理 / 客服配置”，确认配置可以正常加载。
2. 若页面仍失败，继续抓取接口 Response 内容和后端日志，优先排查运行实例是否加载了最新代码。

### 风险点

1. 该修复改变的是接口返回字段类型，但前端本来就是按字符串使用，兼容当前页面。
2. 如果其他未来接口继续直接返回 Java 8 时间类型，仍可能出现类似序列化问题，后续可统一梳理返回 DTO 的时间字段格式。

## 2026-06-22 更新：认证失效统一跳转登录

- 状态：已完成前端认证失效处理，避免后端重启或 token 失效后页面只显示局部“加载失败”，并通过前端生产构建验证。
- 当前任务：用户截图反馈客服配置页顶部提示“客服配置加载失败”，但页面仍显示已登录管理员状态。
- 问题定位：后端接口 `/api/admin/support/customer-service` 已存在，数据库 V12 迁移已执行，`customer_service_config` 表存在且有 1 条配置记录；因此问题不在路由或迁移。当前更符合前端缓存登录态仍显示账号，但后端已不接受当前 token 时，接口返回非业务 JSON 的 403，页面仅展示加载失败。
- 实现原则：只增加前端统一认证失效处理，不修改登录接口、后端权限、数据库结构或业务接口。
- 范围控制：未修改 `.env` 或秘钥配置；未安装依赖；未删除文件；未修改后端接口、数据库结构、路由或项目架构。

### 实施内容

1. 在 `frontend/src/api/http.ts` 增加 Axios 响应拦截器。
2. 当接口返回 `401`，或返回非业务 JSON 响应体的 `403` 时，判定为登录态失效。
3. 自动清理 `youou_aso_auth` 本地登录缓存，并跳转到登录页。
4. 跳转登录页时保留当前路径为 `redirect`，重新登录后可回到原页面。
5. 保留业务型 `403` 的原行为，避免角色权限不足场景被误判为登录失效。

### 修改文件

1. `frontend/src/api/http.ts`
2. `PROJECT_PROGRESS.md`

### 验证结果

1. `Invoke-WebRequest http://localhost:8080/api/admin/support/customer-service`：未登录请求返回 `403`，确认后端路由已存在。
2. 通过 JDBC 只读检查本地数据库：Flyway V12 `customer service config` 迁移成功，`customer_service_config` 表存在，配置记录数为 1。
3. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
4. 前端构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本轮未处理。

### 下一步任务清单

1. 刷新客服配置页；如果当前 token 已失效，应自动跳转登录页。
2. 重新登录管理员账号后进入“系统管理 / 客服配置”，确认配置可正常加载。
3. 后续可考虑在登录失效跳转前增加统一提示文案，例如“登录已过期，请重新登录”。

### 风险点

1. 当前仅对认证层返回的 `401` 或非业务 JSON `403` 做自动退出；业务接口主动返回的 `FORBIDDEN` 仍交给页面处理，避免误退出。
2. 若后端未来把认证失败也包装成业务 JSON，需要同步调整前端判定逻辑。

## 2026-06-22 更新：综合订单列表操作按钮收敛

- 状态：已完成综合订单列表确认/执行按钮收敛，并通过前端生产构建验证。
- 当前任务：用户指出待确认、待执行订单已经拆成独立页面，其他订单列表不应再显示确认、执行按钮。
- 实现原则：仅调整管理员端订单列表按钮展示规则，不改后端状态流转、不改接口、不改数据库。
- 范围控制：未修改 `.env` 或秘钥配置；未安装依赖；未删除文件；未修改后端接口、数据库结构、路由或项目架构。

### 实施内容

1. 管理员端综合订单列表不再显示“确认”按钮，确认操作保留在独立的待确认订单页。
2. 管理员端综合订单列表不再显示“执行”按钮，执行操作仅在独立的待执行订单页显示。
3. 待执行订单页继续保留单条执行和批量执行能力。
4. 移除综合订单列表中不再使用的确认接口导入和确认函数。

### 修改文件

1. `frontend/src/views/admin/StoreOrdersView.vue`
2. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. 前端构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本轮未处理。

### 下一步任务清单

1. 刷新管理员端苹果订单、谷歌订单、iPad 订单等综合列表，确认不再展示确认/执行按钮。
2. 打开待确认订单页，确认仍可执行确认操作。
3. 打开待执行订单页，确认仍可执行单条执行和批量执行操作。

### 风险点

1. 本轮只收敛前端入口；后端确认/执行接口仍保留，供对应独立业务页面调用。

## 2026-06-22 更新：订单列表内部 ID 改为订单编号

- 状态：已完成订单列表第一列从内部 ID 改为业务订单编号，并通过前端生产构建验证。
- 当前任务：用户要求订单列表中 `ID` 列改为“订单编号”，避免业务页面展示内部数据库 ID。
- 实现原则：只调整前端列表展示字段，不修改接口、不修改数据库、不影响详情、筛选和导出。
- 范围控制：未修改 `.env` 或秘钥配置；未安装依赖；未删除文件；未修改后端接口、数据库结构、路由或项目架构。

### 实施内容

1. 管理员端订单列表第一列从 `id` 改为 `orderNo`，列名使用现有“订单编号”多语言文案。
2. 用户端订单列表第一列从 `id` 改为 `orderNo`，保持与管理员端一致。
3. 订单编号列增加等宽字体和不换行展示，提升长编号的可读性。

### 修改文件

1. `frontend/src/views/admin/StoreOrdersView.vue`
2. `frontend/src/views/user/StoreOrdersView.vue`
3. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. 前端构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本轮未处理。

### 下一步任务清单

1. 刷新管理员端和用户端订单列表，确认第一列显示业务订单编号而不是内部 ID。
2. 如果后续希望待确认订单列表也固定展示订单编号列，可在确认业务人员需要后单独补充。

### 风险点

1. 订单编号较长，窄屏下表格可能需要横向滚动；本轮已使用不换行展示，避免编号断裂导致识别困难。

## 2026-06-22 更新：订单列表应用列信息收敛

- 状态：已完成订单列表应用列展示收敛，并通过前端生产构建验证。
- 当前任务：用户反馈订单列表“应用”列信息过多，应用名称下方同时显示订单号和 App ID/Bundle ID，影响业务阅读。
- 实现原则：只调整列表展示，不删除数据、不修改接口、不影响搜索、详情和导出；订单号仍保留在详情页、导出和内容筛选能力中。
- 范围控制：未修改 `.env` 或秘钥配置；未安装依赖；未删除文件；未修改后端接口、数据库结构、路由或项目架构。

### 实施内容

1. 管理员端订单列表应用列改为只显示应用图标、应用名称和应用商店标签。
2. 管理员端待确认订单列表应用列同步收敛，去掉订单号和 App ID/Bundle ID 的行内展示。
3. 用户端订单列表应用列同步收敛，保持用户端和管理员端视觉规则一致。
4. 移除对应页面中已不再使用的 `.app-meta` 样式，避免残留无效样式。
5. 将应用列最小宽度从 `260` 收敛到 `220`，释放表格横向空间。

### 修改文件

1. `frontend/src/views/admin/StoreOrdersView.vue`
2. `frontend/src/views/admin/PendingConfirmOrdersView.vue`
3. `frontend/src/views/user/StoreOrdersView.vue`
4. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. 前端构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本轮未处理。

### 下一步任务清单

1. 刷新管理员端订单列表和待确认订单列表，确认应用列不再展示订单号和 App ID/Bundle ID。
2. 刷新用户端订单列表，确认应用列同样只展示应用核心信息。
3. 如后续业务人员仍需要在列表快速复制订单号，可单独设计“订单号”列或详情入口中的复制按钮。

### 风险点

1. 本轮只是展示收敛，查询和导出仍保留订单号；如果业务希望彻底隐藏订单号，需要单独确认权限和审计需求。

## 2026-06-22 更新：客服配置与用户充值二维码配置

- 状态：已完成全局客服配置能力，管理员可在系统管理中维护充值客服二维码，用户端点击“充值”时读取该配置并展示客服二维码。
- 当前任务：用户确认客服二维码放到“客服配置”中维护，充值没有在线充值接口，点击充值只弹出客服二维码。
- 实现原则：使用数据库持久化全局客服配置，避免硬编码二维码；仅新增必要的配置表、后端接口和前端页面，不修改 `.env`、秘钥、依赖或无关模块。
- 范围控制：未修改秘钥配置；未安装依赖；未删除文件；数据库仅新增经确认的客服配置表迁移，不改变订单、用户、钱包等既有表结构。

### 实施内容

1. 新增 `customer_service_config` 配置表迁移，固定维护一条全局客服配置，包含客服名称、二维码图片 URL、联系说明、启用状态和更新时间。
2. 新增客服配置后端模块：领域对象、DTO、Repository、Service 和 Controller。
3. 新增用户端只读接口 `GET /api/customer/support/customer-service`，用于充值弹窗读取已启用的客服配置。
4. 新增管理员端读取和保存接口 `GET/PUT /api/admin/support/customer-service`，仅管理员可维护配置。
5. 管理员端系统管理菜单新增“客服配置”页面，支持启用/停用、维护客服名称、二维码图片 URL 和联系说明，并提供用户端预览。
6. 用户端顶部“充值”按钮不再依赖静态二维码，点击后动态读取客服配置并在弹窗中展示二维码、客服名称和联系说明。
7. 补充客服配置相关中英文多语言文案。

### 修改文件

1. `backend/src/main/resources/db/migration/V12__customer_service_config.sql`
2. `backend/src/main/java/com/youou/aso/modules/support/domain/CustomerServiceConfig.java`
3. `backend/src/main/java/com/youou/aso/modules/support/dto/CustomerServiceConfigResult.java`
4. `backend/src/main/java/com/youou/aso/modules/support/dto/UpdateCustomerServiceConfigCommand.java`
5. `backend/src/main/java/com/youou/aso/modules/support/repository/CustomerServiceConfigRepository.java`
6. `backend/src/main/java/com/youou/aso/modules/support/repository/JdbcCustomerServiceConfigRepository.java`
7. `backend/src/main/java/com/youou/aso/modules/support/service/CustomerServiceConfigService.java`
8. `backend/src/main/java/com/youou/aso/modules/support/api/CustomerServiceConfigController.java`
9. `backend/src/test/java/com/youou/aso/modules/support/service/CustomerServiceConfigServiceTest.java`
10. `backend/src/test/java/com/youou/aso/modules/support/api/CustomerServiceConfigControllerTest.java`
11. `frontend/src/api/support.ts`
12. `frontend/src/views/admin/CustomerServiceConfigView.vue`
13. `frontend/src/layouts/AdminLayout.vue`
14. `frontend/src/layouts/UserLayout.vue`
15. `frontend/src/router/index.ts`
16. `frontend/src/i18n/locales/zh-CN.ts`
17. `frontend/src/i18n/locales/en-US.ts`
18. `PROJECT_PROGRESS.md`

### 验证结果

1. `mvn.cmd "-Dtest=CustomerServiceConfigServiceTest,CustomerServiceConfigControllerTest" test`：通过，7 个客服配置相关后端测试全部成功。
2. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
3. 前端构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本轮未处理。
4. 本地后端曾被重启用于让新增迁移和接口生效；用户随后说明后端由其自行重启，后续不再由 Codex 操作服务进程。

### 下一步任务清单

1. 用户自行重启后端，使 Flyway 执行 V12 迁移并加载新增客服配置接口。
2. 管理员端进入“系统管理 / 客服配置”，填写 http/https 二维码图片地址、客服名称和联系说明，并启用配置。
3. 用户端刷新页面后点击右上角“充值”，确认弹窗展示配置后的客服二维码和说明。
4. 如后续需要上传二维码文件而不是填写图片 URL，需要单独设计文件上传、存储、安全校验和访问控制方案。

### 风险点

1. 当前版本支持配置二维码图片 URL，不包含图片上传和对象存储；图片可用性依赖配置的外部或内部静态资源地址。
2. 启用客服配置时后端要求二维码 URL 必须是 `http` 或 `https`，避免保存无效或危险协议。
3. 新增接口和数据库迁移需要后端重启后生效；如果浏览器仍连接旧后端进程，会看不到“客服配置”接口。

## 2026-06-22 更新：系统菜单、价格单位与账户安全提示收敛

- 状态：已完成操作日志菜单移除、价格配置页货币单位展示、账户设置登录安全提示收敛，并通过前端生产构建验证。
- 当前任务：用户要求去掉操作日志菜单；价格配置页面显示货币单位；账户设置的登录安全不再提示账户是否强制修改密码，系统统一不强制。
- 实现原则：仅调整前端菜单、路由入口、页面展示和中英文文案，不改动后端权限、数据库结构、价格接口、登录接口或账号字段。
- 范围控制：未修改 `.env` 或秘钥配置；未安装依赖；未删除文件；未修改后端接口、数据库结构或项目架构。

### 实施内容

1. 从管理员端系统管理菜单中移除“操作日志”菜单项。
2. 移除 `/admin/audit-logs` 前端路由入口，避免继续通过原菜单路径进入操作日志页。
3. 价格配置页每个单价输入框增加 `$` 前缀和 `USD` 后缀，明确价格配置使用美元单位。
4. 用户端和管理员端账户设置页的登录安全卡片不再根据 `forcePasswordChange` 显示强制/不强制状态。
5. 登录安全提示统一改为“系统当前不强制修改密码”，并补充后续如开放密码修改将走独立安全流程。
6. 补充对应中英文多语言文案。

### 修改文件

1. `frontend/src/layouts/AdminLayout.vue`
2. `frontend/src/router/index.ts`
3. `frontend/src/views/admin/PricingView.vue`
4. `frontend/src/views/admin/AccountSettingsView.vue`
5. `frontend/src/views/user/AccountSettingsView.vue`
6. `frontend/src/i18n/locales/zh-CN.ts`
7. `frontend/src/i18n/locales/en-US.ts`
8. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. `rg "admin-audit-logs|/admin/audit-logs|menu\\.auditLogs|passwordStatusTitle|auth\\.forcePasswordChange \\?" frontend/src/views frontend/src/layouts frontend/src/router`：无匹配，确认页面/菜单/路由中不再引用旧操作日志入口和旧强制改密状态提示。
3. 前端构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本轮未处理。

### 下一步任务清单

1. 刷新管理员端系统管理菜单，确认操作日志菜单不再显示。
2. 打开价格配置页，确认每个单价输入框都显示 `$` 和 `USD`。
3. 分别打开用户端和管理员端账户设置页，确认登录安全只显示系统统一不强制修改密码提示。

### 风险点

1. 本轮只是移除前端操作日志入口，没有删除 `AuditLogsView.vue` 文件或后端审计能力；如果后续需要彻底下线操作日志模块，应单独确认删除/权限/审计影响。
2. 后端账号 DTO 仍保留 `forcePasswordChange` 字段用于兼容现有登录结果，本轮仅在账户设置页不展示该状态。

## 2026-06-22 更新：重启本地后端使订单时间修复生效

- 状态：已完成本地后端服务重启，当前 8080 端口已由最新 `youou-aso/backend` 代码启动。
- 当前任务：用户反馈页面中订单确认/执行时间仍然是旧的 UTC 时间，截图中的新订单仍显示确认时间早于创建时间。
- 问题定位：代码和测试已经修复，但当前浏览器连接的 8080 后端仍是修复前启动的 JVM；JVM 不会自动热加载本轮修改后的 class，因此页面仍使用旧逻辑写入时间。
- 实现原则：只重启已确认的本地 Youou-ASO 后端进程，不改动代码逻辑、数据库结构、路由、`.env` 或依赖。
- 范围控制：未修改 `.env` 或秘钥配置；未安装依赖；未删除文件；未修改数据库结构、路由或项目架构。

### 实施内容

1. 读取当前 Java 进程命令行，确认旧后端进程指向 `E:\AppFast\youou-aso\backend\target\classes`。
2. 停止已确认的旧 Youou-ASO 后端 JVM 进程。
3. 使用 `mvn.cmd spring-boot:run -Dspring-boot.run.profiles=local` 从 `E:\AppFast\youou-aso\backend` 启动最新后端代码。
4. 确认 8080 端口已重新监听，并确认新 Java 进程命令行来自当前项目目录。

### 修改文件

1. `PROJECT_PROGRESS.md`

### 验证结果

1. `Get-NetTCPConnection -LocalPort 8080 -State Listen`：确认 8080 端口正在监听。
2. `Get-CimInstance Win32_Process`：确认新后端进程命令行为 `spring-boot:run -Dspring-boot.run.profiles=local`，项目目录为 `E:\AppFast\youou-aso\backend`。

### 下一步任务清单

1. 刷新浏览器后重新创建一笔关键词安装订单，并在管理员端点击确认、执行，确认新写入的确认/执行时间不再少 8 小时。
2. 对已经由旧后端写入的异常订单，如需修复历史显示，需要单独确认是否执行一次性数据校正。

### 风险点

1. 本轮只让新代码在本地服务中生效，不会自动修复旧订单中已经写入数据库的异常确认/执行时间。
2. 后端是通过后台 Maven 进程启动，后续如需关闭服务，需要停止对应 `spring-boot:run` 进程。

## 2026-06-22 更新：关键词安装按执行小时计算完成时间

- 状态：已完成关键词安装订单执行完成时间规则修正，并通过后端订单测试和前端生产构建验证。
- 当前任务：用户确认关键词安装有执行时间，点击执行后应按执行小时计算完成时间，例如执行 1h 就是在点击执行后一小时完成。
- 实现原则：仅调整订单执行逻辑中的关键词安装预计完成时间计算，不改动数据库结构、路由、接口契约或前端页面结构。
- 范围控制：未修改 `.env` 或秘钥配置；未安装依赖；未删除文件；未修改数据库结构、路由或项目架构。

### 实施内容

1. 在 `OrderService.applyExecution()` 中，管理员点击执行关键词安装订单时，将 `expectedCompletedAt` 重置为 `executedAt + executionHours`。
2. 如果历史数据或异常数据缺失执行小时，则按 `1` 小时兜底，避免订单进入执行中后没有预计完成时间。
3. 保留原有到期判断逻辑：预计完成时间已到或已过则立即完成，否则进入执行中，后续由定时任务到期完成。
4. 新增单元测试覆盖关键词安装订单执行 1 小时后完成的预计完成时间规则。

### 修改文件

1. `backend/src/main/java/com/youou/aso/modules/order/service/OrderService.java`
2. `backend/src/test/java/com/youou/aso/modules/order/service/OrderServiceTest.java`
3. `PROJECT_PROGRESS.md`

### 验证结果

1. `mvn.cmd "-Dtest=OrderServiceTest" test`：通过，23 个订单服务测试全部成功。
2. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
3. 前端构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本轮未处理。

### 下一步任务清单

1. 重启后端服务后，新建关键词安装订单并选择执行小时，管理员点击执行后确认订单详情中的预计完成时间为执行时间加对应小时数。
2. 等待定时任务触发或手动调用完成检查，确认到期后订单自动从执行中变为已完成。
3. 如其他订单类型也需要按“点击执行后持续时长”计算完成时间，需要单独确认每类订单的执行时长字段和规则。

### 风险点

1. 历史关键词安装订单如果已经处于执行中且预计完成时间按旧规则写入，本轮不会自动批量重算。
2. 当前兜底执行小时为 1 小时，仅用于处理历史或异常空值；正常新订单仍由前端和后端校验限制在 1 到 8 小时。

## 2026-06-22 更新：订单确认与执行时间时区修正

- 状态：已完成订单确认、执行、完成以及特殊订单审核相关业务时间的时区修正，并通过后端订单测试和前端生产构建验证。
- 当前任务：用户截图指出订单详情中确认时间早于创建时间，流程时间顺序明显异常。
- 问题定位：订单创建时间由 MySQL `CURRENT_TIMESTAMP` 写入，当前本地连接配置为 `serverTimezone=Asia/Shanghai`；但订单服务和特殊订单审核服务的 `now()` 使用 UTC 转 `LocalDateTime`，导致确认、执行、完成、审核时间比数据库创建时间少 8 小时。
- 实现原则：统一订单业务时间为 `Asia/Shanghai`，与数据库连接时区保持一致；不修改数据库结构、路由、接口契约或前端展示结构。
- 范围控制：未修改 `.env` 或秘钥配置；未安装依赖；未删除文件；未修改数据库结构、路由或项目架构。

### 实施内容

1. `OrderService` 新增业务时区常量 `Asia/Shanghai`，`now()` 改为按业务时区生成 `LocalDateTime`。
2. `SpecialOrderAuditService` 新增业务时区常量 `Asia/Shanghai`，审核、提交、管理员提交正式订单等时间统一按业务时区生成。
3. 特殊订单用户提交正式订单时的订单日期也从 UTC 日期改为业务时区日期，避免接近午夜时跨天。
4. 更新 `OrderServiceTest` 和 `SpecialOrderAuditServiceTest` 中的时间断言，固定 Clock 仍使用 UTC instant，但预期业务时间按 `Asia/Shanghai` 转换。

### 修改文件

1. `backend/src/main/java/com/youou/aso/modules/order/service/OrderService.java`
2. `backend/src/main/java/com/youou/aso/modules/order/service/SpecialOrderAuditService.java`
3. `backend/src/test/java/com/youou/aso/modules/order/service/OrderServiceTest.java`
4. `backend/src/test/java/com/youou/aso/modules/order/service/SpecialOrderAuditServiceTest.java`
5. `PROJECT_PROGRESS.md`

### 验证结果

1. `mvn.cmd "-Dtest=OrderServiceTest,SpecialOrderAuditServiceTest" test`：通过，27 个订单相关单元测试全部成功。
2. 首次运行测试时发现 `SpecialOrderAuditService` 仍有一处 UTC 日期转换残留导致编译失败，已修复为业务时区后重跑通过。
3. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
4. 前端构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本轮未处理。

### 下一步任务清单

1. 重启后端服务后新建一笔订单，执行确认和执行操作，确认订单详情中的创建、确认、执行时间按真实流程递增。
2. 对截图中已经写入的历史异常订单，如需修复显示，需要单独确认是否做一次性数据校正脚本，避免误改审计数据。
3. 后续如部署到非上海时区服务器，仍应保持 JDBC `serverTimezone` 和业务时区配置一致。

### 风险点

1. 本轮修复只影响后续新写入或更新的订单时间，历史已写错的确认/执行/完成时间不会自动回补。
2. 当前业务时区硬编码为 `Asia/Shanghai`，符合当前数据库连接配置；如果未来支持多时区，需要抽取为配置项并统一所有模块时间策略。

## 2026-06-22 更新：用户管理页余额与登录列交界留白修正

- 状态：已完成用户管理页“可用余额”和“最后登录”列交界留白修正，并通过前端生产构建验证。
- 当前任务：用户截图反馈两列仍然贴在一起，前一次仅调整文本缩进不足以解决表头和单元格交界处的视觉拥挤。
- 实现原则：仅调整管理员端用户管理表格局部列 class、列宽和 Element Plus 单元格内边距，不改动业务字段、接口、数据结构、路由或权限逻辑。
- 范围控制：未修改 `.env` 或秘钥配置；未安装依赖；未删除文件；未修改后端接口、数据库结构、路由或项目架构。

### 实施内容

1. 为可用余额列增加专用 `balance-column` class，并应用到表头和内容单元格。
2. 为最后登录列增加专用 `date-column` class，并应用到表头和内容单元格。
3. 对余额列 `.cell` 增加右侧内边距，对最后登录列 `.cell` 增加左侧内边距，从列容器层面制造稳定留白。
4. 将最后登录列宽调整为 `230`，确保日期时间或空值占位都不会紧贴余额列。

### 修改文件

1. `frontend/src/views/admin/CustomerManagementView.vue`
2. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. 构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本轮未处理。

### 下一步任务清单

1. 刷新管理员端用户管理页，确认可用余额列和最后登录列之间的表头、金额、空值占位都有稳定间距。
2. 如仍需更强分隔，可后续考虑在两列之间增加轻量竖向分隔线，但当前先保持表格视觉克制。

### 风险点

1. 本轮仍是纯展示调整；如果后续 Element Plus 升级改变内部 `.cell` 结构，相关深层样式可能需要同步复核。

## 2026-06-22 更新：用户管理页余额与登录列间距微调

- 状态：已完成用户管理页“可用余额”和“最后登录”列的视觉间距微调，并通过前端生产构建验证。
- 当前任务：用户指出可用余额和最后登录两列视觉上贴得太近。
- 实现原则：仅调整管理员端用户管理表格局部列宽和单元格内边距，不改动业务字段、接口、数据结构、路由或权限逻辑。
- 范围控制：未修改 `.env` 或秘钥配置；未安装依赖；未删除文件；未修改后端接口、数据库结构、路由或项目架构。

### 实施内容

1. 将可用余额列宽从 `150` 收敛到 `140`，保留金额右对齐和等宽数字显示。
2. 将最后登录列宽从 `190` 调整为 `205`，给日期时间留出更稳定的阅读空间。
3. 为最后登录单元格增加轻量左侧内缩，使其与右对齐金额之间形成更明确的视觉分隔。

### 修改文件

1. `frontend/src/views/admin/CustomerManagementView.vue`
2. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. 构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本轮未处理。

### 下一步任务清单

1. 刷新管理员端用户管理页，确认可用余额和最后登录之间的间距更自然。
2. 如仍觉得表格横向节奏不均，可继续按真实浏览器截图微调客户列弹性宽度和日期列间距。

### 风险点

1. 本轮为纯展示微调，窄屏下仍依赖 Element Plus 表格横向滚动承载固定列宽。

## 2026-06-22 更新：用户管理页表格列比例优化

- 状态：已完成用户管理页表格列宽比例优化，并通过前端生产构建验证。
- 当前任务：用户截图指出用户管理表格整体列比例不够美观，部分列挤在一起，部分列间距过大。
- 实现原则：仅调整管理员端用户管理页表格展示比例，不改动业务字段、接口、数据结构、路由或权限逻辑。
- 范围控制：未修改 `.env` 或秘钥配置；未安装依赖；未删除文件；未修改后端接口、数据库结构、路由或项目架构。

### 实施内容

1. 将客户列最小宽度调整为更适合用户名和邮箱展示的比例，由客户信息承接主要可变空间。
2. 收敛账户状态、可用余额、最后登录、创建时间、操作列宽，避免宽屏下列与列之间被拉得过散。
3. 取消操作列右侧固定定位，让操作列参与正常表格排布，减少按钮漂在页面最右侧的割裂感。

### 修改文件

1. `frontend/src/views/admin/CustomerManagementView.vue`
2. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. 构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本轮未处理。

### 下一步任务清单

1. 刷新管理员端用户管理页，确认客户、状态、余额、时间和操作列的横向节奏更均衡。
2. 如后续仍觉得宽屏下表格过长，可继续评估为管理后台列表页增加统一的最大阅读宽度或表格密度规范。

### 风险点

1. 本轮只调整列宽参数；不同屏幕宽度下仍依赖 Element Plus 表格自身布局算法，极窄屏幕可能仍需要横向滚动。

## 2026-06-22 更新：用户管理页移除冻结余额展示

- 状态：已完成用户管理页冻结余额列移除，并通过前端生产构建验证。
- 当前任务：用户确认系统当前没有冻结金额业务场景，用户管理页不应继续展示冻结余额。
- 实现原则：仅收敛管理员端用户管理页展示字段，不改动后端字段、接口返回、钱包流水、数据库结构或权限逻辑，避免影响已有兼容性。
- 范围控制：未修改 `.env` 或秘钥配置；未安装依赖；未删除文件；未修改后端接口、数据库结构、路由或项目架构。

### 实施内容

1. 移除管理员端用户管理表格中的“冻结余额”列。
2. 用户管理页保留客户、账户状态、可用余额、最后登录、创建时间和操作列，页面信息更贴合当前业务场景。
3. 保留前端类型和后端接口中的 `frozenBalance` 字段，不在本轮做系统级删除，避免引入数据兼容风险。

### 修改文件

1. `frontend/src/views/admin/CustomerManagementView.vue`
2. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. 构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本轮未处理。

### 下一步任务清单

1. 刷新管理员端用户管理页，确认表格不再展示冻结余额，列宽和内容密度更自然。
2. 如确认整个系统都不再需要冻结金额字段，可单独评估后端 DTO、钱包模型、数据库字段和历史数据兼容性的清理方案。

### 风险点

1. 本轮仅移除页面展示，后端仍保留 `frozenBalance` 字段；这是为了避免小需求牵动数据库和接口兼容性。
2. 如果后续完全删除冻结金额能力，需要先确认没有历史数据、导出字段、接口调用方或财务审计依赖该字段。

## 2026-06-22 更新：用户管理页内容留白收紧

- 状态：已完成用户管理页左右与顶部内容留白收紧，并通过前端生产构建验证。
- 当前任务：用户截图指出用户管理页内容区两侧间距过大。
- 实现原则：仅调整用户管理页局部布局间距，不影响其他页面和业务逻辑。
- 范围控制：未修改 `.env` 或秘钥配置；未安装依赖；未删除文件；未修改后端接口、数据库结构、路由或项目架构。

### 实施内容

1. 将用户管理页外层 padding 从 `18px 24px 28px` 收紧为 `12px 16px 24px`。
2. 将移动端外层 padding 从 `12px` 收紧为 `10px`。
3. 概览卡片横向间距从 `14px` 收紧为 `12px`，概览区和表格间距从 `16px` 收紧为 `14px`。
4. 概览卡片垂直内边距略收紧，减少首屏空白感。

### 修改文件

1. `frontend/src/views/admin/CustomerManagementView.vue`
2. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. 构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本轮未处理。

### 下一步任务清单

1. 刷新管理员端用户管理页，确认左侧、右侧和顶部留白更贴近顶部标题与内容区。
2. 如仍感觉宽，可继续统一后台列表页内容边距规范，避免单页样式不一致。

### 风险点

1. 本轮只调整用户管理页，其他后台列表页仍可能存在不同边距，需要后续按页面逐一校准或抽取统一布局规范。

## 2026-06-22 更新：用户管理页双状态收敛与视觉优化

- 状态：已完成管理员端用户管理页状态收敛和页面视觉优化，并通过前端生产构建验证。
- 当前任务：用户指出用户管理页面粗糙，并确认账户状态只需要“启用/禁用”，不需要“锁定”。
- 实现原则：前端业务界面仅暴露启用和禁用两个状态；如果后端历史数据仍返回 `LOCKED`，前端按禁用展示和处理；不修改后端枚举、数据库结构或权限逻辑。
- 范围控制：未修改 `.env` 或秘钥配置；未安装依赖；未删除文件；未修改后端接口、数据库结构、路由或项目架构。

### 实施内容

1. 用户管理页新增概览区：客户总数、启用账户、禁用账户、可用余额合计。
2. 表格第一列从内部用户 ID 改为“客户”识别结构，展示头像圆标、用户名和邮箱。
3. 用户 ID 不再作为首要业务列展示。
4. 账户状态在界面上收敛为启用/禁用，`LOCKED` 历史状态统一按禁用展示。
5. 操作列只保留启用/禁用切换，不再显示锁定按钮。
6. 优化表格行高、hover、客户信息排版、概览卡片和响应式布局。
7. 中英文文案去掉“锁定”描述，并补充概览区文案。

### 修改文件

1. `frontend/src/views/admin/CustomerManagementView.vue`
2. `frontend/src/i18n/locales/zh-CN.ts`
3. `frontend/src/i18n/locales/en-US.ts`
4. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. 构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本轮未处理。

### 下一步任务清单

1. 刷新管理员端用户管理页，确认概览区、客户列、状态标签和操作按钮展示正常。
2. 对启用账户点击禁用、对禁用账户点击启用，确认状态变更流程正常。
3. 如后续需要客户详情页，再单独设计客户资产、订单、流水的聚合视图。

### 风险点

1. 本轮仅在前端隐藏并映射 `LOCKED` 状态，后端仍保留该枚举以兼容历史数据；如果希望彻底移除锁定状态，需要另行确认后端和数据库数据迁移。

## 2026-06-22 更新：订单列表应用列精简与订单详情页

- 状态：已完成订单列表应用列精简、用户端/管理员端订单详情页和详情查询接口，并通过前后端验证。
- 当前任务：用户指出应用列不应继续显示地区，因为应用和地区已经解耦；同时要求系统提供订单详情页面。
- 实现原则：列表只保留高频浏览信息，完整订单参数进入详情页；后端新增只读详情接口并校验用户订单归属；不修改数据库结构和订单状态流转。
- 范围控制：未修改 `.env` 或秘钥配置；未安装依赖；未删除文件；未修改数据库结构、核心订单流程或项目架构。

### 实施内容

1. 后端新增用户端订单详情接口 `GET /api/customer/orders/{id}`，普通用户只能查看自己的订单。
2. 后端新增管理员端订单详情接口 `GET /api/admin/orders/{id}`，管理员可查看订单详情。
3. 详情接口复用现有 `OrderResult`，包含订单基础字段和 `items` 明细，不新增数据库表。
4. `OrderService` 新增用户/管理员详情查询方法，并在返回前附加订单明细。
5. 前端订单 API 新增 `getCustomerOrder()` 和 `getAdminOrder()`。
6. 用户端新增 `/user/orders/:id` 订单详情页。
7. 管理员端新增 `/admin/orders/:id` 订单详情页。
8. 用户端订单列表、管理员订单列表、待确认订单列表增加“详情”入口。
9. 订单列表应用列不再显示地区标签，包括 `MULTI`。
10. 管理员订单列表应用列不再显示 `用户 {id}`，避免把客户内部标识挤在应用信息里；客户信息先在详情页展示。
11. 补充订单详情页中英文文案。

### 修改文件

1. `backend/src/main/java/com/youou/aso/modules/order/service/OrderService.java`
2. `backend/src/main/java/com/youou/aso/modules/order/api/CustomerOrderController.java`
3. `backend/src/main/java/com/youou/aso/modules/order/api/AdminOrderController.java`
4. `backend/src/test/java/com/youou/aso/modules/order/service/OrderServiceTest.java`
5. `backend/src/test/java/com/youou/aso/modules/order/api/AdminOrderControllerTest.java`
6. `frontend/src/api/orders.ts`
7. `frontend/src/router/index.ts`
8. `frontend/src/views/user/OrderDetailView.vue`
9. `frontend/src/views/admin/OrderDetailView.vue`
10. `frontend/src/views/user/StoreOrdersView.vue`
11. `frontend/src/views/admin/StoreOrdersView.vue`
12. `frontend/src/views/admin/PendingConfirmOrdersView.vue`
13. `frontend/src/i18n/locales/zh-CN.ts`
14. `frontend/src/i18n/locales/en-US.ts`
15. `PROJECT_PROGRESS.md`

### 验证结果

1. `mvn.cmd "-Dtest=OrderServiceTest,AdminOrderControllerTest" test`：通过，27 个订单相关测试全部成功。
2. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
3. 首次执行未加引号的 Maven 命令时，PowerShell 将逗号解析为参数分隔导致命令解析失败；已使用带引号命令重新验证通过。
4. 前端构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本轮未处理。

### 下一步任务清单

1. 在用户端订单列表点击“详情”，确认可进入 `/user/orders/:id` 并展示订单基础信息、费用、明细和执行记录。
2. 在管理员端订单列表和待确认订单列表点击“详情”，确认可进入 `/admin/orders/:id`。
3. 用普通用户尝试访问其他用户订单详情，确认后端返回订单不存在或禁止访问。
4. 后续如需管理员端详情页显示客户用户名和邮箱，应在订单详情结果中补充客户展示字段，避免继续使用内部用户 ID。

### 风险点

1. 管理员详情页当前客户信息仍只能显示内部客户 ID，因为现有订单结果未包含用户名和邮箱；后续可扩展 DTO 或关联客户表查询。
2. 订单详情页首版复用现有订单明细字段，复杂特殊订单的协商内容仍主要在特殊审核模块中展示，后续可按业务需要补充来源审核信息。

## 2026-06-22 更新：订单筛选地区名称多语言显示修正

- 状态：已完成订单列表地区筛选下拉的名称显示修正，并通过前端生产构建验证。
- 当前任务：用户指出中文环境下地区下拉只显示 `AE/AR/AT` 等代码不合理，英文环境也不应只显示代码。
- 实现原则：复用后端地区接口已返回的 `nameZh/nameEn`，按当前语言显示可读地区名称；地区筛选提交值仍保持原有 `code`，不改变查询接口和业务逻辑。
- 范围控制：未修改 `.env` 或秘钥配置；未安装依赖；未删除文件；未修改后端接口、数据库结构、路由或权限逻辑。

### 实施内容

1. 管理员订单列表、用户订单列表、待确认订单列表的地区筛选选项从纯地区代码改为 `MarketRegion` 对象。
2. 中文环境显示 `nameZh`，英文环境显示 `nameEn`。
3. 地区下拉项改为“国旗 + 地区名称 + 地区代码”的主次结构，代码作为辅助信息保留。
4. 地区下拉排序改为按当前语言下的地区名称排序，避免只按代码排序造成业务人员识别困难。
5. 保持 `el-option` 的提交值为地区 `code`，订单查询参数不变。

### 修改文件

1. `frontend/src/views/admin/StoreOrdersView.vue`
2. `frontend/src/views/user/StoreOrdersView.vue`
3. `frontend/src/views/admin/PendingConfirmOrdersView.vue`
4. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. 构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本轮未处理。

### 下一步任务清单

1. 在中文环境展开订单列表地区筛选，确认显示为“国旗 + 中文地区名称 + 代码”。
2. 切换英文环境后再次展开地区筛选，确认显示为“flag + English region name + code”。
3. 选择地区后执行搜索，确认接口仍按地区代码筛选订单。

### 风险点

1. 地区名称依赖后端地区配置中的 `nameZh/nameEn`，如果某些地区名称为空，会影响显示质量；当前代码保持接口数据直出，未新增前端兜底字典。
2. 国旗仍使用 `flagcdn.com` 远程图片，网络异常时图标可能不显示，但地区名称和筛选功能不受影响。

## 2026-06-22 更新：订单筛选应用、商店与地区下拉视觉优化

- 状态：已完成前端订单列表筛选下拉的展示优化，并通过前端生产构建验证。
- 当前任务：用户反馈订单查询筛选中“应用列表要显示图标和名字”，随后补充“应用商店列表也要显示图标”“还有地区”。
- 实现原则：仅优化订单列表筛选下拉的展示层，不改变筛选字段、接口参数、订单查询逻辑、路由、数据库结构或权限逻辑。
- 范围控制：未修改 `.env` 或秘钥配置；未安装依赖；未删除文件；未修改后端接口、数据库结构或项目架构。

### 实施内容

1. 管理员订单列表、用户订单列表、待确认订单列表的应用筛选下拉改为“应用图标 + 应用名称 + Bundle ID/App ID”结构。
2. 应用图标优先使用 `appIconUrl`，没有图标时使用应用名称首字母作为兜底。
3. 应用商店筛选下拉改为“商店图标 + 商店名称”结构，分别区分 App Store、Google Play、iPad Store 和全部。
4. 地区筛选下拉改为“国旗图标 + 国家/地区代码”结构，全部地区保留统一的 ALL 图标。
5. 为三个筛选下拉增加专用 `popper-class`，优化下拉项高度、内边距、圆角、hover 状态和文本截断，避免下拉展开后显得粗糙拥挤。

### 修改文件

1. `frontend/src/views/admin/StoreOrdersView.vue`
2. `frontend/src/views/user/StoreOrdersView.vue`
3. `frontend/src/views/admin/PendingConfirmOrdersView.vue`
4. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. 构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本轮未处理。

### 下一步任务清单

1. 在浏览器中分别打开管理员订单列表、用户订单列表、待确认订单列表，展开应用、应用商店、地区筛选下拉，确认图标、文字、hover 和选中状态符合预期。
2. 如果后续需要显示完整国家/地区名称，可在地区接口或前端映射中补充名称字段，再从当前“国旗 + 代码”升级为“国旗 + 名称 + 代码”。

### 风险点

1. 地区国旗使用 `flagcdn.com` 远程图片，网络异常时国旗可能无法显示，但不影响筛选功能。
2. 当前地区数据只有代码，未新增国家/地区名称映射，因此先按现有数据展示代码。

## 2026-06-22 更新：系统金额显示补充美元单位

- 状态：已完成前端主要金额展示补充美元 `$` 单位，并通过前端生产构建验证。
- 当前任务：用户指出系统金额需要有货币单位。
- 实现原则：延续当前用户端顶部余额已使用 `$` 的展示习惯，统一将界面金额显示为美元格式；不改变后端金额字段、计算逻辑和导出原始数值。
- 范围控制：仅修改前端金额格式化函数、中英文金额标签和进度记录；未修改 `.env` 或秘钥配置；未安装依赖；未删除文件；未修改后端接口、数据库结构、路由或计价规则。

### 实施内容

1. 用户端收支明细、首页看板金额显示增加 `$` 前缀。
2. 管理员端首页看板、客户管理余额、财务流水金额显示增加 `$` 前缀。
3. 用户端和管理员端创建订单页预估金额、单价、费用明细显示增加 `$` 前缀。
4. 管理员端待确认订单金额显示增加 `$` 前缀。
5. 管理员端特殊审核金额、用户端特殊需求支付金额显示增加 `$` 前缀。
6. 中英文文案中“金额”“充值金额”“订单金额”“预估金额”“单价”等标签补充 `$` 单位。
7. CSV 导出仍保留原始数值，避免影响后续表格导入或二次分析。

### 修改文件

1. `frontend/src/views/user/TransactionsView.vue`
2. `frontend/src/views/user/DashboardView.vue`
3. `frontend/src/views/user/OrderCreateView.vue`
4. `frontend/src/views/user/PromotionServicesView.vue`
5. `frontend/src/views/admin/DashboardView.vue`
6. `frontend/src/views/admin/CustomerManagementView.vue`
7. `frontend/src/views/admin/FinanceManagementView.vue`
8. `frontend/src/views/admin/OrderCreateView.vue`
9. `frontend/src/views/admin/PendingConfirmOrdersView.vue`
10. `frontend/src/views/admin/AuditManagementView.vue`
11. `frontend/src/i18n/locales/zh-CN.ts`
12. `frontend/src/i18n/locales/en-US.ts`
13. `PROJECT_PROGRESS.md`

### 验证结果

1. `rg -n "function formatMoney|function money|toLocaleString|toFixed\\(2\\)" frontend/src/views frontend/src/layouts`：确认主要金额格式化函数已返回 `$...`。
2. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
3. 前端构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本轮未处理。

### 下一步任务清单

1. 刷新财务流水、充值记录、订单创建、客户管理、首页看板，确认金额展示带 `$`。
2. 后续如需支持人民币或多币种，应新增全局货币配置和统一格式化工具，再迁移当前硬编码 `$` 展示。

### 风险点

1. 本轮统一按美元展示，未引入多币种配置。
2. 部分 CSV 导出仍输出原始数值，不带 `$`，这是为了保留机器可处理性。

## 2026-06-22 更新：财务客户下拉选项视觉优化

- 状态：已完成管理员端财务页面客户下拉框弹层样式优化，并通过前端生产构建验证。
- 当前任务：用户截图指出客户下拉框内用户名和邮箱排版粗糙、拥挤。
- 实现原则：保持现有客户选择逻辑不变，仅优化下拉弹层选项高度、间距、hover 和选中态；避免影响其他页面的 Element Plus 下拉框。
- 范围控制：仅修改管理员端财务页面局部模板和样式、进度记录；未修改 `.env` 或秘钥配置；未安装依赖；未删除文件；未修改后端接口、路由、数据库结构或项目架构。

### 实施内容

1. 为财务页面两个客户下拉框增加专用 `popper-class`。
2. 固定客户选项最小高度，避免用户名和邮箱上下挤压。
3. 用户名和邮箱分为主次两行显示，用户名加粗，邮箱使用浅灰小字号。
4. 优化下拉弹层内边距、选项圆角、hover 背景和选中背景。
5. 保持筛选客户和充值客户选择逻辑不变。

### 修改文件

1. `frontend/src/views/admin/FinanceManagementView.vue`
2. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. 前端构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本轮未处理。

### 下一步任务清单

1. 刷新管理员端充值记录页，展开客户下拉框，确认每个客户选项上下间距正常。
2. 在财务流水和充值弹窗中分别展开客户下拉框，确认样式一致。

### 风险点

1. 本轮通过 `popper-class` 局部作用到财务客户下拉，不影响其他页面；如果后续多个页面都需要类似客户选择器，可抽成统一组件。

## 2026-06-22 更新：财务页面客户识别与业务化文案优化

- 状态：已完成管理员端财务流水和充值记录页面的客户识别方式优化，并通过前端生产构建验证。
- 当前任务：用户指出财务管理两个页面粗糙、描述偏技术化，且“用户ID”对业务人员没有识别价值。
- 实现原则：界面面向业务人员展示“客户账号/邮箱”；内部仍使用 `customerId` 调用现有接口；不修改数据库结构和后端接口。
- 范围控制：仅修改管理员端财务页面、前端客户列表复用逻辑、中英文文案和进度记录；未修改 `.env` 或秘钥配置；未安装依赖；未删除文件；未修改路由、后端接口、数据库结构或项目架构。

### 实施内容

1. 财务流水页说明文案改为业务表达：用于核对客户余额变动、订单扣款、退款和人工调整记录。
2. 充值记录页说明文案改为业务表达：用于为客户登记线下充值并查看充值到账记录。
3. 财务页面加载现有客户列表，复用客户账号和邮箱作为识别信息。
4. 筛选条件从“用户ID”输入框改为“客户”下拉选择，支持按客户账号或邮箱检索。
5. 流水表格从显示用户ID改为显示客户用户名和邮箱。
6. 充值弹窗中的客户字段从用户ID输入框改为客户下拉选择。
7. 保留内部 `customerId` 作为接口查询和充值提交参数，避免影响现有后端逻辑。

### 修改文件

1. `frontend/src/views/admin/FinanceManagementView.vue`
2. `frontend/src/i18n/locales/zh-CN.ts`
3. `frontend/src/i18n/locales/en-US.ts`
4. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. `rg` 检查确认财务页面界面层不再以“用户ID”作为筛选项或表格列展示；`customerId` 仅保留为脚本内部接口参数。
3. 前端构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本轮未处理。

### 下一步任务清单

1. 刷新管理员端“财务管理 / 财务流水”，确认筛选项显示为客户下拉，表格显示用户名和邮箱。
2. 刷新管理员端“财务管理 / 充值记录”，确认充值弹窗可选择客户账号或邮箱。
3. 如后续客户量较大，应将客户下拉改为远程搜索，避免一次性加载全部客户。

### 风险点

1. 当前复用现有 `/api/admin/customers` 客户列表接口；客户数量增长后可能需要改为远程搜索分页。
2. 本轮未修改后端钱包流水返回结构，因此流水接口本身仍只返回 `customerId`，客户名和邮箱由前端客户列表映射。

## 2026-06-22 更新：管理员端充值按钮归属修正

- 状态：已修正管理员端财务管理下充值按钮显示位置，并通过前端生产构建验证。
- 当前任务：用户指出充值记录中需要充值却没有，财务流水中不需要却有。
- 实现原则：充值操作归属于“充值记录”页面；“财务流水”仅作为全量流水查询页，不提供充值入口。
- 范围控制：仅修改管理员端财务页面按钮显示条件和进度记录；未修改 `.env` 或秘钥配置；未安装依赖；未删除文件；未修改后端接口、路由、数据库结构或项目架构。

### 实施内容

1. 将管理员端财务页面充值按钮显示条件从 `!isRechargeRecordsPage` 改为 `isRechargeRecordsPage`。
2. `/admin/finance` 财务流水页不再显示充值按钮。
3. `/admin/finance/recharges` 充值记录页显示充值按钮，并继续复用原有充值弹窗和提交接口。

### 修改文件

1. `frontend/src/views/admin/FinanceManagementView.vue`
2. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. 前端构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本轮未处理。

### 下一步任务清单

1. 刷新管理员端“财务管理 / 财务流水”，确认不显示充值按钮。
2. 刷新管理员端“财务管理 / 充值记录”，确认显示充值按钮且点击可打开充值弹窗。

### 风险点

1. 本轮只调整按钮显示位置，没有修改充值接口权限和充值记录查询逻辑。

## 2026-06-22 更新：重复标题页面去重与首屏内容重排

- 状态：已完成用户端和管理员端业务页面重复标题去重，并通过前端生产构建验证。
- 当前任务：用户要求将顶部栏标题与页面内部重复标题的页面重新设计，避免类似“订单执行 / 已完成订单”下方又出现同级标题的粗糙观感。
- 实现原则：顶部栏作为唯一页面定位区；页面内容区不再重复同级 `h1`；保留有业务意义的区块标题（如“基本信息”“登录安全”“费用明细”）；列表页优先展示说明、筛选、操作和表格内容。
- 范围控制：仅修改前端页面模板和局部样式；未修改 `.env` 或秘钥配置；未安装依赖；未删除文件；未修改路由、后端接口、数据库结构或项目架构。

### 实施内容

1. 用户端首页移除内部欢迎大标题，改为轻量说明与刷新操作，指标卡片成为首屏主体。
2. 用户端推广服务移除重复页面标题，服务卡片直接作为首屏主体。
3. 用户端收支明细移除重复标题，仅保留说明、刷新按钮、余额概览与流水表格。
4. 用户端账户设置移除重复标题区，账号资料卡片直接作为首屏主体。
5. 管理员端首页移除内部后台首页大标题，保留说明、刷新、指标卡片和待办内容。
6. 管理员端推广服务移除重复标题，保留页面说明与服务入口卡片。
7. 管理员端审核管理移除重复标题，保留审核说明、刷新按钮和审核表格。
8. 管理员端财务流水/充值记录移除重复标题，保留说明、操作按钮、筛选和流水表格。
9. 管理员端价格配置、地区配置、管理员管理移除重复标题，保留说明、操作按钮和主要内容。
10. 管理员端账户设置移除重复标题区，账号资料与设置卡片直接作为首屏主体。
11. 管理员端操作日志、站点配置占位页改为说明卡片，避免只有重复标题和一句话。

### 修改文件

1. `frontend/src/views/user/DashboardView.vue`
2. `frontend/src/views/user/PromotionServicesView.vue`
3. `frontend/src/views/user/TransactionsView.vue`
4. `frontend/src/views/user/AccountSettingsView.vue`
5. `frontend/src/views/admin/DashboardView.vue`
6. `frontend/src/views/admin/PromotionServicesView.vue`
7. `frontend/src/views/admin/AuditManagementView.vue`
8. `frontend/src/views/admin/FinanceManagementView.vue`
9. `frontend/src/views/admin/PricingView.vue`
10. `frontend/src/views/admin/RegionView.vue`
11. `frontend/src/views/admin/AdminAccountsView.vue`
12. `frontend/src/views/admin/AccountSettingsView.vue`
13. `frontend/src/views/admin/AuditLogsView.vue`
14. `frontend/src/views/admin/SiteConfigView.vue`
15. `PROJECT_PROGRESS.md`

### 验证结果

1. `rg -n "<h1|\\.eyebrow|\\.subtitle|page-title" frontend/src/views/admin frontend/src/views/user`：用户端和管理员端业务页面已无内部重复 `h1`，剩余匹配仅为说明类 `page-note`。
2. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
3. 前端构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本轮未处理。

### 下一步任务清单

1. 在浏览器刷新用户端首页、推广服务、收支明细、账户设置，确认首屏不再出现与顶部栏重复的大标题。
2. 在浏览器刷新管理员端首页、审核管理、财务管理、价格配置、地区配置、管理员管理、账户设置、操作日志、站点配置，确认标题层级更清爽。
3. 后续如需要更进一步提升视觉质量，可继续统一 `page-note`、筛选栏和表格卡片的组件化样式。

### 风险点

1. 本轮未引入公共组件，避免扩大改动；因此多个页面仍各自维护相似的 `page-note` 样式。
2. 本轮仅处理用户端和管理员端业务页面，公开首页、登录、注册、忘记密码等公共页面保留自身营销/认证标题。

## 2026-06-22 更新：顶部栏层级标题视觉优化

- 状态：已完成用户端和管理员端顶部栏层级标题视觉优化，并通过前端生产构建验证。
- 当前任务：用户截图反馈“订单执行 / 已完成订单”这种纯文本层级标题样式不好。
- 实现原则：保留“一级菜单 / 二级菜单”的信息层级，但改为结构化渲染，分别控制父级、分隔符和当前页视觉权重；一级菜单页面仍只显示单个当前标题。
- 范围控制：仅修改用户端和管理员端布局顶部栏标题结构、样式及进度记录；未修改 `.env` 或秘钥配置；未安装依赖；未删除文件；未修改菜单、路由路径、后端接口、数据库结构或项目架构。

### 实施内容

1. 将顶部栏标题从单一字符串改为结构化标题对象：`parent` 和 `current`。
2. 二级菜单页面渲染为父级、轻量分隔符和当前页三段内容。
3. 父级菜单标题使用较浅灰色和中等字重，降低视觉噪音。
4. 当前页面标题保持深色加粗，强化当前页面定位。
5. 分隔符使用浅灰色并单独控制间距，避免黑色斜杠显得生硬。
6. 用户端与管理员端采用相同的标题规则和视觉样式。

### 修改文件

1. `frontend/src/layouts/UserLayout.vue`
2. `frontend/src/layouts/AdminLayout.vue`
3. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. 前端构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本轮未处理。

### 下一步任务清单

1. 刷新管理员端“已完成订单”，确认顶部栏父级“订单执行”变浅，当前页“已完成订单”更突出。
2. 检查用户端订单二级菜单，确认同样显示为更轻量的层级标题。
3. 如果后续仍觉得标题区过重，可继续把父级标题改为可点击式面包屑，或在顶部栏增加更细的 breadcrumb 行。

### 风险点

1. 本轮仅优化标题视觉表现，不改变标题语义和页面路由。
2. 结构化标题依赖 `topTitleKey`，新增二级菜单仍需正确配置该 meta。

## 2026-06-22 更新：顶部栏二级菜单显示层级路径

- 状态：已完成顶部栏标题规则纠偏，并通过前端生产构建验证。
- 当前任务：用户指出二级菜单不应只显示一级菜单；一级菜单页面显示一级菜单，例如“首页”，二级菜单页面显示“一级菜单 / 二级菜单”，例如“订单 / 苹果订单”。
- 实现原则：继续复用路由 meta `topTitleKey`；没有 `topTitleKey` 的页面按原有一级标题显示；存在 `topTitleKey` 的页面拼接一级标题和当前页面标题；如果两者相同则避免重复。
- 范围控制：仅修改用户端和管理员端布局标题计算逻辑及进度记录；未修改 `.env` 或秘钥配置；未安装依赖；未删除文件；未修改路由路径、后端接口、数据库结构或项目架构。

### 实施内容

1. 用户端顶部栏标题规则改为：一级页面显示 `titleKey`，二级页面显示 `topTitleKey / titleKey`。
2. 管理员端顶部栏标题规则同步改为：一级页面显示 `titleKey`，二级页面显示 `topTitleKey / titleKey`。
3. 增加重复保护：如果一级标题和当前标题一致，只显示一次。
4. 保持现有菜单高亮、路由 meta 和页面内部内容不变。

### 修改文件

1. `frontend/src/layouts/UserLayout.vue`
2. `frontend/src/layouts/AdminLayout.vue`
3. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. 前端构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本轮未处理。

### 下一步任务清单

1. 刷新首页，确认顶部栏显示“首页”。
2. 进入用户端苹果订单，确认顶部栏显示“订单 / 苹果订单”。
3. 进入管理员端充值记录、待执行订单、系统管理子页面，确认显示为“财务管理 / 充值记录”、“订单执行 / 待执行订单”、“系统管理 / 价格配置”等层级路径。

### 风险点

1. 本轮只改顶部栏标题显示，不处理页面内部标题重复问题。
2. 后续新增二级菜单时仍需补充 `topTitleKey`，否则顶部栏无法显示一级菜单路径。

## 2026-06-22 更新：二级菜单页面顶部栏显示一级菜单

- 状态：已完成用户端和管理员端二级菜单页面顶部栏标题规则调整，并通过前端生产构建验证。
- 当前任务：用户要求如果菜单是二级菜单，顶部栏也要显示一级菜单。
- 实现原则：不改变路由路径、菜单选中逻辑或页面业务内容；仅新增路由 meta `topTitleKey`，由布局顶部栏优先读取一级菜单标题。
- 范围控制：仅修改前端路由 meta、用户/管理员布局标题读取逻辑和进度记录；未修改 `.env` 或秘钥配置；未安装依赖；未删除文件；未修改后端接口、数据库结构或项目架构。

### 实施内容

1. `UserLayout.vue` 和 `AdminLayout.vue` 顶部栏标题优先读取 `route.meta.topTitleKey`，没有配置时继续读取原有 `titleKey`。
2. 用户端订单二级菜单（苹果订单、谷歌订单、iPad订单）顶部栏统一显示一级菜单“订单”。
3. 管理员端订单执行二级菜单（待执行、执行中、已完成）顶部栏统一显示一级菜单“订单执行”。
4. 管理员端订单二级菜单（待确认订单、苹果订单、谷歌订单、iPad订单）顶部栏统一显示一级菜单“订单”。
5. 管理员端财务管理二级菜单（财务流水、充值记录）顶部栏统一显示一级菜单“财务管理”。
6. 管理员端系统管理二级菜单（价格配置、地区配置、站点配置、管理员管理、操作日志、账户设置等）顶部栏统一显示一级菜单“系统管理”。

### 修改文件

1. `frontend/src/layouts/UserLayout.vue`
2. `frontend/src/layouts/AdminLayout.vue`
3. `frontend/src/router/index.ts`
4. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. 前端构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本轮未处理。

### 下一步任务清单

1. 进入用户端订单二级菜单，确认顶部栏显示“订单”。
2. 进入管理员端订单执行、订单、财务管理、系统管理下的二级菜单，确认顶部栏显示对应一级菜单。
3. 后续如要解决页面内部标题与顶部栏重复，应单独执行“页面内部标题去重”任务。

### 风险点

1. 本轮只改顶部栏显示规则，不移除页面内部标题。
2. 如果未来新增二级菜单，需要在路由 meta 中同步设置 `topTitleKey`。

## 2026-06-22 更新：管理员端充值记录独立菜单

- 状态：已完成管理员端财务管理下的“充值记录”独立菜单，并通过前端构建和后端钱包测试验证。
- 当前任务：用户要求管理员端财务管理里面把充值记录单独做一个菜单。
- 实现原则：不新增数据库结构；复用现有钱包流水表和财务页面组件；后端查询接口增加可选交易类型过滤，确保充值记录不是前端从有限流水里临时筛选。
- 范围控制：仅修改管理员端菜单、路由、财务页面展示模式、钱包查询接口过滤和相关测试；未修改 `.env` 或秘钥配置；未安装依赖；未删除文件；未修改数据库结构或项目架构。

### 实施内容

1. 管理员端侧边栏将“财务管理”调整为一级分组。
2. “财务管理”分组下新增两个二级菜单：
   - `财务流水`：进入 `/admin/finance`，查看全部钱包流水。
   - `充值记录`：进入 `/admin/finance/recharges`，仅查看管理员充值记录。
3. 新增路由 `/admin/finance/recharges`，复用 `FinanceManagementView.vue`，通过 route meta 指定 `transactionType: ADMIN_RECHARGE`。
4. 财务页面根据路由模式切换标题、副标题和是否显示“充值”按钮；充值记录页隐藏充值按钮，只展示记录查询。
5. 前端钱包 API 支持传递 `transactionType` 查询参数。
6. 后端 `/api/admin/wallet/transactions` 增加可选 `transactionType` 查询参数，并在仓储层用参数化 SQL 过滤。
7. 增加钱包查询服务测试，覆盖管理员按 `ADMIN_RECHARGE` 类型筛选。

### 修改文件

1. `frontend/src/layouts/AdminLayout.vue`
2. `frontend/src/router/index.ts`
3. `frontend/src/views/admin/FinanceManagementView.vue`
4. `frontend/src/api/wallet.ts`
5. `frontend/src/i18n/locales/zh-CN.ts`
6. `frontend/src/i18n/locales/en-US.ts`
7. `backend/src/main/java/com/youou/aso/modules/wallet/api/AdminWalletController.java`
8. `backend/src/main/java/com/youou/aso/modules/wallet/service/WalletQueryService.java`
9. `backend/src/main/java/com/youou/aso/modules/wallet/repository/WalletQueryRepository.java`
10. `backend/src/main/java/com/youou/aso/modules/wallet/repository/JdbcWalletQueryRepository.java`
11. `backend/src/test/java/com/youou/aso/modules/wallet/service/WalletQueryServiceTest.java`
12. `PROJECT_PROGRESS.md`

### 验证结果

1. `mvn.cmd "-Dtest=WalletQueryServiceTest,AdminWalletServiceTest" test`：通过，7 个钱包相关测试全部成功。
2. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
3. 前端构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本轮未处理。

### 下一步任务清单

1. 刷新管理员端后台，确认侧边栏“财务管理”可展开，并显示“财务流水”和“充值记录”。
2. 进入“充值记录”，确认页面标题为充值记录且表格只展示管理员充值流水。
3. 在“财务流水”页面确认仍可发起充值，并且全部流水查询不受影响。
4. 如后续需要导出充值记录，可在该独立页面上补充导出按钮。

### 风险点

1. 本轮新增的是可选过滤参数，旧的 `/api/admin/wallet/transactions` 调用不传 `transactionType` 时仍返回全部流水。
2. 充值记录页复用财务页面组件，后续如果两个页面视觉或字段差异扩大，可能需要拆分为独立视图组件。

## 2026-06-22 更新：创建订单摘要顶部移除金额

- 状态：已移除用户端和管理员端创建订单页订单摘要顶部金额展示，并通过前端生产构建验证。
- 当前任务：用户要求订单摘要上边不要显示金额。
- 实现原则：金额只在“费用明细”和底部“合计”区域展示，避免摘要顶部和合计区重复；不改订单金额计算、提交 payload 或后端接口。
- 范围控制：仅修改两个订单创建页模板/样式和进度记录；未修改 `.env` 或秘钥配置；未安装依赖；未删除文件；未修改后端接口、数据库结构、路由结构或项目架构。

### 实施内容

1. 用户端创建订单页订单摘要标题区只保留“订单摘要”，移除顶部金额/需审核展示。
2. 管理员端创建订单页订单摘要标题区只保留“订单摘要”，移除顶部金额展示。
3. 保留摘要底部合计金额和提交按钮，费用金额仍在费用明细区展示。
4. 清理不再使用的 `.summary-heading strong` 样式。

### 修改文件

1. `frontend/src/views/user/OrderCreateView.vue`
2. `frontend/src/views/admin/OrderCreateView.vue`
3. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. 前端构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本轮未处理。

### 下一步任务清单

1. 刷新创建订单页，确认右侧摘要顶部仅显示“订单摘要”。
2. 确认费用明细和底部合计仍正常显示金额。
3. 切换特殊订单，确认用户端摘要顶部不再出现“需审核”重复展示。

### 风险点

1. 本轮仅移除重复展示，不影响实际订单金额计算。
2. 如果后续需要在摘要标题旁显示状态，应单独设计状态标签位置，避免与金额合计混淆。

## 2026-06-22 更新：移除创建订单摘要 sticky 偏移

- 状态：已移除用户端和管理员端创建订单页右侧订单摘要的 sticky 偏移，并通过前端生产构建验证。
- 当前任务：用户截图反馈右侧订单摘要仍然没有和左侧主表单顶部对齐，视觉上几乎没有变化。
- 根因定位：上一轮虽然调整了 `top` 数值，但右侧摘要仍保留 `position: sticky`，实际渲染中仍会被 sticky 偏移影响，导致卡片顶线比左侧主表单明显靠下。
- 范围控制：仅修改两个订单创建页样式和进度记录；未修改 `.env` 或秘钥配置；未安装依赖；未删除文件；未修改后端接口、数据库结构、路由结构或项目架构。

### 实施内容

1. 用户端创建订单页移除右侧摘要卡片的 `position: sticky` 和 `top`。
2. 管理员端创建订单页同步移除右侧摘要卡片的 `position: sticky` 和 `top`。
3. 移除响应式样式中不再需要的 `top: auto`。
4. 保留 `align-self: start`、`margin-top: 0`、300px 摘要宽度和现有费用明细/合计结构。

### 修改文件

1. `frontend/src/views/user/OrderCreateView.vue`
2. `frontend/src/views/admin/OrderCreateView.vue`
3. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. 前端构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本轮未处理。

### 下一步任务清单

1. 刷新创建订单页，确认右侧摘要卡片不再下移，边框顶线与左侧主表单边框顶线对齐。
2. 如仍有视觉偏差，需要使用浏览器截图测量外层容器实际 `top` 值后继续像素级调整。
3. 如果后续仍需要吸顶效果，应改为给摘要内部内容增加 sticky，而不是让整个网格项带 `top` 偏移。

### 风险点

1. 本轮取消了右侧摘要滚动吸顶效果，以换取初始顶线严格对齐。
2. 长表单滚动到底部时摘要不会固定在视口内；如业务强需求吸顶，需要重新设计内部 sticky 结构。

## 2026-06-22 更新：创建订单摘要与主表单顶部对齐

- 状态：已修正用户端和管理员端创建订单页右侧订单摘要的顶部对齐，并通过前端生产构建验证。
- 当前任务：用户反馈当前右侧合计/订单摘要区域并没有和左侧主表单区域对齐。
- 实现原则：保持现有双栏结构和 sticky 体验，只修正网格项对齐、外边距和摘要卡片内边距，不改订单业务逻辑。
- 范围控制：仅修改两个订单创建页样式和进度记录；未修改 `.env` 或秘钥配置；未安装依赖；未删除文件；未修改后端接口、数据库结构、路由结构或项目架构。

### 实施内容

1. 用户端创建订单页右侧摘要卡片增加 `align-self: start` 和 `margin-top: 0`，确保作为网格项与左侧表单顶部对齐。
2. 管理员端创建订单页同步应用相同对齐规则。
3. 将摘要卡片顶部内边距调整为 `16px 20px 20px`，与左侧首个表单区块的顶部节奏一致。
4. sticky 偏移由 `88px` 调整为 `76px`，避免滚动吸顶时视觉上过度下沉。
5. 响应式单栏场景下将 `top` 置为 `auto`，避免静态布局残留 sticky 偏移语义。

### 修改文件

1. `frontend/src/views/user/OrderCreateView.vue`
2. `frontend/src/views/admin/OrderCreateView.vue`
3. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. 前端构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本轮未处理。

### 下一步任务清单

1. 刷新创建订单页，确认右侧订单摘要卡片边框顶线与左侧主表单卡片边框顶线对齐。
2. 滚动页面，确认右侧摘要吸顶时不会明显下沉。
3. 在窄屏下确认摘要卡片堆叠到表单下方，没有保留桌面端 sticky 偏移。

### 风险点

1. 如果实际运行页面外层 header 高度继续变化，sticky `top` 可能还需要按真实 header 高度微调。
2. 本轮主要通过 CSS 规则约束对齐；如后续视觉审查仍发现像素级偏差，建议用浏览器截图测量后继续微调。

## 2026-06-22 更新：创建订单摘要合并合计与费用明细

- 状态：已完成用户端和管理员端创建订单页结算区合并，并通过前端生产构建验证。
- 当前任务：用户确认将底部“合计”区域合并到右侧订单摘要，并要求费用明细直接展示，不再只是一个提示按钮。
- 实现原则：仅调整前端展示结构；订单金额计算、提交 payload、余额扣款和后端接口均保持不变。
- 范围控制：仅修改两个订单创建页、订单创建中英文文案和进度记录；未修改 `.env` 或秘钥配置；未安装依赖；未删除文件；未修改后端接口、数据库结构、路由结构或项目架构。

### 实施内容

1. 移除用户端和管理员端创建订单页底部固定结算栏。
2. 在右侧订单摘要卡片中新增“费用明细”区域，按订单类型直接展开计费项。
3. 关键词安装显示关键词数、单价和小计。
4. 下载量显示天数、每日下载量、单价和小计。
5. 星级评分和用户评价分别显示 5 星、4 星数量、单价和小计。
6. 特殊订单在用户端显示待审核定价说明，在管理员端显示管理员填写金额。
7. 将“合计”和提交按钮合并到订单摘要卡片底部，形成完整结算侧栏。

### 修改文件

1. `frontend/src/views/user/OrderCreateView.vue`
2. `frontend/src/views/admin/OrderCreateView.vue`
3. `frontend/src/i18n/locales/zh-CN.ts`
4. `frontend/src/i18n/locales/en-US.ts`
5. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. 前端构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本轮未处理。

### 下一步任务清单

1. 刷新用户端创建订单页，确认右侧摘要内直接显示费用明细、合计和提交按钮。
2. 切换订单类型，检查关键词安装、下载量、评分、评价和特殊订单的明细展示是否符合业务含义。
3. 管理员端创建订单页复测特殊订单金额填写后，摘要合计是否同步显示。

### 风险点

1. 本轮只调整展示，未改变后端真实计费逻辑；如后续需要展示更细的地区级费用明细，需要扩展前端明细计算结构。
2. 右侧摘要宽度由 260px 调整为 300px，以容纳费用明细；窄屏下仍会自动堆叠。

## 2026-06-22 更新：订单创建地区下拉改为国旗加名称

- 状态：已完成用户端和管理员端订单创建页国家/地区下拉展示优化，并通过前端生产构建验证。
- 当前任务：用户指出创建订单页地区下拉不应该只显示 `US` 代码，应显示国旗图标加国家/地区名称。
- 实现原则：提交值仍保持地区代码，避免影响后端订单创建、价格计算和地区校验；展示层改为当前语言下的地区名称与国旗图标。
- 范围控制：仅修改两个订单创建页的地区下拉展示和局部样式；未修改 `.env` 或秘钥配置；未安装依赖；未删除文件；未修改后端接口、数据库结构、路由结构或项目架构。

### 实施内容

1. 用户端创建订单页所有国家/地区下拉改为显示“国旗 + 地区名称”。
2. 管理员端创建订单页同步改为显示“国旗 + 地区名称”。
3. 下拉输入框前缀显示当前已选地区的国旗，展开选项显示国旗和中英文地区名。
4. 地区名称根据当前语言自动选择 `nameZh` 或 `nameEn`。
5. 保留原有地区代码作为表单值和提交值，确保业务逻辑不变。

### 修改文件

1. `frontend/src/views/user/OrderCreateView.vue`
2. `frontend/src/views/admin/OrderCreateView.vue`
3. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. 前端构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本轮未处理。

### 下一步任务清单

1. 刷新用户端创建订单页，确认国家/地区下拉显示国旗和中文地区名称。
2. 切换英文后确认同一下拉显示国旗和英文地区名称。
3. 管理员端创建订单页复测关键词、下载量、评分、评论和特殊订单的地区下拉展示。

### 风险点

1. 国旗图片沿用项目应用管理页已有 `flagcdn.com` 地址；如果客户网络无法访问外部图片，国旗可能无法加载但地区名称仍可显示。
2. 本轮没有引入本地国旗资源包，避免新增依赖和资源体积。

## 2026-06-22 更新：订单创建地区下拉解除应用关联禁用

- 状态：已修复订单创建页地区下拉仍受应用选择/应用地区关联影响的问题，并通过前端生产构建验证。
- 当前任务：用户反馈应用和地区不再关联后，创建订单页“国家/地区”下拉不应再先禁用。
- 根因定位：此前已将地区数据源改为“当前商店支持的启用地区”，但模板禁用条件仍保留 `!selectedApp`；同时从路由参数进入创建页时仍用应用旧的 `regionCodes` 初始化地区，导致应用地区关联被拆分后表现不一致。
- 范围控制：仅修改用户端和管理员端订单创建页前端逻辑；未修改 `.env` 或秘钥配置；未安装依赖；未删除文件；未修改后端接口、数据库结构、路由结构或项目架构。

### 实施内容

1. 用户端创建订单页的普通地区下拉、关键词地区下拉、下载量/评分/评论地区下拉，禁用条件统一改为“当前商店无启用地区时禁用”。
2. 管理员端创建订单页同步应用相同逻辑，避免管理员给用户创建订单时仍被应用地区关联影响。
3. 路由参数带应用进入创建页时，不再使用应用的旧地区关联初始化，而是调用统一的商店可用地区默认值逻辑。
4. 保持提交前仍要求选择应用，地区选择只负责地区维度，不再承担应用校验。

### 修改文件

1. `frontend/src/views/user/OrderCreateView.vue`
2. `frontend/src/views/admin/OrderCreateView.vue`
3. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. 前端构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本轮未处理。

### 下一步任务清单

1. 刷新创建订单页，确认选择商店后国家/地区下拉可直接选择当前商店支持的启用地区。
2. 选择应用后确认地区下拉不会因为应用没有地区关联而变灰。
3. 从应用列表点击创建订单进入时，确认默认地区来自当前商店支持地区，而不是应用旧地区字段。

### 风险点

1. 如果后台没有配置某个商店的启用地区，该商店下拉仍会按预期禁用。
2. 提交订单仍需要选择应用；本轮只是解除地区选择与应用地区关联的错误耦合。

## 2026-06-22 更新：登录页新增记住密码选项

- 状态：已完成登录页“记住密码”复选框展示与记住偏好逻辑，并通过前端生产构建验证。
- 当前任务：用户要求页面再添加一个记住密码。
- 安全处理：本轮不把明文密码写入 `localStorage` 或源码。勾选后仅保存账号和记住偏好，密码保存交由浏览器原生密码管理器结合 `autocomplete="username/current-password"` 处理；这样既满足页面入口，也避免应用侧持久化明文密码。
- 范围控制：仅修改公开登录页和中英文文案；未修改 `.env` 或秘钥配置；未安装依赖；未删除文件；未修改后端接口、数据库结构、路由结构或项目架构。

### 实施内容

1. 登录页密码框下方新增“记住密码”复选框，与“忘记密码”保持同一行布局。
2. 登录页加载时读取 `youou_aso_remember_login` 偏好，自动回填账号并恢复复选框状态。
3. 登录成功后，如果勾选“记住密码”，保存账号和记住偏好；如果未勾选，则清理该偏好。
4. 保留账号输入框 `autocomplete="username"` 和密码输入框 `autocomplete="current-password"`，便于浏览器密码管理器完成安全密码保存和回填。
5. 补充中英文文案 `auth.rememberPassword`。

### 修改文件

1. `frontend/src/views/public/LoginView.vue`
2. `frontend/src/i18n/locales/zh-CN.ts`
3. `frontend/src/i18n/locales/en-US.ts`
4. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. 前端构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本轮未处理。

### 下一步任务清单

1. 刷新登录页，确认“记住密码”复选框与“忘记密码”同一行展示正常。
2. 勾选后登录，退出登录再回到登录页，确认账号可自动回填，浏览器密码管理器可按自身策略回填密码。
3. 如需完全由系统实现跨浏览器“记住我”能力，应另行设计 HttpOnly Secure Cookie + refresh token 方案，而不是保存用户明文密码。

### 风险点

1. 浏览器是否自动回填密码取决于浏览器密码管理器策略和用户是否允许保存密码。
2. 当前实现不会保存明文密码，因此不是不安全的“本地记住密码”实现；如果业务强制要求无浏览器提示也回填密码，需要先确认安全方案。

## 2026-06-22 更新：修复服务重启后登录状态失效

- 状态：已修复后端重启后 JWT 签名密钥变化导致登录状态失效的问题，并通过认证相关后端测试和前端构建验证。
- 当前任务：用户反馈每次重启服务后登录状态都没有了。
- 根因定位：前端登录态会持久化到 `localStorage`，但后端 `SecurityConfig` 在 `YOUOU_JWT_SECRET` 为空时每次启动都会生成新的随机 JWT 签名密钥；服务重启后旧 token 无法验签，`/api/auth/me` 返回未认证，前端 `bootstrap()` 捕获失败后执行 `logout()`，表现为登录状态丢失。
- 范围控制：仅修改后端 JWT 密钥解析逻辑、配置项和认证配置测试；未修改 `.env` 或任何秘钥内容；未安装依赖；未删除文件；未修改数据库结构、路由结构或项目架构。

### 实施内容

1. `YOUOU_JWT_SECRET` 显式配置时继续优先使用该密钥，保持生产部署可控。
2. 未配置 `YOUOU_JWT_SECRET` 时，不再每次启动使用内存随机密钥，而是从 `youou.security.jwt-secret-file` 指定的本机文件读取密钥。
3. 如果本机密钥文件不存在，则首次启动生成高强度随机密钥并写入该文件，后续重启复用。
4. 新增配置项 `youou.security.jwt-secret-file`，默认值为 `${user.home}/.youou-aso/jwt-secret`，也可通过 `YOUOU_JWT_SECRET_FILE` 覆盖。
5. 新增 `SecurityConfigTest` 覆盖：
   - 显式配置密钥时不创建本机密钥文件。
   - 未配置密钥时生成并复用同一个本机密钥文件。

### 修改文件

1. `backend/src/main/java/com/youou/aso/config/SecurityConfig.java`
2. `backend/src/main/resources/application.yml`
3. `backend/src/test/java/com/youou/aso/config/SecurityConfigTest.java`
4. `PROJECT_PROGRESS.md`

### 验证结果

1. `mvn.cmd "-Dtest=SecurityConfigTest,AuthServiceTest" test`：通过，7 个认证/安全配置相关测试全部成功。
2. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
3. 前端构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本轮未处理。

### 下一步任务清单

1. 重启后端服务一次，重新登录；之后再次重启后端，刷新前端页面，确认登录状态仍然有效。
2. 生产部署时应显式设置 `YOUOU_JWT_SECRET`，并确保长度至少 32 字节；不要依赖临时容器文件系统中的自动生成密钥。
3. 如果使用多实例部署，所有实例必须共享同一个 `YOUOU_JWT_SECRET`，否则跨实例请求仍会出现 token 验签失败。

### 风险点

1. 本机自动生成的密钥文件适合本地开发或单机部署；如果运行环境的用户目录会被清空，重启后仍可能重新生成密钥。
2. 本轮没有改前端 token 刷新机制，登录有效期仍由 `YOUOU_JWT_TTL_SECONDS` / `youou.security.jwt-ttl-seconds` 控制。

## 2026-06-22 更新：订单创建地区下拉改为商店启用地区

- 状态：已修复订单创建页“添加地区配置后下拉列表只有一个地区”的问题，并通过前端构建与后端订单服务测试验证。
- 当前任务：根据用户截图反馈，关键词安装订单第 6 步添加第二个地区配置时，国家/地区下拉仍只显示 US，无法选择其它国家/地区。
- 根因定位：创建订单页地区下拉使用 `selectedApp.regionCodes` 作为数据源，也就是“当前应用已关联地区”；当应用只关联 US 时，新增地区配置也只能选择 US。后端订单创建同时还要求地区必须已关联到应用，导致即使前端放开选项也会提交失败。该逻辑与此前确认的“应用与地区关联分开，创建订单时选择国家/地区”不一致。
- 范围控制：仅修改订单创建页地区选项来源和后端订单地区校验；未修改 `.env` 或秘钥配置；未安装依赖；未删除文件；未修改数据库结构、路由结构或项目架构。

### 实施内容

1. 用户端订单创建页加载 `/api/regions/enabled`，地区下拉改为显示当前应用商店支持的启用地区。
2. 管理员端订单创建页同步改为使用当前应用商店支持的启用地区，保持管理员给用户下单流程一致。
3. 新增地区配置时，优先默认选中尚未使用过的启用地区；如果所有地区都已使用，则兜底选择第一个启用地区。
4. 后端普通订单创建校验从“地区必须已关联到应用”改为“地区必须启用且支持当前应用商店”。
5. 后端特殊订单审核/管理员特殊订单提交同步使用相同地区校验规则，避免特殊订单仍被应用关联地区限制。
6. 增加订单服务回归测试：应用只关联 US 时，仍可提交支持当前商店的 JP 关键词安装订单，并正确保存订单明细地区。

### 修改文件

1. `frontend/src/views/user/OrderCreateView.vue`
2. `frontend/src/views/admin/OrderCreateView.vue`
3. `backend/src/main/java/com/youou/aso/modules/order/service/OrderService.java`
4. `backend/src/main/java/com/youou/aso/modules/order/service/SpecialOrderAuditService.java`
5. `backend/src/test/java/com/youou/aso/modules/order/service/OrderServiceTest.java`
6. `backend/src/test/java/com/youou/aso/modules/order/service/SpecialOrderAuditServiceTest.java`
7. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. `mvn.cmd "-Dtest=OrderServiceTest,SpecialOrderAuditServiceTest" test`：通过，24 个订单相关测试全部成功。
3. 首次运行未加引号的 `mvn.cmd -Dtest=OrderServiceTest,SpecialOrderAuditServiceTest test` 被 PowerShell 将逗号解析为参数分隔导致命令解析失败；随后已用带引号命令重新验证通过。
4. 前端构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本轮未处理。

### 下一步任务清单

1. 重启后端服务或重新部署最新后端代码，使新的地区校验逻辑生效。
2. 刷新用户端创建订单页，选择一个只关联 US 的应用后点击“添加地区配置”，确认下拉可选择当前商店支持的其它启用地区。
3. 用两个不同地区各添加关键词和数量，提交订单后确认订单主地区为 `MULTI`，明细中保存各自地区。
4. 管理员端创建订单页也需要用真实管理员账号复测同样流程。

### 风险点

1. 当前订单提交只允许选择系统启用且支持对应商店的地区；如果后台地区配置未启用或未勾选对应商店，前端下拉不会显示，后端也会拒绝。
2. 应用搜索/添加应用仍需要在某个地区校验真实应用；本轮只调整创建订单时的目标投放地区，不改变应用校验流程。

## 2026-06-20 更新：公开页与用户端真实浏览器视觉审查精修

- 状态：已完成公开页与用户端核心页面的三轮真实 Chrome 视觉审查和本轮精修，并通过前端生产构建验证。
- 当前任务：按照用户要求，不使用静态预览，基于真实运行的前端开发服务和后端服务，对系统页面从主题到细节进行循环视觉审查；发现审查流程或页面问题先修复再继续审查。
- 范围控制：本轮仅修改前端视觉样式和视觉审查脚本；未修改 `.env` 或秘钥配置；未安装依赖；未删除文件；未修改数据库结构、后端接口、业务规则、路由结构或项目架构。

### 审查与精修内容

1. 新增并修正真实 Chrome DevTools 审查脚本，覆盖桌面端与窄屏端，分别输出公开页和用户端页面截图、横向溢出检测、运行期异常和网络错误采集结果。
2. 修复审查脚本中公开登录/注册页被已有登录态重定向的问题，公开页审查前清理登录态，用户端页面审查前再注入真实登录态。
3. 修复审查脚本改造中的语法错误后重新执行，确保后续视觉判断来自有效截图。
4. 用户端应用管理页空态文案从英文 `No data` 改为中英文语言包文案，并提升筛选工具栏和表格卡片的边框、阴影、表头与空态质感。
5. 用户端订单列表、收支明细和首页表格卡片统一增加产品级容器、表头背景、行高和空态高度，减少页面粗糙感。
6. 创建订单页优化卡片阴影、底部提交栏、系统时间格式；窄屏下系统时间改为可换行展示，避免右侧裁切。
7. 创建订单页窄屏下关键词表格改为三列紧凑结构，隐藏桌面端用于还原参考图比例的空白占位列，避免表格列错位感。
8. 公开页移动端头部增加防换行和弹性布局处理，修复 logo、登录、注册在窄屏下被挤成竖排的问题。

### 修改文件

1. `frontend/src/layouts/PublicLayout.vue`
2. `frontend/src/views/user/ApplicationManagementView.vue`
3. `frontend/src/views/user/StoreOrdersView.vue`
4. `frontend/src/views/user/TransactionsView.vue`
5. `frontend/src/views/user/DashboardView.vue`
6. `frontend/src/views/user/OrderCreateView.vue`
7. `frontend/src/i18n/locales/zh-CN.ts`
8. `frontend/src/i18n/locales/en-US.ts`
9. `visual-audit/run-cdp-audit.mjs`
10. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. `mvn.cmd -Dtest=AuthServiceTest test`：通过，5 个认证服务测试全部成功。
3. `node visual-audit\run-cdp-audit.mjs`：真实 Chrome 审查通过，桌面端和窄屏端共 22 个截图目标均无横向溢出，采集到的运行期异常/网络错误数量为 0。
4. 人工复查截图：
   - `mobile-public-home.png`：移动端公开首页头部不再逐字换行。
   - `mobile-public-login.png`：移动端登录页卡片和头部布局稳定。
   - `mobile-user-order-create.png`：创建订单移动端系统时间完整可读，地区关键词表格列对齐。
   - `desktop-user-order-create.png`：创建订单桌面端保持参考图方向的主表单与右侧摘要结构。
5. 构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本轮未处理。

### 下一步任务清单

1. 提供可用管理员测试账号后，继续用真实登录态循环审查管理员端所有页面，不能用伪造登录态或静态页面替代。
2. 管理员端审查重点包括：用户管理、应用管理、推广服务、订单执行、订单列表、审核管理、财务管理、系统管理相关页面。
3. 如果管理员端真实浏览器审查发现接口错误、401/500、横向溢出、标题重复、空态粗糙、表格错位或移动端裁切，需要先修复后再继续下一轮审查。
4. 后续可继续把公开页语言切换也统一为登录后常用下拉样式，但这会改变公开页既有交互，本轮未扩展。

### 风险点

1. 本轮真实浏览器审查已覆盖公开页和用户端核心页面；管理员端因缺少可用管理员登录态，尚未完成真实浏览器闭环审查。
2. `visual-audit` 目录下会生成本地截图和审查报告，仅用于本地视觉审查，不应作为生产资源引用。
3. 审查脚本依赖本机 Chrome、真实前端开发服务和本地后端服务；换机器或端口后需要同步调整运行环境。

## 2026-06-20 更新：登录后顶部语言切换改为下拉样式

- 状态：已完成用户端和管理员端顶部语言切换下拉化，并通过前端生产构建验证。
- 当前任务：根据用户确认，将顶部“中文 / English”分段按钮改成多数网站常用的语言下拉选择样式。
- 范围控制：仅修改登录后的用户端和管理员端布局展示；未修改公开页语言切换；未修改 `.env` 或秘钥配置；未安装依赖；未删除文件；未修改后端接口、数据库结构、路由结构或项目架构。

### 实施内容

1. 用户端 `UserLayout` 顶部语言切换由分段按钮改为 `el-dropdown` 下拉菜单。
2. 管理员端 `AdminLayout` 顶部语言切换同步改为 `el-dropdown` 下拉菜单。
3. 下拉触发器采用“语言图标 + 当前语言 + 下拉箭头”的胶囊按钮样式。
4. 下拉菜单中保留“中文”和“English”两个选项，并用勾选图标标识当前语言。
5. 保持原有 `setLocale` 行为：切换后写入本地语言偏好并更新 `document.documentElement.lang`。
6. 账户设置页中的语言偏好仍保留表单单选样式；公开页 `PublicLayout` 暂不修改。

### 修改文件

1. `frontend/src/layouts/UserLayout.vue`
2. `frontend/src/layouts/AdminLayout.vue`
3. `PROJECT_PROGRESS.md`

### 验证结果

1. `rg -n "language-switch|language-trigger|language-check|currentLanguageLabel|Connection|Check" frontend/src/layouts/UserLayout.vue frontend/src/layouts/AdminLayout.vue frontend/src/layouts/PublicLayout.vue`：确认用户端和管理员端已接入下拉语言控件，公开页仍保留旧控件。
2. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
3. 构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本次未处理。

### 下一步任务清单

1. 在浏览器中分别打开用户端和管理员端页面，确认语言下拉位置、宽度、菜单展开方向和当前语言勾选符合预期。
2. 如果需要登录页、注册页等公开页面也统一为下拉样式，再单独同步 `PublicLayout`。
3. 后续可考虑抽取公共语言切换组件，减少 `UserLayout` 和 `AdminLayout` 重复样式；该抽取属于小重构，需另行确认。

### 风险点

1. 当前只改登录后布局，公开页仍是分段按钮，因此不同区域语言切换样式暂时不完全一致。
2. 两个布局内暂时保留重复模板和样式，以控制本次改动范围；如果后续继续统一设计，可再抽公共组件。

## 2026-06-20 更新：修复登录后用户端账号信息短暂为空

- 状态：已完成登录返回账号信息补齐和用户端显示兜底修复，并通过后端认证服务测试与前端生产构建验证。
- 当前任务：根据用户反馈，用户已经登录后右上角账号区域显示为“当前账户/空信息”，刷新页面后才恢复真实用户名和邮箱。
- 根因定位：后端 `/api/auth/login` 返回的 `LoginResult` 缺少 `username` 和 `email` 字段；前端登录成功后立即把登录返回写入 Pinia/localStorage 并跳转，因此首次进入用户端页面时用户名和邮箱为空。刷新页面后会走 `/api/auth/me`，该接口返回了完整账号信息，所以刷新后恢复正常。
- 范围控制：仅修改登录返回 DTO、登录服务返回内容、认证服务测试、用户端账号显示兜底和多语言文案；未修改 `.env` 或秘钥配置；未安装依赖；未删除文件；未修改数据库结构、路由结构或项目架构。

### 实施内容

1. 后端 `LoginResult` 补充 `username` 和 `email` 字段，使 `/api/auth/login` 与前端登录态字段保持一致。
2. `AuthService` 在普通用户和管理员登录成功时，直接返回账号用户名和邮箱，避免依赖刷新后的 `/api/auth/me` 才补全信息。
3. `AuthServiceTest` 增加登录返回用户名、邮箱断言，防止后续回归。
4. 用户端顶部账号区域增加稳健兜底：如果异常情况下用户名和邮箱仍为空，则显示“用户 {ID}”，下拉中显示“用户ID：{ID}”，不再出现“当前账户/当”。
5. 用户端账户设置页、用户首页与顶部栏统一账号显示兜底逻辑。
6. 中英文文案补充 `account.userFallback`，并将通用未知账号文案从“当前账户”收敛为“账户/Account”。

### 修改文件

1. `backend/src/main/java/com/youou/aso/modules/account/dto/LoginResult.java`
2. `backend/src/main/java/com/youou/aso/modules/account/service/AuthService.java`
3. `backend/src/test/java/com/youou/aso/modules/account/service/AuthServiceTest.java`
4. `frontend/src/layouts/UserLayout.vue`
5. `frontend/src/views/user/AccountSettingsView.vue`
6. `frontend/src/views/user/DashboardView.vue`
7. `frontend/src/i18n/locales/zh-CN.ts`
8. `frontend/src/i18n/locales/en-US.ts`
9. `PROJECT_PROGRESS.md`

### 验证结果

1. `rg -n "new LoginResult|username\\(\\)|email\\(\\)|userFallback|accountSubline" ...`：确认登录返回构造、测试断言和用户端显示兜底已接入。
2. `mvn.cmd -Dtest=AuthServiceTest test`：通过，5 个认证服务测试全部成功。
3. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
4. 构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本次未处理。

### 下一步任务清单

1. 重启后端服务或重新打包部署后端，使新的 `/api/auth/login` 返回结构生效。
2. 清理浏览器旧登录态后重新登录普通用户，确认无需刷新即可在右上角、账户设置页和首页看到用户名/邮箱。
3. 管理员端登录也复测一次，确认登录返回补齐 username/email 后顶部账号信息仍正常。

### 风险点

1. 这是接口返回字段的向后兼容增强，现有前端本来已经按这些字段读取，因此对当前前端是修复；如果存在外部客户端严格校验登录响应字段，需要确认其能接受新增字段。
2. 运行中的旧后端进程不会自动生效，必须重启后端或重新部署最新 jar。

## 2026-06-20 更新：用户端顶部余额与客服二维码充值弹窗

- 状态：已完成用户端顶部账户余额展示和充值弹窗调整，并通过前端生产构建验证。
- 当前任务：根据用户确认，账户余额和充值入口应显示在用户端右上角；点击“充值”不调用充值接口、不跳转页面，而是弹出客服二维码，引导用户联系客服充值。
- 范围控制：仅修改用户端布局和多语言文案；未修改 `.env` 或秘钥配置；未安装依赖；未删除文件；未修改后端接口、数据库结构、路由结构或项目架构。

### 实施内容

1. 在用户端顶部栏右侧新增账户余额区域，展示“账户余额”、当前余额、“最新”刷新入口和“充值”按钮。
2. 用户端布局挂载时调用现有 `getCustomerWallet()` 读取账户余额；点击“最新”可手动刷新余额。
3. 将“充值”按钮行为改为打开客服二维码弹窗，不再跳转到收支明细页面，也不调用充值接口。
4. 弹窗预留客服二维码图片展示位置；当前项目未发现可用客服二维码图片资源，因此先显示“客服二维码待配置”的占位提示，避免使用伪造二维码。
5. 补充中英文多语言文案：账户余额、最新、客服二维码、扫码充值说明、二维码待配置。

### 修改文件

1. `frontend/src/layouts/UserLayout.vue`
2. `frontend/src/i18n/locales/zh-CN.ts`
3. `frontend/src/i18n/locales/en-US.ts`
4. `PROJECT_PROGRESS.md`

### 验证结果

1. `rg -n "rechargeDialogVisible|customerServiceQrUrl|qr-placeholder|customerServiceQr|scanCustomerServiceQr|qrNotConfigured|goRecharge|wallet-pill" frontend/src/layouts/UserLayout.vue frontend/src/i18n/locales/zh-CN.ts frontend/src/i18n/locales/en-US.ts`：确认用户端余额区域、充值弹窗和多语言文案已接入。
2. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
3. 构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本次未处理。

### 下一步任务清单

1. 提供真实客服二维码图片后，将图片资源接入 `customerServiceQrUrl`，使充值弹窗显示正式二维码。
2. 在浏览器中用普通用户账号刷新用户端页面，确认右上角余额、刷新按钮、充值弹窗位置与视觉效果符合预期。
3. 如需支持运营后台维护客服二维码，应单独确认配置来源和权限方案，再评估是否新增后端配置能力。

### 风险点

1. 当前没有真实二维码资源，因此充值弹窗显示占位状态；上线前必须配置真实客服二维码。
2. 余额只在页面挂载和用户点击“最新”时刷新，不是实时推送；充值到账后用户需要手动刷新或重新进入页面查看最新余额。

## 2026-06-20 更新：管理员用户管理页视觉优化

- 状态：已完成管理员端用户管理页视觉优化，并通过前端生产构建验证。
- 当前任务：根据用户截图反馈，用户管理页面样式粗糙，存在标题重复、筛选区换行混乱、表格缺少产品级容器和操作密度不佳等问题。
- 范围控制：仅修改管理员用户管理页面模板和样式；未修改 `.env` 或秘钥配置；未安装依赖；未删除文件；未修改后端接口、数据库结构、路由结构或项目架构。

### 实施内容

1. 移除页面内重复标题区，避免与后台顶部栏“用户管理”重复。
2. 将筛选、搜索、清空和刷新整合为单行工具栏。
3. 新增白色内容卡片容器，统一边框、圆角和轻阴影。
4. 优化表格表头背景、行高、文字颜色和 hover 状态。
5. 用户名、邮箱改为稳定单行显示，并支持溢出 tooltip，避免长文本撑高表格行。
6. 金额列使用等宽数字风格，提升财务数据可读性。
7. 操作列改为紧凑按钮组，减少按钮散乱感。
8. 增加移动端下工具栏换行适配。

### 修改文件

1. `frontend/src/views/admin/CustomerManagementView.vue`
2. `PROJECT_PROGRESS.md`

### 验证结果

1. `rg -n "eyebrow|subtitle|customer-toolbar|customer-card|action-group|primary-cell" frontend/src/views/admin/CustomerManagementView.vue`：确认旧标题类不再存在，新结构和样式已接入。
2. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
3. 前端构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本次未处理。

### 下一步任务清单

1. 刷新管理员用户管理页，确认筛选区、表格行高和操作按钮视觉效果符合后台管理页面标准。
2. 继续检查管理员端财务管理、审核管理等列表页是否存在同类粗糙布局问题。

### 风险点

1. 本次只调整前端展示结构和样式，不改变用户状态修改、搜索和刷新逻辑。

## 2026-06-20 更新：管理员推广服务页移除需审核标签

- 状态：已完成管理员端推广服务页特殊订单卡片“需审核”标签移除，并通过前端生产构建验证。
- 当前任务：根据用户截图反馈，管理员推广服务页两个特殊订单卡片标题旁不应显示“需审核”。
- 范围控制：仅修改管理员推广服务页面模板和样式；未修改 `.env` 或秘钥配置；未安装依赖；未删除文件；未修改后端接口、数据库结构、路由结构或项目架构。

### 实施内容

1. 移除管理员推广服务页特殊订单卡片标题旁的 `promotionPage.auditBadge` 标签。
2. 移除管理员推广服务页未再使用的 `.service-badge` 样式。
3. 保留用户端推广服务页的“需审核”标签，因为用户端特殊订单仍走提交需求和审核支付流程。

### 修改文件

1. `frontend/src/views/admin/PromotionServicesView.vue`
2. `PROJECT_PROGRESS.md`

### 验证结果

1. `rg -n "service-badge|auditBadge" frontend/src/views/admin/PromotionServicesView.vue`：无匹配结果。
2. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
3. 前端构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本次未处理。

### 下一步任务清单

1. 刷新管理员推广服务页，确认关键词保排名和关键词覆盖卡片不再显示“需审核”。
2. 继续检查管理员端页面是否还有用户端流程文案残留。

### 风险点

1. 本次只移除管理员端展示标签，不改变特殊订单创建、扣款和待支付流程。

## 2026-06-20 更新：管理员推广服务页移除重复小标题

- 状态：已完成管理员端推广服务页重复小标题移除，并通过前端生产构建验证。
- 当前任务：根据用户截图反馈，页面顶部同时显示小标题“推广服务”和主标题“推广服务”，造成重复。
- 范围控制：仅修改管理员推广服务页面模板和样式；未修改 `.env` 或秘钥配置；未安装依赖；未删除文件；未修改后端接口、数据库结构、路由结构或项目架构。

### 实施内容

1. 移除管理员推广服务页顶部 eyebrow 小标题渲染。
2. 移除该页面未再使用的 `.eyebrow` 样式。
3. 保留主标题“推广服务”和页面说明文案。

### 修改文件

1. `frontend/src/views/admin/PromotionServicesView.vue`
2. `PROJECT_PROGRESS.md`

### 验证结果

1. `rg -n "adminPromotion\\.eyebrow|\\.eyebrow" frontend/src/views/admin/PromotionServicesView.vue`：无匹配结果。
2. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
3. 前端构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本次未处理。

### 下一步任务清单

1. 刷新管理员推广服务页面，确认顶部只显示一个“推广服务”标题。
2. 继续检查其它页面是否存在重复标题或重复说明。

### 风险点

1. 本次只移除重复展示元素，不改变页面跳转和下单流程。

## 2026-06-20 更新：管理员推广服务页去除代客下单文案

- 状态：已完成管理员端推广服务页文案修正，并通过前端生产构建验证。
- 当前任务：根据用户反馈，推广服务页面不要写“代客下单”。
- 范围控制：仅修改前端多语言文案和进度记录；未修改 `.env` 或秘钥配置；未安装依赖；未删除文件；未修改后端接口、数据库结构、路由结构或项目架构。

### 实施内容

1. 将中文 `adminPromotion.eyebrow` 从“代客下单”改为“推广服务”。
2. 将英文 `adminPromotion.eyebrow` 从 `Create for customer` 改为 `Promotion Services`。
3. 检查前端代码中不再残留“代客下单 / Create for customer”展示文案。

### 修改文件

1. `frontend/src/i18n/locales/zh-CN.ts`
2. `frontend/src/i18n/locales/en-US.ts`
3. `PROJECT_PROGRESS.md`

### 验证结果

1. `rg -n "代客下单|Create for customer" frontend/src`：无匹配结果。
2. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
3. 前端构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本次未处理。

### 下一步任务清单

1. 刷新管理员端推广服务页面，确认顶部小标题显示为“推广服务”。
2. 继续检查其它页面是否还有不符合产品语气的运营文案。

### 风险点

1. 本次只调整展示文案，不改变管理员给用户创建订单的业务流程。

## 2026-06-20 更新：管理员特殊订单直接填金额下单

- 状态：已完成管理员端特殊订单代客下单流程调整，并通过前端生产构建和后端测试验证。
- 当前任务：根据用户确认，用户端特殊订单仍保留原“提交需求、管理员审核、用户确认支付”流程；管理员端给用户下特殊订单时直接填写金额，余额充足则立即扣款生成正式订单，余额不足则进入“已审核待支付”。
- 范围控制：仅修改管理员创建订单前端、管理员创建订单接口返回、特殊订单服务逻辑和对应测试；未修改 `.env` 或秘钥配置；未安装依赖；未删除文件；未修改数据库结构或路由结构。

### 实施内容

1. 管理员创建订单接口新增特殊订单分支：
   - `RANK_GUARANTEE`
   - `KEYWORD_COVERAGE`
2. 新增 `AdminCreateOrderResult` 返回对象，用于区分：
   - `paid=true`：余额充足，已扣款并生成正式订单
   - `waitPayment=true`：余额不足，已生成“已审核待支付”的特殊审核记录
3. 新增管理员专用特殊订单服务方法 `submitAdminSpecialOrder`：
   - 校验用户、应用、特殊订单类型和金额
   - 创建已审核待支付的特殊审核记录
   - 尝试复用既有 `submitApprovedAudit` 扣款并生成正式订单
   - 捕获 `BALANCE_NOT_ENOUGH` 后保留审核记录为 `APPROVED_WAIT_SUBMIT`
4. 管理员创建订单页：
   - 特殊订单不再显示“暂不支持”
   - 增加订单金额输入
   - 摘要区显示填写金额
   - 提交后根据结果跳转订单列表或审核管理页
5. 用户端特殊订单流程未修改，仍按原需求提交和审核支付流程执行。
6. 更新管理员推广服务文案，特殊订单按钮改为可立即订购。
7. 更新 `AdminOrderControllerTest`，覆盖普通代客下单和特殊订单余额不足待支付分支。

### 修改文件

1. `backend/src/main/java/com/youou/aso/modules/order/dto/AdminCreateOrderResult.java`
2. `backend/src/main/java/com/youou/aso/modules/order/api/AdminOrderController.java`
3. `backend/src/main/java/com/youou/aso/modules/order/service/SpecialOrderAuditService.java`
4. `backend/src/test/java/com/youou/aso/modules/order/api/AdminOrderControllerTest.java`
5. `frontend/src/api/orders.ts`
6. `frontend/src/views/admin/OrderCreateView.vue`
7. `frontend/src/i18n/locales/zh-CN.ts`
8. `frontend/src/i18n/locales/en-US.ts`
9. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. `mvn test`：通过，后端 68 个测试全部通过。
3. `rg -n "AdminCreateOrderResult|submitAdminSpecialOrder|specialAmount|adminSpecialWaitPaymentSuccess|special-amount-panel|specialPending" ...`：确认后端分支、前端金额字段、返回类型和文案已接入。
4. 前端构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本次未处理。
5. 后端测试运行时仍存在本地环境相关提示：Flyway 提示 MySQL 8.3 高于当前测试过的版本、Spring Boot 测试生成开发密码、ByteBuddy 动态 agent 警告，均未阻断测试。

### 下一步任务清单

1. 在浏览器中用管理员账号创建关键词保排名/关键词覆盖订单，分别验证余额充足和余额不足两条路径。
2. 确认余额不足后用户端特殊订单列表显示为“已审核待支付”，且用户充值后可继续点击支付。
3. 检查管理员创建页中特殊订单是否还需要补充可选备注/协商内容字段；当前按用户要求只填写金额。

### 风险点

1. 管理员特殊订单当前默认生成的协商内容为“服务名 - 应用名”，没有额外备注字段；如果后续运营需要记录更细的协商内容，需要再增加前端输入和后端入参。
2. 管理员创建订单接口返回结构由纯 `OrderResult` 改为 `AdminCreateOrderResult`，当前已同步管理员创建页调用；如未来有其它调用方，需要按新结构读取。

## 2026-06-20 更新：评分和评价订单最后一步地区配置修正

- 状态：已完成用户端和管理员端创建订单页评分、评价订单最后一步修正，并通过前端生产构建验证。
- 当前任务：根据用户反馈，星级评分和用户评价应与下载量一致，最后一步分别为“地区评分”和“地区评价”，国家/地区选择也应放在最后一步，而不是前置地区步骤或“订单参数”区块。
- 范围控制：仅修改前端创建订单页面模板、样式和少量多语言文案；未修改 `.env` 或秘钥配置；未安装依赖；未删除文件；未修改后端接口、数据库结构、路由结构或项目架构。

### 实施内容

1. 评分和评价订单隐藏前置通用“地区”步骤，国家/地区选择移动到最后一步。
2. 下载量、评分、评价的最后一步统一进入地区指标表格。
3. 评分订单最后一步标题改为“地区评分”，并包含国家/地区、5 星评分数量、4 星评分数量。
4. 评价订单最后一步标题改为“地区评价”，并包含国家/地区、5 星评论数量、4 星评论数量。
5. 将原下载量专用样式抽象为通用 `region-metric-*` 样式，供下载量、评分、评价共用。
6. 新增 `orderCreate.regionRatings`、`orderCreate.regionReviews` 中英文文案。
7. 用户端和管理员端同步应用。

### 修改文件

1. `frontend/src/views/user/OrderCreateView.vue`
2. `frontend/src/views/admin/OrderCreateView.vue`
3. `frontend/src/i18n/locales/zh-CN.ts`
4. `frontend/src/i18n/locales/en-US.ts`
5. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. `rg -n "download-region|region-metric|regionRatings|regionReviews|RATING', 'REVIEW" ...`：确认旧下载量专用类名无残留，评分和评价已接入地区指标表格。
3. 前端构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本次未处理。

### 下一步任务清单

1. 在浏览器中分别切换到评分和评价订单，确认最后一步标题和字段显示正确。
2. 继续检查各订单类型的步骤编号是否需要统一优化，避免隐藏前置地区步骤后出现视觉上的编号跳跃。
3. 如后续需要多国家/地区评分或评价，需要确认接口和数据结构扩展方案。

### 风险点

1. 本次复用现有单地区字段 `regionCode`，因此评分和评价订单当前支持一个国家/地区加对应数量；如果要一个订单配置多个国家/地区，需要后续确认后端模型和接口调整。

## 2026-06-20 更新：下载量订单第 7 步地区下载量修正

- 状态：已完成用户端和管理员端创建订单页下载量订单第 7 步修正，并通过前端生产构建验证。
- 当前任务：根据用户反馈，下载量订单第 7 步不应显示为“订单参数”且不能只有一个数量输入，应为“地区下载量”，并包含国家/地区选择和每日下载量。
- 范围控制：仅修改前端创建订单页面模板、样式和少量多语言文案；未修改 `.env` 或秘钥配置；未安装依赖；未删除文件；未修改后端接口、数据库结构、路由结构或项目架构。

### 实施内容

1. 下载量订单隐藏前置通用“地区”步骤，避免页面重复选择地区。
2. 下载量订单原“订单参数”区块改为“地区下载量”。
3. 在下载量第 7 步中新增国家/地区选择和每日下载量输入，分别复用现有 `regionCode` 与 `dailyDownloadCount` 字段。
4. 新增 `orderCreate.regionDownloads` 中英文文案。
5. 新增下载量地区表格样式，使其与创建订单第 7 步的表格视觉语言保持一致。
6. 用户端和管理员端同步应用。

### 修改文件

1. `frontend/src/views/user/OrderCreateView.vue`
2. `frontend/src/views/admin/OrderCreateView.vue`
3. `frontend/src/i18n/locales/zh-CN.ts`
4. `frontend/src/i18n/locales/en-US.ts`
5. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. `rg -n "regionDownloads|download-region-table|form\\.orderType !== 'KEYWORD_INSTALL' && form\\.orderType !== 'DOWNLOAD'|dailyDownloadCount" ...`：确认下载量专属区块、文案和字段绑定已接入。
3. 前端构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本次未处理。

### 下一步任务清单

1. 在浏览器中切换到下载量订单，确认第 7 步标题显示为“地区下载量”。
2. 确认下载量订单可选择国家/地区，并可填写每日下载量。
3. 继续检查下载量、评分、评论等非关键词订单参数区是否需要进一步按设计图统一视觉细节。

### 风险点

1. 本次复用现有单地区字段 `regionCode`，因此下载量订单当前支持一个国家/地区加一个每日下载量；如果后续要求一个下载量订单内配置多个国家/地区，需要确认接口与数据结构扩展。

## 2026-06-20 更新：创建订单第 7 步表格行列对齐修正

- 状态：已完成用户端和管理员端创建订单页第 7 步关键词表格行列对齐修正，并通过前端生产构建验证。
- 当前任务：根据用户反馈，问题不在初始值，而是表格第二行与表头列线错开，需要按真实表格单元格结构修正。
- 范围控制：仅修改前端创建订单页面第 7 步关键词表格模板和样式；未修改 `.env` 或秘钥配置；未安装依赖；未删除文件；未修改后端接口、数据库结构、路由结构或项目架构。

### 实施内容

1. 第 7 步关键词表格 body 行从“3 个控件直接放入 grid”改为 4 个 `.keyword-cell` 单元格：
   - 关键词单元格
   - 每日数量单元格
   - 操作单元格
   - 右侧空白单元格
2. 表头和 body 行继续共用 `180px 180px 64px minmax(140px, 1fr)` 列宽。
3. 删除旧的 body 行子元素 margin 布局，改为单元格 padding 和右边框。
4. 为 body 行补齐竖向边框，使第二行列线与表头一致。
5. 用户端和管理员端同步应用。

### 修改文件

1. `frontend/src/views/user/OrderCreateView.vue`
2. `frontend/src/views/admin/OrderCreateView.vue`
3. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. `rg -n "keyword-cell|keyword-action-cell|keyword-empty-cell|keyword-row > \\*|grid-template-columns: 180px 180px 64px" ...`：确认新增单元格结构和列宽存在，旧的 `keyword-row > *` 规则无残留。
3. 前端构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本次未处理。

### 下一步任务清单

1. 刷新创建订单页，确认第 7 步表头和第二行列线完全对齐。
2. 继续对照设计图检查输入框左右 padding 和删除图标位置。

### 风险点

1. 本次仅调整表格结构和样式，不改变关键词数量计算和订单提交逻辑。

## 2026-06-20 更新：创建订单第 7 步表格二次细节对齐

- 状态：已完成用户端和管理员端创建订单页第 7 步关键词表格二次细节调整，并通过前端生产构建验证。
- 当前任务：根据用户对比截图，第 7 步关键词表格仍有细节问题：每日数量默认显示 `1`、删除按钮是粉色块、行高与输入框视觉和设计图不一致。
- 范围控制：仅修改前端创建订单页面模板、样式和第 7 步关键词输入默认值；未修改 `.env` 或秘钥配置；未安装依赖；未删除文件；未修改后端接口、数据库结构、路由结构或项目架构。

### 实施内容

1. 关键词安装的新增关键词行数量默认值从 `1` 改为空值，页面显示“请输入每日数量”placeholder。
2. `KeywordInput.quantity` 类型调整为 `number | null`，计算逻辑仍只收集数量大于 0 的有效关键词行。
3. 第 7 步关键词行删除按钮改为 `text` 图标按钮，避免显示为粉色块。
4. 新增 `.keyword-delete-button` 样式，将删除按钮压缩为 28px 图标按钮。
5. 第 7 步关键词表格输入框高度调整为 32px，行高调整为 48px，更贴近设计图。
6. 用户端和管理员端同步应用。

### 修改文件

1. `frontend/src/views/user/OrderCreateView.vue`
2. `frontend/src/views/admin/OrderCreateView.vue`
3. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. `rg -n "quantity: null|quantity: 1|keyword-delete-button|el-input-number" ...`：确认第 7 步关键词默认数量为空且使用专用删除按钮；保留的 `el-input-number` 属于下载/评分/评论参数区。
3. `rg -n "min-height: 32px|Number\\(item.quantity\\)" ...`：确认输入框高度和数量计算逻辑存在。
4. 前端构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本次未处理。

### 下一步任务清单

1. 刷新创建订单页，确认每日数量显示 placeholder 而不是默认 `1`。
2. 对照设计图继续检查关键词表格列宽和删除图标位置。

### 风险点

1. 初始数量为空后，用户必须填写每日数量，提交校验仍依赖有效数量大于 0。

## 2026-06-20 更新：创建订单第 7 步表格细节修正

- 状态：已完成用户端和管理员端创建订单页第 7 步关键词表格细节修正，并通过前端生产构建验证。
- 当前任务：根据用户截图反馈，第 7 步关键词表格仍与设计图不一致，包括表头 key 外露、每日数量为数字步进器、表格过宽等问题。
- 范围控制：仅修改前端创建订单页面模板、样式和少量多语言文案；未修改 `.env` 或秘钥配置；未安装依赖；未删除文件；未修改后端接口、数据库结构、路由结构或项目架构。

### 实施内容

1. 将关键词表头从错误的 `ordersPage.keyword` 改为已有文案 `orderCreate.keywords`，避免页面显示 i18n key。
2. 第 7 步“每日数量”输入由 `el-input-number` 改为普通 `el-input type="number"`，去掉加减号控件，更接近设计图。
3. 新增 `orderCreate.dailyQuantityPlaceholder`：
   - 中文：`请输入每日数量`
   - 英文：`Enter daily quantity`
4. 第 7 步关键词表格宽度限制为 `min(100%, 665px)`，避免在宽屏下被拉满。
5. “添加关键词”按钮改为 text 风格，降低视觉存在感，减少与设计图差异，同时保留多关键词业务能力。
6. 用户端和管理员端同步应用。

### 修改文件

1. `frontend/src/views/user/OrderCreateView.vue`
2. `frontend/src/views/admin/OrderCreateView.vue`
3. `frontend/src/i18n/locales/zh-CN.ts`
4. `frontend/src/i18n/locales/en-US.ts`
5. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：第一次出现 Vite/Rollup 输出路径偶发错误，重跑后通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. `rg -n "ordersPage.keyword|dailyQuantityPlaceholder|item.quantity|orderCreate.keywords|width: min" ...`：确认关键词表头、每日数量普通输入框、占位文案和表格宽度已接入。
3. 前端构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本次未处理。

### 下一步任务清单

1. 刷新创建订单页，确认第 7 步表头显示为“关键词”，每日数量为普通输入框。
2. 继续对照设计图检查第 7 步表格整体高度和按钮间距。

### 风险点

1. 每日数量仍绑定数字值并参与原有计算逻辑，但浏览器原生 number 输入可能在不同浏览器上有轻微样式差异。
2. 为保留多关键词能力，“添加关键词”仍存在，只是弱化为 text 按钮。

## 2026-06-20 更新：创建订单第 7 步国家地区占位文案修正

- 状态：已完成用户端和管理员端创建订单页第 7 步国家/地区占位文案修正，并通过前端生产构建验证。
- 当前任务：根据用户反馈，第 7 步地区关键词区域的 placeholder 也需要和“国家/地区”标签对应。
- 范围控制：仅修改前端创建订单页面模板和少量多语言文案；未修改 `.env` 或秘钥配置；未安装依赖；未删除文件；未修改后端接口、数据库结构、路由结构或项目架构。

### 实施内容

1. 新增创建订单专用文案 `orderCreate.selectCountryRegion`。
2. 中文显示为“请选择国家/地区”，英文显示为 `Select country/region`。
3. 用户端和管理员端第 7 步地区关键词区域的地区选择 placeholder 改用该文案。
4. 普通订单参数中的地区选择 placeholder 仍保留原 `applications.selectRegion`。

### 修改文件

1. `frontend/src/views/user/OrderCreateView.vue`
2. `frontend/src/views/admin/OrderCreateView.vue`
3. `frontend/src/i18n/locales/zh-CN.ts`
4. `frontend/src/i18n/locales/en-US.ts`
5. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. `rg -n "selectCountryRegion|selectRegion" ...`：确认第 7 步使用 `orderCreate.selectCountryRegion`，普通地区选择仍使用 `applications.selectRegion`。
3. 前端构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本次未处理。

### 下一步任务清单

1. 刷新创建订单页，确认第 7 步 placeholder 显示为“请选择国家/地区”。
2. 继续对照设计图检查第 7 步输入框宽度、表格高度和按钮间距。

### 风险点

1. 本次仅改占位文案，不改变地区选择和订单提交逻辑。

## 2026-06-20 更新：创建订单第 7 步国家地区标签修正

- 状态：已完成用户端和管理员端创建订单页第 7 步标签文案修正，并通过前端生产构建验证。
- 当前任务：根据用户截图反馈，第 7 步地区关键词区域左侧标签应显示“国家/地区”，而不是“地区”。
- 范围控制：仅修改前端创建订单页面模板和少量多语言文案；未修改 `.env` 或秘钥配置；未安装依赖；未删除文件；未修改后端接口、数据库结构、路由结构或项目架构。

### 实施内容

1. 新增创建订单专用文案 `orderCreate.countryRegion`。
2. 中文显示为“国家/地区”，英文显示为 `Country/Region`。
3. 用户端和管理员端第 7 步地区关键词区域的地区选择标签改用该文案。
4. 普通订单参数中的地区字段仍保留原 `applications.region`，避免影响其它表单语义。

### 修改文件

1. `frontend/src/views/user/OrderCreateView.vue`
2. `frontend/src/views/admin/OrderCreateView.vue`
3. `frontend/src/i18n/locales/zh-CN.ts`
4. `frontend/src/i18n/locales/en-US.ts`
5. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. `rg -n "countryRegion|applications\\.region" ...`：确认第 7 步使用 `orderCreate.countryRegion`，普通地区字段仍使用 `applications.region`。
3. 前端构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本次未处理。

### 下一步任务清单

1. 刷新创建订单页，确认第 7 步显示为“国家/地区”。
2. 继续对照设计图检查第 7 步输入框宽度、表格高度和按钮间距。

### 风险点

1. 本次仅改标签文案，不改变地区选择、关键词数量和提交逻辑。

## 2026-06-20 更新：创建订单第 7 步操作区精准修正

- 状态：已完成用户端和管理员端创建订单页第 7 步地区关键词操作区修正，并通过前端生产构建验证。
- 当前任务：根据用户截图反馈，第 7 步“操作/删除地区”区域与设计图差异明显，删除按钮被撑成横向长条，需要精准还原为小红色按钮。
- 范围控制：仅修改前端创建订单页面模板和样式；未修改 `.env` 或秘钥配置；未安装依赖；未删除文件；未修改后端接口、数据库结构、路由结构或项目架构。

### 实施内容

1. 在地区关键词配置行中恢复“操作 + 删除地区”区域。
2. 将删除地区按钮改为固定宽度小按钮，避免被 Element Plus 布局撑满。
3. 地区选择行调整为固定列宽：
   - 国家/地区标签 80px
   - 地区选择控件 220px
4. 操作区域调整为固定列宽：
   - 操作标签 42px
   - 删除地区按钮 78px
5. 关键词表格表头补齐右侧空白列，使竖向分割线更接近设计图。
6. 用户端和管理员端同步应用该样式。

### 修改文件

1. `frontend/src/views/user/OrderCreateView.vue`
2. `frontend/src/views/admin/OrderCreateView.vue`
3. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. `rg -n "region-actions|remove-region-button|keyword-table-head|grid-template-columns: 300px auto|grid-template-columns: 42px 78px|<span></span>" ...`：确认操作区、小红按钮、表头空白列和固定列宽已接入。
3. 前端构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本次未处理。

### 下一步任务清单

1. 刷新创建订单页，确认“删除地区”不再横向撑满，而是显示为设计图中的小红按钮。
2. 继续对照设计图微调第 7 步的表格高度、输入框宽度和按钮间距。

### 风险点

1. 固定列宽更贴近设计图，但在很窄屏幕下仍依赖已有移动端媒体查询切换为单列布局。

## 2026-06-20 更新：创建订单页按设计图细节还原

- 状态：已完成用户端和管理员端创建订单页主要视觉细节还原，并通过前端生产构建验证。
- 当前任务：根据用户反馈，当前创建订单页与设计图在图标、宽度比例、元素尺寸等细节上仍存在差异，需要在保留标题区域移除的前提下尽量还原设计图。
- 范围控制：仅修改前端创建订单页面模板和样式；未修改 `.env` 或秘钥配置；未安装依赖；未删除文件；未修改后端接口、数据库结构、路由结构或项目架构。

### 实施内容

1. 服务类型图标替换为更接近设计图的组合：
   - 关键词安装：搜索图标
   - 下载量：下载图标
   - 评分：星标图标
   - 评论：评论气泡图标
   - 关键词保排名：锁定图标
   - 关键词覆盖：网格图标
2. 应用商店分段控件增加视觉图标：
   - App Store 使用蓝色应用图标
   - Google Play 使用三角形品牌化图标
   - iPad Store 使用设备图标
3. 管理员端应用选择旁新增“添加应用”按钮，并跳转到管理员应用管理页。
4. 用户端应用选择旁新增“添加应用”按钮，并跳转到用户应用管理页。
5. 主表单与摘要区域比例调整为更接近设计图：
   - 右侧摘要栏收窄为 260px
   - 主表单与摘要间距调整为 18px
   - 卡片圆角和阴影减轻
6. 表单控件细节调整：
   - 服务类型和商店分段高度压缩到接近设计图
   - 步骤标题字号压小
   - 用户/应用/时间等控件宽度收敛到 310px 左右
7. 地区关键词表格列宽按设计图改为：
   - 关键词列 180px
   - 每日数量列 180px
   - 操作列 64px
   - 右侧保留空白弹性列

### 修改文件

1. `frontend/src/views/user/OrderCreateView.vue`
2. `frontend/src/views/admin/OrderCreateView.vue`
3. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. `rg -n "ChatDotRound|Grid|Iphone|Lock|app-picker-line|store-option|google-play-icon|grid-template-columns: 180px 180px 64px|304px|height: 44px" ...`：确认新图标、商店图标、应用选择行、表格列宽已接入，旧的 304px 摘要宽度和 44px 分段高度已无残留。
3. 前端构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本次未处理。

### 下一步任务清单

1. 在真实页面刷新管理员端创建订单页，与设计图继续比较首屏高度、表格宽度和摘要卡位置。
2. 如果需要更进一步还原，可继续微调顶部内容区内边距、底部提交条位置和图标视觉细节。

### 风险点

1. Google Play 图标为 CSS 近似绘制，不是官方品牌图片。
2. “添加应用”按钮跳转到应用管理页，没有打开参考图中可能暗示的弹窗式新增应用。

## 2026-06-20 更新：创建订单页再次移除标题区域

- 状态：已完成用户端和管理员端创建订单页内容标题区域移除，并通过前端生产构建验证。
- 当前任务：根据用户反馈，创建订单页顶部标题区域不需要展示，应直接进入订单创建表单。
- 范围控制：仅修改前端创建订单页面模板和样式；未修改 `.env` 或秘钥配置；未安装依赖；未删除文件；未修改后端接口、数据库结构、路由结构或项目架构。

### 实施内容

1. 管理员端创建订单页移除 `.order-titlebar`，包括面包屑、标题和说明。
2. 用户端创建订单页移除 `.order-titlebar`，包括面包屑、标题和说明。
3. 清理两个页面中 `.order-titlebar`、`.breadcrumb` 相关样式。
4. 保留创建订单主表单、右侧订单摘要、底部提交条和地区关键词区域布局。

### 修改文件

1. `frontend/src/views/user/OrderCreateView.vue`
2. `frontend/src/views/admin/OrderCreateView.vue`
3. `PROJECT_PROGRESS.md`

### 验证结果

1. `rg -n "order-titlebar|breadcrumb|adminSubtitle|promotionPage\\.descriptions\\.KEYWORD_INSTALL" frontend/src/views/admin/OrderCreateView.vue frontend/src/views/user/OrderCreateView.vue`：无匹配结果，确认标题区域引用已移除。
2. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
3. 前端构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本次未处理。

### 下一步任务清单

1. 刷新用户端和管理员端创建订单页，确认首屏直接进入表单区域。
2. 继续按用户截图检查创建订单页面局部视觉差异。

### 风险点

1. 移除页面内标题后，当前位置提示主要依赖侧栏菜单和路由。

## 2026-06-20 更新：创建订单地区关键词区域对齐参考图

- 状态：已完成用户端和管理员端创建订单页地区关键词区域局部对齐，并通过前端生产构建验证。
- 当前任务：根据用户截图反馈，地区关键词区域与参考图不一致，需要调整标题、工具按钮和添加地区配置按钮位置。
- 范围控制：仅修改前端创建订单页面模板、样式和少量多语言文案；未修改 `.env` 或秘钥配置；未安装依赖；未删除文件；未修改后端接口、数据库结构、路由结构或项目架构。

### 实施内容

1. 将关键词安装参数区标题从“地区配置”调整为“地区关键词”。
2. 在标题右侧新增参考图样式的“下载模板”和“批量导入”视觉按钮。
3. 将“添加地区配置”按钮从标题右侧移动到地区配置表格下方。
4. 用户端和管理员端创建订单页同步应用该地区关键词区域布局。
5. 新增中英文文案：
   - `orderCreate.regionKeywords`
   - `orderCreate.downloadTemplate`
   - `orderCreate.batchImport`
   - `orderCreate.addRegionConfig`

### 修改文件

1. `frontend/src/views/user/OrderCreateView.vue`
2. `frontend/src/views/admin/OrderCreateView.vue`
3. `frontend/src/i18n/locales/zh-CN.ts`
4. `frontend/src/i18n/locales/en-US.ts`
5. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. `rg -n "regionKeywords|downloadTemplate|batchImport|addRegionConfig|region-tool-buttons|add-region-button" ...`：确认地区关键词标题、工具按钮、底部添加地区按钮和多语言文案已接入。
3. 前端构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本次未处理。

### 下一步任务清单

1. 在真实页面刷新创建订单页，确认地区关键词区域的按钮位置、间距和表格宽度与参考图一致。
2. 如果需要真实可用的模板下载和批量导入，需要单独确认导入格式、错误校验和后端处理方式后再实现。
3. 继续根据页面截图做局部视觉差异调整。

### 风险点

1. 本次“下载模板”和“批量导入”仅为视觉入口，未绑定真实下载或导入逻辑。
2. 为保留现有多关键词能力，页面仍保留“添加关键词”按钮，可能与参考图单行示例存在轻微差异。

## 2026-06-20 更新：创建订单区域重新对齐参考图流程表单

- 状态：已完成用户端和管理员端创建订单区域重排，并通过前端生产构建验证。
- 当前任务：根据用户反馈，创建订单主表单区域与参考设计图差异较大，需要按设计图的流程表单、右侧摘要和底部提交方式重新调整。
- 范围控制：仅修改前端创建订单页面模板、样式和少量多语言文案；未修改 `.env` 或秘钥配置；未安装依赖；未删除文件；未修改后端接口、数据库结构、路由结构或项目架构。

### 实施内容

1. 管理员端创建订单页恢复轻量标题区域，包含面包屑、标题和说明，更贴近参考图首屏结构。
2. 管理员端创建订单主表单由原来的 3 个大区块重排为流程步骤：
   - 选择服务类型
   - 选择应用商店
   - 用户名
   - 应用
   - 订单时间
   - 执行小时
   - 地区关键词
3. 用户端创建订单页同步重排为同风格流程步骤，但不显示管理员专属用户选择项。
4. 订单时间旁新增 `System Time (UTC+8)` 动态时间展示，使用前端本地时间按东八区格式化。
5. 关键词安装的地区配置从卡片式编辑改为参考图风格：
   - 地区选择与删除地区操作在同一行。
   - 关键词、每日数量、操作改为表格结构。
   - 保留一个订单内多个地区、每个地区多个关键词和数量的现有业务能力。
6. 执行小时从数字步进器调整为下拉选择 1h-8h，更接近参考图的选择控件。
7. 新增中英文文案 `orderCreate.dailyQuantity`，用于关键词表格的“每日数量”列。

### 修改文件

1. `frontend/src/views/user/OrderCreateView.vue`
2. `frontend/src/views/admin/OrderCreateView.vue`
3. `frontend/src/i18n/locales/zh-CN.ts`
4. `frontend/src/i18n/locales/en-US.ts`
5. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. `rg -n "order-titlebar|step-field-grid|systemTimeText|keyword-table|dailyQuantity|section-heading-actions" ...`：确认标题栏、流程网格、系统时间、关键词表格和多语言文案已接入。
3. 前端构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本次未处理。

### 下一步任务清单

1. 在真实页面中刷新管理员端创建订单页，确认主表单区域是否已接近参考图的排列和密度。
2. 在真实页面中刷新用户端创建订单页，确认无管理员用户字段且流程布局自然。
3. 如需进一步还原参考图，可继续补充“添加应用”按钮、“下载模板/批量导入”按钮和“明细”弹窗功能。

### 风险点

1. `System Time (UTC+8)` 使用前端本地时间格式化为东八区展示，不代表服务器时间。
2. 本次仍未实现模板下载、批量导入、添加应用快捷弹窗或明细弹窗，仅完成创建区域视觉和结构对齐。

## 2026-06-20 更新：创建订单页移除内容标题区域

- 状态：已完成用户端和管理员端创建订单页标题区域移除，并通过前端生产构建验证。
- 当前任务：根据用户反馈，创建订单页不再展示页面内面包屑、标题和说明区域，让表单工作区直接进入主内容。
- 范围控制：仅修改前端创建订单页面模板和样式；未修改 `.env` 或秘钥配置；未安装依赖；未删除文件；未修改后端接口、数据库结构、路由结构或项目架构。

### 实施内容

1. 管理员端创建订单页移除 `.order-intro` 区域，包括面包屑、标题和说明文案。
2. 用户端创建订单页同步移除 `.order-intro` 区域，包括面包屑、标题和说明文案。
3. 清理两个页面中不再使用的 `.order-intro`、`.breadcrumb` 相关样式。
4. 保留现有左侧订单表单、右侧订单摘要和底部提交条，不改变订单创建逻辑。

### 修改文件

1. `frontend/src/views/user/OrderCreateView.vue`
2. `frontend/src/views/admin/OrderCreateView.vue`
3. `PROJECT_PROGRESS.md`

### 验证结果

1. `rg -n "order-intro|breadcrumb|adminSubtitle|promotionPage\\.descriptions\\.KEYWORD_INSTALL" frontend/src/views/admin/OrderCreateView.vue frontend/src/views/user/OrderCreateView.vue`：无匹配结果，确认页面标题区域相关引用已移除。
2. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
3. 前端构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本次未处理。

### 下一步任务清单

1. 刷新用户端和管理员端创建订单页，确认首屏内容不再被标题区占用。
2. 继续检查创建订单页在不同服务类型下的表单密度、摘要卡高度和底部提交条是否自然。
3. 如需进一步压缩首屏高度，可继续针对服务类型、商店选择和地区关键词表格做局部密度优化。

### 风险点

1. 去掉页面内标题后，当前位置提示主要依赖侧栏菜单和浏览器路由；如果后续需要更强位置感，可在全局顶部栏统一处理。
2. 本次未实现“明细”弹窗、模板下载、批量导入或系统时间展示功能。

## 2026-06-20 更新：创建订单页按参考图重做视觉结构

- 状态：已完成用户端和管理员端创建订单页参考图视觉结构实现，并通过前端生产构建验证。
- 当前任务：参考用户提供的设计图实现创建订单页面，避免中间区域模式，形成产品级后台下单工作台。
- 范围控制：仅修改前端创建订单页面模板、样式和少量多语言文案；未修改 `.env` 或秘钥配置；未安装依赖；未删除文件；未修改后端接口、数据库结构、路由结构或项目架构。

### 实施内容

1. 管理员端创建订单页按参考图改为：
   - 顶部轻量面包屑和说明。
   - 左侧大表单面板。
   - 右侧订单摘要卡片。
   - 底部合计、明细和提交订单操作条。
2. 用户端创建订单页同步为相同视觉结构，隐藏管理员专属的用户选择项。
3. 服务类型从普通 tab 调整为参考图风格的横向分段按钮组，当前服务高亮为蓝色。
4. 应用商店选择调整为三段式按钮组，选中态与参考图保持一致。
5. 表单分区标题增加 `* 1.`、`* 2.`、`* 3.` 编号样式，贴近参考图的步骤表达。
6. 右侧订单摘要增加服务类型、应用商店、用户/应用、天数、数量、执行小时等关键字段，并新增计费说明区。
7. 底部提交区新增合计金额、明细入口和提交按钮，按钮风格改为蓝色主操作。
8. 背景调整为浅蓝灰后台工作台视觉，主表单和摘要卡片使用白色卡片、轻阴影和 8px 圆角。
9. 新增中英文文案：
   - `orderCreate.total`
   - `orderCreate.details`
   - `orderCreate.billingNoteTitle`
   - `orderCreate.billingNote`

### 修改文件

1. `frontend/src/views/user/OrderCreateView.vue`
2. `frontend/src/views/admin/OrderCreateView.vue`
3. `frontend/src/i18n/locales/zh-CN.ts`
4. `frontend/src/i18n/locales/en-US.ts`
5. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. `rg -n "order-intro|order-action-bar|billing-note|billingNote|action-total|grid-template-columns: repeat\\(6" ...`：确认参考图结构和文案已接入用户端、管理员端和中英文语言包。
3. 前端构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本次未处理。

### 下一步任务清单

1. 刷新管理员端创建订单页，确认整体布局与参考图一致：左侧主表单、右侧摘要、底部提交条。
2. 刷新用户端创建订单页，确认视觉结构一致且不会显示管理员用户选择项。
3. 检查不同服务类型切换时，摘要金额、参数区域和底部提交按钮展示是否自然。
4. 如果后续要完全还原图中的“下载模板/批量导入/System Time”等细节，可作为单独功能继续补充。

### 风险点

1. 本次只按参考图重构视觉结构，没有新增模板下载、批量导入和系统时间展示功能。
2. 页面内新增的“明细”目前是视觉入口，未绑定弹窗或明细展开逻辑，后续可单独实现。

## 2026-06-20 更新：创建订单页面取消中间区域布局

- 状态：已完成用户端和管理员端创建订单页面去中间区域布局调整，并通过前端生产构建验证。
- 当前任务：不要中间区域模式的布局，创建订单页面应随后台主内容区域全宽展开。
- 范围控制：仅修改前端创建订单页面样式；未修改 `.env` 或秘钥配置；未安装依赖；未删除文件；未修改后端接口、数据库结构、路由结构或项目架构。

### 实施内容

1. 用户端创建订单页移除 `order-shell` 的 `max-width: 1280px` 和 `margin: 0 auto`，不再居中收窄。
2. 管理员端创建订单页移除 `order-shell` 的 `max-width: 1280px` 和 `margin: 0 auto`，改为占满主内容区宽度。
3. 用户端评分/评论数量区域移除局部 `max-width: 520px`，避免参数区继续呈现中间窄块。
4. 保留现有左右工作区和摘要栏结构，但整体不再是中间区域容器。

### 修改文件

1. `frontend/src/views/user/OrderCreateView.vue`
2. `frontend/src/views/admin/OrderCreateView.vue`
3. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. `rg -n "max-width: 1280px|margin: 0 auto|max-width: 520px" frontend/src/views/user/OrderCreateView.vue frontend/src/views/admin/OrderCreateView.vue`：无残留中间区域限制规则。
3. 前端构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本次未处理。

### 下一步任务清单

1. 刷新用户端创建订单页，确认内容从左侧主内容区开始铺开，不再居中收窄。
2. 刷新管理员端创建订单页，确认工作区占满后台内容宽度，右侧摘要栏仍正常固定。

### 风险点

1. 本次仅调整布局宽度规则，未修改订单创建业务逻辑。

## 2026-06-20 更新：创建订单页面产品级布局重构

- 状态：已完成用户端和管理员端创建订单页面布局重构，并通过前端生产构建验证。
- 当前任务：修复创建订单页面标题重复、大量空白、滚动条和不合理布局问题，使页面更符合产品级运营后台体验。
- 范围控制：仅修改前端创建订单页面模板与样式；未修改 `.env` 或秘钥配置；未安装依赖；未删除文件；未修改后端接口、数据库结构、路由结构或项目架构。

### 实施内容

1. 管理员端创建订单页面移除内部 `page-header`，避免与外层布局标题重复。
2. 用户端创建订单页面从“上方表单 + 下方摘要条”改为统一工作台布局：
   - 顶部服务类型 tab。
   - 左侧目标信息和订单参数。
   - 右侧订单摘要与提交按钮。
3. 管理员端创建订单页面压缩原有三段卡片和右侧摘要栏间距，取消步骤圆点装饰，降低页面视觉噪音。
4. 两端统一表单栅格、摘要栏、地区配置、关键词行和移动端响应式规则。
5. 关键词安装的多地区配置保留完整功能，但减少卡片 padding、缩小数量输入列和操作列，降低无效空白。
6. 右侧摘要栏保留 sticky 行为，窄屏自动下移，避免挤压主表单。

### 修改文件

1. `frontend/src/views/user/OrderCreateView.vue`
2. `frontend/src/views/admin/OrderCreateView.vue`
3. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. `rg -n "page-header|summary-row|eyebrow|flow-section span|orderCreate.adminSubtitle|adminEyebrow" frontend/src/views/user/OrderCreateView.vue frontend/src/views/admin/OrderCreateView.vue`：无残留内部大标题和旧摘要条引用。
3. 尝试使用浏览器插件连接本地页面进行视觉检查时，当前运行环境返回 `sandboxPolicy` 元数据缺失错误，未能完成浏览器截图验证；已完成构建验证和静态布局复核。
4. 前端构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本次未处理。

### 下一步任务清单

1. 刷新用户端创建订单页，确认页面只保留外层标题，首屏没有异常大空白。
2. 刷新管理员端创建订单页，确认不再出现重复标题，表单和摘要区域比例正常。
3. 分别在桌面和窄屏宽度下检查关键词安装多地区配置，确认不会出现横向滚动条。
4. 如仍有截图中的具体异常位置，按截图继续做局部视觉修正。

### 风险点

1. 本次没有修改订单创建业务逻辑，仅调整布局和样式；交互功能应保持不变。
2. 浏览器自动截图验证受当前工具环境限制未完成，真实页面仍建议刷新后人工确认一次首屏视觉效果。

## 2026-06-20 更新：关键词安装单订单多地区明细

- 状态：已完成关键词安装订单在同一张订单内配置多个国家/地区和关键词数量，并通过后端完整测试与前端生产构建验证。
- 当前任务：创建订单时先选择国家或地区，再添加关键词和数量，并可继续添加第二个国家或地区、关键词和数量；多个地区必须落在同一个订单里。
- 范围控制：本次已按用户确认新增小范围数据库迁移；未修改 `.env` 或秘钥配置；未安装依赖；未删除文件；未大改路由或项目架构。

### 实施内容

1. 新增 Flyway V11 迁移，为 `aso_order_item` 增加 `region_code` 字段和索引，用于保存每条订单明细所属国家/地区。
2. 后端 `CreateOrderCommand.KeywordQuantity` 扩展 `regionCode` 字段，并保留旧的 `keyword + quantity` 构造方式兼容既有测试和旧请求。
3. 关键词安装计价改为按 `地区 + 关键词` 合并数量：
   - 同一地区同一关键词会合并数量。
   - 不同地区同一关键词会保存为不同明细。
   - 总价仍按所有地区关键词数量合计乘关键词安装单价。
4. 同一关键词安装订单包含多个地区时，订单主表 `region_code` 标记为 `MULTI`，具体地区保存在订单明细。
5. 后端地区校验改为逐条校验关键词明细里的 `regionCode` 是否属于该应用已关联地区。
6. 订单地区筛选支持命中多地区订单明细地区，即筛选某个地区时可查到 `MULTI` 主订单中包含该地区明细的订单。
7. 用户端和管理员端创建订单页：
   - 关键词安装时隐藏全局地区选择。
   - 改为“地区配置”分组。
   - 每个分组选择一个国家/地区。
   - 每个分组下可添加多个关键词和数量。
   - 可添加/删除地区分组。
8. 下载量、评分、评论、特殊订单仍保持原有单地区流程。
9. 中英文多语言新增地区分组相关文案。

### 修改文件

1. `backend/src/main/resources/db/migration/V11__order_item_region_code.sql`
2. `backend/src/main/java/com/youou/aso/modules/order/domain/OrderItem.java`
3. `backend/src/main/java/com/youou/aso/modules/order/dto/CreateOrderCommand.java`
4. `backend/src/main/java/com/youou/aso/modules/order/dto/OrderItemResult.java`
5. `backend/src/main/java/com/youou/aso/modules/order/repository/JdbcOrderItemRepository.java`
6. `backend/src/main/java/com/youou/aso/modules/order/repository/JdbcOrderRepository.java`
7. `backend/src/main/java/com/youou/aso/modules/order/service/OrderService.java`
8. `backend/src/test/java/com/youou/aso/modules/order/service/OrderServiceTest.java`
9. `frontend/src/api/orders.ts`
10. `frontend/src/views/user/OrderCreateView.vue`
11. `frontend/src/views/admin/OrderCreateView.vue`
12. `frontend/src/i18n/locales/zh-CN.ts`
13. `frontend/src/i18n/locales/en-US.ts`
14. `PROJECT_PROGRESS.md`

### 验证结果

1. 先新增后端测试 `createKeywordInstallOrderKeepsRegionPerKeywordItemInOneOrder` 并运行：
   - 首次执行失败，失败原因为 `KeywordQuantity` 不支持第三个 `regionCode` 参数，符合 RED 预期。
2. `mvn.cmd "-Dtest=OrderServiceTest#createKeywordInstallOrderKeepsRegionPerKeywordItemInOneOrder" test`：通过，确认同一订单保存 US/JP 多地区关键词明细。
3. `mvn.cmd "-Dtest=OrderServiceTest" test`：通过，18 个订单服务测试成功。
4. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
5. `mvn.cmd test`：通过，67 个后端测试成功；Flyway 成功校验 11 个迁移，并把本地 schema 从 V10 升级到 V11。
6. 前端构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本次未处理。

### 下一步任务清单

1. 打开用户端创建订单页，选择关键词安装，确认可添加多个地区配置和各自关键词数量。
2. 打开管理员端创建订单页，确认管理员代用户创建关键词安装订单时同样可配置多个地区。
3. 提交一个包含两个地区的关键词安装订单，确认订单列表显示为一张订单，主地区为 `MULTI`，明细中保留各地区。
4. 使用地区筛选查询多地区订单，确认筛选某个明细地区时该订单能被查到。

### 风险点

1. 多地区订单主表 `region_code` 使用 `MULTI` 标记，现有列表如果直接展示地区字段会看到 `MULTI`；后续可按产品需要显示为“多地区”。
2. 本次只为关键词安装支持单订单多地区明细；下载量、评分、评论和特殊订单仍为单地区流程。
3. 本地数据库已由 Flyway 自动升级到 V11，其他环境部署时需要正常执行迁移。

## 2026-06-20 更新：应用列表移除创建时间列

- 状态：已完成用户端和管理员端应用列表创建时间列移除，并通过前端生产构建验证。
- 当前任务：创建时间字段不要了，应用列表只保留业务信息列和操作列。
- 范围控制：仅修改前端应用列表展示和对应应用管理语言包；未修改 `.env` 或秘钥配置；未安装依赖；未删除文件；未修改数据库结构、后端接口、路由结构或项目架构。

### 实施内容

1. 用户端应用管理列表移除“创建时间”列。
2. 管理员端应用管理列表移除“创建时间”列。
3. 保留用户端和管理员端“操作”列，继续显示创建订单和删除图标。
4. 移除应用管理模块中不再使用的 `applications.createdAt` 中英文文案。
5. 保留接口类型中的 `createdAt` 字段，避免影响后端返回兼容和其他潜在调用。

### 修改文件

1. `frontend/src/views/user/ApplicationManagementView.vue`
2. `frontend/src/views/admin/ApplicationManagementView.vue`
3. `frontend/src/i18n/locales/zh-CN.ts`
4. `frontend/src/i18n/locales/en-US.ts`
5. `PROJECT_PROGRESS.md`

### 验证结果

1. `rg -n "applications.createdAt" frontend/src`：无残留引用。
2. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
3. 前端构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本次未处理。

### 下一步任务清单

1. 刷新用户端应用管理页面，确认不再显示创建时间列，操作列仍可创建订单和删除。
2. 刷新管理员端应用管理页面，确认不再显示创建时间列，操作列仍可创建订单和删除。

### 风险点

1. 本次只移除列表展示列，后端仍会返回 `createdAt`，不会影响历史数据和接口兼容。

## 2026-06-20 更新：应用列表创建时间与操作列调整

- 状态：已完成应用列表字段与操作列调整，并通过后端完整测试和前端生产构建验证。
- 当前任务：将应用列表中的校验时间改为创建时间；补齐操作列名，操作列包含创建订单和删除图标。
- 范围控制：仅修改应用管理列表展示、管理员应用软删除接口和对应前端调用；未修改 `.env` 或秘钥配置；未安装依赖；未删除文件；未修改数据库结构、路由结构或项目架构。

### 实施内容

1. 用户端应用列表新增“创建时间”列，并保留操作列中的创建订单和删除图标。
2. 管理员端应用列表将原“校验时间”列改为“创建时间”列。
3. 管理员端应用列表新增“操作”列，提供创建订单和删除图标操作。
4. 管理员删除应用改为后端禁用应用记录，不做物理删除，避免影响历史订单和审计数据。
5. 补充中英文多语言文案 `applications.createdAt`。

### 修改文件

1. `backend/src/main/java/com/youou/aso/modules/appmanagement/api/AdminCustomerAppController.java`
2. `backend/src/main/java/com/youou/aso/modules/appmanagement/service/CustomerAppService.java`
3. `frontend/src/api/applications.ts`
4. `frontend/src/views/user/ApplicationManagementView.vue`
5. `frontend/src/views/admin/ApplicationManagementView.vue`
6. `frontend/src/i18n/locales/zh-CN.ts`
7. `frontend/src/i18n/locales/en-US.ts`
8. `PROJECT_PROGRESS.md`

### 验证结果

1. `mvn.cmd "-Dtest=CustomerAppServiceTest" test`：通过，8 个应用管理服务测试成功。
2. `mvn.cmd test`：通过，66 个后端测试成功。
3. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
4. 前端构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本次未处理。

### 下一步任务清单

1. 刷新用户端应用管理页面，确认列名显示为“创建时间”，操作列展示创建订单和删除图标。
2. 刷新管理员端应用管理页面，确认用户信息、创建时间和操作列展示正常。
3. 在管理员端点击创建订单图标，确认进入创建订单页并带入应用、用户和商店信息。
4. 在管理员端点击删除图标，确认应用从列表移除且历史订单不受影响。

### 风险点

1. 管理员删除目前采用禁用语义，接口路径为 `DELETE /api/admin/apps/{id}`，前端表现为删除，但数据不会物理删除；这是为了保护历史订单和审计数据。
2. 本次未新增管理员删除控制器专项测试，但已通过完整后端测试和编译；后续如果继续强化应用管理权限测试，可补充该接口的 MockMvc 用例。

## 2026-06-20 更新：用户信息显示去除 ID

- 状态：已完成管理员端用户信息展示调整，并通过前端构建验证。
- 当前任务：用户信息显示用户名和邮箱，不显示用户 ID。
- 范围控制：仅修改前端展示文案；未修改接口、后端逻辑、数据库结构、路由、`.env` 或秘钥配置；未安装依赖，未删除文件。

### 实施内容

1. 管理员应用列表用户列改为显示用户名和邮箱，缺失时显示 `-`，不再使用 `ID` 兜底。
2. 管理员创建订单页用户下拉、摘要和确认弹窗统一显示 `用户名 / 邮箱`，不再显示 `用户名（ID xxx）`。

### 修改文件

1. `frontend/src/views/admin/ApplicationManagementView.vue`
2. `frontend/src/views/admin/OrderCreateView.vue`
3. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. 构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本次未处理。

### 下一步任务清单

1. 刷新管理员应用列表，确认用户列只显示用户名和邮箱。
2. 打开管理员创建订单页，确认用户下拉、摘要和确认弹窗不再显示 ID。

### 风险点

1. 如果某条应用数据缺少用户名或邮箱，前端会显示 `-`；需要确认后端数据完整性时可单独检查客户账号数据。

## 2026-06-20 更新：应用与地区关联拆分

- 状态：已完成应用主体与地区关联拆分、应用唯一约束调整、管理员应用列表用户信息展示，并通过后端测试与前端构建验证。
- 当前任务：应用管理中把应用和地区的关联分开，以用户、应用和商店作为唯一约束；应用列表不显示地区；管理员端应用列表显示用户名字。
- 范围控制：本次经用户确认后修改数据库迁移和相关业务逻辑；未修改 `.env` 或秘钥配置；未安装依赖，未删除文件。

### 实施内容

1. 新增 Flyway V10 迁移 `customer_app_region` 关联表，用于保存应用和地区的多对多关联。
2. 迁移现有 `customer_app.region_code` 到 `customer_app_region`：
   - 历史同一用户、同一商店、同一应用在多个地区的活动重复记录会合并到最小 `id` 的应用主体。
   - 重复活动应用主体会被置为 `DISABLED`，避免新唯一约束冲突。
3. `customer_app` 活动唯一键调整为：
   - `customer_id`
   - `store_type`
   - `active_app_identifier`
4. 后端应用创建逻辑调整：
   - 应用不存在时创建应用主体并写入地区关联。
   - 应用已存在但地区未关联时，不新增应用主体，只追加地区关联。
   - 应用已存在且地区已关联时，返回 `APP_ALREADY_EXISTS`。
5. 应用列表接口结果新增：
   - `regionCodes`
   - `customerUsername`
   - `customerEmail`
6. 管理员应用列表通过关联 `customer_account` 返回用户信息，前端新增用户列。
7. 用户端和管理员端应用列表移除地区筛选和地区列，列表只展示应用主体。
8. 用户端和管理员端订单创建页改为：
   - 先选应用。
   - 再从该应用关联的 `regionCodes` 中选择下单地区。
   - 普通订单和特殊审核提交都会携带 `regionCode`。
9. 后端普通订单和特殊审核提交会校验选择地区是否属于该应用关联地区，并将选择地区写入订单/审核记录。
10. 新增服务测试覆盖同一应用追加不同地区时不创建重复应用主体。

### 修改文件

1. `backend/src/main/resources/db/migration/V10__customer_app_region_relation.sql`
2. `backend/src/main/java/com/youou/aso/modules/appmanagement/domain/CustomerApp.java`
3. `backend/src/main/java/com/youou/aso/modules/appmanagement/dto/CustomerAppResult.java`
4. `backend/src/main/java/com/youou/aso/modules/appmanagement/repository/CustomerAppRepository.java`
5. `backend/src/main/java/com/youou/aso/modules/appmanagement/repository/JdbcCustomerAppRepository.java`
6. `backend/src/main/java/com/youou/aso/modules/appmanagement/service/CustomerAppService.java`
7. `backend/src/main/java/com/youou/aso/modules/order/dto/CreateOrderCommand.java`
8. `backend/src/main/java/com/youou/aso/modules/order/dto/SubmitSpecialAuditCommand.java`
9. `backend/src/main/java/com/youou/aso/modules/order/api/CustomerOrderController.java`
10. `backend/src/main/java/com/youou/aso/modules/order/api/AdminOrderController.java`
11. `backend/src/main/java/com/youou/aso/modules/order/api/CustomerSpecialOrderAuditController.java`
12. `backend/src/main/java/com/youou/aso/modules/order/service/OrderService.java`
13. `backend/src/main/java/com/youou/aso/modules/order/service/SpecialOrderAuditService.java`
14. `backend/src/test/java/com/youou/aso/modules/appmanagement/service/CustomerAppServiceTest.java`
15. `frontend/src/api/applications.ts`
16. `frontend/src/api/orders.ts`
17. `frontend/src/api/specialOrderAudits.ts`
18. `frontend/src/views/user/ApplicationManagementView.vue`
19. `frontend/src/views/admin/ApplicationManagementView.vue`
20. `frontend/src/views/user/OrderCreateView.vue`
21. `frontend/src/views/admin/OrderCreateView.vue`
22. `PROJECT_PROGRESS.md`

### 验证结果

1. 先新增应用服务测试并运行 `mvn.cmd "-Dtest=CustomerAppServiceTest" test`：首次失败，失败原因为 `CustomerAppResult.regionCodes()` 不存在，符合 RED 预期。
2. `mvn.cmd "-Dtest=CustomerAppServiceTest" test`：通过，8 个测试成功。
3. `mvn.cmd "-Dtest=CustomerAppServiceTest,OrderServiceTest,SpecialOrderAuditServiceTest,AdminOrderControllerTest,CustomerSpecialOrderAuditControllerTest" test`：通过，36 个测试成功。
4. `mvn.cmd test`：通过，66 个后端测试成功；Flyway 成功校验 10 个迁移，并把本地 schema 从 V9 升级到 V10。
5. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
6. 构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本次未处理。

### 下一步任务清单

1. 重启后端服务后刷新用户端应用管理页，确认应用列表不再显示地区，重复添加同一应用不同地区时只追加地区关联。
2. 刷新管理员应用列表，确认显示用户列，并且不再显示地区列。
3. 打开用户端和管理员端订单创建页，选择应用后确认地区下拉来自该应用已关联地区。
4. 使用同一应用关联两个地区后分别创建订单，确认订单列表中的订单地区为创建时选择的地区。

### 风险点

1. V10 迁移会将历史同一用户、同一商店、同一应用的多地区重复活动应用合并为一个活动应用主体，并把其他重复主体置为 `DISABLED`；历史订单仍保留原 `customer_app_id`，不会被删除。
2. `customer_app.region_code` 暂时保留为主地区/兼容字段，真正的多地区关系以 `customer_app_region` 为准；后续如要彻底移除主地区字段，需要单独评估订单、审核和导出依赖。
3. 本次未做浏览器截图验证，已通过后端测试和前端构建，真实页面仍建议刷新后确认交互。

## 2026-06-20 更新：侧边栏一级菜单互斥展开

- 状态：已完成用户端和管理员端侧边栏一级菜单展开行为调整，并通过前端构建验证。
- 当前任务：一个一级菜单展开时，其他一级菜单自动收缩。
- 范围控制：仅修改前端布局菜单属性；未修改路由、菜单结构、后端接口、数据库结构、`.env` 或秘钥配置；未安装依赖，未删除文件。

### 实施内容

1. 管理员端侧边栏 `el-menu` 增加 `unique-opened` 属性。
2. 用户端侧边栏 `el-menu` 增加 `unique-opened` 属性。
3. 保持原有 `router`、`default-active`、菜单项和权限判断不变。

### 修改文件

1. `frontend/src/layouts/AdminLayout.vue`
2. `frontend/src/layouts/UserLayout.vue`
3. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. 构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本次未处理。

### 下一步任务清单

1. 刷新管理员端侧边栏，依次展开“订单执行”“订单”“系统管理”，确认每次只保留一个一级菜单展开。
2. 刷新用户端侧边栏，展开“订单”后再点击其他可展开菜单时确认互斥行为正常。

### 风险点

1. 当前用户端只有一个二级菜单，“互斥展开”主要在管理员端更明显；用户端同步配置是为了保持布局行为一致。

## 2026-06-20 更新：关键词安装订单改为关键词数量明细

- 状态：已完成后端参数模型、计价逻辑、用户端/管理员端创建页表单调整，并通过后端全量测试与前端构建验证。
- 当前任务：修正关键词安装订单参数，改为“添加关键词”，并允许每个关键词单独填写数量。
- 范围控制：仅修改订单创建参数、关键词安装计价和订单创建页面；未修改数据库结构、路由、`.env` 或秘钥配置；未安装依赖，未删除文件。

### 实施内容

1. 后端 `CreateOrderCommand` 新增 `keywordItems` 参数，结构为关键词和数量；保留旧 `keywords` 参数兼容已有调用。
2. 客户端和管理员端创建订单接口请求体新增 `keywordItems`，提交时传入订单服务。
3. 关键词安装计价改为按每个关键词数量汇总：
   - 订单总数量 = 所有关键词数量之和。
   - 订单总金额 = 所有关键词数量之和 * 关键词安装单价。
   - 订单明细按关键词分别保存 `itemName`、`quantity`、`unitPrice`、`amount`。
4. 后端对空关键词、空数量和非正数量做过滤，重复关键词会合并数量；旧 `keywords` 入参仍按每个关键词数量 1 处理。
5. 用户端和管理员端订单创建页将关键词文本框改为可增删的关键词行，每行包含关键词输入框和数量输入框。
6. 前端预估数量与提交 payload 同步改为使用 `keywordItems`。
7. 补充中英文文案：添加关键词、关键词输入占位文案。
8. 新增订单服务测试，覆盖关键词安装按不同关键词数量计费和生成明细。

### 修改文件

1. `backend/src/main/java/com/youou/aso/modules/order/dto/CreateOrderCommand.java`
2. `backend/src/main/java/com/youou/aso/modules/order/service/OrderService.java`
3. `backend/src/main/java/com/youou/aso/modules/order/api/CustomerOrderController.java`
4. `backend/src/main/java/com/youou/aso/modules/order/api/AdminOrderController.java`
5. `backend/src/test/java/com/youou/aso/modules/order/service/OrderServiceTest.java`
6. `backend/src/test/java/com/youou/aso/modules/order/api/AdminOrderControllerTest.java`
7. `frontend/src/api/orders.ts`
8. `frontend/src/views/user/OrderCreateView.vue`
9. `frontend/src/views/admin/OrderCreateView.vue`
10. `frontend/src/i18n/locales/zh-CN.ts`
11. `frontend/src/i18n/locales/en-US.ts`
12. `PROJECT_PROGRESS.md`

### 验证结果

1. `mvn.cmd "-Dtest=OrderServiceTest,AdminOrderControllerTest" test`：通过，20 个测试成功。
2. `mvn.cmd test`：通过，65 个后端测试成功；Flyway 校验 9 个迁移，当前 schema 为 V9。
3. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
4. 构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本次未处理。

### 下一步任务清单

1. 在真实浏览器中分别打开用户端 `/user/orders/create?orderType=KEYWORD_INSTALL` 和管理员端 `/admin/orders/create?orderType=KEYWORD_INSTALL`，确认关键词行增删、数量输入和摘要数量显示正常。
2. 使用余额充足的测试用户提交关键词安装订单，确认订单总数量、金额和订单明细与关键词数量一致。
3. 在订单列表导出中确认现有导出是否需要包含关键词明细；如需要，需要单独扩展导出字段。

### 风险点

1. 本次保留旧 `keywords` 入参兼容，但新前端已改为提交 `keywordItems`；后续外部调用方如仍使用旧入参，将按每个关键词数量 1 计费。
2. 订单列表当前主要展示主订单数量和金额，关键词明细需要进入详情或后续扩展导出才能完整查看。
3. 本次未进行浏览器截图复核，页面交互已通过 TypeScript 和构建验证，真实观感仍需打开页面确认。

## 2026-06-20 更新：管理员创建订单页视觉细节复核

- 状态：已完成管理员创建订单页视觉细节二次调整与前端构建验证。
- 当前任务：继续检查创建订单页重构后的细节，清理摘要区信息重复问题。
- 范围控制：仅修改管理员创建订单页样式和模板细节；未修改接口、提交逻辑、路由、数据库结构、`.env` 或秘钥配置；未安装依赖，未删除文件。

### 实施内容

1. 移除右侧摘要面板中重复的“预估金额”信息块。
2. 保留摘要顶部的大金额展示，减少视觉重复。
3. 保留订单类型、用户、应用、天数、数量和提交按钮。
4. 继续保持普通订单与特殊订单的不同摘要呈现。

### 修改文件

1. `frontend/src/views/admin/OrderCreateView.vue`
2. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. 构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本次未处理。
3. 前端本地 5173 服务当前未运行；浏览器工具仍受环境初始化问题影响，未完成截图复核。

### 下一步任务清单

1. 启动前端服务后，用管理员账号打开 `/admin/orders/create` 进行真实页面检查。
2. 重点检查服务类型 Tab 是否换行自然、商店按钮是否挤压、右侧摘要是否跟随滚动合理。
3. 如果截图中仍显拥挤，下一步将服务类型从 Tab 改为卡片式选择。

### 风险点

1. 当前仍缺少真实浏览器截图验证，视觉判断主要基于代码结构、CSS 和构建结果。

## 2026-06-20 更新：管理员创建订单页视觉重构

- 状态：已完成管理员创建订单页的代码级视觉重构与前端构建验证。
- 当前任务：根据视觉审查结论，改善管理员创建订单页面层级混乱、表单拥挤、摘要不突出的问题。
- 范围控制：仅修改管理员创建订单页模板、样式和中英文文案；未修改接口、提交逻辑、路由、数据库结构、`.env` 或秘钥配置；未安装依赖，未删除文件。

### 实施内容

1. 将页面改为两栏工作台结构：
   - 左侧为订单创建主流程。
   - 右侧为订单摘要和提交区。
2. 左侧主流程拆为三个清晰区块：
   - 服务类型
   - 用户与应用
   - 订单参数
3. 增加右侧订单摘要面板，集中展示订单类型、用户、应用、天数、数量和预估金额。
4. 将提交按钮移动到摘要面板内，形成更明确的确认提交区域。
5. 特殊订单展示独立提示区，避免用户进入页面后只看到禁用按钮而不明所以。
6. 统一表单控件宽度，降低四列表单拥挤感，移动端自动改为单列布局。
7. 补充中英文文案键：
   - `serviceSection`
   - `targetSection`
   - `parameterSection`
   - `summarySection`
   - `specialReviewTitle`

### 修改文件

1. `frontend/src/views/admin/OrderCreateView.vue`
2. `frontend/src/i18n/locales/zh-CN.ts`
3. `frontend/src/i18n/locales/en-US.ts`
4. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. 构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本次未处理。
3. 浏览器工具本轮初始化失败，未完成真实浏览器截图复核；本次先完成代码级视觉重构和构建验证。

### 下一步任务清单

1. 使用真实浏览器打开 `/admin/orders/create`，检查桌面端两栏布局、Tab 宽度、右侧摘要 sticky 表现。
2. 在窄屏视口检查单列布局、店铺切换按钮和摘要面板是否换行自然。
3. 逐个切换关键词安装、下载量、评分、评论、关键词保排名、关键词覆盖，确认表单区和摘要区状态一致。

### 风险点

1. 本次未改业务逻辑，特殊订单仍不可由管理员直接提交；页面仅用更明确的视觉提示承接这个限制。
2. 未完成真实浏览器截图复核，后续仍需要人工或浏览器工具检查具体渲染细节。

## 2026-06-20 更新：管理员菜单移除站点配置入口

- 状态：已完成管理员端“站点配置”菜单入口移除与前端构建验证。
- 当前任务：根据反馈从管理员侧边栏中去掉“站点配置”。
- 范围控制：仅移除菜单入口；未删除页面文件，未删除路由，未修改后端接口、数据库结构、`.env` 或秘钥配置；未安装依赖，未删除文件。

### 实施内容

1. 从管理员“系统管理”菜单中移除“站点配置”菜单项。
2. 保留 `/admin/site-config` 路由和 `SiteConfigView.vue` 文件，避免删除文件或扩大改动范围。

### 修改文件

1. `frontend/src/layouts/AdminLayout.vue`
2. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. 构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本次未处理。

### 下一步任务清单

1. 刷新管理员端，展开“系统管理”，确认不再显示“站点配置”。
2. 如后续确认该页面永久废弃，再单独评估是否移除路由和页面文件。

### 风险点

1. 当前只隐藏菜单入口，直接访问 `/admin/site-config` 仍可能打开页面；这是为了遵守不删除文件和不扩大路由变更的安全边界。

## 2026-06-20 更新：批量执行仅保留在待执行订单

- 状态：已完成管理员订单列表批量执行入口收敛与前端构建验证。
- 当前任务：执行中、已完成以及其他所有订单列表不再显示批量执行，只有“订单执行 > 待执行订单”保留批量执行。
- 范围控制：仅修改管理员订单列表页的批量执行相关 UI 展示逻辑；未修改后端批量执行接口、单条执行逻辑、路由、数据库结构、`.env` 或秘钥配置；未安装依赖，未删除文件。

### 实施内容

1. 在 `StoreOrdersView.vue` 中新增 `showBatchExecute` 计算属性。
2. 仅当路由固定状态为 `PENDING_EXECUTION` 时显示：
   - 批量执行按钮
   - 表格多选列
   - 已选订单数量提示
3. 执行中订单、已完成订单、苹果订单、谷歌订单、iPad订单不再显示批量执行相关 UI。
4. 保留待执行订单页的单条执行和批量执行能力。

### 修改文件

1. `frontend/src/views/admin/StoreOrdersView.vue`
2. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. 构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本次未处理。

### 下一步任务清单

1. 打开 `/admin/orders/pending-execution`，确认有多选列、已选数量和批量执行按钮。
2. 打开 `/admin/orders/executing` 和 `/admin/orders/completed`，确认没有批量执行按钮和多选列。
3. 打开 `/admin/orders/apple`、`/admin/orders/google`、`/admin/orders/ipad`，确认没有批量执行按钮和多选列。

### 风险点

1. 批量执行能力仍保留在前端代码和后端接口中，但入口仅在待执行订单页展示；如用户通过调试工具直接调用接口，仍由后端状态校验兜底。

## 2026-06-20 更新：订单执行页隐藏订单类型筛选

- 状态：已完成订单执行类页面查询条件再次精简与前端构建验证。
- 当前任务：在“订单执行”菜单下的待执行订单、执行中订单、已完成订单页面中移除查询条件里的“订单类型/任务类型”筛选。
- 范围控制：仅修改管理员订单列表页的筛选条件展示逻辑；未修改路由、请求接口、后端业务规则、数据库结构、`.env` 或秘钥配置；未安装依赖，未删除文件。

### 实施内容

1. 在 `StoreOrdersView.vue` 中新增 `showOrderTypeFilter` 计算属性。
2. 当路由带有固定 `statusFilter` 时，查询条件不显示“任务类型/订单类型”筛选。
3. 普通苹果订单、谷歌订单、iPad订单列表仍显示订单类型筛选。
4. 保留表格中的任务类型列，方便用户阅读订单类型。

### 修改文件

1. `frontend/src/views/admin/StoreOrdersView.vue`
2. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. 构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本次未处理。

### 下一步任务清单

1. 打开 `/admin/orders/pending-execution`，确认查询条件中没有“状态”和“任务类型”筛选。
2. 打开 `/admin/orders/executing` 和 `/admin/orders/completed`，确认同样隐藏这两个筛选。
3. 打开 `/admin/orders/apple`、`/admin/orders/google`、`/admin/orders/ipad`，确认仍显示状态和任务类型筛选。

### 风险点

1. 订单执行类页面的筛选项减少后，细分订单类型需要通过普通订单列表筛选查看；当前符合菜单固定状态页的简化诉求。

## 2026-06-20 更新：订单执行页隐藏状态筛选

- 状态：已完成订单执行类页面查询条件调整与前端构建验证。
- 当前任务：在“订单执行”菜单下的待执行订单、执行中订单、已完成订单页面中移除查询条件里的“状态”筛选。
- 范围控制：仅修改管理员订单列表页的筛选条件展示逻辑；未修改路由、请求接口、后端业务规则、数据库结构、`.env` 或秘钥配置；未安装依赖，未删除文件。

### 实施内容

1. 在 `StoreOrdersView.vue` 中新增 `showStatusFilter` 计算属性。
2. 当路由带有固定 `statusFilter` 时，查询条件不显示“状态”筛选。
3. 普通苹果订单、谷歌订单、iPad订单列表仍显示“状态”筛选。
4. 保留表格中的状态列，方便用户阅读订单当前状态。
5. 后端查询参数仍使用路由固定状态，确保三个订单执行页面只展示对应状态数据。

### 修改文件

1. `frontend/src/views/admin/StoreOrdersView.vue`
2. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. 构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本次未处理。

### 下一步任务清单

1. 打开 `/admin/orders/pending-execution`，确认查询条件中没有“状态”筛选。
2. 打开 `/admin/orders/executing` 和 `/admin/orders/completed`，确认同样不显示“状态”筛选。
3. 打开 `/admin/orders/apple`、`/admin/orders/google`、`/admin/orders/ipad`，确认仍显示“状态”筛选。

### 风险点

1. 状态筛选被隐藏后，用户无法在三个固定状态页面中临时切换状态；如需查看其他状态，应通过对应菜单或普通订单列表进入。

## 2026-06-20 更新：管理员状态订单归入订单执行菜单

- 状态：已完成管理员端状态订单菜单归类调整与前端构建验证。
- 当前任务：根据反馈，将“待执行订单、执行中订单、已完成订单”归纳到同一个一级菜单下，而不是作为三个独立一级菜单。
- 范围控制：仅修改管理员侧边栏菜单结构和中英文菜单文案；未修改路由、页面业务逻辑、后端接口、数据库结构、`.env` 或秘钥配置；未安装依赖，未删除文件。

### 实施内容

1. 新增管理员侧边栏一级菜单“订单执行”。
2. 将以下三个入口归入“订单执行”下：
   - 待执行订单
   - 执行中订单
   - 已完成订单
3. 原“订单”一级菜单继续保留待确认订单、苹果订单、谷歌订单、iPad订单。
4. 补充英文菜单名 `Order Execution`。

### 修改文件

1. `frontend/src/layouts/AdminLayout.vue`
2. `frontend/src/i18n/locales/zh-CN.ts`
3. `frontend/src/i18n/locales/en-US.ts`
4. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. 构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本次未处理。

### 下一步任务清单

1. 刷新管理员端，确认侧边栏显示“订单执行”一级菜单。
2. 展开“订单执行”，确认包含待执行订单、执行中订单、已完成订单。
3. 展开“订单”，确认只保留待确认订单和三类商店订单。

### 风险点

1. “订单执行”命名覆盖待执行、执行中、已完成三个状态；如产品侧希望强调历史查询，可后续改名为“执行订单”或“任务订单”。

## 2026-06-20 更新：管理员状态订单改为一级菜单

- 状态：已完成管理员端“待执行订单、执行中订单、已完成订单”一级菜单调整与前端构建验证。
- 当前任务：将待执行、执行中、已完成三个订单状态入口从“订单”二级菜单移出，单独作为管理员侧边栏一级菜单。
- 范围控制：仅修改管理员侧边栏菜单结构；未修改路由、页面业务逻辑、后端接口、数据库结构、`.env` 或秘钥配置；未安装依赖，未删除文件。

### 实施内容

1. 在管理员侧边栏中新增三个一级菜单入口：
   - 待执行订单
   - 执行中订单
   - 已完成订单
2. 从“订单”二级菜单中移除上述三个状态入口。
3. “订单”二级菜单继续保留：
   - 待确认订单
   - 苹果订单
   - 谷歌订单
   - iPad订单
4. 保留已有状态路由和列表默认筛选逻辑不变。

### 修改文件

1. `frontend/src/layouts/AdminLayout.vue`
2. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. 构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本次未处理。

### 下一步任务清单

1. 刷新管理员端，确认侧边栏一级菜单包含待执行订单、执行中订单、已完成订单。
2. 展开“订单”菜单，确认内部仅保留待确认订单、苹果订单、谷歌订单、iPad订单。
3. 分别打开三个一级状态菜单，确认列表仍按对应状态自动筛选。

### 风险点

1. 管理员菜单项数量增加后，较小屏幕下侧边栏可能需要滚动；当前保持现有布局不做额外视觉重构。

## 2026-06-20 更新：管理员订单状态菜单补齐

- 状态：已完成管理员端“待执行订单、执行中订单、已完成订单”菜单补齐与前端构建验证。
- 当前任务：在管理员端订单二级菜单中增加待执行、执行中、已完成三个入口，方便按订单状态直接进入列表。
- 范围控制：仅修改前端路由、管理员菜单、订单列表默认筛选和中英文文案；未修改后端接口、业务规则、数据库结构、`.env` 或秘钥配置；未安装依赖，未删除文件。

### 实施内容

1. 管理员路由新增：
   - `/admin/orders/pending-execution`
   - `/admin/orders/executing`
   - `/admin/orders/completed`
2. 管理员订单二级菜单新增：
   - 待执行订单
   - 执行中订单
   - 已完成订单
3. 复用现有 `StoreOrdersView.vue`，通过路由 `statusFilter` 自动带入默认状态筛选。
4. 保留现有苹果订单、谷歌订单、iPad订单入口和订单列表筛选、导出、批量执行能力。
5. 补充中英文菜单文案。

### 修改文件

1. `frontend/src/router/index.ts`
2. `frontend/src/layouts/AdminLayout.vue`
3. `frontend/src/views/admin/StoreOrdersView.vue`
4. `frontend/src/i18n/locales/zh-CN.ts`
5. `frontend/src/i18n/locales/en-US.ts`
6. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. 构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本次未处理。

### 下一步任务清单

1. 登录管理员端，展开“订单”菜单，确认显示待确认、待执行、执行中、已完成、苹果、谷歌、iPad订单入口。
2. 分别打开三个状态菜单，确认状态筛选自动带入对应状态。
3. 在待执行订单页验证批量执行仍可用，在执行中和已完成页确认导出仍可用。

### 风险点

1. 状态菜单复用同一个订单列表页，若用户手动清空状态筛选，可查看其他状态订单；这是保留现有筛选灵活性的结果。

## 2026-06-20 更新：管理员特殊服务按钮统一为立即订购

- 状态：已完成管理员端推广服务卡片按钮统一调整与前端构建验证。
- 当前任务：根据截图反馈，将“关键词保排名”和“关键词覆盖”两个特殊服务卡片的按钮从“暂不代下”改为与其他服务一致的“立即订购”。
- 范围控制：仅修改管理员端推广服务卡片的按钮展示和入口跳转逻辑；未修改后端接口、业务规则、路由结构、数据库结构、`.env` 或秘钥配置；未安装依赖，未删除文件。

### 实施内容

1. 取消管理员推广服务页中特殊服务按钮的禁用状态。
2. 将所有服务卡片按钮统一显示为“立即订购”。
3. 允许特殊服务卡片进入管理员订单创建页，并保留创建页内特殊订单审核流程提示与提交保护，避免绕过既有“用户提交需求、管理员审核、用户确认支付”的业务规则。

### 修改文件

1. `frontend/src/views/admin/PromotionServicesView.vue`
2. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. 构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本次未处理。

### 下一步任务清单

1. 刷新 `/admin/promotion`，确认 6 个服务卡片按钮均显示“立即订购”。
2. 点击“关键词保排名”和“关键词覆盖”，确认可进入订单创建页并看到对应订单类型。
3. 如后续需要管理员直接提交特殊订单，需要单独确认是否允许绕过用户确认支付流程，并补充后端权限、审计和扣款规则。

### 风险点

1. 当前仅统一入口和文案，特殊订单的正式提交流程仍未在管理员端开放；这是为了避免管理员直接绕过审核和用户支付确认。

## 2026-06-20 更新：管理员推广服务文案贴近用户端

- 状态：已完成管理员推广服务和管理员订单创建页的文案调整与前端构建验证。
- 当前任务：将管理员端“为用户下单”等偏后台操作感的名称调整为更接近用户端的下单体验。
- 范围控制：仅修改中英文文案，未修改业务逻辑、接口、路由、数据库结构，未修改 `.env` 或秘钥配置，未安装依赖，未删除文件。

### 实施内容

1. 管理员推广服务卡片按钮从“为用户下单”调整为“立即订购”。
2. 管理员订单创建页标题从“为用户创建订单”调整为“创建订单”。
3. 管理员订单提交按钮从“代用户提交订单”调整为“提交订单”。
4. 保留页面中“选择用户”和确认弹窗中的用户扣款说明，避免管理员误操作扣错账户余额。
5. 同步调整英文文案。

### 修改文件

1. `frontend/src/i18n/locales/zh-CN.ts`
2. `frontend/src/i18n/locales/en-US.ts`
3. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. 构建仍存在既有 Rollup PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本次未处理。

### 下一步任务清单

1. 刷新 `/admin/promotion`，确认按钮显示为“立即订购”。
2. 打开 `/admin/orders/create`，确认页面主标题和提交按钮更接近用户端下单体验。
3. 保留确认弹窗中的目标用户与扣款金额提示，继续防止误扣款。

### 风险点

1. 文案弱化了“代用户”字样，但实际仍会从所选用户余额扣款；确认弹窗必须保留。

## 2026-06-20 更新：管理员推广服务与代客下单

- 状态：已完成管理员端推广服务入口、管理员代用户创建普通订单、自动化测试、前后端构建验证和本地后端 jar 启动。
- 当前任务：将推广服务放到管理员端，并允许管理员选择用户后为用户下单。
- 范围控制：未修改数据库结构，未修改 `.env` 或秘钥配置，未安装依赖，未删除文件；本批只支持管理员代用户创建普通订单，特殊订单仍保留原有用户提交、管理员审核、用户确认支付流程。

### 实施内容

1. 新增管理员代客下单接口：
   - `POST /api/admin/orders/create-for-customer`
   - 请求包含 `customerId`、`customerAppId`、订单类型、日期和各类数量字段。
2. 后端复用 `OrderService.createCustomerOrder(customerId, command)`，继续执行既有生产规则：
   - 校验应用属于目标用户。
   - 普通订单提交时自动扣目标用户余额。
   - 余额不足时失败，不允许余额为负。
   - 订单创建后状态仍为“待确认”。
3. 管理员菜单新增“推广服务”入口。
4. 管理员路由新增：
   - `/admin/promotion`
   - `/admin/orders/create`
5. 新增管理员推广服务页，展示 6 类服务；普通订单可点击“为用户下单”，特殊订单显示“暂不代下”并提示仍走原有审核支付流程。
6. 新增管理员下单页，支持选择用户、选择该用户应用、订单类型、商店、日期、执行小时、关键词/下载量/评分/评论数量，并展示预估金额。
7. 管理员提交前弹窗确认，说明将从目标用户余额扣款。
8. 新增前端 `createAdminOrderForCustomer` API 封装和中英文文案。

### 修改文件

1. `backend/src/main/java/com/youou/aso/modules/order/api/AdminOrderController.java`
2. `backend/src/test/java/com/youou/aso/modules/order/api/AdminOrderControllerTest.java`
3. `frontend/src/api/orders.ts`
4. `frontend/src/router/index.ts`
5. `frontend/src/layouts/AdminLayout.vue`
6. `frontend/src/views/admin/PromotionServicesView.vue`
7. `frontend/src/views/admin/OrderCreateView.vue`
8. `frontend/src/i18n/locales/zh-CN.ts`
9. `frontend/src/i18n/locales/en-US.ts`
10. `PROJECT_PROGRESS.md`

### TDD 过程

1. 先在 `AdminOrderControllerTest` 中新增 `adminCanCreateOrderForCustomer`。
2. 首次运行 `mvn.cmd "-Dtest=AdminOrderControllerTest" test` 失败，原因是 `AdminCreateOrderRequest` 和 `createForCustomer` 方法不存在，符合 RED 预期。
3. 在 `AdminOrderController` 中实现最小接口后，定向测试通过。

### 验证结果

1. `mvn.cmd "-Dtest=AdminOrderControllerTest" test`：通过，3 个用例成功。
2. `mvn.cmd test`：通过，64 个后端测试成功。
3. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
4. `mvn.cmd package -DskipTests`：通过，已重新打包后端 jar。
5. 已启动最新后端 jar，新 Java 进程 PID 为 10144。
6. 未登录访问 `POST /api/admin/orders/create-for-customer` 返回 403，确认新接口在线且权限保护正常。
7. 前端构建仍存在既有 Rollup PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本次未处理。

### 下一步任务清单

1. 使用管理员账号打开 `/admin/promotion`，确认推广服务入口和普通订单按钮正常。
2. 进入 `/admin/orders/create`，选择用户、应用和订单内容，验证可为用户提交普通订单。
3. 确认用户余额不足时，代客下单失败并提示余额不足。
4. 确认新建订单进入管理员待确认订单列表和对应商店订单列表。
5. 如需管理员代特殊订单，需要单独设计是否跳过用户确认、是否直接扣款、是否保留协商审核记录。

### 风险点

1. 管理员代客下单会直接扣目标用户余额，当前尚未有操作日志落地；后续补操作日志时应记录管理员 ID、目标用户 ID、订单号和金额。
2. 当前用户选择和应用选择在前端基于列表过滤；用户和应用数量变大后，应增加后端分页和按用户查询应用接口。
3. 特殊订单暂不支持代下，避免绕过现有审核和用户确认支付业务规则。

## 2026-06-20 更新：首页 SVG 图标样式修复

- 状态：已完成首页图标异常放大问题修复与前端构建验证。
- 当前任务：根据截图检查管理员首页样式，修复看板卡片图标被渲染成超大黑色 SVG 的问题。
- 范围控制：仅修改前端样式选择器，未修改业务逻辑、接口、路由、数据库结构，未修改 `.env` 或秘钥配置，未安装依赖，未删除文件。

### 问题原因

1. 管理员首页和用户首页的看板指标图标直接使用 Element Plus SVG 图标组件。
2. 原 scoped CSS 使用 `.metric-icon svg` 选择器，未稳定穿透约束子组件 SVG。
3. 浏览器中 SVG 未被限制尺寸，导致图标以巨大黑色图形铺满页面。

### 实施内容

1. 将管理员首页 `.metric-icon svg` 改为 `.metric-icon :deep(svg)`，明确约束子组件 SVG 尺寸。
2. 将用户首页 `.metric-icon svg` 同步改为 `.metric-icon :deep(svg)`，避免同类问题。
3. 为图标 SVG 增加 `display: block`，减少内联 SVG 的基线空隙影响。

### 修改文件

1. `frontend/src/views/admin/DashboardView.vue`
2. `frontend/src/views/user/DashboardView.vue`
3. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. 构建仍存在既有 Rollup PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本次未处理。

### 下一步任务清单

1. 刷新 `/admin/dashboard`，确认四个指标卡图标恢复为正常小尺寸。
2. 刷新 `/user/dashboard`，确认用户首页指标卡图标也正常。
3. 如页面仍有视觉问题，继续用真实浏览器截图逐项修正间距、层级和响应式表现。

### 风险点

1. 本次只修复 SVG 尺寸，不调整首页信息结构。
2. 如果浏览器缓存了旧前端资源，需要强制刷新后才能看到新样式。

## 2026-06-20 更新：第三批未实现页面补齐（管理员管理）

- 状态：已完成管理员管理的后端接口、前端页面、自动化测试、前后端构建验证和本地后端 jar 重启。
- 当前任务：继续补齐未实现页面中无需新增数据库结构的管理员管理页面。
- 范围控制：未修改数据库结构，未修改 `.env` 或秘钥配置，未安装依赖，未删除文件；未实现创建管理员、重置密码和强制改密，避免绕过后续审计设计。

### 实施内容

1. 新增超级管理员专用管理员管理接口：
   - `GET /api/admin/admin-accounts`：查询全部管理员账号。
   - `PUT /api/admin/admin-accounts/{id}/status`：更新管理员状态为启用、禁用或锁定。
2. 管理员列表返回账号 ID、用户名、邮箱、角色、状态、强制改密标记、语言偏好、最后登录时间和创建/更新时间。
3. 后端限制只有 `SUPER_ADMIN` 可访问管理员管理接口。
4. 后端禁止超级管理员将自己的账号改为禁用或锁定，降低误操作锁死后台风险。
5. 管理员管理页面改为真实列表，支持状态标签、启用/禁用/锁定操作和确认弹窗。
6. 当前登录管理员自身的禁用/锁定按钮在前端置灰，后端也做二次保护。
7. 新增前端 API 封装 `adminAccounts.ts`，补齐中英文文案。

### 修改文件

1. `backend/src/main/java/com/youou/aso/modules/account/api/AdminAccountController.java`
2. `backend/src/main/java/com/youou/aso/modules/account/service/AdminAccountService.java`
3. `backend/src/main/java/com/youou/aso/modules/account/dto/AdminAccountResult.java`
4. `backend/src/main/java/com/youou/aso/modules/account/repository/AdminAccountRepository.java`
5. `backend/src/main/java/com/youou/aso/modules/account/repository/JdbcAdminAccountRepository.java`
6. `backend/src/test/java/com/youou/aso/modules/account/api/AdminAccountControllerTest.java`
7. `backend/src/test/java/com/youou/aso/modules/account/service/AuthServiceTest.java`
8. `frontend/src/api/adminAccounts.ts`
9. `frontend/src/views/admin/AdminAccountsView.vue`
10. `frontend/src/i18n/locales/zh-CN.ts`
11. `frontend/src/i18n/locales/en-US.ts`
12. `PROJECT_PROGRESS.md`

### TDD 过程

1. 先新增 `AdminAccountControllerTest`，覆盖超级管理员查询管理员列表、更新管理员状态、普通管理员禁止访问。
2. 首次运行 `mvn.cmd "-Dtest=AdminAccountControllerTest" test` 失败，原因是新增控制器、服务和 DTO 尚不存在，符合 RED 预期。
3. 实现最小后端接口后，因仓储接口扩展补齐既有测试内存仓储方法。
4. 定向测试通过后，运行后端全量测试通过。

### 验证结果

1. `mvn.cmd "-Dtest=AdminAccountControllerTest" test`：通过，3 个用例成功。
2. `mvn.cmd test`：通过，63 个后端测试成功。
3. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
4. `mvn.cmd package -DskipTests`：通过，已重新打包后端 jar。
5. 已启动最新后端 jar，新 Java 进程 PID 为 6332。
6. 未登录访问 `GET /api/admin/admin-accounts` 返回 403，确认新接口在线且权限保护正常。
7. 前端构建仍存在既有 Rollup PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本次未处理。

### 下一步任务清单

1. 使用超级管理员账号打开 `/admin/admin-accounts`，验证管理员列表和状态切换。
2. 使用普通管理员账号确认菜单不可见且直接访问接口返回 403。
3. 剩余未实现页面为站点配置和操作日志；这两项需要新增数据库表或确认已有外部存储方案后再继续。
4. 如要补管理员创建、密码重置、强制改密，需要单独设计审计日志和密码安全策略。

### 风险点

1. 当前只做管理员状态管理，不支持新增管理员；生产环境新增管理员仍需后续安全流程。
2. 如果所有超级管理员都被其他超级管理员禁用，仍可能造成权限恢复困难；后续可限制至少保留一个启用的超级管理员。
3. 操作日志尚未落地，因此本次管理员状态变更还没有持久化审计记录。

## 2026-06-20 更新：第二批未实现页面补齐（用户管理与地区配置）

- 状态：已完成用户管理、地区配置的后端接口、前端页面、自动化测试、前后端构建验证和本地后端 jar 重启。
- 当前任务：继续按照未实现页面清单补齐后台管理页面，第二批处理不需要新增数据库结构的用户列表和地区开关。
- 范围控制：未修改数据库结构，未修改 `.env` 或秘钥配置，未安装依赖，未删除文件；未实现密码重置、创建用户和审计日志，避免在本批次扩大安全敏感范围。

### 实施内容

1. 新增管理员用户管理接口：
   - `GET /api/admin/customers`：按用户名或邮箱查询用户列表。
   - `PUT /api/admin/customers/{id}/status`：更新用户状态为启用、禁用或锁定。
2. 用户列表返回用户 ID、用户名、邮箱、状态、强制改密标记、语言偏好、余额、冻结余额、最后登录时间和创建/更新时间。
3. 新增管理员地区配置接口：
   - `GET /api/admin/regions`：查询全部地区，包含启用状态和商店支持开关。
   - `PUT /api/admin/regions/{code}`：更新地区启用状态和 App Store、Google Play、iPad Store 支持状态。
4. 扩展现有 `CustomerAccountRepository` 和 `MarketRegionRepository`，复用既有表字段，不新增迁移。
5. 管理员“用户管理”页面改为真实列表，支持搜索、余额展示、状态标签、启用/禁用/锁定操作和确认弹窗。
6. 管理员“地区配置”页面改为真实列表，支持地区启用和各商店支持开关即时保存。
7. 新增前端 API 封装 `customers.ts` 和 `regions.ts`，补齐中英文文案。

### 修改文件

1. `backend/src/main/java/com/youou/aso/modules/account/api/AdminCustomerController.java`
2. `backend/src/main/java/com/youou/aso/modules/account/service/AdminCustomerService.java`
3. `backend/src/main/java/com/youou/aso/modules/account/dto/CustomerAccountResult.java`
4. `backend/src/main/java/com/youou/aso/modules/account/repository/CustomerAccountRepository.java`
5. `backend/src/main/java/com/youou/aso/modules/account/repository/JdbcCustomerAccountRepository.java`
6. `backend/src/main/java/com/youou/aso/modules/appmanagement/api/AdminMarketRegionController.java`
7. `backend/src/main/java/com/youou/aso/modules/appmanagement/service/AdminMarketRegionService.java`
8. `backend/src/main/java/com/youou/aso/modules/appmanagement/dto/AdminMarketRegionResult.java`
9. `backend/src/main/java/com/youou/aso/modules/appmanagement/repository/MarketRegionRepository.java`
10. `backend/src/main/java/com/youou/aso/modules/appmanagement/repository/JdbcMarketRegionRepository.java`
11. `backend/src/test/java/com/youou/aso/modules/account/api/AdminCustomerControllerTest.java`
12. `backend/src/test/java/com/youou/aso/modules/appmanagement/api/AdminMarketRegionControllerTest.java`
13. `backend/src/test/java/com/youou/aso/modules/account/service/AuthServiceTest.java`
14. `backend/src/test/java/com/youou/aso/modules/appmanagement/service/CustomerAppServiceTest.java`
15. `frontend/src/api/customers.ts`
16. `frontend/src/api/regions.ts`
17. `frontend/src/views/admin/CustomerManagementView.vue`
18. `frontend/src/views/admin/RegionView.vue`
19. `frontend/src/i18n/locales/zh-CN.ts`
20. `frontend/src/i18n/locales/en-US.ts`
21. `PROJECT_PROGRESS.md`

### TDD 过程

1. 先新增 `AdminCustomerControllerTest`，覆盖管理员查询用户、更新用户状态、普通用户禁止访问管理员用户接口。
2. 先新增 `AdminMarketRegionControllerTest`，覆盖管理员查询地区、更新地区开关、普通用户禁止访问管理员地区接口。
3. 首次运行 `mvn.cmd "-Dtest=AdminCustomerControllerTest,AdminMarketRegionControllerTest" test` 失败，原因是新增控制器、服务和 DTO 尚不存在，符合 RED 预期。
4. 实现最小后端接口后，定向测试通过。
5. 因仓储接口扩展，补齐既有测试内存仓储的最小实现，保持旧测试兼容。

### 验证结果

1. `mvn.cmd "-Dtest=AdminCustomerControllerTest,AdminMarketRegionControllerTest" test`：通过，6 个用例成功。
2. `mvn.cmd test`：通过，60 个后端测试成功。
3. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
4. `mvn.cmd package -DskipTests`：通过，已重新打包后端 jar。
5. 已启动最新后端 jar，新 Java 进程 PID 为 14708。
6. 未登录访问 `GET /api/admin/customers` 返回 403，确认新接口在线且权限保护正常。
7. 前端构建仍存在既有 Rollup PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本次未处理。

### 下一步任务清单

1. 使用管理员账号打开 `/admin/customers`，验证用户列表、搜索和状态切换。
2. 使用管理员账号打开 `/admin/regions`，验证地区启用状态和商店支持开关保存后刷新仍生效。
3. 下一批处理站点配置、管理员管理、操作日志；其中站点配置和操作日志大概率需要新增持久化表或明确落库方案，开始前需单独确认。
4. 用户管理的创建用户、重置密码和强制改密属于安全敏感操作，建议作为独立批次设计接口、审计和权限边界。

### 风险点

1. 用户管理当前允许管理员禁用或锁定用户，未限制操作自身或超级管理员，因为该接口只管理普通用户表；后续如果增加管理员管理需单独做保护。
2. 地区配置关闭后只影响新建应用和新订单选择，历史应用和历史订单不会删除或回滚。
3. 用户列表每行会查询钱包信息，当前规模可接受；用户量增大后应改为一次 SQL join 或分页查询。

## 2026-06-20 更新：第一批未实现页面补齐

- 状态：已完成用户首页、管理员首页、用户账户设置、管理员账户设置的真实页面实现与前端构建验证，待真实账号浏览器验证接口数据展示。
- 当前任务：按照未实现页面清单逐批补齐页面功能，第一批优先处理不需要新增数据库结构的首页与账户设置页。
- 范围控制：未修改数据库结构，未新增后端接口，未修改前端路由，未修改 `.env` 或秘钥配置，未安装依赖，未删除文件。账户设置页未提供无后端支撑的密码修改假功能。

### 实施内容

1. 用户首页改为账户概览页，复用已有用户端接口展示可用余额、应用数量、待确认订单、执行中订单、最近订单和最近收支。
2. 管理员首页改为运营看板，复用已有管理员端接口展示待确认订单、待审核特殊需求、待执行订单、管理应用数量、待处理订单、待审核需求和最近财务流水。
3. 用户账户设置页展示当前账号 ID、用户名、邮箱、语言偏好、登录安全状态和退出登录入口。
4. 管理员账户设置页展示管理员账号 ID、用户名、邮箱、角色、语言偏好、登录安全状态和退出登录入口。
5. 语言切换会同步更新 i18n、浏览器语言缓存和登录态中的 locale 字段。
6. 补齐首页与账户设置页所需中英文文案。

### 修改文件

1. `frontend/src/views/user/DashboardView.vue`
2. `frontend/src/views/admin/DashboardView.vue`
3. `frontend/src/views/user/AccountSettingsView.vue`
4. `frontend/src/views/admin/AccountSettingsView.vue`
5. `frontend/src/i18n/locales/zh-CN.ts`
6. `frontend/src/i18n/locales/en-US.ts`
7. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. 构建仍存在既有 Rollup PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本次未处理。
3. 本次未跑后端测试，因为未修改后端代码。

### 下一步任务清单

1. 使用普通用户账号打开 `/user/dashboard` 和 `/user/settings`，确认钱包、应用、订单、流水和账号信息正常展示。
2. 使用管理员账号打开 `/admin/dashboard` 和 `/admin/settings`，确认订单、审核、财务、应用和角色信息正常展示。
3. 第二批继续补齐“用户管理”和“地区配置”页面；这两页需要检查是否补充后端管理接口。
4. 站点配置、管理员管理、操作日志涉及配置持久化、权限和审计，需要在第二批后单独确认是否允许新增数据库结构。

### 风险点

1. 首页统计目前基于现有列表接口在前端聚合，适合当前阶段快速落地；当订单或流水数据量变大后，应新增后端汇总接口减少列表拉取成本。
2. 账户设置页暂不支持修改密码，因为后端没有对应安全接口；为了避免伪功能和绕过审计，本次只展示真实状态和退出登录。
3. 管理员首页会并发调用订单、审核、财务、应用接口；若任一接口失败，页面会提示加载失败，后续可按模块做局部容错。

## 2026-06-20 更新：管理员端菜单精简

- 状态：已完成前端菜单结构调整与构建验证，待真实浏览器确认菜单层级和默认展开体验。
- 当前任务：精简管理员端侧边栏菜单，减少顶层入口数量，提升后台操作导航效率。
- 范围控制：未修改前端路由定义，未删除任何页面，未修改数据库结构，未修改 `.env` 或秘钥配置，未安装依赖，未删除文件。

### 实施内容

1. 将“待确认订单”从顶层菜单移入“订单”二级菜单，作为订单组下的独立入口。
2. 保留“订单”二级菜单结构，包含：待确认订单、苹果订单、谷歌订单、iPad订单。
3. 新增“系统管理”二级菜单，收纳：价格配置、地区配置、站点配置、管理员管理、操作日志、账户设置。
4. 顶层菜单收敛为：首页、用户管理、应用管理、订单、审核管理、财务管理、系统管理。
5. 补齐中英文 `menu.systemManagement` 文案。

### 修改文件

1. `frontend/src/layouts/AdminLayout.vue`
2. `frontend/src/i18n/locales/zh-CN.ts`
3. `frontend/src/i18n/locales/en-US.ts`
4. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 构建成功。
2. 构建仍存在既有 Rollup PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本次未处理。

### 下一步任务清单

1. 使用管理员账号打开后台，确认“订单”和“系统管理”二级菜单层级符合预期。
2. 分别点击待确认订单、三类商店订单、价格配置、地区配置、站点配置、操作日志、账户设置，确认路由正常。
3. 如需要进一步精简，可考虑把“审核管理”也并入“订单”组，但这会改变业务分组，需要再确认。

### 风险点

1. 本次只调整菜单分组，不改路由；如果用户习惯旧顶层入口，可能需要短暂适应。
2. 待确认订单现在位于订单二级菜单下，仍是独立菜单项，但不再作为顶层菜单展示。

## 2026-06-20 更新：执行中订单到期自动完成定时任务

- 状态：已完成后端代码修改、TDD 验证、全量后端测试、本地 jar 重新打包与后端重启。
- 当前任务：新增后台定时任务，使执行中且已到预计完成时间的订单即使无人打开列表，也能自动变为已完成。
- 范围控制：未修改数据库结构，未修改订单状态规则，未修改 `.env` 或秘钥配置，未安装依赖，未删除文件。

### 实施内容

1. 将 `OrderService` 中原有列表查询前触发的到期完成逻辑提取为公共事务方法 `completeExpiredExecutingOrders()`，返回本次完成数量。
2. 用户端和管理员端订单列表仍继续调用该方法，保留原有查询时兜底完成行为。
3. 新增 `OrderCompletionScheduler`，默认初始延迟 60 秒、固定延迟 60 秒执行一次。
4. 定时任务间隔支持配置覆盖：
   - `youou.order.completion.initial-delay-ms`
   - `youou.order.completion.fixed-delay-ms`
5. 定时任务执行失败时记录错误日志，避免异常静默吞掉；完成数量大于 0 时记录 info 日志。
6. 在应用入口启用 Spring Scheduling。

### 修改文件

1. `backend/src/main/java/com/youou/aso/YououAsoApplication.java`
2. `backend/src/main/java/com/youou/aso/modules/order/service/OrderService.java`
3. `backend/src/main/java/com/youou/aso/modules/order/job/OrderCompletionScheduler.java`
4. `backend/src/test/java/com/youou/aso/modules/order/service/OrderServiceTest.java`
5. `backend/src/test/java/com/youou/aso/modules/order/job/OrderCompletionSchedulerTest.java`
6. `PROJECT_PROGRESS.md`

### TDD 过程

1. 先在 `OrderServiceTest` 增加 `completeExpiredExecutingOrdersReturnsCompletedCount`，要求只完成到期执行中订单并返回完成数量。
2. 新增 `OrderCompletionSchedulerTest`，要求调度器调用 `OrderService.completeExpiredExecutingOrders()`。
3. 首次运行 `mvn.cmd "-Dtest=OrderServiceTest,OrderCompletionSchedulerTest" test` 失败，原因是完成方法仍为 private 且无返回值、调度器类不存在，符合 RED 预期。
4. 实现公共完成方法、调度器和 `@EnableScheduling` 后，定向测试通过。

### 验证结果

1. `mvn.cmd "-Dtest=OrderServiceTest,OrderCompletionSchedulerTest" test`：通过，17 个用例成功。
2. `mvn.cmd test`：通过，54 个后端测试成功。
3. `mvn.cmd package -DskipTests`：首次因旧后端 jar 被 PID 15216 锁定失败；停止旧进程后重新打包通过。
4. 已启动最新后端 jar，新 Java 进程 PID 为 4864。
5. 未登录访问 `GET /api/admin/orders` 返回 403，确认最新后端服务可访问且权限保护正常。

### 下一步任务清单

1. 准备一笔预计完成时间已过的执行中订单，观察定时任务是否在 60 秒左右自动改为已完成。
2. 真实账号端到端验证普通订单和特殊订单从待执行、执行中到已完成的状态流转。
3. 如生产部署需要更短或更长周期，在环境配置中覆盖 `youou.order.completion.fixed-delay-ms`。

### 风险点

1. 当前定时任务为单实例应用内调度；如果未来多实例部署，同一批到期订单可能被多个实例同时扫描，需要结合数据库条件更新或分布式锁进一步加固。
2. 定时任务只处理 `EXECUTING` 且 `expected_completed_at <= now` 的订单，不会处理待执行、已取消或已完成订单。
3. 默认每 60 秒执行一次，极端情况下订单完成状态最多延迟约 1 分钟。

## 2026-06-20 更新：特殊订单控制器测试补强

- 状态：已完成测试代码新增与后端全量测试验证。
- 当前任务：为特殊订单用户端和管理员端 HTTP 控制器补充权限与调用链测试，提高特殊订单端到端流程回归保障。
- 范围控制：本次只新增测试，不修改生产业务代码；未修改数据库结构，未修改 `.env` 或秘钥配置，未安装依赖，未删除文件。

### 实施内容

1. 新增用户端特殊订单审核控制器测试，覆盖普通用户提交特殊需求、支付已审核需求生成正式订单、管理员不能调用用户端特殊审核接口。
2. 新增管理员端特殊订单审核控制器测试，覆盖管理员审核协商内容和价格、管理员取消未提交特殊需求、普通用户不能调用管理员端特殊审核接口。
3. 测试复用现有轻量 controller 单测风格，直接验证 controller 权限校验和 service 参数传递，不启动 WebMvc。

### 修改文件

1. `backend/src/test/java/com/youou/aso/modules/order/api/CustomerSpecialOrderAuditControllerTest.java`
2. `backend/src/test/java/com/youou/aso/modules/order/api/AdminSpecialOrderAuditControllerTest.java`
3. `PROJECT_PROGRESS.md`

### 验证结果

1. `mvn.cmd "-Dtest=CustomerSpecialOrderAuditControllerTest,AdminSpecialOrderAuditControllerTest" test`：通过，6 个用例成功。
2. `mvn.cmd test`：通过，52 个后端测试成功。

### 下一步任务清单

1. 使用真实普通用户提交关键词保排名或关键词覆盖特殊需求。
2. 使用管理员账号审核特殊需求，填写协商内容和价格。
3. 回到用户端点击“支付并提交”，确认余额扣款、特殊需求变为 `SUBMITTED`、正式订单进入对应订单列表并显示“待确认”。
4. 使用管理员账号确认该特殊正式订单，继续验证待执行、执行中、自动完成流程。

### 风险点

1. 本次是自动化测试补强，不替代真实浏览器登录态下的端到端验证。
2. 控制器测试使用 mock service，覆盖权限和参数传递；真实数据库写入、余额扣款和状态迁移仍由 `SpecialOrderAuditServiceTest` 与后续人工链路验证覆盖。

## 2026-06-20 更新：特殊订单用户支付确认入口

- 状态：已完成前端代码修改与构建验证，待使用真实用户账号在已审核待支付特殊需求上完成一次支付提交。
- 当前任务：强化特殊订单审核通过后的用户端支付语义，让用户在扣余额前确认价格，并在成功后进入正式订单列表。
- 范围控制：未新增接口，未新增前端路由，未修改数据库结构，未修改 `.env` 或秘钥配置，未安装依赖，未删除文件。

### 实施内容

1. 用户端推广服务页中，`APPROVED_WAIT_SUBMIT` 特殊需求的操作按钮从“提交订单”改为“支付并提交”。
2. 点击“支付并提交”时弹出确认框，展示将从账户余额扣除的协商价格，并说明会生成正式订单等待管理员确认。
3. 用户确认后继续调用既有 `POST /api/customer/special-order-audits/{id}/submit`，由后端完成扣款和正式订单生成。
4. 提交成功后刷新特殊需求列表，并按特殊需求所属商店跳转到对应用户订单列表。
5. 补齐中英文支付确认相关文案。

### 修改文件

1. `frontend/src/views/user/PromotionServicesView.vue`
2. `frontend/src/i18n/locales/zh-CN.ts`
3. `frontend/src/i18n/locales/en-US.ts`
4. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 构建成功。
2. 构建仍存在既有 Rollup PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本次未处理。

### 下一步任务清单

1. 使用真实普通用户提交关键词保排名或关键词覆盖特殊需求。
2. 使用管理员账号审核该特殊需求，填写协商内容和价格。
3. 回到用户端点击“支付并提交”，确认余额扣款、特殊需求变为 `SUBMITTED`、正式订单进入对应订单列表并显示“待确认”。
4. 使用管理员账号确认该特殊正式订单，继续验证待执行、执行中、完成流程。

### 风险点

1. 本次只强化前端支付确认体验，扣款、防重复提交、余额不足保护仍以后端为准。
2. 尚未使用真实登录态完成特殊订单端到端流程；如果测试账号余额不足，会按后端返回提示用户先充值。
3. 特殊订单正式订单的订单时间目前由后端按提交当天生成，后续如需要长期服务周期字段，需要单独确认字段和数据库设计。

## 2026-06-20 更新：管理员订单取消入口前端接入

- 状态：已完成前端代码修改与构建验证，待使用管理员账号在真实订单数据上执行取消操作确认退款流水。
- 当前任务：把管理员订单取消/退款后端能力接入后台订单列表和待确认订单页。
- 范围控制：未新增前端路由，未修改菜单结构，未修改数据库结构，未修改 `.env` 或秘钥配置，未安装依赖，未删除文件。

### 实施内容

1. 前端订单 API 新增 `cancelAdminOrder(id, reason)`，调用 `POST /api/admin/orders/{id}/cancel`。
2. 管理员订单列表对 `PENDING_CONFIRM` 和 `PENDING_EXECUTION` 订单显示“取消”按钮。
3. 待确认订单页对每条待确认订单显示“取消”按钮。
4. 点击取消时弹出原因输入框，确认后提交取消请求，成功后刷新当前列表。
5. 补齐中英文取消订单文案，包括取消提示、原因占位、保留订单、取消成功等。

### 修改文件

1. `frontend/src/api/orders.ts`
2. `frontend/src/views/admin/StoreOrdersView.vue`
3. `frontend/src/views/admin/PendingConfirmOrdersView.vue`
4. `frontend/src/i18n/locales/zh-CN.ts`
5. `frontend/src/i18n/locales/en-US.ts`
6. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 构建成功。
2. 构建仍存在既有 Rollup PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本次未处理。

### 下一步任务清单

1. 使用管理员账号在待确认订单页取消一笔真实待确认订单，确认状态变为“已取消”。
2. 使用普通用户查看收支明细，确认出现 `ORDER_REFUND` 退款流水，余额回到预期值。
3. 在普通管理员订单列表取消一笔待执行订单，确认同样退款并刷新列表。
4. 继续完善特殊订单审核后的用户支付入口和正式订单生成流程端到端验证。

### 风险点

1. 前端显示取消按钮的状态与后端规则保持一致，仅覆盖待确认和待执行；执行中订单不提供取消入口。
2. 当前取消原因输入框允许留空，后端会写入默认原因 `ADMIN_CANCELLED`。
3. 尚未用真实登录态浏览器完成取消和退款端到端操作，需要下一步配合实际账号数据验证。

## 2026-06-20 更新：管理员订单取消与退款后端闭环

- 状态：已完成后端代码修改、TDD 验证、全量后端测试、本地 jar 重新打包与后端重启，待前端管理员订单页接入取消按钮并用真实管理员账号端到端验证。
- 当前任务：补齐普通正式订单的管理员取消能力，统一终态为 `CANCELLED`，并在未执行前取消时退回订单扣款。
- 范围控制：未新增数据库迁移，复用既有 `refund_transaction_id`、`ORDER_REFUND` 和 `CANCELLED`；未修改 `.env` 或秘钥配置，未安装依赖，未删除文件。

### 实施内容

1. `WalletService` 新增 `refundForOrder()`，用于订单退款场景。
2. `JdbcWalletService` 新增退款实现：锁定用户钱包、增加余额、写入 `CREDIT / ORDER_REFUND` 钱包流水。
3. `OrderService` 新增 `cancelOrder()`：仅允许取消 `PENDING_CONFIRM` 和 `PENDING_EXECUTION` 订单；已执行中、已完成、已取消等状态拒绝取消。
4. 订单取消时如存在扣款流水和订单金额，则按订单总金额全额退款并写入 `refund_transaction_id`，随后将订单状态更新为 `CANCELLED`。
5. 管理员订单接口新增 `POST /api/admin/orders/{id}/cancel`，请求体支持 `{ reason }`，继续沿用管理员权限校验。
6. 新增管理员订单控制器测试，覆盖管理员取消与普通用户禁止调用。

### 修改文件

1. `backend/src/main/java/com/youou/aso/modules/wallet/service/WalletService.java`
2. `backend/src/main/java/com/youou/aso/modules/wallet/service/JdbcWalletService.java`
3. `backend/src/main/java/com/youou/aso/modules/order/service/OrderService.java`
4. `backend/src/main/java/com/youou/aso/modules/order/api/AdminOrderController.java`
5. `backend/src/test/java/com/youou/aso/modules/order/service/OrderServiceTest.java`
6. `backend/src/test/java/com/youou/aso/modules/order/service/SpecialOrderAuditServiceTest.java`
7. `backend/src/test/java/com/youou/aso/modules/order/api/AdminOrderControllerTest.java`
8. `PROJECT_PROGRESS.md`

### TDD 过程

1. 先在 `OrderServiceTest` 增加取消待执行订单应退款并标记 `CANCELLED`、执行中订单应拒绝取消且不退款两个测试。
2. 首次运行 `mvn.cmd "-Dtest=OrderServiceTest" test` 失败，原因是 `OrderService.cancelOrder()` 和 `WalletService.refundForOrder()` 尚不存在，符合 RED 预期。
3. 补齐钱包退款和订单取消实现后，因 `SpecialOrderAuditServiceTest` 的测试假实现缺少新接口方法出现编译失败；补齐测试夹具后重新验证通过。

### 验证结果

1. `mvn.cmd "-Dtest=OrderServiceTest" test`：通过，15 个用例成功。
2. `mvn.cmd "-Dtest=AdminOrderControllerTest" test`：通过，2 个用例成功。
3. `mvn.cmd test`：通过，46 个后端测试成功。
4. `mvn.cmd package -DskipTests`：首次因运行中的旧后端 jar 锁定目标文件失败；停止 PID 15352 后重新执行通过。
5. 已启动最新后端 jar，新 Java 进程 PID 为 15216。
6. 未登录调用 `POST /api/admin/orders/1/cancel` 返回 403，确认新接口已加载且仍受权限保护。

### 下一步任务清单

1. 前端管理员订单列表和待确认订单页接入取消按钮/弹窗，调用 `POST /api/admin/orders/{id}/cancel`。
2. 使用管理员账号取消一笔待确认或待执行订单，确认订单状态为“已取消”，用户收支明细生成 `ORDER_REFUND` 流水。
3. 使用普通用户提交订单后确认余额扣款，再由管理员取消，验证余额回滚到预期值。
4. 继续完善特殊订单审核后的用户支付入口和正式订单生成流程端到端验证。

### 风险点

1. 当前实现只允许取消未执行前订单；执行中订单不做全额退款，避免对已消耗服务成本的订单误退。如后续需要执行中取消，应补充部分退款和结算规则。
2. 取消原因暂存于既有 `reject_reason` 字段，没有新增单独取消原因字段；若后续需要更精细的审计字段，需要确认数据库结构变更。
3. 当前后端接口已完成，前端尚未提供取消入口，仍需下一步接入页面操作。

## 2026-06-20 更新：后台财务页接入用户充值

- 状态：已完成前端代码修改与构建验证，待使用管理员账号在真实浏览器中提交一笔小额充值做端到端确认。
- 当前任务：将已新增的管理员充值后端接口接入后台财务管理页，补齐普通订单余额充值入口。
- 范围控制：未新增前端路由，未修改菜单结构，未修改数据库结构，未修改 `.env` 或秘钥配置，未安装依赖，未删除文件。

### 实施内容

1. 在钱包 API 中新增管理员充值请求类型和 `rechargeCustomerWallet()` 方法，调用 `POST /api/admin/wallet/recharge`。
2. 后台财务管理页工具栏新增“充值”按钮，打开用户充值弹窗。
3. 充值弹窗支持填写用户 ID、充值金额、充值备注，并进行前端基础校验。
4. 充值提交成功后自动关闭弹窗、将流水筛选切换到该用户并刷新交易列表。
5. 补齐中英文充值相关文案，避免多语言环境出现裸露 key。

### 修改文件

1. `frontend/src/api/wallet.ts`
2. `frontend/src/views/admin/FinanceManagementView.vue`
3. `frontend/src/i18n/locales/zh-CN.ts`
4. `frontend/src/i18n/locales/en-US.ts`
5. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 构建成功。
2. 构建仍存在既有 Rollup PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本次未处理。

### 下一步任务清单

1. 使用管理员账号打开后台财务管理页，提交一笔小额充值，确认接口返回成功并生成 `ADMIN_RECHARGE` 钱包流水。
2. 使用已充值的普通用户提交一笔普通订单，确认余额扣款、订单进入“待确认”、收支明细出现订单扣款流水。
3. 继续补齐管理员取消订单与退款闭环，统一写入 `CANCELLED` 状态。
4. 继续完善特殊订单审核后的用户支付入口和正式订单生成流程端到端验证。

### 风险点

1. 前端已做基础校验，但充值权限和金额合法性仍以后端校验为准。
2. 当前充值入口不包含客户搜索选择器，需要管理员输入准确用户 ID；后续如客户量增长，可在确认范围后接入客户选择弹窗。
3. 本次未做真实登录态浏览器提交验证，因为当前回合只完成代码构建验证；仍需管理员账号实测一次完整充值链路。

## 2026-06-20 更新：管理员用户充值接口补齐

- 状态：已完成后端代码修改、自动化验证、本地后端重新打包与重启，待前端财务页接入充值入口或用管理员账号直接调用接口验证。
- 当前任务：补齐普通订单“用户需先充值”的真实流程缺口，提供管理员为用户钱包充值的后端能力。
- 范围控制：未新增数据库迁移，复用既有 `wallet_account` 和 `wallet_transaction` 表；未修改路由结构，未修改 `.env` 或秘钥配置，未安装依赖，未删除文件。

### 实施内容

1. 新增 `AdminRechargeCommand`，描述管理员充值入参：用户 ID、金额、备注。
2. 新增 `AdminWalletService`，校验金额必须大于 0，锁定用户钱包后增加余额。
3. 新增管理员钱包仓储接口和 JDBC 实现，负责查询加锁的钱包、更新余额、写入钱包流水。
4. 充值流水写入 `CREDIT / ADMIN_RECHARGE`，记录充值前余额、充值后余额和备注。
5. 管理员钱包接口新增 `POST /api/admin/wallet/recharge`，沿用现有管理员鉴权。
6. 补充服务层测试，覆盖充值成功和非正数金额拒绝。

### 修改文件

1. `backend/src/main/java/com/youou/aso/modules/wallet/dto/AdminRechargeCommand.java`
2. `backend/src/main/java/com/youou/aso/modules/wallet/service/AdminWalletService.java`
3. `backend/src/main/java/com/youou/aso/modules/wallet/repository/AdminWalletRepository.java`
4. `backend/src/main/java/com/youou/aso/modules/wallet/repository/JdbcAdminWalletRepository.java`
5. `backend/src/main/java/com/youou/aso/modules/wallet/api/AdminWalletController.java`
6. `backend/src/test/java/com/youou/aso/modules/wallet/service/AdminWalletServiceTest.java`
7. `PROJECT_PROGRESS.md`

### 验证结果

1. `mvn.cmd "-Dtest=AdminWalletServiceTest" test`：先 RED，缺少充值命令、服务和仓储接口；实现后通过，2 个管理员充值服务测试成功。
2. `mvn.cmd test`：通过，42 个后端测试成功，订单、钱包查询、应用管理、价格配置和 Spring Boot 上下文未回归。
3. `mvn.cmd package -DskipTests`：通过，已重新生成后端 jar。
4. 已停止旧后端进程 `4148`，启动新后端进程 `15352`；未登录访问 `POST /api/admin/wallet/recharge` 返回 403，说明新接口已响应且鉴权生效。

### 下一步任务清单

1. 前端财务管理页接入管理员充值按钮/弹窗，让运营可以在页面中为用户充值。
2. 使用管理员账号调用充值接口，为测试用户充值后创建普通订单，验证扣款和钱包流水。
3. 如后续需要管理员余额调整或退款能力，可基于本次仓储和服务模式扩展 `ADMIN_ADJUSTMENT` 或 `ORDER_REFUND`，但需单独确认业务规则。

### 风险点

1. 目前只补了后端充值接口，前端财务页尚未接入充值操作入口。
2. 充值接口允许管理员直接增加余额，必须依赖管理员鉴权和后续审计；如需要更严格生产审计，可继续补操作日志关联。

## 2026-06-20 更新：执行中订单到期自动完成

- 状态：已完成后端代码修改、自动化验证、本地后端重新打包与重启，待用真实订单数据确认到期后列表自动转为已完成。
- 当前任务：补齐订单生命周期要求，执行中的订单到达预计完成时间后自动变为已完成。
- 范围控制：未新增后台定时任务，未修改数据库结构，未修改路由，未修改 `.env` 或秘钥配置，未安装依赖，未删除文件。

### 实施内容

1. 新增订单服务测试，覆盖执行中订单已过预计完成时间时，管理员订单查询会先将其转为 `COMPLETED`，并从 `EXECUTING` 筛选结果中移除。
2. 订单仓储新增 `findExecutingDueBefore`，按 `status = EXECUTING` 和 `expected_completed_at <= 当前时间` 查找已到期订单。
3. 用户端和管理员端订单列表查询前会在同一事务内归档到期的执行中订单。
4. 归档时写入 `completedAt`，不改变执行人和执行时间，保留原执行记录。
5. 未引入调度器，避免修改后台任务；本次采用“列表查询时自动归档”的最小生产可用闭环。

### 修改文件

1. `backend/src/main/java/com/youou/aso/modules/order/repository/OrderRepository.java`
2. `backend/src/main/java/com/youou/aso/modules/order/repository/JdbcOrderRepository.java`
3. `backend/src/main/java/com/youou/aso/modules/order/service/OrderService.java`
4. `backend/src/test/java/com/youou/aso/modules/order/service/OrderServiceTest.java`
5. `backend/src/test/java/com/youou/aso/modules/order/service/SpecialOrderAuditServiceTest.java`
6. `PROJECT_PROGRESS.md`

### 验证结果

1. `mvn.cmd "-Dtest=OrderServiceTest" test`：先 RED，过期执行中订单仍出现在执行中查询结果；实现后通过，13 个订单服务测试成功。
2. `mvn.cmd test`：通过，40 个后端测试成功，特殊订单审核、钱包、价格、应用管理和 Spring Boot 上下文未回归。
3. `mvn.cmd package -DskipTests`：通过，已重新生成后端 jar。
4. 已停止旧后端进程 `12296`，启动新后端进程 `4148`；未登录访问 `GET /api/admin/orders` 返回 403，说明新后端已响应且鉴权生效。

### 下一步任务清单

1. 在真实订单数据中准备一笔 `EXECUTING` 且 `expected_completed_at` 已过的订单，刷新用户端或管理员端订单列表，确认状态自动变为已完成。
2. 检查执行中筛选结果不再包含已到期订单，已完成筛选结果可以查到该订单。
3. 后续如需要真正脱离用户访问的自动完成，可单独确认是否允许新增后台定时任务；本次没有引入该类架构变更。

### 风险点

1. 当前自动完成触发点是订单列表查询，不是后台定时器；如果长时间无人访问订单列表，数据库状态会在下一次查询时更新。
2. 每次列表查询会额外扫描已到期执行中订单；当前 SQL 有明确状态和时间条件，后续数据量大时可评估索引或专用任务。

## 2026-06-20 更新：待确认订单页查询筛选栏补齐

- 状态：已完成前端代码修改与构建验证，待在管理员账号中确认待确认订单页筛选和导出行为。
- 当前任务：继续完善管理员端订单二级菜单中的待确认订单页，让该页具备与订单查询参考图一致的筛选能力。
- 范围控制：未新增后端接口，未修改数据库，未修改路由，未修改 `.env` 或秘钥配置，未安装依赖，未删除文件。

### 实施内容

1. 管理员待确认订单页新增查询筛选栏，支持内容筛选、应用、商店、国家/地区、任务类型、订单类型、订单时间、创建时间。
2. 查询时状态固定为 `PENDING_CONFIRM`，保证该页面仍然只展示待确认订单。
3. 应用筛选复用 `getAdminApps()`，地区筛选复用 `getEnabledRegions()`，与管理员订单页保持稳定选项来源。
4. 商店和地区下拉补齐 `ALL`、商店代码、地区代码小标识，与普通订单页视觉一致。
5. 清空按钮会重置筛选条件并重新加载待确认订单。
6. 导出仍导出当前筛选结果，避免导出与页面显示不一致。

### 修改文件

1. `frontend/src/views/admin/PendingConfirmOrdersView.vue`
2. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 构建成功。
2. 构建仍存在既有 Rollup PURE 注释提示、主 chunk 超过 500k、npm 新版本提示，均非本次新增且非阻断项。

### 下一步任务清单

1. 使用管理员账号进入待确认订单页，按应用、商店、地区、任务类型筛选，确认列表始终只返回待确认订单。
2. 在筛选后导出 CSV，确认导出内容与当前页面结果一致。
3. 继续检查待确认订单页和普通订单页的表格列宽、操作按钮、空状态在真实数据下是否一致。

### 风险点

1. 待确认订单页现在会额外加载应用和地区选项，与管理员订单页一致；若后台应用数量很大，后续可考虑专用轻量筛选选项接口。
2. 页面筛选栏变完整后高度增加，移动端会更长；已复用响应式换行规则避免横向溢出。

## 2026-06-20 更新：订单查询筛选栏响应式收口

- 状态：已完成前端样式调整与构建验证，待在浏览器中检查中等宽度和移动端实际排布。
- 当前任务：继续优化订单查询参考图相关页面，解决筛选项、日期范围和按钮在窄屏下挤压的问题。
- 范围控制：仅修改订单列表页样式，未修改后端接口，未修改数据库，未修改路由，未修改 `.env` 或秘钥配置，未安装依赖，未删除文件。

### 实施内容

1. 用户端订单页筛选栏增加中等宽度断点，收紧筛选项间距并压缩日期范围控件宽度。
2. 用户端订单页移动端改为筛选项整行堆叠，输入框、下拉框、日期控件全宽显示。
3. 用户端订单页极窄屏下按钮自动两列换行，避免按钮文字互相挤压。
4. 管理员端订单页同步同一套响应式规则，并针对按钮更多的情况开启按钮自动换行。

### 修改文件

1. `frontend/src/views/user/StoreOrdersView.vue`
2. `frontend/src/views/admin/StoreOrdersView.vue`
3. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 构建成功。
2. 构建仍存在既有 Rollup PURE 注释提示、主 chunk 超过 500k、npm 新版本提示，均非本次新增且非阻断项。

### 下一步任务清单

1. 在浏览器中用桌面宽度、中等宽度和手机宽度分别检查用户端订单页筛选栏。
2. 在浏览器中检查管理员端订单页，重点看“批量执行”和“导出”等按钮在窄屏下是否换行自然。
3. 如果浏览器插件恢复可用，补截图级视觉审查；否则继续用构建和人工页面检查推进。

### 风险点

1. 极窄屏下按钮变成两列，适合触控但会增加筛选栏高度；这是避免文字溢出的取舍。
2. 日期范围控件在中等宽度下宽度略缩小，需用真实浏览器确认占位文案不会显得过挤。

## 2026-06-20 更新：订单查询地区筛选选项稳定化

- 状态：已完成前端代码修改与构建验证，待在浏览器中确认地区下拉不再受当前订单结果影响。
- 当前任务：继续完善订单查询参考图中的筛选体验，将地区筛选从订单结果提取改为复用启用地区接口。
- 范围控制：未新增后端接口，未修改数据库，未修改路由，未修改 `.env` 或秘钥配置，未安装依赖，未删除文件。

### 实施内容

1. 用户端订单页新增启用地区列表加载，复用 `getEnabledRegions()` 作为地区筛选稳定来源。
2. 管理员端订单页同步使用启用地区列表作为地区筛选稳定来源。
3. 地区选项会根据当前商店筛选自动过滤支持范围：App Store、Google Play、iPad Store 分别使用地区配置中的支持字段。
4. 当已选地区不支持切换后的商店时，自动清空地区筛选，避免提交矛盾查询条件。
5. 补齐中英文“地区筛选加载失败”文案。

### 修改文件

1. `frontend/src/views/user/StoreOrdersView.vue`
2. `frontend/src/views/admin/StoreOrdersView.vue`
3. `frontend/src/i18n/locales/zh-CN.ts`
4. `frontend/src/i18n/locales/en-US.ts`
5. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 构建成功。
2. 构建仍存在既有 Rollup PURE 注释提示、主 chunk 超过 500k、npm 新版本提示，均非本次新增且非阻断项。

### 下一步任务清单

1. 在用户端和管理员端订单页筛选不同商店，确认地区下拉随商店支持范围变化。
2. 确认订单列表为空时，地区下拉仍能显示启用地区，而不是只剩当前结果中的地区。
3. 继续检查订单查询栏移动端布局，必要时压缩间距和日期控件宽度。

### 风险点

1. 订单页现在会额外请求一次启用地区接口；该接口已有应用管理页复用，属于可接受的小范围复用。
2. 地区下拉展示的是系统启用地区，不一定代表当前账号已有订单地区；这是筛选能力更完整的取舍。

## 2026-06-20 更新：订单查询筛选下拉视觉标识

- 状态：已完成前端代码修改与构建验证，待在浏览器中确认筛选下拉展开后的视觉效果。
- 当前任务：继续对齐订单查询参考图，为订单页商店和地区筛选下拉补充可扫描的小标识。
- 范围控制：未修改后端接口，未修改数据库，未修改路由，未修改 `.env` 或秘钥配置，未安装依赖，未删除文件。

### 实施内容

1. 用户端订单页商店筛选下拉新增 `ALL`、`APP`、`GP`、`iPad` 小标识。
2. 用户端订单页地区筛选下拉新增 `ALL` 和地区代码小标识。
3. 管理员端订单页同步同一套商店和地区筛选下拉视觉标识。
4. 标识颜色与页面已有商店/地区标签保持一致：全部为橙色提示，商店为蓝色，地区为灰色。

### 修改文件

1. `frontend/src/views/user/StoreOrdersView.vue`
2. `frontend/src/views/admin/StoreOrdersView.vue`
3. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 构建成功。
2. 构建仍存在既有 Rollup PURE 注释提示、主 chunk 超过 500k、npm 新版本提示，均非本次新增且非阻断项。

### 下一步任务清单

1. 在浏览器中打开用户端和管理员端订单页，展开商店/地区下拉，确认 `ALL` 和代码标识对齐参考图。
2. 继续优化订单筛选栏整体间距与响应式换行，避免小屏下按钮和日期控件挤压。
3. 若浏览器插件恢复可用，补一次截图级视觉审查；当前仍以构建和本地页面访问作为基础验证。

### 风险点

1. 下拉选中后的输入框仍显示 Element Plus 的普通 label，不显示自定义小标，这是当前最小改动；如需选中态也带小标，需要改成自定义 select 触发器。
2. 地区选项目前来自当前订单结果；如果筛选结果为空，地区下拉只剩“全部”，后续可考虑像应用筛选一样改为独立地区选项来源。

## 2026-06-20 更新：订单表格应用信息视觉统一

- 状态：已完成前端代码修改与构建验证，待在浏览器中确认用户端订单页、管理员订单页、待确认订单页的表格观感。
- 当前任务：继续对齐订单查询参考图，在不新增表格列的前提下提升订单列表信息密度和专业感。
- 范围控制：未修改后端接口，未修改数据库，未修改路由，未修改 `.env` 或秘钥配置，未安装依赖，未删除文件。

### 实施内容

1. 用户端订单页应用单元格改为展示应用名、订单号、App ID/Bundle ID、商店标签、地区标签。
2. 管理员端订单页应用单元格同步展示订单号、App 标识、用户 ID、商店标签、地区标签。
3. 管理员待确认订单页同步同一套应用单元格结构，并去掉重复的订单号和商店独立列，避免信息重复和表格过宽。
4. 创建时间显示统一格式化，避免原始时间字符串中的 `T` 影响表格观感。
5. 补齐中英文 `customerIdShort` 文案，用于管理员订单列表中的用户标识。

### 修改文件

1. `frontend/src/views/user/StoreOrdersView.vue`
2. `frontend/src/views/admin/StoreOrdersView.vue`
3. `frontend/src/views/admin/PendingConfirmOrdersView.vue`
4. `frontend/src/i18n/locales/zh-CN.ts`
5. `frontend/src/i18n/locales/en-US.ts`
6. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 构建成功。
2. 构建仍存在既有 Rollup PURE 注释提示、主 chunk 超过 500k、npm 新版本提示，均非本次新增且非阻断项。

### 下一步任务清单

1. 在浏览器中检查用户端苹果/谷歌/iPad订单页，确认应用单元格信息不拥挤、不换行异常。
2. 在浏览器中检查管理员订单页和待确认订单页，确认用户 ID、订单号、商店和地区标签便于扫描。
3. 继续按参考图补订单页筛选栏视觉细节，例如商店下拉图标、地区显示、按钮间距和移动端换行。

### 风险点

1. 应用单元格承载的信息更多，极窄屏下可能出现行高增加；已使用换行和省略控制，仍需浏览器确认真实数据效果。
2. 待确认订单页去掉了独立订单号和商店列，但同样信息已进入应用单元格；如果运营更偏好独立列，可再恢复为可视列。

## 2026-06-20 更新：订单查询应用筛选选项稳定化

- 状态：已完成前端代码修改与构建验证，待在真实账号订单页中确认应用下拉选项和查询结果。
- 当前任务：继续对齐订单查询参考图，将用户端和管理员端订单页的“应用”筛选从当前订单结果中提取，改为从应用管理接口加载稳定选项。
- 范围控制：未修改数据库，未修改路由，未修改后端订单接口，未修改 `.env` 或秘钥配置，未安装依赖，未删除文件。

### 实施内容

1. 用户端订单页复用 `getCustomerApps()` 加载当前用户全部有效应用，作为应用筛选下拉选项。
2. 管理员端订单页复用 `getAdminApps()` 加载全部有效应用，作为应用筛选下拉选项。
3. 应用筛选选项会根据当前商店筛选自动收窄；如果已选应用与切换后的商店不匹配，会自动清空应用筛选，避免提交矛盾查询条件。
4. 应用下拉选项展示应用名和 App ID/Bundle ID 等标识，便于多应用场景下区分。
5. 补齐中英文“应用筛选加载失败”文案，避免接口失败时出现原始 i18n key。

### 修改文件

1. `frontend/src/views/user/StoreOrdersView.vue`
2. `frontend/src/views/admin/StoreOrdersView.vue`
3. `frontend/src/i18n/locales/zh-CN.ts`
4. `frontend/src/i18n/locales/en-US.ts`
5. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 构建成功。
2. 构建仍存在既有 Rollup PURE 注释提示、主 chunk 超过 500k、npm 新版本提示，均非本次新增且非阻断项。

### 下一步任务清单

1. 在真实用户账号订单页检查应用下拉：无论当前订单筛选结果是否为空，都应能看到该商店下已有应用。
2. 在管理员订单页检查应用下拉：应能看到全部有效应用，并随商店筛选联动。
3. 继续补订单查询表格展示细节，例如商店/地区视觉标识、金额/订单号展示密度、用户端订单信息列是否需要更接近参考图。
4. 如需更严格验证，可后续补前端组件测试；当前项目尚未配置前端测试脚本，本次以构建作为验证门槛。

### 风险点

1. 订单页现在会额外请求一次应用列表接口；如果账号应用量很大，后续可考虑后端分页或专用轻量筛选选项接口。
2. 应用筛选只加载有效应用；历史订单如果关联的是已禁用应用，仍能通过内容筛选查询，但不会出现在应用下拉中。

## 2026-06-20 更新：管理员待执行订单批量执行后端化

- 状态：已完成代码修改与自动化验证，待在真实管理员账号中刷新待执行订单页确认批量执行交互。
- 当前任务：将管理员待执行订单列表的“批量执行”从前端逐条循环调用改为后端单接口批量处理，避免部分订单执行成功、部分订单失败造成状态不一致。
- 范围控制：未新增数据库迁移，未修改前端页面路由，未修改 `.env` 或秘钥配置，未安装依赖，未删除文件。

### 实施内容

1. 后端订单服务新增 `executeOrders` 批量执行方法，统一校验订单 ID 列表不能为空。
2. 批量执行会先完整读取并预校验所有订单，只有全部订单均为 `PENDING_EXECUTION` 时才更新，避免部分成功。
3. 批量执行复用单笔执行规则：未到期订单进入 `EXECUTING`，已到期订单直接进入 `COMPLETED`。
4. 管理员订单接口新增既有订单模块下的 `POST /api/admin/orders/batch-execute`，返回批量执行后的订单结果。
5. 前端管理员订单页批量执行改为调用单个后端批量接口，不再对每个订单发起独立执行请求。
6. 增加服务层测试覆盖批量执行成功、到期直接完成、包含非待执行订单时整体拒绝并保持未更新。

### 修改文件

1. `backend/src/main/java/com/youou/aso/modules/order/service/OrderService.java`
2. `backend/src/main/java/com/youou/aso/modules/order/api/AdminOrderController.java`
3. `backend/src/test/java/com/youou/aso/modules/order/service/OrderServiceTest.java`
4. `frontend/src/api/orders.ts`
5. `frontend/src/views/admin/StoreOrdersView.vue`
6. `PROJECT_PROGRESS.md`

### 验证结果

1. `mvn.cmd "-Dtest=OrderServiceTest" test`：先 RED，因 `executeOrders` 尚未实现导致编译失败；实现后通过，12 个订单服务测试成功。
2. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 构建成功；仍存在既有 Rollup PURE 注释提示、主 chunk 超过 500k、npm 新版本提示，均非阻断项。
3. `mvn.cmd test`：通过，39 个后端测试成功，订单批量执行与现有订单、特价审核流程未出现回归。
4. `mvn.cmd package -DskipTests`：通过，已重新生成后端 jar。
5. 已停止旧后端进程 `6640`，并启动新后端进程 `12296`；未登录访问 `POST /api/admin/orders/batch-execute` 返回 403，说明新接口已由本地后端响应且鉴权生效。

### 下一步任务清单

1. 使用真实管理员账号进入待执行订单列表，勾选多笔待执行订单，确认批量执行成功后状态进入执行中或已完成。
2. 使用包含非待执行订单的选择集进行页面验证，确认前端提示失败且后端不会产生部分执行。
3. 继续补齐订单列表筛选条件与用户端订单页面细节，保持与参考图一致。

### 风险点

1. 批量执行接口依赖服务层事务与预校验保证整体一致性；如果后续替换仓储实现，需要继续保留“先校验、后更新”的顺序。
2. 当前前端只传递选中订单 ID，不附带页面筛选条件；这是符合批量操作语义的最小变更。
3. 当前本地后端已重启到新 jar；如果之后再次修改后端代码，仍需重新打包重启才会在页面生效。

## 2026-06-20 更新：特殊订单审核与正式提交扣款闭环

- 状态：已完成代码修改、自动化验证和本地后端重启，待用户在真实账号中走一遍提交需求、管理员审核、用户支付提交正式订单流程。
- 当前任务：继续推进关键词保排名、关键词覆盖两类特殊订单，从“暂未开放”补成可提交需求、管理员锁定协商内容和价格、用户确认后扣余额并生成正式订单。
- 范围控制：未新增数据库迁移，复用既有 `special_order_audit` 表；未修改 `.env` 或秘钥配置，未安装依赖，未新增菜单路由。

### 实施内容

1. 新增特殊订单审核领域对象、仓储接口、JDBC 仓储、结果 DTO 和提交/审核命令 DTO。
2. 新增特殊订单审核服务，支持用户提交需求、管理员审核通过、管理员取消、用户提交已审核需求为正式订单。
3. 用户提交特殊需求时不扣款，状态为 `PENDING_REVIEW`。
4. 管理员审核通过时写入协商内容和价格，状态为 `APPROVED_WAIT_SUBMIT`；审核后价格和内容只通过正式提交使用，不再在用户提交时重算。
5. 用户提交已审核需求时自动扣余额，生成正式 `aso_order`，状态为 `PENDING_CONFIRM`，并写入单条订单明细；余额不足时不生成订单。
6. 管理员取消未提交需求时统一进入 `CANCELLED` 状态，取消原因必填。
7. 新增用户端特殊审核接口和管理员端特殊审核接口。
8. 前端订单创建页允许选择关键词保排名/关键词覆盖，特殊订单显示需求内容输入框并提交审核需求。
9. 推广服务页展示“我的特殊订单需求”，审核通过后提供“提交订单”按钮。
10. 管理员审核管理页从占位页改为特殊订单审核表格，支持审核通过和取消。
11. 补齐中英文文案与特殊审核状态展示。

### 修改文件

1. `backend/src/main/java/com/youou/aso/modules/order/domain/SpecialOrderAudit.java`
2. `backend/src/main/java/com/youou/aso/modules/order/dto/SubmitSpecialAuditCommand.java`
3. `backend/src/main/java/com/youou/aso/modules/order/dto/ReviewSpecialAuditCommand.java`
4. `backend/src/main/java/com/youou/aso/modules/order/dto/SpecialOrderAuditResult.java`
5. `backend/src/main/java/com/youou/aso/modules/order/repository/SpecialOrderAuditRepository.java`
6. `backend/src/main/java/com/youou/aso/modules/order/repository/JdbcSpecialOrderAuditRepository.java`
7. `backend/src/main/java/com/youou/aso/modules/order/service/SpecialOrderAuditService.java`
8. `backend/src/main/java/com/youou/aso/modules/order/api/CustomerSpecialOrderAuditController.java`
9. `backend/src/main/java/com/youou/aso/modules/order/api/AdminSpecialOrderAuditController.java`
10. `backend/src/test/java/com/youou/aso/modules/order/service/SpecialOrderAuditServiceTest.java`
11. `frontend/src/api/specialOrderAudits.ts`
12. `frontend/src/views/user/OrderCreateView.vue`
13. `frontend/src/views/user/PromotionServicesView.vue`
14. `frontend/src/views/admin/AuditManagementView.vue`
15. `frontend/src/i18n/locales/zh-CN.ts`
16. `frontend/src/i18n/locales/en-US.ts`
17. `PROJECT_PROGRESS.md`

### 验证结果

1. `mvn.cmd "-Dtest=SpecialOrderAuditServiceTest" test`：先 RED，缺少特殊审核领域对象、DTO、仓储和服务时编译失败；实现后通过，5 个特殊订单服务测试成功。
2. `mvn.cmd "-Dtest=OrderServiceTest" test`：通过，10 个普通订单服务测试成功。
3. `mvn.cmd test`：通过，37 个后端测试成功，Spring Boot 上下文启动成功，Flyway 校验当前 schema 为 V9 且无需迁移。
4. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 构建成功；仍存在既有 Rollup PURE 注释提示、主 chunk 超过 500k、npm 新版本提示，均非阻断项。
5. `mvn.cmd package -DskipTests`：第一次因旧后端 jar 被 Java 进程占用失败；停止旧进程后重新执行通过。
6. 已启动新的本地后端 Java 进程，访问 `http://127.0.0.1:8080/api/customer/special-order-audits` 返回 403，说明接口已由新后端响应且鉴权生效。
7. 继续验证：通过公开注册/登录接口创建临时用户并登录，`GET /api/customer/special-order-audits` 返回成功且空列表；使用无效应用提交特殊需求返回 400，说明接口进入鉴权后的业务校验。
8. 继续验证：内置浏览器插件在打开本地页面时连续断开，未能完成 browser 页面截图验证；已用 HTTP API、`mvn.cmd "-Dtest=SpecialOrderAuditServiceTest" test` 和 `npm.cmd run build` 兜底验证。

### 下一步任务清单

1. 使用真实用户账号在推广服务页选择关键词保排名或关键词覆盖，提交特殊需求。
2. 使用管理员账号进入审核管理页，填写协商内容和价格并审核通过。
3. 回到用户推广服务页，确认状态显示“已审核待支付”，点击“提交订单”后检查余额扣减和正式订单进入待确认。
4. 在管理员待确认订单页确认特殊订单，再走待执行、执行中、完成流程。
5. 后续如特殊订单需要明确服务周期，应确认是否扩展 `special_order_audit` 表增加日期/周期字段；本次暂按正式提交当天到次日 0 点作为执行日期。

### 风险点

1. 特殊审核表当前没有日期字段，本次正式订单日期按提交当天生成，适合作为最小闭环；若特殊服务有长期执行周期，应单独确认数据库扩展。
2. 钱包扣款事务在正式提交时发生，余额不足时不会生成订单；但本地真实数据仍需用账号余额验证一次。
3. 管理员审核页这次只实现核心表格和审核/取消操作，暂未加复杂筛选和导出。

## 2026-06-20 更新：订单明细持久化与订单导出明细列

- 状态：已完成代码修改与验证，待用户在真实页面中确认导出 CSV 内容是否满足运营使用。
- 当前任务：继续推进订单功能，将正式订单的关键词、下载量、评分、评论等计费明细写入既有 `aso_order_item` 表，并在用户端/管理员端订单导出中带出明细。
- 范围控制：未新增数据库迁移，未修改 `.env` 或秘钥配置，未安装依赖，未删除文件；本次使用既有 `aso_order_item` 表结构。

### 实施内容

1. 后端新增订单明细领域对象、仓储接口与 JDBC 实现，订单创建后批量写入 `aso_order_item`。
2. 订单查询时按订单 ID 批量读取明细并挂载到订单结果，避免每行订单单独查询。
3. 关键词安装按关键词生成明细项，费用仍为关键词数量乘单价，不受执行小时影响。
4. 下载量生成单条下载明细，数量为天数乘每日下载量，保留每日下载量 metadata。
5. 评分和评论按 5 星、4 星分别生成明细项，使用各自价格计算金额。
6. 后端订单 DTO 返回 `items` 明细数组，前端订单类型同步扩展。
7. 用户端订单列表、管理员订单列表、待确认订单页的 CSV 导出新增“明细 / Details”列。
8. 补齐订单明细类型的中英文文案，避免导出出现原始枚举 key。

### 修改文件

1. `backend/src/main/java/com/youou/aso/modules/order/domain/OrderItem.java`
2. `backend/src/main/java/com/youou/aso/modules/order/domain/AsoOrder.java`
3. `backend/src/main/java/com/youou/aso/modules/order/dto/OrderItemResult.java`
4. `backend/src/main/java/com/youou/aso/modules/order/dto/OrderResult.java`
5. `backend/src/main/java/com/youou/aso/modules/order/repository/OrderItemRepository.java`
6. `backend/src/main/java/com/youou/aso/modules/order/repository/JdbcOrderItemRepository.java`
7. `backend/src/main/java/com/youou/aso/modules/order/service/OrderService.java`
8. `backend/src/test/java/com/youou/aso/modules/order/service/OrderServiceTest.java`
9. `frontend/src/api/orders.ts`
10. `frontend/src/views/user/StoreOrdersView.vue`
11. `frontend/src/views/admin/StoreOrdersView.vue`
12. `frontend/src/views/admin/PendingConfirmOrdersView.vue`
13. `frontend/src/i18n/locales/zh-CN.ts`
14. `frontend/src/i18n/locales/en-US.ts`
15. `PROJECT_PROGRESS.md`

### 验证结果

1. `mvn.cmd "-Dtest=OrderServiceTest" test`：通过，10 个订单服务测试成功。
2. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 构建成功；仍存在既有 Rollup PURE 注释提示、主 chunk 超过 500k、npm 新版本提示，均非阻断项。
3. `mvn.cmd test`：通过，32 个后端测试成功，Spring Boot 上下文启动成功，Flyway 校验当前 schema 为 V9 且无需迁移。

### 下一步任务清单

1. 在真实页面创建一笔关键词安装、下载量、评分或评论订单，确认 `aso_order_item` 有对应明细记录。
2. 在用户端订单页面和管理员端订单页面导出 CSV，确认“明细”列符合运营查看习惯。
3. 后续可继续补订单详情抽屉/详情页，让页面内也能查看明细，而不只在导出中查看。
4. 继续完善特殊订单审核通过后正式提交、自动扣余额、取消流程和用户可见状态。

### 风险点

1. 历史订单没有 `aso_order_item` 明细，导出明细列会为空，这是预期兼容行为。
2. 当前下载量 metadata 只保存每日下载量，后续如果需要地区分拆、关键词扩展配置，应在不破坏既有结构的前提下扩展 metadata。
3. 本次没有改表格可视列，明细只进入 CSV；如需页面内展示，需要单独确认交互方式。

## 2026-06-20 更新：应用列表分类字段替代状态展示

- 状态：已完成代码修改与验证，待用户在浏览器刷新页面确认视觉效果。
- 当前任务：应用管理列表不再展示状态字段，改为展示真实应用分类，保持参考图中的“分类”列方向。
- 用户确认：本次需要数据库结构调整，已获得确认。

### 实施内容

1. 后端 `customer_app` 新增 `category` 字段，并新增 Flyway 迁移 `V4__customer_app_category.sql`。
2. 创建应用时保存应用商店返回或解析得到的分类；Apple 使用 iTunes 返回的 `primaryGenreName`，Google Play 尽量从公开页面元数据解析分类，解析不到则为空。
3. 后端应用列表结果新增 `category`，内部 `status` 仍保留用于软删除和列表过滤。
4. 用户端和管理员端应用列表将“状态”列替换为“分类”列，分类为空时显示 `-`。
5. 前端接口类型与中英文文案补齐 `category`。
6. 应用查询关键字支持匹配分类字段。

### 修改文件

1. `backend/src/main/java/com/youou/aso/modules/appmanagement/domain/CustomerApp.java`
2. `backend/src/main/java/com/youou/aso/modules/appmanagement/dto/CustomerAppResult.java`
3. `backend/src/main/java/com/youou/aso/modules/appmanagement/service/VerifiedStoreApp.java`
4. `backend/src/main/java/com/youou/aso/modules/appmanagement/service/StoreAppSearchResult.java`
5. `backend/src/main/java/com/youou/aso/modules/appmanagement/service/CustomerAppService.java`
6. `backend/src/main/java/com/youou/aso/modules/appmanagement/service/HttpAppStoreVerifier.java`
7. `backend/src/main/java/com/youou/aso/modules/appmanagement/repository/JdbcCustomerAppRepository.java`
8. `backend/src/main/resources/db/migration/V4__customer_app_category.sql`
9. `backend/src/test/java/com/youou/aso/modules/appmanagement/service/CustomerAppServiceTest.java`
10. `frontend/src/api/applications.ts`
11. `frontend/src/views/user/ApplicationManagementView.vue`
12. `frontend/src/views/admin/ApplicationManagementView.vue`
13. `frontend/src/i18n/locales/zh-CN.ts`
14. `frontend/src/i18n/locales/en-US.ts`
15. `PROJECT_PROGRESS.md`

### 验证结果

1. `mvn.cmd -Dtest=CustomerAppServiceTest test`：通过，6 个应用管理服务测试成功。
2. `mvn.cmd test`：通过，12 个后端测试成功；Flyway 已将本地 MySQL `youou_aso` schema 从 v3 迁移到 v4。
3. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 构建成功。

### 下一步任务清单

1. 刷新用户端 `/user/applications`，确认应用列表表头显示“分类”，不再显示“状态/已校验”。
2. 刷新管理员端应用管理页，确认同样显示“分类”列。
3. 新增或重新保存一个 App Store 应用，确认分类能随真实应用信息落库并展示。
4. 若 Google Play 分类为空，后续可评估接入更稳定的应用元数据服务或缓存策略。

### 风险点

1. Apple 分类来源稳定；Google Play 分类依赖公开页面结构，页面结构变化时可能解析不到，系统会降级为空值。
2. 本次保留后端 `status` 字段用于软删除，不在列表展示，避免破坏现有应用隐藏逻辑。
3. 本地数据库已经执行 v4 迁移；如果当前运行中的后端进程未重启，页面接口仍可能使用旧代码，需要重启后端生效。

## 2026-06-20 更新：应用列表筛选与表格视觉参考图调整

- 状态：已完成前端代码修改与构建验证，待用户刷新页面确认视觉效果。
- 当前任务：应用管理页参考截图样式，筛选下拉加图标，筛选项只显示当前应用资产中存在的数据，并调整表格列宽比例。

### 实施内容

1. 用户端应用管理页的商店筛选下拉加入系统主色图标：全部、App Store、Google Play、iPad Store。
2. 管理员端应用管理页同步加入商店筛选图标。
3. 用户端和管理员端地区筛选下拉加入国旗图标，地区选项只来自当前已有应用的地区，不再展示系统维护的全部地区。
4. 商店筛选选项只来自当前已有应用的商店，不再固定展示全部商店类型。
5. 用户端和管理员端表格的商店列、国家/地区列加入对应图标。
6. 用户端表格参考截图调整列宽比例：应用列内置图标，商店/国家地区/分类列拉开，操作列收窄；移除列表中的校验时间列，让列结构更接近参考图。
7. `ALL` 标识改为系统主色蓝系，不再固定橙色。

### 修改文件

1. `frontend/src/views/user/ApplicationManagementView.vue`
2. `frontend/src/views/admin/ApplicationManagementView.vue`
3. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 构建成功。

### 下一步任务清单

1. 刷新用户端 `/user/applications`，确认筛选下拉图标、地区国旗和列宽比例是否接近参考图。
2. 刷新管理员端应用管理页，确认筛选下拉和表格图标是否一致。
3. 若希望管理员端也完全采用参考图列结构，可进一步确认是否移除管理员端列表里的校验时间列。

### 风险点

1. 筛选选项来自当前应用资产；当用户尚未添加任何应用时，商店和地区下拉只会显示“全部”。
2. 国旗图标按两位国家/地区代码生成；非国家代码或特殊地区代码会回退显示代码本身。
3. 前端构建仍存在既有 Rollup PURE 注释提示和主 chunk 超过 500k 的非阻断警告，本次未处理。

## 2026-06-20 更新：地区国旗图标显示修复

- 状态：已完成前端代码修改与构建验证。
- 当前任务：地区下拉与列表国家/地区列在国家名称前显示真实国旗图标，避免浏览器将 emoji 国旗降级显示为 `US` 等字母。

### 实施内容

1. 用户端应用管理页将地区图标从 emoji 文本改为实际国旗图片。
2. 管理员端应用管理页同步将地区图标改为实际国旗图片。
3. 国旗图片尺寸统一为 22x16，并增加轻量边框，保证下拉与表格内视觉稳定。

### 修改文件

1. `frontend/src/views/user/ApplicationManagementView.vue`
2. `frontend/src/views/admin/ApplicationManagementView.vue`
3. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 构建成功。

### 下一步任务清单

1. 刷新用户端 `/user/applications`，打开国家/地区下拉，确认“美国”等地区前显示国旗图片。
2. 刷新管理员端应用管理页，确认地区下拉与表格列显示一致。

### 风险点

1. 当前国旗图片使用公开 CDN 地址生成，若生产环境要求完全内网或禁止外链图片，后续应将国旗资源本地化到前端静态资源中。
2. 前端构建仍存在既有 Rollup PURE 注释提示和主 chunk 超过 500k 的非阻断警告，本次未处理。

## 2026-06-20 更新：添加应用弹窗分类展示修复

- 状态：已完成代码修改与构建验证，待用户刷新页面确认。
- 当前任务：修复添加应用时搜索结果和选中应用预览没有显示分类字段的问题。

### 根因

后端搜索接口和前端类型已经读取并返回 `category` 字段，应用列表也已经使用该字段；但添加应用弹窗的搜索结果项和选中应用预览区域没有渲染 `category`，因此看起来像“没有读取出来”。

### 实施内容

1. 用户端添加应用弹窗的搜索结果中显示应用分类。
2. 用户选中搜索结果后，在选中应用预览中继续显示应用分类。
3. 为分类文本补充轻量样式，保持与现有弹窗信息层级一致。

### 修改文件

1. `frontend/src/views/user/ApplicationManagementView.vue`
2. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 构建成功。

### 下一步任务清单

1. 刷新用户端 `/user/applications`，打开添加应用弹窗。
2. 通过应用名称搜索真实应用，确认搜索结果中显示分类。
3. 选中应用后，确认选中预览区域显示分类，并且搜索列表自动收起。
4. 保存应用后，确认应用列表分类列正常显示。

### 风险点

1. 如果用户不从搜索结果中选择应用，而是直接输入 App ID 或 Bundle ID 提交，弹窗提交前不会展示分类；分类会在后端校验真实应用并保存后，随列表刷新显示。
2. Google Play 分类依赖公开页面元数据解析，页面结构变化时可能解析为空；Apple App Store 分类来源相对稳定。
3. 如果浏览器仍连接旧前端资源或旧后端进程，需要刷新页面或重启对应服务后才能看到最新效果。

## 2026-06-20 更新：添加应用弹窗保存按钮文案调整

- 状态：已完成代码修改，待构建验证与用户刷新确认。
- 当前任务：添加应用弹窗中按钮不再显示“校验并保存”，改为更符合当前交互的“保存”。

### 实施内容

1. 中文按钮文案从“校验并保存”调整为“保存”。
2. 英文按钮文案从“Verify and save”调整为“Save”。
3. 保持现有保存流程、接口校验和真实应用入库逻辑不变，仅调整用户可见文案。

### 修改文件

1. `frontend/src/i18n/locales/zh-CN.ts`
2. `frontend/src/i18n/locales/en-US.ts`
3. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 构建成功。

### 下一步任务清单

1. 刷新用户端 `/user/applications`，打开添加应用弹窗。
2. 确认底部主按钮显示为“保存”。
3. 切换英文语言时，确认按钮显示为“Save”。

### 风险点

1. 本次只改按钮显示文案，不改变后端仍会在保存时做真实应用校验的生产安全逻辑。

## 2026-06-20 更新：新增应用分类入库兜底修复

- 状态：已完成代码修改，待测试验证与用户刷新确认。
- 当前任务：修复搜索结果中有分类，但新增应用保存后列表分类为空的问题。

### 根因

添加应用时，前端保存接口只提交 `storeType`、`regionCode`、`appIdentifier`，没有把用户选中的搜索结果分类一起提交。后端保存时会重新校验真实应用，并只使用二次校验结果中的分类；当二次校验无法解析分类时，数据库保存的 `category` 就为空。

### 实施内容

1. 创建应用请求增加可选 `category` 字段，用作搜索结果分类候选值。
2. 后端 `CreateCustomerAppCommand` 增加可选 `categoryHint`，并保留原三参构造，兼容现有调用。
3. 后端仍然必须先校验应用真实存在；只有二次校验分类为空时，才使用 `categoryHint` 作为兜底。
4. 对兜底分类做空值归一化和最大长度截断，避免异常长文本入库。
5. 前端保存应用时，如果用户选择了搜索结果，则把 `selectedApp.category` 一并提交。
6. 增加服务层测试覆盖“校验成功但分类为空时使用分类候选”的场景。

### 修改文件

1. `backend/src/main/java/com/youou/aso/modules/appmanagement/dto/CreateCustomerAppCommand.java`
2. `backend/src/main/java/com/youou/aso/modules/appmanagement/api/CustomerAppController.java`
3. `backend/src/main/java/com/youou/aso/modules/appmanagement/service/CustomerAppService.java`
4. `backend/src/test/java/com/youou/aso/modules/appmanagement/service/CustomerAppServiceTest.java`
5. `frontend/src/api/applications.ts`
6. `frontend/src/views/user/ApplicationManagementView.vue`
7. `PROJECT_PROGRESS.md`

### 验证结果

1. `mvn.cmd -Dtest=CustomerAppServiceTest test`：通过，7 个应用管理服务测试成功。
2. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 构建成功。

### 下一步任务清单

1. 刷新用户端 `/user/applications`，打开添加应用弹窗。
2. 搜索一个带分类的应用并选中。
3. 保存后确认应用列表分类列显示该分类。
4. 如果后端服务还在旧进程中，重启后端后再验证。

### 风险点

1. 本次没有绕过真实应用校验；即使前端传入分类，后端仍要先确认应用真实存在。
2. 分类候选只在后端二次校验分类为空时使用，避免覆盖商店接口返回的真实分类。

## 2026-06-20 更新：添加应用搜索空状态显示修复

- 状态：已完成代码修改，待构建验证与用户刷新确认。
- 当前任务：修复添加应用弹窗中已经搜索出应用列表时，底部仍显示“未找到对应应用”的问题。

### 根因

空状态组件 `el-empty` 使用了 `v-else-if="hasSearched"`，但它实际只跟在“已选中应用预览”后面，并没有跟搜索结果列表形成互斥关系。因此只要用户还没选中应用，即使 `searchResults` 已经有数据，空状态也会显示。

### 实施内容

1. 将空状态显示条件改为 `hasSearched && searchResults.length === 0 && !selectedApp`。
2. 保持搜索列表、选中应用预览和保存逻辑不变。

### 修改文件

1. `frontend/src/views/user/ApplicationManagementView.vue`
2. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 构建成功。

### 下一步任务清单

1. 刷新用户端 `/user/applications`。
2. 打开添加应用弹窗并搜索有结果的应用，确认不再显示“未找到对应应用”。
3. 搜索无结果的关键词，确认空状态仍正常显示。

### 风险点

1. 本次仅调整前端显示条件，不影响搜索接口、保存接口和应用校验逻辑。
## 2026-06-20 更新：添加应用重复保存提示修复
- 状态：已完成代码修改，待构建验证与用户刷新确认。
- 当前任务：修复用户在添加应用弹窗中选中已存在应用后，点击保存出现 `System error` 的问题。

### 根因

添加应用弹窗可以搜索并选中已经加入当前账号的应用，但前端仍允许再次保存。后端会拒绝重复应用，返回重复错误；前端未识别该错误码，导致界面显示通用的 `System error`。

### 实施内容

1. 选中的应用如果已存在于当前应用列表，预览区标签显示为“已添加应用”。
2. 已添加应用的保存按钮禁用，避免用户重复提交。
3. 提交函数增加重复应用前置判断，直接提示“该应用已添加，无需重复保存”。
4. 错误处理补充 `APP_ALREADY_EXISTS` 映射，后端返回重复错误时不再显示通用系统错误。
5. 补充中英文多语言文案。

### 修改文件

1. `frontend/src/views/user/ApplicationManagementView.vue`
2. `frontend/src/i18n/locales/zh-CN.ts`
3. `frontend/src/i18n/locales/en-US.ts`
4. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 构建成功。

### 下一步任务清单

1. 刷新用户端 `/user/applications` 页面。
2. 打开添加应用弹窗，搜索一个已经添加过的应用。
3. 确认预览区显示“已添加应用”，并且保存按钮不可点击。
4. 如果后端仍返回重复应用错误，确认提示为“该应用已添加，无需重复保存”。

### 风险点

1. 本次仅阻止当前页面已加载列表中的重复保存；跨设备或列表未刷新时，仍以后端唯一性校验作为最终保护。
2. 该修复不改变应用真实校验、分类入库和应用列表查询逻辑。
## 2026-06-20 更新：应用重复判断按地区修复与软删除唯一约束调整
- 状态：已完成代码修改、数据库迁移验证与构建验证。
- 当前任务：修复同一应用不同地区被误判重复，以及已删除应用仍可能触发唯一约束冲突的问题。

### 根因

前端重复判断使用了选中搜索结果中的地区字段，而保存时使用的是弹窗当前选择的地区字段；两者口径不一致时，可能把不同地区的应用误判为已添加。后端服务层和仓储查询已经只把 `ACTIVE` 应用视为重复，但数据库旧唯一索引 `uk_customer_app` 没有区分状态，软删除后的旧记录仍会阻止再次添加。

### 实施内容

1. 前端重复判断统一使用当前弹窗的 `storeType`、`regionCode` 和选中应用的 `appIdentifier`。
2. 前端比较地区时统一转大写、比较应用标识时去除首尾空格，避免大小写或空格带来的误判。
3. 新增 Flyway V5 迁移，删除旧的全量唯一索引。
4. 新增生成列 `active_app_identifier`，仅当 `status = 'ACTIVE'` 时写入应用标识，非有效状态为 `NULL`。
5. 新增唯一索引 `uk_customer_app_active`，只约束有效应用的 `customer_id + store_type + region_code + active_app_identifier`。

### 修改文件

1. `frontend/src/views/user/ApplicationManagementView.vue`
2. `backend/src/main/resources/db/migration/V5__customer_app_active_unique_key.sql`
3. `PROJECT_PROGRESS.md`

### 验证结果

1. `mvn.cmd -Dtest=CustomerAppServiceTest test`：通过，7 个应用管理服务测试成功。
2. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 构建成功。
3. `mvn.cmd spring-boot:run "-Dspring-boot.run.profiles=local"`：Flyway 成功验证 5 个迁移，并将本地 MySQL `youou_aso` 从版本 4 迁移到版本 5；随后应用启动因 8080 端口已被占用而停止，迁移已完成。

### 下一步任务清单

1. 刷新用户端 `/user/applications` 页面。
2. 添加同一个应用但选择不同地区，确认不再显示“已添加应用”，并且可以保存。
3. 删除一个应用后，再次添加相同商店、相同地区、相同应用，确认不再因旧记录触发唯一约束冲突。
4. 确认同一用户、同一商店、同一地区、同一应用仍然不能重复添加有效记录。

### 风险点

1. 本次数据库迁移依赖 MySQL 生成列与唯一索引能力，已在本地 MySQL 8.3 上执行成功。
2. 当前 8080 端口已有后端进程占用；如果该进程是旧代码，需重启到当前源码或新包后才能获得最新前端配套逻辑和后端代码。
## 2026-06-20 更新：应用商店搜索超时降级修复
- 状态：已完成代码修改与构建验证。
- 当前任务：修复添加应用弹窗中搜索 App Store 应用时，由外部商店请求慢或不可达导致前端显示“应用搜索失败”的问题。

### 根因

后端 `HttpAppStoreVerifier` 虽然已经对外部商店查询异常做了降级处理，但 `RestClient` 没有配置连接超时和读取超时。当 App Store 或 Google Play 请求长时间无响应时，前端 Axios 的 15 秒超时可能先触发，用户界面就会显示“应用搜索失败”，而不是正常进入“未找到对应应用”的空状态。

### 实施内容

1. 为外部商店查询的 `RestClient` 配置连接超时 4 秒、读取超时 8 秒。
2. 保留原有异常降级逻辑：外部商店查询失败时返回空列表或空校验结果，不让异常穿透到用户端。
3. 增加后端 warn 日志，记录商店、地区、关键词或应用标识，方便后续排查外部接口波动。
4. 未改变应用保存前必须真实校验的业务规则；校验失败仍然不能保存。

### 修改文件

1. `backend/src/main/java/com/youou/aso/modules/appmanagement/service/HttpAppStoreVerifier.java`
2. `PROJECT_PROGRESS.md`

### 验证结果

1. `mvn.cmd -Dtest=CustomerAppServiceTest test`：通过，7 个应用管理服务测试成功。
2. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 构建成功。
3. `netstat -ano | Select-String ':8080'`：确认本地 8080 当前已有 Java 进程占用。
4. `Get-CimInstance Win32_Process -Filter "ProcessId = 11096"`：当前权限拒绝读取该进程命令行，因此未自动停止该进程。

### 下一步任务清单

1. 重启当前 8080 后端进程到最新代码。
2. 刷新用户端 `/user/applications` 页面。
3. 打开添加应用弹窗，选择 App Store / 美国，搜索 `wechat`。
4. 如果外部商店可访问，应显示应用列表；如果外部商店不可访问或超时，应快速显示空结果，不再长时间卡住或显示通用搜索失败。

### 风险点

1. 外部 App Store / Google Play 本身存在网络可达性和反爬限制，超时降级只能保证系统稳定返回，不能保证每次都能搜到结果。
2. 当前 8080 进程未由本次操作自动重启；如果它仍是旧代码，需要重启后才能生效。

## 2026-06-20 更新：登录前首页按设计图重做
- 状态：已完成代码修改与构建验证，待用户在浏览器中确认视觉效果。
- 当前任务：按用户提供的首页设计图，重新实现 Youou-ASO 登录前首页。

### 实施内容

1. 重写登录前首页主体，改为左侧品牌标语、CTA、平台标签，右侧“推广服务”模拟面板，下方服务数据指标卡和底部下一段标题露出。
2. 公共顶栏调整为白色半透明导航、蓝色圆形品牌标识、登录/注册入口和胶囊语言切换。
3. 服务面板补齐 App Store / Google Play / iPad Store 平台胶囊，以及 6 个推广服务卡片、图标、说明和箭头。
4. 指标区补齐 10,000+ 服务应用数、98.6% 客户满意度、5 年+ 行业经验、50+ 专业团队。
5. 增加桌面、平板、手机响应式布局，避免文字和卡片在小屏重叠。
6. 同步补齐中英文首页文案，保证语言切换不出现缺失键。

### 修改文件

1. `frontend/src/views/public/HomeView.vue`
2. `frontend/src/layouts/PublicLayout.vue`
3. `frontend/src/i18n/locales/zh-CN.ts`
4. `frontend/src/i18n/locales/en-US.ts`
5. `../PROJECT_PROGRESS.md`
6. `PROJECT_PROGRESS.md`

### 验证结果

1. `rg --files -g "*test*" -g "*spec*" youou-aso\frontend`：未发现前端现成测试入口，因此本次未新增单元测试。
2. `npm.cmd run build`：首次失败，原因是 Element Plus 图标库没有导出 `Shield` 图标。
3. 替换为已有 `Lock` 图标后再次执行 `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 构建成功。
4. `Get-NetTCPConnection -LocalPort 5173 -ErrorAction SilentlyContinue`：确认 5173 未被占用。
5. `Start-Process npm.cmd run dev -- --host 127.0.0.1 --port 5173`：已启动本地前端开发服务。
6. `Invoke-WebRequest http://127.0.0.1:5173/`：返回 `200`，首页可访问。

### 下一步任务清单

1. 打开 `http://127.0.0.1:5173/`，确认首页视觉是否贴近设计图。
2. 分别切换中文和英文，确认标题、按钮、服务说明和指标文案正常显示。
3. 在窄屏浏览器宽度下确认服务卡片和指标卡不会重叠或溢出。
4. 如需更贴近设计图，可继续微调卡片宽度、主标题字号、背景装饰和图标样式。

### 风险点

1. 本次仅调整登录前首页和公共顶栏视觉，不涉及后端、数据库、认证、订单或应用管理逻辑。
2. 首页中的指标是品牌展示文案，不来自后台真实统计；生产上线前如需真实数据，应接入后台配置或统计接口。
3. 前端构建仍存在既有 Rollup PURE 注释提示和主 chunk 超过 500k 的非阻断警告，本次未处理。
## 2026-06-20 更新：添加应用保存按钮必须先选中搜索结果
- 状态：已完成代码修改与前端构建验证，待用户在页面刷新后确认交互效果。
- 当前任务：修复添加应用弹窗中，未搜索并选中真实应用时保存按钮仍可点击的问题。

### 根因

添加应用弹窗的保存按钮禁用条件把 `form.keyword` 作为可保存依据。用户只要输入了应用名称或标识，即使没有从搜索结果中选中真实应用，保存按钮也会启用。提交函数中也存在 `selectedApp` 为空时从输入框内容兜底生成应用标识的逻辑，导致前端交互与“必须选择真实搜索结果”的业务规则不一致。

### 实施内容

1. 保存按钮禁用条件调整为：未选中搜索结果时禁用，已添加过的应用也禁用。
2. `submitApp` 增加前置防御：没有 `selectedApp` 时直接提示“请先搜索并选择真实应用”并停止提交。
3. 保存 payload 的 `appIdentifier` 改为只来源于已选中的搜索结果，移除输入框兜底解析逻辑。
4. 删除不再使用的 `normalizeAppIdentifier` 函数，避免后续误用。
5. 更新中英文提示文案，去掉“可直接输入应用标识或商店链接保存”的旧引导。

### 修改文件

1. `frontend/src/views/user/ApplicationManagementView.vue`
2. `frontend/src/i18n/locales/zh-CN.ts`
3. `frontend/src/i18n/locales/en-US.ts`
4. `PROJECT_PROGRESS.md`

### 验证结果

1. `rg "normalizeAppIdentifier|selectSearchResultFirst|直接输入可校验|enter a verifiable" frontend/src/views/user/ApplicationManagementView.vue frontend/src/i18n/locales`：确认旧兜底函数和旧引导文案已移除，仅保留新增的必须选中搜索结果提示。
2. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 构建成功。
3. 构建仍有既有的 Rollup PURE 注释警告与主 chunk 超过 500k 的非阻断警告，本次未处理。

### 下一步任务清单

1. 刷新用户端 `/user/applications` 页面。
2. 打开“添加应用”弹窗，选择商店和地区，只输入应用关键字但不点击搜索结果，确认保存按钮保持禁用。
3. 搜索并点击一个结果后，确认保存按钮启用。
4. 修改搜索框内容后，确认已选应用被清空且保存按钮重新禁用。

### 风险点

1. 本次只收紧前端交互，不改变后端真实应用校验和唯一性约束；后端仍应作为最终安全边界。
2. 搜索框仍支持输入 App 名称、Apple ID、Bundle ID 或商店链接作为搜索关键词，但不能再直接绕过搜索结果选择进行保存。
## 2026-06-20 更新：应用分类按当前语言显示
- 状态：已完成代码修改与前端构建验证，待用户刷新页面确认分类显示。
- 当前任务：修复搜索应用保存后，应用分类语言与当前中英文设置不一致的问题。

### 根因

应用分类字段当前直接来自外部商店接口并原样保存、原样展示。App Store 美国区常返回英文 `primaryGenreName`，Google Play 搜索和详情请求当前固定使用 `hl=en`，因此中文界面里也会看到英文分类。这不是美国区 App 数据异常，而是我们没有对分类做本地化展示。

### 实施内容

1. 新增前端分类显示工具 `formatAppCategory`，按当前语言把常见 App Store / Google Play 分类映射为中英文显示。
2. 用户端应用列表分类列改为按当前语言显示。
3. 用户端添加应用弹窗中的搜索结果和已选应用预览分类改为按当前语言显示。
4. 管理员端应用列表分类列改为按当前语言显示。
5. 后端入库的原始分类字段保持不变，避免本次为显示问题引入数据库结构变更。

### 修改文件

1. `frontend/src/utils/appCategory.ts`
2. `frontend/src/views/user/ApplicationManagementView.vue`
3. `frontend/src/views/admin/ApplicationManagementView.vue`
4. `PROJECT_PROGRESS.md`

### 验证结果

1. `rg -n "row\\.category \\|\\||result\\.category \\}\\}|selectedApp\\.category \\}\\}|formatAppCategory" frontend/src/views frontend/src/utils`：确认用户端和管理员端分类展示均已走 `formatAppCategory`，未发现旧的原样展示写法。
2. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 构建成功。
3. 构建仍有既有的 Rollup PURE 注释警告与主 chunk 超过 500k 的非阻断警告，本次未处理。

### 下一步任务清单

1. 刷新用户端 `/user/applications` 页面，确认英文分类在中文界面显示为中文。
2. 切换到 English，确认分类显示为英文。
3. 打开添加应用弹窗，搜索并选中 App，确认搜索结果和已选应用预览中的分类也随语言切换显示。
4. 如果发现某个分类仍未翻译，记录原始分类文本后补充映射表。

### 风险点

1. 本次是展示层本地化，数据库仍保存外部商店返回的原始分类文本；这是为了避免不必要的数据库结构变更。
2. 未知或非常规分类会保留原样显示，避免错误翻译；后续可根据实际搜索结果持续补充映射。

## 2026-06-20 更新：统一价格配置与初始超级管理员账号
- 状态：已完成代码修改、数据库迁移与构建验证，待用户确认后台页面和初始账号登录效果。
- 当前任务：实现全站统一价格配置模块，并为系统添加一个初始系统管理员账号。

### 实施内容

1. 新增后端 `pricing` 模块，包含价格编码、价格配置领域对象、DTO、仓储、服务和管理员接口。
2. 新增 `GET /api/admin/pricing` 和 `PUT /api/admin/pricing`，仅允许管理员账号访问。
3. 价格配置覆盖 6 个统一价格项：关键词安装、下载量、5 星评分、4 星评分、5 星评论、4 星评论。
4. 服务层和接口层均限制价格不能为负数，负数会返回价格非法相关错误。
5. 新增 Flyway `V6__pricing_config.sql`，创建并初始化 `pricing_config` 表。
6. 新增 Flyway `V7__initial_super_admin.sql`，初始化超级管理员账号。
7. 管理端 `PricingView.vue` 改为真实价格维护页面，支持加载、编辑和保存 6 个价格。
8. 新增前端 `api/pricing.ts`，补齐中英文价格配置文案。

### 初始系统管理员

1. 用户名：`superadmin`
2. 邮箱：`admin@youou-aso.local`
3. 初始密码：`Youou@2026`
4. 角色：`SUPER_ADMIN`
5. 已设置 `force_password_change = 1`；生产使用前必须尽快修改初始密码。

### 修改文件

1. `backend/src/main/java/com/youou/aso/modules/pricing/domain/PriceCode.java`
2. `backend/src/main/java/com/youou/aso/modules/pricing/domain/PricingConfig.java`
3. `backend/src/main/java/com/youou/aso/modules/pricing/dto/PricingConfigResult.java`
4. `backend/src/main/java/com/youou/aso/modules/pricing/dto/UpdatePricingCommand.java`
5. `backend/src/main/java/com/youou/aso/modules/pricing/repository/PricingConfigRepository.java`
6. `backend/src/main/java/com/youou/aso/modules/pricing/repository/JdbcPricingConfigRepository.java`
7. `backend/src/main/java/com/youou/aso/modules/pricing/service/PricingService.java`
8. `backend/src/main/java/com/youou/aso/modules/pricing/api/AdminPricingController.java`
9. `backend/src/main/resources/db/migration/V6__pricing_config.sql`
10. `backend/src/main/resources/db/migration/V7__initial_super_admin.sql`
11. `backend/src/test/java/com/youou/aso/modules/pricing/service/PricingServiceTest.java`
12. `frontend/src/api/pricing.ts`
13. `frontend/src/views/admin/PricingView.vue`
14. `frontend/src/i18n/locales/zh-CN.ts`
15. `frontend/src/i18n/locales/en-US.ts`
16. `../PROJECT_PROGRESS.md`
17. `PROJECT_PROGRESS.md`

### 验证结果

1. `mvn.cmd -Dtest=PricingServiceTest test`：首次失败，缺少 `pricing` 包、服务、仓储和领域对象，符合 TDD RED 预期。
2. `mvn.cmd -Dtest=PricingServiceTest test`：实现后通过，3 个价格服务测试成功。
3. 使用项目已有 `spring-security-crypto` 生成 `Youou@2026` 的 BCrypt 哈希，并用 `matches` 验证结果为 `true`。
4. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 构建成功；仍有既有 Rollup PURE 注释和主 chunk 超过 500k 非阻断警告。
5. `mvn.cmd test`：通过，16 个后端测试全部成功。
6. 后端全量测试期间 Flyway 成功验证 7 个迁移，并将本地 MySQL `youou_aso` 从版本 5 迁移到版本 7。

### 下一步任务清单

1. 使用 `superadmin / Youou@2026` 登录后台，确认能够进入管理员界面。
2. 打开 `/admin/pricing`，确认 6 个价格项能加载、修改和保存。
3. 完成首次登录强制改密流程前，避免将该初始密码用于生产公开环境。
4. 后续普通订单计价模块应从 `pricing_config` 读取价格，并在订单创建时固化价格快照。

### 风险点

1. 初始密码虽然只以 BCrypt 哈希入库，但明文初始密码已在交付说明中出现；生产上线前必须立即修改。
2. 当前强制改密字段已设置，但完整的“首次登录必须修改密码”流程仍需后续实现或验收。
3. 价格配置当前只提供统一价格表，不区分商店、地区、用户等级，符合第一版需求。
4. 后端测试日志仍出现 Spring Boot 默认 generated security password 提示，属于既有安全配置收尾项，本次未处理。
## 2026-06-20 更新：添加应用重复时补充明确说明
- 状态：已完成代码修改与前端构建验证，待用户刷新页面确认交互效果。
- 当前任务：修复添加应用时选中已存在应用后，保存按钮被禁用但没有明确原因说明的问题。

### 根因

添加应用弹窗已经能识别当前账号中相同商店、地区和应用标识的已存在应用，并禁用保存按钮以避免重复提交。但界面只在已选应用标签处显示“已添加应用”，保存按钮本身没有说明，用户不容易理解为什么不能继续保存。

### 实施内容

1. 保留重复应用不能保存的前端保护逻辑。
2. 选中已存在应用时，在已选应用卡片下方显示警告提示，说明当前账号已维护该应用和地区。
3. 保存按钮禁用时增加 tooltip，未选应用时提示“请先搜索并选择真实应用”，重复应用时提示“不能重复添加，可在应用列表中直接创建订单”。
4. 新增中英文多语言文案 `appAlreadyAddedTip`。

### 修改文件

1. `frontend/src/views/user/ApplicationManagementView.vue`
2. `frontend/src/i18n/locales/zh-CN.ts`
3. `frontend/src/i18n/locales/en-US.ts`
4. `PROJECT_PROGRESS.md`

### 验证结果

1. `rg -n "duplicate-app-alert|saveDisabledReason|appAlreadyAddedTip|save-button-wrap" frontend/src/views/user/ApplicationManagementView.vue frontend/src/i18n/locales`：确认重复应用提示、保存按钮禁用原因和中英文文案均已接入。
2. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 构建成功。
3. 构建仍有既有的 Rollup PURE 注释警告与主 chunk 超过 500k 的非阻断警告，本次未处理。

### 下一步任务清单

1. 刷新用户端 `/user/applications` 页面。
2. 打开“添加应用”弹窗，搜索并选中已存在于当前列表中的同商店、同地区应用。
3. 确认弹窗显示重复应用警告提示，并且保存按钮禁用。
4. 将鼠标移到禁用的保存按钮上，确认能看到禁用原因。
5. 选择一个未添加过的应用后，确认警告消失且保存按钮可用。

### 风险点

1. 本次只改善前端说明和按钮反馈，不改变后端重复校验，后端仍是最终保护。
2. Element Plus 禁用按钮无法直接触发 tooltip，因此按钮外层增加了 `span` 包裹，仅用于提示展示，不影响提交逻辑。

## 2026-06-20 更新：添加应用弹窗地区国旗与按钮间距修复

- 状态：已完成前端代码修改与构建验证，待用户刷新页面确认视觉效果。
- 当前任务：修复添加应用弹窗底部“取消 / 保存”按钮挨得太近的问题，并让弹窗内的地区下拉框也显示国旗图标。

### 根因

1. 保存按钮外层为了支持禁用状态 tooltip 增加了 `span` 包裹，Element Plus 默认相邻按钮间距没有作用到内部按钮。
2. 应用列表筛选地区已使用国旗图标，但添加应用弹窗里的地区选择器仍只显示文本。

### 实施内容

1. 为 `.save-button-wrap` 增加左侧间距，恢复“取消”和“保存”之间的正常视觉距离。
2. 添加应用弹窗地区选择器增加已选地区前缀国旗。
3. 地区下拉选项增加国旗与地区名称组合展示。
4. 复用现有 `flagUrl`、`flag-icon` 和 `option-row` 样式，不新增依赖、不调整业务逻辑。

### 修改文件

1. `frontend/src/views/user/ApplicationManagementView.vue`
2. `PROJECT_PROGRESS.md`

### 验证结果

1. `rg -n "form.regionCode|save-button-wrap|flagUrl\(region.code\)|flagUrl\(form.regionCode\)" frontend/src/views/user/ApplicationManagementView.vue`：确认地区已选值、地区选项和按钮间距样式均已接入。
2. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 构建成功。
3. 构建仍存在既有 Rollup PURE 注释提示与主 chunk 超过 500k 的非阻断警告，本次未处理。

### 下一步任务清单

1. 刷新用户端 `/user/applications` 页面。
2. 打开“添加应用”弹窗，确认底部“取消”和“保存”按钮之间有正常间距。
3. 打开地区下拉框，确认已选地区和下拉选项均显示国旗图标。
4. 如需完全避免外链图片依赖，后续可评估将国旗资源本地化。

### 风险点

1. 本次仅调整前端展示和间距，不改后端接口、数据库结构和应用保存逻辑。
2. 国旗图标继续依赖现有 flagcdn 外链策略，与当前列表筛选地区图标保持一致。

## 2026-06-20 更新：操作台品牌区返回当前首页

- 状态：已完成前端代码修改，待构建验证与用户刷新确认。
- 当前任务：确认并实现左上角系统图标/名称点击后的跳转行为：登录前回网站首页，登录后在操作台内回当前身份首页。

### 设计规则

1. 公共未登录布局保持现状：点击 `Youou-ASO` 品牌区返回网站首页 `/`。
2. 用户端操作台：点击左上角品牌区返回用户首页 `/user/dashboard`。
3. 管理员端操作台：点击左上角品牌区返回管理员首页 `/admin/dashboard`。
4. 不新增菜单项、不改变现有路由守卫和权限逻辑。

### 实施内容

1. 将用户端侧栏品牌区从静态容器改为 `router-link`，跳转到 `/user/dashboard`。
2. 将管理员端侧栏品牌区从静态容器改为 `router-link`，跳转到 `/admin/dashboard`。
3. 保持原有视觉布局，仅补充链接去下划线和鼠标指针样式。

### 修改文件

1. `frontend/src/layouts/UserLayout.vue`
2. `frontend/src/layouts/AdminLayout.vue`
3. `PROJECT_PROGRESS.md`

### 验证结果

1. `Select-String -Path frontend/src/layouts/UserLayout.vue -Pattern '/user/dashboard','shell-brand'`：确认用户端品牌区已跳转到 `/user/dashboard`。
2. `Select-String -Path frontend/src/layouts/AdminLayout.vue -Pattern '/admin/dashboard','shell-brand'`：确认管理员端品牌区已跳转到 `/admin/dashboard`。
3. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 构建成功。
4. 构建仍存在既有 Rollup PURE 注释提示与主 chunk 超过 500k 的非阻断警告，本次未处理。

### 下一步任务清单

1. 刷新用户端页面，点击左上角 `Youou-ASO`，确认返回用户首页。
2. 刷新管理员端页面，点击左上角 `Youou-ASO`，确认返回管理员首页。
3. 若后续需要从操作台访问网站首页，可在用户下拉菜单中单独设计“访问官网”入口。

### 风险点

1. 本次仅调整前端导航交互，不改认证、权限、接口或数据库。
2. 当前操作台品牌区跳转到具体 dashboard 路由，避免直接跳 `/user` 或 `/admin` 时子路由为空。

## 2026-06-20 更新：订单模块实现计划整理

- 状态：已完成当前代码状态检查与基线验证，订单功能尚未开始代码实现，待确认后按阶段推进。
- 当前任务：整理 Youou-ASO 订单功能开发计划，覆盖普通订单、特殊审核订单、用户端订单创建/列表/导出、管理员确认/执行/批量执行和多语言。

### 当前代码状态

1. 项目目录下未发现 `AGENTS.md` 文件，本次按用户消息中的项目规则执行。
2. 后端已有 `aso_order` 初始表、订单类型枚举 `OrderType`、订单状态枚举 `OrderStatus`、特殊审核状态枚举 `SpecialAuditStatus`，但缺少订单明细、订单仓储、订单服务、订单接口、特殊审核表和扣款流水闭环。
3. 后端已有统一价格配置模块 `pricing_config`，当前覆盖关键词安装、下载量、5 星评分、4 星评分、5 星评论、4 星评论；尚未覆盖关键词保排名、关键词覆盖的协商价格审核流程。
4. 后端钱包当前只有 `wallet_account` 和注册时初始化钱包能力，缺少查询、原子扣款、交易流水、退款和并发版本更新能力。
5. 前端用户端和管理员端订单菜单/路由骨架已经存在；订单列表页和待确认订单页仍是占位内容。
6. 前端推广服务页已有 6 类服务展示，但未接订单创建页、特殊需求提交页或正式订单提交接口。

### 分阶段实现计划

1. 第一阶段：补齐订单与钱包生产基础。
   - 新增小步 Flyway 迁移，扩展订单所需明细/快照字段，新增订单明细表、特殊审核表、钱包交易表。
   - 新增钱包原子扣款与余额查询能力，保证余额不能为负，并记录扣款流水。
   - 新增订单领域对象、DTO、仓储与服务层，先覆盖普通订单创建计价和状态流转核心规则。
2. 第二阶段：实现普通订单创建与用户端订单列表。
   - 接入关键词安装、下载量、评分、评论四类普通订单。
   - 订单创建时从 `pricing_config` 读取价格并固化价格快照。
   - 实现用户端订单创建页、订单列表页、状态展示和 CSV 导出。
3. 第三阶段：实现管理员订单确认与执行。
   - 实现待确认订单独立页、确认/拒绝入口。
   - 实现待执行列表、单个执行、批量执行、到期自动完成规则。
   - 实现管理员订单列表和 CSV 导出。
4. 第四阶段：实现特殊订单审核闭环。
   - 关键词保排名、关键词覆盖首次提交进入待审核。
   - 管理员审核时可修改协商内容和价格；审核通过后锁定，用户看到“已审核待支付”。
   - 用户确认提交正式订单时自动扣余额并进入待确认；管理员可取消长期未提交的审核需求。
5. 第五阶段：补齐中英文多语言、前后端验证、异常文案和回归测试。

### 计划中的安全与生产约束

1. 不修改 `.env` 或任何秘钥配置。
2. 不安装新依赖，导出优先使用后端 CSV 响应或前端现有能力。
3. 不删除文件，不大幅重构现有模块。
4. 数据库只通过小步 Flyway 迁移扩展现有订单能力，不替换现有架构。
5. 所有扣款必须在后端事务中完成，余额不足直接拒绝创建正式订单，余额不得为负。
6. 管理员接口必须校验管理员身份；用户订单接口必须限定当前登录用户数据。

### 基线验证结果

1. `mvn.cmd test`：通过，16 个后端测试全部成功；Flyway 当前 schema 版本为 7。
2. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 构建成功。
3. 前端构建仍存在既有 Rollup PURE 注释提示与主 chunk 超过 500k 的非阻断警告，本次未处理。

### 下一步任务清单

1. 确认是否允许本次订单功能按上述阶段新增 Flyway 迁移并实现第一阶段后端基础。
2. 若确认，优先用 TDD 编写钱包扣款、普通订单计价、状态流转服务测试。
3. 小步实现钱包交易表、订单明细表、特殊审核表及对应仓储/服务。
4. 第一阶段完成后运行 `mvn.cmd test`，再更新本文件记录修改文件、验证结果和剩余风险。

### 风险点

1. 订单需求涉及数据库扩展、扣款、状态流转和导出，属于新功能和数据库修改，需要确认后再开始代码实现。
2. 当前没有钱包交易流水表；如果只在订单表记录扣款，会影响后续收支明细、退款审计和财务对账，不建议作为生产方案。
3. 当前订单页是占位页，前端工作量较大，应先以后端接口稳定为前提逐步接入，避免前后端字段反复变更。

## 2026-06-20 更新：订单模块第一阶段后端基础

- 状态：已完成后端第一阶段基础代码修改与验证，待用户确认后继续接用户端订单创建页、列表和导出。
- 当前任务：实现普通订单创建计价、钱包余额扣款、订单确认/执行状态流转、订单基础表结构扩展和最小后端接口。

### 实施计划

1. 使用 TDD 先写订单服务测试，覆盖关键词安装、下载量、评分计价、执行小时限制、余额不足拒绝、管理员确认和逾期执行自动完成。
2. 补齐订单领域对象、创建命令、结果 DTO、订单服务和仓储接口。
3. 新增 JDBC 订单仓储与钱包扣款服务，保持现有 `JdbcTemplate` 风格。
4. 新增 Flyway `V8__order_wallet_foundation.sql`，只做小步表结构扩展，不删除或重建已有表。
5. 新增用户创建订单、管理员确认订单、管理员执行订单的最小 API 入口。
6. 补齐本次新增业务错误码的中英文文案。

### TDD 过程

1. `mvn.cmd -Dtest=OrderServiceTest test`：首次失败，缺少 `AsoOrder`、`OrderService`、`CreateOrderCommand`、`OrderRepository`、`WalletService` 等订单基础类，符合 RED 预期。
2. 实现订单基础后再次运行 `mvn.cmd -Dtest=OrderServiceTest test`：通过 6 个订单服务测试。
3. 新增管理员确认订单测试后运行 `mvn.cmd -Dtest=OrderServiceTest test`：失败，缺少 `confirmOrder` 方法，符合 RED 预期。
4. 实现 `confirmOrder` 后运行 `mvn.cmd -Dtest=OrderServiceTest test`：通过 7 个订单服务测试。

### 实施内容

1. 普通订单创建支持关键词安装、下载量、评分、评论四类订单计价。
2. 关键词安装费用按关键词数量乘统一单价，执行小时只做 1 到 8 小时合法性校验，不参与计费。
3. 下载量费用按包含首尾日期的天数乘每日下载量乘统一单价；例如 19 日到 21 日按 3 天计算，预计完成时间为 22 日 0 点。
4. 评分和评论分别使用 5 星、4 星单价计算总金额。
5. 正式订单创建时在后端事务中扣减钱包余额，余额不足抛出 `BALANCE_NOT_ENOUGH`，不保存订单。
6. 订单创建时固化应用快照、地区、价格编码、单价、数量、总金额、扣款前后余额和扣款流水 ID。
7. 管理员确认订单时状态从 `PENDING_CONFIRM` 变为 `PENDING_EXECUTION`。
8. 管理员执行订单时状态从 `PENDING_EXECUTION` 变为 `EXECUTING`；如果预计完成时间已经过去，则直接变为 `COMPLETED`。
9. 新增 `wallet_transaction` 表记录订单扣款流水，为后续收支明细、退款和财务对账做基础。
10. 新增 `special_order_audit` 表，为关键词保排名、关键词覆盖审核闭环预留生产表结构。
11. 新增 `aso_order_item` 表，为后续订单明细、关键词/评分/评论拆分展示和导出预留结构。
12. 新增接口：
    - `POST /api/customer/orders`
    - `POST /api/admin/orders/{id}/confirm`
    - `POST /api/admin/orders/{id}/execute`

### 修改文件

1. `backend/src/main/java/com/youou/aso/config/TimeConfig.java`
2. `backend/src/main/java/com/youou/aso/modules/order/domain/AsoOrder.java`
3. `backend/src/main/java/com/youou/aso/modules/order/dto/CreateOrderCommand.java`
4. `backend/src/main/java/com/youou/aso/modules/order/dto/OrderResult.java`
5. `backend/src/main/java/com/youou/aso/modules/order/repository/OrderRepository.java`
6. `backend/src/main/java/com/youou/aso/modules/order/repository/JdbcOrderRepository.java`
7. `backend/src/main/java/com/youou/aso/modules/order/service/OrderService.java`
8. `backend/src/main/java/com/youou/aso/modules/order/api/CustomerOrderController.java`
9. `backend/src/main/java/com/youou/aso/modules/order/api/AdminOrderController.java`
10. `backend/src/main/java/com/youou/aso/modules/wallet/domain/WalletTransactionType.java`
11. `backend/src/main/java/com/youou/aso/modules/wallet/service/WalletDebitResult.java`
12. `backend/src/main/java/com/youou/aso/modules/wallet/service/WalletService.java`
13. `backend/src/main/java/com/youou/aso/modules/wallet/service/JdbcWalletService.java`
14. `backend/src/main/resources/db/migration/V8__order_wallet_foundation.sql`
15. `backend/src/main/resources/i18n/messages_zh_CN.properties`
16. `backend/src/main/resources/i18n/messages_en_US.properties`
17. `backend/src/test/java/com/youou/aso/modules/order/service/OrderServiceTest.java`
18. `PROJECT_PROGRESS.md`

### 验证结果

1. `mvn.cmd -Dtest=OrderServiceTest test`：通过，7 个订单服务测试成功。
2. `mvn.cmd test`：通过，23 个后端测试全部成功。
3. Flyway 已成功验证 8 个迁移，并将本地 MySQL `youou_aso` schema 从版本 7 迁移到版本 8。
4. 测试日志仍存在既有 Spring Boot generated security password 提示、MyBatis mapper 未发现提示、Flyway MySQL 8.3 支持版本提示和 ByteBuddy 动态加载警告，本次未处理。

### 下一步任务清单

1. 实现订单查询接口：用户按商店/状态查看自己的订单，管理员按商店/状态查看全站订单。
2. 实现用户端订单创建页面，接入关键词安装、下载量、评分、评论四类普通订单表单。
3. 实现用户端订单列表页面和导出功能。
4. 实现管理员待确认订单页、待执行列表、批量执行和管理员导出。
5. 实现关键词保排名、关键词覆盖特殊审核提交流程、管理员审核锁价和用户审核后付款提交流程。
6. 将 `aso_order_item` 写入真实订单明细，支撑后续列表详情和导出。
7. 评估是否需要立即补充退款接口；当前拒绝订单后的退款闭环尚未实现。

### 风险点

1. 本次只完成后端第一阶段基础，前端订单创建页、订单列表、导出和特殊审核流程仍未接入。
2. `wallet_transaction.related_order_id` 当前在扣款时尚未回填订单 ID；订单表已保存 `deducted_transaction_id`，后续若财务对账需要双向关联，应增加安全回填逻辑。
3. 特殊审核表已创建，但业务服务尚未实现；关键词保排名、关键词覆盖仍不能通过接口提交审核。
4. 当前订单明细表已创建，但普通订单创建尚未写入拆分明细；下一阶段接列表/导出时应补齐。
5. 本地数据库已经执行 V8 迁移；如果运行中的后端进程未重启，需要重启后新接口和新表结构才会生效。

## 2026-06-20 更新：订单查询、列表和导出接入

- 状态：已完成后端订单查询接口、用户/管理员订单列表页面和前端 CSV 导出接入，待用户刷新页面确认。
- 当前任务：让用户端和管理员端订单菜单展示真实订单数据，并支持基础筛选、确认、执行、批量执行和导出。

### 实施计划

1. 后端继续使用 TDD，先补订单查询过滤测试。
2. 后端新增 `OrderQuery`，扩展订单仓储和服务，支持按客户、商店、状态查询订单。
3. 接入 `GET /api/customer/orders` 和 `GET /api/admin/orders`。
4. 前端新增 `api/orders.ts`，统一维护订单类型、状态、查询、确认和执行接口。
5. 用户端订单页替换占位内容，按当前商店加载订单，支持状态筛选和 CSV 导出。
6. 管理员商店订单页替换占位内容，支持状态筛选、确认、单个执行、批量执行和 CSV 导出。
7. 管理员待确认订单页替换占位内容，固定显示 `PENDING_CONFIRM` 订单并支持确认和导出。
8. 补齐订单页中英文文案。

### TDD 过程

1. `mvn.cmd -Dtest=OrderServiceTest test`：首次失败，缺少 `OrderQuery`，符合 RED 预期。
2. 补齐 `OrderQuery`、仓储查询方法和服务查询方法后，`mvn.cmd -Dtest=OrderServiceTest test`：通过，9 个订单服务测试成功。

### 实施内容

1. 用户订单查询只返回当前登录用户自己的订单，并支持 `storeType`、`status` 过滤。
2. 管理员订单查询支持按 `storeType`、`status` 过滤全站订单。
3. 用户端 `/user/orders/apple`、`/user/orders/google`、`/user/orders/ipad` 展示订单信息和状态，不暴露管理操作。
4. 管理员端商店订单页展示订单信息、用户 ID、状态和管理操作。
5. 管理员端待确认订单页独立展示待确认订单，确认后刷新列表。
6. 管理员端待执行订单支持表格选择后批量执行；当前通过逐个调用已有执行接口完成，不新增批量接口。
7. 用户端和管理员端订单列表均支持前端 CSV 导出，不新增依赖。
8. 多语言新增 `ordersPage` 文案，覆盖订单类型、状态、按钮、提示和导出反馈。

### 修改文件

1. `backend/src/main/java/com/youou/aso/modules/order/dto/OrderQuery.java`
2. `backend/src/main/java/com/youou/aso/modules/order/repository/OrderRepository.java`
3. `backend/src/main/java/com/youou/aso/modules/order/repository/JdbcOrderRepository.java`
4. `backend/src/main/java/com/youou/aso/modules/order/service/OrderService.java`
5. `backend/src/main/java/com/youou/aso/modules/order/api/CustomerOrderController.java`
6. `backend/src/main/java/com/youou/aso/modules/order/api/AdminOrderController.java`
7. `backend/src/test/java/com/youou/aso/modules/order/service/OrderServiceTest.java`
8. `frontend/src/api/orders.ts`
9. `frontend/src/views/user/StoreOrdersView.vue`
10. `frontend/src/views/admin/StoreOrdersView.vue`
11. `frontend/src/views/admin/PendingConfirmOrdersView.vue`
12. `frontend/src/i18n/locales/zh-CN.ts`
13. `frontend/src/i18n/locales/en-US.ts`
14. `PROJECT_PROGRESS.md`

### 验证结果

1. `mvn.cmd -Dtest=OrderServiceTest test`：通过，9 个订单服务测试成功。
2. `mvn.cmd test`：通过，29 个后端测试全部成功。
3. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 构建成功。
4. 本次后端测试日志显示 Flyway 验证 9 个迁移，当前本地 schema 为 v9；其中订单基础迁移仍为上一阶段新增的 `V8__order_wallet_foundation.sql`。
5. 前端构建仍存在既有 Rollup PURE 注释提示和主 chunk 超过 500k 的非阻断警告，本次未处理。

### 下一步任务清单

1. 实现用户端订单创建页面，接入关键词安装、下载量、评分、评论四类普通订单表单。
2. 从应用管理页“创建订单”按钮跳转到订单创建页并带入应用 ID。
3. 普通订单创建后刷新订单列表，确认扣款和状态展示一致。
4. 补充订单明细写入 `aso_order_item`，支撑后续订单详情和更完整导出。
5. 实现关键词保排名、关键词覆盖特殊审核提交、管理员审核锁价、用户审核后付款提交。
6. 评估后端批量执行接口；当前前端批量执行是逐个调用单笔执行接口。

### 风险点

1. 当前 CSV 导出由前端基于当前加载列表生成，适合第一版列表导出；如后续订单量很大，应改为后端分页和流式导出，避免浏览器内存压力。
2. 管理员批量执行当前逐个调用单笔接口，部分成功部分失败时需要用户刷新列表确认最终状态；后续可新增事务边界更明确的批量接口。
3. 订单创建页面尚未实现，当前列表只能展示已有订单或通过接口创建的订单。
4. 拒绝订单和退款闭环仍未实现，待确认订单页当前只提供确认操作。

## 2026-06-20 更新：管理员订单查询区参考图调整

- 状态：已完成后端查询能力扩展、管理员订单查询区样式调整和构建验证，待用户刷新页面确认视觉效果。
- 当前任务：根据用户提供的订单查询参考图，调整管理员订单列表查询区；按用户补充说明，本轮不考虑“进度”字段。

### 布局分析

1. 参考图为两行密集查询区，不是卡片式工具栏。
2. 第一行包含内容筛选、应用、商店、国家/地区、任务类型、订单类型。
3. 第二行包含状态、订单时间、创建时间、搜索、清空。
4. 表格表头为浅灰底，列方向为 ID、应用、订单时间、任务类型、订单类型、状态、创建时间、操作。
5. 用户明确说明系统当前没有“进度”字段，因此本轮没有实现进度列或进度筛选。

### 实施内容

1. 后端 `OrderQuery` 扩展真实筛选字段：
   - `keyword`
   - `customerAppId`
   - `regionCode`
   - `orderType`
   - `specialOrder`
   - `orderDateFrom`
   - `orderDateTo`
   - `createdDateFrom`
   - `createdDateTo`
2. 后端 JDBC 查询支持按订单号、应用标识、应用名称进行内容筛选。
3. 后端 JDBC 查询支持应用、地区、任务类型、普通/特殊订单、订单日期范围、创建日期范围筛选。
4. 用户端和管理员端订单查询接口均支持扩展参数，保持后续前端能力一致。
5. 管理员订单列表页顶部改为参考图式两行查询区。
6. 管理员订单列表表格列改为：ID、应用、订单时间、任务类型、订单类型、状态、创建时间、操作。
7. 管理员订单列表继续保留批量执行和导出能力，但不新增依赖。
8. 订单结果新增 `sourceAuditId` 返回字段，用于前端区分普通订单和特殊订单。
9. 补齐中英文订单查询区文案。

### TDD 过程

1. `mvn.cmd -Dtest=OrderServiceTest test`：首次失败，原因是 `OrderQuery` 不支持扩展筛选字段，符合 RED 预期。
2. 扩展 `OrderQuery`、仓储 SQL、控制器参数和测试假仓储后，`mvn.cmd -Dtest=OrderServiceTest test`：通过，10 个订单服务测试成功。

### 修改文件

1. `backend/src/main/java/com/youou/aso/modules/order/dto/OrderQuery.java`
2. `backend/src/main/java/com/youou/aso/modules/order/dto/OrderResult.java`
3. `backend/src/main/java/com/youou/aso/modules/order/repository/JdbcOrderRepository.java`
4. `backend/src/main/java/com/youou/aso/modules/order/api/CustomerOrderController.java`
5. `backend/src/main/java/com/youou/aso/modules/order/api/AdminOrderController.java`
6. `backend/src/test/java/com/youou/aso/modules/order/service/OrderServiceTest.java`
7. `frontend/src/api/orders.ts`
8. `frontend/src/views/admin/StoreOrdersView.vue`
9. `frontend/src/i18n/locales/zh-CN.ts`
10. `frontend/src/i18n/locales/en-US.ts`
11. `PROJECT_PROGRESS.md`

### 验证结果

1. `mvn.cmd -Dtest=OrderServiceTest test`：通过，10 个订单服务测试成功。
2. `mvn.cmd test`：通过，30 个后端测试全部成功。
3. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 构建成功。
4. `cmd /c netstat -ano | findstr 517`：确认 Vite 已监听 `[::1]:5173`，可访问 `http://localhost:5173`。
5. 首次尝试用 `Start-Process` 启动前端开发服务器失败，原因为 Windows 环境变量 `PATH/Path` 键冲突；改用 `cmd start /min` 后端口确认可用。
6. 前端构建仍存在既有 Rollup PURE 注释提示和主 chunk 超过 500k 的非阻断警告，本次未处理。

### 下一步任务清单

1. 刷新管理员端订单列表页，确认查询区视觉是否贴近参考图。
2. 在管理员端测试内容筛选、商店、地区、任务类型、订单类型、状态、订单时间和创建时间筛选。
3. 如需用户端订单列表也采用同样查询区，再同步调整用户端页面。
4. 后续若系统增加真实进度字段，再补充进度列和进度筛选。
5. 继续实现用户端订单创建页，接入普通订单下单流程。

### 风险点

1. 应用和地区下拉选项当前来自当前加载的订单数据；如果希望在无订单时也展示全量应用/地区，需要额外接应用和地区接口作为选项来源。
2. 管理员订单页仍按现有商店二级菜单进入，页面内也提供商店筛选；如果后续希望严格一页只查当前商店，可将商店筛选锁定为当前菜单商店。
3. 特殊订单业务尚未实现，当前普通/特殊订单筛选依赖 `sourceAuditId` 是否为空。

## 2026-06-20 更新：钱包余额与流水只读查询

- 状态：已完成代码修改与验证，待用户刷新页面并确认展示效果。
- 当前任务：在不影响并行应用管理开发的前提下，补齐钱包余额和钱包交易流水的只读查询能力，并修正初始管理员不需要强制修改密码的迁移结果。

### 实施内容

1. 新增钱包交易领域对象、查询 DTO、查询仓储和查询服务。
2. 新增用户端接口：`GET /api/customer/wallet`、`GET /api/customer/wallet/transactions`。
3. 新增管理员端接口：`GET /api/admin/wallet/transactions`，支持按 `customerId` 过滤，`limit` 最大限制为 100。
4. 用户端 `/user/transactions` 页面接入真实钱包余额、冻结余额和最近流水列表。
5. 管理端 `/admin/finance` 页面接入真实钱包流水列表，并支持按用户 ID 查询。
6. 新增 `V9__disable_initial_admin_force_password_change.sql`，将初始管理员 `superadmin` 的 `force_password_change` 改为 `0`。
7. 补齐钱包页面中英文文案。

### 修改文件

1. `backend/src/main/java/com/youou/aso/modules/wallet/domain/WalletTransaction.java`
2. `backend/src/main/java/com/youou/aso/modules/wallet/dto/WalletOverviewResult.java`
3. `backend/src/main/java/com/youou/aso/modules/wallet/dto/WalletTransactionResult.java`
4. `backend/src/main/java/com/youou/aso/modules/wallet/repository/WalletQueryRepository.java`
5. `backend/src/main/java/com/youou/aso/modules/wallet/repository/JdbcWalletQueryRepository.java`
6. `backend/src/main/java/com/youou/aso/modules/wallet/service/WalletQueryService.java`
7. `backend/src/main/java/com/youou/aso/modules/wallet/api/CustomerWalletController.java`
8. `backend/src/main/java/com/youou/aso/modules/wallet/api/AdminWalletController.java`
9. `backend/src/main/resources/db/migration/V9__disable_initial_admin_force_password_change.sql`
10. `backend/src/test/java/com/youou/aso/modules/wallet/service/WalletQueryServiceTest.java`
11. `frontend/src/api/wallet.ts`
12. `frontend/src/views/user/TransactionsView.vue`
13. `frontend/src/views/admin/FinanceManagementView.vue`
14. `frontend/src/i18n/locales/zh-CN.ts`
15. `frontend/src/i18n/locales/en-US.ts`
16. `PROJECT_PROGRESS.md`

### 验证结果

1. `mvn.cmd -Dtest=WalletQueryServiceTest test`：首次失败，原因是钱包查询服务、交易对象和仓储尚不存在，符合 TDD RED 预期。
2. `mvn.cmd -Dtest=WalletQueryServiceTest test`：实现后通过，4 个钱包查询服务测试全部成功。
3. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 构建成功；仍存在既有 Rollup PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题。
4. `mvn.cmd test`：通过，29 个后端测试全部成功；Flyway 成功校验 9 个迁移，并将本地 schema 从 V8 迁移到 V9。

### 下一步任务清单

1. 登录普通用户后打开 `/user/transactions`，确认余额、冻结余额和订单扣款流水展示正常。
2. 登录管理员后打开 `/admin/finance`，确认能看到钱包流水，并可按用户 ID 过滤。
3. 后续接订单列表时，将订单号或订单 ID 与钱包流水中的 `related_order_id` 形成可点击关联。
4. 后续做充值、退款、管理员调整时，必须继续在后端事务内写入 `wallet_transaction`，保证财务审计闭环。

### 风险点

1. 本次只实现只读查询，不包含充值、退款、人工调账或导出，避免在订单主流程尚未完全稳定前扩大财务操作面。
2. `wallet_transaction.related_order_id` 当前扣款写入仍为空，后续订单保存后需要补齐订单与流水关联，便于审计追踪。
3. 管理员流水接口目前只支持最近 N 条查询，尚未做分页和日期范围筛选；数据量增长后需要扩展分页。
4. 后端全量测试仍出现 Spring Boot generated security password 提示，属于既有安全配置收尾项，本次未处理。

## 2026-06-20 更新：订单与财务页面视觉审查优化

- 状态：已完成前端视觉优化和构建验证，待用户在浏览器中确认页面观感。
- 当前任务：对最近新增的订单、待确认订单、收支明细和财务管理页面做视觉审查，并进行小范围优化。

### 审查发现

1. 用户订单页、待确认订单页、钱包页和财务页的表格容器视觉不统一，有的直接铺在页面上，有的使用边框表格。
2. 管理端订单筛选区控件过密，横向间距不均，字段多时阅读压力较大。
3. 表格空状态使用英文 `No data`，和中文页面不一致。
4. 金额、状态、操作列的对齐方式不够统一。
5. 钱包页和订单页的后台操作台视觉语言不一致，缺少统一的白底卡片、弱边框和表头层次。

### 实施内容

1. 用户订单页、管理员待确认订单页、收支明细页和财务管理页统一增加白底表格卡片，使用 8px 圆角、浅边框和轻量阴影。
2. 统一表格表头为浅灰底、弱文本色和中等字重，减少页面杂乱感。
3. 管理员订单页筛选区从多行 flex 改为响应式 grid，字段标签置于输入框上方，改善密集筛选的可读性。
4. 财务管理页筛选区改为独立卡片面板，和管理员订单筛选保持同类风格。
5. 金额列统一右对齐，状态/方向列居中，操作列保持居中。
6. 补齐订单和钱包表格空状态中英文文案。
7. 保持业务逻辑、路由、接口和数据库结构不变。

### 修改文件

1. `frontend/src/views/user/StoreOrdersView.vue`
2. `frontend/src/views/admin/StoreOrdersView.vue`
3. `frontend/src/views/admin/PendingConfirmOrdersView.vue`
4. `frontend/src/views/user/TransactionsView.vue`
5. `frontend/src/views/admin/FinanceManagementView.vue`
6. `frontend/src/i18n/locales/zh-CN.ts`
7. `frontend/src/i18n/locales/en-US.ts`
8. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 构建成功。
2. 构建仍存在既有 Rollup PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题。
3. 本次尝试启动本地 dev server 做浏览器审查时，前台运行的 `npm.cmd run dev -- --host 127.0.0.1 --port 5174` 能正常输出 Vite ready，但后台 `Start-Process` 启动后 `Invoke-WebRequest` 未连通；因此本次主要基于代码结构和构建结果做视觉审查，仍需浏览器人工确认。

### 下一步任务清单

1. 在浏览器中分别打开用户端订单页、管理员订单页、待确认订单页、收支明细页和财务管理页，确认表格卡片、筛选区和空状态观感。
2. 在窄屏宽度下检查筛选区是否按单列/双列自然换行。
3. 若后续希望进一步统一后台页面，可抽取公共 `PageHeader`、`FilterPanel`、`TableCard` 组件，但这属于结构优化，需要单独确认。

### 风险点

1. 本次只做视觉层优化，没有接入自动化截图验证；最终视觉仍需浏览器确认。
2. 多个页面仍存在局部重复 CSS，当前为了避免大幅重构未抽公共组件。
3. 后台页面主 chunk 超过 500k 的既有构建警告未处理。

## 2026-06-20 更新：撤回过重的页面视觉优化

- 状态：已按用户反馈撤回上一轮过重的视觉样式，并通过前端构建验证，待用户刷新页面确认是否回到更接近原先观感。
- 用户反馈：上一轮视觉优化后“不如刚开始的时候好看”。

### 处理原则

1. 不回退订单查询、状态收敛、导出、钱包流水等业务功能。
2. 只撤回上一轮导致页面变重的视觉处理：厚卡片、阴影、边框和管理端订单筛选网格。
3. 保留不影响观感的空状态文案和表格基础可读性。

### 实施内容

1. 管理员订单页筛选区从卡片式 grid 恢复为更接近原先参考图的两行紧凑 flex 布局。
2. 移除订单页、待确认页、收支明细页和财务页表格容器上的重边框、圆角和阴影。
3. 待确认页标题字号、间距和操作区恢复到更接近原先轻量状态。
4. 财务页筛选区撤掉卡片化外观，恢复更直接的横向筛选布局。
5. 表头从较重的 `#f8fafc / 600` 调回较轻的 `#f6f7f9 / 500`。

### 修改文件

1. `frontend/src/views/admin/StoreOrdersView.vue`
2. `frontend/src/views/admin/PendingConfirmOrdersView.vue`
3. `frontend/src/views/user/TransactionsView.vue`
4. `frontend/src/views/admin/FinanceManagementView.vue`
5. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 构建成功。
2. 构建仍存在既有 Rollup PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题。

### 下一步任务清单

1. 用户刷新相关页面，确认当前观感是否比上一轮更接近预期。
2. 后续视觉优化应先基于页面截图或参考图逐页调整，不再跨多个页面套统一卡片样式。
3. 如仍需优化，优先微调间距、字号、按钮颜色和表格密度，不做结构性重排。

### 风险点

1. 本次是针对上一轮视觉反馈的快速纠偏，仍未进行浏览器截图级视觉验证。
2. 局部 CSS 仍有重复，暂不抽公共组件，避免扩大改动范围。

## 2026-06-20 更新：用户端订单查询区参考图同步调整

- 状态：已完成前端代码修改与构建验证，待用户刷新页面确认视觉效果。
- 当前任务：按用户提供的订单查询参考图，同步调整用户端订单列表页；按用户补充说明，本轮不实现“进度”字段。

### 布局分析

1. 参考图是两行紧凑查询区，字段横向排列，查询区下方直接进入表格。
2. 用户端订单页应保留订单信息和状态展示，不暴露管理员确认、执行、批量执行等管理操作。
3. 系统当前没有真实进度字段，因此用户端表格不增加进度列，也不增加进度筛选。

### 实施内容

1. 用户端 `/user/orders/apple`、`/user/orders/google`、`/user/orders/ipad` 共用订单页同步为参考图风格查询区。
2. 查询区支持内容筛选、应用、商店、国家/地区、任务类型、订单类型、状态、订单时间和创建时间。
3. 用户端复用已扩展的订单查询接口参数，保持与管理员订单查询能力一致。
4. 用户端表格列调整为：ID、应用、订单时间、任务类型、订单类型、状态、创建时间。
5. 保留用户端 CSV 导出能力，不新增依赖，不修改路由、数据库或后端接口结构。

### 修改文件

1. `frontend/src/views/user/StoreOrdersView.vue`
2. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 构建成功。
2. 构建仍存在既有 Rollup PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题。

### 下一步任务清单

1. 刷新用户端 `/user/orders/apple`、`/user/orders/google`、`/user/orders/ipad`，确认查询区和表格列是否符合参考图方向。
2. 测试用户端内容筛选、商店、地区、任务类型、订单类型、状态、订单时间和创建时间筛选。
3. 后续继续实现用户端订单创建页，接入普通订单下单、余额扣款和订单列表刷新流程。
4. 如果希望应用和地区下拉在无订单时也展示全量选项，后续需要接入应用/地区接口作为下拉数据源。

### 风险点

1. 用户端应用和地区下拉选项当前来自当前加载的订单数据；无订单或当前筛选结果为空时，下拉可选项也会受影响。
2. 用户端页面仍按商店二级菜单进入，同时页面内提供商店筛选；如果后续希望严格锁定当前商店，可把商店筛选固定为当前菜单对应商店。
3. 本轮按确认不实现进度字段，后续若新增真实进度数据，再补充进度列和筛选项。

## 2026-06-20 更新：订单终止状态合并为已取消

- 状态：已完成代码修改与验证，待用户刷新页面确认筛选项显示。
- 当前任务：根据用户确认，将订单终止状态收敛为一个“已取消”，不再区分“已拒绝”和“已取消”。

### 实施内容

1. 后端订单状态枚举移除 `REJECTED`，订单主状态只保留 `CANCELLED` 作为取消/终止类状态。
2. 前端订单接口类型移除 `REJECTED`。
3. 用户端订单页和管理员订单页的状态筛选移除“已拒绝”，仅保留“已取消”。
4. 中英文多语言文案移除 `REJECTED` 状态。
5. 保持数据库结构、路由和现有订单流程不变。

### 修改文件

1. `backend/src/main/java/com/youou/aso/modules/order/domain/OrderStatus.java`
2. `frontend/src/api/orders.ts`
3. `frontend/src/views/user/StoreOrdersView.vue`
4. `frontend/src/views/admin/StoreOrdersView.vue`
5. `frontend/src/i18n/locales/zh-CN.ts`
6. `frontend/src/i18n/locales/en-US.ts`
7. `PROJECT_PROGRESS.md`

### 验证结果

1. `rg "REJECTED|已拒绝|Rejected" backend frontend -n`：无残留匹配。
2. `mvn.cmd -Dtest=OrderServiceTest test`：通过，10 个订单服务测试全部成功。
3. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 构建成功。
4. 构建仍存在既有 Rollup PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题。

### 下一步任务清单

1. 刷新用户端和管理员端订单列表，确认状态筛选中不再出现“已拒绝”。
2. 后续实现取消订单动作时，统一写入 `CANCELLED`，并按订单扣款情况补齐退款/钱包流水逻辑。
3. 后续实现特殊订单取消时，也应统一展示为“已取消”，避免用户看到多套终止口径。

### 风险点

1. 当前项目代码不会生成 `REJECTED` 订单；如果外部历史数据库中已存在 `status = 'REJECTED'` 的订单，读取时需要先清理或转换为 `CANCELLED`。
2. 本轮只收敛状态枚举和展示口径，尚未新增取消订单接口，也未实现退款闭环。

## 2026-06-20 更新：用户端普通订单创建页

- 状态：已完成代码修改与验证，待用户刷新页面并在真实账号下确认下单流程。
- 当前任务：将用户端 `/user/promotion` 从服务占位页升级为普通订单创建页，接入关键词安装、下载量、评分、评论四类普通订单提交。

### 实施内容

1. 新增客户侧只读价格接口 `GET /api/customer/pricing`，复用现有价格配置服务，只允许普通用户读取。
2. 为客户侧价格接口新增单元测试，覆盖普通用户可读、管理员不可调用客户侧接口。
3. 前端价格 API 新增 `getCustomerPricingConfig()`，用于订单创建页加载全站统一单价。
4. 用户端 `/user/promotion` 改为订单创建页面，使用现有路由，不新增前端路由。
5. 订单类型以 tab 展示：关键词安装、下载量、星级评分、用户评价可提交；关键词保排名和关键词覆盖保留为禁用的特殊订单入口，后续接审核流程。
6. 页面支持应用商店切换、应用选择、订单时间、关键词执行小时、关键词/下载量/评分/评论数量配置。
7. 前端按现有计价规则展示预估数量和金额：
   - 关键词安装：关键词数量 * 关键词单价，执行小时不参与计费。
   - 下载量：天数 * 每日下载量 * 下载单价。
   - 评分：5 星数量 * 5 星单价 + 4 星数量 * 4 星单价。
   - 评论：5 星数量 * 5 星单价 + 4 星数量 * 4 星单价。
8. 提交普通订单后调用现有 `POST /api/customer/orders`，后端继续负责最终计价、余额扣款和订单状态写入；成功后跳转到对应商店订单列表。
9. 应用管理页原有“创建订单”入口继续跳转到 `/user/promotion?appId=...&storeType=...`，订单创建页会自动预选应用。
10. 补齐订单创建页中英文文案。

### 修改文件

1. `backend/src/main/java/com/youou/aso/modules/pricing/api/CustomerPricingController.java`
2. `backend/src/test/java/com/youou/aso/modules/pricing/api/CustomerPricingControllerTest.java`
3. `frontend/src/api/pricing.ts`
4. `frontend/src/views/user/PromotionServicesView.vue`
5. `frontend/src/i18n/locales/zh-CN.ts`
6. `frontend/src/i18n/locales/en-US.ts`
7. `PROJECT_PROGRESS.md`

### TDD 过程

1. 先新增 `CustomerPricingControllerTest`，运行 `mvn.cmd -Dtest=CustomerPricingControllerTest test` 失败，失败原因为 `CustomerPricingController` 不存在，符合 RED 预期。
2. 新增 `CustomerPricingController` 后，重新运行 `mvn.cmd -Dtest=CustomerPricingControllerTest test` 通过，2 个测试成功。

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 构建成功。
2. `mvn.cmd "-Dtest=CustomerPricingControllerTest,OrderServiceTest" test`：通过，12 个测试成功。
3. `mvn.cmd test`：通过，32 个后端测试全部成功；Flyway 校验 9 个迁移，当前本地 schema 为 V9。
4. 首次运行未加引号的 `mvn.cmd -Dtest=CustomerPricingControllerTest,OrderServiceTest test` 被 PowerShell 将逗号解析为参数分隔导致命令解析失败；随后已用带引号命令重新验证通过。
5. 构建仍存在既有 Rollup PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题。

### 下一步任务清单

1. 重启后端服务后刷新 `/user/promotion`，确认订单创建页可以加载客户侧价格接口。
2. 从应用管理页点击“创建订单”，确认能自动预选对应应用和商店。
3. 使用余额充足的普通用户测试关键词安装、下载量、评分、评论四类普通订单提交，确认提交后进入对应订单列表并显示“待确认”。
4. 实现订单明细落库到 `aso_order_item`，保存关键词、下载量、评分/评论配置，支撑详情和更完整导出。
5. 继续实现管理员取消订单和退款闭环，统一写入 `CANCELLED`。
6. 后续接入关键词保排名、关键词覆盖的特殊审核流程，再打开订单创建页中的两个特殊订单入口。

### 风险点

1. 前端金额只是预估，最终金额仍以后端提交时按最新价格配置计算并扣款为准。
2. 本轮未修改数据库结构，因此关键词、评分和评论的明细内容仍未落入 `aso_order_item`，当前列表主要展示主订单金额、数量和状态。
3. 新增客户侧价格接口需要后端服务重启后生效；如果浏览器连着旧后端进程，订单创建页会提示加载失败。
4. 特殊订单入口暂未开放提交，避免绕过审核锁价流程。

## 2026-06-20 更新：推广服务页与订单创建页拆分修正

- 状态：已完成前端代码修正与构建验证，待用户刷新页面确认。
- 当前任务：修正上一轮将 `/user/promotion` 直接改成订单创建页的问题，恢复推广服务页本身，并将订单创建表单迁移到独立页面。

### 实施内容

1. 新增用户端订单创建页面 `frontend/src/views/user/OrderCreateView.vue`，承载上一轮已实现的普通订单创建表单。
2. 新增前端路由 `/user/orders/create`，路由名为 `user-order-create`。
3. 恢复 `frontend/src/views/user/PromotionServicesView.vue` 为推广服务卡片入口页，不再加载用户应用、价格配置或订单创建数据。
4. 推广服务页普通订单按钮跳转到 `user-order-create`，并通过 `orderType` 预选订单类型。
5. 推广服务页特殊订单按钮暂不跳转普通订单创建页，仅提示特殊审核流程暂未开放，避免绕过审核锁价流程。
6. 应用管理页“创建订单”入口改为跳转 `user-order-create`，继续携带 `appId` 和 `storeType` 自动预选应用。
7. 补齐推广服务页中英文文案。

### 修改文件

1. `frontend/src/views/user/OrderCreateView.vue`
2. `frontend/src/views/user/PromotionServicesView.vue`
3. `frontend/src/views/user/ApplicationManagementView.vue`
4. `frontend/src/router/index.ts`
5. `frontend/src/i18n/locales/zh-CN.ts`
6. `frontend/src/i18n/locales/en-US.ts`
7. `PROJECT_PROGRESS.md`

### 验证结果

1. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 构建成功。
2. `rg "path: '/user/promotion'|/user/promotion\\?|user-order-create|OrderCreateView" frontend/src -n`：确认旧的应用管理页跳转 `/user/promotion` 已无残留，普通订单入口统一指向 `user-order-create`。
3. 构建仍存在既有 Rollup PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题。

### 下一步任务清单

1. 刷新 `/user/promotion`，确认页面恢复为推广服务卡片入口，而不是订单表单。
2. 点击推广服务页 4 个普通服务，确认跳转 `/user/orders/create` 并预选订单类型。
3. 从应用管理页点击“创建订单”，确认进入 `/user/orders/create` 并预选应用。
4. 后续接入特殊订单审核流程后，再打开关键词保排名、关键词覆盖的提交入口。

### 风险点

1. 本次为前端路由和页面归属修正，没有新增后端逻辑或数据库结构。
2. `/user/orders/create` 是新增前端路由；如果浏览器还加载旧前端资源，需要刷新页面或重启前端开发服务后验证。
## 2026-06-20 更新：真实前后端预览与订单页面视觉审查修正

- 状态：已完成前端代码修正、后端最新 jar 重建、真实浏览器复测，待用户确认窄屏侧栏处理方式是否符合预期。
- 当前任务：修复静态预览不可靠与 Vite 依赖缓存问题，在真实前后端服务下审查并修正订单相关页面视觉/控制台问题。

### 问题定位

1. 静态预览只能验证构建产物，不会走 Vite 真实开发代理与登录态，订单页审查不够准确。
2. Vite 真实开发服务此前出现依赖预构建缓存异常；已清理 `frontend/node_modules/.vite` 与 `.vite-temp` 后使用 `--force` 重新优化。
3. 订单接口 500 的根因不是前端页面，而是本地启动了旧后端 jar；旧 jar 内迁移版本只到 V3，当前源码和数据库已到 V9。
4. 真实浏览器审查发现订单创建页顶部标题仍显示推广服务且菜单未高亮、窄屏固定侧栏造成横向溢出、空订单表格触发 `ordersPage.types.undefined` 警告。

### 实施内容

1. 重新构建后端 jar，并重启真实后端服务，确认 Flyway 校验 9 个迁移且订单列表接口返回空数组而不是 500。
2. 用户端订单创建路由标题改为 `orderCreate.title`，并通过 `activeMenu` 高亮推广服务入口。
3. 用户端和管理员端布局在 700px 以下隐藏固定侧栏、压缩顶部栏和页面 padding，避免整页横向滚动；表格仍由 Element Plus 自身处理内部横向滚动。
4. 订单列表、管理员订单列表、待确认订单页的订单类型/状态/商店显示函数增加空值兜底，避免空数据或异常行触发 i18n undefined 警告。
5. 订单创建页 `el-radio-button` 改用 `value`，消除 Element Plus 3.0 弃用警告。
6. 中英文补充订单创建页标题文案。

### 修改文件

1. `frontend/src/router/index.ts`
2. `frontend/src/layouts/UserLayout.vue`
3. `frontend/src/layouts/AdminLayout.vue`
4. `frontend/src/views/user/OrderCreateView.vue`
5. `frontend/src/views/user/StoreOrdersView.vue`
6. `frontend/src/views/admin/StoreOrdersView.vue`
7. `frontend/src/views/admin/PendingConfirmOrdersView.vue`
8. `frontend/src/i18n/locales/zh-CN.ts`
9. `frontend/src/i18n/locales/en-US.ts`
10. `PROJECT_PROGRESS.md`

### 执行命令与验证结果

1. `mvn.cmd package -DskipTests`：首次因旧 Java 进程锁定 jar 失败；停止由本次启动的旧进程后重试通过，最新 jar 已生成。
2. 真实后端启动验证：日志显示 Tomcat 启动在 8080，Flyway 成功校验 9 个迁移，schema 已是 V9。
3. 真实接口验证：`GET /api/customer/orders?storeType=APP_STORE` 与 `GET /api/admin/orders?storeType=APP_STORE` 均返回成功空数组，不再 500。
4. `npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 构建成功。
5. 浏览器复测：
   - `/user/orders/create?orderType=DOWNLOAD` 桌面：标题为“创建订单”，推广服务菜单高亮，横向溢出 0，无控制台错误/警告。
   - `/user/orders/create?orderType=DOWNLOAD` 390px：横向溢出 0，无控制台错误/警告。
   - `/user/orders/apple` 390px：横向溢出 0，无订单加载失败，无控制台错误/警告。
   - `/admin/pending-confirm-orders` 390px：横向溢出 0，无订单加载失败，无控制台错误/警告。
   - `/admin/orders/apple` 桌面：横向溢出 0，无订单加载失败，无控制台错误/警告。
6. 构建仍存在既有 Rollup PURE 注释提示、主 chunk 超过 500k 和 npm 新版本提示，均为非阻断问题，本次未处理。

### 下一步任务清单

1. 用户在真实浏览器中确认 700px 以下隐藏侧栏的移动端处理是否接受；如果需要移动端菜单入口，应单独设计抽屉/折叠菜单。
2. 为订单创建页准备真实测试应用和余额数据，继续验证普通订单提交、扣款、待确认列表刷新。
3. 继续实现特殊订单审核流，包括审核提交、管理员协商内容/价格、审核通过后用户支付生成正式订单。
4. 后续可考虑抽取公共后台布局响应式策略，但需要避免一次性大改多个业务页面。

### 风险点

1. 本次移动端处理为小范围 CSS 收敛，在窄屏隐藏侧栏；这解决横向溢出，但不是完整移动端导航方案。
2. 当前本地预览依赖真实后端、MySQL 和测试账号数据；换环境后需重新启动最新后端 jar 或使用 `mvn spring-boot:run`。
3. 后端启动日志仍有 Spring Boot 默认 generated security password、MyBatis mapper 未发现提示、Flyway MySQL 8.3 支持版本提示，属于既有非阻断收尾项。
## 2026-06-28 更新：普通订单创建支持多地区数量配置

- 状态：已完成代码修改、后端完整测试、前端生产构建和后端 jar 打包。
- 当前任务：下载量、星级评分、用户评价订单在创建订单时不应只有一个数量字段，应像关键词安装一样支持在同一个订单中配置多个国家/地区及各自数量。
- 实现原则：复用现有订单明细表 `aso_order_item` 保存多地区明细；不新增依赖；不修改 `.env` 或秘钥；不删除文件；不做不必要的路由或架构调整。

### 实施内容

1. 后端创建订单命令新增 `regionItems` 参数，用于承载多地区下载量、评分、评价数量。
2. 用户端和管理员端创建普通订单时，下载量、星级评分、用户评价改为多地区配置表单。
3. 后端计价逻辑按每个地区生成订单明细：
   - 下载量：每个地区按 `天数 * 每日下载量 * 单价` 计费。
   - 星级评分：每个地区分别按 5 星评分和 4 星评分数量计费。
   - 用户评价：每个地区分别按 5 星评价和 4 星评价数量计费。
4. 关键词安装继续使用已有的“地区 + 多关键词 + 每日数量”结构。
5. 前端订单摘要改为汇总所有地区数量，提交 payload 同步传入 `regionItems`。
6. 调整多地区数量表格样式，修复表头与内容列数不一致导致的错位风险。
7. 补齐订单创建页中“操作”文案的中英文翻译。

### 修改文件

1. `backend/src/main/java/com/youou/aso/modules/order/dto/CreateOrderCommand.java`
2. `backend/src/main/java/com/youou/aso/modules/order/api/CustomerOrderController.java`
3. `backend/src/main/java/com/youou/aso/modules/order/api/AdminOrderController.java`
4. `backend/src/main/java/com/youou/aso/modules/order/service/OrderService.java`
5. `backend/src/test/java/com/youou/aso/modules/order/service/OrderServiceTest.java`
6. `frontend/src/api/orders.ts`
7. `frontend/src/views/user/OrderCreateView.vue`
8. `frontend/src/views/admin/OrderCreateView.vue`
9. `frontend/src/i18n/locales/zh-CN.ts`
10. `frontend/src/i18n/locales/en-US.ts`
11. `PROJECT_PROGRESS.md`

### 验证结果

1. `cmd /c mvn.cmd -Dtest=OrderServiceTest test`：通过，26 个订单服务测试全部成功。
2. `cmd /c npx vue-tsc --noEmit`：通过，前端类型检查成功。
3. `cmd /c npx vite build --debug`：通过，用于确认第一次 Vite 路径异常不是业务代码问题。
4. `cmd /c npm.cmd run build`：通过，前端生产构建成功。
5. `cmd /c mvn.cmd test package`：通过，91 个后端测试全部成功，并生成最新后端 jar。
6. `Compress-Archive`：已生成前端部署包 `deploy/youou-aso-frontend-20260628-012649.zip`。
7. `Copy-Item`：已生成后端部署 jar 副本 `deploy/youou-aso-backend-20260628-012649.jar`。

### 下一步任务清单

1. 将最新 `frontend/dist` 覆盖到线上前端站点目录，并重启或刷新 Nginx 缓存策略后验证页面。
2. 将最新 `backend/target/youou-aso-backend-0.1.0-SNAPSHOT.jar` 上传覆盖线上后端 jar，并重启 Java 项目。
3. 在线上分别创建下载量、星级评分、用户评价订单，验证多个国家/地区能落入同一个订单详情。
4. 在线上检查订单列表、订单详情、财务流水金额是否与多地区明细合计一致。
5. 如需要特殊订单也支持多地区审核明细，需要单独设计审核明细存储结构并做数据库迁移。

### 风险点

1. 特殊订单审核表当前仍是单地区字段；若要求关键词保排名、关键词覆盖也支持多地区，需要新增审核明细结构，不能用拼接文本方式临时处理。
2. 前端构建仍存在既有的 VueUse PURE 注释提示、主 chunk 超过 500k 提示和 npm 新版本提示，均为非阻断问题，本次未处理。
3. 线上发布需要同时更新前端静态资源和后端 jar，否则页面提交了 `regionItems` 但旧后端不会处理多地区明细。
## 2026-06-28 更新：创建订单地区配置按订单类型隔离

- 状态：已完成前端小范围修复，并通过类型检查和生产构建验证。
- 当前任务：修复创建订单页中“添加地区配置”使用同一份 `regionGroups`，导致在一个订单类型添加地区后，其它订单类型也同步出现地区组的问题。
- 根因：用户端和管理员端创建订单页共用 `form.regionGroups` 承载关键词安装、下载量、星级评分、用户评价四类普通订单的地区配置；订单类型切换时没有保存和恢复各类型自己的草稿。

### 实施内容

1. 用户端创建订单页新增按订单类型缓存地区配置草稿的逻辑。
2. 管理员端创建订单页新增同样的按订单类型缓存地区配置草稿逻辑。
3. 切换订单类型时，先保存旧类型地区配置，再加载新类型自己的地区配置。
4. 应用变化时统一重置四类普通订单的地区配置草稿，避免跨应用或跨商店带入旧地区。
5. 保留已完成的“添加地区配置”按钮局部宽度修复，按钮不再被列布局拉伸成整行。

### 修改文件

1. `frontend/src/views/user/OrderCreateView.vue`
2. `frontend/src/views/admin/OrderCreateView.vue`
3. `PROJECT_PROGRESS.md`

### 验证结果

1. `cmd /c npx vue-tsc --noEmit`：通过，前端类型检查成功。
2. `cmd /c npm.cmd run build`：通过，前端生产构建成功。
3. 构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 提示和 npm 新版本提示，均为非阻断问题，本次未处理。

### 下一步任务清单

1. 在线上或本地真实页面手动验证：下载量新增地区后，切换到评分、评价、关键词安装时不再共享同一份地区组。
2. 继续检查创建订单页其它订单类型的表格列宽、按钮位置和摘要金额是否与预期一致。
3. 如需重新部署，需要重新打包并覆盖线上前端静态资源。

### 风险点

1. 本次只修复前端表单状态隔离，不改后端接口和数据库结构。
2. 如果用户希望不同订单类型之间保留同一个地区选择但不同数量，需要另行设计“共享地区、独立参数”的交互模型；当前实现为各订单类型完全独立草稿。
## 2026-06-28 更新：创建订单地区选择去默认值与去重

- 状态：已完成前端小范围修复，并通过类型检查和生产构建验证。
- 当前任务：修复创建订单页新增地区时默认选中美国，以及同一订单内多个地区组可以选择同一个国家/地区的问题。
- 实现原则：只调整用户端和管理员端创建订单页表单行为，不改后端接口、数据库结构、路由或依赖。

### 实施内容

1. 用户端创建订单页新增地区组时不再自动选中第一个地区，新增行保持空值，用户必须手动选择国家/地区。
2. 管理员端创建订单页保持同样逻辑，新增地区组不再默认美国或其它第一个可用地区。
3. 地区下拉选项按当前订单类型的地区组进行过滤：其它地区组已选中的国家/地区不再出现在当前行可选列表中。
4. 当前行已经选中的地区会继续保留在当前行下拉中，避免打开下拉时当前值消失。
5. 选应用或切换应用时，四类普通订单的地区草稿重置为空地区组，避免默认带入第一个地区。

### 修改文件

1. `frontend/src/views/user/OrderCreateView.vue`
2. `frontend/src/views/admin/OrderCreateView.vue`
3. `PROJECT_PROGRESS.md`

### 验证结果

1. `cmd /c npx vue-tsc --noEmit`：通过，前端类型检查成功。
2. `cmd /c npm.cmd run build`：通过，前端生产构建成功。
3. 构建仍存在既有 VueUse PURE 注释提示、主 chunk 超过 500k 提示和 npm 新版本提示，均为非阻断问题，本次未处理。

### 下一步任务清单

1. 在真实页面手动验证：新增地区组后地区下拉为空，不自动显示美国。
2. 在同一订单类型中选择一个地区后，验证其它地区组的下拉列表不再出现该地区。
3. 分别验证关键词安装、下载量、星级评分、用户评价四类普通订单的地区选择行为一致。

### 风险点

1. 由于不再默认地区，用户提交订单前必须主动选择国家/地区；这是符合当前产品要求的行为变化。
2. 如果线上前端资源有缓存，需要重新覆盖静态资源并刷新浏览器缓存后才能看到最新行为。
## 2026-06-28 更新：关键词保排名老项目字段差异排查

- 状态：已完成只读排查，未修改业务代码，待用户确认是否进入实现。
- 当前任务：对比老项目中关键词保排名订单字段与新项目现有特殊订单审核实现，确认是否存在新项目未结构化实现的字段。
- 实现原则：本次仅阅读代码和接口文档，不修改 `.env`、秘钥、依赖、数据库结构、路由或业务代码。

### 排查结论

1. 老项目关键词保排名下单主数据包含 `appId`、`customerAppId`、`storeType`、`orderType`、`orderStatus`、`communicateType`、`communicateNumber`、`remark`、`orderKeywordRanks`。
2. 老项目 `orderKeywordRanks` 为多地区结构：每个地区包含 `area` 和 `keywordRankList`。
3. 老项目关键词保排名明细字段包含 `keyword` 和 `targetRank`。
4. 老项目后端 `keyword_order_extension` 还预留/使用了运营执行字段：`rank`、`price`、`successDay`、`targetDay`、`spend`、`totalPrice`、`itemStatus`、`delFlag`。
5. 新项目当前特殊订单审核只保存单个 `regionCode`、`requestedContent`、`negotiatedContent`、`negotiatedPrice` 和审核状态，没有结构化保存多地区关键词、目标排名、联系方式和关键词执行明细。

### 涉及读取文件

1. `Appfast-rouyi/api.md`
2. `Appfast-rouyi/src/views/promotion/createOrder.vue`
3. `RuoYi-Vue/ruoyi-system/src/main/java/com/ruoyi/system/domain/KeywordOrder.java`
4. `RuoYi-Vue/ruoyi-system/src/main/java/com/ruoyi/system/domain/KeywordOrderExtension.java`
5. `RuoYi-Vue/ruoyi-system/src/main/java/com/ruoyi/system/domain/dto/OrderKeywordRankDTO.java`
6. `RuoYi-Vue/ruoyi-system/src/main/java/com/ruoyi/system/domain/dto/KeywordRankDTO.java`
7. `youou-aso/backend/src/main/java/com/youou/aso/modules/order/domain/SpecialOrderAudit.java`
8. `youou-aso/backend/src/main/resources/db/migration/V8__order_wallet_foundation.sql`
9. `youou-aso/frontend/src/api/specialOrderAudits.ts`
10. `youou-aso/frontend/src/views/user/SpecialOrdersView.vue`
11. `youou-aso/frontend/src/views/admin/AuditManagementView.vue`

### 验证结果

1. 已通过 `rg` 搜索定位老项目和新项目关键词保排名相关实现。
2. 已人工对比老项目下单参数、前端表单、后端领域对象和新项目特殊订单审核表结构。
3. 本次未运行构建或测试，因为未修改业务代码。

### 下一步任务清单

1. 等待确认是否要将关键词保排名从“纯文本审核需求”升级为“结构化多地区关键词目标排名”。
2. 若确认实现，需要先设计生产可用的数据结构，优先新增特殊订单审核明细表或复用明确可承载的明细表，不建议把多地区关键词拼接进文本字段。
3. 同步评估关键词覆盖服务是否也要按老项目结构化迁移。

### 风险点

1. 结构化实现会涉及数据库迁移和前后端接口调整，属于大改动保护范围，需要用户确认后才能实施。
2. 如果继续使用 `requestedContent` 文本保存关键词和目标排名，后续查询、导出、执行状态追踪、财务核算和运营统计都会不可维护。
## 2026-06-28 更新：其他订单类型老项目字段差异排查

- 状态：已完成只读排查，未修改业务代码，待用户确认是否进入实现。
- 当前任务：继续对比老项目普通订单字段与新项目当前实现，确认除关键词保排名外是否仍有字段缺口。
- 实现原则：本次仅阅读代码和接口文档，不修改 `.env`、秘钥、依赖、数据库结构、路由或业务代码。

### 排查结论

1. 关键词安装、下载量、评分订单的核心下单字段在新项目中基本已覆盖，包括应用、商店、日期、执行小时、地区和数量类字段。
2. 评论订单存在明显字段缺口：老项目支持按地区、星级提交 `commentDetailList`，每条评论包含 `commentTitle` 和 `commentContent`；新项目当前只保存 `review5Count` 和 `review4Count` 数量，没有保存具体评论标题和评论内容。
3. 老项目评分 DTO 里存在 `fileId`、`fileName` 字段，普通订单扩展表也有 `fileId`、`fileName`；新项目评分订单当前没有文件导入/附件字段。
4. 老项目普通订单扩展表存在运营执行类字段：`rank`、`targetRank`、`changeRank`、`progress`、`fileId`、`fileName`、`delFlag`；新项目 `aso_order_item` 当前主要保存 `itemType`、`itemName`、`regionCode`、`quantity`、`unitPrice`、`amount`、`metadataJson`，尚未结构化覆盖这些运营执行字段。
5. 老项目主订单包含 `communicateType`、`communicateNumber`、`remark`；新项目普通订单创建当前未接入联系方式和备注字段。

### 涉及读取文件

1. `Appfast-rouyi/api.md`
2. `RuoYi-Vue/ruoyi-system/src/main/java/com/ruoyi/system/domain/NormalOrder.java`
3. `RuoYi-Vue/ruoyi-system/src/main/java/com/ruoyi/system/domain/NormalOrderExtension.java`
4. `RuoYi-Vue/ruoyi-system/src/main/java/com/ruoyi/system/domain/dto/OrderAreaKeywordDTO.java`
5. `RuoYi-Vue/ruoyi-system/src/main/java/com/ruoyi/system/domain/dto/OrderAreaDownloadDTO.java`
6. `RuoYi-Vue/ruoyi-system/src/main/java/com/ruoyi/system/domain/dto/OrderAreaScoreDTO.java`
7. `RuoYi-Vue/ruoyi-system/src/main/java/com/ruoyi/system/domain/dto/OrderAreaCommentDTO.java`
8. `RuoYi-Vue/ruoyi-system/src/main/java/com/ruoyi/system/domain/dto/CommentDetailDTO.java`
9. `youou-aso/backend/src/main/java/com/youou/aso/modules/order/dto/CreateOrderCommand.java`
10. `youou-aso/backend/src/main/java/com/youou/aso/modules/order/domain/AsoOrder.java`
11. `youou-aso/backend/src/main/java/com/youou/aso/modules/order/domain/OrderItem.java`
12. `youou-aso/backend/src/main/java/com/youou/aso/modules/order/service/OrderService.java`
13. `youou-aso/frontend/src/api/orders.ts`
14. `youou-aso/frontend/src/views/user/OrderCreateView.vue`
15. `youou-aso/frontend/src/views/admin/OrderCreateView.vue`

### 验证结果

1. 已通过 `rg` 搜索和关键文件读取完成字段对比。
2. 已确认新项目评论订单未保存具体评论标题和评论内容。
3. 本次未运行构建或测试，因为未修改业务代码。

### 下一步任务清单

1. 等待确认是否优先补齐评论订单的结构化评论明细字段。
2. 若确认实现，需要评估是否在 `aso_order_item.metadata_json` 中保存评论明细，还是新增评论明细表；生产可维护性上更推荐结构化明细表或明确的 JSON schema。
3. 再确认是否需要补齐普通订单联系方式、备注、文件导入附件和运营执行进度字段。

### 风险点

1. 评论标题/内容属于用户输入内容，补齐时需要做长度限制、敏感内容处理、导出安全和 XSS 防护。
2. 文件导入/附件能力涉及上传存储、文件校验和安全扫描，属于较大范围变更，需要单独确认。
3. 运营执行字段如果直接塞进无约束 JSON，短期快但长期查询、导出和统计维护成本较高。
## 2026-06-28 更新：补齐老项目遗漏的结构化下单字段

- 状态：已完成后端、前端与验证；本次按用户确认执行，新增数据库迁移，不复刻老项目旧状态和运营执行字段。
- 当前任务：补齐关键词保排名/关键词覆盖、评论订单、普通订单联系方式与备注等遗漏能力，同时保持新项目现有订单架构和页面路由不变。
- 实现原则：生产可用、结构化存储、前后端字段一致；不修改 `.env`/秘钥；不安装依赖；不删除业务文件；不引入旧项目 `delFlag`、旧状态码和执行过程字段。

### 实施内容

1. 普通订单主表新增 `contact_type`、`contact_value`、`remark`，用于保存联系方式和备注。
2. 评论订单新增结构化评论明细，支持按地区保存星级、评论标题和评论内容，并按明细数量聚合计价。
3. 特殊订单审核新增结构化关键词明细，支持关键词保排名保存地区、关键词、目标排名；关键词覆盖保存地区、关键词、覆盖说明。
4. 用户端和管理员端创建订单页同步补齐评论明细、特殊订单关键词明细、联系方式和备注输入。
5. 订单详情页补充联系方式、备注、评论明细展示；特殊订单审核列表补充关键词明细摘要展示。
6. 后端服务补充长度校验、星级校验、目标排名校验和明细保存/查询聚合逻辑。
7. 迁移文件最终使用 `V16__structured_order_details.sql`，避免与已有 `V10__customer_app_region_relation.sql` 冲突。

### 修改文件

1. `backend/src/main/java/com/youou/aso/modules/order/domain/AsoOrder.java`
2. `backend/src/main/java/com/youou/aso/modules/order/domain/OrderCommentDetail.java`
3. `backend/src/main/java/com/youou/aso/modules/order/domain/SpecialOrderAudit.java`
4. `backend/src/main/java/com/youou/aso/modules/order/domain/SpecialOrderAuditItem.java`
5. `backend/src/main/java/com/youou/aso/modules/order/dto/CreateOrderCommand.java`
6. `backend/src/main/java/com/youou/aso/modules/order/dto/OrderCommentDetailResult.java`
7. `backend/src/main/java/com/youou/aso/modules/order/dto/OrderResult.java`
8. `backend/src/main/java/com/youou/aso/modules/order/dto/SpecialOrderAuditItemResult.java`
9. `backend/src/main/java/com/youou/aso/modules/order/dto/SpecialOrderAuditResult.java`
10. `backend/src/main/java/com/youou/aso/modules/order/dto/SubmitSpecialAuditCommand.java`
11. `backend/src/main/java/com/youou/aso/modules/order/api/AdminOrderController.java`
12. `backend/src/main/java/com/youou/aso/modules/order/api/CustomerOrderController.java`
13. `backend/src/main/java/com/youou/aso/modules/order/api/CustomerSpecialOrderAuditController.java`
14. `backend/src/main/java/com/youou/aso/modules/order/repository/JdbcOrderCommentDetailRepository.java`
15. `backend/src/main/java/com/youou/aso/modules/order/repository/JdbcOrderRepository.java`
16. `backend/src/main/java/com/youou/aso/modules/order/repository/JdbcSpecialOrderAuditItemRepository.java`
17. `backend/src/main/java/com/youou/aso/modules/order/repository/OrderCommentDetailRepository.java`
18. `backend/src/main/java/com/youou/aso/modules/order/repository/SpecialOrderAuditItemRepository.java`
19. `backend/src/main/java/com/youou/aso/modules/order/service/OrderService.java`
20. `backend/src/main/java/com/youou/aso/modules/order/service/SpecialOrderAuditService.java`
21. `backend/src/main/resources/db/migration/V16__structured_order_details.sql`
22. `backend/src/test/java/com/youou/aso/modules/order/api/AdminOrderControllerTest.java`
23. `backend/src/test/java/com/youou/aso/modules/order/service/OrderServiceTest.java`
24. `backend/src/test/java/com/youou/aso/modules/order/service/SpecialOrderAuditServiceTest.java`
25. `frontend/src/api/orders.ts`
26. `frontend/src/api/specialOrderAudits.ts`
27. `frontend/src/views/user/OrderCreateView.vue`
28. `frontend/src/views/admin/OrderCreateView.vue`
29. `frontend/src/views/user/OrderDetailView.vue`
30. `frontend/src/views/admin/OrderDetailView.vue`
31. `frontend/src/views/user/SpecialOrdersView.vue`
32. `frontend/src/views/admin/AuditManagementView.vue`
33. `frontend/src/i18n/locales/zh-CN.ts`
34. `frontend/src/i18n/locales/en-US.ts`
35. `PROJECT_PROGRESS.md`

### 验证结果

1. `cmd /c mvn.cmd "-Dtest=OrderServiceTest,SpecialOrderAuditServiceTest" test`：通过，33 个服务测试成功。
2. `cmd /c npx vue-tsc --noEmit`：通过，前端类型检查成功。
3. `cmd /c npm.cmd run build`：通过，前端生产构建成功；仍有既有 VueUse PURE 注释、主 chunk 超过 500k、npm 新版本提示，均为非阻断提示。
4. `cmd /c mvn.cmd test`：首次失败，原因是测试构建产物中残留改名前的 `V10__structured_order_details.sql`，并且管理员特殊订单测试 mock 仍按旧签名匹配。
5. `cmd /c mvn.cmd clean test`：通过，93 个后端测试成功；本地测试数据库已应用 `V16` 迁移。

### 下一步任务清单

1. 在真实页面手动验证用户端和管理员端评论订单：新增地区、添加多条评论、提交后详情页可见评论明细。
2. 在真实页面手动验证关键词保排名和关键词覆盖：提交多条关键词明细后，用户端/管理员端特殊订单列表可见摘要。
3. 若后续需要附件/文件导入能力，应单独设计上传、存储、校验和安全扫描流程，不建议塞入本次订单字段补齐范围。
4. 若后续需要运营执行过程字段（排名变化、进度、消耗等），应按执行模块单独建模，避免复刻旧项目扩展表杂糅字段。

### 风险点

1. 本次新增 `V16` 数据库迁移，线上发布必须先做好数据库备份，并确保 Flyway 迁移顺序与当前线上版本一致。
2. `mvn clean test` 已在本地测试库应用 `V16`，如需回滚本地库需要按数据库备份/迁移记录处理，不能直接删除迁移记录。
3. 评论标题/内容、特殊订单覆盖说明均为用户输入内容，后续导出或富文本展示时仍需保持转义，避免 XSS 和注入风险。
4. 当前未实现附件上传、文件导入、运营执行进度字段，这是有意控制范围，不属于遗漏功能补齐的本次交付。
## 2026-06-28 更新：纠正普通订单联系方式字段归属

- 状态：已完成纠偏实现与验证。根据用户反馈重新核对老系统，确认联系方式只属于关键词保排名/关键词覆盖等特殊订单提交流程，普通订单不应包含联系方式或订单备注字段。
- 当前任务：将前一次误加到普通订单主表和普通订单下单/详情链路中的联系方式、备注移除，并保留特殊订单审核链路中的联系方式能力。
- 实现原则：不复刻老系统冗余字段；不修改 `.env`/秘钥；不安装依赖；不删除文件；不改路由和整体架构；已应用过的 `V16` 不回写修改，使用 `V17` 做前向迁移，避免 Flyway 校验风险。

### 实施内容

1. 普通订单领域模型、创建命令、查询结果、仓储保存/读取逻辑移除 `contactType`、`contactValue`、`remark` 业务字段。
2. 新增 `V17__move_special_order_contact_fields.sql`，将联系方式字段放入 `special_order_audit`，并从 `aso_order` 删除前次误加的联系方式和备注字段。
3. 用户端/管理员端创建订单页中，联系方式输入仅在特殊订单类型下展示与提交；普通订单创建 payload 不再提交联系方式或备注。
4. 用户端/管理员端订单详情页不再展示普通订单联系方式或备注。
5. 特殊订单审核 DTO、仓储、服务、审核列表补充联系方式读写和展示；管理员为用户创建特殊订单时保留联系方式传递。
6. 补充 DTO/Service 兼容构造与测试断言，避免旧测试调用受特殊订单联系方式参数影响。

### 修改文件

1. `backend/src/main/java/com/youou/aso/modules/order/domain/AsoOrder.java`
2. `backend/src/main/java/com/youou/aso/modules/order/domain/SpecialOrderAudit.java`
3. `backend/src/main/java/com/youou/aso/modules/order/dto/CreateOrderCommand.java`
4. `backend/src/main/java/com/youou/aso/modules/order/dto/OrderResult.java`
5. `backend/src/main/java/com/youou/aso/modules/order/dto/SpecialOrderAuditResult.java`
6. `backend/src/main/java/com/youou/aso/modules/order/dto/SubmitSpecialAuditCommand.java`
7. `backend/src/main/java/com/youou/aso/modules/order/api/AdminOrderController.java`
8. `backend/src/main/java/com/youou/aso/modules/order/api/CustomerOrderController.java`
9. `backend/src/main/java/com/youou/aso/modules/order/api/CustomerSpecialOrderAuditController.java`
10. `backend/src/main/java/com/youou/aso/modules/order/repository/JdbcOrderRepository.java`
11. `backend/src/main/java/com/youou/aso/modules/order/repository/JdbcSpecialOrderAuditRepository.java`
12. `backend/src/main/java/com/youou/aso/modules/order/service/OrderService.java`
13. `backend/src/main/java/com/youou/aso/modules/order/service/SpecialOrderAuditService.java`
14. `backend/src/main/resources/db/migration/V17__move_special_order_contact_fields.sql`
15. `backend/src/test/java/com/youou/aso/modules/order/api/AdminOrderControllerTest.java`
16. `frontend/src/api/orders.ts`
17. `frontend/src/api/specialOrderAudits.ts`
18. `frontend/src/views/user/OrderCreateView.vue`
19. `frontend/src/views/admin/OrderCreateView.vue`
20. `frontend/src/views/user/OrderDetailView.vue`
21. `frontend/src/views/admin/OrderDetailView.vue`
22. `frontend/src/views/user/SpecialOrdersView.vue`
23. `frontend/src/views/admin/AuditManagementView.vue`
24. `frontend/src/i18n/locales/zh-CN.ts`
25. `frontend/src/i18n/locales/en-US.ts`
26. `PROJECT_PROGRESS.md`

### 验证结果

1. `cmd /c mvn.cmd test`：通过，93 个后端测试成功；Flyway 已验证 17 个迁移，并将本地库从 v16 迁移到 v17。
2. `cmd /c npx vue-tsc --noEmit`：通过，前端类型检查成功。
3. `cmd /c npm.cmd run build`：通过，前端生产构建成功；仍有既有 VueUse PURE 注释、主 chunk 超过 500k、npm 新版本提示，均为非阻断提示。
4. `rg` 检查确认普通订单领域模型、普通订单结果 DTO、普通订单 JDBC 仓储和普通订单详情页不再包含联系方式/备注字段。

### 下一步任务清单

1. 在真实页面手动验证普通订单类型：关键词安装、下载量、评分、评论下单页面均不出现联系方式/备注，提交后详情页也不展示。
2. 在真实页面手动验证特殊订单类型：关键词保排名、关键词覆盖必须填写联系方式，审核列表可见联系方式摘要。
3. 若后续需要订单备注能力，应先明确它属于普通订单、特殊订单还是客服沟通记录，再单独设计字段归属，避免再次混入所有订单类型。

### 风险点

1. `V17` 会删除 `aso_order.contact_type`、`aso_order.contact_value`、`aso_order.remark`，若线上已写入这三个字段，需要发布前确认是否存在误写数据并决定是否迁移到 `special_order_audit` 或归档。
2. 本地库已从 v16 迁移到 v17，后续不要再修改已应用迁移文件的内容，新增修正继续使用新的迁移版本。
3. 联系方式属于用户输入内容，展示和导出时仍需保持转义，避免 XSS 或数据泄露风险。
## 2026-06-28 更新：评论订单添加评论按钮移入列表操作列

- 状态：已完成前端 UI 调整与验证。
- 当前任务：将评论订单明细中的“添加评论”按钮从列表下方移动到评论明细列表最后一列，保持用户端和管理员端创建订单页一致。
- 实现原则：仅调整当前页面局部布局，不修改后端、接口、路由、数据库、依赖或订单提交流程。

### 实施内容

1. 用户端创建订单页：评论明细列表最后一列改为操作单元，第一行显示“添加评论”按钮，同时保留删除按钮。
2. 管理员端创建订单页：同步上述布局，保持两端交互一致。
3. 移除评论明细列表下方独立的“添加评论”按钮。
4. 调整评论明细列表最后一列宽度和操作按钮对齐，避免按钮文字挤压。

### 修改文件

1. `frontend/src/views/user/OrderCreateView.vue`
2. `frontend/src/views/admin/OrderCreateView.vue`
3. `PROJECT_PROGRESS.md`

### 验证结果

1. `cmd /c npx vue-tsc --noEmit`：通过，前端类型检查成功。
2. `cmd /c npm.cmd run build`：通过，前端生产构建成功；仍有既有 VueUse PURE 注释、主 chunk 超过 500k、npm 新版本提示，均为非阻断提示。
3. `rg` 检查确认“添加评论”按钮已位于用户端和管理员端评论明细列表行内操作列。

### 下一步任务清单

1. 在真实页面手动验证用户端评论订单：第一行最后一列可添加评论，多行情况下每行删除按钮可用。
2. 在真实页面手动验证管理员端评论订单：交互与用户端一致。
3. 检查窄屏下评论明细列表最后一列按钮是否需要进一步压缩为纯图标按钮。

### 风险点

1. 本次只改 UI 布局，不改变提交数据；主要风险是窄屏下最后一列按钮展示空间不足。
2. “添加评论”仅在第一行显示，若后续希望每行都能添加，需要再调整交互规则。
## 2026-06-28 更新：关键词和评论明细新增项置顶与补全限制

- 状态：已完成前端交互调整与验证。
- 当前任务：按用户反馈，让“添加评论”与“添加关键词”保持同样的位置和交互，并让新增项显示在第一行；第一行未填完整前不能继续新增。
- 实现原则：只改用户端/管理员端创建订单页的前端表单交互，不修改后端、接口、路由、数据库、依赖或订单提交结构。

### 实施内容

1. 评论明细列表改为与关键词列表一致的列结构：删除按钮位于“操作”列，“添加评论”位于最后一列。
2. 评论明细表头新增最后一列空列头，使按钮位置与关键词列表对齐。
3. 关键词新增和评论新增都改为插入到第一行，便于用户优先填写最新添加项。
4. 第一行关键词未填写关键词或每日数量时，禁用“添加关键词”，并在函数层做提示兜底。
5. 第一行评论未填写标题或内容时，禁用“添加评论”，并在函数层做提示兜底。
6. 用户端和管理员端创建订单页同步处理，并补充中英文提示文案。

### 修改文件

1. `frontend/src/views/user/OrderCreateView.vue`
2. `frontend/src/views/admin/OrderCreateView.vue`
3. `frontend/src/i18n/locales/zh-CN.ts`
4. `frontend/src/i18n/locales/en-US.ts`
5. `PROJECT_PROGRESS.md`

### 验证结果

1. `cmd /c npx vue-tsc --noEmit`：通过，前端类型检查成功。
2. `cmd /c npm.cmd run build`：通过，前端生产构建成功；仍有既有 VueUse PURE 注释、主 chunk 超过 500k、npm 新版本提示，均为非阻断提示。
3. `rg` 检查确认用户端和管理员端均已包含 `canAddKeywordItem`、`canAddReviewItem`、新增置顶逻辑和补全提示文案。

### 下一步任务清单

1. 在真实页面手动验证关键词订单：第一行未填完整时不能继续添加；填完后新增行出现在最上方。
2. 在真实页面手动验证评论订单：添加评论按钮位于最后一列；第一行未填完整时不能继续添加；填完后新增评论出现在最上方。
3. 检查窄屏下评论明细最后两列是否需要进一步收窄或改为纯图标。

### 风险点

1. 新增项置顶会改变用户填写顺序感知，适合快速连续录入，但需要页面手动确认是否符合运营习惯。
2. 当前补全限制基于第一行判断；如果用户删除或编辑第一行为空，会再次阻止继续新增，这是预期行为。
## 2026-06-28 更新：特殊订单联系方式类型改为下拉框

- 状态：已完成前端 UI 调整与验证。
- 当前任务：按老项目交互，将关键词保排名/关键词覆盖等特殊订单中的联系方式类型从自由输入改为下拉选择。
- 实现原则：只调整前端表单控件和文案，不修改后端、接口、路由、数据库或依赖；新项目继续保存字符串类型，避免复刻老项目数字枚举耦合。

### 实施内容

1. 用户端创建订单页：特殊订单联系方式类型改为 `el-select`。
2. 管理员端创建订单页：同步改为 `el-select`。
3. 下拉选项为 `WeChat`、`Telegram`、`WhatsApp`。
4. 提交值继续使用字符串 `WeChat`、`Telegram`、`WhatsApp`，不改变后端存储结构。
5. 中英文占位文案改为选择型提示。

### 修改文件

1. `frontend/src/views/user/OrderCreateView.vue`
2. `frontend/src/views/admin/OrderCreateView.vue`
3. `frontend/src/i18n/locales/zh-CN.ts`
4. `frontend/src/i18n/locales/en-US.ts`
5. `PROJECT_PROGRESS.md`

### 验证结果

1. `cmd /c npx vue-tsc --noEmit`：通过，前端类型检查成功。
2. `cmd /c npm.cmd run build`：通过，前端生产构建成功；仍有既有 VueUse PURE 注释、主 chunk 超过 500k、npm 新版本提示，均为非阻断提示。
3. `rg` 检查确认用户端和管理员端均已使用 `contactTypeOptions` 与 `el-select`。

### 下一步任务清单

1. 在真实页面手动验证用户端特殊订单：联系方式类型只能选择 WeChat、Telegram、WhatsApp。
2. 在真实页面手动验证管理员端特殊订单：联系方式类型下拉框与用户端一致。
3. 检查特殊订单审核列表中联系方式展示是否符合运营阅读习惯。

### 风险点

1. 如果已有数据中存在非三种选项的历史字符串，下拉框不会主动展示为可选项；当前仅影响新建表单，不影响历史列表展示。
2. 新项目没有沿用老项目 `1/2/3` 数字值，若未来需要与老系统同步数据，需要做映射转换。
## 2026-06-28 更新：特殊订单参数区样式优化

- 状态：已完成前端样式调整与验证。
- 当前任务：根据截图反馈优化特殊订单“订单参数”区域的视觉表现，让关键词明细、联系方式和需求/金额区域更统一、美观。
- 实现原则：仅调整用户端/管理员端创建订单页局部 UI 样式，不修改后端、接口、路由、数据库、依赖或提交数据结构。

### 实施内容

1. 用户端特殊订单参数区增加统一浅色容器，收敛表格、联系方式和需求内容之间的视觉关系。
2. 管理员端特殊订单参数区同步增加统一浅色容器，金额输入区域使用同样的卡片式边框。
3. 特殊订单关键词明细表格改为 100% 容器宽度，增加圆角、浅色表头和统一边框色。
4. “添加关键词”按钮移入表格底部操作栏，避免按钮悬在表格边框内显得松散。
5. 联系方式区域改为带边框的双列表单块，联系方式类型列固定为更合适的宽度，联系方式输入列自适应。
6. 用户端需求内容 textarea 增加独立卡片式边框和稳定最小高度。
7. 补充窄屏响应式规则：联系方式改为单列，特殊订单明细表格允许横向滚动，避免布局挤坏。

### 修改文件

1. `frontend/src/views/user/OrderCreateView.vue`
2. `frontend/src/views/admin/OrderCreateView.vue`
3. `PROJECT_PROGRESS.md`

### 验证结果

1. `cmd /c npx vue-tsc --noEmit`：通过，前端类型检查成功。
2. `cmd /c npm.cmd run build`：通过，前端生产构建成功；仍有既有 VueUse PURE 注释、主 chunk 超过 500k、npm 新版本提示，均为非阻断提示。
3. `rg` 检查确认用户端和管理员端均已包含 `special-item-footer`、特殊订单容器宽度和联系方式区域样式。

### 下一步任务清单

1. 在真实页面手动验证用户端关键词保排名/关键词覆盖参数区视觉是否比截图更统一。
2. 在真实页面手动验证管理员端特殊订单参数区金额输入区域是否协调。
3. 检查 1366px、平板宽度和手机宽度下特殊订单明细表格是否需要进一步压缩列宽。

### 风险点

1. 本次只做局部样式优化，不改变业务逻辑；主要风险是不同屏幕宽度下表格横向滚动体验需要实机确认。
2. 特殊订单参数区最大宽度收敛为 1120px，若后续需要展示更多列，可能需要重新评估列宽。
## 2026-06-29 更新：星级评分订单金额按天数计算

- 状态：已完成前后端金额计算修复与验证。
- 当前任务：修复星级评分下单金额只按每日评分数量计算、未乘以订单天数的问题。
- 实现原则：只修正星级评分订单的金额/数量计算口径，不改路由、数据库、依赖和订单提交结构；前端展示与后端最终扣费保持一致。

### 实施内容

1. 用户端创建订单页：星级评分订单摘要中的数量、预估金额、费用明细均改为按 `订单天数 × 每日评分数量 × 单价` 计算。
2. 管理员端创建订单页：同步修复星级评分订单摘要和费用明细计算口径。
3. 后端订单服务：`calculateRating` 在单地区旧字段和多地区明细两种路径下，均将 4 星/5 星每日数量乘以订单天数后再生成计费项。
4. 后端测试：新增星级评分单地区、多地区跨天订单测试，覆盖数量、总金额和明细金额，避免前端展示正确但后端扣费仍错误。
5. 评论订单未纳入本次乘天数修复，因为评论明细代表具体评论内容，直接按天数复制会改变用户填写语义；本次仅处理截图反馈的星级评分。

### 修改文件

1. `frontend/src/views/user/OrderCreateView.vue`
2. `frontend/src/views/admin/OrderCreateView.vue`
3. `backend/src/main/java/com/youou/aso/modules/order/service/OrderService.java`
4. `backend/src/test/java/com/youou/aso/modules/order/service/OrderServiceTest.java`
5. `PROJECT_PROGRESS.md`

### 验证结果

1. `cmd /c mvn.cmd -Dtest=OrderServiceTest test`：通过，29 个后端订单服务测试成功。
2. `cmd /c mvn.cmd test`：通过，95 个后端测试成功；Flyway 校验 17 个迁移成功。
3. `cmd /c npx vue-tsc --noEmit`：通过，前端类型检查成功。
4. `cmd /c npm.cmd run build`：通过，前端生产构建成功；仍有既有 VueUse PURE 注释、主 chunk 超过 500k、npm 新版本提示，均为非阻断提示。
5. 代码检查确认用户端/管理员端星级评分摘要数量、金额和费用明细均已乘以 `totalDays`，后端星级评分计费项数量也已乘以订单天数。

### 下一步任务清单

1. 在真实页面手动验证截图场景：2026-06-29 到 2026-07-31 共 33 天，5 星数量 1、4 星数量 1，若单价均为 $0.01，摘要应显示数量 66、合计 $0.66。
2. 提交一笔星级评分测试订单，确认账户扣款、订单详情、消费记录中的金额与前端摘要一致。
3. 如后续确认评论订单也需要“每日评论数量 × 天数”的业务模型，应先重新设计评论录入方式，避免把一条具体评论内容错误复制为多天计费。

### 风险点

1. 本次修复只影响新创建的星级评分订单；历史已创建订单不会自动重算金额。
2. 如果运营侧原本希望星级评分数量表示“整个周期总量”而非“每日数量”，本次修复会改变计费口径；当前根据页面“天数”和截图反馈判断应按每日数量乘天数。
3. 前端金额展示只是预估，最终扣费以后端为准；本次已同步后端计算，降低展示与扣费不一致风险。
## 2026-06-29 更新：订单详情执行字段与单价展示修复

- 状态：已完成前端详情页展示修复与验证。
- 当前任务：修复订单详情中非关键词安装订单错误展示“执行小时”，以及费用信息单价在多明细订单中显示为 `-` 的问题。
- 实现原则：只调整用户端/管理员端订单详情页展示逻辑，不修改后端接口、数据库、路由、依赖或订单计费结构。

### 实施内容

1. 用户端订单详情页：只有 `KEYWORD_INSTALL` 订单显示“执行小时”，其他普通订单类型不再展示该字段。
2. 管理员端订单详情页：同步上述订单类型条件，避免用户端和后台展示不一致。
3. 关键词安装订单的日期标签从通用“订单时间”改为“执行日期”，与“执行小时”组成独立展示语义。
4. 费用信息中的单价增加前端汇总兜底：优先展示主单 `unitPrice`；主单为空且明细只有一个有效单价时展示该单价；明细存在多个不同单价时展示“多单价”，避免误导为单一价格。
5. 订单明细表中的逐行单价保持不变，继续展示每个计费项自己的真实单价。

### 修改文件

1. `frontend/src/views/user/OrderDetailView.vue`
2. `frontend/src/views/admin/OrderDetailView.vue`
3. `frontend/src/i18n/locales/zh-CN.ts`
4. `frontend/src/i18n/locales/en-US.ts`
5. `PROJECT_PROGRESS.md`

### 验证结果

1. `cmd /c npx vue-tsc --noEmit`：通过，前端类型检查成功。
2. `cmd /c npm.cmd run build`：通过，前端生产构建成功；仍有既有 VueUse PURE 注释、主 chunk 超过 500k、npm 新版本提示，均为非阻断提示。

### 下一步任务清单

1. 在真实页面手动验证星级评分、下载量、用户评价等非关键词安装订单：基础信息不再显示“执行小时”，费用信息能正确显示单价或“多单价”。
2. 在真实页面手动验证关键词安装订单：基础信息显示“执行日期”和“执行小时”，费用信息单价展示正常。
3. 如果后续需要更细的多单价展示，可考虑在费用卡片中展示单价范围，但需先确认运营侧展示口径。

### 风险点

1. 本次为前端展示逻辑修复，不改变订单数据和扣费金额。
2. 多单价订单在费用卡片中显示“多单价”是防误导处理，真实单价仍以订单明细表逐行展示为准。
## 2026-06-29 更新：订单详情地区显示与空内容列收敛

- 状态：已完成前端详情页展示修复与验证。
- 当前任务：修复订单详情基础信息缺少地区、明细地区在部分订单中显示 `-`，以及无内容订单类型仍展示空“内容”列的问题。
- 实现原则：只调整用户端/管理员端订单详情展示逻辑，不修改后端接口、数据库、路由、依赖或订单数据结构。

### 实施内容

1. 用户端订单详情基础信息新增“地区”字段，展示主单 `regionCode` 对应的地区名称。
2. 管理员端订单详情基础信息同步新增“地区”字段。
3. 订单明细表地区显示增加回退逻辑：优先使用明细行 `regionCode`，为空时回退到主单 `regionCode`，兼容旧订单和单地区订单。
4. 评论明细表地区显示同步使用回退逻辑，避免单地区旧数据展示为 `-`。
5. 订单明细表“内容”列改为按数据存在性展示：只有明细中存在真实 `itemName` 时才显示；评分、下载等无内容订单类型不再展示整列空值。

### 修改文件

1. `frontend/src/views/user/OrderDetailView.vue`
2. `frontend/src/views/admin/OrderDetailView.vue`
3. `PROJECT_PROGRESS.md`

### 验证结果

1. `cmd /c npx vue-tsc --noEmit`：通过，前端类型检查成功。
2. `cmd /c npm.cmd run build`：通过，前端生产构建成功；仍有既有 VueUse PURE 注释、主 chunk 超过 500k、npm 新版本提示，均为非阻断提示。

### 下一步任务清单

1. 在真实页面手动验证评分/下载订单详情：基础信息显示地区，明细行地区不再为空，且无“内容”列。
2. 在真实页面手动验证关键词安装订单详情：基础信息显示地区，明细表继续显示关键词内容列。
3. 在真实页面手动验证多地区订单详情：主单地区显示“多地区”，明细行显示各自具体地区。

### 风险点

1. 本次仅改变前端展示，不改变历史订单数据；旧订单明细没有地区时会回退展示主单地区。
2. “内容”列按是否存在 `itemName` 自动展示，若未来某类订单需要展示非 `itemName` 的说明内容，需要单独设计列映射。
## 2026-06-29 更新：订单列表新增新建订单入口

- 状态：已完成前端入口新增与验证。
- 当前任务：在订单列表页面增加“新建订单”按钮，点击后直接跳转到创建订单页面。
- 实现原则：复用已有创建订单路由，只增加页面入口；不修改后端接口、数据库、路由、依赖或订单创建流程。

### 实施内容

1. 用户端订单列表查询操作区新增“新建订单”主按钮，点击跳转到 `user-order-create`。
2. 管理员端订单列表查询操作区新增“新建订单”主按钮，点击跳转到 `admin-order-create`。
3. 按钮使用 Element Plus `Plus` 图标，保持与现有按钮风格一致。
4. 新增订单列表专用中英文文案：`ordersPage.newOrder`。

### 修改文件

1. `frontend/src/views/user/StoreOrdersView.vue`
2. `frontend/src/views/admin/StoreOrdersView.vue`
3. `frontend/src/i18n/locales/zh-CN.ts`
4. `frontend/src/i18n/locales/en-US.ts`
5. `PROJECT_PROGRESS.md`

### 验证结果

1. `cmd /c npx vue-tsc --noEmit`：通过，前端类型检查成功。
2. `cmd /c npm.cmd run build`：通过，前端生产构建成功；仍有既有 VueUse PURE 注释、主 chunk 超过 500k、npm 新版本提示，均为非阻断提示。

### 下一步任务清单

1. 在真实页面手动验证用户端订单列表点击“新建订单”后进入创建订单页。
2. 在真实页面手动验证管理员端订单列表点击“新建订单”后进入管理员创建订单页。
3. 检查窄屏下查询操作按钮是否换行正常。

### 风险点

1. 本次只增加前端跳转入口，不改变订单创建权限和提交逻辑。
2. 管理员端在待执行/执行中/已完成等状态列表也会显示“新建订单”，因为这些页面复用同一个订单列表组件；如后续希望只在按商店分类订单列表显示，需要再增加路由元信息条件。
## 2026-06-29 更新：订单编号与流水编号缩短

- 状态：已完成后端编号规则调整与验证。
- 当前任务：缩短订单编号和钱包流水编号，减少日期后面的数字位数过多导致的可读性问题。
- 实现原则：只影响新生成编号，不修改历史编号、不改数据库字段、不改接口结构；继续保留唯一索引兜底。

### 实施内容

1. 新增统一业务编号工具 `BusinessNumberGenerator`，格式为 `前缀 + yyMMdd + 6位后缀`。
2. 普通订单编号从 `YO + yyyyMMddHHmmss + 6位后缀` 调整为 `YO + yyMMdd + 6位后缀`，示例：`YO260629123456`。
3. 特殊订单提交后生成的正式订单编号同步使用新格式。
4. 钱包扣款/退款流水号、管理员充值流水号从 `WT + yyyyMMddHHmmss + 6位后缀` 调整为 `WT + yyMMdd + 6位后缀`，示例：`WT260629123456`。
5. 特殊需求审核号 `SA...` 未纳入本次修改，避免扩大任务范围；本次只处理用户反馈的订单编号和流水编号。
6. 新增和更新测试，覆盖公共编号工具、普通订单生成、特殊订单转正式订单、管理员充值流水生成。

### 修改文件

1. `backend/src/main/java/com/youou/aso/common/util/BusinessNumberGenerator.java`
2. `backend/src/main/java/com/youou/aso/modules/order/service/OrderService.java`
3. `backend/src/main/java/com/youou/aso/modules/order/service/SpecialOrderAuditService.java`
4. `backend/src/main/java/com/youou/aso/modules/wallet/service/JdbcWalletService.java`
5. `backend/src/main/java/com/youou/aso/modules/wallet/service/AdminWalletService.java`
6. `backend/src/test/java/com/youou/aso/common/util/BusinessNumberGeneratorTest.java`
7. `backend/src/test/java/com/youou/aso/modules/order/service/OrderServiceTest.java`
8. `backend/src/test/java/com/youou/aso/modules/order/service/SpecialOrderAuditServiceTest.java`
9. `backend/src/test/java/com/youou/aso/modules/wallet/service/AdminWalletServiceTest.java`
10. `PROJECT_PROGRESS.md`

### 验证结果

1. `cmd /c "mvn.cmd -Dtest=BusinessNumberGeneratorTest,OrderServiceTest,SpecialOrderAuditServiceTest,AdminWalletServiceTest test"`：通过，38 个相关测试成功。
2. `cmd /c mvn.cmd test`：通过，96 个后端测试成功；Flyway 校验 17 个迁移成功。
3. `rg` 检查确认订单号/流水号生成已统一使用 `BusinessNumberGenerator`；仅特殊审核号 `SA` 保留旧规则。

### 下一步任务清单

1. 新建一笔普通订单，确认订单号显示为 `YO260629xxxxxx` 这类 14 位短编号。
2. 触发订单扣款或管理员充值，确认流水号显示为 `WT260629xxxxxx` 这类 14 位短编号。
3. 若后续也希望缩短特殊需求审核号 `SA...`，需要单独确认后再处理。

### 风险点

1. 新规则只影响新数据，历史订单号和流水号不会自动变短。
2. 6 位后缀仍依赖唯一索引兜底；极端高并发下如果发生碰撞，数据库唯一约束会拒绝重复编号，后续可再引入重试机制。
3. 编号年份改为两位，人工排查跨世纪数据不如四位年份直观；当前业务可读性优先，且数据库时间字段仍保留完整年份。
## 2026-06-29 更新：订单列表筛选区响应式布局修复

- 状态：已完成前端布局修复与验证。
- 当前任务：修复部分电脑、浏览器缩放或可用宽度较窄时，订单列表筛选区换行散乱、操作按钮独占一行、表格区域视觉不协调的问题。
- 实现原则：只调整用户端/管理员端订单列表的布局样式，不修改筛选逻辑、路由、后端接口、数据库或依赖。

### 实施内容

1. 用户端订单列表筛选区从 `flex` 换行布局调整为响应式 `grid` 布局，筛选项按可用宽度自动排列。
2. 管理员端订单列表筛选区同步调整为响应式 `grid` 布局，避免不同电脑或缩放比例下控件散开。
3. 日期控件、下拉框、输入框统一使用容器宽度，避免固定宽度在窄屏或高 DPI 下提前挤压换行。
4. 操作按钮组保留原功能和顺序，允许自然换行但不再制造大块空白。
5. 管理员端补齐和用户端一致的查询面板卡片样式与表格卡片边框/阴影，让页面视觉更统一。
6. 表格卡片增加溢出控制，降低固定列阴影和宽表对整体布局的影响。

### 修改文件

1. `frontend/src/views/user/StoreOrdersView.vue`
2. `frontend/src/views/admin/StoreOrdersView.vue`
3. `PROJECT_PROGRESS.md`

### 验证结果

1. `cmd /c npx vue-tsc --noEmit`：通过，前端类型检查成功。
2. `cmd /c npm.cmd run build`：通过，前端生产构建成功；仍有既有 VueUse PURE 注释、主 chunk 超过 500k、npm 新版本提示，均为非阻断提示。

### 下一步任务清单

1. 在出现问题的电脑上刷新 `/admin/orders/apple`，确认筛选区不再出现大块空白和按钮孤立换行。
2. 分别检查 100%、125%、150% 浏览器缩放下管理员端订单列表布局。
3. 同步检查用户端订单列表，确认响应式排列和按钮换行正常。

### 风险点

1. 本次只改 CSS 和模板类名，不改变业务逻辑；主要风险是不同分辨率下筛选项每行数量会与之前不同。
2. 如果某些电脑宽度极窄，按钮仍会换行，但应保持紧凑、有序，不再出现截图中的松散布局。
## 2026-06-29 更新：常规订单余额不足进入待支付并支持重新编辑提交

- 状态：已完成常规订单支付状态流程调整，并通过后端全量测试与前端构建验证。
- 当前任务：修改常规订单提交逻辑；余额不足时保存订单为“待支付”，余额足时进入“已支付待确认”；待支付订单可在用户订单列表重新编辑提交。
- 实现原则：仅处理常规订单支付和重新提交流程；不修改数据库结构、不安装依赖、不修改 `.env` 或秘钥；特殊订单审核流程保持原有逻辑。

### 实施内容

1. 后端新增订单状态 `PENDING_PAYMENT`，用于表示常规订单已创建但未扣款成功。
2. 调整 `OrderService.createCustomerOrder()`：余额足时扣款并进入 `PENDING_CONFIRM`；余额不足时不抛出失败给前端丢单，而是保存订单、明细和评论明细，状态为 `PENDING_PAYMENT`，扣款流水和余额快照保持为空。
3. 新增 `OrderService.resubmitPendingPaymentOrder()`：仅允许当前用户自己的常规待支付订单重新提交；重新计算价格、替换订单明细、再次尝试扣款；余额足则进入 `PENDING_CONFIRM`，余额不足则继续保持 `PENDING_PAYMENT`。
4. 新增用户端接口 `POST /api/customer/orders/{id}/resubmit`，请求体复用创建订单结构，权限继续使用当前登录用户。
5. 仓储层新增待支付订单草稿更新和订单明细替换能力；只替换当前待支付订单的明细，不删除订单或历史订单。
6. 管理员代用户创建普通订单时按真实订单状态返回：余额不足返回 `paid=false`、`waitPayment=true`，避免前端误判已支付。
7. 前端订单状态新增“待支付”，原 `PENDING_CONFIRM` 文案改为“已支付待确认”。
8. 用户端订单列表对待支付普通订单新增“编辑提交”入口，跳转创建订单页并携带 `orderId`。
9. 用户端创建订单页支持待支付订单编辑模式：加载原订单明细，回填关键词、下载量、评分、评论等常规订单表单；提交时调用重新提交接口。
10. 管理员端订单列表、详情页和首页状态标签同步支持待支付状态；管理员代下单余额不足时提示充值并返回订单列表。

### 修改文件

1. `backend/src/main/java/com/youou/aso/modules/order/domain/OrderStatus.java`
2. `backend/src/main/java/com/youou/aso/modules/order/service/OrderService.java`
3. `backend/src/main/java/com/youou/aso/modules/order/api/CustomerOrderController.java`
4. `backend/src/main/java/com/youou/aso/modules/order/api/AdminOrderController.java`
5. `backend/src/main/java/com/youou/aso/modules/order/dto/AdminCreateOrderResult.java`
6. `backend/src/main/java/com/youou/aso/modules/order/repository/OrderRepository.java`
7. `backend/src/main/java/com/youou/aso/modules/order/repository/JdbcOrderRepository.java`
8. `backend/src/main/java/com/youou/aso/modules/order/repository/OrderItemRepository.java`
9. `backend/src/main/java/com/youou/aso/modules/order/repository/JdbcOrderItemRepository.java`
10. `backend/src/main/java/com/youou/aso/modules/order/repository/OrderCommentDetailRepository.java`
11. `backend/src/main/java/com/youou/aso/modules/order/repository/JdbcOrderCommentDetailRepository.java`
12. `backend/src/test/java/com/youou/aso/modules/order/service/OrderServiceTest.java`
13. `backend/src/test/java/com/youou/aso/modules/order/service/SpecialOrderAuditServiceTest.java`
14. `frontend/src/api/orders.ts`
15. `frontend/src/views/user/StoreOrdersView.vue`
16. `frontend/src/views/user/OrderCreateView.vue`
17. `frontend/src/views/user/OrderDetailView.vue`
18. `frontend/src/views/user/DashboardView.vue`
19. `frontend/src/views/admin/StoreOrdersView.vue`
20. `frontend/src/views/admin/OrderCreateView.vue`
21. `frontend/src/views/admin/OrderDetailView.vue`
22. `frontend/src/views/admin/DashboardView.vue`
23. `frontend/src/i18n/locales/zh-CN.ts`
24. `frontend/src/i18n/locales/en-US.ts`
25. `PROJECT_PROGRESS.md`

### 验证结果

1. `cmd /c mvn.cmd -Dtest=OrderServiceTest test`：通过，31 个订单服务测试成功；新增测试覆盖余额不足生成待支付、待支付重提余额仍不足、待支付重提余额足转已支付待确认。
2. `cmd /c npx vue-tsc --noEmit`：通过，前端类型检查成功。
3. `cmd /c mvn.cmd test`：通过，98 个后端测试成功；Flyway 校验 17 个迁移成功，无需数据库结构变更。
4. `cmd /c npm.cmd run build`：通过，前端生产构建成功；仍有既有 VueUse PURE 注释、主 chunk 超过 500k、npm 新版本提示，均为非阻断提示。

### 下一步任务清单

1. 在真实页面用低余额账号创建一笔普通订单，确认提交后提示“余额不足，请去充值”，订单列表出现“待支付”。
2. 在订单列表点击“编辑提交”，确认原订单内容可回填并可修改。
3. 充值后重新提交同一待支付订单，确认状态进入“已支付待确认”，并生成消费流水。
4. 在余额仍不足时重新提交，确认订单继续保持“待支付”，不会生成扣款流水。
5. 管理员待确认订单页检查只显示“已支付待确认”订单，不显示“待支付”订单。

### 风险点

1. 待支付订单回填依赖订单明细反推表单数据；新创建的待支付订单可正常回填，若历史异常订单缺少明细，可能只能回填基础信息。
2. 下载量和星级评分编辑回填会根据订单天数把总数量还原为每日数量；如果未来业务改为“总量”模型，需要同步调整回填逻辑。
3. 管理员端本次只支持代下单余额不足后的状态展示和提示，不提供管理员替用户重新提交待支付订单，避免扩大权限和代扣款风险。
4. `youou-aso` 在当前 Git 根下显示为整体未跟踪目录，本次只按实际编辑文件记录修改范围，未处理其他旧项目目录的既有改动。
## 2026-06-29 更新：待支付订单余额快照非空修复

- 状态：已完成余额不足创建待支付订单时的余额快照修复，并通过后端全量测试与前端构建验证。
- 当前任务：修复低余额提交普通订单时前端出现 `System error` 的问题。
- 根因分析：待支付订单没有扣款流水，但旧表结构要求 `balance_before` 和 `balance_after` 非空；之前实现将两者置空，导致数据库插入失败，被全局异常处理为系统错误。
- 实现原则：不修改数据库结构、不新增迁移、不安装依赖、不修改 `.env` 或秘钥；只调整待支付订单写入逻辑。

### 实施内容

1. `WalletService` 新增 `currentBalanceForUpdate(customerId)`，用于在同一事务中读取当前钱包余额。
2. `JdbcWalletService` 通过 `SELECT * FROM wallet_account WHERE customer_id = ? FOR UPDATE` 获取当前余额，保证与扣款尝试处于一致的事务锁语义。
3. `OrderService` 在捕获 `BALANCE_NOT_ENOUGH` 时，状态仍设为 `PENDING_PAYMENT`，但将 `balanceBefore` 和 `balanceAfter` 都写为当前余额；`deductedTransactionId` 继续保持为空，表示没有实际扣款流水。
4. 更新订单服务测试：余额不足创建待支付订单、余额不足重新提交待支付订单时，都断言余额快照等于当前余额。
5. 补齐测试假钱包服务的新接口实现，保持特殊订单测试编译通过。

### 修改文件

1. `backend/src/main/java/com/youou/aso/modules/wallet/service/WalletService.java`
2. `backend/src/main/java/com/youou/aso/modules/wallet/service/JdbcWalletService.java`
3. `backend/src/main/java/com/youou/aso/modules/order/service/OrderService.java`
4. `backend/src/test/java/com/youou/aso/modules/order/service/OrderServiceTest.java`
5. `backend/src/test/java/com/youou/aso/modules/order/service/SpecialOrderAuditServiceTest.java`
6. `PROJECT_PROGRESS.md`

### 验证结果

1. `cmd /c mvn.cmd -Dtest=OrderServiceTest test`：通过，31 个订单服务测试成功；确认待支付订单余额快照非空。
2. `cmd /c mvn.cmd test`：通过，98 个后端测试成功；Flyway 校验 17 个迁移成功，无新增数据库迁移。
3. `cmd /c npx vue-tsc --noEmit`：通过，前端类型检查成功。
4. `cmd /c npm.cmd run build`：通过，前端生产构建成功；仍有既有 VueUse PURE 注释、主 chunk 超过 500k、npm 新版本提示，均为非阻断提示。

### 下一步任务清单

1. 在真实页面重新用余额低于订单金额的账号提交普通订单，确认不再出现 `System error`。
2. 确认订单列表出现“待支付”，订单详情中余额快照显示为提交时余额，且没有消费流水。
3. 充值后从订单列表“编辑提交”重新提交，确认状态转为“已支付待确认”并生成消费流水。

### 风险点

1. 待支付订单的 `balanceBefore`/`balanceAfter` 表示“提交时余额快照”，不是扣款流水快照；是否扣款仍以 `deductedTransactionId` 是否为空为准。
2. 该修复不改变历史已生成订单；如果线上已有插入失败请求，不会自动补单，需要用户重新提交。
## 2026-06-29 更新：待支付订单列表操作按钮文案收敛

- 状态：已完成用户端订单列表待支付订单操作按钮文案调整，并通过前端类型检查。
- 当前任务：将订单列表中待支付普通订单的“编辑提交”按钮改为“编辑”。
- 实现原则：只调整前端国际化文案，不修改订单编辑跳转、重新提交接口、支付状态、数据库结构或后端逻辑。

### 实施内容

1. 中文文案 `ordersPage.editSubmit` 从“编辑提交”改为“编辑”。
2. 英文文案同步从 “Edit and submit” 改为 “Edit”。
3. 保留原有按钮位置、图标、显示条件和点击跳转逻辑。

### 修改文件

1. `frontend/src/i18n/locales/zh-CN.ts`
2. `frontend/src/i18n/locales/en-US.ts`
3. `PROJECT_PROGRESS.md`

### 验证结果

1. `cmd /c npx vue-tsc --noEmit`：通过，前端类型检查成功。

### 下一步任务清单

1. 刷新用户端订单列表，确认待支付订单操作列显示“编辑”。
2. 点击“编辑”确认仍进入创建订单页并回填待支付订单内容。

### 风险点

1. 本次仅改文案，不影响订单重新提交逻辑；如线上仍显示旧文案，需要清理浏览器或静态资源缓存。
## 2026-06-29 更新：待支付订单操作按钮改为上下两行

- 状态：已完成用户端订单列表操作列布局调整，并通过前端类型检查。
- 当前任务：将待支付订单操作列中的“编辑”和“详情”改为上下两行显示。
- 实现原则：只调整用户端订单列表局部 CSS，不修改按钮文案、跳转逻辑、订单状态、接口、数据库结构或后端逻辑。

### 实施内容

1. 用户端订单列表 `.row-actions` 从自动横向换行改为固定纵向排列。
2. 操作列内按钮显示为上方“编辑”、下方“详情”。
3. 保持原有按钮显示条件、图标、点击事件和权限判断不变。

### 修改文件

1. `frontend/src/views/user/StoreOrdersView.vue`
2. `PROJECT_PROGRESS.md`

### 验证结果

1. `cmd /c npx vue-tsc --noEmit`：通过，前端类型检查成功。

### 下一步任务清单

1. 刷新用户端订单列表，确认待支付订单操作列上下两行显示。
2. 点击“编辑”和“详情”，确认跳转仍然正常。

### 风险点

1. 本次只改用户端订单列表操作列样式；如线上仍显示横向排列，需要清理浏览器或静态资源缓存。
## 2026-06-29 更新：待支付订单编辑页回填修复

- 状态：已完成用户端待支付订单编辑页回填修复，并通过前端类型检查。
- 当前任务：修复从订单列表点击“编辑”进入创建订单页后，原订单信息没有加载回表单的问题。
- 根因分析：创建订单页在应用、地区、价格等基础数据尚未加载完成时就可能处理 `orderId`，后续又因为已记录当前编辑订单号而跳过真正回填；同时订单类型和应用相关监听器可能在回填后把地区配置切回默认草稿。
- 实现原则：只修复用户端待支付订单编辑回填链路；不修改接口、路由、数据库、依赖、订单状态和重新提交业务逻辑。

### 实施内容

1. 用户端创建订单页新增基础数据就绪标记，只有应用、地区和价格配置加载完成后才处理待支付订单 `orderId`。
2. 新增已回填订单号标记，避免“已进入编辑模式”和“已完成表单回填”混用导致跳过回填。
3. 订单不存在、不可编辑或路由没有 `orderId` 时，同步清理编辑状态和已回填状态。
4. 回填待支付订单时，暂停 `storeType`、`customerAppId`、`orderType` 相关监听器，避免回填值被默认地区或订单类型草稿覆盖。
5. 回填保护延迟到 Vue 下一轮更新后再解除，保证同一轮响应式监听不会误清空表单。

### 修改文件

1. `frontend/src/views/user/OrderCreateView.vue`
2. `PROJECT_PROGRESS.md`

### 验证结果

1. `cmd /c npx vue-tsc --noEmit`：通过，前端类型检查成功。
2. `rg` 检查确认创建订单页已包含 `baseDataReady`、`hydratedOrderId` 和回填期间监听器保护逻辑。

### 下一步任务清单

1. 在用户端订单列表点击待支付订单“编辑”，确认应用、订单类型、日期、地区和数量能自动回填。
2. 对下载量、星级评分、关键词安装、用户评论四类待支付订单分别做一次回填检查。
3. 修改回填后的数量并重新提交，确认余额足时进入“已支付待确认”，余额不足时仍保持“待支付”。

### 风险点

1. 本次只修复前端表单回填时序，不改变后端待支付订单数据；如果历史订单本身缺少明细，仍无法完整反推表单。
2. 线上更新后若浏览器缓存旧静态资源，可能仍看到编辑页空白，需要强制刷新或清理缓存。
## 2026-06-29 更新：待支付订单未修改禁止重新提交

- 状态：已完成用户端待支付订单编辑提交前的未修改拦截，并通过前端类型检查。
- 当前任务：待支付订单进入编辑页后，如果用户没有修改订单内容，不能直接再次提交。
- 实现原则：只在前端提交前做误操作拦截；不修改后端重新提交接口、订单状态、路由、数据库或依赖。

### 实施内容

1. 用户端创建订单页将普通订单提交参数构造抽成 `buildCustomerOrderPayload()`，避免新建提交和编辑对比使用两套逻辑。
2. 待支付订单回填完成后，记录一份原始订单 payload 快照。
3. 编辑模式提交前，将当前 payload 与原始快照比较；如果完全一致，提示“订单内容未修改，无需重新提交”，并中止接口调用。
4. 当路由没有有效 `orderId`、订单不可编辑或加载失败时，同步清理原始快照，避免状态串用。
5. 补充中文和英文提示文案。

### 修改文件

1. `frontend/src/views/user/OrderCreateView.vue`
2. `frontend/src/i18n/locales/zh-CN.ts`
3. `frontend/src/i18n/locales/en-US.ts`
4. `PROJECT_PROGRESS.md`

### 验证结果

1. `cmd /c npx vue-tsc --noEmit`：通过，前端类型检查成功。
2. `rg` 检查确认创建订单页已包含 `originalPayloadSnapshot`、`buildCustomerOrderPayload`、`serializeOrderPayload` 和未修改提示文案。

### 下一步任务清单

1. 打开待支付订单编辑页，不修改任何内容直接提交，确认提示“订单内容未修改，无需重新提交”，且不跳转订单列表。
2. 修改任意有效订单内容后提交，确认仍会调用重新提交逻辑。
3. 分别验证关键词安装、下载量、星级评分、用户评论四类待支付普通订单的未修改拦截。

### 风险点

1. 本次拦截以提交 payload 为准；如果用户改动后又改回原值，会被视为未修改并禁止提交。
2. 该逻辑会阻止“仅充值后不改订单内容直接重新提交”的操作；如后续希望支持单独支付待支付订单，建议增加独立“去支付/重新支付”动作，而不是复用编辑提交。
## 2026-06-29 更新：待支付订单编辑页语义区分

- 状态：已完成用户端待支付订单编辑模式的页面标题和按钮文案区分，并通过前端类型检查。
- 当前任务：修复从订单列表点击“编辑”后进入页面仍显示“创建订单”的体验问题。
- 实现原则：继续复用现有创建订单页和重新提交接口，只调整编辑模式展示语义；不新增路由、不修改后端接口、数据库或订单状态逻辑。

### 实施内容

1. 用户端顶部页面标题在 `/user/orders/create?orderId=...` 下显示为“编辑订单”。
2. 普通订单编辑模式下，右侧提交按钮从“提交订单”改为“提交修改”。
3. 新建订单、特殊订单提交需求等原有文案保持不变。
4. 补充中文和英文文案：`orderCreate.editTitle`、`orderCreate.submitChanges`。

### 修改文件

1. `frontend/src/layouts/UserLayout.vue`
2. `frontend/src/views/user/OrderCreateView.vue`
3. `frontend/src/i18n/locales/zh-CN.ts`
4. `frontend/src/i18n/locales/en-US.ts`
5. `PROJECT_PROGRESS.md`

### 验证结果

1. `cmd /c npx vue-tsc --noEmit`：通过，前端类型检查成功。
2. `rg` 检查确认编辑标题、提交修改按钮文案和中英文文案均已添加。

### 下一步任务清单

1. 从用户端订单列表点击待支付订单“编辑”，确认顶部标题显示“编辑订单”。
2. 确认右侧按钮显示“提交修改”，新建订单入口仍显示“创建订单 / 提交订单”。
3. 验证未修改直接提交仍提示“订单内容未修改，无需重新提交”。

### 风险点

1. 本次仍复用创建订单页承载编辑表单，URL 路径仍是 `/orders/create?orderId=...`；只是用户可见标题和按钮已区分编辑模式。
2. 如果后续需要更完整的产品语义，可单独新增 `/orders/:id/edit` 路由，但这属于路由变更，需要另行确认。
## 2026-06-29 更新：待支付订单编辑页增加返回列表

- 状态：已完成用户端待支付订单编辑页返回列表入口，并通过前端类型检查。
- 当前任务：编辑订单页需要提供返回列表按钮，避免用户进入编辑表单后只能通过侧边栏或浏览器返回。
- 实现原则：只在编辑模式显示返回入口；不新增路由、不修改后端接口、数据库或订单提交逻辑。

### 实施内容

1. 用户端创建订单页在编辑模式下显示“返回列表”按钮。
2. 返回列表按钮点击后，根据当前订单 `storeType` 返回对应的苹果、谷歌或 iPad 订单列表。
3. 新建订单模式不显示该按钮，保持原有创建流程简洁。
4. 补充中文和英文文案：`orderCreate.backToList`。

### 修改文件

1. `frontend/src/views/user/OrderCreateView.vue`
2. `frontend/src/i18n/locales/zh-CN.ts`
3. `frontend/src/i18n/locales/en-US.ts`
4. `PROJECT_PROGRESS.md`

### 验证结果

1. `cmd /c npx vue-tsc --noEmit`：通过，前端类型检查成功。
2. `rg` 检查确认 `backToOrderList`、`edit-toolbar`、`backToList` 和 `ArrowLeft` 已添加。

### 下一步任务清单

1. 从待支付订单点击“编辑”，确认表单上方显示“返回列表”按钮。
2. 点击“返回列表”，确认返回当前订单所属应用商店的订单列表。
3. 从推广服务进入新建订单，确认不显示“返回列表”按钮。

### 风险点

1. 返回目标按当前表单中的应用商店计算；如果用户在编辑页切换了应用商店，再点击返回会回到切换后的商店列表。
2. 本次只处理用户端待支付编辑页；管理员端创建页没有待支付编辑入口，本次不调整。
## 2026-06-29 更新：用户端订单列表筛选区布局优化

- 状态：已完成用户端订单列表筛选区布局优化，并通过前端类型检查。
- 当前任务：修复订单列表筛选区域控件和按钮混排混乱、状态下拉过长的问题。
- 实现原则：只调整用户端订单列表筛选区模板结构和 CSS；不修改查询字段、接口参数、表格数据、路由或后端逻辑。

### 实施内容

1. 将筛选区拆分为主筛选区、辅助筛选区和操作按钮区，避免按钮与筛选控件混在同一个自适应网格中。
2. 主筛选区保留内容筛选、应用、地区、任务类型、订单类型，按固定比例网格排列。
3. 辅助筛选区包含状态、订单时间、创建时间；状态下拉使用较短固定宽度，避免横向占用过多空间。
4. 操作按钮区单独右对齐，包含新建订单、搜索、清空、导出，并保持按钮组紧凑。
5. 保留窄屏响应式规则，较窄屏幕下筛选项和按钮自然纵向排列。

### 修改文件

1. `frontend/src/views/user/StoreOrdersView.vue`
2. `PROJECT_PROGRESS.md`

### 验证结果

1. `cmd /c npx vue-tsc --noEmit`：通过，前端类型检查成功。
2. `rg` 检查确认 `query-bottom-row`、`query-secondary-fields`、`status-item` 已添加，旧的 `query-grid-secondary` 已不再使用。

### 下一步任务清单

1. 刷新用户端苹果订单列表，确认筛选区两行排列清晰，按钮组不再挤入筛选项。
2. 检查状态下拉宽度是否合适，且完整状态文案仍可选择。
3. 分别在谷歌订单、iPad 订单页面确认同一筛选区布局正常。
4. 在 100%、125%、150% 浏览器缩放下检查按钮换行是否仍保持紧凑。

### 风险点

1. 本次只调整用户端订单列表筛选区；管理员端订单列表筛选区未同步调整，避免扩大当前任务范围。
2. 在极窄屏幕下按钮仍会换行，但应保持成组排列，不再与筛选控件混排。
## 2026-06-29 更新：用户端订单列表筛选区二次排版修复

- 状态：已完成用户端订单列表筛选区二次排版优化，并通过前端类型检查。
- 当前任务：继续修复筛选区排版不美观、页面变窄时按钮可能被控件覆盖的问题。
- 实现原则：只调整用户端订单列表筛选区 CSS；不修改查询字段、接口参数、表格数据、路由或后端逻辑。

### 实施内容

1. 将辅助筛选区和操作按钮区改为纵向分区，按钮独立占一行，避免被日期控件挤压或覆盖。
2. 主筛选区改为 `auto-fit` 自适应网格，减少固定列数在不同宽度下造成的挤压。
3. 筛选项标签统一使用固定标签列宽，控件列自适应，让标签和输入控件对齐更整齐。
4. 状态筛选项使用独立较短宽度，日期控件使用可换行的弹性宽度。
5. 增加 1100px 中等宽度断点，提前让按钮左对齐并独立换行，避免临界宽度下覆盖。

### 修改文件

1. `frontend/src/views/user/StoreOrdersView.vue`
2. `PROJECT_PROGRESS.md`

### 验证结果

1. `cmd /c npx vue-tsc --noEmit`：通过，前端类型检查成功。
2. `rg` 检查确认 `query-bottom-row`、`status-item`、固定标签列和 1100px 断点已添加。

### 下一步任务清单

1. 在用户端苹果订单列表检查筛选区整体排版，确认按钮不再被日期控件覆盖。
2. 缩小浏览器宽度，确认筛选项和按钮按区块换行，不出现重叠。
3. 检查谷歌订单、iPad 订单复用页面的筛选区表现。

### 风险点

1. 本次为用户端局部 CSS 调整，不影响查询逻辑。
2. 如果后续要求管理员端也保持同一视觉标准，需要单独同步管理员端订单列表筛选区。
## 2026-06-29 更新：用户端订单列表筛选区和表格遮挡修复

- 状态：已完成用户端订单列表筛选区重排和表格右侧遮挡修复，并通过前端类型检查。
- 当前任务：修复筛选区仍不美观，以及页面宽度变小时操作固定列覆盖创建时间列的问题。
- 实现原则：只调整用户端订单列表模板和 CSS；不修改查询字段、接口参数、表格数据、路由或后端逻辑。

### 实施内容

1. 将 8 个筛选控件统一放入同一个自适应网格，取消之前割裂的主筛选区/辅助筛选区结构。
2. 操作按钮独立放在筛选面板底部操作栏，避免与筛选控件混排。
3. 筛选项标签和控件采用统一栅格，提升标签、输入框、下拉框和日期控件的对齐一致性。
4. 取消用户端订单列表操作列的 `fixed="right"`，避免窄宽度下覆盖创建时间列。
5. 收敛订单号、应用、订单时间、任务类型、订单类型、状态、创建时间和操作列宽，降低横向溢出概率。
6. 订单时间和创建时间增加专用文本样式，保持表格行内容更稳定。

### 修改文件

1. `frontend/src/views/user/StoreOrdersView.vue`
2. `PROJECT_PROGRESS.md`

### 验证结果

1. `cmd /c npx vue-tsc --noEmit`：通过，前端类型检查成功。
2. `rg` 检查确认用户端订单列表已无 `fixed=`，旧的 `query-bottom-row`、`query-secondary-fields` 筛选分区类已不再存在。

### 下一步任务清单

1. 刷新用户端苹果订单列表，确认筛选区控件统一排列，按钮独立在底部。
2. 缩小浏览器宽度，确认右侧操作列不再覆盖创建时间列。
3. 检查谷歌订单、iPad 订单复用页面的筛选区和表格表现。

### 风险点

1. 取消固定操作列后，极窄宽度下需要横向滚动或自然压缩查看最右侧操作列，但不会再覆盖中间列内容。
2. 本次只调整用户端订单列表，管理员端未同步修改。
## 2026-06-29 更新：用户端订单列表筛选区紧凑网格修正

- 状态：已完成用户端订单列表筛选区紧凑网格修正，并通过前端类型检查。
- 当前任务：修复筛选区越改越散、状态和日期控件铺满整行导致视觉不美观的问题。
- 实现原则：只调整用户端订单列表筛选区 CSS；不修改查询字段、接口参数、表格数据、路由或后端逻辑。

### 实施内容

1. 筛选区从 `auto-fit` 自适应列改为明确的桌面端 4 列网格，8 个筛选项稳定显示为两行。
2. 中等宽度下按 3 列、2 列逐级降级，小屏下单列显示，避免控件互相挤压。
3. 筛选项标签列宽从 64px 收敛到 62px，控件列自适应，整体更紧凑。
4. 保留底部独立按钮栏，按钮不再混入筛选项网格。
5. 保留表格操作列取消固定的遮挡修复。

### 修改文件

1. `frontend/src/views/user/StoreOrdersView.vue`
2. `PROJECT_PROGRESS.md`

### 验证结果

1. `cmd /c npx vue-tsc --noEmit`：通过，前端类型检查成功。
2. `rg` 检查确认筛选区使用明确的 4/3/2/1 列规则，已无 `auto-fit`。

### 下一步任务清单

1. 刷新用户端苹果订单列表，确认筛选区为紧凑两行布局。
2. 缩小浏览器宽度，确认筛选区按 3 列、2 列、1 列自然降级。
3. 检查谷歌订单、iPad 订单复用页面表现。

### 风险点

1. 固定 4 列布局在较窄桌面会更早降级为 3 列或 2 列，这是为了避免铺满和遮挡。
2. 本次只调整用户端订单列表，管理员端未同步修改。

## 2026-06-29 更新：用户端与管理员端列表分页能力补齐

- 状态：已完成本次确认范围内的列表分页改造，并通过后端测试、前端类型检查和前端生产构建验证。
- 当前任务：为用户端和管理员端主要列表补充分页，避免订单、流水、应用、客户、审核等数据增长后一次性加载过多。
- 实现原则：保留现有路由和数据库结构；不安装依赖；不修改 `.env` 或秘钥配置；后端接口保持旧数组响应兼容，只有传入 `page/pageSize` 时返回分页对象。

### 实施内容

1. 新增通用前端 `PageResult<T>` 类型，订单、钱包流水、应用、客户、特殊订单审核接口补充分页请求方法。
2. 后端订单列表补充分页查询与总数统计，用户端和管理员端订单列表、管理员待确认订单列表接入真实后端分页。
3. 后端钱包流水补充分页查询与按类型、方向、订单类型、创建日期筛选，用户端收支明细、消费记录和管理员财务/充值记录接入真实后端分页。
4. 后端应用管理、客户管理、特殊订单审核补充分页查询和总数统计；对应用户端/管理员端页面接入分页。
5. 管理员账号列表、地区列表属于小规模配置/账号表，本次补充前端分页展示，不改变现有业务接口。
6. Dashboard 最近数据表和订单详情内部明细表保留不分页，因为它们不是独立完整列表页；钱包类型配置保留不分页，避免影响固定枚举配置批量编辑。
7. 为新增仓库分页方法提供默认实现，保证现有测试替身和旧调用兼容；生产 JDBC 实现仍走 SQL `LIMIT/OFFSET` 分页。

### 修改文件

1. `backend/src/main/java/com/youou/aso/common/api/PageResult.java`
2. `backend/src/main/java/com/youou/aso/modules/order/api/CustomerOrderController.java`
3. `backend/src/main/java/com/youou/aso/modules/order/api/AdminOrderController.java`
4. `backend/src/main/java/com/youou/aso/modules/order/api/CustomerSpecialOrderAuditController.java`
5. `backend/src/main/java/com/youou/aso/modules/order/api/AdminSpecialOrderAuditController.java`
6. `backend/src/main/java/com/youou/aso/modules/order/service/OrderService.java`
7. `backend/src/main/java/com/youou/aso/modules/order/service/SpecialOrderAuditService.java`
8. `backend/src/main/java/com/youou/aso/modules/order/repository/OrderRepository.java`
9. `backend/src/main/java/com/youou/aso/modules/order/repository/JdbcOrderRepository.java`
10. `backend/src/main/java/com/youou/aso/modules/order/repository/SpecialOrderAuditRepository.java`
11. `backend/src/main/java/com/youou/aso/modules/order/repository/JdbcSpecialOrderAuditRepository.java`
12. `backend/src/main/java/com/youou/aso/modules/wallet/api/CustomerWalletController.java`
13. `backend/src/main/java/com/youou/aso/modules/wallet/api/AdminWalletController.java`
14. `backend/src/main/java/com/youou/aso/modules/wallet/service/WalletQueryService.java`
15. `backend/src/main/java/com/youou/aso/modules/wallet/repository/WalletQueryRepository.java`
16. `backend/src/main/java/com/youou/aso/modules/wallet/repository/JdbcWalletQueryRepository.java`
17. `backend/src/main/java/com/youou/aso/modules/appmanagement/api/CustomerAppController.java`
18. `backend/src/main/java/com/youou/aso/modules/appmanagement/api/AdminCustomerAppController.java`
19. `backend/src/main/java/com/youou/aso/modules/appmanagement/service/CustomerAppService.java`
20. `backend/src/main/java/com/youou/aso/modules/appmanagement/repository/CustomerAppRepository.java`
21. `backend/src/main/java/com/youou/aso/modules/appmanagement/repository/JdbcCustomerAppRepository.java`
22. `backend/src/main/java/com/youou/aso/modules/account/api/AdminCustomerController.java`
23. `backend/src/main/java/com/youou/aso/modules/account/service/AdminCustomerService.java`
24. `backend/src/main/java/com/youou/aso/modules/account/repository/CustomerAccountRepository.java`
25. `backend/src/main/java/com/youou/aso/modules/account/repository/JdbcCustomerAccountRepository.java`
26. `frontend/src/api/http.ts`
27. `frontend/src/api/orders.ts`
28. `frontend/src/api/wallet.ts`
29. `frontend/src/api/applications.ts`
30. `frontend/src/api/customers.ts`
31. `frontend/src/api/specialOrderAudits.ts`
32. `frontend/src/views/user/StoreOrdersView.vue`
33. `frontend/src/views/admin/StoreOrdersView.vue`
34. `frontend/src/views/admin/PendingConfirmOrdersView.vue`
35. `frontend/src/views/user/TransactionsView.vue`
36. `frontend/src/views/user/ConsumptionRecordsView.vue`
37. `frontend/src/views/admin/FinanceManagementView.vue`
38. `frontend/src/views/user/ApplicationManagementView.vue`
39. `frontend/src/views/admin/ApplicationManagementView.vue`
40. `frontend/src/views/admin/CustomerManagementView.vue`
41. `frontend/src/views/user/SpecialOrdersView.vue`
42. `frontend/src/views/admin/AuditManagementView.vue`
43. `frontend/src/views/admin/AdminAccountsView.vue`
44. `frontend/src/views/admin/RegionView.vue`
45. `PROJECT_PROGRESS.md`

### 验证结果

1. `cmd /c mvn test`：通过，后端 98 个测试全部成功。
2. `cmd /c npx vue-tsc --noEmit`：通过，前端类型检查成功。
3. `cmd /c npm.cmd run build`：通过，前端生产构建成功。
4. 构建仍存在既有非阻断提示：VueUse PURE 注释提示、主 chunk 超过 500k、npm 新版本提示；本次未处理。

### 下一步任务清单

1. 部署后在用户端订单列表、特殊订单、收支明细、消费记录、应用管理逐页验证分页数据和筛选联动。
2. 部署后在管理员端订单列表、待确认订单、财务/充值记录、应用管理、用户管理、特殊订单审核、管理员账号、地区列表验证分页。
3. 如后续需要“全量客户统计”，为用户管理顶部统计单独新增聚合接口，避免用分页当前页数据冒充全量统计。
4. 如数据量继续增长，可进一步为管理员账号、地区等配置列表补充后端分页接口。

### 风险点

1. 订单、流水、应用、客户、特殊订单审核已经使用后端分页；管理员账号和地区列表本次为前端分页，适合当前小规模配置数据，但不是大数据方案。
2. 客户管理顶部统计沿用当前加载数据口径，分页后显示的是当前页统计；如业务要求全量统计，需要单独开发聚合接口。
3. 旧接口未传 `page/pageSize` 时仍返回数组，传分页参数时返回分页对象；后续新增调用方需要明确使用对应 API 方法。

## 2026-06-29 更新：分页组件跟随语言环境切换

- 状态：已完成 Element Plus 全局语言配置，分页组件文案会随当前中英文语言环境切换。
- 当前任务：修复新增分页组件仍显示默认语言的问题。
- 实现原则：只改前端根组件全局配置，不逐个页面硬编码分页文案；不修改后端、路由、数据库、依赖和秘钥配置。

### 实施内容

1. 在 `App.vue` 外层增加 `el-config-provider`，统一承载 Element Plus 组件语言配置。
2. 使用现有 `vue-i18n` 的 `locale` 作为唯一语言来源。
3. 将 `zh-CN` 映射到 Element Plus `zh-cn` 语言包，将 `en-US` 映射到 Element Plus `en` 语言包。
4. 所有 `el-pagination` 及其他 Element Plus 内置文案组件自动跟随当前语言切换。

### 修改文件

1. `frontend/src/App.vue`
2. `PROJECT_PROGRESS.md`

### 验证结果

1. `cmd /c npx vue-tsc --noEmit`：通过，前端类型检查成功。
2. `cmd /c npm.cmd run build`：通过，前端生产构建成功。
3. 构建仍存在既有非阻断提示：VueUse PURE 注释提示、主 chunk 超过 500k、npm 新版本提示；本次未处理。

### 下一步任务清单

1. 在用户端任一带分页列表页面切换中文/英文，确认分页总数、每页条数、跳页等内置文案同步切换。
2. 在管理员端任一带分页列表页面切换中文/英文，确认分页内置文案同步切换。
3. 如后续发现其他 Element Plus 组件仍有未本地化文案，继续复用当前全局 `ElConfigProvider` 方案处理。

### 风险点

1. 本次为全局 Element Plus 语言配置，影响范围覆盖所有 Element Plus 内置组件文案；业务自定义文案仍由现有 `vue-i18n` 控制。
2. 仅支持当前项目已有的 `zh-CN` 和 `en-US` 两种语言；新增语言时需要补充 Element Plus locale 映射。

## 2026-06-29 更新：收支明细方向文案优化

- 状态：已完成收支方向文案优化，并通过前端类型检查和生产构建验证。
- 当前任务：优化钱包流水中“方向”字段语义，让用户更容易理解收入/支出含义。
- 实现原则：仅修改前端 i18n 文案，不改接口字段、数据逻辑、后端、路由、数据库、依赖和秘钥配置。

### 实施内容

1. 中文 `wallet.direction` 从“方向”调整为“收支方向”。
2. 中文 `CREDIT / DEBIT` 保持“收入 / 支出”，符合财务流水常见表达。
3. 英文 `wallet.direction` 从 `Direction` 调整为 `Income/Expense`。
4. 英文 `CREDIT / DEBIT` 从 `Credit / Debit` 调整为 `Income / Expense`，降低会计术语理解成本。
5. 因该文案被用户端收支明细、首页流水摘要、管理员财务流水等复用，相关页面统一生效。

### 修改文件

1. `frontend/src/i18n/locales/zh-CN.ts`
2. `frontend/src/i18n/locales/en-US.ts`
3. `PROJECT_PROGRESS.md`

### 验证结果

1. `cmd /c npx vue-tsc --noEmit`：通过，前端类型检查成功。
2. `cmd /c npm.cmd run build`：通过，前端生产构建成功。
3. 构建仍存在既有非阻断提示：VueUse PURE 注释提示、主 chunk 超过 500k、npm 新版本提示；本次未处理。

### 下一步任务清单

1. 在用户端收支明细页面确认筛选项和表格列显示为“收支方向 / 收入 / 支出”。
2. 在管理员端财务流水和首页流水摘要确认同一字段文案一致。
3. 如后续需要进一步细分资金来源，可在流水类型字段中继续使用“订单扣款、订单退款、充值”等业务类型。

### 风险点

1. 本次仅为展示文案调整，不影响 `CREDIT / DEBIT` 枚举值和后端审计语义。
2. 英文从会计术语 `Credit / Debit` 改为普通用户更容易理解的 `Income / Expense`，如后续面向专业财务人员，可再评估是否恢复更专业表达。
## 2026-06-29 更新：执行中订单暂停、恢复执行与批量暂停

- 状态：已按确认范围完成管理员端执行中订单暂停、暂停订单恢复执行、执行中订单批量暂停，并通过后端测试、前端类型检查和生产构建验证。
- 当前任务：参考老系统执行中订单暂停能力，在新项目中补回必要状态流转和管理员操作入口，但不完全复刻老系统，不改数据库结构和大路由架构。
- 实现原则：只新增订单状态流转、接口、前端按钮和展示文案；不修改 `.env`、秘钥、依赖、数据库结构；不改变订单金额、扣款、退款和自动完成的既有规则。

### 实施内容

1. 后端订单状态新增 `PAUSED`，表示订单已暂停。
2. 后端新增单个暂停、单个恢复执行、批量暂停服务方法，分别限制 `EXECUTING -> PAUSED`、`PAUSED -> EXECUTING`，非法状态统一拒绝。
3. 管理员订单接口新增 `/api/admin/orders/{id}/pause`、`/api/admin/orders/{id}/resume`、`/api/admin/orders/batch-pause`。
4. 管理员订单列表在执行中订单显示“暂停”，暂停订单显示“恢复执行”，执行中列表支持勾选后批量暂停。
5. 前端订单 API、订单状态类型、中英文文案补充暂停相关能力。
6. 用户端/管理员端订单列表、订单详情和首页摘要补充 `PAUSED` 状态展示兜底，避免暂停后显示缺省文案。
7. 后端服务层补充暂停、恢复执行、批量暂停及非法状态拒绝测试。

### 修改文件

1. `backend/src/main/java/com/youou/aso/modules/order/domain/OrderStatus.java`
2. `backend/src/main/java/com/youou/aso/modules/order/service/OrderService.java`
3. `backend/src/main/java/com/youou/aso/modules/order/api/AdminOrderController.java`
4. `backend/src/test/java/com/youou/aso/modules/order/service/OrderServiceTest.java`
5. `backend/src/test/java/com/youou/aso/modules/order/api/AdminOrderControllerTest.java`
6. `frontend/src/api/orders.ts`
7. `frontend/src/views/admin/StoreOrdersView.vue`
8. `frontend/src/views/user/StoreOrdersView.vue`
9. `frontend/src/views/user/OrderDetailView.vue`
10. `frontend/src/views/admin/OrderDetailView.vue`
11. `frontend/src/views/user/DashboardView.vue`
12. `frontend/src/views/admin/DashboardView.vue`
13. `frontend/src/i18n/locales/zh-CN.ts`
14. `frontend/src/i18n/locales/en-US.ts`
15. `PROJECT_PROGRESS.md`

### 验证结果

1. `cmd /c mvn test`：通过，后端 107 个测试全部成功。
2. `cmd /c npx vue-tsc --noEmit`：通过，前端类型检查成功。
3. `cmd /c npm.cmd run build`：通过，前端生产构建成功。
4. 构建仍存在既有非阻断提示：VueUse PURE 注释提示、主 chunk 超过 500k、npm 新版本提示；本次未处理。

### 下一步任务清单

1. 部署后在管理员端“执行中订单”列表验证单个暂停和批量暂停按钮显示、确认弹窗和列表刷新。
2. 在管理员端通过状态筛选查看“已暂停”订单，验证恢复执行按钮和恢复后状态流转。
3. 在用户端订单列表和订单详情验证暂停订单状态标签显示正确。
4. 如后续需要暂停时间累计或恢复后顺延预计完成时间，需要单独确认业务规则后再设计。

### 风险点

1. 本次暂停/恢复只做状态切换，不记录暂停人、暂停时间和恢复时间，因为当前未新增数据库字段；如需要审计轨迹，需要后续确认数据库变更。
2. 恢复执行后沿用原预计完成时间；如果暂停时间较长且预计完成时间已过，恢复后可能在下一次自动完成检查中变为已完成，这一点需要结合真实业务再决定是否顺延完成时间。
3. 批量暂停采用整体校验，选中订单里只要存在非执行中订单就拒绝，避免部分暂停造成状态不一致。
## 2026-06-29 更新：关键词安装模板下载与批量导入补齐

- 状态：已完成用户端和管理员端创建订单页“下载模板 / 批量导入”按钮的实际功能补齐，并按用户要求保留 Excel 依赖。
- 当前任务：关键词安装订单的地区关键词区域原按钮只有展示入口，没有模板生成和导入处理；本次补回可用的 Excel 模板下载与批量导入能力。
- 实现原则：只处理当前创建订单页的导入导出能力；不修改路由、数据库、后端接口、密钥配置和订单提交结构；Excel 解析仅在点击下载/导入时动态加载，降低普通下单页面首屏负担。

### 实施内容

1. 为用户端创建订单页接入关键词导入模板下载，生成 `.xlsx` 文件，包含 `regionCode`、`keyword`、`dailyQuantity` 三列和示例数据。
2. 为管理员端创建订单页同步接入同样的 `.xlsx` 模板下载能力。
3. 为用户端和管理员端批量导入按钮接入文件选择与解析，支持 `.xlsx` 导入，并保留 `.csv` 兼容导入。
4. 导入时按当前应用商店支持地区校验 `regionCode`，校验关键词必填、每日数量为正整数，并按地区和关键词合并数量。
5. 导入成功后直接填充地区关键词表格，并同步更新关键词安装类型的地区配置草稿，避免切换订单类型后数据丢失。
6. 补充中英文导入成功、文件类型错误、空文件、行格式错误、地区不支持、数量错误等提示文案。
7. 新增 `exceljs@^4.4.0` 前端依赖；未使用 `xlsx`，因为其当前可安装版本存在高危且无可用修复。
8. 将 `exceljs` 改为动态加载，只有点击模板下载或导入 Excel 文件时才加载解析库。

### 修改文件

1. `frontend/package.json`
2. `frontend/package-lock.json`
3. `frontend/src/views/user/OrderCreateView.vue`
4. `frontend/src/views/admin/OrderCreateView.vue`
5. `frontend/src/i18n/locales/zh-CN.ts`
6. `frontend/src/i18n/locales/en-US.ts`
7. `PROJECT_PROGRESS.md`

### 验证结果

1. `cmd /c npx vue-tsc --noEmit`：通过，前端类型检查成功。
2. `cmd /c npm.cmd run build`：通过，前端生产构建成功。
3. `cmd /c npm.cmd audit --omit=dev`：发现 `exceljs -> uuid` 带来的 2 个 moderate 风险，当前 npm 无可用自动修复；已记录为风险点。
4. 构建仍存在既有非阻断提示：VueUse PURE 注释提示、主 chunk 超过 500k、`exceljs` 懒加载 chunk 超过 500k、npm 新版本提示；本次未调整构建架构。

### 下一步任务清单

1. 在用户端关键词安装下单页实际下载模板，确认文件可被 Excel 打开。
2. 使用模板填写多个地区、多条关键词后导入，确认表格按地区分组并合并重复关键词数量。
3. 在管理员端创建订单页执行同样的模板下载和导入验证。
4. 如后续要求支持老式 `.xls` 二进制文件，需要单独评估解析库和安全风险。

### 风险点

1. `exceljs@4.4.0` 的间接依赖 `uuid@8.3.2` 存在 npm audit moderate 风险，当前无可用自动修复；已按用户要求保留 Excel 依赖。
2. `exceljs` 浏览器包体积较大，已通过动态加载降低首屏影响，但点击模板下载或导入时仍需加载约 940k 的懒加载 chunk。
3. 当前导入支持 `.xlsx` 和 `.csv`，不支持旧版 `.xls` 二进制格式，避免引入更高风险或不维护的解析方案。
## 2026-06-29 更新：待确认订单批量确认补齐

- 状态：已完成管理员端待确认订单的批量确认能力，并通过后端测试、前端类型检查和生产构建验证。
- 当前任务：待确认订单只有单个确认按钮，缺少批量确认入口；本次补齐批量确认接口、前端按钮和选择交互。
- 实现原则：复用现有订单状态流转和单个确认规则；不修改数据库结构、路由结构、密钥配置和订单金额规则；批量确认采用整体校验，避免部分确认造成状态不一致。

### 实施内容

1. 后端 `OrderService` 新增批量确认方法，要求订单 ID 列表非空，并整体校验所有订单都处于 `PENDING_CONFIRM`。
2. 后端新增管理员接口 `POST /api/admin/orders/batch-confirm`，返回批量确认后的订单列表。
3. 批量确认统一设置 `PENDING_EXECUTION`、确认管理员 ID 和同一个确认时间。
4. 管理员“待确认订单”页面新增表格勾选列、批量确认按钮、已选择数量提示和确认弹窗。
5. 管理员按店铺拆分的订单列表在状态为 `PENDING_CONFIRM` 时显示批量确认入口，并补回单行确认按钮。
6. 前端订单 API 新增 `batchConfirmAdminOrders`。
7. 补充中英文文案：批量确认、未选择待确认订单、批量确认成功、批量确认弹窗提示。
8. 后端补充服务层和控制器层批量确认测试。

### 修改文件

1. `backend/src/main/java/com/youou/aso/modules/order/service/OrderService.java`
2. `backend/src/main/java/com/youou/aso/modules/order/api/AdminOrderController.java`
3. `backend/src/test/java/com/youou/aso/modules/order/service/OrderServiceTest.java`
4. `backend/src/test/java/com/youou/aso/modules/order/api/AdminOrderControllerTest.java`
5. `frontend/src/api/orders.ts`
6. `frontend/src/views/admin/PendingConfirmOrdersView.vue`
7. `frontend/src/views/admin/StoreOrdersView.vue`
8. `frontend/src/i18n/locales/zh-CN.ts`
9. `frontend/src/i18n/locales/en-US.ts`
10. `PROJECT_PROGRESS.md`

### 验证结果

1. `cmd /c mvn test`：通过，后端 110 个测试全部成功。
2. `cmd /c npx vue-tsc --noEmit`：通过，前端类型检查成功。
3. `cmd /c npm.cmd run build`：通过，前端生产构建成功。
4. 构建仍存在既有非阻断提示：VueUse PURE 注释提示、主 chunk 超过 500k、`exceljs` 懒加载 chunk 超过 500k、npm 新版本提示；本次未调整构建架构。

### 下一步任务清单

1. 部署后在管理员“待确认订单”页面勾选多个待确认订单，验证批量确认弹窗、成功提示和列表刷新。
2. 在苹果/谷歌/iPad 订单列表筛选“已支付待确认”状态，验证批量确认入口和单行确认按钮显示正确。
3. 验证混合状态订单不会被批量确认接口部分处理。
4. 如后续需要记录批量操作审计日志，需要单独确认审计字段或日志方案。

### 风险点

1. 批量确认采用整体校验，列表中只要包含非 `PENDING_CONFIRM` 订单就会拒绝，避免部分成功但也意味着用户需要重新选择合法订单。
2. 本次不新增确认备注、批次号或审计表；当前仍沿用订单表已有确认管理员和确认时间字段。
3. 通用订单列表只有在当前状态筛选为 `PENDING_CONFIRM` 时显示批量确认选择列，避免所有订单页面默认出现批量操作造成误操作。
## 2026-06-29 更新：管理员订单详情客户信息展示优化

- 状态：已按用户确认范围完成管理员订单详情“客户”字段优化，改为优先展示用户名，并在下方展示邮箱；后端管理员订单查询/详情返回补充客户用户名与邮箱。
- 当前任务：解决订单详情中“客户 用户 4”语义不清的问题，让管理员能直接看到真实用户名和邮箱。
- 实现原则：只做订单展示所需的小范围补充；不修改 `.env`、秘钥、路由、数据库结构和订单业务状态；用户端订单接口不主动挂载客户身份信息，避免不必要的数据暴露。

### 实施内容

1. `AsoOrder` 增加非持久化展示字段 `customerUsername`、`customerEmail`，用于接口结果承载客户身份信息。
2. `OrderResult` 增加 `customerUsername`、`customerEmail` 返回字段，保持接口加法兼容。
3. `CustomerAccountRepository` 增加按 ID 批量查询默认方法，JDBC 实现使用 `WHERE id IN (...)` 批量查询，避免管理员订单列表后续出现 N+1 查询。
4. `OrderService` 仅在管理员订单列表、管理员分页列表、管理员订单详情中挂载客户用户名和邮箱。
5. 管理员订单详情页将客户信息改为两行展示：第一行用户名，第二行邮箱；缺失用户名时继续回退到原有“用户 {id}”。
6. 后端服务层测试补充管理员订单详情返回客户用户名和邮箱的断言。

### 修改文件

1. `backend/src/main/java/com/youou/aso/modules/order/domain/AsoOrder.java`
2. `backend/src/main/java/com/youou/aso/modules/order/dto/OrderResult.java`
3. `backend/src/main/java/com/youou/aso/modules/account/repository/CustomerAccountRepository.java`
4. `backend/src/main/java/com/youou/aso/modules/account/repository/JdbcCustomerAccountRepository.java`
5. `backend/src/main/java/com/youou/aso/modules/order/service/OrderService.java`
6. `backend/src/test/java/com/youou/aso/modules/order/service/OrderServiceTest.java`
7. `frontend/src/api/orders.ts`
8. `frontend/src/views/admin/OrderDetailView.vue`
9. `PROJECT_PROGRESS.md`

### 验证结果

1. `cmd /c mvn test`：通过，后端 110 个测试全部成功。
2. `cmd /c npx vue-tsc --noEmit`：通过，前端类型检查成功。
3. `cmd /c npm.cmd run build`：通过，前端生产构建成功。
4. 构建仍存在既有非阻断提示：VueUse PURE 注释提示、主 chunk 超过 500k、`exceljs` 懒加载 chunk 超过 500k、npm 新版本提示；本次未调整构建架构。

### 下一步任务清单

1. 部署后在管理员订单详情页检查“客户”字段是否显示用户名和邮箱。
2. 在管理员订单列表分页场景检查接口响应中是否包含 `customerUsername`、`customerEmail`。
3. 如后续需要在订单列表也直接展示客户信息，再单独确认列表列宽和筛选需求后实现。

### 风险点

1. 本次返回字段为加法变更，前端旧页面不会受影响；但第三方调用方如严格校验响应字段，需要兼容新增字段。
2. 用户端订单查询未主动挂载客户用户名和邮箱，避免扩大身份信息暴露范围；如果后续用户端需要显示账号信息，应单独评估。
3. 管理员列表批量挂载客户信息依赖客户账号表数据存在；若历史订单的 `customer_id` 已不存在，会继续回退显示用户 ID。
## 2026-06-29 更新：订单详情费用信息卡片展示优化

- 状态：已完成用户端和管理员端订单详情页费用信息区域的展示优化，并通过前端类型检查和生产构建。
- 当前任务：优化订单详情中“费用信息”卡片层级不清、总金额与数量/单价混排导致观感不合理的问题。
- 实现原则：仅调整前端展示结构和局部样式；不修改金额计算逻辑、接口字段、后端服务、路由、数据库、依赖和秘钥配置。

### 实施内容

1. 用户端订单详情页费用卡片改为“总金额”主展示。
2. 管理员端订单详情页费用卡片同步改为“总金额”主展示。
3. 数量和单价改为下方两个紧凑指标块，作为辅助结算信息展示。
4. 保留原有单价展示逻辑：单一单价显示具体金额，多单价订单继续显示“多单价”。
5. 调整费用卡片局部 CSS，使金额、数量、单价层级更清晰，并避免长单价格文本撑乱布局。

### 修改文件

1. `frontend/src/views/user/OrderDetailView.vue`
2. `frontend/src/views/admin/OrderDetailView.vue`
3. `PROJECT_PROGRESS.md`

### 验证结果

1. `cmd /c npx vue-tsc --noEmit`：通过，前端类型检查成功。
2. `cmd /c npm.cmd run build`：通过，前端生产构建成功。
3. 构建仍存在既有非阻断提示：VueUse PURE 注释提示、主 chunk 超过 500k、`exceljs` 懒加载 chunk 超过 500k、npm 新版本提示；本次未调整构建架构。

### 下一步任务清单

1. 部署后分别检查用户端和管理员端订单详情页费用卡片展示效果。
2. 使用单一单价订单和多单价订单分别验证单价区域显示是否符合预期。
3. 如后续希望费用区域展示更详细的结算公式，可基于订单明细数据单独确认字段和文案后再实现。

### 风险点

1. 本次只调整展示层，不改变订单金额、数量、单价来源；若历史订单数据本身异常，展示仍会按接口返回值呈现。
2. 多单价订单仍显示“多单价”，具体单价继续通过下方订单明细表查看，避免在摘要卡片堆叠过多信息。
## 2026-06-29 更新：订单详情费用卡片补充天数展示

- 状态：已完成用户端和管理员端订单详情费用卡片补充“天数”指标，并通过前端类型检查和生产构建。
- 当前任务：处理订单详情费用区域只展示金额、数量、单价，结算信息中看不到天数，容易误解为未按天数计费的问题。
- 实现原则：本次只补充详情页展示信息，不修改已保存订单金额、不修改扣款流水、不修改后端计费逻辑、路由、数据库、依赖和秘钥配置。

### 实施内容

1. 用户端订单详情费用卡片新增“天数”指标。
2. 管理员端订单详情费用卡片同步新增“天数”指标。
3. 费用卡片指标区从固定两列改为自适应网格，避免天数、数量、单价在窄宽度下拥挤或溢出。
4. 保持总金额继续使用接口返回的订单已保存金额，避免前端展示金额与真实扣款/流水不一致。

### 修改文件

1. `frontend/src/views/user/OrderDetailView.vue`
2. `frontend/src/views/admin/OrderDetailView.vue`
3. `PROJECT_PROGRESS.md`

### 验证结果

1. `cmd /c npx vue-tsc --noEmit`：通过，前端类型检查成功。
2. `cmd /c npm.cmd run build`：通过，前端生产构建成功。
3. 构建仍存在既有非阻断提示：VueUse PURE 注释提示、主 chunk 超过 500k、`exceljs` 懒加载 chunk 超过 500k、npm 新版本提示；本次未调整构建架构。

### 下一步任务清单

1. 部署后检查用户端和管理员端订单详情费用卡片中是否同时显示金额、天数、数量、单价。
2. 使用新建下载量/星级评分订单验证新订单金额是否按天数计费。
3. 如需修正历史订单中已经保存的错误金额，需要单独确认数据库与财务流水修复方案后再处理。

### 风险点

1. 截图中的历史订单看起来保存金额本身未乘天数；本次不在前端重算覆盖总金额，避免与实际扣款记录不一致。
2. 历史错误订单若要修正，可能涉及订单金额、钱包余额、消费流水和审计记录，需要单独制定安全的数据修复流程。
## 2026-06-29 更新：订单列表筛选区布局收敛

- 状态：已完成用户端订单列表、管理员端订单列表、管理员待确认订单列表筛选区布局收敛，并通过前端类型检查和生产构建。
- 当前任务：修复订单列表筛选区域控件排布散乱、按钮混在字段行中、页面变窄时按钮和控件容易拥挤的问题。
- 实现原则：只调整前端模板结构和局部 CSS；不修改筛选参数、查询接口、分页逻辑、路由、数据库、依赖和秘钥配置。

### 实施内容

1. 管理员订单列表将操作按钮从筛选字段网格中移出，改为独立工具条。
2. 用户端订单列表筛选字段改为自适应网格，统一控件宽度和标签布局。
3. 管理员待确认订单列表从旧的横向 `query-row` 布局改为自适应 `query-grid`，并将按钮独立为工具条。
4. 三处筛选面板统一卡片边框、阴影、内边距、分隔线和按钮换行规则。
5. 日期范围、状态、应用、地区等控件统一使用容器宽度，避免状态控件过长或窄屏溢出。
6. 小屏下筛选字段自动单列展示，按钮按可换行规则排布，避免互相覆盖。

### 修改文件

1. `frontend/src/views/user/StoreOrdersView.vue`
2. `frontend/src/views/admin/StoreOrdersView.vue`
3. `frontend/src/views/admin/PendingConfirmOrdersView.vue`
4. `PROJECT_PROGRESS.md`

### 验证结果

1. `cmd /c npx vue-tsc --noEmit`：通过，前端类型检查成功。
2. `cmd /c npm.cmd run build`：通过，前端生产构建成功。
3. 构建仍存在既有非阻断提示：VueUse PURE 注释提示、主 chunk 超过 500k、`exceljs` 懒加载 chunk 超过 500k、npm 新版本提示；本次未调整构建架构。

### 下一步任务清单

1. 部署后检查用户端苹果/谷歌/iPad订单列表筛选区在桌面和窄屏下的排布。
2. 部署后检查管理员端苹果/谷歌/iPad订单列表筛选区按钮是否独立成行且不覆盖表格。
3. 部署后检查管理员待确认订单列表批量确认、搜索、清空、刷新、导出按钮是否正常显示和响应。

### 风险点

1. 本次仅调整布局和样式，不改变筛选请求参数；若某些屏幕宽度下仍希望固定每行字段数量，需要继续按实际分辨率微调网格最小宽度。
2. 管理员待确认订单列表同步增加了筛选面板卡片样式，视觉上会比旧页面更接近普通订单列表。
## 2026-06-29 更新：订单列表日期筛选靠左紧凑化

- 状态：已完成用户端订单列表、管理员端订单列表、管理员待确认订单列表日期筛选控件布局微调，并通过前端类型检查和生产构建。
- 当前任务：解释并修复“订单时间/创建时间”两个日期控件被放到最右侧、标签离控件过远、控件过度拉伸的问题。
- 实现原则：仅调整前端列表筛选区局部样式，不修改筛选字段、请求参数、接口、分页、路由、数据库、依赖和秘钥配置。

### 实施内容

1. 管理员普通订单列表的第二行筛选区从均分网格改为靠左换行排列，使“状态、订单时间、创建时间”按内容自然从左向右排列。
2. 用户端订单列表和管理员待确认订单列表的日期筛选项设置合理宽度，避免日期范围控件占满整列后显得被推到右侧。
3. 三个列表页的日期筛选项在窄屏下恢复为 100% 宽度，保证移动端和窄屏不会溢出。
4. 待确认订单列表同步收紧标签与控件间距，并取消标签固定最小宽度，保持与普通订单列表一致。

### 修改文件

1. `frontend/src/views/user/StoreOrdersView.vue`
2. `frontend/src/views/admin/StoreOrdersView.vue`
3. `frontend/src/views/admin/PendingConfirmOrdersView.vue`
4. `PROJECT_PROGRESS.md`

### 验证结果

1. `cmd /c npx vue-tsc --noEmit`：通过，前端类型检查成功。
2. `cmd /c npm.cmd run build`：通过，前端生产构建成功。
3. 构建仍存在既有非阻断提示：VueUse PURE 注释提示、主 chunk 超过 500k、`exceljs` 懒加载 chunk 超过 500k、npm 新版本提示；本次未调整构建架构。

### 下一步任务清单

1. 部署后在用户端和管理员端订单列表检查筛选区日期控件是否靠左且不再被拉到最右侧。
2. 在 1366px、笔记本窄屏和移动宽度下检查筛选标签与控件间距、按钮换行、日期控件宽度是否符合预期。
3. 如后续仍需更精细的视觉统一，可再按字段类型为应用、地区、任务类型等设置独立宽度规则。

### 风险点

1. 本次为局部 CSS 调整，不改变业务查询逻辑；如果某些语言环境下日期占位文案更长，日期控件宽度可能仍需继续微调。
2. 管理员普通订单列表第二行改为 flex 靠左排列，视觉会比此前均分网格更紧凑；若后续新增更多筛选项，需要继续观察换行后的排列密度。
## 2026-06-29 更新：管理员订单列表筛选区对齐用户端样式

- 状态：已完成管理员端普通订单列表筛选区与用户端订单列表筛选区的布局对齐，并通过前端类型检查和生产构建。
- 当前任务：修复管理员端订单列表筛选区与用户端样式不一致的问题，避免管理员端单独使用第二行筛选容器导致观感不同。
- 实现原则：仅调整前端模板结构和局部 CSS，不修改筛选字段、请求参数、接口、分页、路由、数据库、依赖和秘钥配置。

### 实施内容

1. 管理员普通订单列表移除独立的 `query-grid-secondary` 筛选容器。
2. 将管理员端“状态、订单时间、创建时间”放回与用户端一致的同一个 `query-grid` 中。
3. 清理管理员端 `query-grid-secondary` 遗留样式和响应式规则。
4. 管理员端按钮区对齐用户端样式，统一 `width`、`min-width`、顶部间距和分割线间距。
5. 保留窄屏下状态和日期筛选项 100% 宽度规则，避免小屏溢出。

### 修改文件

1. `frontend/src/views/admin/StoreOrdersView.vue`
2. `PROJECT_PROGRESS.md`

### 验证结果

1. `cmd /c npx vue-tsc --noEmit`：通过，前端类型检查成功。
2. `cmd /c npm.cmd run build`：通过，前端生产构建成功。
3. 构建仍存在既有非阻断提示：VueUse PURE 注释提示、主 chunk 超过 500k、`exceljs` 懒加载 chunk 超过 500k、npm 新版本提示；本次未调整构建架构。

### 下一步任务清单

1. 部署后对比用户端订单列表和管理员端订单列表筛选区，确认字段排列、按钮区间距和换行规则一致。
2. 在 1366px、笔记本窄屏和移动宽度下检查管理员端筛选区是否仍保持可用且不遮挡表格。
3. 如果待确认订单列表也需要完全复用普通订单列表的视觉节奏，可再单独确认是否继续统一。

### 风险点

1. 本次只对齐管理员普通订单列表与用户端普通订单列表；管理员待确认订单列表因存在批量确认等操作，仍保留自身页面结构。
2. 日期控件宽度仍沿用当前列表样式规则，若后续国际化文案变长，可能需要继续按语言环境微调。
## 2026-06-29 更新：管理员订单列表用户筛选与用户列

- 状态：已完成管理员端订单列表筛选区结构修复、用户筛选、列表用户列和后端按用户过滤能力，并通过后端测试、前端类型检查和生产构建。
- 当前任务：修复管理员订单列表筛选区样式仍不正确的问题，并在管理员端增加用户筛选和订单列表用户字段。
- 实现原则：保持现有路由、数据库结构和模块边界不变；仅做订单查询参数扩展、列表展示和局部样式修复；不修改 `.env`、秘钥和依赖。

### 实施内容

1. 修复管理员端 `StoreOrdersView` 中状态、订单时间、创建时间控件脱离 `query-grid` 的模板结构问题，使筛选项重新回到统一网格内。
2. 管理员订单列表新增“用户”筛选下拉，复用已有管理员客户列表接口，选项显示用户名和邮箱。
3. 管理员订单列表新增“用户”列，显示用户名和邮箱；缺失用户名时回退显示用户 ID。
4. 前端订单查询参数新增 `customerId`，管理员订单列表搜索、清空、分页加载均带入该筛选条件。
5. 后端 `OrderQuery` 增加 `customerId` 字段，管理员订单查询接口接收该参数，JDBC 查询按 `customer_id` 过滤。
6. 补充订单服务测试，验证管理员订单列表可按用户过滤。
7. 中英文国际化补充“用户 / 全部用户”文案。

### 修改文件

1. `backend/src/main/java/com/youou/aso/modules/order/dto/OrderQuery.java`
2. `backend/src/main/java/com/youou/aso/modules/order/api/AdminOrderController.java`
3. `backend/src/main/java/com/youou/aso/modules/order/api/CustomerOrderController.java`
4. `backend/src/main/java/com/youou/aso/modules/order/repository/JdbcOrderRepository.java`
5. `backend/src/test/java/com/youou/aso/modules/order/service/OrderServiceTest.java`
6. `frontend/src/api/orders.ts`
7. `frontend/src/views/admin/StoreOrdersView.vue`
8. `frontend/src/i18n/locales/zh-CN.ts`
9. `frontend/src/i18n/locales/en-US.ts`
10. `PROJECT_PROGRESS.md`

### 验证结果

1. `cmd /c mvn test`：通过，后端 111 个测试全部成功。
2. `cmd /c npx vue-tsc --noEmit`：通过，前端类型检查成功。
3. `cmd /c npm.cmd run build`：首次与并行检查命令同时执行时出现一次 Vite/Rollup `index.html` 路径发射异常；单独重跑后通过，前端生产构建成功。
4. 构建仍存在既有非阻断提示：VueUse PURE 注释提示、主 chunk 超过 500k、`exceljs` 懒加载 chunk 超过 500k、npm 新版本提示；本次未调整构建架构。

### 下一步任务清单

1. 部署后在管理员端苹果/谷歌/iPad订单列表检查筛选区是否恢复为统一网格布局。
2. 使用不同用户筛选订单，确认分页总数、列表数据和用户列显示一致。
3. 检查执行中、待执行、已完成等状态菜单下用户筛选与状态路由筛选是否能同时生效。
4. 如后续需要在导出文件中也加入用户列，可单独确认导出字段方案后实现。

### 风险点

1. 管理员用户筛选当前加载已有客户列表接口；当用户数量非常大时，后续可能需要改为远程搜索分页选择，但本次不改接口结构。
2. 后端新增 `customerId` 查询参数为加法变更，旧调用不传该参数不受影响；用户端仍由 `customerId` 权限上下文隔离，不依赖前端传参。
## 2026-06-29 更新：管理员订单列表用户列显示兜底修复

- 状态：已完成管理员端订单列表用户列显示兜底修复，并通过后端测试、前端类型检查和生产构建。
- 当前任务：修复管理员订单列表中用户字段全部显示用户 ID，未显示用户名和邮箱的问题。
- 实现原则：不修改数据库结构、路由、秘钥和依赖；仅增强前端显示兜底，并补充后端服务层测试确保客户身份挂载逻辑不退化。

### 实施内容

1. 管理员订单列表新增 `customerMap`，基于已加载的客户列表按 `customerId` 映射客户信息。
2. 用户列显示优先级调整为：订单接口返回的用户名/邮箱优先，其次使用客户列表兜底，最后才显示用户 ID 或 `-`。
3. 用户筛选下拉选项复用统一的 `customerName` 方法，避免选项和列表显示逻辑不一致。
4. 后端 `OrderServiceTest` 增加管理员订单列表挂载用户名和邮箱的测试用例。

### 修改文件

1. `frontend/src/views/admin/StoreOrdersView.vue`
2. `backend/src/test/java/com/youou/aso/modules/order/service/OrderServiceTest.java`
3. `PROJECT_PROGRESS.md`

### 验证结果

1. `cmd /c mvn test`：通过，后端 112 个测试全部成功。
2. `cmd /c npx vue-tsc --noEmit`：通过，前端类型检查成功。
3. `cmd /c npm.cmd run build`：通过，前端生产构建成功。
4. 构建仍存在既有非阻断提示：VueUse PURE 注释提示、主 chunk 超过 500k、`exceljs` 懒加载 chunk 超过 500k、npm 新版本提示；本次未调整构建架构。

### 下一步任务清单

1. 部署后刷新管理员订单列表，确认用户列显示用户名和邮箱，不再全是用户 ID。
2. 检查用户筛选下拉和列表用户列是否显示一致。
3. 如果仍出现某些历史订单只显示用户 ID，需要核查这些订单的 `customer_id` 是否对应存在的客户账号。

### 风险点

1. 当前前端兜底依赖管理员客户列表接口；如果某个历史订单的客户账号已不存在，仍会按预期回退显示用户 ID。
2. 后端服务层已经覆盖客户身份挂载测试，但线上如果后端未重新部署，前端兜底仍可改善列表显示。
## 2026-06-29 更新：管理员订单列表用户下拉样式优化

- 状态：已完成管理员端订单列表“用户”筛选下拉框的局部样式优化，并通过前端类型检查和生产构建。
- 当前任务：修复用户筛选下拉选项显示拥挤、用户名和邮箱层级不清晰、长列表面板观感不佳的问题。
- 实现原则：仅调整管理员订单列表页面局部模板属性和 CSS，不修改接口、筛选逻辑、路由、数据库、依赖、`.env` 或秘钥配置。

### 实施内容

1. 为管理员订单列表用户筛选 `el-select` 增加专用 `popper-class`，避免样式影响其它下拉框。
2. 将用户选项统一为舒展的两行布局：用户名加粗显示，邮箱以浅色小字号显示，并固定行高和间距。
3. 优化用户下拉弹层宽度、选项内边距、悬停背景和最大滚动高度，避免长用户列表撑开页面。
4. “全部用户”选项保留紧凑单行展示，避免和具体用户选项混在一起显得突兀。

### 修改文件

1. `frontend/src/views/admin/StoreOrdersView.vue`
2. `PROJECT_PROGRESS.md`

### 验证结果

1. `cmd /c npx vue-tsc --noEmit`：通过，前端类型检查成功。
2. `cmd /c npm.cmd run build`：通过，前端生产构建成功。
3. 构建仍存在既有非阻断提示：VueUse PURE 注释提示、主 chunk 超过 500k、`exceljs` chunk 超过 500k、npm 新版本提示；本次未调整构建架构和依赖。

### 下一步任务清单

1. 部署后在管理员端苹果/谷歌/iPad订单列表检查用户筛选下拉的宽度、两行文本间距和滚动高度是否符合预期。
2. 使用用户名、邮箱较长的账号测试下拉选项截断效果，确认不会覆盖相邻内容。
3. 如后续客户数量很大，可再评估是否将用户筛选改为远程搜索分页选择，但这属于接口和交互扩展，需要另行确认。

### 风险点

1. 当前优化只影响 Element Plus 下拉弹层的局部样式；如果浏览器缩放比例或系统字体差异较大，可能仍需微调面板宽度。
2. 用户筛选仍基于当前已加载客户列表，未改变数据来源和分页策略。
## 2026-06-29 更新：待确认订单列表用户筛选与列表样式优化

- 状态：已完成管理员端待确认订单页用户筛选、用户列和列表局部排版优化，并通过前端类型检查和生产构建。
- 当前任务：修复待确认订单列表样式混乱、筛选区域缺少用户字段、列表缺少用户字段的问题。
- 实现原则：仅调整待确认订单页前端查询参数、展示字段和局部 CSS；复用已有管理员客户列表接口和订单查询 `customerId` 参数；不修改路由、数据库、依赖、`.env` 或秘钥配置。

### 实施内容

1. 待确认订单筛选区新增“用户”下拉筛选，支持按用户名/邮箱展示，选择后将 `customerId` 带入订单分页查询。
2. 待确认订单列表新增“用户”列，优先显示订单返回的用户名和邮箱，其次用已加载客户列表兜底，最后才回退为用户 ID。
3. 待确认订单导出 CSV 同步加入用户字段，避免页面列表和导出内容不一致。
4. 优化待确认订单表格行内样式：应用名称截断、应用列宽收敛、用户信息两行展示、操作按钮以规则网格排列。
5. 为用户下拉框增加专用弹层样式，统一选项高度、两行文本间距、面板宽度、滚动高度和 hover 背景。

### 修改文件

1. `frontend/src/views/admin/PendingConfirmOrdersView.vue`
2. `PROJECT_PROGRESS.md`

### 验证结果

1. `cmd /c npx vue-tsc --noEmit`：通过，前端类型检查成功。
2. `cmd /c npm.cmd run build`：通过，前端生产构建成功。
3. 构建仍存在既有非阻断提示：VueUse PURE 注释提示、主 chunk 超过 500k、`exceljs` chunk 超过 500k、npm 新版本提示；本次未调整构建架构和依赖。

### 下一步任务清单

1. 部署后在管理员端待确认订单页检查用户筛选是否能正确过滤列表和分页总数。
2. 检查不同用户名、邮箱长度下用户列和用户下拉选项是否正常截断、不挤压应用列。
3. 在小屏或浏览器缩放环境下检查操作列固定区域和确认/取消按钮是否仍保持规整。

### 风险点

1. 用户筛选复用当前已加载客户列表；如果客户数量非常大，后续可能需要远程搜索分页选择，但本次不扩展接口形态。
2. 表格新增用户列后横向信息量增加，小屏下仍依赖 Element Plus 表格横向滚动和固定操作列。
## 2026-06-29 更新：待确认订单操作列布局二次收敛

- 状态：已完成待确认订单列表操作列按钮布局二次优化，并通过前端类型检查和生产构建。
- 当前任务：修复待确认订单操作列中“详情 / 确认 / 取消”按钮仍显得松散、不够规整的问题。
- 实现原则：仅调整待确认订单页操作列模板类名和局部 CSS；不修改确认、取消、详情跳转等业务逻辑；不修改路由、数据库、依赖、`.env` 或秘钥配置。

### 实施内容

1. 操作列宽度从 180 收敛为 170，减少固定列视觉空白。
2. 为“确认”按钮增加专用样式类，和“详情”“取消”一起按固定网格定位。
3. 操作区改为两列固定宽度布局：详情位于左侧，确认位于右侧，取消固定在确认按钮下方。
4. 固定操作按钮高度、宽度、内边距和间距，避免窄屏或浏览器缩放时按钮漂移。

### 修改文件

1. `frontend/src/views/admin/PendingConfirmOrdersView.vue`
2. `PROJECT_PROGRESS.md`

### 验证结果

1. `cmd /c npx vue-tsc --noEmit`：通过，前端类型检查成功。
2. `cmd /c npm.cmd run build`：通过，前端生产构建成功。
3. 构建仍存在既有非阻断提示：VueUse PURE 注释提示、主 chunk 超过 500k、`exceljs` chunk 超过 500k、npm 新版本提示；本次未调整构建架构和依赖。

### 下一步任务清单

1. 部署后在待确认订单列表检查操作列是否呈现“详情/确认”一行、“取消”对齐确认下方的规整布局。
2. 在浏览器缩放和较窄窗口下检查固定操作列是否仍然不遮挡分页和表格内容。
3. 如后续仍希望进一步压缩操作列，可评估改为图标按钮加 tooltip，但需要确认交互方案。

### 风险点

1. 当前操作列采用固定按钮宽度，对中文短文案适配良好；若后续英文文案变长，可能需要按语言环境微调宽度。
2. 操作列仍为 Element Plus 固定列，极窄屏下会依赖表格横向滚动。
## 2026-06-29 更新：审核订单归入订单列表

- 状态：已完成前端层面的审核订单入口合并，管理员端和用户端不再展示独立审核/特殊订单菜单入口，审核单合入订单列表展示，并通过前端类型检查和生产构建。
- 当前任务：取消独立“审核管理”入口，将审核中的特殊订单归入订单列表，通过不同状态区分。
- 实现原则：不删除文件、不改数据库、不改后端表结构、不安装依赖、不修改 `.env` 或秘钥；复用现有订单 API 和特殊审核 API，在前端订单列表中统一展示和操作，保持变更范围可控。

### 实施内容

1. 管理员端隐藏侧边栏“审核管理”菜单，旧 `/admin/audits` 路由改为重定向到 `/admin/orders/apple`。
2. 用户端隐藏“特殊订单”菜单，旧 `/user/orders/special` 路由改为重定向到 `/user/orders/apple`。
3. 前端订单状态类型新增列表层状态 `PENDING_REVIEW`、`APPROVED_WAIT_SUBMIT`、`SUBMITTED`，用于订单列表统一展示审核单状态。
4. 管理员订单列表同时加载普通订单分页和特殊审核单，按当前用户、应用、地区、任务类型、订单类型、状态、关键字、创建时间等条件过滤后合入同一表格。
5. 管理员订单列表中待审核行显示“审核”动作，审核通过调用原特殊审核接口；待审核行取消调用原特殊审核取消接口；普通订单操作保持原逻辑。
6. 用户订单列表同时加载普通订单分页和用户特殊审核单，合入同一表格展示；已审核待支付行显示“支付并提交”动作，调用原特殊审核提交接口。
7. 订单列表中的特殊审核行使用审核编号作为订单号，状态显示复用特殊审核状态文案；审核行的订单时间展示为 `-`，避免误导为真实执行周期。

### 修改文件

1. `frontend/src/api/orders.ts`
2. `frontend/src/router/index.ts`
3. `frontend/src/layouts/AdminLayout.vue`
4. `frontend/src/layouts/UserLayout.vue`
5. `frontend/src/views/admin/StoreOrdersView.vue`
6. `frontend/src/views/user/StoreOrdersView.vue`
7. `PROJECT_PROGRESS.md`

### 验证结果

1. `cmd /c npx vue-tsc --noEmit`：通过，前端类型检查成功。
2. `cmd /c npm.cmd run build`：通过，前端生产构建成功。
3. 构建仍存在既有非阻断提示：VueUse PURE 注释提示、主 chunk 超过 500k、`exceljs` chunk 超过 500k、npm 新版本提示；本次未调整构建架构和依赖。

### 下一步任务清单

1. 部署后检查管理员端苹果/谷歌/iPad订单列表是否能看到待审核、已审核待支付、已提交订单的特殊审核行。
2. 检查管理员订单列表中待审核特殊行的“审核”和“取消”动作是否能正常刷新列表状态。
3. 检查用户端订单列表中已审核待支付特殊行的“支付并提交”是否能正常生成真实订单。
4. 如果后续需要严格的统一分页总数和跨普通订单/审核单的后端排序，需要新增后端聚合查询接口；本次为了不改数据库和降低风险，采用前端聚合展示。

### 风险点

1. 当前是前端聚合普通订单分页和特殊审核列表，审核单数量很多时，列表总数和跨类型排序不会像后端统一分页一样精确。
2. 特殊审核行没有进入订单详情页，避免用审核 ID 打开真实订单详情；如需完整审核详情，需要后续设计统一详情页或在列表中增加审核详情弹窗。
3. 后端特殊审核 API 仍保留，旧文件未删除，符合当前“不删除文件”和“不改数据库结构”的约束。
## 2026-06-29 更新：管理员余额调整功能

- 状态：已完成管理员端退款、赠送、扣减三类余额调整能力，保留原“充值”仅用于真实充值到账，并通过后端完整测试和前端生产构建。
- 当前任务：在现有充值只能增加余额的基础上，新增可审计的退款/赠送/扣减余额调整功能。
- 实现原则：不修改 `.env` 或秘钥配置、不安装依赖、不改钱包表结构；新增固定流水类型和数据迁移，仅补齐余额调整业务能力。

### 实施内容

1. 后端新增 `AdminWalletAdjustmentType`，支持 `REFUND`、`GIFT`、`DEDUCT` 三种管理员余额调整动作。
2. 后端新增 `AdminBalanceAdjustmentCommand` 和 `/api/admin/wallet/adjustments` 接口，继续走管理员权限校验。
3. `AdminWalletService` 新增 `adjustBalance`：退款/赠送按收入增加余额，扣减按支出减少余额；金额必须大于 0，备注必填，扣减后余额不能小于 0。
4. 钱包流水类型新增 `ADMIN_REFUND`、`ADMIN_GIFT`、`ADMIN_DEDUCT`，保留旧 `ADMIN_ADJUSTMENT` 兼容历史记录。
5. 新增 Flyway 数据迁移 `V18__admin_wallet_adjustment_types.sql`，初始化三类新增流水类型的中英文显示名。
6. 管理端财务页新增“余额调整”按钮和弹窗，支持选择客户、调整类型、金额和必填备注；在充值记录页发起调整成功后自动跳转财务流水页，便于查看新流水。
7. 前端钱包 API、用户端收支明细筛选、财务页和中英文语言包同步新增余额调整类型和文案。
8. 后端单元测试补充退款、赠送、扣减、余额不足、备注为空等场景；类型配置测试改为按枚举总数断言。

### 修改文件

1. `backend/src/main/java/com/youou/aso/modules/wallet/domain/AdminWalletAdjustmentType.java`
2. `backend/src/main/java/com/youou/aso/modules/wallet/domain/WalletTransactionType.java`
3. `backend/src/main/java/com/youou/aso/modules/wallet/domain/WalletTransactionTypeConfig.java`
4. `backend/src/main/java/com/youou/aso/modules/wallet/dto/AdminBalanceAdjustmentCommand.java`
5. `backend/src/main/java/com/youou/aso/modules/wallet/service/AdminWalletService.java`
6. `backend/src/main/java/com/youou/aso/modules/wallet/api/AdminWalletController.java`
7. `backend/src/main/java/com/youou/aso/modules/wallet/repository/JdbcWalletTransactionTypeConfigRepository.java`
8. `backend/src/main/resources/db/migration/V18__admin_wallet_adjustment_types.sql`
9. `backend/src/test/java/com/youou/aso/modules/wallet/service/AdminWalletServiceTest.java`
10. `backend/src/test/java/com/youou/aso/modules/wallet/service/WalletTransactionTypeConfigServiceTest.java`
11. `frontend/src/api/wallet.ts`
12. `frontend/src/views/admin/FinanceManagementView.vue`
13. `frontend/src/views/user/TransactionsView.vue`
14. `frontend/src/i18n/locales/zh-CN.ts`
15. `frontend/src/i18n/locales/en-US.ts`
16. `PROJECT_PROGRESS.md`

### 验证结果

1. `cmd /c npx vue-tsc --noEmit`：通过，前端类型检查成功。
2. `cmd /c mvn test "-Dtest=AdminWalletServiceTest,WalletTransactionTypeConfigServiceTest"`：通过，钱包相关定向测试 10 个全部成功。
3. `cmd /c npm.cmd run build`：通过，前端生产构建成功。
4. `cmd /c mvn test`：首次因 Maven `target/classes` 残留旧版本迁移文件报 Flyway V16 冲突；执行 `cmd /c mvn clean test` 清理构建产物后通过，后端 117 个测试全部成功。
5. 构建仍存在既有非阻断提示：VueUse PURE 注释提示、主 chunk 超过 500k、`exceljs` chunk 超过 500k、npm 新版本提示；本次未调整构建架构和依赖。

### 下一步任务清单

1. 部署后在管理端财务页分别测试退款、赠送、扣减，确认余额、收入/支出方向、流水类型、变动前后余额和备注都正确。
2. 在余额不足场景测试扣减，确认后端拒绝操作且不会写入流水。
3. 检查“流水类型配置”页面是否显示新增三类类型，必要时由管理员调整展示名称。
4. 如后续需要更严格的财务审批，可再设计余额调整审核流；本次为管理员直接调整并写完整审计流水。

### 风险点

1. V18 迁移会新增三类固定流水类型显示名；测试时本地库已执行到 v18，其他环境部署时需正常跑 Flyway。
2. 余额调整属于高权限财务操作，目前依赖管理员权限和流水审计，不包含二次审批。
3. MySQL 8.3 对迁移中的 `VALUES()` 写法给出弃用警告，但迁移执行成功；该写法项目历史迁移中已有使用，本次保持一致。

## 2026-06-29 更新：待审核订单独立列表入口

- 状态：已完成管理端“待审核订单”独立入口，待审核订单像“待确认订单”一样从普通订单列表入口中抽离出来展示，并通过前端类型检查和生产构建。
- 当前任务：在不恢复旧“审核管理”模块、不新增数据库结构、不改后端接口的前提下，为管理端增加独立的待审核订单菜单和页面入口。
- 实现原则：复用现有订单列表组件和特殊订单审核 API；不删除文件、不修改 `.env` 或秘钥配置、不安装依赖、不调整数据库和后端架构。

### 实施内容

1. 管理端新增 `/admin/pending-review-orders` 路由，复用 `StoreOrdersView.vue`，并通过 `statusFilter: 'PENDING_REVIEW'` 固定展示待审核数据。
2. 管理端订单菜单中新增“待审核订单”入口，位置与“待确认订单”并列，便于管理员按处理状态进入。
3. 管理端订单列表组件支持路由级 `PENDING_REVIEW` 状态过滤：固定待审核入口时不再跳过特殊审核单加载，同时普通订单行会按状态过滤掉，避免混入无关普通订单。
4. 管理端订单列表标题新增待审核订单文案分支，页面标题和菜单文案保持一致。
5. 中英文语言包新增 `menu.pendingReviewOrders`，保证现有多语言环境下文案可正常显示。

### 修改文件

1. `frontend/src/router/index.ts`
2. `frontend/src/layouts/AdminLayout.vue`
3. `frontend/src/views/admin/StoreOrdersView.vue`
4. `frontend/src/i18n/locales/zh-CN.ts`
5. `frontend/src/i18n/locales/en-US.ts`
6. `PROJECT_PROGRESS.md`

### 验证结果

1. `cmd /c npx vue-tsc --noEmit`：通过，前端类型检查成功。
2. `cmd /c npm.cmd run build`：通过，前端生产构建成功。
3. 构建仍存在既有非阻断提示：VueUse PURE 注释提示、主 chunk 超过 500k、`exceljs` chunk 超过 500k、npm 新版本提示；本次未调整构建架构和依赖。

### 下一步任务清单

1. 部署后在管理端点击“待审核订单”，确认只显示 `PENDING_REVIEW` 特殊审核订单，并且审核/取消动作正常刷新列表。
2. 检查苹果、谷歌、iPad 订单入口中是否仍可通过状态筛选看到审核状态订单，确认新入口和综合列表展示不冲突。
3. 如后续需要严格统一分页总数和跨普通订单/审核单的后端排序，可评估新增后端聚合查询接口；本次为避免数据库和架构改动，仍采用现有前端聚合方案。

### 风险点

1. 当前待审核入口复用前端聚合逻辑加载特殊审核单；审核单数量很大时，分页总数和后端统一分页相比仍不够精确。
2. 待审核行仍不是完整真实订单详情页数据，详情能力保持现状，避免用审核单 ID 误打开普通订单详情。
## 2026-06-29 更新：管理员代下特殊订单恢复金额输入

- 状态：已完成管理员端关键词保排名/关键词覆盖代用户下单金额输入恢复，并修复金额为空时右侧合计显示 `$0.00` 的问题。
- 当前任务：修正管理员代用户下特殊订单应填写订单金额的业务要求，撤回无金额创建待审核单的临时兼容逻辑。
- 实现原则：管理员代下单必须填写金额；金额为空时页面显示“待填写”而不是 `$0.00`；不改数据库、不改路由、不改 `.env` 或依赖。

### 实施内容

1. 管理员端特殊订单创建页恢复“订单金额（$）”输入块。
2. 管理员端特殊订单提交按钮重新要求金额大于 0，避免空金额提交。
3. 右侧费用明细和合计在金额未填写时显示“待填写”，输入金额后再显示实际金额。
4. 管理员端特殊订单 payload 恢复传递 `specialAmount`，后端继续按已有金额创建/扣款/待支付逻辑处理。
5. 撤回后端无金额创建 `PENDING_REVIEW` 特殊审核单的分支、返回工厂方法和对应测试，恢复金额必填语义。
6. 中英文语言包新增“待填写 / Pending input”文案。

### 修改文件

1. `frontend/src/views/admin/OrderCreateView.vue`
2. `frontend/src/views/user/OrderCreateView.vue`
3. `frontend/src/i18n/locales/zh-CN.ts`
4. `frontend/src/i18n/locales/en-US.ts`
5. `backend/src/main/java/com/youou/aso/modules/order/service/SpecialOrderAuditService.java`
6. `backend/src/main/java/com/youou/aso/modules/order/api/AdminOrderController.java`
7. `backend/src/main/java/com/youou/aso/modules/order/dto/AdminCreateOrderResult.java`
8. `backend/src/test/java/com/youou/aso/modules/order/api/AdminOrderControllerTest.java`
9. `PROJECT_PROGRESS.md`

### 验证结果

1. `cmd /c npx vue-tsc --noEmit`：通过，前端类型检查成功。
2. `cmd /c mvn test "-Dtest=AdminOrderControllerTest,SpecialOrderAuditServiceTest"`：通过，15 个后端定向测试全部成功。
3. `cmd /c npm.cmd run build`：通过，前端生产构建成功。
4. 构建仍存在既有非阻断提示：VueUse PURE 注释提示、主 chunk 超过 500k、`exceljs` chunk 超过 500k、npm 新版本提示；本次未调整构建架构和依赖。

### 下一步任务清单

1. 部署后在管理员端创建关键词保排名/关键词覆盖，确认金额为空时合计显示“待填写”，按钮不可提交。
2. 输入金额后确认合计同步显示金额，提交后按余额情况进入已支付待确认或已审核待支付。
3. 在用户端创建这两类需求时确认仍不要求用户填写金额，价格由后续审核确认。

### 风险点

1. 管理员代下单和用户自助提交需求现在金额行为不同：管理员必须填金额，用户不填金额；这是按当前业务口径区分。
2. 后端保留原有管理员带金额创建特殊订单逻辑，金额校验仍由服务层处理。
3. 本次未改特殊订单详情展示，地区关键词明细在详情页不足的问题仍需后续单独处理。

## 2026-06-29 更新：特殊订单创建页移除重复地区和金额输入

- 状态：已完成关键词保排名/关键词覆盖创建页中重复“地区”字段和管理员端“订单金额”输入的清理，并补齐管理员端无金额创建待审核特殊单的后端兼容逻辑。
- 当前任务：修复特殊订单页面中仍残留顶部单独地区选择、管理员端仍要求填写订单金额的问题。
- 实现原则：页面不再要求临时价格；价格仍由后续审核/线下协商确认；不改数据库结构、不改路由、不改 `.env` 或依赖。

### 实施内容

1. 用户端创建订单页在特殊订单类型下隐藏顶部单独“地区”选择，地区只保留下方地区组。
2. 管理员端创建订单页在特殊订单类型下隐藏顶部单独“地区”选择，地区只保留下方地区组。
3. 管理员端特殊订单页面移除“订单金额（$）”输入块，避免创建页出现未协商价格。
4. 管理员端特殊订单摘要改为显示待审核/待确认价格，不再显示 `$0.00` 的伪金额明细。
5. 管理员端创建特殊订单时 `specialAmount` 传空，后端改为创建 `PENDING_REVIEW` 特殊审核单。
6. 保留原有管理员端带金额创建特殊订单的后端能力，兼容已有调用路径；仅当前页面不再传金额。
7. 新增控制器测试覆盖管理员无金额创建特殊审核单的场景。

### 修改文件

1. `frontend/src/views/user/OrderCreateView.vue`
2. `frontend/src/views/admin/OrderCreateView.vue`
3. `backend/src/main/java/com/youou/aso/modules/order/service/SpecialOrderAuditService.java`
4. `backend/src/main/java/com/youou/aso/modules/order/api/AdminOrderController.java`
5. `backend/src/main/java/com/youou/aso/modules/order/dto/AdminCreateOrderResult.java`
6. `backend/src/test/java/com/youou/aso/modules/order/api/AdminOrderControllerTest.java`
7. `PROJECT_PROGRESS.md`

### 验证结果

1. `cmd /c npx vue-tsc --noEmit`：通过，前端类型检查成功。
2. `cmd /c mvn test "-Dtest=AdminOrderControllerTest,SpecialOrderAuditServiceTest"`：通过，16 个后端定向测试全部成功。
3. `cmd /c npm.cmd run build`：通过，前端生产构建成功。
4. 构建仍存在既有非阻断提示：VueUse PURE 注释提示、主 chunk 超过 500k、`exceljs` chunk 超过 500k、npm 新版本提示；本次未调整构建架构和依赖。

### 下一步任务清单

1. 部署后在用户端和管理员端创建关键词保排名/关键词覆盖，确认页面只出现地区组，不再出现顶部单独地区。
2. 部署后在管理员端创建这两类特殊订单，确认不需要填写金额即可进入待审核订单列表。
3. 在待审核订单列表完成审核并填写协商价格，确认后续待支付/提交流程仍正常。

### 风险点

1. 管理员端无金额创建特殊单现在进入 `PENDING_REVIEW`，不再直接进入已审核待支付；这是为了避免创建页设置临时价格。
2. 后端仍保留带金额创建特殊订单的兼容路径，如果后续不再需要，可另行确认是否收敛接口行为。
3. 本次未改审核详情和订单详情展示，特殊订单明细展示不足的问题仍需按后续任务单独处理。

## 2026-06-29 更新：关键词保排名/关键词覆盖创建页交互对齐老系统

- 状态：已完成用户端和管理员端创建订单页中“关键词保排名”“关键词覆盖”两类特殊订单的页面字段与交互调整，并通过前端类型检查和生产构建。
- 当前任务：仅修改页面字段和交互，不改特殊审核流程、不改后端接口、不改数据库、不改路由、不改价格审核逻辑。
- 实现原则：保留现有 `SpecialOrderAudit` 审核链路；价格仍按线下协商/管理员审核确认，不在用户端设置临时价格；页面输入在提交前展平成现有 `specialItems` payload，保持接口兼容。

### 实施内容

1. 用户端创建订单页将两类特殊订单明细从单行平铺改为按地区分组展示，每个地区组下维护关键词明细。
2. 管理员端创建订单页同步使用按地区分组的特殊订单明细交互。
3. 关键词保排名的目标排名由数字输入改为下拉选项：`top1`、`top2`、`top3`、`top5`、`top10`；提交时继续使用现有数字值 `1/2/3/5/10`，不改后端字段。
4. 关键词覆盖页面字段改为“当前排名”，弱化原“覆盖说明”列；提交时复用现有 `coverageNote` 字段保持接口兼容。
5. 特殊订单关键词新增逻辑改为与关键词/评论明细一致：新增行插入第一行，并且第一行关键词填写后才能继续添加。
6. 特殊订单地区组支持添加/删除，并过滤其他地区组已选地区，避免同一订单类型内重复选择同一地区。
7. 用户端价格摘要继续显示待审核/管理员确认价格，不增加临时价格；管理员端保留协商金额输入。
8. 中英文语言包补充“当前排名”和特殊关键词行校验提示文案。

### 修改文件

1. `frontend/src/views/user/OrderCreateView.vue`
2. `frontend/src/views/admin/OrderCreateView.vue`
3. `frontend/src/i18n/locales/zh-CN.ts`
4. `frontend/src/i18n/locales/en-US.ts`
5. `PROJECT_PROGRESS.md`

### 验证结果

1. `cmd /c npx vue-tsc --noEmit`：通过，前端类型检查成功。
2. `cmd /c npm.cmd run build`：通过，前端生产构建成功。
3. 构建仍存在既有非阻断提示：VueUse PURE 注释提示、主 chunk 超过 500k、`exceljs` chunk 超过 500k、npm 新版本提示；本次未调整构建架构和依赖。

### 下一步任务清单

1. 部署后在用户端分别创建关键词保排名和关键词覆盖审核单，检查地区组、目标排名下拉、当前排名字段、联系方式和需求内容是否符合预期。
2. 部署后在管理员端创建这两类订单，检查地区组明细、协商金额和提交后的审核/待支付流程是否仍正常。
3. 检查审核列表和订单列表中的特殊审核单明细展示是否能满足查看需求；如需像老系统详情页一样完整展示地区关键词明细，可作为后续详情展示任务单独处理。

### 风险点

1. 关键词覆盖的“当前排名”当前复用原 `coverageNote` 字段提交，未新增数据库字段；如果后续需要严格区分覆盖说明和当前排名，需要单独确认数据库扩展。
2. 目标排名采用数字值存储、页面显示 `topN`，未保存原始字符串；这避免数据库变更，但历史接口返回仍会按数字展示。
3. 本次只改创建页字段和交互，未改审核详情/订单详情展示；若详情页仍显示不够完整，需要后续继续补展示层。
## 2026-06-29 更新：管理员特殊订单金额输入移至右侧费用区

- 状态：已完成管理员端创建特殊订单页面的金额输入位置调整。
- 当前任务：将管理员代用户下关键词保排名/关键词覆盖订单时需要填写的订单金额，从左侧订单参数区域移动到右侧订单摘要/费用区域。
- 实现原则：只改前端布局，不改后端接口、不改数据库、不改路由、不改用户端逻辑。

### 实施内容

1. 管理员端创建订单页中，特殊订单的“订单金额（$）”输入框不再显示在左侧订单参数底部。
2. 将“订单金额（$）”输入框移动到右侧摘要栏，放置在计费说明与费用明细之间。
3. 金额为空时继续显示“待填写”，金额大于 0 后继续同步合计金额和提交按钮状态。
4. 保留原有提交校验：管理员代用户创建特殊订单时，订单金额必须大于 0。
5. 补充右侧金额输入容器样式，使其与费用明细区域视觉一致。

### 修改文件

1. `frontend/src/views/admin/OrderCreateView.vue`
2. `PROJECT_PROGRESS.md`

### 验证结果

1. `cmd /c npx vue-tsc --noEmit`：通过，前端类型检查成功。
2. `cmd /c npm.cmd run build`：通过，前端生产构建成功。
3. 构建仍存在既有非阻断提示：VueUse PURE 注释提示、主 chunk 和 `exceljs` chunk 超过 500k、npm 新版本提示；本次未调整依赖和构建配置。

### 下一步任务清单

1. 在管理员端创建关键词保排名/关键词覆盖订单时，确认金额输入出现在右侧费用区域。
2. 确认未填写金额时提交按钮不可用，右侧合计显示“待填写”。
3. 输入金额后确认费用明细、合计金额和提交按钮状态同步更新。

### 风险点

1. 本次只调整管理员端创建订单页布局，未调整订单详情或审核详情中的金额展示。
2. 右侧摘要栏宽度较窄，金额输入使用全宽展示；如后续增加更多费用控件，可能需要统一重构摘要栏布局。
## 2026-06-29 更新：管理员代下特殊订单移除联系方式字段

- 状态：已完成管理员端代用户创建特殊订单时的联系方式字段移除。
- 当前任务：管理员代用户下关键词保排名/关键词覆盖订单时，不再要求填写联系方式，避免出现“填写管理员还是用户联系方式”的业务歧义。
- 实现原则：只改管理员端创建订单页前端逻辑，不改用户端、不改后端接口、不改数据库、不改路由。

### 实施内容

1. 管理员端创建订单页中，特殊订单区域移除“联系方式类型 / 联系方式”输入区域。
2. 管理员端特殊订单提交时，不再校验联系方式是否填写。
3. 管理员端提交 payload 中 `contactType`、`contactValue` 固定传 `null`，保持现有接口字段兼容。
4. 清理管理员端未使用的联系方式表单字段、联系方式下拉选项和对应样式。
5. 用户端创建特殊订单仍保留联系方式字段和校验，便于用户自助提交需求时留下沟通方式。

### 修改文件

1. `frontend/src/views/admin/OrderCreateView.vue`
2. `PROJECT_PROGRESS.md`

### 验证结果

1. `cmd /c npx vue-tsc --noEmit`：通过，前端类型检查成功。
2. `cmd /c npm.cmd run build`：通过，前端生产构建成功。
3. 构建仍存在既有非阻断提示：VueUse PURE 注释提示、主 chunk 和 `exceljs` chunk 超过 500k、npm 新版本提示；本次未调整依赖和构建配置。

### 下一步任务清单

1. 在管理员端创建关键词保排名/关键词覆盖订单，确认页面不再显示联系方式输入。
2. 确认管理员端特殊订单只需用户、应用、地区关键词明细和订单金额即可提交。
3. 在用户端创建同类特殊订单，确认联系方式字段仍保留且校验正常。

### 风险点

1. 本次未改后端字段结构，历史订单或用户端订单中的联系方式仍会按原逻辑保留。
2. 如果后续详情页需要展示“管理员代下单无联系方式”的区别，需要在详情展示层单独处理。
## 2026-06-29 更新：订单列表过滤已提交特殊审核记录避免重复显示

- 状态：已完成用户端和管理员端订单列表重复记录修复。
- 当前任务：修复特殊订单在余额足够并生成真实订单后，订单列表同时显示 `SUBMITTED` 审核记录和真实订单的问题。
- 实现原则：只调整前端列表聚合展示逻辑，不改下单流程、不改审核流程、不改后端接口、不改数据库和路由。

### 实施内容

1. 管理员端订单列表加载特殊审核记录时，过滤掉状态为 `SUBMITTED` 的审核记录。
2. 用户端订单列表加载特殊审核记录时，同步过滤掉状态为 `SUBMITTED` 的审核记录。
3. 保留 `PENDING_REVIEW`、`APPROVED_WAIT_SUBMIT`、`CANCELLED` 等尚需作为审核/待支付状态展示的特殊记录。
4. 已生成真实订单的特殊审核记录继续通过真实订单行展示，避免同一次下单出现两条列表记录。

### 修改文件

1. `frontend/src/views/admin/StoreOrdersView.vue`
2. `frontend/src/views/user/StoreOrdersView.vue`
3. `PROJECT_PROGRESS.md`

### 验证结果

1. `cmd /c npx vue-tsc --noEmit`：通过，前端类型检查成功。
2. `cmd /c npm.cmd run build`：通过，前端生产构建成功。
3. 构建仍存在既有非阻断提示：VueUse PURE 注释提示、主 chunk 和 `exceljs` chunk 超过 500k、npm 新版本提示；本次未调整依赖和构建配置。

### 下一步任务清单

1. 管理员端创建余额足够的关键词保排名/关键词覆盖订单，确认订单列表只显示真实订单。
2. 用户端订单列表确认同一特殊订单不再同时显示 `SUBMITTED` 审核记录和真实订单。
3. 创建余额不足或待审核特殊订单，确认待审核/已审核待支付记录仍能显示。

### 风险点

1. 本次只修正列表展示聚合，后端仍保留 `SUBMITTED` 审核记录作为审计链路数据。
2. 如果后续需要单独查询历史审核记录，仍应通过审核记录列表或详情入口展示，而不是混入订单列表。
## 2026-06-29 更新：用户特殊订单需求内容改为选填并修复成功后误报失败

- 状态：已完成用户端关键词保排名/关键词覆盖下单的需求内容选填调整，并修复提交成功后同时出现失败提示的问题。
- 当前任务：特殊订单中的“需求内容”应为选填；提交成功后不应因跳转到不存在的特殊订单路由而误报失败。
- 实现原则：不改数据库、不改路由结构、不改审核流程，仅调整字段校验、默认内容和提交成功后的跳转目标。

### 实施内容

1. 用户端创建特殊订单时，不再校验“需求内容”为必填。
2. “需求内容”占位文案改为选填说明，中英文同步调整。
3. 后端客户提交特殊审核需求时，移除 `requestedContent` 的 `@NotBlank` 校验。
4. 服务层在用户未填写需求内容时，使用默认特殊订单内容作为审核记录内容，避免后续审核/生成订单出现空标题。
5. 修复用户端特殊需求提交成功后跳转到不存在的 `user-special-orders` 路由导致 catch 分支误报“需求提交失败”的问题；改为跳转到当前应用商店订单列表。

### 修改文件

1. `frontend/src/views/user/OrderCreateView.vue`
2. `frontend/src/i18n/locales/zh-CN.ts`
3. `frontend/src/i18n/locales/en-US.ts`
4. `backend/src/main/java/com/youou/aso/modules/order/api/CustomerSpecialOrderAuditController.java`
5. `backend/src/main/java/com/youou/aso/modules/order/service/SpecialOrderAuditService.java`
6. `PROJECT_PROGRESS.md`

### 验证结果

1. `cmd /c npx vue-tsc --noEmit`：通过，前端类型检查成功。
2. `cmd /c npm.cmd run build`：通过，前端生产构建成功。
3. `cmd /c mvn test "-Dtest=CustomerSpecialOrderAuditControllerTest,SpecialOrderAuditServiceTest"`：通过，9 个后端定向测试全部成功。
4. 构建仍存在既有非阻断提示：VueUse PURE 注释提示、主 chunk 和 `exceljs` chunk 超过 500k、npm 新版本提示；后端测试存在 ByteBuddy/JDK 动态代理提示；本次未调整依赖和构建配置。

### 下一步任务清单

1. 用户端创建关键词保排名/关键词覆盖订单时，留空“需求内容”并确认可以提交。
2. 提交成功后确认只显示成功提示，不再同时显示失败提示。
3. 确认提交成功后跳转到对应应用商店订单列表，且待审核记录正常出现。

### 风险点

1. 需求内容为空时，后端会使用默认服务内容作为审核记录内容；如后续需要区分“用户未填写”，可在详情展示层补充标识。
2. 本次未删除语言包中旧的必填提示文案，避免影响其它潜在复用点；当前创建页已不再调用该提示。

## 2026-06-29 更新：添加应用弹窗控件宽度收窄

- 状态：已完成代码调整与构建验证，待用户刷新页面确认视觉效果。
- 当前任务：根据截图反馈，将用户端“添加应用”弹窗中的“地区”和“应用”两个控件长度收窄，避免表单在弹窗内显得过长。
- 实现原则：只调整前端局部样式，不修改接口、路由、业务逻辑、数据库结构或依赖。

### 实施内容

1. 为添加应用弹窗的地区下拉框和 App 输入框统一增加 `add-app-control` 样式类。
2. 桌面端将两个控件宽度统一收窄为 `360px`，并保留 `max-width: 100%`，防止窄容器溢出。
3. 移动端继续让这两个控件占满可用宽度，保证小屏可用性。
4. 保留原有商店切换、地区加载、应用搜索、搜索图标按钮和保存逻辑不变。

### 修改文件

1. `frontend/src/views/user/ApplicationManagementView.vue`
2. `PROJECT_PROGRESS.md`
3. `../PROJECT_PROGRESS.md`

### 验证结果

1. `cmd /c npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. 构建仍存在既有非阻断提示：VueUse PURE 注释提示、主 chunk 和 `exceljs` chunk 超过 500k、npm 新版本提示；本次未调整依赖和构建配置。

### 下一步任务清单

1. 刷新用户端 `/user/applications`，打开“添加应用”弹窗，确认“地区”和“应用”两个控件长度是否符合预期。
2. 在桌面宽度和窄屏宽度分别检查弹窗，确认控件不溢出、不遮挡提示文案。
3. 如视觉上仍偏长或偏短，可继续在 `360px` 基础上微调为更贴合截图的固定宽度。

### 风险点

1. 本次未启动浏览器做截图级视觉验证，最终观感仍需用户在当前页面确认。
2. 只调整用户端添加应用弹窗，不影响管理员端应用管理页或订单创建页里的地区选择控件。

## 2026-06-29 更新：临时/未实现功能审查

- 状态：已完成代码静态审查，未修改业务代码，待用户确认是否进入修复。
- 当前任务：检查项目中是否仍存在临时、占位、未接接口或未实现的功能入口。
- 实现原则：只做读取和分析，不删除文件、不调整路由、不补接口、不改数据库。

### 审查发现

1. `frontend/src/views/public/ForgotPasswordView.vue`：找回密码页已挂载到 `/forgot-password`，登录页也有入口，但“发送验证邮件”按钮没有提交逻辑，后端也没有找回/重置密码接口。
2. `frontend/src/views/admin/SiteConfigView.vue`：`/admin/site-config` 路由仍存在，页面只有“维护客服二维码、联系说明、首页默认内容覆盖和防冒用提示”的说明面板，没有真实表单或接口；侧边栏当前没有菜单入口，但手动访问路由仍可打开。
3. `frontend/src/views/admin/AuditLogsView.vue`、`frontend/src/views/admin/AuditManagementView.vue`、`frontend/src/views/user/SpecialOrdersView.vue`：属于未被当前路由引用的历史/遗留视图文件，其中 `AuditLogsView.vue` 只有说明面板。
4. 账户安全相关文案仍保留 `passwordApiPending`、`forcePasswordChange` 等字段；当前账户设置页没有改密入口，后端也没有改密/强制改密接口，属于后续安全能力未落地，但目前不形成可点击假保存流程。
5. 搜索到的 `mock/demo` 多数位于测试代码或分类名称中，未发现生产主流程中明显使用 mock 数据冒充真实数据。

### 执行命令与验证结果

1. `rg -n "TODO|FIXME|HACK|XXX|待实现|未实现|暂未|占位|placeholder|mock|demo|coming soon|敬请期待|not implemented|No data|dummy|fake|stub" frontend\src backend\src -S`：发现主要线索为找回密码、站点配置、操作日志占位和历史文案。
2. `rg -n "@GetMapping|@PostMapping|@PutMapping|@DeleteMapping|@RequestMapping" backend\src\main\java -S`：未发现找回密码、重置密码、站点配置或操作日志相关后端接口。
3. `rg -n "AuditLogsView|AuditManagementView|SpecialOrdersView|SiteConfigView|ForgotPasswordView" frontend\src -S`：确认 `ForgotPasswordView` 和 `SiteConfigView` 有路由，另外三个视图当前未挂载。
4. PowerShell 扫描 `<el-button>`：确认找回密码页按钮没有点击处理；其它多行按钮命中为误报或已有事件。

### 下一步任务清单

1. 优先处理 `/forgot-password`：要么实现完整的找回密码安全流程，要么临时隐藏入口并明确说明暂不开放。
2. 处理 `/admin/site-config`：要么补齐真实站点配置模块，要么移除/重定向该隐藏占位路由。
3. 清理未挂载历史视图前需用户确认；如果保留，应注明用途，避免后续误以为功能已上线。
4. 后续若重新启用强制改密，应同步补齐后端接口、前端流程、权限校验和操作审计。

### 风险点

1. 找回密码属于安全高风险功能，不能用前端假提示或弱校验替代，必须有令牌、有效期、邮件发送、限流和审计。
2. 站点配置和操作日志如果面向管理员开放，必须接真实持久化和权限控制，不能停留在说明面板。
3. 本次为静态审查，未启动浏览器逐页点击验证，仍建议后续按菜单和路由做一次人工验收。

## 2026-06-29 更新：移除不需要的占位与遗留功能

- 状态：已按用户确认完成前端清理，并通过生产构建验证。
- 当前任务：移除审查结果中的 2、3、4 项，即站点配置占位页、未挂载遗留视图，以及账户安全未落地文案。
- 实现原则：只清理前端路由、视图文件和未使用文案；不修改数据库结构、不修改后端 DTO、不安装依赖。

### 实施内容

1. 移除 `/admin/site-config` 路由，避免手动访问到站点配置占位页。
2. 删除已确认不需要的遗留视图文件：`SiteConfigView.vue`、`AuditLogsView.vue`、`AuditManagementView.vue`、`SpecialOrdersView.vue`。
3. 清理中英文语言包中不再使用的 `siteConfig`、`auditLogs`、`passwordNormal`、`forcePasswordChange`、`passwordApiPending`、`adminSecurityHint` 文案。
4. 保留后端已有 `forcePasswordChange` 字段和前端登录态兼容字段，避免扩大到数据库和接口契约变更。
5. 保留管理员首页的“审核管理”快捷入口文案，因为它当前仍用于正常跳转到待审核订单入口。

### 修改文件

1. `frontend/src/router/index.ts`
2. `frontend/src/i18n/locales/zh-CN.ts`
3. `frontend/src/i18n/locales/en-US.ts`
4. `frontend/src/views/admin/SiteConfigView.vue`
5. `frontend/src/views/admin/AuditLogsView.vue`
6. `frontend/src/views/admin/AuditManagementView.vue`
7. `frontend/src/views/user/SpecialOrdersView.vue`
8. `PROJECT_PROGRESS.md`
9. `../PROJECT_PROGRESS.md`

### 验证结果

1. `rg -n "SiteConfigView|AuditLogsView|AuditManagementView|SpecialOrdersView|admin-site-config|site-config|menu\.siteConfig|menu\.auditLogs|passwordApiPending|passwordNormal|adminSecurityHint" frontend\src -S`：无残留命中。
2. `cmd /c npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
3. 构建仍存在既有非阻断提示：VueUse PURE 注释提示、主 chunk 和 `exceljs` chunk 超过 500k、npm 新版本提示；本次未调整依赖和构建配置。

### 下一步任务清单

1. 刷新管理员端，确认系统管理菜单中没有站点配置入口，手动访问 `/admin/site-config` 不再进入占位页。
2. 后续如果要彻底移除强制改密字段，需要单独设计数据库迁移、后端 DTO 变更和兼容策略。
3. 找回密码 `/forgot-password` 仍是待处理项；后续应优先决定实现完整安全流程，或隐藏入口。

### 风险点

1. 本次删除的是前端遗留文件和隐藏占位路由；如果外部文档曾直接引用 `/admin/site-config`，访问行为会变化。
2. 未删除后端 `forcePasswordChange` 字段是为了避免数据库结构变更；因此底层字段仍存在，但不再以未落地文案暴露给前端用户。

## 2026-06-29 更新：生成本地部署包

- 状态：已完成前端生产构建、后端打包测试和本地部署包生成。
- 当前任务：先按现有 `deploy` 目录模式生成可部署产物，不执行远程发布、不启动生产进程。
- 实现原则：不安装新依赖、不修改 `.env` 或秘钥配置、不修改数据库结构。

### 实施内容

1. 前端执行生产构建，生成最新 `frontend/dist`。
2. 后端执行 Maven package，运行测试并生成 Spring Boot 可执行 jar。
3. 将前端 dist 复制到带时间戳的部署目录，并压缩为 zip。
4. 将后端 jar 复制到 `deploy` 目录并使用同一时间戳命名。

### 部署产物

1. `deploy/youou-aso-frontend-20260629-194351/`
2. `deploy/youou-aso-frontend-20260629-194351.zip`
3. `deploy/youou-aso-backend-20260629-194351.jar`

### 验证结果

1. `cmd /c npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. `cmd /c mvn.cmd package`：通过，后端 117 个测试全部成功，生成 `youou-aso-backend-0.1.0-SNAPSHOT.jar`。
3. `rg -n "SiteConfigView|AuditLogsView|AuditManagementView|SpecialOrdersView|site-config|admin-site-config|passwordApiPending" deploy\youou-aso-frontend-20260629-194351 -S`：无残留命中。
4. 已确认前端 zip 大小约 854 KB，后端 jar 大小约 45.97 MB。
5. 构建仍存在既有非阻断提示：VueUse PURE 注释提示、chunk 超过 500k、npm 新版本提示；后端测试存在 ByteBuddy/JDK 动态代理提示、Flyway 对 MySQL 8.3 的版本支持提示、Spring Boot generated security password 提示。

### 下一步任务清单

1. 如要部署到正式服务器，需要确认目标服务器、Nginx/静态资源目录、后端启动方式和生产环境变量。
2. 生产启动后必须确认 `YOUOU_JWT_SECRET` 或 `YOUOU_JWT_SECRET_FILE` 已配置，数据库、Redis、日志路径均使用生产值。
3. 部署后访问前端并检查登录、应用管理、订单、财务、客服配置等核心页面。

### 风险点

1. 本次仅生成本地部署包，没有连接远程服务器，也没有替换线上运行中的服务。
2. 后端 package 测试使用本地 `local` profile，连接了本地 MySQL `youou_aso` 并确认 schema 当前为 18；正式环境迁移仍需单独确认备份和环境变量。

## 2026-06-29 更新：登录后误跳首页修复与前端重新打包

- 状态：已完成前端跳转逻辑修复、构建验证和新前端部署包生成。
- 当前任务：修复登录成功后可能因为 `redirect=/` 回到公开首页的问题。
- 实现原则：只调整前端登录跳转保护，不修改后端、不修改数据库、不安装依赖。

### 实施内容

1. 登录成功后不再无条件信任 `redirect` 参数。
2. 仅允许 `/user/` 或 `/admin/` 下的受保护路径作为登录后跳转目标。
3. 当 `redirect` 为空、为 `/` 或其它公开页面时，按账号类型跳转到 `/user/dashboard` 或 `/admin/dashboard`。

### 修改文件

1. `frontend/src/views/public/LoginView.vue`
2. `PROJECT_PROGRESS.md`
3. `../PROJECT_PROGRESS.md`

### 部署产物

1. `deploy/youou-aso-frontend-20260629-200428/`
2. `deploy/youou-aso-frontend-20260629-200428.zip`

### 验证结果

1. `cmd /c npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. 前端部署 zip 已生成，大小约 854 KB。
3. 构建仍存在既有非阻断提示：VueUse PURE 注释提示、chunk 超过 500k、npm 新版本提示。

### 下一步任务清单

1. 只需要重新部署新的前端 zip，不需要替换后端 jar。
2. 部署后清理浏览器缓存或使用无痕窗口重新测试登录。
3. 分别用普通用户和管理员账号确认登录后进入对应 dashboard。

### 风险点

1. 如果线上 Nginx 未正确代理 `/api` 到后端 8080，登录态仍可能无法建立；需要通过浏览器 Network 查看 `/api/auth/login` 是否返回 200。
## 2026-06-29 更新：Google Play 应用图标代理加载修复

- 状态：已完成代码调整与定向验证，待用户在真实页面刷新确认 Google Play 搜索结果图标显示。
- 当前任务：修复用户端添加应用时，Google Play 商店搜索结果文字可加载但图标在不开本地代理时无法显示的问题。
- 根因判断：Google Play 搜索结果的图标 URL 指向 Google 图片域名，前端原先直接通过浏览器 `<img>` 请求外链；在用户网络无法直接访问 Google 图片域名时图标失败，本地开启代理后可显示。
- 实现原则：不安装新依赖、不修改数据库结构、不修改路由结构、不改动 Google Play 搜索/保存业务流程，仅新增受限后端图标代理与前端 URL 转换。

### 实施内容

1. 新增后端 `AppIconProxyService`，只允许 `https` 且域名属于 Google 图片白名单的图标 URL。
2. 图标代理限制 URL 长度、响应类型、响应大小和网络超时，并将上游抓取异常收敛为业务错误，避免公开任意 URL 代理和 SSRF 风险。
3. 在用户应用接口下新增 `GET /api/customer/apps/icon-proxy?url=...`，仍要求客户登录后访问，避免成为公开图片代理。
4. 新增前端 `proxiedAppIconUrl` 工具，仅对 `GOOGLE_PLAY` 且属于 Google 图片域名的图标改走本站 `/api/customer/apps/icon-proxy`。
5. 更新用户端应用管理页的应用列表、搜索结果、已选应用三个图标展示点；App Store 和 iPad Store 图标仍保持原 URL。
6. 新增后端服务测试，覆盖允许 Google 图片域名和拒绝非白名单域名。

### 修改文件

1. `backend/src/main/java/com/youou/aso/modules/appmanagement/service/AppIconProxyService.java`
2. `backend/src/main/java/com/youou/aso/modules/appmanagement/api/CustomerAppController.java`
3. `backend/src/test/java/com/youou/aso/modules/appmanagement/service/AppIconProxyServiceTest.java`
4. `frontend/src/utils/appIconProxy.ts`
5. `frontend/src/views/user/ApplicationManagementView.vue`
6. `PROJECT_PROGRESS.md`
7. `../PROJECT_PROGRESS.md`

### 验证结果

1. `cmd /c mvn.cmd test -Dtest=AppIconProxyServiceTest`：先因 `AppIconProxyService` 未实现失败，确认测试覆盖新增行为。
2. `cmd /c mvn.cmd test -Dtest=AppIconProxyServiceTest`：实现后通过，2 个测试成功。
3. `cmd /c npm.cmd run build`：前端类型检查与生产构建通过。
4. `cmd /c mvn.cmd test "-Dtest=AppIconProxyServiceTest,YououAsoApplicationTests"`：通过，3 个测试成功，Spring 上下文可正常装配新服务。
5. 构建仍存在既有非阻断提示：VueUse PURE 注释提示、前端 chunk 超过 500k、npm 新版本提示；后端上下文测试仍出现既有 Flyway/MySQL 版本提示、Spring Boot generated security password 提示和 ByteBuddy 动态代理提示。

### 下一步任务清单

1. 刷新用户端 `/user/applications`，打开“添加应用”，选择 Google Play 后搜索应用，确认不开本地代理时搜索结果图标可显示。
2. 选择搜索结果并保存后，确认应用列表中的 Google Play 图标同样可显示。
3. 如线上服务器本身也无法访问 Google 图片域名，则需要为服务器出站网络配置受控代理或改为后端持久化缓存图标文件；该项属于部署/基础设施变更，需要单独确认。

### 风险点

1. 当前修复依赖后端服务器能够访问 Google 图片域名；如果服务器部署在无法访问该域名的网络环境，代理接口也会返回失败。
2. 图标代理已做域名白名单、协议限制、大小限制和登录保护，但仍会增加少量后端出站请求与带宽消耗；后续如访问量增加，建议改为持久化缓存或 CDN 化。
3. 本次只处理用户端应用管理页的 Google Play 图标；其它页面若仍直接展示已保存的 Google 图标 URL，可按同一工具函数逐步接入。
## 2026-06-29 更新：Google Play 后端出站代理配置支持

- 状态：已完成代码调整与后端定向验证，待在 IntelliJ 运行配置中设置代理环境变量后重启后端验证。
- 当前任务：根据运行截图修复 Google Play 添加应用时后端访问 `https://play.google.com/store/search` 超时的问题。
- 根因判断：浏览器本地代理只影响前端/浏览器请求，Spring Boot 后端 JVM 进程没有自动走浏览器代理；当前失败发生在 `HttpAppStoreVerifier.searchGooglePlay` 搜索阶段，早于图标显示和图标代理。
- 实现原则：不安装新依赖、不修改 `.env`、不硬编码本地代理端口、不修改数据库结构和业务流程；通过受控配置让生产/本地环境显式决定是否为商店出站请求启用代理。

### 实施内容

1. 新增 `StoreHttpClientFactory`，统一创建应用商店相关 `RestClient`。
2. `StoreHttpClientFactory` 支持读取 `youou.store.http-proxy-url`，也支持标准环境变量 `HTTPS_PROXY`、`https_proxy`、`HTTP_PROXY`、`http_proxy`。
3. 仅接受 `http`/`https` 代理 URL，且必须包含 host 和 port；无效代理配置会被忽略并输出警告，不会硬失败启动。
4. 将 `HttpAppStoreVerifier` 的 Google Play/App Store 搜索与校验请求改为使用统一工厂。
5. 将 `AppIconProxyService` 的图标抓取请求改为使用同一工厂，确保搜索和图标代理的网络路径一致。
6. 新增 `StoreHttpClientFactoryTest`，覆盖显式代理 URL、无效代理 URL 和无代理配置三种情况。

### 修改文件

1. `backend/src/main/java/com/youou/aso/modules/appmanagement/service/StoreHttpClientFactory.java`
2. `backend/src/main/java/com/youou/aso/modules/appmanagement/service/HttpAppStoreVerifier.java`
3. `backend/src/main/java/com/youou/aso/modules/appmanagement/service/AppIconProxyService.java`
4. `backend/src/test/java/com/youou/aso/modules/appmanagement/service/StoreHttpClientFactoryTest.java`
5. `PROJECT_PROGRESS.md`
6. `../PROJECT_PROGRESS.md`

### 验证结果

1. `cmd /c mvn.cmd test -Dtest=StoreHttpClientFactoryTest`：先因 `StoreHttpClientFactory` 未实现失败，确认测试覆盖新增行为。
2. `cmd /c mvn.cmd test "-Dtest=StoreHttpClientFactoryTest,AppIconProxyServiceTest"`：通过，5 个测试成功。
3. `cmd /c mvn.cmd test "-Dtest=StoreHttpClientFactoryTest,AppIconProxyServiceTest,YououAsoApplicationTests"`：通过，6 个测试成功，Spring 上下文可正常装配。
4. 后端测试仍存在既有非阻断提示：Flyway/MySQL 版本提示、Spring Boot generated security password 提示、ByteBuddy 动态代理提示。

### 下一步任务清单

1. 在 IntelliJ 后端 Run Configuration 中增加环境变量，例如：`YOUOU_STORE_HTTP_PROXY_URL=http://127.0.0.1:7890`，端口按本机代理实际监听端口填写。
2. 重启后端，启动日志中应出现 `Store HTTP client proxy enabled from youou.store.http-proxy-url`。
3. 重新进入用户端 `/user/applications` 添加应用，选择 Google Play 后搜索，确认后端不再出现 `play.google.com/store/search` 连接超时。
4. 如果线上部署也需要访问 Google Play，应在服务器环境变量中配置同类受控代理；不要把代理地址硬编码进源码。

### 风险点

1. 如果本地代理端口填写错误，后端仍会搜索失败，但日志会显示已启用代理来源，便于排查。
2. 如果代理需要用户名密码认证，当前实现不会处理认证信息；本地常见 HTTP 代理无需认证，生产如需认证应单独设计安全配置方式。
3. 代理只作用于应用商店校验、搜索和图标抓取相关 HTTP 客户端，不会改变数据库、认证、订单等其它模块的网络行为。
## 2026-06-29 更新：撤回 Google Play 后端代理方向并恢复原搜索链路

- 状态：已完成纠偏代码调整与验证，待用户在本地开代理场景重新确认 Google Play 搜索恢复。
- 当前任务：根据用户反馈撤回“支持后端代理配置”的错误方向，恢复原本开代理时可加载 Google Play 应用信息的行为。
- 用户反馈：需求是“不依赖本地代理也能显示”，不是“让后端支持代理”；上一轮改动导致原本开代理可用的路径也不可用。
- 纠偏原则：先恢复原有可用行为，不继续扩大代理方案；不删除文件，因全局规则禁止未经确认删除文件。

### 实施内容

1. `HttpAppStoreVerifier` 恢复为原来的 `SimpleClientHttpRequestFactory` 创建方式，不再通过 `StoreHttpClientFactory` 读取代理配置。
2. 用户端应用管理页恢复直接使用 `appIconUrl` 渲染图标，不再改写到 `/api/customer/apps/icon-proxy`。
3. `CustomerAppController` 移除新增的 `/api/customer/apps/icon-proxy` 入口和 `AppIconProxyService` 依赖。
4. `StoreHttpClientFactory` 和 `AppIconProxyService` 暂时保留为未接入业务链路的遗留文件，后续如用户明确允许删除，可再清理。
5. 明确剩余真实问题：如果后端和浏览器都无法访问 Google Play/Google 图片域名，又不使用任何代理或第三方数据源，就无法实时从 Google 官方页面抓取应用信息；生产可行方案应改为受控数据源、持久化缓存或用户手动录入/后台审核流程，需要单独确认。

### 修改文件

1. `backend/src/main/java/com/youou/aso/modules/appmanagement/service/HttpAppStoreVerifier.java`
2. `backend/src/main/java/com/youou/aso/modules/appmanagement/api/CustomerAppController.java`
3. `backend/src/main/java/com/youou/aso/modules/appmanagement/service/AppIconProxyService.java`
4. `backend/src/main/java/com/youou/aso/modules/appmanagement/service/StoreHttpClientFactory.java`
5. `frontend/src/views/user/ApplicationManagementView.vue`
6. `PROJECT_PROGRESS.md`
7. `../PROJECT_PROGRESS.md`

### 验证结果

1. `rg -n "icon-proxy|proxiedAppIconUrl|StoreHttpClientFactory httpClientFactory|AppIconProxyService appIconProxyService|youou.store.http-proxy-url|HTTP_PROXY|HTTPS_PROXY" backend\src\main\java frontend\src -S`：业务调用链路中不再存在新增代理接口引用或后端搜索代理工厂注入；仅剩未被引用的遗留文件内容。
2. `cmd /c mvn.cmd test "-Dtest=YououAsoApplicationTests,CustomerAppServiceTest"`：通过，9 个测试成功，Spring 上下文正常。
3. `cmd /c npm.cmd run build`：通过，前端类型检查与生产构建成功。
4. 构建仍存在既有非阻断提示：前端 VueUse PURE 注释提示、chunk 超过 500k、npm 新版本提示；后端 Flyway/MySQL 版本提示、Spring Boot generated security password 提示、ByteBuddy 动态代理提示。

### 下一步任务清单

1. 重启后端和前端，开本地代理后重新测试 Google Play 添加应用搜索，确认恢复到改动前可加载信息的状态。
2. 若目标是“不使用代理也能添加 Google Play 应用”，需要选择生产方案：
   - 后台维护应用元数据缓存/数据库，用户搜索本地缓存；
   - 接入一个当前网络可访问的官方或合规第三方应用元数据服务；
   - 允许用户手动填写包名、应用名、图标并进入后台审核。
3. 如确认要清理上一轮遗留文件，需要用户明确批准删除：`StoreHttpClientFactory.java`、`StoreHttpClientFactoryTest.java`、`AppIconProxyService.java`、`AppIconProxyServiceTest.java`、`frontend/src/utils/appIconProxy.ts`。

### 风险点

1. 当前只是恢复开代理可用路径；不开代理实时访问 Google Play 的网络可达性问题没有被源码层面消除。
2. 未删除遗留文件是为了遵守“禁止未经确认删除文件”的规则；这些文件未接入业务链路，但后续建议清理，避免误解。
3. 如果继续要求“不开代理也实时从 Google Play 获取信息”，在当前网络条件下不可保证，需要外部可访问数据源或本地持久化数据支撑。
## 2026-06-30 更新：用户端和管理员端支持手动添加应用

- 状态：已完成代码调整与定向验证，待用户在真实页面中确认搜索失败时的手动填写保存体验。
- 当前任务：应用添加流程增加“搜索填充 + 手动兜底填写”能力，解决 Google Play 因网络原因搜索/图标信息加载失败时无法添加应用的问题；管理员端同时支持给指定用户添加应用。
- 实现原则：不修改数据库结构、不安装依赖、不修改认证和订单架构；用户端只能给自己添加，管理员端可选择用户添加；原有搜索结果选择流程保留。

### 实施内容

1. 后端新增 `CreateManualCustomerAppCommand`，承载手动填写的商店、地区、App ID/Bundle ID、应用名称、图标 URL 和分类。
2. `CustomerAppService` 新增 `createManual`，跳过实时商店校验，但保留地区启用、商店支持、字段长度、必填项和重复应用校验。
3. 用户端 `POST /api/customer/apps` 在请求包含 `appName` 时走手动创建路径；不包含时保持原来的商店校验路径。
4. 管理员端 `POST /api/admin/apps` 新增创建接口，管理员可指定 `customerId` 并保存手动应用信息。
5. 用户端应用管理弹窗新增应用信息预览区，以及应用名称、App ID/Bundle ID、图标 URL 输入框；搜索结果点击后自动填充这些字段，搜索失败时可手动填写后保存。
6. 管理员端应用管理新增“添加应用”弹窗，包含用户选择、商店、地区、搜索、手动应用信息预览与保存。
7. 前端 API 类型扩展，新增 `createAdminAppForCustomer`。
8. 补充中英文文案，说明可搜索自动填充，也可因网络失败手动填写。

### 修改文件

1. `backend/src/main/java/com/youou/aso/modules/appmanagement/dto/CreateManualCustomerAppCommand.java`
2. `backend/src/main/java/com/youou/aso/modules/appmanagement/service/CustomerAppService.java`
3. `backend/src/main/java/com/youou/aso/modules/appmanagement/api/CustomerAppController.java`
4. `backend/src/main/java/com/youou/aso/modules/appmanagement/api/AdminCustomerAppController.java`
5. `backend/src/test/java/com/youou/aso/modules/appmanagement/service/CustomerAppServiceTest.java`
6. `backend/src/test/java/com/youou/aso/modules/appmanagement/api/CustomerAppControllerTest.java`
7. `backend/src/test/java/com/youou/aso/modules/appmanagement/api/AdminCustomerAppControllerTest.java`
8. `frontend/src/api/applications.ts`
9. `frontend/src/views/user/ApplicationManagementView.vue`
10. `frontend/src/views/admin/ApplicationManagementView.vue`
11. `frontend/src/i18n/locales/zh-CN.ts`
12. `frontend/src/i18n/locales/en-US.ts`
13. `../PROJECT_PROGRESS.md`
14. `PROJECT_PROGRESS.md`

### 验证结果

1. `cmd /c mvn.cmd test -Dtest=CustomerAppServiceTest`：通过，10 个测试成功；包含手动创建成功、应用名称为空拦截、原有自动校验与重复拦截。
2. `cmd /c mvn.cmd test "-Dtest=CustomerAppControllerTest,AdminCustomerAppControllerTest"`：通过，3 个测试成功；包含用户手动创建转发、管理员代用户创建、非管理员禁止。
3. `cmd /c npm.cmd run build`：通过，Vue TypeScript 检查和 Vite 生产构建成功。
4. `cmd /c mvn.cmd test "-Dtest=CustomerAppServiceTest,CustomerAppControllerTest,AdminCustomerAppControllerTest,YououAsoApplicationTests"`：通过，14 个测试成功，Spring 上下文正常装配。
5. 仍存在既有非阻断提示：前端 VueUse PURE 注释、chunk 超过 500k、npm 新版本提示；后端 Flyway/MySQL 版本提示、Spring Boot generated security password 提示、ByteBuddy 动态代理提示。

### 下一步任务清单

1. 在用户端 `/user/applications` 打开添加应用，模拟 Google Play 搜索失败后手动填写应用名称、Bundle ID 和图标 URL，确认可保存。
2. 在管理员端 `/admin/applications` 打开添加应用，选择用户后手动填写应用信息，确认应用出现在该用户名下。
3. 确认重复添加同用户、同商店、同地区、同 App ID/Bundle ID 时会被阻止。
4. 如后续需要降低人工填写错误率，可单独增加后台审核标记或应用信息来源字段；该项涉及数据结构扩展，需另行确认。

### 风险点

1. 手动添加路径不再强制实时访问 App Store 或 Google Play，因此应用名称、图标 URL 和 ID 的准确性依赖录入人员；这是为绕开网络不可达问题所做的受控兜底。
2. 图标 URL 仍由浏览器直接加载；如果填写的是 Google 图片域名且用户网络不可达，图标可能仍无法显示，但应用本身可以保存并用于下单。
3. 当前未新增“手动录入/自动校验”来源字段，因为不改数据库结构；如后续需要审计区分，需要单独设计迁移。
## 2026-06-30 更新：添加应用手动录入与图标上传优化

- 状态：已实现并通过本地验证，待用户验收。
- 当前任务：用户端和管理员端添加应用时，搜索失败也可以手动填写应用名称、App ID / Bundle ID，并上传应用图标后保存。
- 实现原则：不修改数据库结构、不安装新依赖、不改项目路由架构；图标不使用 base64 写入 `app_icon_url`，而是上传到受控目录并保存短 URL。

### 实施内容

1. 后端新增应用图标上传接口 `POST /api/app-icons`，登录用户可上传 PNG/JPG/WEBP/GIF 图标。
2. 后端新增静态资源映射 `/uploads/app-icons/**`，用于展示已上传的应用图标。
3. 后端限制图标大小默认 1MB，拒绝非图片类型，上传文件名使用 UUID，避免直接使用用户文件名。
4. 用户端添加应用弹窗移除手动信息卡片，改为普通表单项逐项展示：应用名称、App ID / Bundle ID、App 图标。
5. 管理员端添加应用弹窗同步改为普通表单项，并支持为用户添加应用时上传图标。
6. 前端新增图标预览、加载失败提示和本地图片上传按钮。
7. 前端本地开发代理新增 `/uploads`，保证开发环境可预览后端上传的图标。
8. 清理已不再使用的“图标 URL”录入文案。

### 修改文件

1. `backend/src/main/java/com/youou/aso/modules/appmanagement/api/AppIconUploadController.java`
2. `backend/src/main/java/com/youou/aso/modules/appmanagement/service/AppIconStorageService.java`
3. `backend/src/main/java/com/youou/aso/modules/appmanagement/dto/AppIconUploadResult.java`
4. `backend/src/main/java/com/youou/aso/config/UploadResourceConfig.java`
5. `backend/src/main/java/com/youou/aso/config/SecurityConfig.java`
6. `backend/src/test/java/com/youou/aso/modules/appmanagement/api/AppIconUploadControllerTest.java`
7. `frontend/src/api/applications.ts`
8. `frontend/src/views/user/ApplicationManagementView.vue`
9. `frontend/src/views/admin/ApplicationManagementView.vue`
10. `frontend/src/i18n/locales/zh-CN.ts`
11. `frontend/src/i18n/locales/en-US.ts`
12. `frontend/vite.config.ts`
13. `PROJECT_PROGRESS.md`
14. `../PROJECT_PROGRESS.md`

### 验证结果

1. `cmd /c mvn.cmd test -Dtest=AppIconUploadControllerTest`：通过，3 个测试全部成功。
2. `cmd /c mvn.cmd test "-Dtest=AppIconUploadControllerTest,CustomerAppServiceTest,CustomerAppControllerTest,AdminCustomerAppControllerTest,YououAsoApplicationTests"`：通过，17 个测试全部成功。
3. `cmd /c npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
4. 构建仍存在既有 VueUse PURE 注释提示、chunk 超过 500k 提示和 npm 新版本提示，均为非阻断问题，本次未处理。

### 下一步任务清单

1. 部署后确认后端运行用户对上传目录有写入权限。
2. 生产环境如需固定上传路径，可配置 `youou.upload.app-icon-dir` 指向持久化目录。
3. 部署前端后，在用户端和管理员端分别测试：搜索结果图标加载、图标加载失败后上传、自行手动填写并保存。
4. 如前端与后端不在同一域名下部署，需要确认 Nginx 将 `/uploads/app-icons/` 正确转发或静态映射到后端上传目录。

### 风险点

1. 当前图标上传保存到本机文件目录，生产环境需要保证目录持久化，避免重装或迁移服务时丢失文件。
2. 当前未引入对象存储或 CDN；如果后续上传量很大，建议迁移到对象存储并只保存 URL。
3. 上传接口限制了类型和大小，但仍需在网关或 Nginx 层同步限制请求体大小，避免无效大文件请求占用资源。
## 2026-06-30 更新：修复管理员端添加应用搜索 FORBIDDEN

- 状态：已修复并通过本地验证，待用户验收。
- 问题现象：管理员端“添加应用”弹窗中输入关键字搜索 Google Play 应用时，页面提示 `FORBIDDEN`。
- 根因：管理员端复用了用户端应用商店搜索接口 `/api/customer/apps/search`，该接口会校验 `CUSTOMER` 账号；管理员账号调用时被后端按权限拒绝。

### 实施内容

1. 后端在管理员应用控制器中新增 `GET /api/admin/apps/search`。
2. 新接口复用现有商店搜索服务，但只允许 `ADMIN` 账号调用。
3. 前端管理员端应用管理页改为调用管理员专用搜索接口。
4. 为管理员搜索接口补充单元测试：管理员可搜索，普通用户不可调用管理员搜索。

### 修改文件

1. `backend/src/main/java/com/youou/aso/modules/appmanagement/api/AdminCustomerAppController.java`
2. `backend/src/test/java/com/youou/aso/modules/appmanagement/api/AdminCustomerAppControllerTest.java`
3. `frontend/src/api/applications.ts`
4. `frontend/src/views/admin/ApplicationManagementView.vue`
5. `PROJECT_PROGRESS.md`
6. `../PROJECT_PROGRESS.md`

### 验证结果

1. `cmd /c mvn.cmd test -Dtest=AdminCustomerAppControllerTest`：通过，4 个测试全部成功。
2. `cmd /c npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
3. 构建仍存在既有 VueUse PURE 注释提示、chunk 超过 500k 提示和 npm 新版本提示，均为非阻断问题，本次未处理。

### 下一步任务清单

1. 部署新版后端和前端后，在管理员端添加应用弹窗重新测试 Google Play 搜索。
2. 同步测试 App Store / iPad Store 搜索，确认管理员端均不再走用户端搜索接口。
3. 如果线上仍出现 `FORBIDDEN`，检查后端服务是否已重启到包含 `/api/admin/apps/search` 的版本。

### 风险点

1. 管理员搜索接口仍依赖外部商店网络访问；网络不可达时会走搜索失败提示，用户仍可手动填写保存。
2. 如果只部署前端不部署后端，管理员端会请求不存在的 `/api/admin/apps/search`，需要前后端一起更新。
## 2026-06-30 更新：修复管理员端选择搜索结果后仍显示未找到

- 状态：已修复并通过本地验证，待用户验收。
- 问题现象：管理员端添加应用弹窗中，搜索结果已选择并回填表单后，下方仍显示“未找到对应应用”。
- 根因：管理员端选择搜索结果后会清空 `searchResults`，但页面空状态只判断 `hasSearched && searchResults.length === 0`，没有记录“已经选中应用”的状态，导致空状态误显示。

### 实施内容

1. 管理员端添加 `selectedApp` 状态，选择搜索结果后记录已选应用。
2. 管理员端选择结果后展示“已加载应用”信息块，行为与用户端保持一致。
3. 空状态判断改为 `hasSearched && searchResults.length === 0 && !selectedApp`。
4. 输入关键字、切换商店、切换地区、重置弹窗时同步清空已选应用状态。

### 修改文件

1. `frontend/src/views/admin/ApplicationManagementView.vue`
2. `PROJECT_PROGRESS.md`
3. `../PROJECT_PROGRESS.md`

### 验证结果

1. `cmd /c npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. 构建仍存在既有 VueUse PURE 注释提示、chunk 超过 500k 提示和 npm 新版本提示，均为非阻断问题，本次未处理。

### 下一步任务清单

1. 部署新版前端后，在管理员端添加应用弹窗搜索并选择应用，确认下方显示“已加载应用”而不是“未找到对应应用”。
2. 分别测试 Google Play、App Store、iPad Store 的选择回填和保存。

### 风险点

1. 本次仅修复管理员端弹窗 UI 状态，不涉及后端接口、数据库或权限逻辑。
2. 如果浏览器缓存旧前端资源，可能仍看到旧空状态，需要强制刷新或清理缓存。
## 2026-06-30 更新：账户设置增加修改密码功能

- 状态：已实现并通过本地验证，待用户验收。
- 当前任务：在用户端和管理员端的账户设置中增加修改密码能力，要求使用当前登录账号自己的旧密码校验后保存新密码。
- 实现原则：不修改数据库结构、不安装新依赖、不调整登录架构；后端只更新当前登录账号的密码哈希，避免越权修改其它账号。

### 实施内容

1. 后端新增 `PUT /api/auth/password` 接口，接收当前密码、新密码、确认新密码。
2. 后端按当前登录账号类型区分 `CUSTOMER` 和 `ADMIN`，分别更新 `customer_account` 或 `admin_account` 的 `password_hash`。
3. 修改密码前校验登录态、旧密码是否正确、新密码长度 8-72、两次新密码是否一致、新旧密码不能相同。
4. 新密码继续使用现有 `BCryptPasswordEncoder` 加密保存，成功后清除 `force_password_change` 标记。
5. 用户端账户设置页新增修改密码表单和保存按钮。
6. 管理员端账户设置页新增同样的修改密码表单和保存按钮。
7. 前端提交前先做确认密码一致、新旧密码不同的基础校验，并在成功后清空密码输入框。
8. 为服务层补充客户账号和管理员账号修改密码单元测试，并同步补齐受接口变更影响的测试 fake repository。

### 修改文件

1. `backend/src/main/java/com/youou/aso/modules/account/api/AuthController.java`
2. `backend/src/main/java/com/youou/aso/modules/account/dto/ChangePasswordCommand.java`
3. `backend/src/main/java/com/youou/aso/modules/account/service/AuthService.java`
4. `backend/src/main/java/com/youou/aso/modules/account/repository/AdminAccountRepository.java`
5. `backend/src/main/java/com/youou/aso/modules/account/repository/CustomerAccountRepository.java`
6. `backend/src/main/java/com/youou/aso/modules/account/repository/JdbcAdminAccountRepository.java`
7. `backend/src/main/java/com/youou/aso/modules/account/repository/JdbcCustomerAccountRepository.java`
8. `backend/src/test/java/com/youou/aso/modules/account/service/AuthServiceTest.java`
9. `backend/src/test/java/com/youou/aso/modules/order/service/OrderServiceTest.java`
10. `frontend/src/api/auth.ts`
11. `frontend/src/views/user/AccountSettingsView.vue`
12. `frontend/src/views/admin/AccountSettingsView.vue`
13. `frontend/src/i18n/locales/zh-CN.ts`
14. `frontend/src/i18n/locales/en-US.ts`
15. `PROJECT_PROGRESS.md`
16. `../PROJECT_PROGRESS.md`

### 验证结果

1. `cmd /c mvn.cmd test -Dtest=AuthServiceTest`：通过，9 个测试成功。
2. `cmd /c mvn.cmd test "-Dtest=AuthServiceTest,YououAsoApplicationTests"`：通过，10 个测试成功。
3. `cmd /c npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
4. 构建仍存在既有非阻断提示：VueUse PURE 注释提示、chunk 超过 500k 提示、npm 新版本提示；后端测试仍有既有 MySQL/Flyway、Spring Boot generated security password、ByteBuddy 动态代理提示，本次未处理。

### 下一步任务清单

1. 部署前后端后，在用户端账户设置页验证：旧密码错误会失败、两次新密码不一致会失败、正确修改后可用新密码登录。
2. 部署前后端后，在管理员端账户设置页验证同样的三种场景。
3. 如后续需要“修改密码后强制重新登录”或“踢掉其它会话”，需要单独设计 token 版本号或 token 黑名单机制，不能用临时前端清状态替代。

### 风险点

1. 当前修改密码成功后不会让已有 token 立即失效，这是沿用现有认证机制的结果；如果生产安全策略要求改密后立即失效，需要另行确认并设计。
2. 新增 repository 方法只更新密码哈希、强制改密标记和更新时间，不触碰账号其它字段，避免误覆盖账户资料。
3. 修改密码接口依赖当前登录身份，只允许修改自己的密码，不提供管理员代改他人密码入口，避免越权风险。
## 2026-06-30 更新：账户设置修改密码改为弹窗

- 状态：已调整并通过前端构建验证，待用户验收。
- 当前任务：修改上一版账户设置中“修改密码表单直接显示在页面上”的交互问题。
- 实现原则：只调整前端展示方式，不修改后端接口、权限逻辑、数据库结构或认证机制。

### 实施内容

1. 用户端账户设置页的“登录安全”面板不再直接显示当前密码、新密码、确认新密码输入框。
2. 管理员端账户设置页同步调整，不再把修改密码表单常驻显示在页面中。
3. “登录安全”面板仅保留操作按钮：修改密码、退出登录。
4. 点击“修改密码”后打开弹窗，在弹窗内填写当前密码、新密码、确认新密码并保存。
5. 弹窗关闭后自动清空密码输入，避免敏感内容残留在页面状态中。
6. 保留原有前端校验和后端接口调用逻辑，不改变修改密码业务规则。

### 修改文件

1. `frontend/src/views/user/AccountSettingsView.vue`
2. `frontend/src/views/admin/AccountSettingsView.vue`
3. `frontend/src/i18n/locales/zh-CN.ts`
4. `frontend/src/i18n/locales/en-US.ts`
5. `PROJECT_PROGRESS.md`
6. `../PROJECT_PROGRESS.md`

### 验证结果

1. `cmd /c npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. 构建仍存在既有非阻断提示：VueUse PURE 注释提示、chunk 超过 500k 提示、npm 新版本提示，本次未处理。

### 下一步任务清单

1. 部署前端后，在用户端账户设置页确认页面只显示“修改密码”和“退出登录”按钮。
2. 点击“修改密码”确认弹窗打开、校验提示正常、成功后弹窗关闭并清空输入。
3. 在管理员端账户设置页重复验证同样流程。

### 风险点

1. 本次仅改变 UI 展示方式，不改变后端安全规则；如果后续需要更严格的改密后 token 失效策略，仍需单独设计。
2. 弹窗关闭会清空输入，用户误关闭后需要重新填写，这是为了避免敏感内容残留。
## 2026-06-30 更新：找回密码邮箱验证码流程

- 状态：已实现并通过本地验证，待用户验收。
- 当前任务：实现登录页“忘记密码/找回密码”功能，采用邮箱验证码形式重置密码。
- 实现原则：生产可用优先；验证码持久化保存、只保存哈希；不把邮箱是否存在暴露给前端；SMTP 账号密码只通过配置读取，不写入源码。

### 实施内容

1. 后端新增 Flyway 迁移 `V19__password_reset_code.sql`，创建 `password_reset_code` 表保存验证码哈希、账号类型、账号 ID、过期时间、使用时间和错误次数。
2. 后端新增公开接口 `POST /api/auth/password-reset/code`，用于发送 6 位邮箱验证码。
3. 后端新增公开接口 `POST /api/auth/password-reset/confirm`，用于提交邮箱、验证码、新密码和确认密码后重置密码。
4. 后端验证码 10 分钟有效，最多允许 5 次错误尝试；同一邮箱 60 秒内重复请求不会重复发送，降低邮件成本和滥用风险。
5. 后端发送验证码时不向前端暴露邮箱是否存在；不存在的邮箱直接返回统一成功响应。
6. 后端新增 `spring-boot-starter-mail`，通过 `spring.mail.*` 和 `youou.mail.from` 配置 SMTP，不修改 `.env`，不硬编码密钥。
7. 后端重置成功后使用现有 BCrypt 加密保存新密码，并清除 `force_password_change` 标记。
8. 前端 `ForgotPasswordView.vue` 从静态页面改为完整邮箱验证码流程：邮箱、验证码、新密码、确认新密码、发送验证码倒计时和提交重置。
9. 前端 `auth.ts` 新增请求验证码和确认重置密码 API 方法。
10. 补充中英文前端文案和后端错误码文案。

### 修改文件

1. `backend/pom.xml`
2. `backend/src/main/resources/db/migration/V19__password_reset_code.sql`
3. `backend/src/main/java/com/youou/aso/modules/account/api/AuthController.java`
4. `backend/src/main/java/com/youou/aso/modules/account/service/AuthService.java`
5. `backend/src/main/java/com/youou/aso/modules/account/service/PasswordResetCodeGenerator.java`
6. `backend/src/main/java/com/youou/aso/modules/account/service/SecurePasswordResetCodeGenerator.java`
7. `backend/src/main/java/com/youou/aso/modules/account/service/PasswordResetMailSender.java`
8. `backend/src/main/java/com/youou/aso/modules/account/service/SmtpPasswordResetMailSender.java`
9. `backend/src/main/java/com/youou/aso/modules/account/service/UnavailablePasswordResetMailSender.java`
10. `backend/src/main/java/com/youou/aso/modules/account/domain/PasswordResetCode.java`
11. `backend/src/main/java/com/youou/aso/modules/account/dto/RequestPasswordResetCodeCommand.java`
12. `backend/src/main/java/com/youou/aso/modules/account/dto/ConfirmPasswordResetCommand.java`
13. `backend/src/main/java/com/youou/aso/modules/account/repository/PasswordResetCodeRepository.java`
14. `backend/src/main/java/com/youou/aso/modules/account/repository/JdbcPasswordResetCodeRepository.java`
15. `backend/src/main/java/com/youou/aso/config/SecurityConfig.java`
16. `backend/src/main/java/com/youou/aso/config/TimeConfig.java`
17. `backend/src/main/java/com/youou/aso/config/PasswordResetMailConfig.java`
18. `backend/src/main/java/com/youou/aso/common/error/ErrorCode.java`
19. `backend/src/main/resources/i18n/messages_zh_CN.properties`
20. `backend/src/main/resources/i18n/messages_en_US.properties`
21. `backend/src/test/java/com/youou/aso/modules/account/service/AuthServiceTest.java`
22. `frontend/src/api/auth.ts`
23. `frontend/src/views/public/ForgotPasswordView.vue`
24. `frontend/src/i18n/locales/zh-CN.ts`
25. `frontend/src/i18n/locales/en-US.ts`
26. `PROJECT_PROGRESS.md`
27. `../PROJECT_PROGRESS.md`

### 验证结果

1. 先执行 `cmd /c mvn.cmd test -Dtest=AuthServiceTest`，新增测试按预期失败，证明找回密码验证码能力尚未实现。
2. 实现后执行 `cmd /c mvn.cmd test -Dtest=AuthServiceTest`：通过，14 个测试成功。
3. 最终执行 `cmd /c mvn.cmd test "-Dtest=AuthServiceTest,YououAsoApplicationTests"`：通过，15 个测试成功，Spring 上下文正常启动，Flyway 迁移校验通过。
4. 最终执行 `cmd /c npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
5. 构建仍存在既有非阻断提示：VueUse PURE 注释提示、chunk 超过 500k 提示、npm 新版本提示；后端仍有既有 MySQL/Flyway 版本提示、Spring Boot generated security password 提示、ByteBuddy 动态代理提示，本次未处理。

### 下一步任务清单

1. 上线前配置 SMTP：`spring.mail.host`、`spring.mail.port`、`spring.mail.username`、`spring.mail.password`、`spring.mail.properties.mail.smtp.auth`、TLS/SSL 相关参数，以及 `youou.mail.from`。
2. 部署后在 `/forgot-password` 验证：发送验证码、60 秒内重复点击不重复发送、错误验证码 5 次后失效、验证码过期后失效、正确验证码可重置密码并登录。
3. 验证管理员邮箱和用户邮箱都可以通过统一流程重置密码。
4. 如后续需要更强防刷，应增加 IP 级别限流或网关限流；本次只做了邮箱级 60 秒冷却。

### 风险点

1. 如果生产环境未配置 SMTP，应用可启动，但发送验证码接口会返回“密码重置邮件服务不可用”。
2. 本次新增 Flyway 迁移会在后端启动时创建 `password_reset_code` 表；本地验证时数据库已迁移到 v19。
3. 当前改密后仍不会让旧 JWT 立即失效，沿用现有认证机制；如果要强制失效，需要单独设计 token 版本或黑名单。
4. 邮件内容目前为简单纯文本英文模板；如需品牌化或中英文模板，可后续单独优化。
## 2026-06-30 更新：添加应用默认图标占位改为上传样式

- 状态：已调整并通过前端构建验证，待用户验收。
- 当前任务：添加应用弹窗中无图标时不再默认显示字母 `A`，改为上传图标样式的占位。
- 实现原则：只调整用户端和管理员端添加应用弹窗的前端展示；不修改上传接口、保存逻辑、权限、数据库结构或生产配置。

### 实施内容

1. 用户端添加应用弹窗：无图标时显示 `UploadFilled` 上传占位图标，不再显示应用名首字母或默认 `A`。
2. 管理员端添加应用弹窗：同步使用相同上传占位图标。
3. 为无图标占位增加虚线边框和浅色背景，使其更接近上传入口的视觉表达。
4. 有真实图标 URL 时仍优先展示图片；图片加载失败时仍保留原有失败提示和上传按钮。

### 修改文件

1. `frontend/src/views/user/ApplicationManagementView.vue`
2. `frontend/src/views/admin/ApplicationManagementView.vue`
3. `PROJECT_PROGRESS.md`
4. `../PROJECT_PROGRESS.md`

### 验证结果

1. `cmd /c npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. 构建仍存在既有非阻断提示：VueUse PURE 注释提示、chunk 超过 500k 提示、npm 新版本提示，本次未处理。

### 下一步任务清单

1. 部署新版前端后，在用户端添加应用弹窗确认无图标时显示上传占位图标。
2. 在管理员端添加应用弹窗重复确认同样效果。
3. 上传真实图标后确认预览图片正常替换上传占位。

### 风险点

1. 本次仅调整前端占位样式，不影响应用保存和图标上传接口。
2. 线上如果仍看到字母 `A`，优先检查浏览器或静态资源缓存是否仍在使用旧前端包。
## 2026-06-30 更新：添加应用图标区域支持点击上传

- 状态：已调整并通过前端构建验证，待用户验收。
- 当前任务：添加应用弹窗中，应用图标区域本身也可以点击触发上传，不必只点击旁边的“上传图标”按钮。
- 实现原则：只调整用户端和管理员端前端交互；不修改上传接口、文件校验、保存逻辑、权限、数据库结构或生产配置。

### 实施内容

1. 用户端添加应用弹窗：将图标预览区域从普通展示元素改为按钮元素，点击后触发已有文件选择逻辑。
2. 管理员端添加应用弹窗：同步支持点击图标区域上传。
3. 上传中禁用图标区域点击，避免重复触发文件选择。
4. 为图标区域补充 hover、focus-visible 和 disabled 状态，保持键盘可访问性和交互反馈。
5. 保留原有“上传图标”按钮作为备用入口，不改变现有用户习惯。

### 修改文件

1. `frontend/src/views/user/ApplicationManagementView.vue`
2. `frontend/src/views/admin/ApplicationManagementView.vue`
3. `PROJECT_PROGRESS.md`
4. `../PROJECT_PROGRESS.md`

### 验证结果

1. `cmd /c npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. 构建仍存在既有非阻断提示：VueUse PURE 注释提示、chunk 超过 500k 提示、npm 新版本提示，本次未处理。

### 下一步任务清单

1. 部署新版前端后，在用户端添加应用弹窗点击图标占位，确认可打开文件选择。
2. 在管理员端添加应用弹窗重复验证同样交互。
3. 上传真实图标后再次点击预览图，确认可以重新选择替换图标。

### 风险点

1. 本次复用已有上传逻辑，不改变后端校验和保存行为。
2. 线上如果点击无效，优先确认静态资源是否已更新以及浏览器缓存是否清理。
## 2026-06-30 更新：前后端部署包重新打包

- 状态：已完成本地打包，待上传部署。
- 当前任务：基于当前代码重新生成后端 jar 和前端生产 zip，准备部署新版。
- 实现原则：只执行本地构建和打包；不修改 `.env`、不写入生产秘钥、不连接服务器覆盖文件、不重启线上服务。

### 实施内容

1. 在 `backend` 执行 Maven package，生成 Spring Boot 可运行 jar。
2. 将后端 jar 复制到 `deploy`，按时间戳命名。
3. 在 `frontend` 执行生产构建。
4. 将 `frontend/dist` 复制到 `deploy` 的时间戳目录，并压缩为前端部署 zip。
5. 检查前端 zip 内容，确认顶层包含 `index.html`，并包含 `assets` 静态资源目录内容。

### 生成文件

1. `deploy/youou-aso-backend-20260630-100744.jar`
2. `deploy/youou-aso-frontend-20260630-100744/`
3. `deploy/youou-aso-frontend-20260630-100744.zip`

### 验证结果

1. `cmd /c mvn.cmd package`：通过，后端 136 个测试全部成功，jar 生成成功。
2. `cmd /c npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
3. `Compress-Archive`：成功生成前端部署 zip。
4. 压缩包检查：确认 `index.html` 位于 zip 顶层，`assets` 条目数量为 77。
5. 构建仍存在既有非阻断提示：Flyway MySQL 版本提示、Spring Boot generated security password 测试提示、ByteBuddy 动态代理提示、VueUse PURE 注释提示、chunk 超过 500k 提示、npm 新版本提示，本次未处理。

### 下一步任务清单

1. 将后端 jar 上传到服务器对应 Java 项目位置，并按现有流程重启后端服务。
2. 将前端 zip 上传到宝塔前端站点目录，解压覆盖后确认 `index.html` 和 `assets` 位于站点根目录。
3. 部署后验证添加应用弹窗：无图标占位、点击图标上传、手动录入保存、管理员为用户添加应用。
4. 部署后验证找回密码页面；如果生产未配置 SMTP，发送验证码会提示邮件服务不可用。

### 风险点

1. 本次只生成本地部署包，没有实际连接服务器部署。
2. 新版后端包含 Flyway v19 迁移，生产后端首次启动会创建 `password_reset_code` 表。
3. 生产找回密码功能依赖 SMTP 环境配置；当前生产配置文件中尚未写入 SMTP 默认项。
4. 线上前端更新后可能受浏览器或 Nginx 缓存影响，需要强制刷新或清理缓存。
## 2026-06-30 更新：生产配置补充 SMTP 环境变量占位

- 状态：已补充并通过后端测试验证，待重新打包部署。
- 当前任务：`application-prod.yml` 中缺少找回密码邮件发送所需 SMTP 配置项，需要补充生产环境变量占位。
- 实现原则：只补充配置占位，不写真实邮箱、授权码或任何秘钥；SMTP 授权码使用 `YOUOU_MAIL_APP_PASSWORD` 命名，避免误解为邮箱登录密码。

### 实施内容

1. 在 `spring.mail` 下新增 SMTP host、port、username、password 和 SMTP auth/STARTTLS 配置。
2. 邮件发送凭证使用环境变量读取：`YOUOU_MAIL_USERNAME` 和 `YOUOU_MAIL_APP_PASSWORD`。
3. 发件人地址新增 `youou.mail.from`，通过 `YOUOU_MAIL_FROM` 环境变量注入。
4. SMTP 鉴权和 STARTTLS 支持环境变量覆盖：`YOUOU_MAIL_SMTP_AUTH`、`YOUOU_MAIL_STARTTLS_ENABLE`。
5. 未修改 `.env`、未写入真实邮箱账号、未写入真实授权码。

### 修改文件

1. `backend/src/main/resources/application-prod.yml`
2. `PROJECT_PROGRESS.md`
3. `../PROJECT_PROGRESS.md`

### 验证结果

1. `cmd /c mvn.cmd test "-Dtest=AuthServiceTest,YououAsoApplicationTests"`：通过，15 个测试全部成功。
2. 测试仍存在既有非阻断提示：Flyway MySQL 版本提示、Spring Boot generated security password 测试提示、ByteBuddy 动态代理提示，本次未处理。

### 下一步任务清单

1. 重新执行后端打包，使新的 `application-prod.yml` 进入 jar。
2. 生产环境配置 `YOUOU_MAIL_HOST`、`YOUOU_MAIL_PORT`、`YOUOU_MAIL_USERNAME`、`YOUOU_MAIL_APP_PASSWORD`、`YOUOU_MAIL_FROM`。
3. 如果使用 587 端口，通常保持 `YOUOU_MAIL_STARTTLS_ENABLE=true`；如果服务商要求 465 SSL，需要再确认是否补充 SSL 配置项。
4. 部署后在 `/forgot-password` 验证发送验证码。

### 风险点

1. 本次新增的是配置占位，不会让邮件自动可用；生产仍必须配置真实 SMTP 环境变量。
2. 已生成的 `youou-aso-backend-20260630-100744.jar` 不包含本次 prod 配置变更，需要重新打包后再部署。
3. 不同邮件服务商对 465/587、STARTTLS/SSL 的要求不同，部署前需要按实际邮箱服务商文档确认。
## 2026-06-30 更新：后端重新打包包含 SMTP 生产配置

- 状态：已完成本地打包，待上传部署。
- 当前任务：重新生成后端 jar，确保 `application-prod.yml` 中新增的 SMTP 环境变量占位进入部署包。
- 实现原则：只执行本地后端构建和 jar 复制；不修改 `.env`、不写入真实 SMTP 授权码、不连接服务器覆盖文件、不重启线上服务。

### 实施内容

1. 在 `backend` 执行 Maven package，重新生成 Spring Boot 可运行 jar。
2. 将新 jar 复制到 `deploy`，按时间戳命名。
3. 检查 jar 内 `BOOT-INF/classes/application-prod.yml`，确认包含 SMTP 环境变量占位。

### 生成文件

1. `deploy/youou-aso-backend-20260630-110909.jar`

### 验证结果

1. `cmd /c mvn.cmd package`：通过，后端 136 个测试全部成功，jar 生成成功。
2. 包内配置检查：确认新 jar 包含 `YOUOU_MAIL_HOST`、`YOUOU_MAIL_APP_PASSWORD`、`YOUOU_MAIL_FROM`。
3. 构建仍存在既有非阻断提示：Flyway MySQL 版本提示、Spring Boot generated security password 测试提示、ByteBuddy 动态代理提示，本次未处理。

### 下一步任务清单

1. 使用 `youou-aso-backend-20260630-110909.jar` 部署后端，替换之前的 `20260630-100744` 后端 jar。
2. 在生产环境配置 SMTP 环境变量后重启后端服务。
3. 部署后验证 `/forgot-password` 发送验证码。

### 风险点

1. 本次只生成后端 jar，没有重新生成前端包。
2. 生产 SMTP 未配置或配置错误时，找回密码发送验证码仍会失败。
3. 如果邮件服务商要求 465 SSL 直连，当前 STARTTLS 配置可能需要再补 SSL 配置项。
## 2026-06-30 更新：修复 163 邮箱 465 端口验证码发送失败

- 状态：已修复并重新打包后端，待部署验证。
- 问题现象：生产环境使用老系统 163 邮箱配置发送找回密码验证码失败。
- 根因：老系统使用 `smtp.163.com:465` 的 SSL 直连方式；新系统生产配置只有 STARTTLS 开关，没有 `mail.smtp.ssl.enable`，导致 465 场景下连接方式不匹配。

### 实施内容

1. 新增 `ProductionMailConfigTest`，要求生产邮件配置支持 `YOUOU_MAIL_SSL_ENABLE`。
2. 先运行新增测试并确认失败，证明生产配置缺少 SSL 配置项。
3. 在 `application-prod.yml` 中新增 `mail.smtp.ssl.enable`，通过 `YOUOU_MAIL_SSL_ENABLE` 环境变量控制，默认 `false`。
4. 重新打后端 jar，并检查 jar 内 `application-prod.yml` 包含 SSL 配置项。

### 修改文件

1. `backend/src/main/resources/application-prod.yml`
2. `backend/src/test/java/com/youou/aso/config/ProductionMailConfigTest.java`
3. `PROJECT_PROGRESS.md`
4. `../PROJECT_PROGRESS.md`

### 生成文件

1. `deploy/youou-aso-backend-20260630-114857.jar`

### 验证结果

1. `cmd /c mvn.cmd test -Dtest=ProductionMailConfigTest`：修复前按预期失败，缺少 `ssl:`。
2. `cmd /c mvn.cmd test "-Dtest=ProductionMailConfigTest,AuthServiceTest,YououAsoApplicationTests"`：通过，16 个测试成功。
3. `cmd /c mvn.cmd package`：通过，后端 137 个测试全部成功，jar 生成成功。
4. 包内配置检查：确认新 jar 包含 `YOUOU_MAIL_SSL_ENABLE`、`YOUOU_MAIL_STARTTLS_ENABLE`、`YOUOU_MAIL_APP_PASSWORD`。

### 下一步任务清单

1. 部署后端使用 `youou-aso-backend-20260630-114857.jar`。
2. 163 邮箱 465 端口生产环境变量建议配置：
   `YOUOU_MAIL_HOST=smtp.163.com`
   `YOUOU_MAIL_PORT=465`
   `YOUOU_MAIL_USERNAME=发件邮箱`
   `YOUOU_MAIL_APP_PASSWORD=SMTP授权码`
   `YOUOU_MAIL_FROM=发件邮箱`
   `YOUOU_MAIL_SMTP_AUTH=true`
   `YOUOU_MAIL_STARTTLS_ENABLE=false`
   `YOUOU_MAIL_SSL_ENABLE=true`
3. 重启后端后在 `/forgot-password` 重新发送验证码验证。

### 风险点

1. 如果授权码错误、发件邮箱未开启 SMTP、或服务器无法连通 `smtp.163.com:465`，发送仍会失败。
2. 本次只重新打后端 jar，没有重新生成前端包。
## 2026-06-30 更新：订单列表新增续单入口并预填创建页

- 状态：已完成前端实现并通过构建验证，待用户验收。
- 当前任务：对已支付及后续状态的普通订单增加“续单”操作；点击后跳转到创建订单页面，加载原订单内容，用户或管理员可修改后重新提交，新订单价格按当前表单内容和当前价格规则重新计算。
- 实现原则：不修改数据库结构，不新增支付/扣款接口，不继承原订单状态、订单号、执行记录或支付状态；取消订单不允许续单。

### 实施内容

1. 用户端订单列表新增“续单”按钮，仅普通订单且状态为 `PENDING_CONFIRM`、`PENDING_EXECUTION`、`EXECUTING`、`PAUSED`、`COMPLETED` 时显示。
2. 管理员端订单列表同步新增“续单”按钮，点击后进入管理员创建订单页，并带入原订单所属用户。
3. 用户端创建订单页支持 `renewOrderId` 查询参数：拉取原订单详情后只做表单预填，提交时走新建订单逻辑。
4. 管理员端创建订单页支持 `renewOrderId` 查询参数：预填用户、应用、地区、订单类型、日期、关键词/下载量/评分/评论等普通订单内容。
5. `CANCELLED`、`PENDING_PAYMENT`、特殊审核来源订单不显示续单入口，避免取消单误续和特殊订单内容无法完整还原。
6. 新增中英文文案：`续单/Renew` 与不可续单提示。

### 修改文件

1. `frontend/src/views/user/StoreOrdersView.vue`
2. `frontend/src/views/user/OrderCreateView.vue`
3. `frontend/src/views/admin/StoreOrdersView.vue`
4. `frontend/src/views/admin/OrderCreateView.vue`
5. `frontend/src/i18n/locales/zh-CN.ts`
6. `frontend/src/i18n/locales/en-US.ts`
7. `PROJECT_PROGRESS.md`
8. `../PROJECT_PROGRESS.md`

### 验证结果

1. `cmd /c npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. 构建仍存在既有非阻断提示：VueUse PURE 注释提示、chunk 超过 500k 提示、npm 新版本提示，本次未处理。

### 下一步任务清单

1. 部署新前端包后，在用户端订单列表验证已支付/执行中/已完成普通订单显示“续单”，待支付和已取消订单不显示。
2. 点击用户端“续单”后确认进入创建订单页，并正确预填应用、地区、日期、关键词/数量等内容。
3. 在管理员端订单列表验证“续单”入口，确认创建页能带入原订单用户与订单内容。
4. 修改续单页面中的数量或日期后，确认右侧金额重新按当前价格计算。

### 风险点

1. 特殊审核来源订单当前不显示续单入口，因为已生成订单只保存汇总明细，不能完整还原审核时的关键词排名或覆盖说明。
2. 如果原订单关联的应用已被删除或不再属于当前用户，创建页可能无法选中应用，需要后续按实际业务补充禁用应用提示。
3. 本次只做前端续单预填，不修改后端权限和创建逻辑；最终提交仍依赖现有创建订单接口校验。
## 2026-06-30 更新：续单支持特殊订单重新走审核/定价

- 状态：已修正并通过前端构建验证，待用户验收。
- 当前任务：特殊订单也要支持续单；续单时不复制已生成订单本身，而是回到特殊订单创建流程，用户端重新提交审核，管理员端重新进入代用户创建并可重新填写价格。
- 实现原则：不修改数据库结构，不新增后端接口，不复用旧订单状态/支付/执行记录；继续禁止 `CANCELLED` 订单续单。

### 实施内容

1. 用户端和管理员端订单列表的“续单”入口不再排除 `sourceAuditId` 订单，已支付及后续状态的特殊订单也会显示“续单”。
2. 用户端创建订单页识别续单订单的 `sourceAuditId` 后，拉取当前用户的特殊审核单列表，按原审核单回填特殊订单表单。
3. 用户端特殊订单续单会回填应用、商店、订单类型、地区、关键词、目标排名/覆盖说明、联系方式和需求描述；提交时重新生成一条审核单。
4. 管理员端创建订单页识别 `sourceAuditId` 后，拉取特殊审核单列表并回填用户、应用、商店、订单类型、地区、关键词和原协商价/订单金额；管理员仍可修改价格后提交。
5. 如果原审核单无法找到，会提示该订单当前不能续单，避免生成内容不完整的续单。

### 修改文件

1. `frontend/src/views/user/StoreOrdersView.vue`
2. `frontend/src/views/user/OrderCreateView.vue`
3. `frontend/src/views/admin/StoreOrdersView.vue`
4. `frontend/src/views/admin/OrderCreateView.vue`
5. `PROJECT_PROGRESS.md`
6. `../PROJECT_PROGRESS.md`

### 验证结果

1. `cmd /c npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. `rg` 静态检查确认用户端/管理员端均存在 `sourceAuditId` 续单加载、特殊审核单回填和续单入口。
3. 构建仍存在既有非阻断提示：VueUse PURE 注释提示、chunk 超过 500k 提示、npm 新版本提示，本次未处理。

### 下一步任务清单

1. 在用户端找一条已提交后的特殊订单，点击“续单”，确认进入特殊订单区域并回填原需求。
2. 用户端提交续单后，确认生成新的待审核特殊订单，而不是复用旧订单。
3. 管理员端点击特殊订单“续单”，确认用户、应用、特殊明细和价格正确回填，且价格可以修改。
4. 确认 `CANCELLED` 订单仍然不显示续单入口。

### 风险点

1. 特殊订单续单依赖原 `sourceAuditId` 对应的审核单仍可查询；如果历史数据缺失审核单，只能提示不可续单。
2. 管理员端特殊订单续单当前预填原协商价或原订单金额，管理员需要按实际情况确认后再提交。

## 2026-07-01 更新：修复英文窄屏创建订单类型挤压

- 状态：已完成前端样式修复并通过构建验证，待页面验收。
- 当前问题：英文模式下创建订单页面缩小屏幕后，订单类型 tab 文案过长，出现互相挤压和重叠。
- 页面布局分析：订单类型使用 Element Plus tabs，原样式为 6 等分网格；英文长标签在中等宽度下仍被压入同一行，导致相邻 tab 文案覆盖。
- 实现原则：只调整用户端和管理员端创建订单页局部样式，不改业务逻辑、不改文案、不改接口。

### 实施内容

1. 订单类型 tabs 改为可横向滚动的网格，保持每个类型有稳定最小宽度。
2. tab 文案允许换行并居中，避免英文长单词和长短语覆盖相邻项。
3. 移动端继续保留单列展示，避免小屏横向布局失控。
4. 用户端和管理员端创建订单页同步修复。

### 修改文件

1. `frontend/src/views/user/OrderCreateView.vue`
2. `frontend/src/views/admin/OrderCreateView.vue`
3. `PROJECT_PROGRESS.md`
4. `../PROJECT_PROGRESS.md`

### 验证结果

1. `cmd /c npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. 构建仍存在既有非阻断提示：VueUse PURE 注释提示、chunk 超过 500k 提示、npm 新版本提示，本次未处理。

### 下一步任务清单

1. 在英文模式下打开用户端创建订单页，缩小浏览器宽度，确认订单类型不再互相重叠。
2. 在英文模式下打开管理员端创建订单页，同样确认订单类型 tab 不重叠。
3. 检查中文模式下订单类型 tab 显示是否仍然正常。

### 风险点

1. 中等宽度下订单类型区域可能出现横向滚动条，这是为了避免英文长文案重叠。
2. 如果后续继续新增更长的订单类型名称，仍需按实际文案检查最小宽度是否足够。

## 2026-07-01 更新：订单执行明细导出字段严格对齐老系统

- 状态：已完成前端修复并通过构建验证，待页面验收。
- 当前任务：确保不同订单类型的单订单执行明细导出字段与老系统一致。
- 实现原则：不改后端接口、不改数据库、不新增导出字段；只调整前端 Excel 明细导出的字段结构和地区显示。

### 实施内容

1. 移除新系统通用兜底导出的 `item` 字段，避免出现老系统不存在的列。
2. `KEYWORD_INSTALL` 以及老系统未覆盖的新特殊类型统一使用老系统 `Keyword details` 字段：`Store`、`Date`、`App.Id/Bundle.Id`、`Area`、`keyword`、`count`、`time`。
3. `DOWNLOAD` 保持老系统字段：`Store`、`Date`、`App.Id/Bundle.Id`、`Area`、`count`。
4. `RATING` / `REVIEW` 保持老系统字段：`Store`、`Date`、`App.Id/Bundle.Id`、`Area`、`5 Stars`、`4 Stars`。
5. `Area` 导出值改为去掉页面展示中的括号 code，仅保留地区名称，贴近老系统导出。

### 修改文件

1. `frontend/src/utils/orderDetailExport.ts`
2. `PROJECT_PROGRESS.md`
3. `../PROJECT_PROGRESS.md`

### 验证结果

1. `rg` 静态检查确认 `orderDetailExport.ts` 中只剩老系统三套表头，不再存在 `Order details` 或 `item/count` 通用表头。
2. `cmd /c npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
3. 构建仍存在既有非阻断提示：VueUse PURE 注释提示、chunk 超过 500k 提示、npm 新版本提示，本次未处理。

### 下一步任务清单

1. 分别导出关键词安装、下载量、评分、评论订单，确认 Excel 表头与老系统一致。
2. 导出特殊订单，确认不会出现老系统外的新增字段列。
3. 检查 `Area` 列是否只显示地区名称，不再显示括号 code。

### 风险点

1. 新系统特殊订单没有老系统对应导出模板，当前统一套用 `Keyword details` 字段结构以保证字段一致。
2. 如果地区配置缺失，`Area` 仍会回退显示地区 code。

## 2026-07-01 更新：修复订单导出账户列为空

- 状态：已完成前端修复并通过构建验证，待页面验收。
- 当前问题：订单导出文件中“账号/账户”列没有数据。
- 根因分析：导出工具读取的是订单行上的 `customerEmail/customerUsername`；用户端特殊审核行构造时这两个字段为空，管理员端页面展示账户时使用了客户缓存兜底，但导出时直接使用原始订单行，没有复用展示兜底逻辑。
- 实现原则：不改后端接口、不改数据库、不改导出字段结构；仅在导出前生成补齐账户信息的数据副本。

### 实施内容

1. 用户端订单列表导出前使用当前登录账户的用户名/邮箱补齐账户列。
2. 管理员端订单列表导出前复用页面已有的 `orderCustomerName` / `orderCustomerEmail` 兜底逻辑。
3. 管理员端待确认订单导出前同样补齐账户信息。

### 修改文件

1. `frontend/src/views/user/StoreOrdersView.vue`
2. `frontend/src/views/admin/StoreOrdersView.vue`
3. `frontend/src/views/admin/PendingConfirmOrdersView.vue`
4. `PROJECT_PROGRESS.md`
5. `../PROJECT_PROGRESS.md`

### 验证结果

1. `cmd /c npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. 构建仍存在既有非阻断提示：VueUse PURE 注释提示、chunk 超过 500k 提示、npm 新版本提示，本次未处理。

### 下一步任务清单

1. 用户端订单列表点击导出，确认账号列显示当前用户邮箱或用户名。
2. 管理员端订单列表点击导出，确认账号列显示客户邮箱或用户名。
3. 管理员端待确认订单点击导出，确认账号列显示客户邮箱或用户名。

### 风险点

1. 如果某个历史订单对应客户已被删除，管理员端导出只能回退到 `用户ID`，无法补出邮箱。
2. 用户端特殊审核行没有订单详情中的客户字段，当前按当前登录账户补齐，符合用户端只能看自己订单的权限模型。

## 2026-07-01 更新：单个订单执行明细导出

- 状态：已完成前端实现并通过构建验证，待页面验收。
- 当前任务：参考老系统，为单个订单详情增加执行明细导出能力，用户端和管理员端都可导出。
- 实现原则：不改数据库、不改后端接口、不改生产配置；复用订单详情接口已有的 `items` 和 `commentDetails` 数据，在前端生成 `.xlsx` 文件。

### 实施内容

1. 新增 `orderDetailExport.ts`，统一生成单个订单执行明细 Excel。
2. 用户端订单详情页增加“导出明细”按钮，导出文件名为 `orderDetail-订单号-时间戳.xlsx`。
3. 管理员端订单详情页增加“导出明细”按钮，导出格式与用户端一致。
4. 参考老系统字段：
   - 关键词安装：`Store`、`Date`、`App.Id/Bundle.Id`、`Area`、`keyword`、`count`、`time`
   - 下载量：`Store`、`Date`、`App.Id/Bundle.Id`、`Area`、`count`
   - 评分/评论：`Store`、`Date`、`App.Id/Bundle.Id`、`Area`、`5 Stars`、`4 Stars`
5. 关键词安装按订单开始日期导出；下载量、评分、评论按订单日期范围逐日展开。
6. 对老系统未覆盖的新订单类型，导出通用明细表，避免无导出内容。

### 修改文件

1. `frontend/src/utils/orderDetailExport.ts`
2. `frontend/src/views/user/OrderDetailView.vue`
3. `frontend/src/views/admin/OrderDetailView.vue`
4. `frontend/src/i18n/locales/zh-CN.ts`
5. `frontend/src/i18n/locales/en-US.ts`
6. `PROJECT_PROGRESS.md`
7. `../PROJECT_PROGRESS.md`

### 验证结果

1. `cmd /c npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. 构建仍存在既有非阻断提示：VueUse PURE 注释提示、chunk 超过 500k 提示、npm 新版本提示，本次未处理。

### 下一步任务清单

1. 在用户端打开任意订单详情，点击“导出明细”，确认下载 `.xlsx` 且文件名包含 `orderDetail`。
2. 在管理员端打开同一订单详情，确认导出格式与用户端一致。
3. 分别用关键词安装、下载量、评分、评论订单验收字段和日期展开结果。

### 风险点

1. 新系统评分/评论详情中保存的是订单明细总量，老系统保存的是每日执行量；当前导出会按订单天数拆分为每日数量，可能存在四舍五入差异。
2. 新系统独有订单类型没有老系统对应模板，当前按通用明细导出。

## 2026-06-30 更新：续单前端生产包重新打包

- 状态：已完成本地前端生产构建与压缩包生成，待上传部署。
- 当前任务：基于续单及特殊订单续单前端改动重新生成生产前端包。
- 实施范围：仅前端构建与打包；未修改 `.env`、未修改后端代码、未上传服务器、未覆盖已有部署包。

### 生成文件

1. `deploy/youou-aso-frontend-20260630-120918/`
2. `deploy/youou-aso-frontend-20260630-120918.zip`

### 验证结果

1. `cmd /c npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. 压缩包检查：顶层包含 `index.html` 和 `assets\...` 静态资源，没有多套一层目录。
3. 构建仍存在既有非阻断提示：VueUse PURE 注释提示、chunk 超过 500k 提示、npm 新版本提示，本次未处理。

### 下一步任务清单

1. 将 `youou-aso-frontend-20260630-120918.zip` 上传到生产前端部署目录并解压替换静态资源。
2. 部署后在用户端和管理员端分别验证普通订单续单、特殊订单续单、`CANCELLED` 订单不显示续单入口。
3. 若浏览器仍显示旧页面，清理前端缓存或确认反向代理静态资源缓存策略。

### 风险点

1. 本次只生成前端包；后端包仍沿用 `deploy/youou-aso-backend-20260630-114857.jar`。
2. 未在真实生产环境执行页面回归，最终结果以部署后联调为准。

## 2026-06-30 更新：修复找回密码验证码邮件配置兼容性

- 状态：已修复并重新打包后端，待部署验证。
- 当前问题：生产环境找回密码验证码仍提示发送失败。
- 根因分析：后端生产配置只读取 `YOUOU_MAIL_APP_PASSWORD`，如果线上仍使用先前配置名 `YOUOU_MAIL_PASSWORD`，实际 SMTP 密码为空；同时 `youou.mail.from` 默认值为空，未配置 `YOUOU_MAIL_FROM` 时不会自动回退到 `YOUOU_MAIL_USERNAME`，会直接判定邮件服务不可用。
- 安全原则：未修改 `.env` 或任何秘钥文件；日志只记录异常类型和根因文本，不记录验证码、授权码或密码。

### 实施内容

1. `application-prod.yml` 中 `spring.mail.password` 改为兼容 `YOUOU_MAIL_APP_PASSWORD` 和旧变量 `YOUOU_MAIL_PASSWORD`。
2. `youou.mail.from` 改为未配置 `YOUOU_MAIL_FROM` 时回退到 `YOUOU_MAIL_USERNAME`。
3. `SmtpPasswordResetMailSender` 增加安全告警日志，用于区分发件人为空、`JavaMailSender` 缺失、SMTP 发送异常等原因。
4. `ProductionMailConfigTest` 增加生产邮件配置兼容性测试。

### 修改文件

1. `backend/src/main/resources/application-prod.yml`
2. `backend/src/main/java/com/youou/aso/modules/account/service/SmtpPasswordResetMailSender.java`
3. `backend/src/test/java/com/youou/aso/config/ProductionMailConfigTest.java`
4. `PROJECT_PROGRESS.md`
5. `../PROJECT_PROGRESS.md`

### 生成文件

1. `deploy/youou-aso-backend-20260630-122030.jar`

### 验证结果

1. `cmd /c mvn.cmd test -Dtest=ProductionMailConfigTest`：修复前按预期失败；修复后通过，2 个测试成功。
2. `cmd /c mvn.cmd package`：通过，后端 138 个测试全部成功，jar 生成成功。
3. jar 包内配置检查：确认包含 `YOUOU_MAIL_APP_PASSWORD:${YOUOU_MAIL_PASSWORD:}`、`YOUOU_MAIL_FROM:${YOUOU_MAIL_USERNAME:}`、`YOUOU_MAIL_SSL_ENABLE`。

### 下一步任务清单

1. 部署 `youou-aso-backend-20260630-122030.jar` 并确认启动参数包含 `--spring.profiles.active=prod`。
2. 163 邮箱 465 端口建议配置：`YOUOU_MAIL_HOST=smtp.163.com`、`YOUOU_MAIL_PORT=465`、`YOUOU_MAIL_USERNAME=发件邮箱`、`YOUOU_MAIL_APP_PASSWORD=SMTP授权码`、`YOUOU_MAIL_SSL_ENABLE=true`、`YOUOU_MAIL_STARTTLS_ENABLE=false`。
3. 部署后再次触发找回密码验证码；如仍失败，查看 `/opt/youou-aso/logs/application.log` 中 `Password reset mail send failed` 的根因日志。

### 风险点

1. 如果线上没有部署新 jar、没有启用 `prod` profile，或环境变量没有传入 Java 进程，验证码仍会失败。
2. 如果 163 授权码错误、SMTP 未开启、服务器无法连通 `smtp.163.com:465`，新包会在后端日志中暴露根因，但仍不会发送成功。

## 2026-06-30 更新：清理本地临时审计和构建产物

- 状态：已完成本地清理。
- 当前任务：清理项目中可再生成的临时文件，例如 `visual-audit`。
- 安全边界：未删除源码、配置、`.env`、`deploy` 部署包和 `node_modules` 依赖目录。

### 清理内容

1. 删除 `visual-audit/`，包含视觉审计截图、报告和 Chrome 调试 profile。
2. 删除 `frontend/dist/` 前端构建输出。
3. 删除 `backend/target/` Maven 构建输出。
4. 删除 `frontend/` 下 Vite 和 visual-audit 相关临时日志、pid 文件。

### 验证结果

1. 清理前统计：`visual-audit` 约 173.51 MB，`frontend/dist` 约 2.83 MB，`backend/target` 约 47.01 MB。
2. 清理后复查：上述目录和日志文件均不存在。
3. `deploy` 与 `frontend/node_modules` 已保留。

### 下一步任务清单

1. 后续如需本地运行前端，继续使用现有 `node_modules`，重新执行构建即可恢复 `dist`。
2. 后续如需后端 jar，重新执行 Maven package 即可恢复 `target`。

### 风险点

1. 清理后不能直接使用本地旧的 `dist` 或 `target`，需要重新构建。
2. 已生成的部署包仍保留在 `deploy`，不影响当前部署文件使用。

## 2026-07-01 更新：订单导出字段格式调整

- 状态：已完成前端实现并通过构建验证，待页面验收。
- 当前任务：订单导出按截图字段顺序输出，导出文件名必须包含 `order`。
- 实现原则：复用现有前端 CSV 导出方式；不改后端接口、不改数据库、不改订单查询逻辑。

### 实施内容

1. 新增统一订单导出工具 `orderExport.ts`，导出列固定为：账号、订单类型、商店类型、应用ID、订单开始日期、订单结束日期、订单总天数、订单状态、订单金额、联系方式、联系号码。
2. 用户端订单列表导出改为调用统一工具，文件名为 `order-时间戳.csv`。
3. 管理员端订单列表导出改为调用统一工具，文件名为 `order-时间戳.csv`。
4. 管理员待确认订单导出改为调用统一工具，文件名为 `pending-order-时间戳.csv`。
5. `联系方式`、`联系号码` 当前订单列表接口无对应字段，按截图保留为空列。

### 修改文件

1. `frontend/src/utils/orderExport.ts`
2. `frontend/src/views/user/StoreOrdersView.vue`
3. `frontend/src/views/admin/StoreOrdersView.vue`
4. `frontend/src/views/admin/PendingConfirmOrdersView.vue`
5. `PROJECT_PROGRESS.md`
6. `../PROJECT_PROGRESS.md`

### 验证结果

1. `cmd /c npm.cmd run build`：通过，Vue TypeScript 检查与 Vite 生产构建成功。
2. 静态核对：确认导出工具包含截图中的 11 个表头字段。
3. 静态核对：确认用户端订单列表、管理员订单列表、管理员待确认订单列表均调用 `exportOrdersCsv`，且文件名前缀包含 `order`。
4. 构建仍存在既有非阻断提示：VueUse PURE 注释提示、chunk 超过 500k 提示、npm 新版本提示，本次未处理。

### 下一步任务清单

1. 在用户端订单列表点击导出，确认 CSV 文件名包含 `order`，Excel 打开后字段顺序与截图一致。
2. 在管理员端订单列表点击导出，确认账号、应用ID、日期、状态和金额内容正确。
3. 在管理员待确认订单页点击导出，确认文件名为 `pending-order-时间戳.csv`，且字段顺序一致。

### 风险点

1. 当前仍是 CSV 文件，Excel 可直接打开；如后续要求真正 `.xlsx` 格式，需要另行改为 ExcelJS 导出。
2. 联系方式和联系号码暂无订单列表数据来源，当前只能保留空列。

## 2026-07-01 更新：特殊订单审核单号长度统一

- 状态：已完成代码修复，等待用户确认上线结果。
- 当前任务：修复特殊订单显示的订单号长度仍为旧格式的问题。
- 根因：普通订单号已统一使用 `BusinessNumberGenerator.generate("YO", now())`，格式为 `前缀 + yyMMdd + 6位后缀`，长度 14；特殊审核单号 `auditNo` 仍单独使用 `SA + yyyyMMddHHmmss + 6位后缀`，长度 22。订单列表会将特殊审核单的 `auditNo` 作为订单号展示，因此特殊订单看起来没有使用新长度。

### 实施内容

1. 将特殊审核单号生成逻辑改为复用公共业务编号生成器。
2. 保留 `SA` 前缀，生成格式统一为 `SAyyMMdd + 6位后缀`。
3. 补充特殊审核单提交测试，断言 `auditNo` 长度为 14 且匹配 `SA260620\d{6}`。
4. 未修改数据库结构、接口入参、前端显示结构或秘钥配置。

### 修改文件

1. `backend/src/main/java/com/youou/aso/modules/order/service/SpecialOrderAuditService.java`
2. `backend/src/test/java/com/youou/aso/modules/order/service/SpecialOrderAuditServiceTest.java`
3. `PROJECT_PROGRESS.md`
4. `../PROJECT_PROGRESS.md`

### 验证结果

1. `cmd /c mvn.cmd test "-Dtest=BusinessNumberGeneratorTest,SpecialOrderAuditServiceTest,OrderServiceTest"`：通过。
2. 测试结果：48 个测试通过，0 失败，0 错误，0 跳过。

### 下一步任务清单

1. 打包并部署后，在用户端和管理员端特殊订单列表确认新创建的特殊订单号长度为 14 位。
2. 如果历史特殊审核单仍显示旧长度，需要明确是否要做一次数据修正；本次未改历史数据，避免未经确认修改生产数据。

### 风险点

1. 本次只影响新创建的特殊审核单号，历史数据中的旧 `auditNo` 不会自动改变。
2. 编号后缀仍沿用现有 `System.nanoTime() % 1_000_000` 规则，保持与普通订单号一致；未额外调整并发唯一性策略。

## 2026-07-01 更新：特殊订单号修复后端打包

- 状态：已完成后端生产 jar 打包，等待部署验证。
- 当前任务：基于特殊订单审核单号长度修复，重新打包后端服务。
- 安全边界：未修改 `.env`、未修改秘钥配置、未执行服务器部署、未删除文件。

### 实施内容

1. 在 `backend` 执行 Maven 完整打包。
2. 将生成的 Spring Boot jar 从 `backend/target` 复制到 `deploy`。
3. 保留历史部署包，未覆盖旧包。

### 生成文件

1. `deploy/youou-aso-backend-20260701-093115.jar`

### 验证结果

1. `cmd /c mvn.cmd package`：通过。
2. 后端测试结果：138 个测试通过，0 失败，0 错误，0 跳过。
3. 部署包检查：`youou-aso-backend-20260701-093115.jar` 已生成，大小 46,752,644 bytes。

### 下一步任务清单

1. 将 `youou-aso-backend-20260701-093115.jar` 上传到生产服务器后端部署目录。
2. 使用生产配置启动，并确认 `--spring.profiles.active=prod` 生效。
3. 新建一个特殊订单，确认列表展示的特殊订单号为 14 位 `SAyyMMdd + 6位后缀`。
4. 历史特殊订单若仍是旧长编号，需要单独确认是否允许做数据修正。

### 风险点

1. 本次打包只生成本地 jar，未直接发布到服务器。
2. 历史特殊审核单号不会因新包自动变短。

## 2026-07-01 更新：当前前端改动生产包生成

- 状态：已完成前端生产构建和部署 zip 生成，等待部署验证。
- 当前任务：补充打包此前前端改动，包括订单导出、订单明细导出、应用管理添加应用调整、创建订单页英文窄屏布局等当前前端代码。
- 安全边界：未修改 `.env`、未安装新依赖、未执行服务器部署、未删除文件。

### 实施内容

1. 在 `frontend` 执行前端生产构建。
2. 将 `frontend/dist` 顶层内容压缩为部署 zip。
3. 保留历史前端部署包，未覆盖旧包。

### 生成文件

1. `deploy/youou-aso-frontend-20260701-093200.zip`

### 验证结果

1. `cmd /c npm.cmd run build`：通过。
2. 构建产物检查：`index.html` 位于 zip 顶层，`assets` 目录位于 zip 顶层。
3. 部署包检查：`youou-aso-frontend-20260701-093200.zip` 已生成，大小 866,286 bytes。
4. 构建存在既有非阻断提示：VueUse PURE 注释提示、部分 chunk 超过 500 kB、npm 新版本提示；本次未处理。

### 下一步任务清单

1. 将 `youou-aso-frontend-20260701-093200.zip` 上传到前端站点根目录并解压覆盖。
2. 同步部署后端 `youou-aso-backend-20260701-093115.jar`，避免前后端功能版本不一致。
3. 部署后检查订单导出、单订单明细导出、添加应用弹窗、创建订单页英文窄屏显示、特殊订单号展示。

### 风险点

1. 本次只生成本地前端 zip，未直接发布到服务器。
2. 前端静态资源可能受浏览器或 Nginx 缓存影响，部署后需要强制刷新或清缓存验证。

## 2026-07-02 更新：App Store 添加应用搜索支持 Apple ID 精准查询

- 状态：已完成代码修复，待后端测试/打包环境验证。
- 当前问题：添加应用弹窗中输入 Apple ID 后点击搜索，页面提示“未找到对应应用”；实际原因是搜索入口只调用 Apple Search API，把数字 Apple ID 当成关键词搜索，没有走 Apple Lookup API。
- 实现原则：不修改前端接口、不修改数据库、不修改保存数据结构；在后端 App Store verifier 内增强搜索识别逻辑。

### 实施内容

1. App Store / iPad Store 搜索入口新增 Apple ID、Bundle ID、App Store 链接识别。
2. 输入纯数字时优先调用 `lookup?id=...`。
3. 输入 App Store 链接时提取链接中的 `id` 后调用 `lookup?id=...`。
4. 输入 Bundle ID 格式时优先调用 `lookup?bundleId=...`。
5. 普通应用名称仍保留原有 `search?term=...` 搜索逻辑。
6. `verifyApple` 同步复用新的识别逻辑，使保存校验也兼容 App Store 链接。
7. 新增单元测试覆盖 Apple ID、App Store 链接、Bundle ID 与普通名称搜索判断。

### 修改文件

1. `backend/src/main/java/com/youou/aso/modules/appmanagement/service/HttpAppStoreVerifier.java`
2. `backend/src/test/java/com/youou/aso/modules/appmanagement/service/HttpAppStoreVerifierTest.java`
3. `PROJECT_PROGRESS.md`

### 验证结果

1. 已做静态核对：`searchApple` 会在普通名称搜索前先尝试 Apple lookup 参数识别。
2. 尝试执行 `cmd /c mvn.cmd test "-Dtest=HttpAppStoreVerifierTest,CustomerAppServiceTest"`，当前 Codex shell 中 `mvn.cmd` 不在 PATH，且项目没有 Maven wrapper，因此未能在本环境完成 Maven 测试。

### 下一步任务清单

1. 在具备 Maven 的环境执行 `mvn test -Dtest=HttpAppStoreVerifierTest,CustomerAppServiceTest`。
2. 后端测试通过后重新打包 jar 并部署。
3. 部署后在添加应用弹窗输入 Apple ID，例如 `1148524888`，确认可以搜索回填应用信息。
4. 同步验证应用名称搜索、Bundle ID 搜索和 App Store 链接搜索。

### 风险点

1. Apple Lookup 是否返回结果仍受地区参数影响；如果应用未在所选地区上架，仍可能查不到。
2. 当前变更只影响后端搜索与校验逻辑，线上必须部署新后端 jar 后才会生效。

## 2026-07-02 更新：用户端顶部余额与首页可用余额同步

- 状态：已完成前端修复并通过构建验证。
- 当前问题：用户端首页顶部栏“账户余额”显示 `$997`，首页卡片“可用余额”显示 `$947.00`，两个位置金额不一致。
- 根因分析：顶部栏余额在 `UserLayout` 挂载时单独请求一次，首页卡片在 `DashboardView` 刷新时又单独请求一次；下单扣款或页面局部刷新后，首页卡片拿到最新余额，但顶部栏仍保留旧值。
- 实现原则：不修改后端接口、不修改数据库、不改变金额计算口径；仅统一前端余额状态来源。

### 实施内容

1. 新增 `frontend/src/stores/wallet.ts`，用 Pinia 统一保存用户钱包概览。
2. `UserLayout` 顶部余额改为读取共享 wallet store。
3. `DashboardView` 首页余额卡片也改为读取同一个 wallet store。
4. 首页刷新拿到新余额后会写入共享 store，顶部栏同步更新。
5. 顶部栏点击“最新”刷新后，同样会更新首页余额卡片。

### 修改文件

1. `frontend/src/stores/wallet.ts`
2. `frontend/src/layouts/UserLayout.vue`
3. `frontend/src/views/user/DashboardView.vue`
4. `PROJECT_PROGRESS.md`

### 验证结果

1. 使用 bundled Node 执行 `vue-tsc --noEmit`：通过。
2. 使用 bundled Node 执行 `vite build`：通过。
3. 构建仍存在既有非阻断提示：VueUse PURE 注释提示、部分 chunk 超过 500 kB；本次未处理。

### 下一步任务清单

1. 部署新的前端构建产物后，登录用户端首页确认顶部余额和首页可用余额一致。
2. 下单扣款后刷新首页，确认两个位置同时变为扣款后的余额。
3. 点击顶部“最新”，确认首页卡片同步显示同一余额。

### 风险点

1. 本次只修复前端状态同步，不改变后端钱包余额计算。
2. 线上更新后如果浏览器缓存旧静态资源，仍可能短时间看到旧行为，需要强制刷新或清缓存。
## 2026-07-03 更新：下单成功邮件提醒

- 状态：已完成后端接入，并通过相关单元测试验证。
- 当前问题：新系统下单成功后没有邮件提醒，老系统会向配置的收件人发送“用户下单成功提醒”。
- 实现原则：复用现有 Spring Mail 配置；邮件发送失败只记录日志，不阻断用户下单、扣款或特殊订单提交流程。

### 实施内容

1. 新增订单通知发送接口与 SMTP 实现。
2. 普通用户创建订单成功后触发邮件提醒。
3. 待支付订单补款后重新提交成功时触发邮件提醒。
4. 特殊订单审核通过并提交正式订单后触发邮件提醒。
5. 邮件内容包含用户、订单号、订单类型、应用、商店、地区、金额和状态。
6. 新增生产/本地配置项 `youou.mail.order-notification-to`，可通过环境变量 `YOUOU_ORDER_NOTIFICATION_EMAILS` 配置收件人。
7. 未配置收件人、发件人或邮件组件时自动跳过发送，避免影响下单主流程。

### 修改文件

1. `backend/src/main/java/com/youou/aso/modules/order/service/OrderNotificationSender.java`
2. `backend/src/main/java/com/youou/aso/modules/order/service/SmtpOrderNotificationSender.java`
3. `backend/src/main/java/com/youou/aso/modules/order/service/NoopOrderNotificationSender.java`
4. `backend/src/main/java/com/youou/aso/config/OrderNotificationConfig.java`
5. `backend/src/main/java/com/youou/aso/modules/order/service/OrderService.java`
6. `backend/src/main/java/com/youou/aso/modules/order/service/SpecialOrderAuditService.java`
7. `backend/src/main/resources/application-prod.yml`
8. `backend/src/main/resources/application-local.yml`
9. `backend/src/test/java/com/youou/aso/modules/order/service/OrderServiceTest.java`
10. `backend/src/test/java/com/youou/aso/modules/order/service/SpecialOrderAuditServiceTest.java`

### 验证结果

1. `mvn.cmd test "-Dtest=OrderServiceTest,SpecialOrderAuditServiceTest,ProductionMailConfigTest"`：通过。
2. 测试结果：49 个测试通过，0 失败，0 错误，0 跳过。

### 下一步任务清单

1. 部署前配置环境变量 `YOUOU_ORDER_NOTIFICATION_EMAILS`，多个收件人用逗号分隔。
2. 确认生产环境已有 SMTP 相关环境变量或配置可用。
3. 重新打包并部署后端 jar 后，创建一笔订单验证收件邮箱能收到提醒。

### 风险点

1. 本次只完成代码与测试验证，尚未重新打包部署后端 jar。
2. 如果生产环境未配置收件人或 SMTP 信息，系统会跳过邮件发送但不会影响下单。
## 2026-07-03 更新：订单邮件提醒改为超级管理员后台配置

- 状态：已完成前后端接入，并通过后端测试与前端构建验证。
- 当前调整：订单邮件提醒保持老系统语义，发给后台/运营处理人，不发给下单用户；提醒开关和收件邮箱改为超级管理员在后台页面维护。
- 配置落点：新增数据库表 `order_notification_config`，字段包括 `recipients`、`enabled`、`updated_at`。
- 权限边界：前端菜单仅超级管理员可见；后端接口也校验 `SUPER_ADMIN`，普通管理员和用户不可读写。

### 实施内容

1. 新增 Flyway 迁移 `V20__order_notification_config.sql`，创建订单邮件提醒配置表。
2. 新增订单邮件提醒配置的 domain、repository、service、controller。
3. 订单邮件发送器改为实时读取数据库配置，页面保存后无需重启后端即可生效。
4. 移除收件人环境变量依赖；SMTP 发信账号仍复用已有邮件配置。
5. 后台系统管理新增“订单邮件提醒”页面，仅超级管理员显示。
6. 页面支持启用/停用提醒，收件邮箱支持换行、逗号、分号分隔。
7. 新增中英文文案和前端 API。

### 修改文件

1. `backend/src/main/resources/db/migration/V20__order_notification_config.sql`
2. `backend/src/main/java/com/youou/aso/modules/support/**/OrderNotificationConfig*`
3. `backend/src/main/java/com/youou/aso/modules/order/service/SmtpOrderNotificationSender.java`
4. `frontend/src/views/admin/OrderNotificationConfigView.vue`
5. `frontend/src/api/support.ts`
6. `frontend/src/layouts/AdminLayout.vue`
7. `frontend/src/router/index.ts`
8. `frontend/src/i18n/locales/zh-CN.ts`
9. `frontend/src/i18n/locales/en-US.ts`

### 验证结果

1. `mvn.cmd test "-Dtest=OrderNotificationConfigServiceTest,OrderNotificationConfigControllerTest,OrderServiceTest,SpecialOrderAuditServiceTest,ProductionMailConfigTest"`：通过。
2. 后端测试结果：57 个测试通过，0 失败，0 错误，0 跳过。
3. `npm.cmd run build`：通过。
4. 前端构建仍存在既有非阻断提示：VueUse PURE 注释提示、部分 chunk 超过 500 kB。

### 下一步任务清单

1. 重新打包并部署后端 jar，Flyway 会自动创建 `order_notification_config` 表。
2. 重新部署前端构建产物。
3. 使用超级管理员登录后台，在“系统管理 / 订单邮件提醒”中开启提醒并填写收件邮箱。
4. 创建一笔订单，确认运营收件邮箱收到“用户下单成功提醒”。
## 2026-07-03 更新：邮件与订单提醒配置纳入通用系统配置表

- 状态：已完成前后端调整，并通过后端测试与前端构建验证。
- 当前调整：不再使用专用 `order_notification_config` 表；改为新增通用 `system_config` 表，邮件相关配置以 key/value 形式保存。
- 配置范围：SMTP 主机、端口、账号、密码/授权码、发件邮箱、SMTP auth、STARTTLS、SSL、订单提醒开关、订单提醒收件人。
- 权限边界：邮件配置页面仅超级管理员可见；后端接口同样校验 `SUPER_ADMIN`。
- 兼容策略：如果 `system_config` 未配置 SMTP，仍兼容原有环境变量邮件配置；一旦后台保存 SMTP，则优先使用数据库配置。

### 系统配置 key

1. `mail.smtp.host`
2. `mail.smtp.port`
3. `mail.smtp.username`
4. `mail.smtp.password`
5. `mail.from`
6. `mail.smtp.auth`
7. `mail.smtp.starttls.enable`
8. `mail.smtp.ssl.enable`
9. `order.notification.enabled`
10. `order.notification.recipients`

### 实施内容

1. 新增 Flyway 迁移 `V20__system_config.sql`，创建通用系统配置表并初始化邮件相关 key。
2. 新增 `SystemConfigService` 与 JDBC repository，供后续其他系统配置复用。
3. 新增 `MailConfigService` 和 `ConfigurableMailSender`，订单提醒邮件和密码找回邮件都可读取数据库邮件配置。
4. 超级管理员后台新增“邮件配置”页面，支持维护 SMTP 与订单提醒。
5. 邮箱密码不回显；留空保存时保留已有密码。

### 验证结果

1. `mvn.cmd test "-Dtest=MailConfigServiceTest,MailConfigControllerTest,OrderServiceTest,SpecialOrderAuditServiceTest,ProductionMailConfigTest"`：通过。
2. 后端测试结果：58 个测试通过，0 失败，0 错误，0 跳过。
3. `npm.cmd run build`：通过。
4. 前端构建仍存在既有非阻断提示：VueUse PURE 注释提示、部分 chunk 超过 500 kB。

### 下一步任务清单

1. 重新打包并部署后端 jar，Flyway 会自动创建 `system_config` 表。
2. 重新部署前端构建产物。
3. 使用超级管理员登录后台，在“系统管理 / 邮件配置”中维护 SMTP 和订单提醒收件人。
4. 创建一笔订单验证运营提醒邮件；再验证找回密码邮件仍可发送。
## 2026-07-03 更新：订单邮件发送不再阻塞下单响应

- 状态：已完成后端修复，并通过相关测试。
- 问题现象：用户端提示提交订单失败，但数据库中订单与扣款流水已经生成。
- 根因判断：订单创建成功后同步发送订单提醒邮件；当前 SMTP 配置为 `smtp.163.com:465`，但 SSL 未启用，邮件连接可能拖慢到超过前端 15 秒请求超时，造成前端误判失败。

### 实施内容

1. 普通订单创建成功后，订单提醒改为事务提交后异步发送。
2. 特殊订单提交正式订单后，订单提醒同样改为事务提交后异步发送。
3. SMTP 连接、读、写超时设置为 5 秒，避免邮件服务拖住业务线程。

### 验证结果

1. `mvn.cmd test "-Dtest=OrderServiceTest,SpecialOrderAuditServiceTest,MailConfigServiceTest,MailConfigControllerTest"`：通过。
2. 测试结果：56 个测试通过，0 失败，0 错误，0 跳过。

### 下一步任务清单

1. 重启后端服务，让异步邮件修复生效。
2. 163 邮箱使用 465 端口时，建议在后台“邮件配置”中启用 SSL，并关闭 STARTTLS。
## 2026-07-03 更新：订单列表移除地区筛选

- 状态：已完成前端调整，并通过构建验证。
- 调整范围：后台订单列表、用户订单列表、后台待确认订单列表。

### 实施内容

1. 移除订单列表查询区的“地区”下拉筛选。
2. 移除订单列表请求参数中的 `regionCode`。
3. 移除特殊订单需求在列表中按地区过滤的逻辑。
4. 保留订单数据里的地区字段，不影响订单详情、导出和明细展示。

### 验证结果

1. `npm.cmd run build`：通过。
2. 构建仍存在既有非阻断提示：VueUse PURE 注释提示、部分 chunk 超过 500 kB。

## 2026-07-03 更新：批量导入兼容 UK 地区代码

- 状态：已完成前端调整，并通过构建验证。
- 问题判断：英国在系统地区数据中存在，代码为 `GB`；`UK` 是常见写法，但不是当前系统/App Store 使用的地区代码。
- 调整范围：用户创建订单页、后台创建订单页。

### 实施内容

1. 批量导入地区代码增加别名归一化。
2. 导入文件填写 `UK` 时自动转换为 `GB`，再校验当前应用商店是否支持该地区。
3. 系统保存和展示仍使用真实地区代码 `GB`。

### 验证结果

1. `npm.cmd run build`：通过。
2. 构建仍存在既有非阻断提示：VueUse PURE 注释提示、部分 chunk 超过 500 kB。

## 2026-07-03 更新：新增俄罗斯地区并兼容导入别名

- 状态：已完成前后端调整。
- 问题判断：当前地区种子数据缺少俄罗斯，导致 `RU` / `Russia` 均无法通过地区支持校验。
- 调整范围：地区 Flyway 数据、用户创建订单页、后台创建订单页。

### 实施内容

1. 新增 Flyway 迁移 `V21__add_russia_market_region.sql`，写入 `RU / 俄罗斯 / Russia`，并启用 App Store、Google Play、iPad Store 支持。
2. 批量导入地区代码增加别名：`Russia`、`Russian Federation`、`俄罗斯` 自动转换为 `RU`。
3. 系统保存和展示仍使用真实地区代码 `RU`。

### 验证结果

1. `mvn.cmd -DskipTests compile`：通过。
2. `npm.cmd run build`：通过。
3. `mvn.cmd test`：大部分单元测试已通过，但最终 SpringBoot 上下文测试因本地数据库 Flyway `V20` 校验和不一致失败；该问题是本地库已应用迁移与当前源码迁移文件不一致导致，非本次 `V21` 新增俄罗斯地区引起。

## 2026-07-03 更新：添加应用弹窗字段文案去重

- 状态：已完成前端调整，并通过构建验证。
- 问题现象：Google Play 添加应用时，搜索框占位符包含 Bundle ID/商店链接，下方手动标识字段仍显示 `App ID / Bundle ID`，视觉上像重复字段。
- 调整范围：用户应用管理页、后台应用管理页、中英文文案。

### 实施内容

1. 将搜索输入行标签从“应用”调整为“搜索应用”。
2. 手动标识字段按应用商店动态显示：Google Play 显示 `Bundle ID / 包名`，App Store 与 iPad Store 保持 `App ID / Bundle ID`。
3. Google Play 手动标识占位符调整为“请输入 Bundle ID 或包名”。
4. 搜索提示文案改为“应用标识”，避免再次堆叠 App ID / Bundle ID。

### 验证结果

1. `npm.cmd run build`：通过。
2. 构建仍存在既有非阻断提示：VueUse PURE 注释提示、部分 chunk 超过 500 kB。

## 2026-07-03 更新：修复本地 Flyway V20 校验不一致

- 状态：已完成本地开发库修复，并通过后端启动上下文测试。
- 问题现象：后端启动时报 `Migration checksum mismatch for migration version 20`。
- 原因判断：本地数据库已经执行过旧版 `V20__system_config.sql`，后续源码中的 `V20` 内容调整后，Flyway 历史表记录的 checksum 与当前源码不一致。

### 处理内容

1. 对本地 `youou_aso` 开发库执行 Flyway `repair`，将 `flyway_schema_history` 中 `V20` 的 checksum 更新为当前源码版本。
2. 重新运行后端 SpringBoot 上下文测试，Flyway 成功验证 21 个迁移。
3. 本地库已继续执行 `V21__add_russia_market_region.sql`，当前 schema 版本到 `21`。

### 验证结果

1. `mvn.cmd test "-Dtest=YououAsoApplicationTests"`：通过。
2. 仍存在 MySQL 9.7 下的既有非阻断提示：Flyway 建议升级、`VALUES()` 在 MySQL 未来版本中将弃用。

## 2026-07-03 更新：添加应用弹窗打开时刷新地区列表

- 状态：已完成前端调整，并通过构建验证。
- 问题现象：数据库和 `/api/regions/enabled?storeType=APP_STORE` 已返回 `RU / Russia`，但已经打开过的前端页面再次打开添加应用弹窗时仍查不到 Russia。
- 原因判断：应用管理页会把地区列表保存在页面内存中，新增地区后如果页面未刷新，弹窗仍使用旧列表。

### 实施内容

1. 用户端添加应用按钮改为调用 `openCreateDialog()`。
2. 用户端打开添加应用弹窗时重新加载当前应用商店支持地区。
3. 管理员端打开添加应用弹窗时也固定重新加载地区，不再只在列表为空时加载。

### 验证结果

1. `npm.cmd run build`：通过。
2. 接口验证：`/api/regions/enabled?storeType=APP_STORE` 已包含 `RU / Russia`。

## 2026-07-03 更新：关键词安装执行小时选项调整

- 状态：已完成前端调整，并通过构建验证。
- 调整范围：用户创建订单页、后台创建订单页。

### 实施内容

1. 关键词安装的执行小时下拉从连续 `1-8h` 改为固定选项：`1h`、`2h`、`4h`、`6h`、`8h`、`12h`、`16h`、`20h`、`24h`。
2. 用户端和后台端共用同一组固定选项。

### 验证结果

1. `npm.cmd run build`：通过。
2. 构建仍存在既有非阻断提示：VueUse PURE 注释提示、部分 chunk 超过 500 kB。

## 2026-07-03 更新：关键词行支持批量关键词导入

- 状态：已完成前端调整，并通过构建验证。
- 调整范围：用户创建订单页、后台创建订单页、中英文文案。

### 实施内容

1. 在关键词安装的每个地区关键词表中，新增“批量关键词”按钮。
2. 点击后弹出文本输入框，支持按 `关键词 空格 数量` 每行一条导入。
3. 导入后写入当前地区组，已存在的同名关键词会自动累加数量。
4. 增加格式校验、空内容提示和导入成功提示。

### 验证结果

1. `npm.cmd run build`：通过。
2. 构建仍存在既有非阻断提示：VueUse PURE 注释提示、部分 chunk 超过 500 kB。
