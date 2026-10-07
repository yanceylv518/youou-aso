<template>
  <section class="order-create-page">
    <div class="order-shell">
      <el-form label-position="top" class="order-form">
        <div v-if="isEditingOrder" class="edit-toolbar">
          <el-button :icon="ArrowLeft" @click="backToOrderList">
            {{ t('orderCreate.backToList') }}
          </el-button>
        </div>
        <el-alert v-if="originalEditingStatus === 'CANCELLED'" :title="t('orderCreate.cancelledOrderEditHint')" type="info" :closable="false" show-icon />
        <div class="order-workspace">
          <main class="order-main">
            <section class="form-section">
              <div class="section-heading">
                <h2><span aria-hidden="true">*</span> {{ t('orderCreate.serviceSection') }}</h2>
              </div>
              <el-select class="mobile-service-select" v-model="selectedOrderModuleTab" :aria-label="t('visual.switchService')">
                  <el-option v-for="module in availableOrderModules" :key="module.id" :value="String(module.id)" :label="moduleLocalizedName(module)" />
                </el-select>
                <div class="type-tabs">
                <el-tabs v-model="selectedOrderModuleTab">
                  <el-tab-pane
                    v-for="module in availableOrderModules"
                    :key="module.id"
                    :name="String(module.id)"
                  >
                    <template #label>
                      <span class="tab-label">
                        <component :is="moduleIcon(module.orderType)" class="tab-icon" />
                        {{ moduleLocalizedName(module) }}
                      </span>
                    </template>
                  </el-tab-pane>
                </el-tabs>
              </div>

              <div v-if="selectedOrderModule" class="selected-module-card">
                <span class="selected-module-marker" aria-hidden="true"></span>
                <div class="selected-module-content">
                  <span class="selected-module-label">{{ t('orderCreate.selectedModule') }}</span>
                  <strong>{{ moduleLocalizedName(selectedOrderModule) }}</strong>
                  <p v-if="moduleLocalizedDescription(selectedOrderModule)">
                    {{ moduleLocalizedDescription(selectedOrderModule) }}
                  </p>
                </div>
              </div>
            </section>

            <section class="form-section">
              <div class="section-heading">
                <h2><span aria-hidden="true">*</span> {{ t('orderCreate.store') }}</h2>
              </div>
              <el-radio-group v-model="form.storeType" class="store-switch">
                <el-radio-button v-if="availableStores.includes('APP_STORE')" value="APP_STORE">
                  <span class="store-option"><StoreIcon store-type="APP_STORE" size="sm" /> App Store</span>
                </el-radio-button>
                <el-radio-button v-if="availableStores.includes('GOOGLE_PLAY')" value="GOOGLE_PLAY">
                  <span class="store-option"><StoreIcon store-type="GOOGLE_PLAY" size="sm" /> Google Play</span>
                </el-radio-button>
                <el-radio-button v-if="availableStores.includes('IPAD_STORE')" value="IPAD_STORE">
                  <span class="store-option"><StoreIcon store-type="IPAD_STORE" size="sm" /> iPad Store</span>
                </el-radio-button>
              </el-radio-group>
            </section>

            <section class="form-section">
              <div class="section-heading">
                <h2><span aria-hidden="true">*</span> {{ t('orderCreate.app') }}</h2>
              </div>
              <div class="app-picker-line">
                <el-select
                  v-model="form.customerAppId"
                  filterable
                  :placeholder="t('orderCreate.selectApp')"
                  :loading="loading"
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
                <el-button type="primary" class="add-app-button" @click="router.push({ name: 'user-applications' })">
                  {{ t('applications.addApp') }}
                </el-button>
              </div>
            </section>

            <section
              v-if="!['KEYWORD_INSTALL', 'DOWNLOAD', 'RATING', 'REVIEW'].includes(form.orderType) && !isSpecialOrder"
              class="form-section"
            >
              <div class="section-heading">
                <h2><span aria-hidden="true">*</span> {{ t('applications.region') }}</h2>
              </div>
              <div class="single-control">
                <el-select
                  v-model="form.regionCode"
                  filterable
                  :filter-method="filterRegions"
                  @visible-change="handleRegionDropdownVisibility"
                  :placeholder="t('applications.selectRegion')"
                  :disabled="selectedAppRegionCodes.length === 0"
                >
                  <template #prefix>
                    <span v-if="form.regionCode" class="flag-icon">
                      <img :src="flagUrl(form.regionCode)" :alt="form.regionCode" />
                    </span>
                  </template>
                  <el-option
                    v-for="region in prioritizedSelectedAppRegions"
                    :key="region.code"
                    :label="regionSearchLabel(region.code)"
                    :value="region.code"
                  >
                    <span class="option-row">
                      <span class="flag-icon"><img :src="flagUrl(region.code)" :alt="region.code" /></span>
                      <strong>{{ region.code }}</strong>
                      <span>{{ regionLabel(region.code) }}</span>
                    </span>
                  </el-option>
                </el-select>
              </div>
            </section>

            <section v-if="!isSpecialOrder" class="form-section form-compact">
              <div class="step-field-grid">
                <div class="step-field">
                  <div class="section-heading">
                    <h2><span aria-hidden="true">*</span> {{ t(form.orderType === 'KEYWORD_INSTALL' ? 'ordersPage.orderTime' : 'orderCreate.orderDate') }}</h2>
                  </div>
                  <el-radio-group v-if="form.orderType === 'KEYWORD_INSTALL'" v-model="keywordStartMode" class="keyword-start-mode" :aria-label="t('visual.startMode')">
                    <el-radio-button value="immediate">{{ t('visual.startImmediately') }}</el-radio-button>
                    <el-radio-button value="scheduled">{{ t('visual.startScheduled') }}</el-radio-button>
                  </el-radio-group>
                  <p v-if="form.orderType === 'KEYWORD_INSTALL' && keywordStartMode === 'immediate'" class="keyword-start-hint">{{ t('visual.immediateStartHint') }}</p>
                  <el-date-picker
                    v-if="form.orderType === 'KEYWORD_INSTALL' && keywordStartMode === 'scheduled'"
                    v-model="keywordInstallDateTime"
                    type="datetime"
                    format="YYYY-MM-DD HH:mm"
                    time-format="HH:mm"
                    value-format="YYYY-MM-DDTHH:mm"
                    :placeholder="t('orderCreate.selectOrderDateTime')"
                    :disabled-date="disabledKeywordOrderDate"
                  />
                  <el-date-picker
                    v-if="form.orderType !== 'KEYWORD_INSTALL'"
                    v-model="dateRange"
                    type="daterange"
                    value-format="YYYY-MM-DD"
                    range-separator="-"
                    :start-placeholder="t('ordersPage.startDate')"
                    :end-placeholder="t('ordersPage.endDate')"
                  />
                  <p v-if="form.orderType === 'KEYWORD_INSTALL' && keywordTimeInPast" class="order-time-error">
                    {{ t('orderCreate.keywordOrderTimePast') }}
                  </p>
                </div>
                <div class="system-time">
                  <span>{{ t('visual.systemTime') }}</span>
                  <strong>{{ systemTimeText }}</strong>
                </div>
              </div>
            </section>

            <section v-if="form.orderType === 'KEYWORD_INSTALL'" class="form-section">
              <div class="section-heading">
                <h2><span aria-hidden="true">*</span> {{ t('orderCreate.executionHours') }}</h2>
              </div>
              <div class="single-control">
                <el-select v-model="form.executionHours" :placeholder="t('orderCreate.executionHours')">
                  <el-option v-for="hour in executionHourOptions" :key="hour" :label="`${hour}h`" :value="hour" />
                </el-select>
              </div>
            </section>

            <section v-if="form.orderType === 'KEYWORD_INSTALL'" class="form-section">
              <div class="section-heading section-heading-actions">
                <h2><span aria-hidden="true">*</span> {{ t('orderCreate.regionKeywords') }}</h2>
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
                          :filter-method="filterRegions"
                          @visible-change="handleRegionDropdownVisibility"
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
                            :label="regionSearchLabel(region.code)"
                            :value="region.code"
                          >
                            <span class="option-row">
                              <span class="flag-icon"><img :src="flagUrl(region.code)" :alt="region.code" /></span>
                              <strong>{{ region.code }}</strong>
                              <span>{{ regionLabel(region.code) }}</span>
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
              <el-button type="primary" :icon="Plus" class="add-region-button keyword-add-region-button" v-if="!hasChinaRegionSelection" @click="addRegionGroup">
                {{ t('orderCreate.addRegionConfig') }}
              </el-button>
            </section>

            <section v-else class="form-section">
              <div class="section-heading">
                <h2>
                  <span aria-hidden="true">*</span>
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
              <input
                v-if="form.orderType === 'REVIEW'"
                ref="reviewImportInput"
                class="visually-hidden-file"
                type="file"
                accept=".xlsx,.csv,text/csv,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                @change="handleReviewImport"
              />
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
                          :filter-method="filterRegions"
                          @visible-change="handleRegionDropdownVisibility"
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
                            :label="regionSearchLabel(region.code)"
                            :value="region.code"
                          >
                            <span class="option-row">
                              <span class="flag-icon"><img :src="flagUrl(region.code)" :alt="region.code" /></span>
                              <strong>{{ region.code }}</strong>
                              <span>{{ regionLabel(region.code) }}</span>
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
                        <template v-if="form.orderType === 'REVIEW'">
                          <el-button size="small" type="primary" :icon="Download" @click="downloadReviewTemplate">
                            {{ t('orderCreate.downloadReviewTemplate') }}
                          </el-button>
                          <el-button size="small" type="success" :icon="Plus" :loading="reviewUploading" @click="openReviewImport(groupIndex)">
                            {{ t('orderCreate.importReviewContent') }}
                          </el-button>
                        </template>
                      </div>
                    </div>

                    <div

                      class="region-metric-table"
                      :class="form.orderType === 'DOWNLOAD' ? 'region-metric-table-download' : 'region-metric-table-score'"
                    >
                      <div class="region-metric-head">
                        <span v-if="form.orderType === 'DOWNLOAD'">{{ t('orderCreate.dailyDownloadCount') }}</span>
                        <template v-else-if="['RATING', 'REVIEW'].includes(form.orderType)">
                          <span>{{ t('orderCreate.rating5Count') }}</span>
                          <span>{{ t('orderCreate.rating4Count') }}</span>
                        </template>
                        <span></span>
                      </div>
                      <div class="region-metric-row">
                        <div v-if="form.orderType === 'DOWNLOAD'" class="region-metric-cell">
                          <el-input-number v-model="group.dailyDownloadCount" :min="1" :step="1" />
                        </div>
                        <template v-else-if="['RATING', 'REVIEW'].includes(form.orderType)">
                          <div class="region-metric-cell">
                            <el-input-number v-model="group[form.orderType === 'REVIEW' ? 'review5Count' : 'rating5Count']" :min="0" :step="1" :precision="0" />
                          </div>
                          <div class="region-metric-cell">
                            <el-input-number v-model="group[form.orderType === 'REVIEW' ? 'review4Count' : 'rating4Count']" :min="0" :step="1" :precision="0" />
                          </div>
                        </template>
                        <div class="region-metric-cell region-metric-empty-cell"></div>
                      </div>
                    </div>

                    <div v-if="form.orderType === 'REVIEW'" class="review-attachments">
                      <p>{{ t('orderCreate.reviewAttachmentHint') }}</p>
                      <div v-for="(file, index) in group.reviewAttachments" :key="file.id" class="review-attachment-row">
                        <el-button link type="primary" @click="downloadReviewAttachment(file, false, undefined)">{{ file.fileName }}</el-button>
                        <span v-if="file.fileSize">{{ (file.fileSize / 1024).toFixed(1) }} KB</span>
                        <el-button link type="danger" @click="group.reviewAttachments.splice(index, 1)">{{ t('orderCreate.removeAttachment') }}</el-button>
                      </div>
                    </div>
                  </div>
                  <el-button type="primary" :icon="Plus" class="add-region-button metric-add-region-button" v-if="!hasChinaRegionSelection" @click="addRegionGroup">
                    {{ t('orderCreate.addRegionConfig') }}
                  </el-button>
                </div>

                <div v-else-if="isSpecialOrder" class="special-detail-panel">
                  <div class="region-groups">
                    <div v-for="(group, groupIndex) in form.specialGroups" :key="groupIndex" class="region-group special-region-group">
                      <div class="region-group-head">
                        <div class="region-select-line">
                          <span>{{ t('orderCreate.countryRegion') }}</span>
                          <el-select
                            v-model="group.regionCode"
                            filterable
                            :filter-method="filterRegions"
                            @visible-change="handleRegionDropdownVisibility"
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
                              :label="regionSearchLabel(region.code)"
                              :value="region.code"
                            >
                              <span class="option-row">
                                <span class="flag-icon"><img :src="flagUrl(region.code)" :alt="region.code" /></span>
                                <strong>{{ region.code }}</strong>
                                <span>{{ regionLabel(region.code) }}</span>
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
                      <div class="special-item-table" :class="{ 'coverage-keyword-table': form.orderType === 'KEYWORD_COVERAGE' }">
                        <div class="special-item-head">
                          <span>{{ form.orderType === 'CHART_RANK_GUARANTEE' ? t('orderCreate.chartType') : t('orderCreate.keywords') }}</span>
                          <span v-if="isRankGuaranteeType(form.orderType)">{{ t('orderCreate.targetRank') }}</span>
                          <span>{{ t('applications.actions') }}</span>
                          <span></span>
                        </div>
                        <div v-for="(item, index) in group.items" :key="index" class="special-item-row">
                          <el-input v-if="form.orderType === 'CHART_RANK_GUARANTEE'" v-model.trim="item.chartType" maxlength="255" :placeholder="t('orderCreate.chartTypePlaceholder')" />
                          <el-input v-else v-model.trim="item.keyword" maxlength="255" :placeholder="t('orderCreate.keywordPlaceholder')" />
                          <el-select
                            v-if="isRankGuaranteeType(form.orderType)"
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
                              {{ form.orderType === 'CHART_RANK_GUARANTEE' ? t('orderCreate.addChartType') : t('orderCreate.addKeyword') }}
                            </el-button>
                            <el-button
                              v-if="index === 0 && form.orderType === 'KEYWORD_COVERAGE'"
                              size="small"
                              text
                              :icon="Plus"
                              class="add-keyword-button"
                              @click="openBatchKeywordDialog(groupIndex, 'coverage')"
                            >
                              {{ t('orderCreate.batchKeywordImport') }}
                            </el-button>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                  <el-button type="primary" :icon="Plus" class="add-region-button special-add-region-button"  v-if="!hasChinaRegionSelection" @click="addSpecialGroup">
                    {{ t('orderCreate.addRegionConfig') }}
                  </el-button>
                  <div class="contact-grid">
                    <el-form-item :label="t('orderCreate.contactType')">
                      <el-select v-model="form.contactType" :placeholder="t('orderCreate.contactTypePlaceholder')">
                        <el-option
                          v-for="option in contactTypeOptions"
                          :key="option.value"
                          :label="option.label"
                          :value="option.value"
                        />
                      </el-select>
                    </el-form-item>
                    <el-form-item :label="t('orderCreate.contactValue')">
                      <el-input v-model.trim="form.contactValue" maxlength="128" :placeholder="t('orderCreate.contactValuePlaceholder')" />
                    </el-form-item>
                  </div>
                  <el-form-item :label="t('orderCreate.requestedContent')" class="special-request-field">
                    <el-input
                      v-model="form.requestedContent"
                      type="textarea"
                      :rows="5"
                      :placeholder="t('orderCreate.requestedContentPlaceholder')"
                    />
                  </el-form-item>
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
                <strong>{{ selectedOrderModule ? moduleLocalizedName(selectedOrderModule) : typeLabel(form.orderType) }}</strong>
              </div>
              <div class="summary-item">
                <span>{{ t('orderCreate.store') }}</span>
                <strong>{{ storeLabel(form.storeType) }}</strong>
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
              <span>{{ t(isSpecialOrder ? 'visual.billingQuote' : form.orderType === 'REVIEW' ? 'visual.billingReview' : 'visual.billingQuantity') }}</span>
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
                <strong>{{ isSpecialOrder ? t('promotionPage.auditBadge') : money(estimatedAmount) }}</strong>
              </div>
              <el-button
                type="primary"
                class="submit-button"
                :loading="submitting"
                :disabled="submitDisabled || reviewUploading"
                @click="submitOrder"
              >
                {{ submitButtonText }}
              </el-button>
            </div>
          </aside>
        </div>
      </el-form>
    </div>
    <el-dialog append-to-body
      v-model="batchKeywordDialogVisible"
      :title="t('orderCreate.batchKeywordDialogTitle')"
      width="420px"
      class="batch-keyword-dialog"
      @closed="batchKeywordText = ''"
    >
      <div class="batch-keyword-body">
        <p class="batch-keyword-tip">{{ t(batchKeywordMode === 'coverage' ? 'orderCreate.batchCoverageKeywordDialogTip' : 'orderCreate.batchKeywordDialogTip') }}</p>
        <el-input
          v-model="batchKeywordText"
          class="batch-keyword-input"
          type="textarea"
          :autosize="{ minRows: 7, maxRows: 10 }"
          resize="vertical"
          :placeholder="t(batchKeywordMode === 'coverage' ? 'orderCreate.batchCoverageKeywordPlaceholder' : 'orderCreate.batchKeywordPlaceholder')"
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
import { formatCurrency } from '@/utils/presentation'
import { defaultKeywordOrderTime, businessMinute, keywordOrderTimeIsPast } from '@/utils/orderTime'

