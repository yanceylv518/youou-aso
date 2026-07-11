<template>
  <div class="home-page">
    <section class="hero-section" :aria-label="t('home.aria')">
      <div class="hero-inner">
        <div class="hero-copy">
          <p class="eyebrow">
            <Promotion class="eyebrow-icon" />
            {{ t('home.eyebrow') }}
          </p>
          <h1>
            {{ t('home.headlinePrefix') }}<span>{{ t('home.headlineHighlightOne') }}</span>{{ t('home.headlineMiddle') }}<span>{{ t('home.headlineHighlightTwo') }}</span>
          </h1>
          <p class="summary">{{ t('home.summary') }}</p>

          <div class="actions">
            <el-button class="primary-action" type="primary" size="large" @click="$router.push('/register')">
              {{ t('home.start') }}
              <ArrowRight />
            </el-button>
            <el-button class="secondary-action" size="large" @click="$router.push('/login')">
              <User />
              {{ t('home.login') }}
            </el-button>
          </div>

          <div class="store-list" :aria-label="t('home.storesAria')">
            <span v-for="store in stores" :key="store.name">
              <StoreIcon :store-type="store.storeType" size="sm" />
              {{ store.name }}
            </span>
          </div>
        </div>

        <aside class="service-panel" :aria-label="t('home.panelAria')">
          <div class="panel-header">
            <div>
              <strong>{{ t('home.panelTitle') }}</strong>
              <p>{{ t('home.panelSubtitle') }}</p>
            </div>
            <div class="panel-dots" aria-hidden="true">
              <span></span>
              <span></span>
              <span></span>
            </div>
          </div>

          <div class="panel-platforms" :aria-label="t('home.platformsAria')">
            <span class="active">App Store</span>
            <span>Google Play</span>
            <span>iPad Store</span>
          </div>

          <div class="service-list">
            <button v-for="service in services" :key="service.title" type="button" class="service-item">
              <span class="service-icon" :class="service.theme">
                <component :is="service.icon" />
              </span>
              <span class="service-text">
                <strong>{{ service.title }}</strong>
                <small>{{ service.subtitle }}</small>
              </span>
              <ArrowRight class="service-arrow" />
            </button>
          </div>
        </aside>
      </div>

      <div class="metric-panel" :aria-label="t('home.metricsAria')">
        <article v-for="metric in metrics" :key="metric.label" class="metric-item">
          <span class="metric-icon" :class="metric.theme">
            <component :is="metric.icon" />
          </span>
          <span class="metric-content">
            <strong>{{ metric.value }}</strong>
            <span>{{ metric.label }}</span>
            <small>{{ metric.note }}</small>
          </span>
        </article>
      </div>

      <section class="platform-section" :aria-label="t('home.globalAria')">
        <h2>{{ t('home.globalTitle') }}</h2>
        <p>{{ t('home.globalSummary') }}</p>
      </section>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import {
  ArrowRight,
  Briefcase,
  ChatDotRound,
  Download,
  Flag,
  GoldMedal,
  Histogram,
  Promotion,
  Search,
  Star,
  Lock,
  User,
  UserFilled
} from '@element-plus/icons-vue'
import type { StoreType } from '@/api/applications'
import StoreIcon from '@/components/StoreIcon.vue'

const { t } = useI18n()

const stores: Array<{ name: string; storeType: StoreType }> = [
  { name: 'App Store', storeType: 'APP_STORE' },
  { name: 'Google Play', storeType: 'GOOGLE_PLAY' },
  { name: 'iPad Store', storeType: 'IPAD_STORE' }
]

const services = computed(() => [
  { title: t('home.services.keywordInstalls'), subtitle: t('home.serviceSubtitles.keywordInstalls'), icon: Search, theme: 'blue' },
  { title: t('home.services.downloads'), subtitle: t('home.serviceSubtitles.downloads'), icon: Download, theme: 'green' },
  { title: t('home.services.ratings'), subtitle: t('home.serviceSubtitles.ratings'), icon: Star, theme: 'orange' },
  { title: t('home.services.reviews'), subtitle: t('home.serviceSubtitles.reviews'), icon: ChatDotRound, theme: 'purple' },
  { title: t('home.services.keywordRanking'), subtitle: t('home.serviceSubtitles.keywordRanking'), icon: Lock, theme: 'blue-soft' },
  { title: t('home.services.keywordCoverage'), subtitle: t('home.serviceSubtitles.keywordCoverage'), icon: Flag, theme: 'cyan' }
])

