# 子模块代码模板

占位符约定：`${Name}` = 模块名大驼峰（`SysUser`），`${name}` = 首字母小写（`sysUser`），
`${table}` = 表名小写（`sysuser`），`${key}` = 业务唯一键属性（如 `code`，无则省略相关代码）。
字段部分按 create.md 的字段清单逐个展开。完整实例见 `Module.md` 的 SysUser 参考。

## Entity类

`src/main/java/com/baogang/info/entity/${Name}.java`

```java
package com.baogang.info.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.DynamicUpdate;

/** ${表注释} */
@Entity
@Table(name = "${table}")
@DynamicUpdate
public class ${Name} {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 每个字段按 create.md 生成；带 @NotBlank 的加校验注解；WRITE_ONLY 的加 @JsonProperty
    @NotBlank(message = "${key} 不能为空")            // 仅 create.md 标了必填的字段
    @Size(max = ${length}, message = "${col} 长度不能超过 ${length}")
    @Column(name = "${col}", length = ${length})
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)  // 仅 create.md 标了 WRITE_ONLY 的字段
    private String ${field};

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String get${FieldCap}() { return ${field}; }
    public void set${FieldCap}(String ${field}) { this.${field} = ${field}; }
}
```

规则：

- 手写 getter/setter，**不用 Lombok**；`@Size` 的 max 与 `@Column` 的 length 取 create.md 的 length。
- 列名一律小写（例外：`flownode` 的 `X/Y/W/H` 四列 `@Column` 必须大写）。

## Repository类

`src/main/java/com/baogang/info/repository/${Name}Repository.java`

```java
package com.baogang.info.repository;

import com.baogang.info.entity.${Name};
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ${Name}Repository extends JpaRepository<${Name}, Long> {

    Optional<${Name}> findBy${KeyCap}(String ${key});   // 有业务唯一键时

    boolean existsBy${KeyCap}(String ${key});           // save/update 查重用

    int deleteBy${KeyCap}(String ${key});               // 按唯一键删除时
}
```

## Query类

`src/main/java/com/baogang/info/dto/${Name}Query.java`

```java
package com.baogang.info.dto;

/**
 * ${Name} 查询条件（可变、可选）。所有字段默认 null，表示不参与过滤。
 * 通过 POST 请求体接收，支持任意字段组合的过滤条件。
 */
public class ${Name}Query {

    // create.md 字段清单中适合做查询条件的字段（跳过 password 这类敏感字段）
    private String ${field};
    // ... 其余字段

    private Integer page = 1;
    private Integer pageSize = 10;

    // 全部字段手写 getter/setter（含 page/pageSize）
}
```

## Mapper类

`src/main/java/com/baogang/info/mapper/${Name}Mapper.java`

```java
package com.baogang.info.mapper;

import com.baogang.info.dto.${Name}Query;
import com.baogang.info.entity.${Name};
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ${Name}Mapper {

    // 可变条件分页查询（空条件即查全部）
    List<${Name}> query(@Param("q") ${Name}Query q,
                        @Param("offset") long offset,
                        @Param("limit") int limit);

    long countByQuery(@Param("q") ${Name}Query q);
}
```

## Mapper.xml

`src/main/resources/mapper/${Name}Mapper.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.baogang.info.mapper.${Name}Mapper">

    <sql id="Base_Column_List">
        id, ${col1}, ${col2} <!-- 实体全部列，逗号分隔，小写 -->
    </sql>

    <sql id="Query_Where">
        <where>
            <!-- 每个查询字段一条 if；模糊字段（名称/备注/邮箱类）用 LIKE，状态/分类类用 = -->
            <if test="q.${field} != null and q.${field} != ''">AND ${col} = #{q.${field}}</if>
            <if test="q.${field} != null and q.${field} != ''">AND ${col} LIKE CONCAT('%', #{q.${field}}, '%')</if>
        </where>
    </sql>

    <select id="query" resultType="com.baogang.info.entity.${Name}">
        SELECT
        <include refid="Base_Column_List"/>
        FROM page.${table}
        <include refid="Query_Where"/>
        ORDER BY id DESC
        LIMIT #{offset}, #{limit}
    </select>

    <select id="countByQuery" resultType="long">
        SELECT COUNT(1)
        FROM page.${table}
        <include refid="Query_Where"/>
    </select>

</mapper>
```

规则：表名硬编码 `page.` 前缀；列名全小写；分页 `LIMIT #{offset}, #{limit}`（偏移由 Service 用 `(long) pageOffset * size` 算出）。

## Service类

`src/main/java/com/baogang/info/service/${Name}Service.java`

