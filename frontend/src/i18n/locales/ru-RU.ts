import baseEn from './en-US'
import localizedOrders from './orders-page-ru-full'
const en = { ...baseEn, ordersPage: { ...baseEn.ordersPage, ...localizedOrders } }
import wallet from './wallet-ru'
import dynamicPromotion from './dynamic-promotion-ru'
import orderModulesAdmin from './order-modules-admin-ru'
import adminAccounts from './admin-accounts-ru'
import specialAudit from './special-audit-ru'
import orderDetail from './order-detail-ru'
import dashboard from './dashboard-ru'
import customers from './customers-ru'
import accountSettings from './account-settings-ru'
import applications from './applications-ru'
import home from './home-ru'
import generatedConfig from './generated-config-ru'
import generatedOrderCreate from './generated-order-create-ru'
import residual from './generated-residual-ru'
const messages = { ...en, ...generatedConfig, ...generatedOrderCreate, home, wallet, dynamicPromotion, orderModulesAdmin, specialAudit, orderDetail, dashboard, customers, accountSettings, applications, adminAccounts: { ...en.adminAccounts, ...adminAccounts },
  ordersPage:{...en.ordersPage,columnSettings:'Столбцы',selectAllColumns:'Показать все',restoreDefaultColumns:'По умолчанию'},
  common:{...en.common,language:'Язык',chinese:'中文',english:'English',russian:'Русский',portuguese:'Português',spanish:'Español',edit:'Изменить',cancel:'Отмена',save:'Сохранить'},
  account:{unknown:'Аккаунт',userFallback:'Пользователь {id}'},
  public:{nav:{aria:'Публичная навигация',login:'Войти',register:'Регистрация'}},
  auth:{...en.auth,loginTitle:'Вход',loginSubtitle:'Войдите, чтобы управлять приложениями и услугами.',registerTitle:'Регистрация',registerSubtitle:'Создайте аккаунт для управления приложениями.',account:'Имя пользователя или email',username:'Имя пользователя',email:'Email',password:'Пароль',loginButton:'Войти',registerButton:'Создать аккаунт',forgotPassword:'Забыли пароль?',logout:'Выйти',cancel:'Отмена',requiredFields:'Заполните обязательные поля',loginFailed:'Ошибка входа. Проверьте данные.'},
  menu:{...en.menu,home:'Главная',promotion:'Услуги продвижения',applications:'Приложения',orders:'Центр заказов',appleOrders:'Заказы Apple',googleOrders:'Заказы Google',ipadOrders:'Заказы iPad',settings:'Настройки аккаунта',customers:'Пользователи',pendingReviewOrders:'На проверке',pendingConfirmOrders:'На подтверждении',orderExecution:'Статусы заказов',pendingExecutionOrders:'Ожидают выполнения',executingOrders:'Выполняются',pausedOrders:'Приостановлены',completedOrders:'Завершены',auditManagement:'Проверка',finance:'Финансы',financeTransactions:'Финансовые операции',rechargeRecords:'Пополнения',pricing:'Модули заказов',walletTransactionTypeConfig:'Типы операций',customerServiceConfig:'Поддержка',mailConfig:'Почта',homeMetricsConfig:'Данные главной страницы',regions:'Регионы',systemManagement:'Система',adminAccounts:'Администраторы',roles:'Права доступа'},
  homeMetricsConfig:{title:'Данные главной страницы',subtitle:'Настройте четыре показателя на публичной главной странице.',valuePlaceholder:'Введите значение',updatedAt:'Обновлено',previewTip:'После сохранения изменения появятся на главной странице.',required:'Заполните все значения',saved:'Данные сохранены',loadFailed:'Не удалось загрузить данные',saveFailed:'Не удалось сохранить данные'},
}

