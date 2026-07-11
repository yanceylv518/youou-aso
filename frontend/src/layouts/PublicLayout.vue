<template>
  <div class="public-layout">
    <header class="public-header">
      <router-link class="brand" to="/">
        <img class="brand-mark" :src="systemLogo" alt="" />
        <span>{{ $t('app.name') }}</span>
      </router-link>
      <div class="header-actions">
        <nav class="public-nav" :aria-label="t('public.nav.aria')">
          <router-link to="/login">{{ t('public.nav.login') }}</router-link>
          <router-link to="/register">{{ t('public.nav.register') }}</router-link>
        </nav>
        <div class="language-switch" :aria-label="t('common.language')">
          <button type="button" :class="{ active: currentLocale === 'zh-CN' }" @click="setLocale('zh-CN')">
            {{ t('common.chinese') }}
          </button>
          <button type="button" :class="{ active: currentLocale === 'en-US' }" @click="setLocale('en-US')">
            {{ t('common.english') }}
          </button>
        </div>
      </div>
    </header>
    <main>
      <router-view />
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import systemLogo from '@/assets/logo/system-logo.png'
import { LOCALE_STORAGE_KEY, type AppLocale } from '../i18n'

const { locale, t } = useI18n()

const currentLocale = computed(() => locale.value as AppLocale)

const setLocale = (nextLocale: AppLocale) => {
  locale.value = nextLocale
  window.localStorage.setItem(LOCALE_STORAGE_KEY, nextLocale)
  document.documentElement.lang = nextLocale === 'zh-CN' ? 'zh-CN' : 'en'
}
</script>

<style scoped>
.public-layout {
  min-height: 100vh;
  background: #f5f9ff;
  color: #182230;
}

.public-header {
  position: sticky;
  top: 0;
  z-index: 100;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 48px;
  background: rgb(255 255 255 / 94%);
  border-bottom: 1px solid #e8eef8;
  box-shadow: 0 2px 16px rgb(16 24 40 / 4%);
  backdrop-filter: blur(12px);
}

main {
  min-height: calc(100vh - 64px);
}

.brand {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  flex: 0 0 auto;
  color: #132033;
  font-size: 18px;
  font-weight: 900;
  text-decoration: none;
  white-space: nowrap;
}

.brand-mark {
  width: 38px;
  height: 38px;
  display: block;
  flex: 0 0 auto;
  border-radius: 50%;
  object-fit: cover;
}

.public-nav {
  display: flex;
  gap: 16px;
}

.public-nav a {
  color: #344054;
  font-size: 15px;
  font-weight: 700;
  text-decoration: none;
  white-space: nowrap;
}

.public-nav a.router-link-active {
  color: #1b75d0;
  font-weight: 700;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 18px;
}

.language-switch {
  display: inline-flex;
  padding: 4px;
  border: 1px solid #dce6f4;
  border-radius: 999px;
  background: #ffffff;
  box-shadow: inset 0 0 0 1px rgb(255 255 255 / 60%);
}

.language-switch button {
  min-width: 70px;
  height: 30px;
  padding: 0 10px;
  border: 0;
  border-radius: 999px;
  background: transparent;
  color: #667085;
  cursor: pointer;
  font: inherit;
  font-size: 13px;
  font-weight: 700;
}

.language-switch button.active {
  background: linear-gradient(135deg, #2f7df4, #246be0);
  color: #ffffff;
  box-shadow: 0 8px 18px rgb(47 125 244 / 22%);
}

@media (max-width: 700px) {
  .public-header {
    min-height: 54px;
    height: auto;
    gap: 8px 12px;
    flex-wrap: wrap;
    padding: 10px 16px;
  }

  .header-actions {
    flex: 1 1 auto;
    justify-content: flex-end;
    gap: 10px;
  }

  .public-nav {
    flex: 0 0 auto;
    gap: 8px;
  }

  .public-nav a {
    font-size: 13px;
  }

  .language-switch button {
    min-width: 52px;
    padding: 0 7px;
    font-size: 12px;
  }
}
</style>

