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
    adjustmentReason: "Motivo del cambio",
    adjustmentReasonPlaceholder: "Indique el motivo, por ejemplo la retirada de la aplicación de la tienda",
    adjustmentReasonRequired: "Indique el motivo del cambio",
    adjustmentReasonNotRecorded: "No se registró un motivo para este cambio anterior",
    editCompletedTitle: 'Modificar cantidad completada',
    editCompletedTip: 'Solo puede reducir cantidades completadas. La diferencia se devuelve al saldo del cliente al precio original. El pedido permanece completado.',
    completedQuantityInvalid: 'La cantidad debe ser un entero entre cero y la cantidad completada actual.',
    currentNetAmount: 'Importe neto actual',
    saveCompletedAdjustment: 'Guardar y reembolsar',
    closeOrder: 'Cerrar pedido',
    closePausedTip: 'Al cerrar, la cantidad indicada se marca como completada. Debe estar entre cero y la cantidad original; la reducción se reembolsa.',
    closeQuantityInvalid: 'Las cantidades deben ser enteros entre cero y la cantidad original de cada partida.',
    closeSuccess: 'Pedido cerrado',

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
    batchCoverageKeywordDialogTip: 'Introduce una palabra clave por línea, sin cantidad. Se ignoran las líneas vacías y las palabras clave repetidas no se añaden de nuevo.',
    batchCoverageKeywordPlaceholder: 'Ejemplo:\npalabra clave\nmi palabra clave',
    batchCoverageKeywordEmpty: 'Introduce al menos una palabra clave',
    batchCoverageKeywordInvalidRow: 'La palabra clave de la fila {row} no puede superar los 255 caracteres',
    itemPricingBilling: 'El total se calcula sumando el precio de cada elemento multiplicado por los días.', executionDays: 'Días',
    selectOrderDateTime: 'Selecciona la fecha y hora del pedido',
    cancelledOrderEditHint: 'Al reenviar un pedido cancelado se cobra de nuevo el importe actual y se requiere confirmación del administrador.',
    adminEditHint: 'Se conservan el número y el estado. Los aumentos se cobran y las reducciones se reembolsan.',
    adminEditUnavailable: 'Solo se pueden editar pedidos pendientes de confirmación o ejecución. Actualice el estado.',
    reservedOrder: 'Pedido programado',
    orderNotStarted: 'Aún no ha llegado la hora de inicio. Retire los pedidos futuros de la ejecución por lotes.',
    keywordOrderTimePast: 'La hora del pedido no puede ser anterior a la actual (UTC+8, precisión de minutos).',
    specialItemPricingRequired: 'Para cada elemento, introduce un precio unitario mayor que 0 y un número entero de días entre 1 y 3650',
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
