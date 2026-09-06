ALTER TABLE order_module_config
    ADD COLUMN module_description VARCHAR(500) NOT NULL DEFAULT '' AFTER module_name_es,
    ADD COLUMN module_description_en VARCHAR(500) NOT NULL DEFAULT '' AFTER module_description,
    ADD COLUMN module_description_ru VARCHAR(500) NOT NULL DEFAULT '' AFTER module_description_en,
    ADD COLUMN module_description_pt VARCHAR(500) NOT NULL DEFAULT '' AFTER module_description_ru,
    ADD COLUMN module_description_es VARCHAR(500) NOT NULL DEFAULT '' AFTER module_description_pt;

UPDATE order_module_config SET
    module_description = CASE order_type
        WHEN 'KEYWORD_INSTALL' THEN '按关键词和地区配置安装任务，提升目标关键词下的自然表现。'
        WHEN 'DOWNLOAD' THEN '按日期、地区和下载量创建订单，增强应用市场基础热度。'
        WHEN 'RATING' THEN '配置五星和四星评分数量，提升应用的整体评分表现。'
        WHEN 'REVIEW' THEN '配置五星和四星评论数量，增加真实自然的用户反馈。'
        WHEN 'RANK_GUARANTEE' THEN '提交关键词排名目标，由管理员审核并确认执行方案。'
        WHEN 'CHART_RANK_GUARANTEE' THEN '填写榜单目标与排名要求，由管理员审核并确认执行方案。'
        WHEN 'KEYWORD_COVERAGE' THEN '扩展关键词覆盖范围，提升应用在更多搜索词下的可见度。'
        ELSE module_name
    END,
    module_description_en = CASE order_type
        WHEN 'KEYWORD_INSTALL' THEN 'Configure installs by keyword and region to improve organic keyword performance.'
        WHEN 'DOWNLOAD' THEN 'Create download campaigns by date and region to strengthen app market activity.'
        WHEN 'RATING' THEN 'Configure five-star and four-star ratings to improve the overall app rating.'
        WHEN 'REVIEW' THEN 'Configure five-star and four-star reviews to build natural user feedback.'
        WHEN 'RANK_GUARANTEE' THEN 'Submit a keyword ranking target for admin review and execution planning.'
        WHEN 'CHART_RANK_GUARANTEE' THEN 'Set a chart target and ranking requirement for admin review and planning.'
        WHEN 'KEYWORD_COVERAGE' THEN 'Expand keyword coverage to increase visibility across more search terms.'
        ELSE module_name_en
    END,
    module_description_ru = CASE order_type
        WHEN 'KEYWORD_INSTALL' THEN 'Настройте установки по ключевым словам и регионам для улучшения органических позиций.'
        WHEN 'DOWNLOAD' THEN 'Создавайте кампании загрузок по датам и регионам для роста активности приложения.'
        WHEN 'RATING' THEN 'Настройте количество оценок 5 и 4 звезды для повышения общего рейтинга приложения.'
        WHEN 'REVIEW' THEN 'Настройте отзывы на 5 и 4 звезды для формирования естественной обратной связи.'
        WHEN 'RANK_GUARANTEE' THEN 'Укажите целевую позицию ключевого слова для проверки и планирования администратором.'
        WHEN 'CHART_RANK_GUARANTEE' THEN 'Укажите цель и позицию в чарте для проверки и планирования администратором.'
        WHEN 'KEYWORD_COVERAGE' THEN 'Расширьте охват ключевых слов и видимость приложения в поиске.'
        ELSE module_name_ru
    END,
    module_description_pt = CASE order_type
        WHEN 'KEYWORD_INSTALL' THEN 'Configure instalações por palavra-chave e região para melhorar o desempenho orgânico.'
        WHEN 'DOWNLOAD' THEN 'Crie campanhas de downloads por data e região para reforçar a atividade da aplicação.'
        WHEN 'RATING' THEN 'Configure classificações de cinco e quatro estrelas para melhorar a avaliação geral.'
        WHEN 'REVIEW' THEN 'Configure avaliações de cinco e quatro estrelas para criar feedback natural.'
        WHEN 'RANK_GUARANTEE' THEN 'Defina uma posição-alvo da palavra-chave para revisão e planeamento do administrador.'
        WHEN 'CHART_RANK_GUARANTEE' THEN 'Defina o objetivo e a posição no ranking para revisão e planeamento.'
        WHEN 'KEYWORD_COVERAGE' THEN 'Amplie a cobertura de palavras-chave e a visibilidade nas pesquisas.'
        ELSE module_name_pt
    END,
    module_description_es = CASE order_type
        WHEN 'KEYWORD_INSTALL' THEN 'Configura instalaciones por palabra clave y región para mejorar el rendimiento orgánico.'
        WHEN 'DOWNLOAD' THEN 'Crea campañas de descargas por fecha y región para reforzar la actividad de la aplicación.'
        WHEN 'RATING' THEN 'Configura calificaciones de cinco y cuatro estrellas para mejorar la valoración general.'
        WHEN 'REVIEW' THEN 'Configura reseñas de cinco y cuatro estrellas para generar comentarios naturales.'
        WHEN 'RANK_GUARANTEE' THEN 'Define una posición objetivo de palabra clave para revisión y planificación.'
        WHEN 'CHART_RANK_GUARANTEE' THEN 'Define el objetivo y la posición en el ranking para revisión y planificación.'
        WHEN 'KEYWORD_COVERAGE' THEN 'Amplía la cobertura de palabras clave y la visibilidad en las búsquedas.'
        ELSE module_name_es
    END;
