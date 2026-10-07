<template>
  <el-tag v-if="isReservedOrder(order, now)" type="warning" effect="plain" class="reserved-order-tag order-status-tag">{{ t('orderCreate.reservedOrder') }}</el-tag>
</template>

<script setup lang="ts">
import { onUnmounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { isReservedOrder } from '@/utils/orderTime'

defineProps<{ order: Parameters<typeof isReservedOrder>[0] }>()
const { t } = useI18n()
const now = ref(new Date())
const timer = setInterval(() => { now.value = new Date() }, 1000)
onUnmounted(() => clearInterval(timer))
</script>

<style scoped>
.reserved-order-tag { margin-left: 6px; max-width: calc(100% - 6px); }
</style>