import { uploadReviewAttachment, downloadReviewAttachment, type ReviewAttachment } from '@/api/reviewAttachments'
import { computed, nextTick, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { ArrowLeft, ChatDotRound, Delete, Download, Grid, Lock, Plus, Search, Star, Trophy } from '@element-plus/icons-vue'
import { getCustomerApps, getEnabledRegions, type CustomerApp, type MarketRegion, type StoreType } from '@/api/applications'
import { createCustomerOrder, getCustomerOrder, payCustomerOrder, resubmitCustomerOrder, type CreateOrderPayload, type Order, type OrderType } from '@/api/orders'
import { getCustomerPricingConfig, getCustomerOrderTypeRegionPricing, type OrderTypeRegionPricing, type PriceCode } from '@/api/pricing'
import { getCustomerOrderModules, type OrderModuleConfig } from '@/api/orderModules'
import { getCustomerSpecialAudits, submitSpecialAudit, type SpecialOrderAudit } from '@/api/specialOrderAudits'
import StoreIcon from '@/components/StoreIcon.vue'
import { useWalletStore } from '@/stores/wallet'
import { CoverageKeywordImportError, parseCoverageKeywordText } from '@/utils/coverageKeywords'

interface OrderForm {
  orderType: OrderType
  storeType: StoreType
  customerAppId: number | ''
  regionCode: string
  executionHours: number
  regionGroups: RegionKeywordGroup[]
  contactType: string
  contactValue: string
  requestedContent: string
  specialGroups: SpecialRegionGroup[]
}

interface KeywordInput {
  keyword: string
  quantity: number | null
}

interface RegionKeywordGroup {
  regionCode: string
  keywordItems: KeywordInput[]
  reviewAttachments: ReviewAttachment[]
  dailyDownloadCount: number | null
  rating5Count: number
  rating4Count: number
  review5Count: number
  review4Count: number
}

interface SpecialKeywordInput {
  regionCode: string
  keyword: string
  chartType: string
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
  special?: boolean
}

interface BillingLine {
  label: string
  formula: string
  amount: string
}

const route = useRoute()
const router = useRouter()
const { t, locale } = useI18n()
const walletStore = useWalletStore()

const contactTypeOptions = [
  { label: 'WeChat', value: 'WeChat' },
  { label: 'Telegram', value: 'Telegram' },
  { label: 'WhatsApp', value: 'WhatsApp' }
]

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
const editingOrderId = ref<number | null>(null)
const hydratedOrderId = ref<number | null>(null)
const hydratedRenewOrderId = ref<number | null>(null)
const baseDataReady = ref(false)
const originalPayloadSnapshot = ref<string | null>(null)
const originalEditingStatus = ref<Order['status'] | null>(null)
const keywordImportInput = ref<HTMLInputElement | null>(null)
const reviewUploading = ref(false)
const reviewImportInput = ref<HTMLInputElement | null>(null)
const reviewImportGroupIndex = ref<number | null>(null)
const batchKeywordDialogVisible = ref(false)
const batchKeywordText = ref('')
const batchKeywordGroupIndex = ref<number | null>(null)
const batchKeywordMode = ref<'install' | 'coverage'>('install')
const apps = ref<CustomerApp[]>([])
const regions = ref<MarketRegion[]>([])
const orderModules = ref<OrderModuleConfig[]>([])
const regionPricing = ref<OrderTypeRegionPricing[]>([])
const selectedOrderModuleId = ref<number | null>(null)
const regionSearchQuery = ref('')
const regionCodeAliases: Record<string, string> = {
  UK: 'GB',
  RUSSIA: 'RU',
  'RUSSIAN FEDERATION': 'RU',
  '俄罗斯': 'RU'
}
const dateRange = ref<[string, string] | ''>([today(), today()])
const keywordStartMode = ref<'immediate' | 'scheduled'>('immediate')
const keywordInstallDateTime = ref(defaultKeywordOrderTime())
const systemNow = ref(new Date())
let systemTimer: number | undefined
const priceMap = reactive<Record<PriceCode, number>>({
  KEYWORD_INSTALL: 0,
  DOWNLOAD: 0,
  RATING_5: 0,
  RATING_4: 0,
  REVIEW_5: 0,
  REVIEW_4: 0
})
const chinaPriceMap = reactive<Record<PriceCode, number>>({
  KEYWORD_INSTALL: 0,
  DOWNLOAD: 0,
  RATING_5: 0,
  RATING_4: 0,
  REVIEW_5: 0,
  REVIEW_4: 0
})
let hydratingOrder = false

const form = reactive<OrderForm>({
  orderType: 'KEYWORD_INSTALL',
  storeType: 'APP_STORE',
  customerAppId: '',
  regionCode: '',
  executionHours: 1,
  regionGroups: [createRegionGroup()],
  contactType: '',
  contactValue: '',
  requestedContent: '',
  specialGroups: [createSpecialGroup()]
})

const regionGroupDrafts = reactive<Partial<Record<OrderType, RegionKeywordGroup[]>>>({
  KEYWORD_INSTALL: [createRegionGroup()]
})

const services: ServiceOption[] = [
  { code: 'KEYWORD_INSTALL', icon: Search },
  { code: 'DOWNLOAD', icon: Download },
  { code: 'RATING', icon: Star },
  { code: 'REVIEW', icon: ChatDotRound },
  { code: 'RANK_GUARANTEE', icon: Lock, special: true },
  { code: 'CHART_RANK_GUARANTEE', icon: Trophy, special: true },
  { code: 'KEYWORD_COVERAGE', icon: Grid, special: true }
]

const filteredApps = computed(() => apps.value.filter((app) => app.storeType === form.storeType))
const selectedApp = computed(() => filteredApps.value.find((app) => app.id === form.customerAppId) || null)
const selectedAppRegions = computed(() => {
  const allowed = regionPricing.value.find(item => item.orderModuleId === selectedOrderModuleId.value)?.allowedRegionCodes
  return regions.value
    .filter((region) => supportsStore(region, form.storeType))
    .filter((region) => Boolean(allowed?.includes(region.code)))
})
const selectedAppRegionCodes = computed(() => {
  return selectedAppRegions.value
    .map((region) => region.code)
})
const prioritizedSelectedAppRegions = computed(() => prioritizeRegions(selectedAppRegions.value))

function filterRegions(query: string) {
  regionSearchQuery.value = query.trim().toLocaleLowerCase()
}

function handleRegionDropdownVisibility(visible: boolean) {
  if (!visible) regionSearchQuery.value = ''
}

function prioritizeRegions(list: MarketRegion[]) {
  const query = regionSearchQuery.value
  if (!query) return list
  return list
    .map((region, index) => ({ region, index, score: regionSearchScore(region, query) }))
    .filter((item) => item.score < 99)
    .sort((left, right) => left.score - right.score || left.index - right.index)
    .map((item) => item.region)
}

function regionSearchScore(region: MarketRegion, query: string) {
  const code = region.code.toLocaleLowerCase()
  if (code === query) return 0
  if (code.startsWith(query)) return 1
  if (code.includes(query)) return 2
  const names = [region.nameZh, region.nameEn].map((name) => name.toLocaleLowerCase())
  if (names.some((name) => name.startsWith(query))) return 3
  if (names.some((name) => name.includes(query))) return 4
  return 99
}
function normalizeImportRegionCode(value: string) {
  const code = value.trim().toUpperCase()
  return regionCodeAliases[code] || code
}
function isRankGuaranteeType(value: OrderType | '') {
  return value === 'RANK_GUARANTEE' || value === 'CHART_RANK_GUARANTEE'
}
const isSpecialOrder = computed(() => isRankGuaranteeType(form.orderType) || form.orderType === 'KEYWORD_COVERAGE')
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
      review4Count: group.review4Count,
      attachmentIds: group.reviewAttachments.map(file => file.id)
    }))
    .filter((item) => {
      if (form.orderType === 'DOWNLOAD') return Number(item.dailyDownloadCount || 0) > 0
      if (form.orderType === 'RATING') return Number(item.rating5Count || 0) + Number(item.rating4Count || 0) > 0
      if (form.orderType === 'REVIEW') return Number(item.review5Count || 0) + Number(item.review4Count || 0) > 0
      return false
    })
})
const specialItems = computed(() => {
  return form.specialGroups
    .flatMap((group) => group.items.map((item) => ({
      regionCode: group.regionCode,
      keyword: item.keyword.trim(),
      chartType: item.chartType.trim(),
      targetRank: isRankGuaranteeType(form.orderType) ? Number(item.targetRank || 0) : null,
      coverageNote: form.orderType === 'KEYWORD_COVERAGE' ? item.coverageNote.trim() : null
    })))
    .filter((item) => item.regionCode && (form.orderType === 'CHART_RANK_GUARANTEE' ? item.chartType : item.keyword))
    .map((item) => ({
      ...item,
      coverageNote: item.coverageNote || null
    }))
    .filter((item) => !isRankGuaranteeType(form.orderType) || Number(item.targetRank || 0) > 0)
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
  return regionItems.value.reduce((sum, item) => sum + Number(item.review5Count || 0), 0)
})
const totalReview4Count = computed(() => {
  return regionItems.value.reduce((sum, item) => sum + Number(item.review4Count || 0), 0)
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
const selectedPricingRegionCodes = computed(() => form.regionGroups.map((group) => group.regionCode).filter(Boolean))
const usesChinaPrice = computed(() => selectedPricingRegionCodes.value.includes('CN'))
const availableOrderModules = computed(() => orderModules.value
  .filter((module) => module.enabled)
  .slice()
  .sort((left, right) => left.sortOrder - right.sortOrder || left.id - right.id))
const selectedOrderModule = computed(() => orderModules.value.find((module) => module.id === selectedOrderModuleId.value && module.enabled && module.orderType === form.orderType) || null)
const availableStores = computed(() => selectedOrderModule.value?.storeTypes || [])
watch(selectedOrderModule, () => {
  if (hydratingOrder || !selectedOrderModule.value) return
  if (!availableStores.value.includes(form.storeType) && availableStores.value[0]) form.storeType = availableStores.value[0]
  setDefaultRegions()
}, { flush: 'sync' })
const selectedOrderModuleTab = computed({
  get: () => selectedOrderModuleId.value === null ? '' : String(selectedOrderModuleId.value),
  set: (value: string) => {
    const module = availableOrderModules.value.find((item) => item.id === Number(value))
    if (!module) return
    selectedOrderModuleId.value = module.id
    form.orderType = module.orderType
  }
})
function effectivePrice(code: PriceCode, regionCode = selectedPricingRegionCodes.value[0] || '') {
  const override = regionPricing.value.find(item => item.orderModuleId === selectedOrderModuleId.value)?.regionPrices?.[code]?.[regionCode]
  if (override !== undefined && override !== null) return Number(override)
  if (selectedOrderModule.value && !isSpecialOrder.value) {
    return Number(regionCode === 'CN' ? selectedOrderModule.value.chinaUnitPrice : selectedOrderModule.value.unitPrice) || 0
  }
  return regionCode === 'CN' ? chinaPriceMap[code] : priceMap[code]
}
function moduleLocalizedName(module: OrderModuleConfig) {
  const lang = locale.value.toLowerCase()
  if (lang.startsWith('en')) return module.moduleNameEn || module.moduleName
  if (lang.startsWith('ru')) return module.moduleNameRu || module.moduleName
  if (lang.startsWith('pt')) return module.moduleNamePt || module.moduleName
  if (lang.startsWith('es')) return module.moduleNameEs || module.moduleName
  return module.moduleName
}
function moduleLocalizedDescription(module: OrderModuleConfig) {
  const lang = locale.value.toLowerCase()
  if (lang.startsWith('en')) return module.moduleDescriptionEn || module.moduleDescription
  if (lang.startsWith('ru')) return module.moduleDescriptionRu || module.moduleDescription
  if (lang.startsWith('pt')) return module.moduleDescriptionPt || module.moduleDescription
  if (lang.startsWith('es')) return module.moduleDescriptionEs || module.moduleDescription
  return module.moduleDescription
}
function moduleIcon(orderType: OrderType) {
  return services.find((service) => service.code === orderType)?.icon || Search
}
function ensureSelectedOrderModule() {
  if (selectedOrderModule.value) return
  selectedOrderModuleId.value = orderModules.value.find((module) => module.enabled && module.orderType === form.orderType)?.id || null
}
const estimatedAmount = computed(() => {
  if (form.orderType === 'KEYWORD_INSTALL') return keywordItems.value.reduce((sum, item) => sum + item.quantity * effectivePrice('KEYWORD_INSTALL', item.regionCode), 0)
  if (form.orderType === 'DOWNLOAD') return totalDays.value * regionItems.value.reduce((sum, item) => sum + Number(item.dailyDownloadCount || 0) * effectivePrice('DOWNLOAD', item.regionCode), 0)
  if (form.orderType === 'RATING') {
    return totalDays.value * regionItems.value.reduce((sum, item) => sum + Number(item.rating5Count || 0) * effectivePrice('RATING_5', item.regionCode) + Number(item.rating4Count || 0) * effectivePrice('RATING_4', item.regionCode), 0)
  }
  if (form.orderType === 'REVIEW') {
    return regionItems.value.reduce((sum, item) => sum + Number(item.review5Count || 0) * effectivePrice('REVIEW_5', item.regionCode) + Number(item.review4Count || 0) * effectivePrice('REVIEW_4', item.regionCode), 0)
  }
  return 0
})
const billingLines = computed<BillingLine[]>(() => {
  if (form.orderType === 'KEYWORD_INSTALL') {
    return [{
      label: selectedOrderModule.value ? moduleLocalizedName(selectedOrderModule.value) : t('ordersPage.types.KEYWORD_INSTALL'),
      formula: `${t('orderCreate.keywordCount')} ${quantity.value} × ${t('orderCreate.unitPrice')} ${money(effectivePrice('KEYWORD_INSTALL'), 4)}`,
      amount: money(estimatedAmount.value)
    }]
  }
  if (form.orderType === 'DOWNLOAD') {
    return [{
      label: selectedOrderModule.value ? moduleLocalizedName(selectedOrderModule.value) : t('ordersPage.types.DOWNLOAD'),
      formula: `${t('orderCreate.days')} ${totalDays.value} × ${t('orderCreate.dailyDownloadCount')} ${totalDailyDownloadCount.value || 0} × ${t('orderCreate.unitPrice')} ${money(effectivePrice('DOWNLOAD'), 4)}`,
      amount: money(estimatedAmount.value)
    }]
  }
  if (form.orderType === 'RATING') {
    return [
      {
        label: t('orderCreate.rating5Count'),
        formula: `${t('orderCreate.days')} ${totalDays.value} × ${totalRating5Count.value || 0} × ${t('orderCreate.unitPrice')} ${money(effectivePrice('RATING_5'), 4)}`,
        amount: money(totalDays.value * (totalRating5Count.value || 0) * effectivePrice('RATING_5'))
      },
      {
        label: t('orderCreate.rating4Count'),
        formula: `${t('orderCreate.days')} ${totalDays.value} × ${totalRating4Count.value || 0} × ${t('orderCreate.unitPrice')} ${money(effectivePrice('RATING_4'), 4)}`,
        amount: money(totalDays.value * (totalRating4Count.value || 0) * effectivePrice('RATING_4'))
      }
    ]
  }
  if (form.orderType === 'REVIEW') {
    return [
      {
        label: t('orderCreate.review5Count'),
        formula: `${totalReview5Count.value || 0} × ${t('orderCreate.unitPrice')} ${money(effectivePrice('REVIEW_5'), 4)}`,
        amount: money((totalReview5Count.value || 0) * effectivePrice('REVIEW_5'))
      },
      {
        label: t('orderCreate.review4Count'),
        formula: `${totalReview4Count.value || 0} × ${t('orderCreate.unitPrice')} ${money(effectivePrice('REVIEW_4'), 4)}`,
        amount: money((totalReview4Count.value || 0) * effectivePrice('REVIEW_4'))
      }
    ]
  }
  return [{
    label: typeLabel(form.orderType),
    formula: t('orderCreate.specialBillingPending'),
    amount: t('promotionPage.auditBadge')
  }]
})
const keywordTimeInPast = computed(() => keywordStartMode.value === 'scheduled' && keywordOrderTimeIsPast(keywordInstallDateTime.value, systemNow.value))
function disabledKeywordOrderDate(date: Date) {
  const localDate = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
  return localDate < businessMinute().slice(0, 10)
}
const submitDisabled = computed(() => {
  if (submitting.value || !selectedApp.value) return true
  if (isSpecialOrder.value) return specialItems.value.length === 0
  if (form.orderType === 'KEYWORD_INSTALL') return (keywordStartMode.value === 'scheduled' && (!keywordInstallDateTime.value || keywordTimeInPast.value)) || quantity.value <= 0
  return !dateRange.value || quantity.value <= 0
})
const systemTimeText = computed(() => formatSystemTime(systemNow.value))
const isEditingOrder = computed(() => editingOrderId.value !== null || Number.isFinite(Number(route.query.orderId)))
const submitButtonText = computed(() => {
  if (isSpecialOrder.value) return t('orderCreate.submitDemand')
  return isEditingOrder.value ? t('orderCreate.payAndSubmit') : t('orderCreate.submit')
})

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
    void applyPendingOrderQuery()
    void applyRenewOrderQuery()
  },
  { deep: true }
)