```java
package com.baogang.info.service;

import com.baogang.info.common.PageResult;
import com.baogang.info.dto.${Name}Query;
import com.baogang.info.entity.${Name};
import com.baogang.info.exception.ResourceNotFoundException;
import com.baogang.info.mapper.${Name}Mapper;
import com.baogang.info.repository.${Name}Repository;
import com.baogang.info.tool.StringTool;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ${Name}Service {

    private final ${Name}Repository ${name}Repository;
    private final ${Name}Mapper ${name}Mapper;

    public ${Name}Service(${Name}Repository ${name}Repository, ${Name}Mapper ${name}Mapper) {
        this.${name}Repository = ${name}Repository;
        this.${name}Mapper = ${name}Mapper;
    }

    public ${Name} getById(Long id) {
        return ${name}Repository.findById(id).orElse(null);
    }

    @Transactional
    public ${Name} save(${Name} ${name}) {
        ${name}.setId(null); // 新增时忽略客户端传入的 id
        if (${name}Repository.existsBy${KeyCap}(${name}.get${KeyCap}())) {   // 有唯一键时
            throw new IllegalArgumentException("${key} already exists: " + ${name}.get${KeyCap}());
        }
        return ${name}Repository.save(${name});
    }

    @Transactional
    public ${Name} update(Long id, ${Name} ${name}) {
        ${Name} existing = ${name}Repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("${name} not found: " + id));
        // 逐字段 set（把请求体的值同步到 existing）；带"传空则保留原值"语义的字段加 isNotBlank 判断
        // existing.set${FieldCap}(${name}.get${FieldCap}());
        return ${name}Repository.save(existing);
    }

    @Transactional
    public void deleteById(Long id) {
        if (${name}Repository.findById(id).isEmpty()) {
            throw new ResourceNotFoundException("${name} not found: " + id);
        }
        ${name}Repository.deleteById(id);
    }

    // 可变条件查询：接收 ${Name}Query，按非空字段动态拼接 WHERE（空条件即查全部）
    public PageResult<${Name}> search(${Name}Query query, int pageOffset, int size) {
        long total = ${name}Mapper.countByQuery(query);
        List<${Name}> content = ${name}Mapper.query(query, (long) pageOffset * size, size);
        return PageResult.of(content, total, pageOffset + 1, size);
    }
}
```

规则：

- 不存在一律抛 `ResourceNotFoundException`（→ 400），不返回 null、不手写 404 code。
- search 的 `pageOffset` 是 0 基，返回时 `pageOffset + 1` 还原 1 基响应。

## Controller类

`src/main/java/com/baogang/info/controller/${Name}Controller.java`

```java
package com.baogang.info.controller;

import com.baogang.info.common.ApiResponse;
import com.baogang.info.common.PageParam;
import com.baogang.info.common.PageResult;
import com.baogang.info.dto.${Name}Query;
import com.baogang.info.entity.${Name};
import com.baogang.info.exception.ResourceNotFoundException;
import com.baogang.info.service.${Name}Service;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/${name}")
public class ${Name}Controller {

    private final ${Name}Service ${name}Service;

    public ${Name}Controller(${Name}Service ${name}Service) {
        this.${name}Service = ${name}Service;
    }

    // 复杂/可变条件查询：POST 请求体承载 ${Name}Query，支持任意字段组合过滤
    @PostMapping("/search")
    public ApiResponse<PageResult<${Name}>> searchByQuery(@RequestBody ${Name}Query query) {
        PageParam p = PageParam.of(query.getPage(), query.getPageSize());
        return ApiResponse.success(${name}Service.search(query, p.page0(), p.size()));
    }

    @GetMapping("/{id}")
    public ApiResponse<${Name}> getById(@PathVariable Long id) {
        ${Name} ${name} = ${name}Service.getById(id);
        if (${name} == null) {
            throw new ResourceNotFoundException("${name} not found: " + id);
        }
        return ApiResponse.success(${name});
    }

    @PostMapping("/save")
    public ApiResponse<${Name}> save(@Valid @RequestBody ${Name} ${name}) {
        return ApiResponse.success(${name}Service.save(${name}));
    }

    // 修改：路由统一为 PUT /update，id 由请求体携带
    @PutMapping("/update")
    public ApiResponse<${Name}> update(@Valid @RequestBody ${Name} ${name}) {
        if (${name}.getId() == null) {
            throw new IllegalArgumentException("修改操作必须传入 id");
        }
        return ApiResponse.success(${name}Service.update(${name}.getId(), ${name}));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        ${name}Service.deleteById(id);
        return ApiResponse.success("删除处理完毕");
    }

    // 有业务唯一键 ${key} 时额外生成（对照 SysUserController）：
    // @GetMapping("/{" + key + "}/{" + value + "}")  按 ${key} 查详情
    // @DeleteMapping("/{" + key + "}/{" + value + "}") 按 ${key} 删除
}
```

规则：

- Controller 只做参数归一与转发，业务逻辑全部在 Service；无手写 404 code。
