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
    itemPricingBilling: 'Итог рассчитывается как сумма цены каждой позиции, умноженной на количество дней.', executionDays: 'Дни',
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
