<template>
  <section class="promotion-page">
    <p class="page-note">{{ t('adminPromotion.subtitle') }}</p>

    <div class="service-grid">
      <article v-for="service in services" :key="service.code" class="service-card">
        <div class="service-art" :class="service.tone">
          <component :is="service.icon" class="service-icon" />
        </div>
        <div class="service-body">
          <div class="service-heading">
            <h2>{{ typeLabel(service.code) }}</h2>
          </div>
          <p>{{ t(`promotionPage.descriptions.${service.code}`) }}</p>
          <el-button
            type="primary"
            class="service-action"
            @click="goOrder(service)"
          >
            {{ t('adminPromotion.createForCustomer') }}
          </el-button>
        </div>
      </article>
    </div>
  </section>
</template>

<script setup lang="ts">
import { type Component } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { Download, EditPen, Medal, Search, Star, TrendCharts } from '@element-plus/icons-vue'
import type { OrderType } from '@/api/orders'

interface ServiceOption {
  code: OrderType
  icon: Component
  tone: string
  special?: boolean
}

const router = useRouter()
const { t } = useI18n()

const services: ServiceOption[] = [
  { code: 'KEYWORD_INSTALL', icon: Search, tone: 'tone-cyan' },
  { code: 'DOWNLOAD', icon: Download, tone: 'tone-pink' },
  { code: 'RATING', icon: Star, tone: 'tone-blue' },
  { code: 'REVIEW', icon: EditPen, tone: 'tone-pink' },
  { code: 'RANK_GUARANTEE', icon: Medal, tone: 'tone-cyan', special: true },
  { code: 'KEYWORD_COVERAGE', icon: TrendCharts, tone: 'tone-green', special: true }
]

function typeLabel(type: OrderType) {
  return t(`ordersPage.types.${type}`)
}

function goOrder(service: ServiceOption) {
  router.push({
    name: 'admin-order-create',
    query: {
      orderType: service.code
    }
  })
}
</script>

<style scoped>
.promotion-page {
  max-width: 1120px;
  margin: 0 auto;
}

.page-note {
  max-width: 720px;
  margin: 0 0 18px;
  color: #667085;
  line-height: 1.6;
  letter-spacing: 0;
}

.service-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;
}

.service-card {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 4px 14px rgb(24 34 48 / 8%);
}

.service-art {
  height: 82px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.tone-cyan {
  background: #e7f8fa;
  color: #10a9b7;
}

.tone-pink {
  background: #fff0f8;
  color: #db3f98;
}

.tone-blue {
  background: #eef6ff;
  color: #2d7dd2;
}

.tone-green {
  background: #eff9ee;
  color: #42a948;
}

.service-icon {
  width: 36px;
  height: 36px;
}

.service-body {
  display: flex;
  flex: 1;
  flex-direction: column;
  padding: 18px 16px 16px;
}

.service-heading {
  min-height: 28px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.service-heading h2 {
  margin: 0;
  color: #182230;
  font-size: 18px;
}

.service-body p {
  min-height: 48px;
  margin: 8px 0 18px;
  color: #667085;
  font-size: 14px;
  line-height: 1.7;
}

.service-action {
  width: 100%;
  margin-top: auto;
}

@media (max-width: 960px) {
  .service-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .service-grid {
    grid-template-columns: 1fr;
  }
}
</style>