export default {
  ...messages,
  ...residual,
  ordersPage: {
    ...baseEn.ordersPage,
    ...messages.ordersPage,
    adjustmentReason: "Причина изменения",
    adjustmentReasonPlaceholder: "Укажите причину, например удаление приложения из магазина",
    adjustmentReasonRequired: "Укажите причину изменения",
    adjustmentReasonNotRecorded: "Причина старого изменения не записана",
    editCompletedTitle: 'Изменить выполненное количество',
    editCompletedTip: 'Количество можно только уменьшить. Разница по исходной цене возвращается на баланс клиента. Заказ остаётся завершённым.',
    completedQuantityInvalid: 'Укажите целое число от нуля до текущего выполненного количества.',
    currentNetAmount: 'Текущая сумма за вычетом возвратов',
    saveCompletedAdjustment: 'Сохранить и вернуть средства',
    closeOrder: 'Закрыть заказ',
    closePausedTip: 'Указанное количество станет выполненным. Допустимо от нуля до исходного количества; разница возвращается.',
    closeQuantityInvalid: 'Количество должно быть целым числом от нуля до исходного количества каждой позиции.',
    closeSuccess: 'Заказ закрыт',

    actionDetail: 'Подробнее', actionPaySubmit: 'Оплатить', actionPayEdit: 'Оплата/правка', actionRenew: 'Повторить', actionConfirm: 'Подтвердить', actionExecute: 'Выполнить', actionPause: 'Пауза', actionEditProgress: 'Прогресс', actionResume: 'Продолжить', actionReview: 'Проверить', actionCancel: 'Отменить', actionMore: 'Ещё', appIdentifier: 'ID приложения', editOrder: 'Изменить заказ', updateSuccess: 'Заказ обновлён', onlyUnconfirmedEditable: 'Изменять можно только неподтверждённые заказы',
    editSubmit: 'Изменить данные',
    payOrEdit: 'Оплатить / изменить',
    payNow: 'Оплатить',
    payConfirmTitle: 'Подтвердить оплату',
    payConfirmMessage: 'Списать {amount} за текущий заказ без изменения его данных?',
    paySuccess: 'Оплата прошла, заказ отправлен',
    payFailed: 'Оплата не выполнена. Проверьте баланс'
  },
  orderCreate: {

    ...baseEn.orderCreate,
    ...messages.orderCreate,
    batchCoverageKeywordDialogTip: 'Введите по одному ключевому слову в строке без указания количества. Пустые строки пропускаются, повторяющиеся ключевые слова не добавляются повторно.',
    batchCoverageKeywordPlaceholder: 'Пример:\nключевое слово\nмоё ключевое слово',
    batchCoverageKeywordEmpty: 'Введите хотя бы одно ключевое слово',
    batchCoverageKeywordInvalidRow: 'Ключевое слово в строке {row} не должно превышать 255 символов',
    itemPricingBilling: 'Итог рассчитывается как сумма цены каждой позиции, умноженной на количество дней.', executionDays: 'Дни',
    selectOrderDateTime: 'Выберите дату и время заказа',
    cancelledOrderEditHint: 'При повторной отправке отменённого заказа текущая сумма списывается заново, затем требуется подтверждение администратора.',
    adminEditHint: 'Номер и статус сохраняются. Увеличение суммы списывается, уменьшение возвращается.',
    adminEditUnavailable: 'Редактирование доступно только до подтверждения или выполнения. Обновите статус заказа.',
    renewModuleUnavailable: 'Исходная услуга недоступна или не может быть однозначно определена. Выберите услугу перед отправкой.',
    reservedOrder: 'Запланированный заказ',
    orderNotStarted: 'Время начала ещё не наступило. Уберите будущие заказы из пакетного запуска.',
    keywordOrderTimePast: 'Время заказа не может быть раньше текущего (UTC+8, точность до минуты).',
    specialItemPricingRequired: 'Для каждой позиции укажите цену за единицу больше 0 и целое число дней от 1 до 3650',
    chartType: 'Тип чарта',
    chartTypePlaceholder: 'Введите тип чарта',
    addChartType: 'Добавить тип чарта',
    reviewAttachments: "Файлы отзывов",
    reviewAttachmentHint: "Отзывы сохраняются во вложениях. Отображаются только имена файлов. XLSX или CSV, до 20 МБ.",
    reviewAttachmentLimit: "Выберите непустой XLSX или CSV до 20 МБ, не более 20 файлов на регион.",
    removeAttachment: "Удалить",

    payAndSubmit: 'Оплатить и отправить'
  }
}
