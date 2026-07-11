<template>
  <section class="order-create-page">
    <div class="order-shell">
      <el-form label-position="top" class="order-form">
        <div class="order-workspace">
          <main class="order-main">
            <section class="flow-section">
              <div class="section-heading">
                <h2><span>* 1.</span> {{ t('orderCreate.serviceSection') }}</h2>
              </div>
              <div class="type-tabs">
                <el-tabs v-model="form.orderType">
                  <el-tab-pane
                    v-for="service in services"
                    :key="service.code"
                    :name="service.code"
                  >
                    <template #label>
                      <span class="tab-label">
                        <component :is="service.icon" class="tab-icon" />
                        {{ typeLabel(service.code) }}
                      </span>
                    </template>
                  </el-tab-pane>
                </el-tabs>
              </div>

              <el-alert
                v-if="isSpecialOrder"
                class="notice"
                type="info"
                show-icon
                :closable="false"
              >
                {{ t('adminPromotion.specialNotice') }}
              </el-alert>
            </section>

            <section class="flow-section">
              <div class="section-heading">
                <h2><span>* 2.</span> {{ t('orderCreate.store') }}</h2>
              </div>
              <el-radio-group v-model="form.storeType" class="store-switch">
                <el-radio-button value="APP_STORE">
                  <span class="store-option"><StoreIcon store-type="APP_STORE" size="sm" /> App Store</span>
                </el-radio-button>
                <el-radio-button value="GOOGLE_PLAY">
                  <span class="store-option"><StoreIcon store-type="GOOGLE_PLAY" size="sm" /> Google Play</span>
                </el-radio-button>
                <el-radio-button value="IPAD_STORE">
                  <span class="store-option"><StoreIcon store-type="IPAD_STORE" size="sm" /> iPad Store</span>
                </el-radio-button>
              </el-radio-group>
            </section>

            <section class="flow-section flow-compact">
              <div class="step-field-grid">
                <div class="step-field">
                  <div class="section-heading">
                    <h2><span>* 3.</span> {{ t('orderCreate.customer') }}</h2>
                  </div>
                  <el-select
                    v-model="form.customerId"
                    filterable
                    :placeholder="t('orderCreate.selectCustomer')"
                    :loading="loading"
                  >
                    <el-option
                      v-for="customer in customers"
                      :key="customer.id"
                      :label="customerLabel(customer)"
                      :value="customer.id"
                    />
                  </el-select>
                </div>

                <div class="step-field">
                  <div class="section-heading">
                    <h2><span>4.</span> {{ t('orderCreate.app') }}</h2>
                  </div>
                  <div class="app-picker-line">
                    <el-select
                      v-model="form.customerAppId"
                      filterable
                      :placeholder="t('orderCreate.selectApp')"
                      :loading="loading"
                      :disabled="!form.customerId"
                    >
                      <el-option
                        v-for="app in filteredApps"
                        :key="app.id"
                        :label="app.appName"
                        :value="app.id"
                      >
                        <div class="app-option">
                          <span>{{ app.appName }}</span>
                          <small>{{ app.appIdentifier }}</small>
                        </div>
                      </el-option>
                    </el-select>
                    <el-button type="primary" class="add-app-button" @click="router.push({ name: 'admin-applications' })">
                      {{ t('applications.addApp') }}
                    </el-button>
                  </div>
                </div>
              </div>
            </section>

            <section
              v-if="!['KEYWORD_INSTALL', 'DOWNLOAD', 'RATING', 'REVIEW'].includes(form.orderType) && !isSpecialOrder"
              class="flow-section"
            >
              <div class="section-heading">
                <h2><span>* 5.</span> {{ t('applications.region') }}</h2>
              </div>
              <div class="single-control">
                <el-select
                  v-model="form.regionCode"
                  filterable
                  :placeholder="t('applications.selectRegion')"
                  :disabled="selectedAppRegionCodes.length === 0"
                >
                  <template #prefix>
                    <span v-if="form.regionCode" class="flag-icon">
                      <img :src="flagUrl(form.regionCode)" :alt="form.regionCode" />
                    </span>
                  </template>
                  <el-option
                    v-for="region in selectedAppRegions"
                    :key="region.code"
                    :label="regionLabel(region.code)"
                    :value="region.code"
                  >
                    <span class="option-row">
                      <span class="flag-icon"><img :src="flagUrl(region.code)" :alt="region.code" /></span>
                      {{ regionLabel(region.code) }}
                    </span>
                  </el-option>
                </el-select>
              </div>
            </section>

            <section v-if="!isSpecialOrder" class="flow-section flow-compact">
              <div class="step-field-grid">
                <div class="step-field">
                  <div class="section-heading">
                    <h2><span>* {{ form.orderType === 'KEYWORD_INSTALL' ? 5 : 6 }}.</span> {{ t('orderCreate.orderDate') }}</h2>
                  </div>
                  <el-date-picker
                    v-if="form.orderType === 'KEYWORD_INSTALL'"
                    v-model="keywordInstallDate"
                    type="date"
                    value-format="YYYY-MM-DD"
                    :placeholder="t('orderCreate.selectOrderDate')"
                  />
                  <el-date-picker
                    v-else
                    v-model="dateRange"
                    type="daterange"
                    value-format="YYYY-MM-DD"
                    range-separator="-"
                    :start-placeholder="t('ordersPage.startDate')"
                    :end-placeholder="t('ordersPage.endDate')"
                  />
                </div>
                <div class="system-time">
                  <span>System Time (UTC+8):</span>
                  <strong>{{ systemTimeText }}</strong>
                </div>
              </div>
            </section>

            <section v-if="form.orderType === 'KEYWORD_INSTALL'" class="flow-section">
              <div class="section-heading">
                <h2><span>* 6.</span> {{ t('orderCreate.executionHours') }}</h2>
              </div>
              <div class="single-control">
                <el-select v-model="form.executionHours" :placeholder="t('orderCreate.executionHours')">
                  <el-option v-for="hour in executionHourOptions" :key="hour" :label="`${hour}h`" :value="hour" />
                </el-select>
              </div>
            </section>

            <section v-if="form.orderType === 'KEYWORD_INSTALL'" class="flow-section">
              <div class="section-heading section-heading-actions">
                <h2><span>* 7.</span> {{ t('orderCreate.regionKeywords') }}</h2>
                <div class="region-tool-buttons">
                  <el-button size="small" type="primary" :icon="Download" @click="downloadKeywordTemplate">
                    {{ t('orderCreate.downloadTemplate') }}
                  </el-button>
                  <el-button size="small" type="success" :icon="Plus" @click="openKeywordImport">
                    {{ t('orderCreate.batchImport') }}
                  </el-button>
                  <input
                    ref="keywordImportInput"
                    class="visually-hidden-file"
                    type="file"
                    accept=".xlsx,.csv,text/csv,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                    @change="handleKeywordImport"
                  />
                </div>
              </div>
              <div class="region-groups">
                <div v-for="(group, groupIndex) in form.regionGroups" :key="groupIndex" class="region-group">
                  <div class="region-group-head">
                    <div class="region-select-line">
                      <span>{{ t('orderCreate.countryRegion') }}</span>
                      <el-select
                          v-model="group.regionCode"
                          filterable
                          :placeholder="t('orderCreate.selectCountryRegion')"
                          :disabled="selectedAppRegionCodes.length === 0"
                        >
                          <template #prefix>
                            <span v-if="group.regionCode" class="flag-icon">
                              <img :src="flagUrl(group.regionCode)" :alt="group.regionCode" />
                            </span>
                          </template>
                          <el-option
                            v-for="region in availableRegionsForGroup(groupIndex)"
                            :key="region.code"
                            :label="regionLabel(region.code)"
                            :value="region.code"
                          >
                            <span class="option-row">
                              <span class="flag-icon"><img :src="flagUrl(region.code)" :alt="region.code" /></span>
                              {{ regionLabel(region.code) }}
                            </span>
                          </el-option>
                      </el-select>
                      </div>
                    <div class="region-actions">
                      <span>{{ t('applications.actions') }}</span>
                      <el-button
                        size="small"
                        type="danger"
                        :icon="Delete"
                        class="remove-region-button"
                        :disabled="form.regionGroups.length === 1"
                        @click="removeRegionGroup(groupIndex)"
                      >
                        {{ t('orderCreate.removeRegionGroup') }}
                      </el-button>
                    </div>
                  </div>
                  <div class="keyword-table">
                    <div class="keyword-table-head">
                      <span>{{ t('orderCreate.keywords') }}</span>
                      <span>{{ t('orderCreate.dailyQuantity') }}</span>
                      <span>{{ t('applications.actions') }}</span>
                      <span></span>
                    </div>
                        <div v-for="(item, index) in group.keywordItems" :key="index" class="keyword-row">
                          <div class="keyword-cell">
                            <el-input
                              v-model.trim="item.keyword"
                              :placeholder="t('orderCreate.keywordPlaceholder')"
                            />
                          </div>
                          <div class="keyword-cell">
                            <el-input
                              v-model.number="item.quantity"
                              type="number"
                              min="1"
                              :placeholder="t('orderCreate.dailyQuantityPlaceholder')"
                            />
                          </div>
                          <div class="keyword-cell keyword-action-cell">
                            <el-button
                              type="danger"
                              text
                              :icon="Delete"
                              class="keyword-delete-button"
                              :disabled="group.keywordItems.length === 1"
                              @click="removeKeywordItem(groupIndex, index)"
                            />
                          </div>
                          <div class="keyword-cell keyword-empty-cell">
                            <el-button
                              v-if="index === 0"
                              size="small"
                              text
                              :icon="Plus"
                              class="add-keyword-button"
                              :disabled="!canAddKeywordItem(group)"
                              @click="addKeywordItem(groupIndex)"
                            >
                              {{ t('orderCreate.addKeyword') }}
                            </el-button>
                            <el-button
                              v-if="index === 0"
                              size="small"
                              text
                              :icon="Plus"
                              class="add-keyword-button"
                              @click="openBatchKeywordDialog(groupIndex)"
                            >
                              {{ t('orderCreate.batchKeywordImport') }}
                            </el-button>
                          </div>
                        </div>
                  </div>
                </div>
              </div>
              <el-button type="primary" :icon="Plus" class="add-region-button keyword-add-region-button" @click="addRegionGroup">
                {{ t('orderCreate.addRegionConfig') }}
              </el-button>
            </section>

            <section v-else class="flow-section">
              <div class="section-heading">
                <h2>
                  <span>* {{ ['DOWNLOAD', 'RATING', 'REVIEW'].includes(form.orderType) ? 7 : isSpecialOrder ? 6 : 7 }}.</span>
                  {{
                    form.orderType === 'DOWNLOAD'
                      ? t('orderCreate.regionDownloads')
                      : form.orderType === 'RATING'
                        ? t('orderCreate.regionRatings')
                        : form.orderType === 'REVIEW'
                          ? t('orderCreate.regionReviews')
                          : t('orderCreate.parameterSection')
                  }}
                </h2>
              </div>
              <div class="task-panel">
                <div
                  v-if="['DOWNLOAD', 'RATING', 'REVIEW'].includes(form.orderType)"
                  class="region-metric-groups"
                >
                  <div v-for="(group, groupIndex) in form.regionGroups" :key="groupIndex" class="region-metric-group">
                    <div class="region-metric-toolbar">
                      <div class="region-select-line">
                        <span>{{ t('orderCreate.countryRegion') }}</span>
                        <el-select
                          v-model="group.regionCode"
                          filterable
                          :placeholder="t('orderCreate.selectCountryRegion')"
                          :disabled="selectedAppRegionCodes.length === 0"
                        >
                          <template #prefix>
                            <span v-if="group.regionCode" class="flag-icon">
                              <img :src="flagUrl(group.regionCode)" :alt="group.regionCode" />
                            </span>
                          </template>
                          <el-option
                            v-for="region in availableRegionsForGroup(groupIndex)"
                            :key="region.code"
                            :label="regionLabel(region.code)"
                            :value="region.code"
                          >
                            <span class="option-row">
                              <span class="flag-icon"><img :src="flagUrl(region.code)" :alt="region.code" /></span>
                              {{ regionLabel(region.code) }}
                            </span>
                          </el-option>
                        </el-select>
                      </div>
                      <div class="region-action-line">
                        <span>{{ t('orderCreate.actions') }}</span>
                        <el-button
                          type="danger"
                          :icon="Delete"
                          size="small"
                          :disabled="form.regionGroups.length === 1"
                          @click="removeRegionGroup(groupIndex)"
                        >
                          {{ t('orderCreate.removeRegionGroup') }}
                        </el-button>
                      </div>
                    </div>

                    <div
                      v-if="form.orderType !== 'REVIEW'"
                      class="region-metric-table"
                      :class="form.orderType === 'DOWNLOAD' ? 'region-metric-table-download' : 'region-metric-table-score'"
                    >
                      <div class="region-metric-head">
                        <span v-if="form.orderType === 'DOWNLOAD'">{{ t('orderCreate.dailyDownloadCount') }}</span>
                        <template v-else-if="form.orderType === 'RATING'">
                          <span>{{ t('orderCreate.rating5Count') }}</span>
                          <span>{{ t('orderCreate.rating4Count') }}</span>
                        </template>
                        <span></span>
                      </div>
                      <div class="region-metric-row">
                        <div v-if="form.orderType === 'DOWNLOAD'" class="region-metric-cell">
                          <el-input-number v-model="group.dailyDownloadCount" :min="1" :step="1" />
                        </div>
                        <template v-else-if="form.orderType === 'RATING'">
                          <div class="region-metric-cell">
                            <el-input-number v-model="group.rating5Count" :min="0" :step="1" />
                          </div>
                          <div class="region-metric-cell">
                            <el-input-number v-model="group.rating4Count" :min="0" :step="1" />
                          </div>
                        </template>
                        <div class="region-metric-cell region-metric-empty-cell"></div>
                      </div>
                    </div>

                    <div v-else class="review-detail-table">
                      <div class="review-detail-head">
                        <span>{{ t('orderCreate.starLevel') }}</span>
                        <span>{{ t('orderCreate.reviewTitle') }}</span>
                        <span>{{ t('orderCreate.reviewContent') }}</span>
                        <span>{{ t('applications.actions') }}</span>
                        <span></span>
                      </div>
                      <div v-for="(item, index) in group.reviewItems" :key="index" class="review-detail-row">
                        <el-select v-model="item.starLevel" :placeholder="t('orderCreate.starLevel')">
                          <el-option :label="t('orderCreate.review5Count')" :value="5" />
                          <el-option :label="t('orderCreate.review4Count')" :value="4" />
                        </el-select>
                        <el-input v-model.trim="item.commentTitle" maxlength="120" :placeholder="t('orderCreate.reviewTitlePlaceholder')" />
                        <el-input v-model.trim="item.commentContent" maxlength="1000" :placeholder="t('orderCreate.reviewContentPlaceholder')" />
                        <div class="review-action-cell">
                          <el-button
                            type="danger"
                            text
                            :icon="Delete"
                            :disabled="group.reviewItems.length === 1"
                            @click="removeReviewItem(groupIndex, index)"
                          />
                        </div>
                        <div class="review-empty-cell">
                          <el-button
                            v-if="index === 0"
                            size="small"
                            text
                            :icon="Plus"
                            class="review-add-button"
                            :disabled="!canAddReviewItem(group)"
                            @click="addReviewItem(groupIndex)"
                          >
                            {{ t('orderCreate.addReview') }}
                          </el-button>
                        </div>
                      </div>
                    </div>
                  </div>
                  <el-button type="primary" :icon="Plus" class="add-region-button metric-add-region-button" @click="addRegionGroup">
                    {{ t('orderCreate.addRegionConfig') }}
                  </el-button>
                </div>

                <div v-else class="special-detail-panel">
                  <div class="region-groups">
                    <div v-for="(group, groupIndex) in form.specialGroups" :key="groupIndex" class="region-group special-region-group">
                      <div class="region-group-head">
                        <div class="region-select-line">
                          <span>{{ t('orderCreate.countryRegion') }}</span>
                          <el-select
                            v-model="group.regionCode"
                            filterable
                            :placeholder="t('orderCreate.selectCountryRegion')"
                            :disabled="selectedAppRegionCodes.length === 0"
                          >
                            <template #prefix>
                              <span v-if="group.regionCode" class="flag-icon">
                                <img :src="flagUrl(group.regionCode)" :alt="group.regionCode" />
                              </span>
                            </template>
                            <el-option
                              v-for="region in availableSpecialRegionsForGroup(groupIndex)"
                              :key="region.code"
                              :label="regionLabel(region.code)"
                              :value="region.code"
                            >
                              <span class="option-row">
                                <span class="flag-icon"><img :src="flagUrl(region.code)" :alt="region.code" /></span>
                                {{ regionLabel(region.code) }}
                              </span>
                            </el-option>
                          </el-select>
                        </div>
                        <div class="region-actions">
                          <span>{{ t('applications.actions') }}</span>
                          <el-button
                            size="small"
                            type="danger"
                            :icon="Delete"
                            class="remove-region-button"
                            :disabled="form.specialGroups.length === 1"
                            @click="removeSpecialGroup(groupIndex)"
                          >
                            {{ t('orderCreate.removeRegionGroup') }}
                          </el-button>
                        </div>
                      </div>
                      <div class="special-item-table">
                        <div class="special-item-head">
                          <span>{{ t('orderCreate.keywords') }}</span>
                          <span>{{ form.orderType === 'RANK_GUARANTEE' ? t('orderCreate.targetRank') : t('orderCreate.currentRank') }}</span>
                          <span>{{ t('applications.actions') }}</span>
                          <span></span>
                        </div>
                        <div v-for="(item, index) in group.items" :key="index" class="special-item-row">
                          <el-input v-model.trim="item.keyword" maxlength="255" :placeholder="t('orderCreate.keywordPlaceholder')" />
                          <el-select
                            v-if="form.orderType === 'RANK_GUARANTEE'"
                            v-model="item.targetRank"
                            :placeholder="t('orderCreate.targetRankPlaceholder')"
                          >
                            <el-option
                              v-for="option in targetRankOptions"
                              :key="option.value"
                              :label="option.label"
                              :value="option.value"
                            />
                          </el-select>
                          <el-input
                            v-else
                            v-model.trim="item.coverageNote"
                            maxlength="120"
                            :placeholder="t('orderCreate.currentRankPlaceholder')"
                          />
                          <div class="keyword-action-cell">
                            <el-button
                              type="danger"
                              text
                              :icon="Delete"
                              class="keyword-delete-button"
                              :disabled="group.items.length === 1"
                              @click="removeSpecialItem(groupIndex, index)"
                            />
                          </div>
                          <div class="keyword-empty-cell">
                            <el-button
                              v-if="index === 0"
                              size="small"
                              text
                              :icon="Plus"
                              class="add-keyword-button"
                              :disabled="!canAddSpecialItem(group)"
                              @click="addSpecialItem(groupIndex)"
                            >
                              {{ t('orderCreate.addKeyword') }}
                            </el-button>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                  <el-button type="primary" :icon="Plus" class="add-region-button special-add-region-button" @click="addSpecialGroup">
                    {{ t('orderCreate.addRegionConfig') }}
                  </el-button>
                </div>
              </div>
            </section>

          </main>

          <aside class="summary-panel">
            <div class="summary-heading">
              <span>{{ t('orderCreate.summarySection') }}</span>
            </div>
            <div class="summary-list">
              <div class="summary-item">
                <span>{{ t('ordersPage.type') }}</span>
                <strong>{{ typeLabel(form.orderType) }}</strong>
              </div>
              <div class="summary-item">
                <span>{{ t('orderCreate.store') }}</span>
                <strong>{{ storeLabel(form.storeType) }}</strong>
              </div>
              <div class="summary-item">
                <span>{{ t('orderCreate.selectedCustomer') }}</span>
                <strong>{{ selectedCustomer ? customerLabel(selectedCustomer) : '-' }}</strong>
              </div>
              <div class="summary-item">
                <span>{{ t('orderCreate.selectedApp') }}</span>
                <strong>{{ selectedApp?.appName || '-' }}</strong>
              </div>
              <div v-if="!isSpecialOrder" class="summary-item">
                <span>{{ t('orderCreate.days') }}</span>
                <strong>{{ totalDays }}</strong>
              </div>
              <div v-if="!isSpecialOrder" class="summary-item">
                <span>{{ t('orderCreate.quantity') }}</span>
                <strong>{{ quantity }}</strong>
              </div>
              <div v-if="form.orderType === 'KEYWORD_INSTALL'" class="summary-item">
                <span>{{ t('orderCreate.executionHours') }}</span>
                <strong>{{ form.executionHours || '-' }}</strong>
              </div>
            </div>
            <div class="billing-note">
              <strong>{{ t('orderCreate.billingNoteTitle') }}</strong>
              <span>{{ isSpecialOrder ? t('orderCreate.specialBillingPending') : t('orderCreate.billingNote') }}</span>
            </div>
            <div v-if="isSpecialOrder" class="special-amount-panel">
              <el-form-item :label="t('orderCreate.specialAmount')">
                <el-input-number
                  v-model="form.specialAmount"
                  :min="0.01"
                  :precision="2"
                  :step="10"
                  :placeholder="t('orderCreate.specialAmountPlaceholder')"
                />
              </el-form-item>
              <p>{{ t('orderCreate.specialAmountHelp') }}</p>
            </div>
            <div class="billing-breakdown">
              <div class="breakdown-title">{{ t('orderCreate.billingDetails') }}</div>
              <div class="breakdown-list">
                <div v-for="line in billingLines" :key="line.label" class="breakdown-line">
                  <div>
                    <strong>{{ line.label }}</strong>
                    <span>{{ line.formula }}</span>
                  </div>
                  <b>{{ line.amount }}</b>
                </div>
              </div>
            </div>
            <div class="summary-checkout">
              <div class="checkout-total">
                <span>{{ t('orderCreate.total') }}</span>
                <strong>{{ isSpecialOrder && Number(form.specialAmount || 0) <= 0 ? t('orderCreate.amountPendingInput') : money(estimatedAmount) }}</strong>
              </div>
              <el-button
                type="primary"
                class="submit-button"
                :loading="submitting"
                :disabled="submitDisabled"
                @click="submitOrder"
              >
                {{ t('orderCreate.adminSubmit') }}
              </el-button>
            </div>
          </aside>
        </div>
      </el-form>
    </div>
    <el-dialog
      v-model="batchKeywordDialogVisible"
      :title="t('orderCreate.batchKeywordDialogTitle')"
      width="420px"
      class="batch-keyword-dialog"
      @closed="batchKeywordText = ''"
    >
      <div class="batch-keyword-body">
        <p class="batch-keyword-tip">{{ t('orderCreate.batchKeywordDialogTip') }}</p>
        <el-input
          v-model="batchKeywordText"
          class="batch-keyword-input"
          type="textarea"
          :rows="12"
          resize="vertical"
          :placeholder="t('orderCreate.batchKeywordPlaceholder')"
        />
      </div>
      <template #footer>
        <el-button @click="batchKeywordDialogVisible = false">{{ t('orderCreate.batchKeywordCancel') }}</el-button>
        <el-button type="primary" @click="confirmBatchKeywordImport">{{ t('orderCreate.batchKeywordConfirm') }}</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ChatDotRound, Delete, Download, Grid, Lock, Plus, Search, Star } from '@element-plus/icons-vue'
