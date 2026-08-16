<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import menuData from '../config/menu.json'

interface MenuItem {
  name: string
  path: string
  icon?: string
  children?: MenuItem[]
}

defineOptions({ name: 'MenuBar' })

const props = withDefaults(
  defineProps<{ items?: MenuItem[] }>(),
  {
    // 未传入菜单时，默认加载 config/menu.json
    items: () => menuData.menu as MenuItem[],
  },
)

const router = useRouter()

// 记录已展开的父级菜单 path
const expanded = ref<Set<string>>(new Set())

function handleClick(item: MenuItem) {
  const hasChildren = !!(item.children && item.children.length > 0)
  if (hasChildren) {
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
</script>

<template>
  <ul class="menu-tree">
    <li v-for="item in props.items" :key="item.path" class="menu-node">
      <div
        class="menu-item"
        :class="{ 'has-children': !!(item.children && item.children.length) }"
        @click="handleClick(item)"
      >
        <span v-if="item.children && item.children.length" class="menu-arrow">
          {{ expanded.has(item.path) ? '▾' : '▸' }}
        </span>
        <span>{{ item.name }}</span>
      </div>
      <!-- 子菜单默认隐藏，点击父级后展开 -->
      <div v-if="item.children && item.children.length && expanded.has(item.path)" class="menu-children">
        <MenuBar :items="item.children" />
      </div>
    </li>
  </ul>
</template>

<style scoped>
.menu-tree {
  list-style: none;
  margin: 0;
  padding: 0;
  width: 100%;
}

.menu-node {
  width: 100%;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  color: #333;
  cursor: pointer;
  user-select: none;
  transition: background-color 0.2s;
}

.menu-item:hover {
  background-color: rgba(255, 255, 255, 0.6);
}

.menu-item.has-children {
  font-weight: 600;
}

.menu-arrow {
  display: inline-block;
  width: 14px;
  text-align: center;
  font-size: 12px;
  color: #888;
}

.menu-children {
  padding-left: 16px;
}
</style>
