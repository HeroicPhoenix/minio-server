# 通用代码编写规范

## 1. 总体原则

### 1.1 分层职责清晰

系统代码应严格按照 `Controller`、`Service`、`Mapper`、`Entity`、`AO`、`VO` 等分层进行设计，各层职责明确，不得越层滥用。

### 1.2 单一职责

每一层只处理本层应负责的内容：

* `Controller` 负责接收请求、参数校验入口、日志记录、结果返回
* `Service` 负责业务逻辑、事务控制、业务校验、数据处理
* `Mapper` 负责数据库访问
* `Entity` 负责表结构映射
* `AO` 负责入参承载
* `VO` 负责出参封装

### 1.3 保持风格统一

命名、日志、异常、返回值、注解、SQL 编写风格必须统一，避免同一项目内出现多套写法。

### 1.4 模块组织例外

在保证职责清晰的前提下，`security`、`client`、`schedule`、`selector`、`spi` 等模块不要求放在 `config` 包下。

允许并推荐按业务语义独立成平级模块，与 `controller`、`service`、`mapper` 等并列，例如：

* `com.lvwyh.datax.web.security`
* `com.lvwyh.datax.web.client`
* `com.lvwyh.datax.web.schedule`
* `com.lvwyh.datax.web.selector`
* `com.lvwyh.datax.web.spi`

约束：

* `config` 包主要承载基础配置类（如 `@ConfigurationProperties`、配置装配类）
* 安全拦截、客户端调用、调度管理、策略选择、SPI 扩展等应优先放在对应语义模块
* 禁止为了“都放 config”而破坏模块边界与可维护性
* 各模块中的“配置类”可集中放在 `config` 文件夹下统一管理，例如：
  * `com.lvwyh.datax.web.config`
  * `com.lvwyh.datax.web.config.datasource`
  * `com.lvwyh.datax.web.config.security`
* 判断标准：凡是以配置装配为主职责的类（如 `@Configuration`、`@ConfigurationProperties`、`WebMvcConfigurer`、`Feign/线程池/数据源配置`）优先放 `config`；业务逻辑类仍放业务模块目录

### 1.5 新建表扩展字段规范

所有新建业务表必须预留 5 个扩展字段，命名与定义统一如下：

* `EXT_FIELD_1` `VARCHAR(255) DEFAULT NULL COMMENT '扩展字段1'`
* `EXT_FIELD_2` `VARCHAR(255) DEFAULT NULL COMMENT '扩展字段2'`
* `EXT_FIELD_3` `VARCHAR(255) DEFAULT NULL COMMENT '扩展字段3'`
* `EXT_FIELD_4` `VARCHAR(255) DEFAULT NULL COMMENT '扩展字段4'`
* `EXT_FIELD_5` `VARCHAR(255) DEFAULT NULL COMMENT '扩展字段5'`

约束：

* 该规则适用于后续新增的所有业务表（含历史表、队列表、流水表等）
* 这 5 个字段为预留字段，默认不承载核心业务逻辑
* 建表语句、初始化脚本、评审检查必须同步校验该规则

---

## 2. Controller 层规范

### 2.1 类注解规范

Controller 类固定使用以下注解风格：

```java
@Tag(name = "")
@Validated
@RestController
@RequestMapping("/api/...")
```

### 2.2 职责规范

Controller 层只负责：

* 接收请求参数
* 记录请求日志
* 调用 Service
* 组装返回结果

禁止在 Controller 中编写业务逻辑。

### 2.3 日志规范

Controller 中统一定义日志对象：

```java
private static final Logger log = LogManager.getLogger(XxxController.class);
```

日志要求：

* 使用英文日志模板
* 使用占位符 `{}` 输出参数
* 记录关键字段，不直接打印整个对象
* 成功日志中尽量记录主键 ID 或关键业务标识

示例：

```java
log.info("Update request: id={}, name={}", id, name);
```

### 2.4 返回值规范

统一使用项目标准返回结构，例如：

```java
ApiResponse.success("操作成功", data)
```

返回内容应简洁明确，优先返回主键 ID、关键结果对象或标准 VO。

### 2.5 参数与结果转换规范

* Controller 可以调用 Service 后组装为 VO 再返回
* 必要时可直接通过 VO 的构造方法进行转换，例如 `new XxxVO(entity)`
* 不要在 Controller 中进行复杂计算、路径处理、状态判断等业务逻辑

### 2.6 异常处理规范

Controller 层禁止手动 `try-catch` 处理业务异常并返回错误结果，统一交由全局异常处理器处理。