watch(
  () => form.storeType,
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
    ensureSelectedOrderModule()
  }
)

async function loadData() {
  loading.value = true
  baseDataReady.value = false
  try {
    const [appList, regionList, pricingList, regionPricingList, moduleList] = await Promise.all([
      getCustomerApps(),
      getEnabledRegions(),
      getCustomerPricingConfig(),
      getCustomerOrderTypeRegionPricing(),
      getCustomerOrderModules()
    ])
    apps.value = appList
    regions.value = regionList
    regionPricing.value = regionPricingList
    orderModules.value = moduleList
    pricingList.forEach((item) => {
      priceMap[item.code] = Number(item.unitPrice)
      chinaPriceMap[item.code] = Number(item.chinaUnitPrice)
    })
    baseDataReady.value = true
    applyRouteQuery()
    ensureSelectedOrderModule()
    await applyPendingOrderQuery()
    await applyRenewOrderQuery()
  } catch (error) {
    ElMessage.error(errorMessage(error, t('orderCreate.loadFailed')))
  } finally {
    loading.value = false
  }
}

async function submitOrder() {
  if (reviewUploading.value) return
  if (form.orderType === 'KEYWORD_INSTALL' && keywordStartMode.value === 'scheduled' && keywordOrderTimeIsPast(keywordInstallDateTime.value)) {
    ElMessage.warning(t('orderCreate.keywordOrderTimePast'))
    return
  }
  if (!selectedApp.value || (!isSpecialOrder.value && !selectedOrderDates())) {
    ElMessage.warning(t('orderCreate.requiredFields'))
    return
  }
  if (!isSpecialOrder.value && form.orderType !== 'KEYWORD_INSTALL' && regionItems.value.length === 0) {
    ElMessage.warning(t('orderCreate.requiredFields'))
    return
  }
  if (isSpecialOrder.value) {
    if (!form.contactType.trim() || !form.contactValue.trim()) {
      ElMessage.warning(t('orderCreate.contactRequired'))
      return
    }
    if (specialItems.value.length === 0) {
      ElMessage.warning(t('orderCreate.specialItemsRequired'))
      return
    }
    submitting.value = true
    try {
      await submitSpecialAudit({
        orderModuleId: selectedOrderModule.value?.id,
        customerAppId: selectedApp.value.id,
        regionCode: specialItems.value[0]?.regionCode || form.regionCode,
        orderType: form.orderType,
        requestedContent: form.requestedContent.trim(),
        contactType: form.contactType.trim() || null,
        contactValue: form.contactValue.trim() || null,
        items: specialItems.value
      })
      ElMessage.success(t('orderCreate.submitDemandSuccess'))
      await router.push(orderListRoute(form.storeType))
    } catch (error) {
      ElMessage.error(errorMessage(error, t('orderCreate.submitDemandFailed')))
    } finally {
      submitting.value = false
    }
    return
  }
  if (quantity.value <= 0) {
    ElMessage.warning(t('orderCreate.quantityRequired'))
    return
  }

  const payload = buildCustomerOrderPayload()
  if (!payload) {
    ElMessage.warning(t('orderCreate.requiredFields'))
    return
  }
  submitting.value = true
  try {
    const editingId = editingOrderId.value
    const payloadSnapshot = serializeOrderPayload(payload)
    if (editingId && originalEditingStatus.value === 'PENDING_CONFIRM' && payloadSnapshot === originalPayloadSnapshot.value) {
      await router.push(orderListRoute(form.storeType))
      return
    }
    const order = editingId
      ? originalEditingStatus.value === 'PENDING_PAYMENT' && payloadSnapshot === originalPayloadSnapshot.value
        ? await payCustomerOrder(editingId)
        : await resubmitCustomerOrder(editingId, payload)
      : await createCustomerOrder(payload)
    // Read the committed state before showing payment feedback. This also avoids
    // presenting an intermediate response as a failed payment.
    const committedOrder = editingId ? await getCustomerOrder(order.id) : order
    await refreshWalletSilently()
    if (committedOrder.status === 'PENDING_PAYMENT') {
      ElMessage.warning(t('orderCreate.balanceNotEnough'))
    } else {
      ElMessage.success(editingOrderId.value
        ? originalEditingStatus.value === 'PENDING_CONFIRM' ? t('ordersPage.updateSuccess') : t('ordersPage.paySuccess')
        : t('orderCreate.createSuccess'))
    }
    await router.push(orderListRoute(form.storeType))
  } catch (error) {
    ElMessage.error(errorMessage(error, t('orderCreate.createFailed')))
  } finally {
    submitting.value = false
  }
}


