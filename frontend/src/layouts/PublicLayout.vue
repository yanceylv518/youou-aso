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
        <el-dropdown trigger="click" @command="setLocale">
          <button class="language-trigger" type="button" :aria-label="t('common.language')">
            <span class="language-icon">◎</span>{{ currentLocaleOption?.nativeLabel }}<span class="language-chevron">⌄</span>
          </button>
          <template #dropdown><el-dropdown-menu><el-dropdown-item v-for="option in localeOptions" :key="option.code" :command="option.code" :class="{ 'is-selected-locale': currentLocale === option.code }">{{ option.nativeLabel }}</el-dropdown-item></el-dropdown-menu></template>
        </el-dropdown>
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
import { LOCALE_STORAGE_KEY, localeHtmlLang, localeOptions, type AppLocale } from '../i18n'
import { writeBrowserStorage } from '@/utils/browserStorage'

const { locale, t } = useI18n()

const currentLocale = computed(() => locale.value as AppLocale)
const currentLocaleOption = computed(() => localeOptions.find(({ code }) => code === currentLocale.value))

const setLocale = (nextLocale: AppLocale) => {
  locale.value = nextLocale
  writeBrowserStorage(LOCALE_STORAGE_KEY, nextLocale)
  document.documentElement.lang = localeHtmlLang[nextLocale]
}
</script>

<style scoped>
.public-layout {
  min-height: 100vh;
  background: #f5f9ff;
  color: #0f172a;
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
  color: #334155;
  font-size: 15px;
  font-weight: 700;
  text-decoration: none;
  white-space: nowrap;
}

.public-nav a.router-link-active {
  color: #1d4ed8;
  font-weight: 700;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 18px;
}

.language-trigger {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  height: 38px;
  padding: 0 14px;
  border: 1px solid #dce6f4;
  border-radius: 999px;
  background: #ffffff;
  color: #334155;
  cursor: pointer;
  font: inherit;
  font-size: 13px;
  font-weight: 700;
}
.language-icon { color: #2563eb; font-size: 16px; }
.language-chevron { color: #94a3b8; font-size: 14px; }
:global(.is-selected-locale) { color: #2563eb; font-weight: 700; background: #eff6ff; }

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

  .language-trigger { height: 34px; padding: 0 11px; }
}
</style>