禁止写法：

```java
try {
    ...
} catch (Exception e) {
    return ApiResponse.fail(...);
}
```

正确写法：

```java
public ApiResponse<Long> create(...) {
    Long id = service.create(...);
    return ApiResponse.success(id);
}
```

---

## 3. AO 层规范

### 3.1 基本要求

AO 用于承载请求参数，必须：

* 实现 `Serializable`
* 使用 `@Schema` 描述字段含义
* 使用 JSR 校验注解进行参数校验

常用注解包括：

* `@NotNull`
* `@NotBlank`
* `@Min`
* `@Max`
* `@Size`

### 3.2 设计原则

* AO 只负责参数承载与基础校验
* 字段命名要与业务语义一致
* 校验信息要清晰可读

### 3.3 转换能力

AO 可以根据需要提供 `toEntity()` 方法，用于将 AO 转换为 Entity，减少重复转换代码。

示例：

```java
@Schema(description = "移动任务文件夹入参")
public class TaskFolderMoveAO implements Serializable {

    @Schema(description = "文件夹ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "文件夹ID不能为空")
    @Min(value = 1, message = "文件夹ID必须大于0")
    private Long taskFolderId;

    public TaskFolder toEntity() {
        TaskFolder entity = new TaskFolder();
        entity.setTaskFolderId(this.taskFolderId);
        return entity;
    }
}
```

---

## 4. Service 层规范

### 4.1 职责规范

Service 层负责：

* 业务校验
* 业务逻辑处理
* 状态流转
* 路径、层级、规则计算
* 调用 Mapper 完成数据库操作
* 公共逻辑提取与复用

### 4.2 事务规范

所有写操作方法必须添加事务注解：

```java
@Transactional(rollbackFor = Exception.class)
```

### 4.3 异常规范

业务异常统一抛出项目定义的业务异常，例如：

```java
throw new BusinessException("错误信息");
```

禁止直接抛出：

* `RuntimeException`
* `IllegalArgumentException`

### 4.4 入参与返回值规范

* Service 方法入参尽量使用有业务语义的明确字段，不直接传 AO
* Service 方法返回值尽量不使用 VO，可返回 Entity、DTO 或基础类型
* VO 的组装尽量放在 Controller 层处理

### 4.5 代码复用规范

对于重复出现的校验、路径处理、状态判断、树结构处理等公共逻辑，必须及时抽取为私有方法或通用方法，避免重复代码。

---

## 5. Mapper 与 XML 规范

### 5.1 Mapper 接口规范

* 方法命名应体现明确业务语义
* 多参数必须使用 `@Param`
* 优先考虑批量 SQL，避免无意义的单条循环操作

### 5.2 XML 编写规范

* 使用 `<resultMap>` 管理字段映射
* 抽取公共字段为 `<sql id="Base_Column_List">`
* SQL 关键字统一大写
* 查询条件中应考虑状态过滤、逻辑删除等业务限制

### 5.3 性能规范

对于批量更新、树结构更新、状态更新等场景，优先通过一条 SQL 完成，禁止在 Java 中循环逐条更新。

---

## 6. Entity 层规范

### 6.1 基本要求

Entity 必须严格对应数据库表结构，字段名称、类型、含义应与数据库定义保持一致。

### 6.2 设计原则

* Entity 仅用于数据库表结构映射
* 不在 Entity 中承载页面展示逻辑
* 不在 Entity 中堆积复杂业务逻辑

---

## 7. VO 层规范

### 7.1 职责规范

VO 用于接口返回结果封装，面向前端或调用方。

### 7.2 转换规范

VO 可以提供以 Entity 为参数的构造方法，便于在 Controller 中快速完成结果转换，例如：

```java
public XxxVO(XxxEntity entity) {
    ...
}
```

### 7.3 设计原则

* VO 字段应面向返回场景设计
* 可以与 Entity 不完全一致
* 避免将数据库字段原样暴露给前端而不加筛选

---

## 8. 日志规范

### 8.1 请求日志

记录关键请求参数，不要直接打印整个对象，避免日志冗余和敏感信息泄露。

### 8.2 成功日志

成功日志应记录主键 ID、业务单号或关键标识，便于问题追踪。

### 8.3 异常日志

异常场景应记录关键上下文信息，确保问题可定位。

### 8.4 日志语言

统一使用英文模板，格式保持一致。

### 8.5 日志级别要求

* 参数异常：`warn`
* 业务异常：`warn`
* 系统异常：`error`

---

## 9. 异常与统一返回规范