import { getAdminApps, getEnabledRegions, type CustomerApp, type MarketRegion, type StoreType } from '@/api/applications'
import { getAdminCustomers, type CustomerAccount } from '@/api/customers'
import {
  createAdminOrderForCustomer,
  getAdminOrder,
  type AdminCreateOrderPayload,
  type Order,
  type OrderStatus,
  type OrderType
} from '@/api/orders'
import { getPricingConfig, type PriceCode } from '@/api/pricing'
import { getAdminSpecialAudits, type SpecialOrderAudit } from '@/api/specialOrderAudits'
import StoreIcon from '@/components/StoreIcon.vue'

interface OrderForm {
  customerId: number | ''
  orderType: OrderType
  storeType: StoreType
  customerAppId: number | ''
  regionCode: string
  executionHours: number
  regionGroups: RegionKeywordGroup[]
  specialGroups: SpecialRegionGroup[]
  specialAmount: number | null
}

interface KeywordInput {
  keyword: string
  quantity: number | null
}

interface RegionKeywordGroup {
  regionCode: string
  keywordItems: KeywordInput[]
  reviewItems: ReviewInput[]
  dailyDownloadCount: number | null
  rating5Count: number
  rating4Count: number
  review5Count: number
  review4Count: number
}

interface ReviewInput {
  starLevel: 4 | 5
  commentTitle: string
  commentContent: string
}