async function refreshWalletSilently() {
  try {
    await walletStore.loadCustomerWallet()
  } catch {
    // 订单已创建，余额刷新失败不阻断后续跳转。
  }
}
function selectedOrderDates(): [string, string] | null {
  if (form.orderType === 'KEYWORD_INSTALL') {
    if (keywordStartMode.value === 'immediate') { const today = businessMinute().slice(0, 10); return [today, today] }
    return keywordInstallDateTime.value ? [keywordInstallDateTime.value.slice(0, 10), keywordInstallDateTime.value.slice(0, 10)] : null
  }
  return dateRange.value || null
}

function backToOrderList() {
  router.push(orderListRoute(form.storeType))
}

function buildCustomerOrderPayload(): CreateOrderPayload | null {
  if (!selectedApp.value) return null
  const orderDates = selectedOrderDates()
  if (!orderDates) return null
  const [startDate, endDate] = orderDates
  return {
    customerAppId: selectedApp.value.id,
    regionCode: form.orderType === 'KEYWORD_INSTALL' ? null : regionItems.value[0]?.regionCode || null,
    orderType: form.orderType,
    orderModuleId: selectedOrderModule.value?.id || null,
    startDate,
    endDate,
    scheduledStartAt: form.orderType === 'KEYWORD_INSTALL' && keywordStartMode.value === 'scheduled' ? keywordInstallDateTime.value : null,
    startImmediately: form.orderType === 'KEYWORD_INSTALL' && keywordStartMode.value === 'immediate',
    executionHours: form.orderType === 'KEYWORD_INSTALL' ? form.executionHours : null,
    keywords: [],
    keywordItems: form.orderType === 'KEYWORD_INSTALL' ? keywordItems.value : [],
    regionItems: form.orderType === 'KEYWORD_INSTALL' ? [] : regionItems.value,
    reviewDetails: [],
    dailyDownloadCount: null,
    rating5Count: null,
    rating4Count: null,
    review5Count: null,
    review4Count: null
  }
}

