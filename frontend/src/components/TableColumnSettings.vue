<template>
  <el-popover placement="bottom-end" :width="360" trigger="click" popper-class="table-column-settings-popper">
    <template #reference>
      <el-button :icon="Setting">{{ title }}</el-button>
    </template>
    <div class="column-settings">
      <div class="column-settings__header">
        <strong>{{ title }}</strong>
        <span>{{ modelValue.length }}/{{ options.length }}</span>
      </div>
      <el-checkbox-group :model-value="modelValue" class="column-settings__grid" @change="updateSelection">
        <el-checkbox v-for="option in options" :key="option.key" :value="option.key">
          {{ option.label }}
        </el-checkbox>
      </el-checkbox-group>
      <div class="column-settings__footer">
        <el-button size="small" text type="primary" @click="selectAll">{{ selectAllText }}</el-button>
        <el-button size="small" text @click="restoreDefaults">{{ restoreDefaultsText }}</el-button>
      </div>
    </div>
  </el-popover>
</template>

<script setup lang="ts">
import { Setting } from '@element-plus/icons-vue'

export interface TableColumnOption {
  key: string
  label: string
}

const props = defineProps<{
  modelValue: string[]
  options: TableColumnOption[]
  defaults: string[]
  title: string
  selectAllText: string
  restoreDefaultsText: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string[]]
}>()

function updateSelection(value: unknown) {
  const selected = Array.isArray(value) ? value.map(String) : []
  emit('update:modelValue', selected.length ? selected : [props.options[0]?.key].filter(Boolean) as string[])
}

function selectAll() {
  emit('update:modelValue', props.options.map((option) => option.key))
}

function restoreDefaults() {
  emit('update:modelValue', [...props.defaults])
}
</script>

<style scoped>
.column-settings {
  padding: 2px;
}

.column-settings__header,
.column-settings__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.column-settings__header {
  padding: 2px 2px 12px;
  border-bottom: 1px solid #e8edf5;
}

.column-settings__header strong {
  color: #172033;
  font-size: 14px;
}

.column-settings__header span {
  color: #7b8ba5;
  font-size: 12px;
}

.column-settings__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 4px 14px;
  max-height: 360px;
  padding: 12px 2px;
  overflow-y: auto;
}

.column-settings__grid :deep(.el-checkbox) {
  min-width: 0;
  margin-right: 0;
}

.column-settings__grid :deep(.el-checkbox__label) {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.column-settings__footer {
  padding-top: 8px;
  border-top: 1px solid #e8edf5;
}
</style>