interface SpecialKeywordInput {
  regionCode: string
  keyword: string
  targetRank: number | null
  coverageNote: string
}

interface SpecialRegionGroup {
  regionCode: string
  items: SpecialKeywordInput[]
}

interface ServiceOption {
  code: OrderType
  icon: typeof Search
}

interface BillingLine {
  label: string
  formula: string
  amount: string
}

const route = useRoute()
const router = useRouter()
const { t, locale } = useI18n()

const targetRankOptions = [
  { label: 'top1', value: 1 },
  { label: 'top2', value: 2 },
  { label: 'top3', value: 3 },
  { label: 'top5', value: 5 },
  { label: 'top10', value: 10 }
]

const executionHourOptions = [1, 2, 4, 6, 8, 12, 16, 20, 24]

const loading = ref(false)
const submitting = ref(false)
const hydratedRenewOrderId = ref<number | null>(null)
const baseDataReady = ref(false)
const keywordImportInput = ref<HTMLInputElement | null>(null)
const batchKeywordDialogVisible = ref(false)
const batchKeywordText = ref('')
const batchKeywordGroupIndex = ref<number | null>(null)
const customers = ref<CustomerAccount[]>([])
const apps = ref<CustomerApp[]>([])
const regions = ref<MarketRegion[]>([])
const regionCodeAliases: Record<string, string> = {
  UK: 'GB',
  RUSSIA: 'RU',
  'RUSSIAN FEDERATION': 'RU',
  '俄罗斯': 'RU'
}
const dateRange = ref<[string, string] | ''>([today(), today()])
const keywordInstallDate = ref(today())
const systemNow = ref(new Date())
let systemTimer: number | undefined
let hydratingOrder = false
const priceMap = reactive<Record<PriceCode, number>>({
  KEYWORD_INSTALL: 0,
  DOWNLOAD: 0,
  RATING_5: 0,
  RATING_4: 0,
  REVIEW_5: 0,
  REVIEW_4: 0
})

const form = reactive<OrderForm>({
  customerId: '',
  orderType: 'KEYWORD_INSTALL',
  storeType: 'APP_STORE',
  customerAppId: '',
  regionCode: '',
  executionHours: 1,
  regionGroups: [createRegionGroup()],
  specialGroups: [createSpecialGroup()],
  specialAmount: null
})

const regionGroupDrafts = reactive<Partial<Record<OrderType, RegionKeywordGroup[]>>>({
  KEYWORD_INSTALL: [createRegionGroup()]
})

const services: ServiceOption[] = [
  { code: 'KEYWORD_INSTALL', icon: Search },
  { code: 'DOWNLOAD', icon: Download },
  { code: 'RATING', icon: Star },
  { code: 'REVIEW', icon: ChatDotRound },
  { code: 'RANK_GUARANTEE', icon: Lock },
  { code: 'KEYWORD_COVERAGE', icon: Grid }
]