### 9.1 设计目标

系统必须实现统一的异常处理与返回结构，保证：

* 接口返回结构一致
* 业务异常与系统异常清晰分离
* 日志可追踪
* 前端可稳定解析

### 9.2 异常分类规范

| 类型   | 使用场景       | 示例            |
| ---- | ---------- | ------------- |
| 参数异常 | 入参校验失败     | `@Valid` 校验失败 |
| 业务异常 | 业务规则不满足    | 文件夹重名         |
| 系统异常 | 程序错误、不可控异常 | NPE、SQL 异常    |

### 9.3 BusinessException 规范

#### 9.3.1 定义要求

统一使用项目内定义的 `BusinessException`：

```java
package com.lvwyh.datax.api.common.exception;

/**
 * 业务异常
 *
 * 说明：
 * 1. 只用于业务规则错误
 * 2. 不用于系统异常（NPE、SQL异常等）
 */
public class BusinessException extends RuntimeException {

    private final int code;

    /**
     * 默认业务错误 400
     */
    public BusinessException(String message) {
        super(message);
        this.code = 400;
    }

    /**
     * 自定义错误码
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 带异常原因
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = 400;
    }

    /**
     * 带错误码 + 原因
     */
    public BusinessException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
```

#### 9.3.2 使用场景

只允许用于：

* 业务规则校验失败
* 状态不合法
* 权限不足的业务判断
* 数据不符合业务预期

示例：

```java
if (taskFolderId.equals(targetParentTaskFolderId)) {
    throw new BusinessException("不能将文件夹移动到自己下面");
}
```

#### 9.3.3 禁止事项

禁止用于：

* 捕获系统异常后无差别转为 `BusinessException`
* 替代 `RuntimeException` 作为通用异常
* 处理参数校验异常

#### 9.3.4 错误码规范

| 场景     | code |
| ------ | ---- |
| 通用业务错误 | 400  |
| 权限相关   | 403  |
| 资源不存在  | 404  |
| 方法不允许  | 405  |

建议写法：

```java
throw new BusinessException(404, "文件夹不存在");
```

### 9.4 GlobalExceptionHandler 规范

#### 9.4.1 必须统一使用全局异常处理

所有异常必须通过 `@RestControllerAdvice` 进行统一处理，禁止 Controller 自行处理异常返回。

标准实现如下：

```java
package com.lvwyh.datax.api.common.exception;

import com.lvwyh.datax.api.common.response.ApiResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;

/**
 * 全局异常处理
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LogManager.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<?> handleValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .findFirst()
                .orElse("参数错误");

        log.warn("参数校验异常：{}", message);
        return ApiResponse.fail(400, message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ApiResponse<?> handleConstraintViolationException(ConstraintViolationException e) {
        log.warn("参数校验异常：{}", e.getMessage());
        return ApiResponse.fail(400, e.getMessage());
    }

    @ExceptionHandler(BusinessException.class)
    public ApiResponse<?> handleBusinessException(BusinessException e) {
        log.warn("业务异常：{}", e.getMessage());
        return ApiResponse.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ApiResponse<?> handleJsonParseException(HttpMessageNotReadableException e) {
        Throwable cause = e.getMostSpecificCause();
        log.warn("请求JSON解析失败: {}", cause == null ? e.getMessage() : cause.getMessage());
        return ApiResponse.fail(400, "请求JSON格式错误，请检查字段格式");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ApiResponse<Void> handleHttpRequestMethodNotSupported(
            HttpServletRequest request,
            HttpRequestMethodNotSupportedException e) {
        log.warn("请求方法不支持：method={}, uri={}, from={}, message={}",
                request.getMethod(),
                request.getRequestURI(),
                request.getRemoteAddr(),
                e.getMessage());
        return ApiResponse.fail(405, "请求方法不支持");
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<?> handleException(Exception e) {
        log.error("系统异常：", e);
        return ApiResponse.fail(500, "系统内部错误");
    }
}
```

### 9.5 ApiResponse 统一返回规范

#### 9.5.1 标准结构

```json
{
  "code": 200,
  "status": true,
  "message": "操作成功",
  "data": {},
  "timestamp": 1710000000000
}
```

#### 9.5.2 标准实现

