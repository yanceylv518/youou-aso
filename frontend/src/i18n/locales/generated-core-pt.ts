const messages = {
  "pricing": {
    "eyebrow": "Preços globais",
    "title": "Encomendar Módulos",
    "subtitle": "Defina preços para serviços de promoção.",
    "notice": "Os preços não podem ser negativos. Os pedidos existentes devem usar o instantâneo de preços capturado quando o pedido foi criado.",
    "otherRegionPrice": "Preço em outra região",
    "chinaRegionPrice": "Preço na região da China",
    "save": "Salvar preços",
    "saved": "Preços salvos",
    "loadFailed": "Falha ao carregar preços",
    "saveFailed": "Falha ao salvar o preço",
    "items": {
      "KEYWORD_INSTALL": {
        "title": "Preço unitário de instalação de palavra-chave",
        "description": "Cobrado pela contagem de detalhes de palavras-chave."
      },
      "DOWNLOAD": {
        "title": "Baixar preço unitário",
        "description": "Cobrado por dias e total de downloads."
      },
      "RATING_5": {
        "title": "Preço unitário de classificação de 5 estrelas",
        "description": "Usado para pedidos de classificação de 5 estrelas."
      },
      "RATING_4": {
        "title": "Preço unitário de classificação de 4 estrelas",
        "description": "Usado para pedidos de classificação de 4 estrelas."
      },
      "REVIEW_5": {
        "title": "Preço unitário de avaliação de 5 estrelas",
        "description": "Usado para pedidos de revisão de 5 estrelas."
      },
      "REVIEW_4": {
        "title": "Preço unitário de avaliação de 4 estrelas",
        "description": "Usado para pedidos de revisão de 4 estrelas."
      }
    }
  },
  "supportConfig": {
    "title": "Configuração de atendimento ao cliente",
    "subtitle": "Defina o código QR de suporte e os detalhes de contato mostrados durante a recarga.",
    "defaultServiceName": "Youou-ASO Support",
    "defaultContactHint": "Digitalize o código QR para entrar em contato com o atendimento ao cliente para recarga.",
    "serviceName": "Nome do serviço",
    "qrCodeUrl": "URL da imagem QR",
    "qrCodeUrlPlaceholder": "Insira um URL de imagem http/https",
    "contactHint": "Instruções de contato",
    "contactHintPlaceholder": "Exemplo: leia o código QR para entrar em contato com o atendimento ao cliente para recarga. Atualizações de saldo após confirmação.",
    "enabled": "Habilitado",
    "disabled": "Desabilitado",
    "preview": "Visualização do usuário",
    "updatedAt": "Atualizado em",
    "save": "Salvar configuração",
    "saved": "Configuração de atendimento ao cliente salva",
    "loadFailed": "Falha ao carregar a configuração do atendimento ao cliente",
    "saveFailed": "Falha ao salvar a configuração do atendimento ao cliente",
    "serviceNameRequired": "Insira um nome de serviço",
    "qrCodeRequired": "Insira um URL de imagem QR antes de ativar a configuração do atendimento ao cliente"
  },
  "mailConfig": {
    "title": "Configuração de correio",
    "subtitle": "Defina a caixa de correio do remetente e os destinatários para notificações de pedidos.",
    "notice": "Os alertas de pedidos por e-mail correspondem ao sistema legado: eles notificam a equipe de operações, não o cliente que fez o pedido. Deixe a senha em branco para manter a senha atual.",
    "smtpSection": "Configuração SMTP",
    "smtpHost": "Anfitrião SMTP",
    "smtpHostPlaceholder": "por exemplo smtp.gmail.com",
    "smtpPort": "Porta",
    "username": "Nome de usuário do e-mail",
    "password": "Senha de e-mail/senha do aplicativo",
    "passwordConfigured": "Senha de e-mail/senha do aplicativo (configurada)",
    "passwordKeepPlaceholder": "Deixe em branco para manter a senha atual",
    "fromAddress": "Do endereço",
    "fromAddressPlaceholder": "O padrão é o nome de usuário do email",
    "smtpAuth": "Autenticação SMTP",
    "startTlsEnabled": "STARTTLS",
    "sslEnabled": "SSL",
    "orderNotificationSection": "Solicitar alertas por e-mail",
    "orderNotificationRecipients": "Destinatários de alerta de pedido",
    "recipientsPlaceholder": "Um email por linha ou emails separados com vírgulas ou ponto e vírgula",
    "enabled": "Habilitado",
    "disabled": "Desabilitado",
    "preview": "Estado atual",
    "smtpStatus": "SMTP",
    "passwordStatus": "Senha",
    "configured": "Configurado",
    "notConfigured": "Não configurado",
    "usingFallback": "Usando configuração de ambiente",
    "noRecipients": "Nenhum destinatário",
    "updatedAt": "Atualizado em",
    "save": "Salvar configuração",
    "saved": "Configuração de e-mail salva",
    "loadFailed": "Falha ao carregar configuração de e-mail",
    "saveFailed": "Falha ao salvar a configuração do e-mail",
    "recipientsRequired": "Insira os e-mails dos destinatários antes de ativar os alertas",
    "invalidEmail": "Insira endereços de e-mail válidos"
  },
  "walletTypeConfig": {
    "subtitle": "Ajuste os nomes mostrados para os tipos de transação da carteira.",
    "code": "Código do tipo de transação",
    "displayNameZh": "Nome de exibição chinês",
    "displayNameEn": "Nome de exibição em inglês",
    "displayNameZhPlaceholder": "Insira o nome de exibição em chinês",
    "displayNameEnPlaceholder": "Digite o nome de exibição em inglês",
    "localePlaceholder": "Insira o nome de exibição {language}",
    "updatedAt": "Atualizado em",
    "save": "Salvar configuração",
    "saved": "Configuração do tipo de transação salva",
    "required": "Preencha os nomes de exibição em chinês e inglês",
    "loadFailed": "Falha ao carregar a configuração do tipo de transação",
    "saveFailed": "Falha ao salvar a configuração do tipo de transação",
    "empty": "Nenhuma configuração de tipo de transação"
  },
  "regions": {
    "eyebrow": "Regiões de mercado",
    "subtitle": "Gerencie regiões disponíveis e cobertura de loja.",
    "notice": "Desativar o suporte de uma região …2673 tokens truncated… обзор цена за единицу",
    "description": "Используется для заказов на обзор с 5 звездами."
  },
  "REVIEW_4": {
    "title": "4-звездочный обзор цена за единицу",
    "description": "Используется для заказов на обзор с 4 звездами."
  }
} as const

export default messages