const filteredApps = computed(() => {
  return apps.value.filter((app) => app.customerId === form.customerId && app.storeType === form.storeType)
})
const selectedCustomer = computed(() => customers.value.find((customer) => customer.id === form.customerId) || null)
const selectedApp = computed(() => filteredApps.value.find((app) => app.id === form.customerAppId) || null)
const selectedAppRegions = computed(() => {
  return regions.value
    .filter((region) => supportsStore(region, form.storeType))
})
const selectedAppRegionCodes = computed(() => {
  return selectedAppRegions.value
    .map((region) => region.code)
})
function normalizeImportRegionCode(value: string) {
  const code = value.trim().toUpperCase()
  return regionCodeAliases[code] || code
}
const isSpecialOrder = computed(() => form.orderType === 'RANK_GUARANTEE' || form.orderType === 'KEYWORD_COVERAGE')
const keywordItems = computed(() => {
  const result = new Map<string, number>()
  form.regionGroups.forEach((group) => {
    const regionCode = group.regionCode
    group.keywordItems.forEach((item) => {
      const keyword = item.keyword.trim()
      const quantity = Number(item.quantity)
      if (regionCode && keyword && quantity > 0) {
        const key = `${regionCode}\u0000${keyword}`
        result.set(key, (result.get(key) || 0) + quantity)
      }
    })
  })
  return Array.from(result.entries()).map(([key, quantity]) => {
    const [regionCode, keyword] = key.split('\u0000')
    return { regionCode, keyword, quantity }
  })
})
const regionItems = computed(() => {
  return form.regionGroups
    .filter((group) => group.regionCode)
    .map((group) => ({
      regionCode: group.regionCode,
      dailyDownloadCount: group.dailyDownloadCount,
      rating5Count: group.rating5Count,
      rating4Count: group.rating4Count,
      review5Count: group.review5Count,
      review4Count: group.review4Count
    }))
    .filter((item) => {
      if (form.orderType === 'DOWNLOAD') return Number(item.dailyDownloadCount || 0) > 0
      if (form.orderType === 'RATING') return Number(item.rating5Count || 0) + Number(item.rating4Count || 0) > 0
      if (form.orderType === 'REVIEW') return Number(item.review5Count || 0) + Number(item.review4Count || 0) > 0
      return false
    })
})
const reviewDetails = computed(() => {
  return form.regionGroups.flatMap((group) => {
    if (!group.regionCode) return []
    return group.reviewItems
      .filter((item) => item.commentTitle.trim() && item.commentContent.trim())
      .map((item) => ({
        regionCode: group.regionCode,
        starLevel: item.starLevel,
        commentTitle: item.commentTitle.trim(),
        commentContent: item.commentContent.trim()
      }))
  })
})
const specialItems = computed(() => {
  return form.specialGroups
    .flatMap((group) => group.items.map((item) => ({
      regionCode: group.regionCode,
      keyword: item.keyword.trim(),
      targetRank: form.orderType === 'RANK_GUARANTEE' ? Number(item.targetRank || 0) : null,
      coverageNote: form.orderType === 'KEYWORD_COVERAGE' ? item.coverageNote.trim() : null
    })))
    .filter((item) => item.regionCode && item.keyword)
    .map((item) => ({
      ...item,
      coverageNote: item.coverageNote || null
    }))
    .filter((item) => form.orderType !== 'RANK_GUARANTEE' || Number(item.targetRank || 0) > 0)
})
const totalDailyDownloadCount = computed(() => {
  return regionItems.value.reduce((sum, item) => sum + Number(item.dailyDownloadCount || 0), 0)
})
const totalRating5Count = computed(() => {
  return regionItems.value.reduce((sum, item) => sum + Number(item.rating5Count || 0), 0)
})
const totalRating4Count = computed(() => {
  return regionItems.value.reduce((sum, item) => sum + Number(item.rating4Count || 0), 0)
})
const totalReview5Count = computed(() => {
  return reviewDetails.value.filter((item) => item.starLevel === 5).length
})
const totalReview4Count = computed(() => {
  return reviewDetails.value.filter((item) => item.starLevel === 4).length
})
const totalDays = computed(() => {
  if (!dateRange.value) return 0
  const [start, end] = dateRange.value
  const startDate = Date.parse(`${start}T00:00:00Z`)
  const endDate = Date.parse(`${end}T00:00:00Z`)
  if (Number.isNaN(startDate) || Number.isNaN(endDate) || endDate < startDate) return 0
  return Math.floor((endDate - startDate) / 86400000) + 1
})
const quantity = computed(() => {
  if (form.orderType === 'KEYWORD_INSTALL') return keywordItems.value.reduce((sum, item) => sum + item.quantity, 0)
  if (form.orderType === 'DOWNLOAD') return totalDays.value * totalDailyDownloadCount.value
  if (form.orderType === 'RATING') return totalDays.value * (totalRating5Count.value + totalRating4Count.value)
  if (form.orderType === 'REVIEW') return totalReview5Count.value + totalReview4Count.value
  return 0
})
const estimatedAmount = computed(() => {
  if (form.orderType === 'KEYWORD_INSTALL') return quantity.value * priceMap.KEYWORD_INSTALL
  if (form.orderType === 'DOWNLOAD') return quantity.value * priceMap.DOWNLOAD
  if (form.orderType === 'RATING') {
    return totalDays.value * (totalRating5Count.value * priceMap.RATING_5 + totalRating4Count.value * priceMap.RATING_4)
  }
  if (form.orderType === 'REVIEW') {
    return totalReview5Count.value * priceMap.REVIEW_5 + totalReview4Count.value * priceMap.REVIEW_4
  }
  if (isSpecialOrder.value) return Number(form.specialAmount || 0)
  return 0
})
const billingLines = computed<BillingLine[]>(() => {
  if (form.orderType === 'KEYWORD_INSTALL') {
    return [{
      label: t('ordersPage.types.KEYWORD_INSTALL'),
      formula: `${t('orderCreate.keywordCount')} ${quantity.value} × ${t('orderCreate.unitPrice')} ${money(priceMap.KEYWORD_INSTALL)}`,
      amount: money(estimatedAmount.value)
    }]
  }
  if (form.orderType === 'DOWNLOAD') {
    return [{
      label: t('ordersPage.types.DOWNLOAD'),
      formula: `${t('orderCreate.days')} ${totalDays.value} × ${t('orderCreate.dailyDownloadCount')} ${totalDailyDownloadCount.value || 0} × ${t('orderCreate.unitPrice')} ${money(priceMap.DOWNLOAD)}`,
      amount: money(estimatedAmount.value)
    }]
  }
  if (form.orderType === 'RATING') {
    return [
      {
        label: t('orderCreate.rating5Count'),
        formula: `${t('orderCreate.days')} ${totalDays.value} × ${totalRating5Count.value || 0} × ${t('orderCreate.unitPrice')} ${money(priceMap.RATING_5)}`,
        amount: money(totalDays.value * (totalRating5Count.value || 0) * priceMap.RATING_5)
      },
      {
        label: t('orderCreate.rating4Count'),
        formula: `${t('orderCreate.days')} ${totalDays.value} × ${totalRating4Count.value || 0} × ${t('orderCreate.unitPrice')} ${money(priceMap.RATING_4)}`,
        amount: money(totalDays.value * (totalRating4Count.value || 0) * priceMap.RATING_4)
      }
    ]
  }
  if (form.orderType === 'REVIEW') {
    return [
      {
        label: t('orderCreate.review5Count'),
        formula: `${totalReview5Count.value || 0} × ${t('orderCreate.unitPrice')} ${money(priceMap.REVIEW_5)}`,
        amount: money((totalReview5Count.value || 0) * priceMap.REVIEW_5)
      },
      {
        label: t('orderCreate.review4Count'),
        formula: `${totalReview4Count.value || 0} × ${t('orderCreate.unitPrice')} ${money(priceMap.REVIEW_4)}`,
        amount: money((totalReview4Count.value || 0) * priceMap.REVIEW_4)
      }
    ]
  }
  return [{
    label: t('orderCreate.specialAmount'),
    formula: t('orderCreate.adminSpecialBillingFormula'),
    amount: Number(form.specialAmount || 0) > 0 ? money(estimatedAmount.value) : t('orderCreate.amountPendingInput')
  }]
})
const submitDisabled = computed(() => {
  if (submitting.value || !selectedCustomer.value || !selectedApp.value) return true
  if (isSpecialOrder.value) {
    return specialItems.value.length === 0
      || Number(form.specialAmount || 0) <= 0
  }
  if (form.orderType === 'KEYWORD_INSTALL') return !keywordInstallDate.value || quantity.value <= 0
  return !dateRange.value || quantity.value <= 0
})
const systemTimeText = computed(() => formatSystemTime(systemNow.value))

onMounted(() => {
  loadData()
  systemTimer = window.setInterval(() => {
    systemNow.value = new Date()
  }, 1000)
})

onUnmounted(() => {
  if (systemTimer) window.clearInterval(systemTimer)
})

watch(
  () => route.query,
  () => {
    applyRouteQuery()
    void applyRenewOrderQuery()
  },
  { deep: true }
)

watch(
  () => [form.customerId, form.storeType],
  () => {
    if (hydratingOrder) return
    if (form.customerAppId && !filteredApps.value.some((app) => app.id === form.customerAppId)) {
      form.customerAppId = ''
      form.regionCode = ''
    }
  }
)

watch(
  () => form.customerAppId,
  () => {
    if (hydratingOrder) return
    setDefaultRegions()
  }
)

watch(
  () => form.orderType,
  (newType, oldType) => {
    if (hydratingOrder) return
    saveRegionGroupDraft(oldType)
    loadRegionGroupDraft(newType)
  }
)

async function loadData() {
  loading.value = true
  try {
    const [customerList, appList, regionList, pricingList] = await Promise.all([
      getAdminCustomers(),
      getAdminApps(),
      getEnabledRegions(),
      getPricingConfig()
    ])
    customers.value = customerList
    apps.value = appList
    regions.value = regionList
    pricingList.forEach((item) => {
      priceMap[item.code] = Number(item.unitPrice)
    })
    baseDataReady.value = true
    applyRouteQuery()
    await applyRenewOrderQuery()
  } catch (error) {
    ElMessage.error(errorMessage(error, t('orderCreate.loadFailed')))
  } finally {
    loading.value = false
  }
}

async function submitOrder() {
  if (!selectedCustomer.value || !selectedApp.value || (!isSpecialOrder.value && !selectedOrderDates())) {
    ElMessage.warning(t('orderCreate.adminRequiredFields'))
    return
  }
  if (!isSpecialOrder.value && form.orderType !== 'KEYWORD_INSTALL' && form.orderType !== 'REVIEW' && regionItems.value.length === 0) {
    ElMessage.warning(t('orderCreate.adminRequiredFields'))
    return
  }
  if (isSpecialOrder.value) {
    if (specialItems.value.length === 0) {
      ElMessage.warning(t('orderCreate.specialItemsRequired'))
      return
    }
    if (Number(form.specialAmount || 0) <= 0) {
      ElMessage.warning(t('orderCreate.specialAmountRequired'))
      return
    }
  } else if (quantity.value <= 0) {
    ElMessage.warning(t('orderCreate.quantityRequired'))
    return
  }

  const [startDate, endDate] = isSpecialOrder.value ? [today(), today()] : (selectedOrderDates() as [string, string])
  const payload: AdminCreateOrderPayload = {
    customerId: selectedCustomer.value.id,
    customerAppId: selectedApp.value.id,
    regionCode: form.orderType === 'KEYWORD_INSTALL' ? null : isSpecialOrder.value ? specialItems.value[0]?.regionCode || form.regionCode : regionItems.value[0]?.regionCode || null,
    orderType: form.orderType,
    startDate,
    endDate,
    executionHours: form.orderType === 'KEYWORD_INSTALL' ? form.executionHours : null,
    keywords: [],
    keywordItems: form.orderType === 'KEYWORD_INSTALL' ? keywordItems.value : [],
    regionItems: ['DOWNLOAD', 'RATING'].includes(form.orderType) ? regionItems.value : [],
    reviewDetails: form.orderType === 'REVIEW' ? reviewDetails.value : [],
    dailyDownloadCount: null,
    rating5Count: null,
    rating4Count: null,
    review5Count: null,
    review4Count: null,
    specialItems: isSpecialOrder.value ? specialItems.value : [],
    contactType: null,
    contactValue: null,
    specialAmount: isSpecialOrder.value ? Number(form.specialAmount) : null
  }

  try {
    await ElMessageBox.confirm(
      t('orderCreate.adminConfirmMessage', {
        customer: customerLabel(selectedCustomer.value),
        amount: money(estimatedAmount.value)
      }),
      t('orderCreate.adminConfirmTitle'),
      {
        confirmButtonText: t('orderCreate.adminSubmit'),
        cancelButtonText: t('applications.cancel'),
        type: 'warning'
      }
    )
  } catch {
    return
  }

  submitting.value = true
  try {
    const result = await createAdminOrderForCustomer(payload)
    if (result.waitPayment) {
      ElMessage.warning(result.audit ? t('orderCreate.adminSpecialWaitPaymentSuccess') : t('orderCreate.balanceNotEnough'))
      await router.push(result.audit ? { name: 'admin-audits' } : orderListRoute(form.storeType))
      return
    }
    ElMessage.success(t('orderCreate.adminCreateSuccess'))
    await router.push(orderListRoute(form.storeType))
  } catch (error) {
    ElMessage.error(errorMessage(error, t('orderCreate.createFailed')))
  } finally {
    submitting.value = false
  }
}

function selectedOrderDates(): [string, string] | null {
  if (form.orderType === 'KEYWORD_INSTALL') {
    return keywordInstallDate.value ? [keywordInstallDate.value, keywordInstallDate.value] : null
  }
  return dateRange.value || null
}

function createRegionGroup(regionCode = ''): RegionKeywordGroup {
  return {
    regionCode,
    keywordItems: [{ keyword: '', quantity: null }],
    reviewItems: [createReviewItem()],
    dailyDownloadCount: null,
    rating5Count: 0,
    rating4Count: 0,
    review5Count: 0,
    review4Count: 0
  }
}

function createReviewItem(): ReviewInput {
  return {
    starLevel: 5,
    commentTitle: '',
    commentContent: ''
  }
}