```java
package com.lvwyh.datax.api.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "统一接口返回结构")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    @Schema(description = "业务状态码", example = "200")
    private Integer code;

    @Schema(description = "是否成功", example = "true")
    private Boolean status;

    @Schema(description = "提示信息", example = "操作成功")
    private String message;

    @Schema(description = "实际返回数据")
    private T data;

    @Schema(description = "时间戳", example = "1710000000000")
    private Long timestamp;

    public ApiResponse() {
        this.timestamp = System.currentTimeMillis();
    }

    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> r = new ApiResponse<>();
        r.code = 200;
        r.status = true;
        r.message = "操作成功";
        r.data = data;
        return r;
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        ApiResponse<T> r = new ApiResponse<>();
        r.code = 200;
        r.status = true;
        r.message = message;
        r.data = data;
        return r;
    }

    public static <T> ApiResponse<T> fail(int code, String message) {
        ApiResponse<T> r = new ApiResponse<>();
        r.code = code;
        r.status = false;
        r.message = message;
        return r;
    }

    public static <T> ApiResponse<T> fail(String message, int code) {
        return fail(code, message);
    }

    public Integer getCode() {
        return code;
    }

    public Boolean getStatus() {
        return status;
    }

    public boolean isStatus() {
        return Boolean.TRUE.equals(status);
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public Long getTimestamp() {
        return timestamp;
    }
}
```

#### 9.5.3 使用原则

* Controller 层必须返回 `ApiResponse`
* 不允许直接返回对象或字符串
* 不允许使用 HTTP 状态码替代业务状态码

#### 9.5.4 成功返回规范

```java
ApiResponse.success(data);
ApiResponse.success("操作成功", data);
```

#### 9.5.5 失败返回规范

```java
ApiResponse.fail(code, message);
```

---

## 10. 数据与结构变更规范

### 10.1 更新职责边界清晰

不同操作必须职责单一，例如：

* 更新类操作只处理基础属性修改
* 移动类操作只处理父级、路径、层级等结构变更

禁止将不同职责混杂在同一个方法中。

### 10.2 树形或层级结构处理规范

涉及树结构、路径、层级、子树更新时：

* 优先使用 SQL 批量更新
* 禁止先查整棵子树再逐条循环更新
* 必须保证路径、层级、父级等字段更新一致性

### 10.3 特殊校验前置

涉及结构变更时，应提前校验：

* 不能操作自身
* 不能移动到自身子孙节点下
* 不能违反唯一约束
* 不能破坏层级结构

---

## 11. 性能规范

### 11.1 批量优先

批量更新、批量修改、树结构调整等场景必须优先使用 SQL 批处理。

### 11.2 禁止低效实现

禁止以下做法：

* Java 中 `for` 循环逐条更新数据库
* 不必要的重复查询
* 无意义对象转换和层层复制

### 11.3 公共查询与字段复用

公共字段、公共查询片段、公共结果映射应提取复用，避免重复维护。

---

## 12. 命名规范

### 12.1 类命名

* Controller：`XxxController`
* Service：`XxxService`
* Mapper：`XxxMapper`
* Entity：`Xxx`
* AO：`XxxAO`
* VO：`XxxVO`

### 12.2 方法命名

方法名应体现业务语义，避免使用无意义名称，如：

* `updateTaskFolder`
* `moveTaskFolder`
* `queryTaskFolderPage`

不要使用模糊命名，如：

* `doUpdate`
* `handle`
* `processData`

### 12.3 枚举命名

* 枚举类统一使用 `XxxEnum` 命名，如：`UserStatusEnum`
* 枚举项名称统一使用大写英文，如：`ENABLED`、`DISABLED`
* 枚举字段统一使用：`code`、`name`、`desc`
* 必须提供 `fromCode(Integer code)`，非法值统一抛出 `BusinessException`

---

## 13. 业务枚举与字典码规范

### 13.1 适用范围

所有“状态/类型/模式/开关”类业务值，必须使用标准枚举，不允许散落在代码中使用裸数字或字符串。

示例（必须枚举化）：

* 用户状态：`1/2`
* 角色状态：`1/2`
* 权限状态：`1/2`
* 菜单状态：`1/2`

### 13.2 放置位置

* 通用业务枚举放在 `datax-web/src/main/java/com/lvwyh/datax/web/enums`
* 子域枚举放在二级目录，如：`enums/system`、`enums/task`、`enums/trigger`
* 禁止把枚举放入 `util`、`service`、`entity` 包

### 13.3 代码使用规范

* Service/Controller/Interceptor 中涉及状态判断时，必须使用枚举：
  * 正确：`UserStatusEnum.ENABLED.getCode().equals(user.getStatus())`
  * 禁止：`user.getStatus() == 1`
* 禁止在类顶部定义状态常量替代枚举（如 `private static final int ENABLED = 1`）
* 对外入参若是状态码，必须通过 `fromCode` 做合法性约束

