import baseEn from './en-US'
import localizedOrders from './orders-page-es'
const en = { ...baseEn, ordersPage: { ...baseEn.ordersPage, ...localizedOrders } }
import wallet from './wallet-es'
import dynamicPromotion from './dynamic-promotion-es'
import orderModulesAdmin from './order-modules-admin-es'
import adminAccounts from './admin-accounts-es'
import specialAudit from './special-audit-es'
import orderDetail from './order-detail-es'
import dashboard from './dashboard-es'
import customers from './customers-es'
import accountSettings from './account-settings-es'
import applications from './applications-es'
import home from './home-es'
import generatedCore from './generated-core-es'
import residual from './generated-residual-es'
const messages = { ...en, ...generatedCore, home, wallet, dynamicPromotion, orderModulesAdmin, specialAudit, orderDetail, dashboard, customers, accountSettings, applications, adminAccounts: { ...en.adminAccounts, ...adminAccounts },
  ordersPage:{...en.ordersPage,columnSettings:'Columnas',selectAllColumns:'Mostrar todas',restoreDefaultColumns:'Restaurar valores predeterminados'},
  common:{...en.common,language:'Idioma',chinese:'中文',english:'English',russian:'Русский',portuguese:'Português',spanish:'Español',edit:'Editar',cancel:'Cancelar',save:'Guardar'},
  account:{unknown:'Cuenta',userFallback:'Usuario {id}'},
  public:{nav:{aria:'Navegación pública',login:'Iniciar sesión',register:'Registrarse'}},
  auth:{...en.auth,loginTitle:'Iniciar sesión',loginSubtitle:'Accede a tu cuenta para gestionar aplicaciones y servicios.',registerTitle:'Crear cuenta',registerSubtitle:'Crea una cuenta para gestionar tus aplicaciones.',account:'Usuario o correo',username:'Nombre de usuario',email:'Correo electrónico',password:'Contraseña',loginButton:'Entrar',registerButton:'Crear cuenta',forgotPassword:'¿Olvidaste la contraseña?',logout:'Cerrar sesión',requiredFields:'Completa todos los campos obligatorios',loginFailed:'Error al iniciar sesión. Comprueba tus datos.'},
  menu:{...en.menu,home:'Inicio',promotion:'Servicios de promoción',applications:'Aplicaciones',orders:'Centro de pedidos',appleOrders:'Pedidos Apple',googleOrders:'Pedidos Google',ipadOrders:'Pedidos iPad',settings:'Configuración de cuenta',customers:'Usuarios',pendingReviewOrders:'Pendientes de revisión',pendingConfirmOrders:'Pendientes de confirmación',orderExecution:'Estado de pedidos',pendingExecutionOrders:'Pendientes de ejecución',executingOrders:'En ejecución',pausedOrders:'Pausados',completedOrders:'Completados',auditManagement:'Revisión',finance:'Finanzas',financeTransactions:'Movimientos financieros',rechargeRecords:'Recargas',pricing:'Módulos de pedido',walletTransactionTypeConfig:'Tipos de movimiento',customerServiceConfig:'Atención al cliente',mailConfig:'Configuración de correo',homeMetricsConfig:'Datos de inicio',regions:'Regiones',systemManagement:'Sistema',adminAccounts:'Administradores',roles:'Permisos'},
  homeMetricsConfig:{title:'Datos de la página de inicio',subtitle:'Configura los cuatro indicadores de la página pública.',valuePlaceholder:'Introduce el valor',updatedAt:'Actualizado el',previewTip:'Los cambios aparecerán en la página de inicio al guardar.',required:'Completa todos los valores',saved:'Datos guardados',loadFailed:'No se pudieron cargar los datos',saveFailed:'No se pudieron guardar los datos'},
}

export default {
  ...messages,
  ...residual,
  ordersPage: {
    ...baseEn.ordersPage,
    ...messages.ordersPage,
    actionDetail: 'Detalles', actionPaySubmit: 'Pagar', actionPayEdit: 'Pagar/editar', actionRenew: 'Renovar', actionConfirm: 'Confirmar', actionExecute: 'Ejecutar', actionPause: 'Pausar', actionEditProgress: 'Progreso', actionResume: 'Reanudar', actionReview: 'Revisar', actionCancel: 'Cancelar', actionMore: 'Más', appIdentifier: 'ID de aplicación', editOrder: 'Editar pedido', updateSuccess: 'Pedido actualizado', onlyUnconfirmedEditable: 'Solo se pueden editar pedidos sin confirmar',
    editSubmit: 'Editar datos',
    payOrEdit: 'Pagar / editar',
    payNow: 'Pagar ahora',
    payConfirmTitle: 'Confirmar pago',
    payConfirmMessage: '¿Cobrar {amount} por el pedido actual sin modificar sus datos?',
    paySuccess: 'Pago completado y pedido enviado',
    payFailed: 'Error en el pago. Comprueba el saldo e inténtalo de nuevo'
  },
  orderCreate: {

    ...baseEn.orderCreate,
    ...messages.orderCreate,
    itemPricingBilling: 'El total se calcula sumando el precio de cada elemento multiplicado por los días.', executionDays: 'Días',
    chartType: 'Tipo de lista',
    chartTypePlaceholder: 'Introduzca el tipo de lista',
    addChartType: 'Añadir tipo de lista',
    reviewAttachments: "Archivos de reseñas",
    reviewAttachmentHint: "Las reseñas se guardan como archivos. Solo se muestran los nombres. XLSX o CSV, hasta 20 MB por archivo.",
    reviewAttachmentLimit: "Seleccione XLSX o CSV no vacío, hasta 20 MB y 20 archivos por región.",
    removeAttachment: "Eliminar",

    payAndSubmit: 'Pagar y enviar'
  }
}