const metrics = computed(() => [
  { value: '10,000+', label: t('home.metrics.apps'), note: t('home.metrics.appsNote'), icon: Briefcase, theme: 'blue' },
  { value: '98.6%', label: t('home.metrics.satisfaction'), note: t('home.metrics.satisfactionNote'), icon: Histogram, theme: 'green' },
  { value: t('home.metrics.yearsValue'), label: t('home.metrics.experience'), note: t('home.metrics.experienceNote'), icon: GoldMedal, theme: 'purple' },
  { value: '50+', label: t('home.metrics.team'), note: t('home.metrics.teamNote'), icon: UserFilled, theme: 'yellow' }
])
</script>

<style scoped>
.home-page {
  min-height: calc(100vh - 64px);
  overflow: hidden;
  background:
    radial-gradient(circle at 98% 14%, rgb(222 234 255 / 66%) 0 180px, transparent 360px),
    radial-gradient(circle at 0% 68%, rgb(202 221 255 / 72%) 0 116px, transparent 270px),
    linear-gradient(180deg, #f5f9ff 0%, #ffffff 78%);
  color: #142033;
}

.hero-section {
  position: relative;
  width: min(1360px, 100%);
  margin: 0 auto;
  padding: 68px 48px 0;
}

.hero-section::before {
  position: absolute;
  left: -120px;
  bottom: 248px;
  width: 330px;
  height: 135px;
  border: 9px solid rgb(58 124 255 / 22%);
  border-top: 0;
  border-right: 0;
  border-radius: 0 0 0 130px;
  content: "";
  transform: rotate(-8deg);
}

.hero-section::after {
  position: absolute;
  right: 18%;
  bottom: 300px;
  width: 118px;
  height: 118px;
  border-radius: 28px;
  background:
    linear-gradient(135deg, rgb(53 117 246 / 22%), rgb(255 255 255 / 50%)),
    linear-gradient(45deg, transparent 44%, rgb(53 117 246 / 20%) 45% 55%, transparent 56%);
  box-shadow: 0 24px 58px rgb(50 115 245 / 14%);
  content: "";
  transform: rotate(33deg);
}

.hero-inner {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(430px, 520px);
  gap: 72px;
  align-items: center;
}

.hero-copy {
  max-width: 640px;
}

.eyebrow {
  width: fit-content;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 24px;
  padding: 8px 14px;
  border: 1px solid #dbe8ff;
  border-radius: 999px;
  background: rgb(255 255 255 / 76%);
  color: #2878f0;
  font-size: 14px;
  font-weight: 800;
  letter-spacing: 0;
  box-shadow: 0 10px 24px rgb(45 113 234 / 8%);
}

.eyebrow-icon {
  width: 16px;
  height: 16px;
  padding: 3px;
  border-radius: 6px;
  background: #2f7df4;
  color: #ffffff;
}

h1,
h2,
p {
  letter-spacing: 0;
}

h1 {
  margin: 0;
  color: #132033;
  font-size: 58px;
  font-weight: 900;
  line-height: 1.15;
}

h1 span {
  color: #2f7df4;
}

.summary {
  max-width: 620px;
  margin: 26px 0 0;
  color: #667695;
  font-size: 18px;
  line-height: 1.75;
}

.actions {
  display: flex;
  gap: 18px;
  margin-top: 30px;
}

.actions :deep(.el-button) {
  width: 166px;
  height: 50px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 800;
}

.actions :deep(.el-icon),
.actions svg {
  width: 18px;
  height: 18px;
}

.primary-action {
  border-color: #2f7df4;
  background: linear-gradient(135deg, #2f7df4, #246be0);
  box-shadow: 0 16px 30px rgb(47 125 244 / 26%);
}

.secondary-action {
  border-color: #dfe6f2;
  color: #38465e;
  background: rgb(255 255 255 / 88%);
}

.store-list {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-top: 32px;
}

.store-list span {
  min-width: 130px;
  height: 44px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 9px;
  padding: 0 16px;
  border: 1px solid #dde6f4;
  border-radius: 8px;
  background: rgb(255 255 255 / 86%);
  color: #17243a;
  font-weight: 800;
  box-shadow: 0 10px 24px rgb(27 55 105 / 5%);
}

.service-panel {
  position: relative;
  padding: 32px;
  border: 1px solid rgb(218 226 239 / 80%);
  border-radius: 10px;
  background:
    linear-gradient(90deg, rgb(255 255 255 / 98%) 0%, rgb(255 255 255 / 90%) 66%, rgb(246 250 255 / 70%) 100%),
    repeating-linear-gradient(0deg, transparent 0 12px, rgb(60 117 215 / 5%) 13px 14px);
  box-shadow: 12px 12px 0 rgb(129 157 204 / 20%), 0 28px 72px rgb(42 78 140 / 11%);
}

.panel-header {
  display: flex;
  justify-content: space-between;
  gap: 18px;
  align-items: flex-start;
  margin-bottom: 20px;
}

.panel-header strong {
  display: block;
  color: #132033;
  font-size: 24px;
  font-weight: 900;
}

.panel-header p {
  margin: 8px 0 0;
  color: #697795;
  font-size: 15px;
}

.panel-dots {
  display: flex;
  gap: 10px;
  padding-top: 8px;
}

.panel-dots span {
  width: 11px;
  height: 11px;
  border-radius: 50%;
  background: #2f7df4;
}

.panel-dots span:nth-child(2) {
  background: #57c6a3;
}

.panel-dots span:nth-child(3) {
  background: #ffc15c;
}

.panel-platforms {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.panel-platforms span {
  min-width: 112px;
  height: 38px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0 16px;
  border: 1px solid #dfe7f3;
  border-radius: 999px;
  background: #f4f7fb;
  color: #26364e;
  font-weight: 800;
}

.panel-platforms .active {
  border-color: #2f7df4;
  background: #2f7df4;
  color: #ffffff;
  box-shadow: 0 10px 22px rgb(47 125 244 / 24%);
}

.service-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.service-item {
  min-height: 88px;
  display: grid;
  grid-template-columns: 52px minmax(0, 1fr) 18px;
  gap: 14px;
  align-items: center;
  padding: 14px;
  border: 1px solid #dfe7f3;
  border-radius: 8px;
  background: rgb(255 255 255 / 90%);
  color: inherit;
  cursor: default;
  font: inherit;
  text-align: left;
  box-shadow: 0 10px 24px rgb(36 65 115 / 6%);
}

.service-icon,
.metric-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 18px;
}

.service-icon {
  width: 52px;
  height: 52px;
}

.service-icon svg,
.metric-icon svg {
  width: 26px;
  height: 26px;
}

.service-text {
  min-width: 0;
}

.service-text strong {
  display: block;
  overflow-wrap: anywhere;
  color: #162238;
  font-size: 16px;
  font-weight: 900;
  line-height: 1.35;
}

.service-text small {
  display: block;
  margin-top: 5px;
  color: #77849a;
  font-size: 13px;
  font-weight: 700;
  line-height: 1.35;
}

.service-arrow {
  color: #9aa8bd;
}

.blue {
  background: #dbe8ff;
  color: #2f7df4;
}

.green {
  background: #d9f5e6;
  color: #37bd78;
}

.orange {
  background: #ffedcf;
  color: #f5a11c;
}

.purple {
  background: #e7ddff;
  color: #7657d8;
}

.blue-soft {
  background: #dce8ff;
  color: #4179ea;
}

.cyan {
  background: #d6f4f5;
  color: #43b8c3;
}

.yellow {
  background: #ffe5a6;
  color: #eba51f;
}

.metric-panel {
  position: relative;
  z-index: 2;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 0;
  margin-top: 54px;
  padding: 36px 28px;
  border: 1px solid rgb(223 232 246 / 80%);
  border-radius: 12px;
  background: rgb(255 255 255 / 88%);
  box-shadow: 0 24px 58px rgb(36 65 115 / 8%);
  backdrop-filter: blur(10px);
}

.metric-item {
  display: grid;
  grid-template-columns: 68px minmax(0, 1fr);
  gap: 20px;
  align-items: center;
  padding: 0 28px;
  border-right: 1px solid #edf1f7;
}

.metric-item:last-child {
  border-right: 0;
}

.metric-icon {
  width: 68px;
  height: 68px;
  box-shadow: 0 12px 24px rgb(47 125 244 / 16%);
}

.metric-content {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.metric-content strong {
  color: #132033;
  font-size: 28px;
  font-weight: 900;
  line-height: 1;
}

.metric-content span {
  color: #526177;
  font-size: 16px;
  font-weight: 800;
}

.metric-content small {
  color: #7a879b;
  font-size: 13px;
  line-height: 1.35;
}

.platform-section {
  position: relative;
  z-index: 1;
  padding: 84px 16px 34px;
  text-align: center;
}

.platform-section h2 {
  position: relative;
  display: inline-flex;
  align-items: center;
  gap: 24px;
  margin: 0;
  color: #2f7df4;
  font-size: 24px;
  font-weight: 900;
}

.platform-section h2::before,
.platform-section h2::after {
  width: 72px;
  height: 3px;
  border-radius: 999px;
  background: linear-gradient(90deg, transparent, #b7cdfa);
  content: "";
}

.platform-section h2::after {
  background: linear-gradient(90deg, #b7cdfa, transparent);
}

.platform-section p {
  margin: 20px 0 0;
  color: #718098;
  font-size: 16px;
}

@media (max-width: 1120px) {
  .hero-section {
    padding: 48px 28px 0;
  }

  .hero-inner {
    grid-template-columns: 1fr;
    gap: 36px;
  }

  .hero-copy {
    max-width: 780px;
  }

  .hero-section::after {
    right: 8%;
    bottom: 410px;
  }

  .metric-panel {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    row-gap: 28px;
  }

  .metric-item:nth-child(2) {
    border-right: 0;
  }
}

@media (max-width: 760px) {
  .hero-section {
    padding: 34px 18px 0;
  }

  .hero-section::before,
  .hero-section::after {
    display: none;
  }

  h1 {
    font-size: 40px;
  }

  .summary {
    font-size: 16px;
  }

  .actions {
    flex-direction: column;
  }

  .actions :deep(.el-button) {
    width: 100%;
  }

  .store-list span {
    flex: 1 1 150px;
  }

  .service-panel {
    padding: 22px;
  }

  .panel-platforms,
  .service-list {
    grid-template-columns: 1fr;
  }

  .panel-platforms {
    display: grid;
  }

  .service-list {
    display: grid;
  }

  .metric-panel {
    grid-template-columns: 1fr;
    padding: 24px;
  }

  .metric-item,
  .metric-item:nth-child(2) {
    padding: 20px 0;
    border-right: 0;
    border-bottom: 1px solid #edf1f7;
  }

  .metric-item:first-child {
    padding-top: 0;
  }

  .metric-item:last-child {
    padding-bottom: 0;
    border-bottom: 0;
  }

  .platform-section h2 {
    gap: 14px;
    font-size: 21px;
  }

  .platform-section h2::before,
  .platform-section h2::after {
    width: 34px;
  }
}

@media (max-width: 460px) {
  h1 {
    font-size: 34px;
  }

  .service-item {
    grid-template-columns: 46px minmax(0, 1fr) 16px;
    gap: 12px;
  }

  .service-icon {
    width: 46px;
    height: 46px;
  }

  .metric-item {
    grid-template-columns: 58px minmax(0, 1fr);
    gap: 16px;
  }

  .metric-icon {
    width: 58px;
    height: 58px;
  }
}
</style>
