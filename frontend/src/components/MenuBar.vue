<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowRight } from '@element-plus/icons-vue'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import menuData from '../config/menu.json'
import { hasRight } from './sys/permission'

/** 将菜单 icon 字段（Element Plus 图标名）解析为组件；无效时回退到 Document */
function iconComp(name?: string) {
  if (name && (ElementPlusIconsVue as Record<string, unknown>)[name]) {
    return (ElementPlusIconsVue as Record<string, unknown>)[name]
  }
  return ElementPlusIconsVue.Document
}

interface MenuItem {
  name: string
  path: string
  icon?: string
  /** 页面权限标识（page:*），存在时仅当用户拥有该权限才显示菜单 */
  right?: string
  children?: MenuItem[]
}

/**
 * 递归过滤菜单：移除当前用户无权限的页面菜单项。
 * 父级菜单自身的 right 与子级的 right 独立控制：
 * - 叶子项：仅当拥有自身 right 才显示（无 right 则始终显示）。
 * - 父级项：拥有自身 right 即显示；若自身无 right 或无权，只要存在任一可见子项也显示。
 */
function filterMenu(items: MenuItem[]): MenuItem[] {
  return items
    .map((item) => {
      if (item.children && item.children.length) {
        const children = filterMenu(item.children)
        // 父级自身有权限，或存在可见子项时，父级才展示
        if (children.length || !item.right || hasRight(item.right)) {
          return children.length ? { ...item, children } : item
        }
        return null
      }
      return !item.right || hasRight(item.right) ? item : null
    })
    .filter((x): x is MenuItem => x !== null)
}

const props = withDefaults(
  defineProps<{ items?: MenuItem[]; topLevel?: boolean }>(),
  {
    // 未传入菜单时，默认加载 config/menu.json
    items: () => menuData.menu as MenuItem[],
    // 仅顶级菜单需要为箭头预留固定宽度以保证标题对齐；嵌套层级靠 padding 缩进
    topLevel: true,
  },
)

// 最终渲染的菜单（已按权限过滤）
const visibleItems = computed(() => filterMenu(props.items))

defineOptions({ name: 'MenuBar' })

const router = useRouter()
const route = useRoute()

// 记录已展开的父级菜单 path
const expanded = ref<Set<string>>(new Set())

/** 判断菜单项是否包含子菜单 */
function hasChildren(item: MenuItem): boolean {
  return !!(item.children && item.children.length > 0)
}

/** 判断叶子菜单是否为当前激活项（path 与当前路由匹配） */
function isActive(item: MenuItem): boolean {
  return !hasChildren(item) && item.path === route.path
}

/**
 * 递归查找包含 targetPath 的叶子节点，返回其所有祖先节点的 path 列表。
 * 用于进入页面时按当前路由自动展开对应的父级菜单链。
 */
function findActiveAncestors(
  items: MenuItem[],
  targetPath: string,
  ancestors: string[] = [],
): string[] {
  for (const item of items) {
    if (hasChildren(item)) {
      const found = findActiveAncestors(item.children!, targetPath, [...ancestors, item.path])
      if (found.length) return found
    } else if (item.path === targetPath) {
      return ancestors
    }
  }
  return []
}

// 路由变化时自动展开包含当前激活菜单的祖先链；immediate 保证首次进入即生效
watch(
  () => route.path,
  () => {
    const ancestors = findActiveAncestors(props.items, route.path)
    if (ancestors.length) {
      expanded.value = new Set([...expanded.value, ...ancestors])
    }
  },
  { immediate: true },
)

function handleClick(item: MenuItem) {
  if (hasChildren(item)) {
    // 有子菜单：切换展开 / 收起
    const next = new Set(expanded.value)
    if (next.has(item.path)) {
      next.delete(item.path)
    } else {
      next.add(item.path)
    }
    expanded.value = next
  } else if (item.path) {
    // 叶子节点：跳转到对应路径
    router.push(item.path)
  }
}

// 键盘操作：回车 / 空格触发点击，提升可访问性
function handleKeydown(item: MenuItem, event: KeyboardEvent) {
  if (event.key === 'Enter' || event.key === ' ') {
    event.preventDefault()
    handleClick(item)
  }
}
</script>

<template>
  <ul class="menu-tree" role="menu">
    <li v-for="item in visibleItems" :key="item.path || item.name" class="menu-node" role="none">
      <div
        class="menu-item"
        :class="{
          'has-children': hasChildren(item),
          active: isActive(item),
        }"
        role="menuitem"
        :aria-expanded="hasChildren(item) ? expanded.has(item.path) : undefined"
        :tabindex="0"
        @click="handleClick(item)"
        @keydown="handleKeydown(item, $event)"
      >
        <span class="menu-icon">
          <el-icon><component :is="iconComp(item.icon)" /></el-icon>
        </span>
        <span
          class="menu-arrow"
          :class="{
            'has-children': hasChildren(item),
            expanded: expanded.has(item.path),
            'reserve-slot': props.topLevel,
          }"
        >
          <el-icon><ArrowRight /></el-icon>
        </span>
        <span class="menu-label">{{ item.name }}</span>
      </div>
      <!-- 子菜单默认隐藏，点击父级后展开 -->
      <div
        v-if="hasChildren(item) && expanded.has(item.path)"
        class="menu-children"
        role="group"
      >
        <MenuBar :items="filterMenu(item.children!)" :top-level="false" />
      </div>
    </li>
  </ul>