### 13.4 SQL 字典同步规范

每新增一个业务枚举，必须同步在 `init.sql` 的 `CODE_CLS_VAL` 增加对应字典值，写法保持项目统一格式：

```sql
INSERT INTO CODE_CLS_VAL (ID, CODE_CLS, CODE_CLS_CODE, CODE_CLS_NAME, CODE_CLS_DESC, SORT_NUM) VALUES
(NULL,'UserStatus','1','ENABLED','启用',1),
(NULL,'UserStatus','2','DISABLED','禁用',2);
```

要求：

* `CODE_CLS` 与枚举业务语义一致（建议去掉 `Enum` 后缀）
* `CODE_CLS_CODE` 与枚举 `code` 一致
* `CODE_CLS_NAME` 与枚举 `name` 一致
* `CODE_CLS_DESC` 与枚举 `desc` 一致
* `SORT_NUM` 必须从 1 开始递增

### 13.5 枚举模板规范

```java
public enum UserStatusEnum {
    NORMAL(1, "NORMAL", "正常"),
    DISABLED(2, "DISABLED", "禁用"),
    DELETED(3, "DELETED", "逻辑删除");

    private final Integer code;
    private final String name;
    private final String desc;

    UserStatusEnum(Integer code, String name, String desc) {
        this.code = code;
        this.name = name;
        this.desc = desc;
    }

    public Integer getCode() { return code; }
    public String getName() { return name; }
    public String getDesc() { return desc; }

    public static UserStatusEnum fromCode(Integer code) {
        for (UserStatusEnum e : values()) {
            if (e.code.equals(code)) {
                return e;
            }
        }
        throw new BusinessException("非法用户状态: " + code);
    }
}
```

---

## 14. 禁止事项

开发中禁止以下行为：

* 在 Controller 中编写业务逻辑
* 直接抛出 `RuntimeException` 或 `IllegalArgumentException`
* 随意改变统一日志风格
* 在 Service 中直接返回页面展示用 VO
* 在 Java 中循环逐条更新大量数据
* 不经设计随意破坏现有层次结构和职责边界
* Controller 捕获异常并自行返回错误
* 返回非 `ApiResponse`
* 混用多种返回结构
* 使用异常做流程控制

---

## 15. 持续开发要求

后续开发必须持续遵循以下原则：

* Controller 只负责入参与返回
* Service 负责核心业务逻辑
* Mapper/XML 负责高效 SQL 实现
* 复杂结构变更优先考虑 SQL 批量更新
* 统一日志、异常、返回值和命名风格
* 业务异常统一使用 `BusinessException`
* 所有异常统一走 `GlobalExceptionHandler`
* 所有接口统一使用 `ApiResponse`

---

## 16. 最佳实践总结

必须遵守以下核心原则：

* 分层清晰，职责单一
* Controller 不写业务逻辑
* Service 承担核心业务处理
* Mapper/XML 优先使用批量 SQL
* 统一日志模板与日志级别
* 统一业务异常类型
* 统一全局异常处理
* 统一接口返回结构
* 禁止低效实现与风格漂移

---

## 17. PageResult 分页规范

### 17.1 统一分页返回结构

分页查询必须统一使用：

```java
com.lvwyh.datax.core.common.util.PageResult<T>
```

字段含义：

* `total`：总记录数
* `pageNum`：当前页码（从 1 开始）
* `pageSize`：每页条数
* `list`：当前页数据列表

### 17.2 Controller 层写法

* 入参统一接收 `pageNum`、`pageSize`，并使用 JSR 注解校验范围
* Controller 只负责参数接收、日志记录、调用 Service、返回 `ApiResponse<PageResult<VO>>`
* 实体到 VO 分页转换统一使用 `ConvertUtils.convertPage(...)`

### 17.3 Service 层写法

* 统一兜底分页参数：
  * `pageNum <= 0` 时按 `1` 处理
  * `pageSize <= 0` 或超上限时按默认值（建议 `10`）处理
* 统一计算偏移量：`offset = (pageNum - 1) * pageSize`
* 先查总数再查列表，最后返回：

```java
return new PageResult<>(total, safePageNum, safePageSize, list);
```

### 17.4 Mapper/XML 规范

* Mapper 接口分页方法固定包含 `offset`、`pageSize`
* XML 中分页查询与总数查询必须保持同一过滤条件
* 逻辑删除场景下，默认分页查询应过滤“逻辑删除”状态（除非明确传入该状态）