function createSpecialItem(): SpecialKeywordInput {
  return {
    regionCode: '',
    keyword: '',
    targetRank: 1,
    coverageNote: ''
  }
}

function createSpecialGroup(regionCode = ''): SpecialRegionGroup {
  return {
    regionCode,
    items: [createSpecialItem()]
  }
}

function usesRegionGroups(orderType: OrderType) {
  return orderType === 'KEYWORD_INSTALL'
    || orderType === 'DOWNLOAD'
    || orderType === 'RATING'
    || orderType === 'REVIEW'
}

function cloneRegionGroup(group: RegionKeywordGroup): RegionKeywordGroup {
  return {
    regionCode: group.regionCode,
    keywordItems: group.keywordItems.map((item) => ({ ...item })),
    reviewItems: group.reviewItems.map((item) => ({ ...item })),
    dailyDownloadCount: group.dailyDownloadCount,
    rating5Count: group.rating5Count,
    rating4Count: group.rating4Count,
    review5Count: group.review5Count,
    review4Count: group.review4Count
  }
}

function cloneRegionGroups(groups: RegionKeywordGroup[] | undefined, fallbackRegionCode = '') {
  return groups?.length ? groups.map(cloneRegionGroup) : [createRegionGroup(fallbackRegionCode)]
}

function saveRegionGroupDraft(orderType: OrderType) {
  if (!usesRegionGroups(orderType)) return
  regionGroupDrafts[orderType] = cloneRegionGroups(form.regionGroups)
}

function loadRegionGroupDraft(orderType: OrderType) {
  if (!usesRegionGroups(orderType)) return
  form.regionGroups = cloneRegionGroups(regionGroupDrafts[orderType])
}

function availableRegionsForGroup(groupIndex: number) {
  const currentRegionCode = form.regionGroups[groupIndex]?.regionCode
  const usedRegionCodes = new Set(
    form.regionGroups
      .map((group, index) => index === groupIndex ? '' : group.regionCode)
      .filter(Boolean)
  )
  return selectedAppRegions.value.filter((region) => {
    return region.code === currentRegionCode || !usedRegionCodes.has(region.code)
  })
}

function availableSpecialRegionsForGroup(groupIndex: number) {
  const currentRegionCode = form.specialGroups[groupIndex]?.regionCode
  const usedRegionCodes = new Set(
    form.specialGroups
      .map((group, index) => index === groupIndex ? '' : group.regionCode)
      .filter(Boolean)
  )
  return selectedAppRegions.value.filter((region) => {
    return region.code === currentRegionCode || !usedRegionCodes.has(region.code)
  })
}

function addRegionGroup() {
  form.regionGroups.push(createRegionGroup())
}

function removeRegionGroup(index: number) {
  if (form.regionGroups.length <= 1) return
  form.regionGroups.splice(index, 1)
}

function addKeywordItem(groupIndex: number) {
  const group = form.regionGroups[groupIndex]
  if (!group) return
  if (!canAddKeywordItem(group)) {
    ElMessage.warning(t('orderCreate.keywordItemRequired'))
    return
  }
  group.keywordItems.unshift({ keyword: '', quantity: null })
}

function canAddKeywordItem(group: RegionKeywordGroup | undefined) {
  const firstItem = group?.keywordItems[0]
  return Boolean(firstItem?.keyword?.trim()) && Number(firstItem?.quantity || 0) > 0
}

function removeKeywordItem(groupIndex: number, index: number) {
  const group = form.regionGroups[groupIndex]
  if (!group || group.keywordItems.length <= 1) return
  group.keywordItems.splice(index, 1)
}

function openBatchKeywordDialog(groupIndex: number) {
  if (!form.regionGroups[groupIndex]) return
  batchKeywordGroupIndex.value = groupIndex
  batchKeywordText.value = ''
  batchKeywordDialogVisible.value = true
}

function confirmBatchKeywordImport() {
  const groupIndex = batchKeywordGroupIndex.value
  const group = groupIndex === null ? undefined : form.regionGroups[groupIndex]
  if (!group) return
  let parsedItems: KeywordInput[]
  try {
    parsedItems = parseBatchKeywordText(batchKeywordText.value)
  } catch (error) {
    ElMessage.warning(error instanceof Error ? error.message : t('orderCreate.batchKeywordInvalidRow', { row: 1 }))
    return
  }
  if (parsedItems.length === 0) {
    ElMessage.warning(t('orderCreate.batchKeywordEmpty'))
    return
  }

  const keywordMap = new Map<string, number>()
  group.keywordItems.forEach((item) => {
    const keyword = item.keyword.trim()
    const quantity = Number(item.quantity)
    if (keyword && Number.isInteger(quantity) && quantity > 0) {
      keywordMap.set(keyword, (keywordMap.get(keyword) || 0) + quantity)
    }
  })
  parsedItems.forEach((item) => {
    keywordMap.set(item.keyword, (keywordMap.get(item.keyword) || 0) + Number(item.quantity))
  })
  group.keywordItems = Array.from(keywordMap.entries()).map(([keyword, quantity]) => ({ keyword, quantity }))
  regionGroupDrafts.KEYWORD_INSTALL = cloneRegionGroups(form.regionGroups)
  batchKeywordDialogVisible.value = false
  ElMessage.success(t('orderCreate.batchKeywordImportSuccess', { count: parsedItems.length }))
}

function parseBatchKeywordText(text: string): KeywordInput[] {
  return text
    .split(/\r?\n/)
    .map((line, index): KeywordInput | null => {
      const value = line.trim()
      if (!value) return null
      const match = value.match(/^(.+?)\s+(\d+)$/)
      if (!match) {
        throw new Error(t('orderCreate.batchKeywordInvalidRow', { row: index + 1 }))
      }
      const keyword = match[1].trim()
      const quantity = Number(match[2])
      if (!keyword || !Number.isInteger(quantity) || quantity <= 0) {
        throw new Error(t('orderCreate.batchKeywordInvalidRow', { row: index + 1 }))
      }
      return { keyword, quantity }
    })
    .filter((item): item is KeywordInput => Boolean(item))
}

async function downloadKeywordTemplate() {
  const exampleRegions = selectedAppRegions.value.slice(0, 2)
  const workbook = await createExcelWorkbook()
  workbook.creator = 'Youou-ASO'
  workbook.created = new Date()
  const sheet = workbook.addWorksheet('Keyword Import')
  sheet.columns = [
    { header: 'regionCode', key: 'regionCode', width: 18 },
    { header: 'keyword', key: 'keyword', width: 32 },
    { header: 'dailyQuantity', key: 'dailyQuantity', width: 18 }
  ]
  sheet.getRow(1).font = { bold: true }
  sheet.addRow({
    regionCode: exampleRegions[0]?.code || 'US',
    keyword: 'example keyword',
    dailyQuantity: 10
  })
  sheet.addRow({
    regionCode: exampleRegions[1]?.code || exampleRegions[0]?.code || 'US',
    keyword: 'another keyword',
    dailyQuantity: 20
  })
  const buffer = await workbook.xlsx.writeBuffer()
  downloadBlobFile(
    t('orderCreate.keywordTemplateFilename'),
    new Blob([buffer as BlobPart], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
  )
}

function openKeywordImport() {
  if (form.orderType !== 'KEYWORD_INSTALL') return
  keywordImportInput.value?.click()
}

async function handleKeywordImport(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  input.value = ''
  if (!file) return
  const fileName = file.name.toLowerCase()
  const isCsv = fileName.endsWith('.csv') || file.type.includes('csv')
  const isXlsx = fileName.endsWith('.xlsx') || file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
  if (!isCsv && !isXlsx) {
    ElMessage.warning(t('orderCreate.keywordImportInvalidFile'))
    return
  }
  try {
    const rows = isCsv ? parseCsv(await readTextFile(file)) : await readKeywordExcelRows(file)
    importKeywordRows(rows)
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : t('orderCreate.keywordImportReadFailed'))
  }
}

function importKeywordRows(importRows: string[][]) {
  const rows = importRows
    .map((row) => row.map((cell) => cell.trim()))
    .filter((row) => row.some(Boolean))
  const hasHeader = rows[0]?.some((cell) => ['regioncode', 'keyword', 'dailyquantity'].includes(cell.toLowerCase())) || false
  const dataRows = hasHeader ? rows.slice(1) : rows
  const startRowNumber = hasHeader ? 2 : 1
  if (dataRows.length === 0) {
    ElMessage.warning(t('orderCreate.keywordImportEmpty'))
    return
  }

  const supportedRegions = new Set(selectedAppRegions.value.map((region) => region.code.toUpperCase()))
  const groupMap = new Map<string, Map<string, number>>()
  dataRows.forEach((row, index) => {
    const regionCode = normalizeImportRegionCode(row[0] || '')
    const keyword = row[1] || ''
    const dailyQuantity = Number(row[2])
    if (!regionCode || !keyword || row[2] === undefined) {
      throw new Error(t('orderCreate.keywordImportInvalidRow', { row: startRowNumber + index }))
    }
    if (!supportedRegions.has(regionCode)) {
      throw new Error(t('orderCreate.keywordImportUnsupportedRegion', { row: startRowNumber + index, region: regionCode }))
    }
    if (!Number.isInteger(dailyQuantity) || dailyQuantity <= 0) {
      throw new Error(t('orderCreate.keywordImportInvalidQuantity', { row: startRowNumber + index }))
    }
    if (!groupMap.has(regionCode)) {
      groupMap.set(regionCode, new Map<string, number>())
    }
    const keywordMap = groupMap.get(regionCode) as Map<string, number>
    keywordMap.set(keyword, (keywordMap.get(keyword) || 0) + dailyQuantity)
  })

  form.regionGroups = Array.from(groupMap.entries()).map(([regionCode, keywordMap]) => {
    const group = createRegionGroup(regionCode)
    group.keywordItems = Array.from(keywordMap.entries()).map(([keyword, quantity]) => ({ keyword, quantity }))
    return group
  })
  regionGroupDrafts.KEYWORD_INSTALL = cloneRegionGroups(form.regionGroups)
  ElMessage.success(t('orderCreate.keywordImportSuccess', { count: dataRows.length }))
}

async function readKeywordExcelRows(file: File) {
  const workbook = await createExcelWorkbook()
  await workbook.xlsx.load(await file.arrayBuffer())
  const sheet = workbook.worksheets[0]
  if (!sheet) return []
  const rows: string[][] = []
  sheet.eachRow({ includeEmpty: false }, (row) => {
    rows.push([
      excelCellText(row.getCell(1).value),
      excelCellText(row.getCell(2).value),
      excelCellText(row.getCell(3).value)
    ])
  })
  return rows
}

