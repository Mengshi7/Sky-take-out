# 本地模拟支付

仅用于本机课程练习，不会产生微信交易，不需要商户私钥或平台证书。

## 启动

1. 使用 JDK 8 启动 `sky-server`，仅激活 `dev` Profile。
2. `application-dev.yml` 已设置 `sky.payment.mock-enabled: true` 和 `server.address: 127.0.0.1`。
3. 关闭 cpolar 等公网隧道。仅激活 dev 并不能阻止隧道转发本机端口。
4. 微信开发者工具导入 `C:/002-Study/01-Development/TakeOut/资料/day06/微信小程序代码/mp-weixin/mp-weixin`。
5. 本地开发时启用开发者工具的“不校验合法域名”选项，请求地址保持 `http://localhost:8080`。

小程序 `common/vendor.js` 的 `mockPaymentEnabled` 已显式开启。当前只有编译后的微信工程，重新从其他 Vue 源码构建会覆盖这些修改。

## 调用链

`PUT /user/order/payment` → 用户 JWT 拦截器 → `OrderController.payment()` → `OrderServiceImpl.payment()` → `OrderMapper.getByNumberAndUserId()` → `OrderMapper.payMock()`。

仅允许当前用户的待付款、未支付订单更新为待接单、已支付，并填写结账时间。条件 UPDATE 返回一行才算成功；重复支付报错，不重写结账时间。模拟响应不提供伪造的微信签名或交易号。

小程序同时检查本地配置、后端模拟标识、订单号及付款状态，然后跳转原有 `/pages/success/index?orderId=...`。失败不跳转，真实响应继续调用 `wx.requestPayment()`。

## 手动验证

1. 登录后添加购物车并下单：确认 orders 和 order_detail 已保存，购物车已清空。
2. 点击支付：确认成功页出现，订单 `status=2`、`pay_status=1`、`checkout_time` 已填写。
3. 重复请求同一支付接口：应返回业务错误，结账时间不变。
4. 请求不存在订单、用另一用户请求该订单：均应失败。
5. 请求已取消或已完成订单：应失败；缺少 JWT 应返回 401。
6. 用两个请求并发付款：最多一次条件更新成功。
7. 在非 dev、混合 Profile 或绑定公网地址时开启模拟开关：应拒绝启动。

以上数据库和开发者工具验证需要在实际运行环境执行，单元测试不代表已完成这些验证。

## 自动测试

在工程根目录使用 JDK 8 和 Maven：

```text
mvn -pl sky-server -am test -Dtest=OrderPaymentTest,PaymentConfigurationTest -Dsurefire.failIfNoSpecifiedTests=false
node sky-server/src/test/js/payment.test.js
```

测试使用 Mapper、微信工具和小程序 API 替身，不执行真实支付、退款或数据库写入。

## 后续 Day09 作业

当前工程尚未实现 Day09 的查询、取消、接单、派送和完成接口。模拟订单使用原有 Orders 常量，后续课程实现可按原状态处理。

接入用户取消、商家拒单和商家取消时，必须在真实退款调用前增加同一开发开关保护的分支：模拟模式只更新退款及取消状态，正式模式保留 `WeChatPayUtil.refund()`。当前没有这些业务入口，因此没有提前新增退款接口，也不能直接复制完整版退款调用后用于模拟订单。

请使用独立本地测试数据库；不要将模拟订单混入真实支付数据库。

## 关闭模拟模式

后端设置 `sky.payment.mock-enabled: false`，小程序设置 `mockPaymentEnabled = false`，即恢复原有微信支付调用。真实支付需要合法商户配置及证书，本次没有验证真实支付流程；模拟订单不应进行真实退款。

`application-dev.yml` 和测试目录被现有 `.gitignore` 忽略，复制或备份工程时请一并保留。
