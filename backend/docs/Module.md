# 子模块编码规范

## 默认路径

| Path                                      | 说明                    |
| ----------------------------------------- | ----------------------- |
| backend                                   | 根目录                  |
| src/main/java                             | Java代码更根目录        |
| src/main/resources                        | 资源根目录              |
| src/main/resources/mapper                 | mybatis Mapper 配置目录 |
| src/main/resources/templates              | 静态资源目录            |
| src/main/resources/application.properties | 配置文件                |

## Java代码包名

```java
com.baogang.info
```

## `${name}` 表示模块的名称

新建的Java文件和XML文件

| 文件                                          | 说明                  |
| --------------------------------------------- | --------------------- |
| com.baogang.info.entity.${name}               | Entity类              |
| com.baogang.info.controller.${name}Controller | Controller类          |
| com.baogang.info.service.${name}              | Service类             |
| com.baogang.info.mapper.${name}Mapper         | Mybatis Mapper 类     |
| com.baogang.info.repository.${name}Repository | JpaRepository 类      |
| com.baogang.info.dto.${name}Query             | 查询参数类            |
| src/main/resources/mapper/${name}Mapper.xml   | mybatis SQLMapper文件 |

## 参考示例子模块 `SysUser`