async function createExcelWorkbook() {
  const ExcelJS = await import('exceljs')
  return new ExcelJS.Workbook()
}

function excelCellText(value: unknown): string {
  if (value === null || value === undefined) return ''
  if (value instanceof Date) return value.toISOString()
  if (typeof value !== 'object') return String(value)
  const record = value as Record<string, unknown>
  if (typeof record.text === 'string') return record.text
  if (record.result !== undefined && record.result !== null) return String(record.result)
  if (Array.isArray(record.richText)) {
    return record.richText
      .map((part) => typeof part === 'object' && part !== null && 'text' in part ? String((part as { text?: unknown }).text || '') : '')
      .join('')
  }
  return String(value)
}

function readTextFile(file: File) {
  return new Promise<string>((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => resolve(String(reader.result || '').replace(/^\uFEFF/, ''))
    reader.onerror = () => reject(reader.error)
    reader.readAsText(file, 'utf-8')
  })
}

function parseCsv(content: string) {
  const rows: string[][] = []
  let row: string[] = []
  let cell = ''
  let inQuotes = false
  for (let index = 0; index < content.length; index += 1) {
    const char = content[index]
    const next = content[index + 1]
    if (inQuotes) {
      if (char === '"' && next === '"') {
        cell += '"'
        index += 1
      } else if (char === '"') {
        inQuotes = false
      } else {
        cell += char
      }
      continue
    }
    if (char === '"') {
      inQuotes = true
    } else if (char === ',') {
      row.push(cell)
      cell = ''
    } else if (char === '\n') {
      row.push(cell)
      rows.push(row)
      row = []
      cell = ''
    } else if (char !== '\r') {
      cell += char
    }
  }
  row.push(cell)
  rows.push(row)
  return rows
}

function downloadBlobFile(filename: string, blob: Blob) {
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  link.click()
  URL.revokeObjectURL(url)
}

function addReviewItem(groupIndex: number) {
  const group = form.regionGroups[groupIndex]
  if (!group) return
  if (!canAddReviewItem(group)) {
    ElMessage.warning(t('orderCreate.reviewItemRequired'))
    return
  }
  group.reviewItems.unshift(createReviewItem())
}

function canAddReviewItem(group: RegionKeywordGroup | undefined) {
  const firstItem = group?.reviewItems[0]
  return Boolean(firstItem?.starLevel)
    && Boolean(firstItem?.commentTitle?.trim())
    && Boolean(firstItem?.commentContent?.trim())
}

function removeReviewItem(groupIndex: number, index: number) {
  const group = form.regionGroups[groupIndex]
  if (!group || group.reviewItems.length <= 1) return
  group.reviewItems.splice(index, 1)
}

function addSpecialGroup() {
  form.specialGroups.push(createSpecialGroup())
}

function removeSpecialGroup(index: number) {
  if (form.specialGroups.length <= 1) return
  form.specialGroups.splice(index, 1)
}

function addSpecialItem(groupIndex: number) {
  const group = form.specialGroups[groupIndex]
  if (!group) return
  if (!canAddSpecialItem(group)) {
    ElMessage.warning(t('orderCreate.specialItemRequired'))
    return
  }
  group.items.unshift(createSpecialItem())
}

function canAddSpecialItem(group: SpecialRegionGroup | undefined) {
  const firstItem = group?.items[0]
  if (!firstItem?.keyword?.trim()) return false
  if (form.orderType === 'RANK_GUARANTEE') return Number(firstItem.targetRank || 0) > 0
  return true
}

function removeSpecialItem(groupIndex: number, index: number) {
  const group = form.specialGroups[groupIndex]
  if (!group || group.items.length <= 1) return
  group.items.splice(index, 1)
}

function setDefaultRegions() {
  form.regionCode = ''
  regionGroupDrafts.KEYWORD_INSTALL = [createRegionGroup()]
  regionGroupDrafts.DOWNLOAD = [createRegionGroup()]
  regionGroupDrafts.RATING = [createRegionGroup()]
  regionGroupDrafts.REVIEW = [createRegionGroup()]
  form.specialGroups = [createSpecialGroup()]
  loadRegionGroupDraft(form.orderType)
}

async function applyRenewOrderQuery() {
  const queryRenewOrderId = Number(route.query.renewOrderId)
  if (!Number.isFinite(queryRenewOrderId)) {
    hydratedRenewOrderId.value = null
    return
  }
  if (!baseDataReady.value) {
    return
  }
  if (hydratedRenewOrderId.value === queryRenewOrderId) {
    return
  }
  try {
    const order = await getAdminOrder(queryRenewOrderId)
    if (!isRenewableOrderStatus(order.status)) {
      ElMessage.warning(t('ordersPage.renewOrderUnavailable'))
      hydratedRenewOrderId.value = null
      return
    }
    if (order.sourceAuditId) {
      const audit = await loadRenewSourceAudit(order.sourceAuditId)
      if (!audit) {
        ElMessage.warning(t('ordersPage.renewOrderUnavailable'))
        hydratedRenewOrderId.value = null
        return
      }
      hydrateSpecialRenewAudit(order, audit)
    } else {
      hydrateRenewOrder(order)
    }
    hydratedRenewOrderId.value = order.id
  } catch (error) {
    ElMessage.error(errorMessage(error, t('orderCreate.loadFailed')))
    hydratedRenewOrderId.value = null
  }
}

async function loadRenewSourceAudit(sourceAuditId: number) {
  const audits = await getAdminSpecialAudits()
  return audits.find((audit) => audit.id === sourceAuditId) || null
}

function hydrateRenewOrder(order: Order) {
  hydratingOrder = true
  try {
    form.customerId = order.customerId
    form.storeType = order.storeType
    form.orderType = order.orderType
    form.customerAppId = order.customerAppId
    form.regionCode = order.regionCode === 'MULTI' ? '' : order.regionCode || ''
    form.executionHours = order.executionHours || 1
    form.specialAmount = null
    if (order.orderType === 'KEYWORD_INSTALL') {
      keywordInstallDate.value = order.orderStartDate
    } else {
      dateRange.value = [order.orderStartDate, order.orderEndDate]
    }
    const groups = groupsFromOrder(order)
    regionGroupDrafts.KEYWORD_INSTALL = order.orderType === 'KEYWORD_INSTALL' ? cloneRegionGroups(groups) : [createRegionGroup()]
    regionGroupDrafts.DOWNLOAD = order.orderType === 'DOWNLOAD' ? cloneRegionGroups(groups) : [createRegionGroup()]
    regionGroupDrafts.RATING = order.orderType === 'RATING' ? cloneRegionGroups(groups) : [createRegionGroup()]
    regionGroupDrafts.REVIEW = order.orderType === 'REVIEW' ? cloneRegionGroups(groups) : [createRegionGroup()]
    form.regionGroups = cloneRegionGroups(groups)
  } finally {
    void nextTick(() => {
      hydratingOrder = false
    })
  }
}

function hydrateSpecialRenewAudit(order: Order, audit: SpecialOrderAudit) {
  hydratingOrder = true
  try {
    form.customerId = audit.customerId
    form.storeType = audit.storeType
    form.orderType = audit.orderType
    form.customerAppId = audit.customerAppId
    form.regionCode = audit.regionCode || ''
    form.specialGroups = specialGroupsFromAudit(audit)
    const negotiatedPrice = Number(audit.negotiatedPrice || 0)
    form.specialAmount = negotiatedPrice > 0 ? negotiatedPrice : Number(order.totalAmount || 0) || null
  } finally {
    void nextTick(() => {
      hydratingOrder = false
    })
  }
}

function specialGroupsFromAudit(audit: SpecialOrderAudit): SpecialRegionGroup[] {
  const groups = new Map<string, SpecialRegionGroup>()
  audit.items.forEach((item) => {
    const regionCode = item.regionCode || audit.regionCode || ''
    if (!regionCode) return
    const group = groups.get(regionCode) || { regionCode, items: [] }
    group.items.push({
      regionCode,
      keyword: item.keyword || '',
      targetRank: item.targetRank || 1,
      coverageNote: item.coverageNote || ''
    })
    groups.set(regionCode, group)
  })
  const result = Array.from(groups.values())
  result.forEach((group) => {
    group.items = group.items.filter((item) => item.keyword.trim())
    if (group.items.length === 0) group.items = [createSpecialItem()]
  })
  return result.length ? result : [createSpecialGroup(audit.regionCode || '')]
}

function groupsFromOrder(order: Order): RegionKeywordGroup[] {
  if (order.orderType === 'KEYWORD_INSTALL') {
    const groups = new Map<string, RegionKeywordGroup>()
    order.items.forEach((item) => {
      const regionCode = item.regionCode || form.regionCode
      if (!regionCode || !item.itemName) return
      const group = groups.get(regionCode) || createRegionGroup(regionCode)
      group.keywordItems.push({
        keyword: item.itemName,
        quantity: item.quantity || null
      })
      groups.set(regionCode, group)
    })
    return normalizeHydratedGroups(Array.from(groups.values()), 'keywordItems')
  }
  if (order.orderType === 'REVIEW' && order.commentDetails.length > 0) {
    const groups = new Map<string, RegionKeywordGroup>()
    order.commentDetails.forEach((detail) => {
      const group = groups.get(detail.regionCode) || createRegionGroup(detail.regionCode)
      group.reviewItems.push({
        starLevel: detail.starLevel === 4 ? 4 : 5,
        commentTitle: detail.commentTitle,
        commentContent: detail.commentContent
      })
      groups.set(detail.regionCode, group)
    })
    return normalizeHydratedGroups(Array.from(groups.values()), 'reviewItems')
  }

  const days = Math.max(1, order.totalDays || totalDays.value || 1)
  const groups = new Map<string, RegionKeywordGroup>()
  order.items.forEach((item) => {
    const regionCode = item.regionCode || form.regionCode
    if (!regionCode) return
    const group = groups.get(regionCode) || createRegionGroup(regionCode)
    const quantity = Number(item.quantity || 0)
    if (order.orderType === 'DOWNLOAD') {
      group.dailyDownloadCount = Math.max(0, Math.round(quantity / days))
    } else if (order.orderType === 'RATING') {
      if (item.itemType === 'RATING_5') group.rating5Count = Math.max(0, Math.round(quantity / days))
      if (item.itemType === 'RATING_4') group.rating4Count = Math.max(0, Math.round(quantity / days))
    } else if (order.orderType === 'REVIEW') {
      if (item.itemType === 'REVIEW_5') group.review5Count = quantity
      if (item.itemType === 'REVIEW_4') group.review4Count = quantity
    }
    groups.set(regionCode, group)
  })
  return Array.from(groups.values()).length ? Array.from(groups.values()) : [createRegionGroup(form.regionCode)]
}

