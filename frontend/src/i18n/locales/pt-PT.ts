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
    adjustmentReason: "Motivo da alteração",
    adjustmentReasonPlaceholder: "Indique o motivo, por exemplo a remoção da aplicação da loja",
    adjustmentReasonRequired: "Indique o motivo da alteração",
    adjustmentReasonNotRecorded: "Não foi registado um motivo para esta alteração anterior",
    editCompletedTitle: 'Alterar quantidade concluída',
    editCompletedTip: 'Só pode reduzir quantidades concluídas. A diferença é devolvida ao saldo do cliente pelo preço original. O pedido mantém-se concluído.',
    completedQuantityInvalid: 'A quantidade deve ser um inteiro entre zero e a quantidade concluída atual.',
    currentNetAmount: 'Valor líquido atual',
    saveCompletedAdjustment: 'Guardar e reembolsar',
    closeOrder: 'Concluir pedido',
    closePausedTip: 'Ao concluir, a quantidade indicada fica concluída. Deve estar entre zero e a quantidade original; a redução será reembolsada.',
    closeQuantityInvalid: 'As quantidades devem ser inteiros entre zero e a quantidade original de cada item.',
    closeSuccess: 'Pedido concluído',

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
    batchCoverageKeywordDialogTip: 'Introduza uma palavra-chave por linha, sem quantidade. As linhas vazias são ignoradas e as palavras-chave repetidas não são adicionadas novamente.',
    batchCoverageKeywordPlaceholder: 'Exemplo:\npalavra-chave\na minha palavra-chave',
    batchCoverageKeywordEmpty: 'Introduza pelo menos uma palavra-chave',
    batchCoverageKeywordInvalidRow: 'A palavra-chave na linha {row} não pode exceder 255 caracteres',
    itemPricingBilling: 'O total é calculado somando o preço de cada item multiplicado pelos dias.', executionDays: 'Dias',
    selectOrderDateTime: 'Selecione a data e hora do pedido',
    cancelledOrderEditHint: 'Ao reenviar um pedido cancelado, o valor atual será cobrado novamente e será necessária a confirmação do administrador.',
    adminEditHint: 'O número e o estado são mantidos. Os aumentos são cobrados e as reduções reembolsadas.',
    adminEditUnavailable: 'Só é possível editar pedidos pendentes de confirmação ou execução. Atualize o estado.',
    reservedOrder: 'Pedido agendado',
    orderNotStarted: 'A hora de início ainda não chegou. Remova os pedidos futuros da execução em lote.',
    keywordOrderTimePast: 'A hora do pedido não pode ser anterior à atual (UTC+8, precisão de minutos).',
    specialItemPricingRequired: 'Para cada item, introduza um preço unitário superior a 0 e um número inteiro de dias entre 1 e 3650',
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