function serializeOrderPayload(payload: CreateOrderPayload) {
  return JSON.stringify(payload)
}

function createRegionGroup(regionCode = ''): RegionKeywordGroup {
  return {
    regionCode,
    keywordItems: [{ keyword: '', quantity: null }],
    reviewAttachments: [],
    dailyDownloadCount: null,
    rating5Count: 0,
    rating4Count: 0,
    review5Count: 0,
    review4Count: 0
  }
}

function createSpecialItem(): SpecialKeywordInput {
  return {
    regionCode: '',
    keyword: '',
    chartType: '',
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
    reviewAttachments: [...group.reviewAttachments],
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
  clearUnsupportedMainlandRegions(orderType)
}

function excludesMainlandChina(orderType: OrderType = form.orderType) {
  return orderType === 'RATING' || orderType === 'REVIEW'
}

function clearUnsupportedMainlandRegions(orderType: OrderType = form.orderType) {
  if (!excludesMainlandChina(orderType)) return
  form.regionGroups.forEach((group) => {
    if (group.regionCode === 'CN') group.regionCode = ''
  })
}

function availableRegionsForGroup(groupIndex: number) {
  const currentRegionCode = form.regionGroups[groupIndex]?.regionCode
  const usedRegionCodes = new Set(
    form.regionGroups
      .map((group, index) => index === groupIndex ? '' : group.regionCode)
      .filter(Boolean)
  )
  return prioritizeRegions(selectedAppRegions.value.filter((region) => {
    if (excludesMainlandChina() && region.code === 'CN') return false
    if (region.code === currentRegionCode) return true
    if (usedRegionCodes.has('CN')) return false
    if (region.code === 'CN' && usedRegionCodes.size > 0) return false
    return !usedRegionCodes.has(region.code)
  }))
}

function availableSpecialRegionsForGroup(groupIndex: number) {
  const currentRegionCode = form.specialGroups[groupIndex]?.regionCode
  const usedRegionCodes = new Set(
    form.specialGroups
      .map((group, index) => index === groupIndex ? '' : group.regionCode)
      .filter(Boolean)
  )
  return prioritizeRegions(selectedAppRegions.value.filter((region) => {
    if (region.code === currentRegionCode) return true
    if (usedRegionCodes.has('CN')) return false
    if (region.code === 'CN' && usedRegionCodes.size > 0) return false
    return !usedRegionCodes.has(region.code)
  }))
}

const hasChinaRegionSelection = computed(() =>
  form.regionGroups.some((group) => group.regionCode === 'CN')
  || form.specialGroups.some((group) => group.regionCode === 'CN')
)
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

function openBatchKeywordDialog(groupIndex: number, mode: 'install' | 'coverage' = 'install') {
  const group = mode === 'coverage' ? form.specialGroups[groupIndex] : form.regionGroups[groupIndex]
  if (!group) return
  batchKeywordMode.value = mode
  batchKeywordGroupIndex.value = groupIndex
  batchKeywordText.value = ''
  batchKeywordDialogVisible.value = true
}

function confirmBatchKeywordImport() {
  const groupIndex = batchKeywordGroupIndex.value
  if (batchKeywordMode.value === 'coverage') {
    const group = groupIndex === null ? undefined : form.specialGroups[groupIndex]
    if (!group || form.orderType !== 'KEYWORD_COVERAGE') return
    let keywords: string[]
    try {
      keywords = parseCoverageKeywordText(batchKeywordText.value)
    } catch (error) {
      if (!(error instanceof CoverageKeywordImportError)) throw error
      ElMessage.warning(t('orderCreate.batchCoverageKeywordInvalidRow', { row: error.row }))
      return
    }
    if (keywords.length === 0) {
      ElMessage.warning(t('orderCreate.batchCoverageKeywordEmpty'))
      return
    }
    const items = group.items.filter((item) => item.keyword.trim())
    const existingKeywords = new Set(items.map((item) => item.keyword.trim()))
    const newKeywords = keywords.filter((keyword) => !existingKeywords.has(keyword))
    group.items = [...items, ...newKeywords.map((keyword) => ({ ...createSpecialItem(), keyword }))]
    batchKeywordDialogVisible.value = false
    ElMessage.success(t('orderCreate.batchKeywordImportSuccess', { count: newKeywords.length }))
    return
  }
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
    const rows = isCsv ? parseCsv(await readTextFile(file)) : await readExcelRows(file, 3)
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

async function downloadReviewTemplate() {
  const workbook = await createExcelWorkbook()
  workbook.creator = 'Youou-ASO'
  workbook.created = new Date()
  const sheet = workbook.addWorksheet('Review Import')
  sheet.columns = [
    ...(form.storeType === 'GOOGLE_PLAY' ? [] : [{ header: 'Review Title', key: 'reviewTitle', width: 32 }]),
    { header: 'Review', key: 'reviewContent', width: 96 }
  ]
  sheet.getRow(1).font = { bold: true }
  sheet.addRow({
    reviewTitle: 'Great',
    reviewContent: 'The app is easy to operate and runs smoothly. Overall, the experience is great.'
  })
  sheet.addRow({
    reviewTitle: 'Nice',
    reviewContent: 'Practical functions with a clear interface, making it very user-friendly.'
  })
  const buffer = await workbook.xlsx.writeBuffer()
  downloadBlobFile(
    t('orderCreate.reviewTemplateFilename'),
    new Blob([buffer as BlobPart], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
  )
}

function openReviewImport(groupIndex: number) {
  if (form.orderType !== 'REVIEW') return
  if (!selectedApp.value) {
    ElMessage.warning(t('orderCreate.selectApp'))
    return
  }
  const group = form.regionGroups[groupIndex]
  if (!group?.regionCode) {
    ElMessage.warning(t('orderCreate.reviewImportSelectRegion'))
    return
  }
  reviewImportGroupIndex.value = groupIndex
  reviewImportInput.value?.click()
}

async function handleReviewImport(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  input.value = ''
  const index = reviewImportGroupIndex.value
  const group = index === null ? undefined : form.regionGroups[index]
  if (!file || !group) return
  if (reviewUploading.value) return
  if (!selectedApp.value) {
    ElMessage.warning(t('orderCreate.selectApp'))
    return
  }
  if (!/\.(xlsx|csv)$/i.test(file.name) || !file.size || file.size > 20 * 1024 * 1024 || group.reviewAttachments.length >= 20) {
    ElMessage.warning(t('orderCreate.reviewAttachmentLimit'))
    return
  }
  const appId = selectedApp.value.id
  const regionCode = group.regionCode
  reviewUploading.value = true
  try {
    const attachment = await uploadReviewAttachment(file, false, undefined)
    if (form.customerAppId !== appId || form.orderType !== 'REVIEW'
      || group.regionCode !== regionCode || !form.regionGroups.includes(group)) return
    group.reviewAttachments.push(attachment)
    regionGroupDrafts.REVIEW = cloneRegionGroups(form.regionGroups)
  } catch (error) {
    ElMessage.error(errorMessage(error, t('orderCreate.reviewImportReadFailed')))
  } finally {
    reviewUploading.value = false
  }
}

async function readExcelRows(file: File, columnCount: number) {
  const workbook = await createExcelWorkbook()
  await workbook.xlsx.load(await file.arrayBuffer())
  const sheet = workbook.worksheets[0]
  if (!sheet) return []
  const rows: string[][] = []
  sheet.eachRow({ includeEmpty: false }, (row) => {
    rows.push(Array.from({ length: columnCount }, (_, index) => excelCellText(row.getCell(index + 1).value)))
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
  if (!firstItem) return false
  if (!(form.orderType === 'CHART_RANK_GUARANTEE' ? firstItem.chartType.trim() : firstItem.keyword.trim())) return false
  if (isRankGuaranteeType(form.orderType)) return Number(firstItem.targetRank || 0) > 0
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

function applyRouteQuery() {
  const queryStore = route.query.storeType
  if (queryStore === 'APP_STORE' || queryStore === 'GOOGLE_PLAY' || queryStore === 'IPAD_STORE') {
    form.storeType = queryStore
  }

  const queryOrderType = route.query.orderType
  if (isOrderType(queryOrderType)) {
    form.orderType = queryOrderType
  }

  const queryOrderModuleId = Number(route.query.orderModuleId)
  if (Number.isFinite(queryOrderModuleId) && orderModules.value.some((module) => module.id === queryOrderModuleId && module.enabled && module.orderType === form.orderType)) {
    selectedOrderModuleId.value = queryOrderModuleId
  } else {
    ensureSelectedOrderModule()
  }

  const queryAppId = Number(route.query.appId)
  if (Number.isFinite(queryAppId)) {
    const app = apps.value.find((candidate) => candidate.id === queryAppId)
    if (app) {
      form.storeType = app.storeType
      form.customerAppId = app.id
      setDefaultRegions()
    }
  }
}

async function applyPendingOrderQuery() {
  const queryOrderId = Number(route.query.orderId)
  if (!Number.isFinite(queryOrderId)) {
    editingOrderId.value = null
    hydratedOrderId.value = null
    originalPayloadSnapshot.value = null
    originalEditingStatus.value = null
    return
  }
  if (!baseDataReady.value) {
    return
  }
  if (hydratedOrderId.value === queryOrderId) {
    return
  }
  try {
    const order = await getCustomerOrder(queryOrderId)
    if (!['PENDING_PAYMENT', 'PENDING_CONFIRM', 'CANCELLED'].includes(order.status) || order.sourceAuditId) {
      ElMessage.warning(t('ordersPage.onlyUnconfirmedEditable'))
      editingOrderId.value = null
      hydratedOrderId.value = null
      originalPayloadSnapshot.value = null
      return
    }
    hydratePendingPaymentOrder(order)
    editingOrderId.value = order.id
    originalEditingStatus.value = order.status
    hydratedOrderId.value = order.id
    hydratedRenewOrderId.value = null
    const payload = buildCustomerOrderPayload()
    originalPayloadSnapshot.value = payload ? serializeOrderPayload(payload) : null
  } catch (error) {
    ElMessage.error(errorMessage(error, t('orderCreate.loadFailed')))
    editingOrderId.value = null
    hydratedOrderId.value = null
    originalPayloadSnapshot.value = null
  }
}

async function applyRenewOrderQuery() {
  const queryRenewOrderId = Number(route.query.renewOrderId)
  if (!Number.isFinite(queryRenewOrderId)) {
    hydratedRenewOrderId.value = null
    return
  }
  if (!baseDataReady.value || Number.isFinite(Number(route.query.orderId))) {
    return
  }
  if (hydratedRenewOrderId.value === queryRenewOrderId) {
    return
  }
  try {
    const order = await getCustomerOrder(queryRenewOrderId)
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
      hydrateSpecialAudit(audit)
    } else {
      hydratePendingPaymentOrder(order)
      resetRenewOrderDate(order.orderType)
    }
    editingOrderId.value = null
    hydratedOrderId.value = null
    hydratedRenewOrderId.value = order.id
    originalPayloadSnapshot.value = null
  } catch (error) {
    ElMessage.error(errorMessage(error, t('orderCreate.loadFailed')))
    hydratedRenewOrderId.value = null
  }
}

async function loadRenewSourceAudit(sourceAuditId: number) {
  const audits = await getCustomerSpecialAudits()
  return audits.find((audit) => audit.id === sourceAuditId) || null
}

function resetRenewOrderDate(orderType: OrderType) {
  if (orderType === 'KEYWORD_INSTALL') {
    keywordStartMode.value = 'immediate'
    keywordInstallDateTime.value = defaultKeywordOrderTime()
    return
  }
  dateRange.value = ''
}

function hydratePendingPaymentOrder(order: Order) {
  hydratingOrder = true
  try {
    form.storeType = order.storeType
    form.orderType = order.orderType
    selectedOrderModuleId.value = order.orderModuleId || null
    form.customerAppId = order.customerAppId
    form.regionCode = order.regionCode === 'MULTI' ? '' : order.regionCode || ''
    form.executionHours = order.executionHours || 1
    if (order.orderType === 'KEYWORD_INSTALL') {
      keywordStartMode.value = 'scheduled'
      keywordInstallDateTime.value = order.scheduledStartAt?.replace(' ', 'T').slice(0, 16) || `${order.orderStartDate}T00:00`
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

function hydrateSpecialAudit(audit: SpecialOrderAudit) {
  hydratingOrder = true
  try {
    form.storeType = audit.storeType
    form.orderType = audit.orderType
    form.customerAppId = audit.customerAppId
    form.regionCode = audit.regionCode || ''
    form.contactType = audit.contactType || ''
    form.contactValue = audit.contactValue || ''
    form.requestedContent = audit.requestedContent || ''
    form.specialGroups = specialGroupsFromAudit(audit)
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
      chartType: item.chartType || (audit.orderType === 'CHART_RANK_GUARANTEE' ? item.keyword : '') || '',
      targetRank: item.targetRank || 1,
      coverageNote: item.coverageNote || ''
    })
    groups.set(regionCode, group)
  })
  const result = Array.from(groups.values())
  result.forEach((group) => {
    group.items = group.items.filter((item) => audit.orderType === 'CHART_RANK_GUARANTEE' ? item.chartType.trim() : item.keyword.trim())
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

  const groups = new Map<string, RegionKeywordGroup>()
  const days = Math.max(1, Number(order.totalDays || 1))
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
  for (const file of order.reviewAttachments || []) {
    const group = groups.get(file.regionCode || '')
    if (group) group.reviewAttachments.push(file)
  }
  return Array.from(groups.values()).length ? Array.from(groups.values()) : [createRegionGroup(form.regionCode)]
}

function normalizeHydratedGroups(groups: RegionKeywordGroup[], listKey: 'keywordItems') {
  const normalized = groups.length ? groups : [createRegionGroup(form.regionCode)]
  normalized.forEach((group) => {
    if (listKey === 'keywordItems') {
      group.keywordItems = group.keywordItems.filter((item) => item.keyword.trim())
      if (group.keywordItems.length === 0) group.keywordItems = [{ keyword: '', quantity: null }]
    }
  })
  return normalized
}

function orderListRoute(storeType: StoreType) {
  if (storeType === 'GOOGLE_PLAY') return { name: 'user-orders-google' }
  if (storeType === 'IPAD_STORE') return { name: 'user-orders-ipad' }
  return { name: 'user-orders-apple' }
}

function isOrderType(value: unknown): value is OrderType {
  return value === 'KEYWORD_INSTALL'
    || value === 'DOWNLOAD'
    || value === 'RATING'
    || value === 'REVIEW'
    || value === 'RANK_GUARANTEE'
    || value === 'CHART_RANK_GUARANTEE'
    || value === 'KEYWORD_COVERAGE'
}

function isRenewableOrderStatus(status: Order['status']) {
  return status === 'PENDING_CONFIRM'
    || status === 'PENDING_EXECUTION'
    || status === 'EXECUTING'
    || status === 'PAUSED'
    || status === 'COMPLETED'
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

function regionSearchLabel(code: string) {
  return `${code} · ${regionLabel(code)}`
}

function flagUrl(code: string) {
  return `https://flagcdn.com/24x18/${code.toLowerCase()}.png`
}

function typeLabel(type?: OrderType | null) {
  return type ? t(`ordersPage.types.${type}`) : '-'
}

const money = formatCurrency

function storeLabel(storeType: StoreType) {
  if (storeType === 'GOOGLE_PLAY') return 'Google Play'
  if (storeType === 'IPAD_STORE') return 'iPad Store'
  return 'App Store'
}

function today() {
  const date = new Date()
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function formatSystemTime(date: Date) {
  const formatter = new Intl.DateTimeFormat(locale.value, {
    timeZone: 'Asia/Shanghai',
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false
  })
  const parts = Object.fromEntries(formatter.formatToParts(date).map((part) => [part.type, part.value]))
  return `${parts.year}-${parts.month}-${parts.day} ${parts.hour}:${parts.minute}`
}

function errorMessage(error: unknown, fallback: string) {
  if (typeof error === 'object' && error !== null && 'response' in error) {
    const response = (error as { response?: { data?: { code?: string; message?: string } } }).response
    if (response?.data?.code === 'ORDER_TIME_IN_PAST') return t('orderCreate.keywordOrderTimePast')
    if (response?.data?.code === 'BALANCE_NOT_ENOUGH') return t('orderCreate.balanceNotEnough')
    if (response?.data?.code === 'PRICE_NOT_CONFIGURED') return t('orderCreate.priceNotConfigured')
    if (response?.data?.code === 'PRICE_DISABLED') return t('orderCreate.priceDisabled')
    return response?.data?.message || fallback
  }
  return fallback
}
</script>

<style scoped>
.order-time-error { margin: 8px 0 0; color: var(--el-color-danger); font-size: 12px; }
.review-attachments { padding: 16px; }
.review-attachments p { color: #909399; font-size: 13px; }
.review-attachment-row { display: flex; align-items: center; gap: 16px; padding: 6px 0; overflow-wrap: anywhere; }
.order-create-page {
  color: #0f172a;
  min-height: calc(100vh - 112px);
  padding-bottom: 72px;
  background:
    radial-gradient(circle at 18% 0%, rgb(47 125 244 / 7%), transparent 26%),
    linear-gradient(180deg, #f7faff 0%, #f4f7fb 100%);
}

.order-shell {
  width: 100%;
}

.type-tabs {
  margin-bottom: 0;
  padding: 6px;
  border: 1px solid #e7edf5;
  border-radius: 12px;
  background: #f8fafc;
}

.selected-module-card {
  position: relative;
  display: flex;
  gap: 14px;
  align-items: stretch;
  margin-top: 14px;
  padding: 14px 16px;
  overflow: hidden;
  border: 1px solid #cfe0ff;
  border-radius: 12px;
  background: linear-gradient(135deg, #f5f9ff 0%, #fff 72%);
}

.selected-module-marker {
  width: 4px;
  flex: 0 0 4px;
  border-radius: 999px;
  background: linear-gradient(180deg, #2f7cf6, #1757d8);
}

.selected-module-content {
  min-width: 0;
}

.selected-module-label {
  display: block;
  margin-bottom: 3px;
  color: #6480a8;
  font-size: 12px;
  font-weight: 600;
}

.selected-module-content strong {
  display: block;
  color: #17233b;
  font-size: 16px;
  line-height: 1.4;
}

.selected-module-content p {
  margin: 4px 0 0;
  color: #5d6f8d;
  font-size: 13px;
  line-height: 1.65;
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
  display: grid;
  width: 100%;
  min-width: 0;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 7px;
  border: 0;
  overflow: visible;
}

.type-tabs :deep(.el-tabs__item) {
  height: auto;
  min-height: 52px;
  min-width: 0;
  padding: 9px 12px;
  justify-content: center;
  border: 1px solid #dbe4ef;
  border-radius: 8px;
  background: #fff;
  color: #334155;
  font-size: 13px;
  font-weight: 700;
  line-height: 1.3;
  white-space: normal;
  transition: border-color 160ms ease, background 160ms ease, box-shadow 160ms ease, color 160ms ease;
}

.type-tabs :deep(.el-tabs__item:last-child) {
  border-right: 1px solid #dbe4ef;
}

.type-tabs :deep(.el-tabs--top > .el-tabs__header .el-tabs__item:nth-child(2)) {
  padding-left: 12px;
}

.type-tabs :deep(.el-tabs--top > .el-tabs__header .el-tabs__item:last-child) {
  padding-right: 12px;
}

.type-tabs :deep(.el-tabs__item:hover) {
  border-color: #93c5fd;
  color: #1d4ed8;
}

.type-tabs :deep(.el-tabs__item.is-active) {
  border-color: #60a5fa;
  background: linear-gradient(135deg, #eff6ff, #f8fbff);
  box-shadow: 0 5px 14px rgb(37 99 235 / 12%);
  color: #1d4ed8;
}

.type-tabs :deep(.el-tabs__active-bar) {
  display: none;
}

.tab-label {
  display: grid;
  grid-template-columns: 18px minmax(0, 1fr);
  align-items: center;
  gap: 8px;
  width: 100%;
  max-width: 100%;
  text-align: left;
  overflow-wrap: break-word;
}

.tab-icon {
  width: 16px;
  height: 16px;
  color: #64748b;
}

.type-tabs :deep(.el-tabs__item.is-active) .tab-icon {
  color: #2563eb;
}

.order-form {
  width: 100%;
}

.edit-toolbar {
  display: flex;
  justify-content: flex-start;
  margin-bottom: 12px;
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
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  background: #ffffff;
  box-shadow: 0 18px 42px rgb(24 34 48 / 6%);
}

.summary-panel {
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  background: #ffffff;
  box-shadow: 0 18px 42px rgb(24 34 48 / 6%);
  align-self: start;
  margin-top: 0;
}

.form-section {
  padding: 16px 20px;
  border-bottom: 0;
}

.form-section:last-child {
  border-bottom: 0;
}

.section-heading {
  margin-bottom: 10px;
}

.section-heading h2 {
  margin: 0;
  color: #0f172a;
  font-size: 13px;
  font-weight: 800;
}

.section-heading h2 span {
  color: #dc2626;
}

.section-heading-actions {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.region-tool-buttons {
  display: inline-flex;
  align-items: center;
  flex-wrap: wrap;
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

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(260px, 1fr));
  gap: 18px 28px;
  align-items: end;
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

.form-compact {
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
  width: min(100%, 520px);
  display: grid;
  grid-template-columns: minmax(220px, 1fr) max-content;
  gap: 10px;
  align-items: center;
}

.add-app-button {
  width: auto;
  white-space: nowrap;
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
  color: #2563eb;
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
  border: 1px solid #e2e8f0;
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
  background: #eff6ff;
  border-color: transparent;
  color: #2563eb;
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
  color: #64748b;
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
  color: #334155;
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
  grid-template-columns: minmax(0, 340px) minmax(0, max-content);
  gap: 16px 28px;
  align-items: end;
}

.region-select-line {
  display: grid;
  grid-template-columns: max-content minmax(220px, 1fr);
  gap: 12px;
  align-items: center;
}

.region-actions {
  display: grid;
  grid-template-columns: max-content max-content;
  gap: 12px;
  align-items: center;
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
  grid-template-columns: minmax(0, 340px) minmax(0, max-content);
  gap: 16px 28px;
  align-items: end;
}

.region-action-line {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.region-action-line :deep(.el-button + .el-button) {
  margin-left: 0;
}

.region-select-line span,
.region-actions span,
.region-action-line span {
  color: #334155;
  font-size: 14px;
  font-weight: 700;
  line-height: 1.35;
  overflow-wrap: anywhere;
}

.remove-region-button {
  width: auto;
  min-width: 78px;
  max-width: 220px;
  min-height: 28px;
  padding: 6px 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 800;
}

.keyword-table {
  width: min(100%, 665px);
  overflow: hidden;
  border: 1px solid #e2e8f0;
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
  border-right: 1px solid #e2e8f0;
  color: #334155;
  font-size: 13px;
  font-weight: 800;
}

.keyword-table-head span:last-child {
  border-right: 0;
}

.keyword-row {
  min-height: 48px;
  align-items: center;
  border-top: 1px solid #e2e8f0;
}

.keyword-cell {
  min-height: 48px;
  padding: 7px 9px;
  border-right: 1px solid #e2e8f0;
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
  box-shadow: 0 0 0 1px #e2e8f0 inset;
}

.keyword-delete-button {
  width: 28px;
  height: 28px;
  min-height: 28px;
  padding: 0;
  justify-self: center;
  color: #dc2626;
}

.region-metric-table {
  width: min(100%, 665px);
  overflow: hidden;
  border: 1px solid #e2e8f0;
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
  border-right: 1px solid #e2e8f0;
  color: #334155;
  font-size: 13px;
  font-weight: 800;
}

.region-metric-head span:last-child {
  border-right: 0;
}

.region-metric-row {
  min-height: 48px;
  align-items: center;
  border-top: 1px solid #e2e8f0;
}

.region-metric-cell {
  min-height: 48px;
  padding: 7px 9px;
  border-right: 1px solid #e2e8f0;
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
  box-shadow: 0 0 0 1px #e2e8f0 inset;
}

.review-detail-table,
.special-item-table {
  width: min(100%, 860px);
  overflow: hidden;
  border: 1px solid #e2e8f0;
}

.special-item-table {
  width: 100%;
  border-color: #e2e8f0;
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

.coverage-keyword-table .special-item-head,
.coverage-keyword-table .special-item-row {
  grid-template-columns: minmax(220px, 1fr) 64px minmax(220px, 1fr);
}

.review-detail-head,
.special-item-head {
  background: #f7faff;
}

.review-detail-head span,
.special-item-head span {
  min-height: 40px;
  padding: 11px 14px;
  border-right: 1px solid #e2e8f0;
  color: #334155;
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
  border-top: 1px solid #e2e8f0;
}

.review-detail-row > *,
.special-item-row > * {
  min-height: 48px;
  padding: 7px 9px;
  border-right: 1px solid #e2e8f0;
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
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: #fbfdff;
}

.special-item-footer {
  min-height: 42px;
  padding: 7px 10px;
  border-top: 1px solid #e2e8f0;
  display: flex;
  align-items: center;
  background: #ffffff;
}

.contact-grid {
  display: grid;
  grid-template-columns: minmax(220px, 320px) minmax(320px, 1fr);
  gap: 14px 18px;
  padding: 14px 16px 2px;
  border: 1px solid #e1e8f2;
  border-radius: 6px;
  background: #ffffff;
}

.contact-grid :deep(.el-select),
.contact-grid :deep(.el-input) {
  width: 100%;
}

.contact-remark {
  grid-column: 1 / -1;
}

.special-request-field {
  margin-bottom: 0;
  padding: 14px 16px 16px;
  border: 1px solid #e1e8f2;
  border-radius: 6px;
  background: #ffffff;
}

.special-request-field :deep(.el-textarea__inner) {
  min-height: 112px !important;
}

.add-keyword-button {
  align-self: center;
  margin-left: 0;
  min-height: 28px;
  padding: 4px 8px;
  color: #64748b;
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

.summary-panel {
  padding: 16px 20px 20px;
}

.summary-heading {
  padding-bottom: 14px;
  border-bottom: 1px solid #eef2f6;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.summary-heading span,
.summary-item span {
  color: #64748b;
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
  gap: 12px;
}

.summary-item strong {
  min-width: 0;
  color: #0f172a;
  font-size: 14px;
  font-weight: 800;
  text-align: right;
  word-break: break-word;
}

.submit-button {
  width: 100%;
  min-height: 42px;
  font-weight: 800;
  background: #2563eb;
  border-color: #2563eb;
}

.billing-note {
  margin-top: 10px;
  padding: 12px;
  border: 1px solid #9ec5fe;
  border-radius: 6px;
  background: #eff6ff;
  display: flex;
  flex-direction: column;
  gap: 6px;
  color: #334155;
  font-size: 13px;
  line-height: 1.6;
}

.billing-note strong {
  color: #2563eb;
}

.billing-breakdown {
  margin-top: 14px;
  padding-top: 14px;
  border-top: 1px solid #eef2f6;
}

.breakdown-title {
  margin-bottom: 10px;
  color: #0f172a;
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
  color: #0f172a;
  font-size: 13px;
  font-weight: 800;
  line-height: 1.35;
}

.breakdown-line span {
  display: block;
  margin-top: 4px;
  color: #64748b;
  font-size: 12px;
  line-height: 1.45;
}

.breakdown-line b {
  color: #0f172a;
  font-size: 13px;
  font-weight: 800;
  white-space: nowrap;
}

.summary-checkout {
  margin-top: 16px;
  padding-top: 14px;
  border-top: 1px solid #e2e8f0;
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
  color: #0f172a;
}

.checkout-total strong {
  color: #dc2626;
  font-size: 20px;
}

@media (max-width: 980px) {
  .order-workspace {
    grid-template-columns: 1fr;
  }

  .summary-panel {
    position: static;
  }

  .type-tabs :deep(.el-tabs__nav) {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .form-grid {
    grid-template-columns: 1fr;
  }

  .special-detail-panel {
    width: 100%;
  }
}

@media (max-width: 640px) {
  .app-picker-line {
    width: 100%;
    grid-template-columns: 1fr;
  }

  .add-app-button {
    justify-self: start;
  }

  .count-grid,
  .region-group-head,
  .region-metric-toolbar,
  .store-switch {
    grid-template-columns: 1fr;
  }

  .region-select-line,
  .region-actions,
  .region-action-line {
    grid-template-columns: 1fr;
    align-items: start;
  }

  .step-field-grid {
    grid-template-columns: 1fr;
    gap: 10px;
  }

  .system-time {
    width: 100%;
    min-height: auto;
    align-items: flex-start;
    flex-wrap: wrap;
    gap: 4px 8px;
    line-height: 1.5;
    overflow-wrap: anywhere;
  }

  .keyword-table-head,
  .keyword-row {
    grid-template-columns: minmax(0, 1fr) minmax(0, 1fr) 54px;
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

  .coverage-keyword-table .special-item-head,
  .coverage-keyword-table .special-item-row {
    min-width: 520px;
  }

  .contact-grid {
    grid-template-columns: 1fr;
    padding: 12px 12px 0;
  }

  .keyword-table-head span:last-child,
  .keyword-row .keyword-cell:last-child {
    display: none;
  }

  .form-section,
  .summary-panel {
    padding: 14px;
  }

  .summary-item {
    flex-direction: column;
    gap: 4px;
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

.mobile-service-select { display:none; }
.order-main { container-type:inline-size; }
.step-field-grid { grid-template-columns:repeat(2,minmax(0,1fr)); }
.step-field, .app-picker-line { min-width:0; max-width:100%; }
.app-picker-line > .el-select { min-width:0; width:100%; }
@container (max-width:620px) { .step-field-grid { grid-template-columns:minmax(0,1fr); } }
@media(max-width:700px) {
 .mobile-service-select { display:block; width:100%; }
 .type-tabs, .selected-module-card { display:none; }
 .store-switch { display:grid; grid-template-columns:repeat(3,minmax(0,1fr)); width:100%; }
 .store-switch :deep(.el-radio-button), .store-switch :deep(.el-radio-button__inner) { width:100%; min-width:0; }
 .store-switch :deep(.el-radio-button__inner) { padding:10px 3px; font-size:11px; }
 .store-option { gap:3px; }
 .flow-section { margin-bottom:20px; }
}

.keyword-start-mode { display:flex; flex-wrap:wrap; margin-bottom:12px; }
.keyword-start-hint { margin:0; color:#64748b; line-height:1.6; font-size:13px; }
</style>










