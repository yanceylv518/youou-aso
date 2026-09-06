ALTER TABLE order_module_config
 ADD COLUMN module_name_en VARCHAR(100) NOT NULL DEFAULT '' AFTER module_name,
 ADD COLUMN module_name_ru VARCHAR(100) NOT NULL DEFAULT '' AFTER module_name_en,
 ADD COLUMN module_name_pt VARCHAR(100) NOT NULL DEFAULT '' AFTER module_name_ru,
 ADD COLUMN module_name_es VARCHAR(100) NOT NULL DEFAULT '' AFTER module_name_pt;

UPDATE order_module_config SET
 module_name_en = CASE order_type WHEN 'KEYWORD_INSTALL' THEN 'Keyword installs' WHEN 'DOWNLOAD' THEN 'Downloads' WHEN 'RATING' THEN 'Ratings' WHEN 'REVIEW' THEN 'Reviews' WHEN 'RANK_GUARANTEE' THEN 'Keyword ranking guarantee' WHEN 'CHART_RANK_GUARANTEE' THEN 'Chart ranking guarantee' WHEN 'KEYWORD_COVERAGE' THEN 'Keyword coverage' ELSE module_name END,
 module_name_ru = CASE order_type WHEN 'KEYWORD_INSTALL' THEN 'Установка по ключевым словам' WHEN 'DOWNLOAD' THEN 'Загрузки' WHEN 'RATING' THEN 'Оценки' WHEN 'REVIEW' THEN 'Отзывы' WHEN 'RANK_GUARANTEE' THEN 'Удержание позиции' WHEN 'CHART_RANK_GUARANTEE' THEN 'Удержание в рейтинге' WHEN 'KEYWORD_COVERAGE' THEN 'Покрытие ключевых слов' ELSE module_name END,
 module_name_pt = CASE order_type WHEN 'KEYWORD_INSTALL' THEN 'Instalação por palavra-chave' WHEN 'DOWNLOAD' THEN 'Downloads' WHEN 'RATING' THEN 'Classificações' WHEN 'REVIEW' THEN 'Avaliações' WHEN 'RANK_GUARANTEE' THEN 'Manter posição' WHEN 'CHART_RANK_GUARANTEE' THEN 'Manter posição no ranking' WHEN 'KEYWORD_COVERAGE' THEN 'Cobertura de palavras-chave' ELSE module_name END,
 module_name_es = CASE order_type WHEN 'KEYWORD_INSTALL' THEN 'Instalación por palabra clave' WHEN 'DOWNLOAD' THEN 'Descargas' WHEN 'RATING' THEN 'Calificaciones' WHEN 'REVIEW' THEN 'Reseñas' WHEN 'RANK_GUARANTEE' THEN 'Mantener posición' WHEN 'CHART_RANK_GUARANTEE' THEN 'Mantener posición en ranking' WHEN 'KEYWORD_COVERAGE' THEN 'Cobertura de palabras clave' ELSE module_name END;