function normalizeHydratedGroups(groups: RegionKeywordGroup[], listKey: 'keywordItems' | 'reviewItems') {
  const normalized = groups.length ? groups : [createRegionGroup(form.regionCode)]
  normalized.forEach((group) => {
    if (listKey === 'keywordItems') {
      group.keywordItems = group.keywordItems.filter((item) => item.keyword.trim())
      if (group.keywordItems.length === 0) group.keywordItems = [{ keyword: '', quantity: null }]
    }
    if (listKey === 'reviewItems') {
      group.reviewItems = group.reviewItems.filter((item) => item.commentTitle.trim() || item.commentContent.trim())
      if (group.reviewItems.length === 0) group.reviewItems = [createReviewItem()]
    }
  })
  return normalized
}

function isRenewableOrderStatus(status: OrderStatus) {
  return status === 'PENDING_CONFIRM'
    || status === 'PENDING_EXECUTION'
    || status === 'EXECUTING'
    || status === 'PAUSED'
    || status === 'COMPLETED'
}

function applyRouteQuery() {
  const queryStore = route.query.storeType
  if (queryStore === 'APP_STORE' || queryStore === 'GOOGLE_PLAY' || queryStore === 'IPAD_STORE') {
    form.storeType = queryStore
  }

  const queryOrderType = route.query.orderType
  if (isOrderType(queryOrderType)) {
    form.orderType = queryOrderType
  }

  const queryCustomerId = Number(route.query.customerId)
  if (Number.isFinite(queryCustomerId) && customers.value.some((customer) => customer.id === queryCustomerId)) {
    form.customerId = queryCustomerId
  }

  const queryAppId = Number(route.query.appId)
  if (Number.isFinite(queryAppId)) {
    const app = apps.value.find((candidate) => candidate.id === queryAppId)
    if (app) {
      form.customerId = app.customerId
      form.storeType = app.storeType
      form.customerAppId = app.id
      setDefaultRegions()
    }
  }
}

function orderListRoute(storeType: StoreType) {
  if (storeType === 'GOOGLE_PLAY') return { name: 'admin-orders-google' }
  if (storeType === 'IPAD_STORE') return { name: 'admin-orders-ipad' }
  return { name: 'admin-orders-apple' }
}

function isOrderType(value: unknown): value is OrderType {
  return value === 'KEYWORD_INSTALL'
    || value === 'DOWNLOAD'
    || value === 'RATING'
    || value === 'REVIEW'
    || value === 'RANK_GUARANTEE'
    || value === 'KEYWORD_COVERAGE'
}

function supportsStore(region: MarketRegion, storeType: StoreType) {
  if (storeType === 'APP_STORE') return region.supportsAppStore
  if (storeType === 'GOOGLE_PLAY') return region.supportsGooglePlay
  return region.supportsIpadStore
}

function regionLabel(code: string) {
  const region = regions.value.find((item) => item.code === code)
  if (!region) return code
  return locale.value.startsWith('zh') ? region.nameZh : region.nameEn
}

function flagUrl(code: string) {
  return `https://flagcdn.com/24x18/${code.toLowerCase()}.png`
}

function typeLabel(type?: OrderType | null) {
  return type ? t(`ordersPage.types.${type}`) : '-'
}

function customerLabel(customer: CustomerAccount) {
  return `${customer.username} / ${customer.email}`
}

function storeLabel(storeType: StoreType) {
  if (storeType === 'GOOGLE_PLAY') return 'Google Play'
  if (storeType === 'IPAD_STORE') return 'iPad Store'
  return 'App Store'
}

function money(value: number) {
  return `$${Number(value).toFixed(2)}`
}