</template>

<style scoped>
.menu-tree {
  list-style: none;
  margin: 0;
  padding: 8px 0;
  width: 100%;
}

.menu-node {
  width: 100%;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 2px 8px;
  padding: 10px 12px;
  border-radius: 6px;
  cursor: pointer;
  user-select: none;
  transition: background-color 0.2s, color 0.2s, box-shadow 0.2s;
}

.menu-item:hover {
  background-color: #eef3fb;
}

.menu-item.active {
  color: #1e5aa8;
  background: linear-gradient(90deg, rgba(30, 90, 168, 0.14), rgba(30, 144, 255, 0.06));
  box-shadow: inset 3px 0 0 #1e5aa8;
  font-weight: 600;
}

/* 键盘导航时的焦点可见性，提升可访问性 */
.menu-item:focus-visible {
  outline: 2px solid #1e5aa8;
  outline-offset: -2px;
}

.menu-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 18px;
  flex: none;
  font-size: 17px;
  color: #5a6b85;
  transition: color 0.2s;
}

.menu-item:hover .menu-icon {
  color: #1e5aa8;
}

.menu-item.active .menu-icon {
  color: #1e5aa8;
}

.menu-arrow {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 14px;
  flex: none;
  font-size: 12px;
  color: #a3aeba;
  transition: transform 0.2s ease;
}

/* 顶级菜单：无子菜单的项也保留箭头占位宽度（reserve-slot + 隐藏图标），
   保证有/无子菜单的一级标题左对齐 */
.menu-arrow.reserve-slot:not(.has-children) {
  visibility: hidden;
}

/* 嵌套层级：仅对真正拥有子菜单的项显示箭头；无子菜单项不占位，
   缩进由 .menu-item 左 padding 体现，避免标题被额外偏移 */
.menu-children :deep(.menu-arrow):not(.has-children) {
  display: none;
}

/* 展开时箭头顺时针旋转 90°，配合过渡动画形成平滑展开指示 */
.menu-arrow.expanded {
  transform: rotate(90deg);
}

.menu-children {
  padding-left: 0;
}

/* 嵌套层级的缩进与层级引导线。
   注意：递归渲染的 MenuBar 是独立组件，其内部的 .menu-item 不会继承本组件的
   scoped 样式，因此统一用 :deep() 穿透到所有层级的菜单项。 */
.menu-children {
  position: relative;
}

/* 每一层子菜单整体向右缩进，形成清晰的树形层级。
   注意：缩进不再由容器 padding 实现（否则会缩减子项可用宽度，
   导致子菜单比上一级窄），改由子项自身的左 padding 体现，保证各级背景条等宽。 */
.menu-children .menu-tree {
  position: relative;
}

/* 子级左侧引导竖线，强化父子层级视觉；位置对齐子项圆点左侧 */
.menu-children .menu-tree::before {
  content: '';
  position: absolute;
  left: 26px;
  top: 4px;
  bottom: 4px;
  width: 1px;
  background-color: rgba(30, 90, 168, 0.12);
}

/* 通过 :deep() 穿透到递归子组件内部的 .menu-item，弱化子级样式以区分层级。
   子项填满容器（与父级等宽），层级缩进通过左 padding 体现，而非缩减容器宽度。 */
.menu-children :deep(.menu-item) {
  width: auto;
  box-sizing: border-box;
  margin: 1px 8px;
  padding: 8px 12px 8px 34px;
  font-weight: 400;
  color: #4a586b;
  font-size: 16px;
}

.menu-children :deep(.menu-item .menu-icon) {
  width: 16px;
  font-size: 16px;
  color: #7d8aa0;
}

.menu-children :deep(.menu-item:hover) {
  background-color: #eef3fb;
}

.menu-children :deep(.menu-item:hover .menu-icon) {
  color: #1e5aa8;
}

/* 子级键盘焦点可见性与一级对齐，保证跨层级交互态一致 */
.menu-children :deep(.menu-item:focus-visible) {
  outline: 2px solid #1e5aa8;
  outline-offset: -2px;
}

.menu-children :deep(.menu-item.active) {
  color: #1e5aa8;
  background: linear-gradient(90deg, rgba(30, 90, 168, 0.14), rgba(30, 144, 255, 0.06));
  box-shadow: inset 3px 0 0 #1e5aa8;
  font-weight: 600;
}

/* 子级菜单项左侧小圆点指示，进一步区分层级 */
.menu-children :deep(.menu-item)::before {
  content: '';
  flex: none;
  width: 6px;
  height: 6px;
  margin-right: 8px;
  border-radius: 50%;
  background-color: rgba(30, 90, 168, 0.25);
}

.menu-children :deep(.menu-item.active)::before {
  background-color: #1e5aa8;
}
</style>
