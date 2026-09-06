import baseEn from './en-US'
import localizedOrders from './orders-page-pt'
const en = { ...baseEn, ordersPage: { ...baseEn.ordersPage, ...localizedOrders } }
import wallet from './wallet-pt'
import dynamicPromotion from './dynamic-promotion-pt'
import orderModulesAdmin from './order-modules-admin-pt'
import adminAccounts from './admin-accounts-pt'
import specialAudit from './special-audit-pt'
import orderDetail from './order-detail-pt'
import dashboard from './dashboard-pt'
import customers from './customers-pt'
import accountSettings from './account-settings-pt'
import applications from './applications-pt'
import home from './home-pt'
import generatedConfig from './generated-config-pt'
import generatedOrderCreate from './generated-order-create-pt'
import residual from './generated-residual-pt'
const messages = { ...en, ...generatedConfig, ...generatedOrderCreate, home, wallet, dynamicPromotion, orderModulesAdmin, specialAudit, orderDetail, dashboard, customers, accountSettings, applications, adminAccounts: { ...en.adminAccounts, ...adminAccounts },
  ordersPage:{...en.ordersPage,columnSettings:'Colunas',selectAllColumns:'Mostrar todas',restoreDefaultColumns:'Repor predefinições'},
  common:{...en.common,language:'Idioma',chinese:'中文',english:'English',russian:'Русский',portuguese:'Português',spanish:'Español',edit:'Editar',cancel:'Cancelar',save:'Guardar'},
  account:{unknown:'Conta',userFallback:'Utilizador {id}'},
  public:{nav:{aria:'Navegação pública',login:'Iniciar sessão',register:'Registar'}},
  auth:{...en.auth,loginTitle:'Iniciar sessão',loginSubtitle:'Aceda à sua conta para gerir aplicações e serviços.',registerTitle:'Criar conta',registerSubtitle:'Crie uma conta para começar a gerir as suas aplicações.',account:'Utilizador ou email',username:'Nome de utilizador',email:'Email',password:'Palavra-passe',loginButton:'Entrar',registerButton:'Criar conta',forgotPassword:'Esqueceu a palavra-passe?',logout:'Terminar sessão',requiredFields:'Preencha todos os campos obrigatórios',loginFailed:'Falha no início de sessão. Verifique os dados.'},
  menu:{...en.menu,home:'Início',promotion:'Serviços de promoção',applications:'Aplicações',orders:'Centro de encomendas',appleOrders:'Encomendas Apple',googleOrders:'Encomendas Google',ipadOrders:'Encomendas iPad',settings:'Definições da conta',customers:'Utilizadores',pendingReviewOrders:'A aguardar revisão',pendingConfirmOrders:'A aguardar confirmação',orderExecution:'Estado das encomendas',pendingExecutionOrders:'A aguardar execução',executingOrders:'Em execução',pausedOrders:'Pausadas',completedOrders:'Concluídas',auditManagement:'Revisão',finance:'Finanças',financeTransactions:'Movimentos financeiros',rechargeRecords:'Carregamentos',pricing:'Módulos de encomenda',walletTransactionTypeConfig:'Tipos de movimento',customerServiceConfig:'Apoio ao cliente',mailConfig:'Configuração de email',homeMetricsConfig:'Dados da página inicial',regions:'Regiões',systemManagement:'Sistema',adminAccounts:'Administradores',roles:'Permissões'},
  homeMetricsConfig:{title:'Dados da página inicial',subtitle:'Configure os quatro indicadores da página inicial pública.',valuePlaceholder:'Introduza o valor',updatedAt:'Atualizado em',previewTip:'As alterações aparecem na página inicial após guardar.',required:'Preencha todos os valores',saved:'Dados guardados',loadFailed:'Não foi possível carregar os dados',saveFailed:'Não foi possível guardar os dados'},
}

export default {
  ...messages,
  ...residual,
  ordersPage: {
    ...baseEn.ordersPage,
    ...messages.ordersPage,
    actionDetail: 'Detalhes', actionPaySubmit: 'Pagar', actionPayEdit: 'Pagar/editar', actionRenew: 'Renovar', actionConfirm: 'Confirmar', actionExecute: 'Executar', actionPause: 'Pausar', actionEditProgress: 'Progresso', actionResume: 'Retomar', actionReview: 'Rever', actionCancel: 'Cancelar', actionMore: 'Mais', appIdentifier: 'ID da aplicação', editOrder: 'Editar pedido', updateSuccess: 'Pedido atualizado', onlyUnconfirmedEditable: 'Só é possível editar pedidos não confirmados',
    types: { ...messages.ordersPage.types, DOWNLOAD: 'Transferências' },
    editSubmit: 'Editar dados',
    payOrEdit: 'Pagar / editar',
    payNow: 'Pagar agora',
    payConfirmTitle: 'Confirmar pagamento',
    payConfirmMessage: 'Debitar {amount} pela encomenda atual sem alterar os dados?',
    paySuccess: 'Pagamento concluído e encomenda enviada',
    payFailed: 'Falha no pagamento. Verifique o saldo e tente novamente'
  },
  orderCreate: {

    ...baseEn.orderCreate,
    ...messages.orderCreate,
    itemPricingBilling: 'O total é calculado somando o preço de cada item multiplicado pelos dias.', executionDays: 'Dias',
    chartType: 'Tipo de ranking',
    chartTypePlaceholder: 'Introduza o tipo de ranking',
    addChartType: 'Adicionar tipo de ranking',
    reviewAttachments: "Anexos de avaliações",
    reviewAttachmentHint: "As avaliações são guardadas como anexos. Apenas os nomes são mostrados. XLSX ou CSV, até 20 MB por ficheiro.",
    reviewAttachmentLimit: "Selecione XLSX ou CSV não vazio, até 20 MB e 20 anexos por região.",
    removeAttachment: "Remover",

    payAndSubmit: 'Pagar e enviar'
  }
}