function today() {
  const date = new Date()
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function formatSystemTime(date: Date) {
  return new Intl.DateTimeFormat('en-CA', {
    timeZone: 'Asia/Shanghai',
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: 'numeric',
    minute: '2-digit',
    second: '2-digit',
    hour12: true
  }).format(date)
}

function errorMessage(error: unknown, fallback: string) {
  if (typeof error === 'object' && error !== null && 'response' in error) {
    const response = (error as { response?: { data?: { code?: string; message?: string } } }).response
    if (response?.data?.code === 'BALANCE_NOT_ENOUGH') return t('orderCreate.balanceNotEnough')
    if (response?.data?.code === 'PRICE_NOT_CONFIGURED') return t('orderCreate.priceNotConfigured')
    if (response?.data?.code === 'PRICE_DISABLED') return t('orderCreate.priceDisabled')
    return response?.data?.message || fallback
  }
  return fallback
}
</script>

<style scoped>
.order-create-page {
  color: #182230;
  min-height: calc(100vh - 112px);
  padding-bottom: 72px;
  background:
    radial-gradient(circle at 18% 0%, rgb(47 125 244 / 7%), transparent 26%),
    linear-gradient(180deg, #f7faff 0%, #f4f7fb 100%);
}

.order-shell {
  width: 100%;
}

.order-form {
  width: 100%;
}

.order-workspace {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 300px;
  gap: 18px;
  align-items: start;
}

.order-main {
  min-width: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border: 1px solid #e6eaf0;
  border-radius: 6px;
  background: #ffffff;
  box-shadow: 0 16px 36px rgb(24 34 48 / 7%);
}

.summary-panel {
  border: 1px solid #e6eaf0;
  border-radius: 6px;
  background: #ffffff;
  box-shadow: 0 16px 36px rgb(24 34 48 / 7%);
  align-self: start;
  margin-top: 0;
}

.flow-section {
  padding: 16px 20px;
  border-bottom: 0;
}

.flow-section:last-child {
  border-bottom: 0;
}

.section-heading {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.section-heading h2 {
  margin: 0;
  color: #182230;
  font-size: 13px;
  font-weight: 800;
}

.section-heading h2 span {
  color: #ff4d4f;
}

.section-heading-actions {
  justify-content: flex-start;
}

.region-tool-buttons {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.region-tool-buttons :deep(.el-button) {
  min-height: 28px;
  padding: 6px 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 800;
}

.visually-hidden-file {
  display: none;
}

.type-tabs {
  margin-bottom: 0;
}

.type-tabs :deep(.el-tabs__header) {
  margin-bottom: 0;
}

.type-tabs :deep(.el-tabs__nav-scroll) {
  overflow-x: auto;
}

.type-tabs :deep(.el-tabs__nav-wrap::after) {
  display: none;
}

.type-tabs :deep(.el-tabs__nav) {
  display: inline-grid;
  min-width: 100%;
  grid-template-columns: repeat(6, minmax(136px, 1fr));
  border: 1px solid #d7e3f3;
  border-radius: 5px;
  overflow: hidden;
}

.type-tabs :deep(.el-tabs__item) {
  height: auto;
  min-height: 40px;
  min-width: 0;
  padding: 7px 10px;
  justify-content: center;
  border-right: 1px solid #d7e3f3;
  color: #344054;
  font-size: 13px;
  font-weight: 700;
  line-height: 1.25;
  white-space: normal;
}

.type-tabs :deep(.el-tabs__item:last-child) {
  border-right: 0;
}

.type-tabs :deep(.el-tabs__item.is-active) {
  background: #edf6ff;
  color: #1677ff;
}

.type-tabs :deep(.el-tabs__active-bar) {
  display: none;
}

.tab-label {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  max-width: 100%;
  text-align: center;
  overflow-wrap: anywhere;
}

.tab-icon {
  width: 16px;
  height: 16px;
}

.notice {
  margin-top: 12px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(260px, 1fr));
  gap: 18px 28px;
}

.form-grid :deep(.el-select),
.form-grid :deep(.el-date-editor),
.single-control :deep(.el-select),
.step-field :deep(.el-select),
.step-field :deep(.el-date-editor),
.task-panel :deep(.el-input-number),
.count-grid :deep(.el-input-number) {
  width: 100%;
}

.flow-compact {
  padding-top: 12px;
  padding-bottom: 12px;
}

.step-field-grid {
  display: grid;
  grid-template-columns: minmax(260px, 310px) minmax(330px, 1fr);
  gap: 30px;
  align-items: end;
}

.step-field .section-heading {
  margin-bottom: 10px;
}

.single-control {
  max-width: 310px;
}

.option-row {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-weight: 700;
}

.flag-icon {
  display: inline-flex;
  width: 24px;
  height: 18px;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  border-radius: 3px;
  box-shadow: 0 0 0 1px rgba(15, 23, 42, 0.08);
}

.flag-icon img {
  width: 24px;
  height: 18px;
  display: block;
  object-fit: cover;
}

.single-control :deep(.el-input__prefix),
.region-select-line :deep(.el-input__prefix),
.region-metric-cell :deep(.el-input__prefix) {
  display: inline-flex;
  align-items: center;
}

.app-picker-line {
  width: min(100%, 370px);
  display: grid;
  grid-template-columns: minmax(0, 1fr) 78px;
  gap: 10px;
  align-items: center;
}

.add-app-button {
  min-height: 32px;
  padding: 8px 12px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 800;
}

.system-time {
  min-height: 32px;
  display: flex;
  align-items: center;
  gap: 10px;
  color: #001dff;
  font-size: 12px;
  font-weight: 700;
}

.system-time strong {
  font-weight: 700;
}

.store-switch {
  width: 100%;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  border: 1px solid #d7e3f3;
  border-radius: 5px;
  overflow: hidden;
}

.store-switch :deep(.el-radio-button__inner) {
  width: 100%;
  border: 0;
  border-radius: 0;
  padding: 10px 8px;
  font-weight: 800;
}

.store-switch :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: #edf6ff;
  border-color: transparent;
  color: #1677ff;
  box-shadow: none;
}

.store-option {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 13px;
}

.app-option {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.app-option small {
  color: #667085;
}

.task-panel {
  width: 100%;
}

.keyword-editor {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.keyword-editor-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  color: #344054;
  font-size: 14px;
  font-weight: 700;
}

.region-groups {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.region-group {
  padding: 0;
  border: 0;
  border-radius: 0;
  background: transparent;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.region-group-head {
  display: grid;
  grid-template-columns: 300px auto;
  column-gap: 44px;
  align-items: end;
}

.region-select-line,
.region-actions {
  display: grid;
  grid-template-columns: 80px 220px;
  gap: 12px;
  align-items: center;
}

.region-actions {
  grid-template-columns: 42px 78px;
}

.region-metric-groups {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.region-metric-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.region-metric-toolbar {
  display: grid;
  grid-template-columns: 300px auto;
  column-gap: 44px;
  align-items: end;
}

.region-action-line {
  display: grid;
  grid-template-columns: 42px 96px;
  gap: 12px;
  align-items: center;
}

.region-select-line span,
.region-actions span,
.region-action-line span {
  color: #344054;
  font-size: 14px;
  font-weight: 700;
  white-space: nowrap;
}

.remove-region-button {
  width: 78px;
  min-height: 28px;
  padding: 6px 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 800;
}

.keyword-table {
  width: min(100%, 665px);
  overflow: hidden;
  border: 1px solid #d7e3f3;
}

.keyword-table-head,
.keyword-row {
  display: grid;
  grid-template-columns: 180px 180px 64px minmax(140px, 1fr);
}

.keyword-table-head {
  background: #fbfcff;
}

.keyword-table-head span {
  min-height: 40px;
  padding: 11px 14px;
  border-right: 1px solid #d7e3f3;
  color: #344054;
  font-size: 13px;
  font-weight: 800;
}

.keyword-table-head span:last-child {
  border-right: 0;
}

.keyword-row {
  min-height: 48px;
  align-items: center;
  border-top: 1px solid #d7e3f3;
}

.keyword-cell {
  min-height: 48px;
  padding: 7px 9px;
  border-right: 1px solid #d7e3f3;
  display: flex;
  align-items: center;
}

.keyword-cell:last-child {
  border-right: 0;
}

.keyword-action-cell {
  justify-content: center;
}

.keyword-empty-cell {
  justify-content: flex-start;
}

.keyword-cell :deep(.el-input) {
  width: 100%;
}

.keyword-cell :deep(.el-input__wrapper) {
  min-height: 32px;
  box-shadow: 0 0 0 1px #d7e3f3 inset;
}

.keyword-delete-button {
  width: 28px;
  height: 28px;
  min-height: 28px;
  padding: 0;
  justify-self: center;
  color: #ff4d6a;
}

.region-metric-table {
  width: min(100%, 665px);
  overflow: hidden;
  border: 1px solid #d7e3f3;
}

.region-metric-head,
.region-metric-row {
  display: grid;
}

.region-metric-table-download .region-metric-head,
.region-metric-table-download .region-metric-row {
  grid-template-columns: 220px minmax(180px, 1fr);
}

.region-metric-table-score .region-metric-head,
.region-metric-table-score .region-metric-row {
  grid-template-columns: 220px 180px minmax(160px, 1fr);
}

.region-metric-head {
  background: #fbfcff;
}

.region-metric-head span {
  min-height: 40px;
  padding: 11px 14px;
  border-right: 1px solid #d7e3f3;
  color: #344054;
  font-size: 13px;
  font-weight: 800;
}

.region-metric-head span:last-child {
  border-right: 0;
}

.region-metric-row {
  min-height: 48px;
  align-items: center;
  border-top: 1px solid #d7e3f3;
}

.region-metric-cell {
  min-height: 48px;
  padding: 7px 9px;
  border-right: 1px solid #d7e3f3;
  display: flex;
  align-items: center;
}

.region-metric-cell:last-child {
  border-right: 0;
}

.region-metric-cell :deep(.el-select),
.region-metric-cell :deep(.el-input-number) {
  width: 100%;
}

.region-metric-cell :deep(.el-input__wrapper) {
  min-height: 32px;
  box-shadow: 0 0 0 1px #d7e3f3 inset;
}

.review-detail-table,
.special-item-table {
  width: min(100%, 860px);
  overflow: hidden;
  border: 1px solid #d7e3f3;
}

.special-item-table {
  width: 100%;
  border-color: #d8e2f0;
  border-radius: 6px;
  background: #ffffff;
}

.review-detail-head,
.review-detail-row,
.special-item-head,
.special-item-row {
  display: grid;
  align-items: center;
}

.review-detail-head,
.review-detail-row {
  grid-template-columns: 120px 180px minmax(220px, 1fr) 64px minmax(140px, 1fr);
}

.special-item-head,
.special-item-row {
  grid-template-columns: minmax(220px, 1fr) 180px 64px minmax(140px, 1fr);
}

.review-detail-head,
.special-item-head {
  background: #f7faff;
}

.review-detail-head span,
.special-item-head span {
  min-height: 40px;
  padding: 11px 14px;
  border-right: 1px solid #d8e2f0;
  color: #344054;
  font-size: 13px;
  font-weight: 800;
}

.review-detail-head span:last-child,
.special-item-head span:last-child {
  border-right: 0;
}

.review-detail-row,
.special-item-row {
  min-height: 48px;
  border-top: 1px solid #d8e2f0;
}

.review-detail-row > *,
.special-item-row > * {
  min-height: 48px;
  padding: 7px 9px;
  border-right: 1px solid #d8e2f0;
}

.review-detail-row > *:last-child,
.special-item-row > *:last-child {
  border-right: 0;
}

.review-detail-row :deep(.el-select),
.review-detail-row :deep(.el-input),
.special-item-row :deep(.el-select),
.special-item-row :deep(.el-input),
.special-item-row :deep(.el-input-number) {
  width: 100%;
}

.review-action-cell {
  display: flex;
  align-items: center;
  justify-content: center;
}

.review-empty-cell {
  display: flex;
  align-items: center;
  justify-content: flex-start;
}

.review-add-button {
  flex: 0 0 auto;
}

.special-detail-panel {
  display: flex;
  flex-direction: column;
  width: min(100%, 1120px);
  gap: 16px;
  padding: 16px;
  border: 1px solid #d8e2f0;
  border-radius: 8px;
  background: #fbfdff;
}

.special-item-footer {
  min-height: 42px;
  padding: 7px 10px;
  border-top: 1px solid #d8e2f0;
  display: flex;
  align-items: center;
  background: #ffffff;
}

.add-keyword-button {
  align-self: center;
  margin-left: 0;
  min-height: 28px;
  padding: 4px 8px;
  color: #667085;
  font-size: 12px;
}

.add-region-button {
  align-self: flex-start;
  display: inline-flex;
  flex: 0 0 auto;
  min-height: 32px;
  padding: 7px 12px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 800;
}

.keyword-add-region-button {
  margin-top: 18px;
}

.metric-add-region-button {
  margin-top: 0;
}

.count-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(180px, 1fr));
  gap: 16px;
}

.special-placeholder {
  min-height: 120px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 8px;
  padding: 22px;
  border: 1px dashed #f5b86f;
  border-radius: 8px;
  background: #fff8ef;
  color: #7a4b12;
}

.special-placeholder strong {
  color: #7a3d00;
}

.special-placeholder span {
  line-height: 1.7;
}

.special-amount-panel {
  margin-top: 14px;
  padding: 12px;
  border: 1px solid #e1e8f2;
  border-radius: 6px;
  background: #ffffff;
}

.special-amount-panel :deep(.el-input-number),
.special-detail-panel > .el-form-item :deep(.el-input-number) {
  width: 100%;
}

.special-amount-panel :deep(.el-form-item) {
  margin-bottom: 0;
}

.special-amount-panel p,
.special-detail-panel p {
  margin: 8px 0 0;
  color: #667085;
  font-size: 12px;
  line-height: 1.6;
}

.summary-panel {
  padding: 16px 20px 20px;
}

.summary-heading {
  padding-bottom: 14px;
  border-bottom: 1px solid #eef2f6;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.summary-heading span {
  color: #667085;
  font-size: 13px;
  font-weight: 700;
}

.summary-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 18px 0;
}

.summary-item {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.summary-item span {
  color: #667085;
  font-size: 13px;
  white-space: nowrap;
}

.summary-item strong {
  min-width: 0;
  color: #182230;
  font-size: 14px;
  font-weight: 800;
  text-align: right;
  word-break: break-word;
}

.submit-button {
  width: 100%;
  min-height: 42px;
  font-weight: 800;
  background: #1677ff;
  border-color: #1677ff;
}

.billing-note {
  margin-top: 10px;
  padding: 12px;
  border: 1px solid #9ec5fe;
  border-radius: 6px;
  background: #f0f7ff;
  display: flex;
  flex-direction: column;
  gap: 6px;
  color: #344054;
  font-size: 13px;
  line-height: 1.6;
}

.billing-note strong {
  color: #1677ff;
}

.billing-breakdown {
  margin-top: 14px;
  padding-top: 14px;
  border-top: 1px solid #eef2f6;
}

.breakdown-title {
  margin-bottom: 10px;
  color: #182230;
  font-size: 14px;
  font-weight: 800;
}

.breakdown-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.breakdown-line {
  padding: 10px;
  border: 1px solid #edf2f7;
  border-radius: 6px;
  background: #fbfdff;
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 10px;
  align-items: start;
}

.breakdown-line strong {
  display: block;
  color: #182230;
  font-size: 13px;
  font-weight: 800;
  line-height: 1.35;
}

.breakdown-line span {
  display: block;
  margin-top: 4px;
  color: #667085;
  font-size: 12px;
  line-height: 1.45;
}

.breakdown-line b {
  color: #182230;
  font-size: 13px;
  font-weight: 800;
  white-space: nowrap;
}

.summary-checkout {
  margin-top: 16px;
  padding-top: 14px;
  border-top: 1px solid #e6eaf0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.checkout-total {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 8px;
  font-weight: 800;
}

.checkout-total span {
  color: #182230;
}

.checkout-total strong {
  color: #ff3b30;
  font-size: 20px;
}

@media (max-width: 1100px) {
  .order-workspace {
    grid-template-columns: 1fr;
  }

  .summary-panel {
    position: static;
  }

  .type-tabs :deep(.el-tabs__nav) {
    grid-template-columns: repeat(6, minmax(136px, 1fr));
  }

  .special-detail-panel {
    width: 100%;
  }
}

@media (max-width: 700px) {
  .flow-section,
  .summary-panel {
    padding: 14px;
  }

  .form-grid,
  .count-grid,
  .region-group-head,
  .keyword-row,
  .store-switch {
    grid-template-columns: 1fr;
  }

  .special-detail-panel {
    padding: 12px;
  }

  .special-item-table {
    overflow-x: auto;
  }

  .special-item-head,
  .special-item-row {
    min-width: 720px;
  }

  .summary-item {
    align-items: flex-start;
    flex-direction: column;
    gap: 6px;
  }

  .summary-item strong {
    text-align: left;
  }

  .type-tabs :deep(.el-tabs__nav) {
    width: 100%;
    grid-template-columns: 1fr;
  }

  .submit-button {
    width: 100%;
  }
}
</style>








