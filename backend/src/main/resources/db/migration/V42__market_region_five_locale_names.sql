ALTER TABLE market_region ADD COLUMN name_ru VARCHAR(120) NOT NULL DEFAULT '' AFTER name_en, ADD COLUMN name_pt VARCHAR(120) NOT NULL DEFAULT '' AFTER name_ru, ADD COLUMN name_es VARCHAR(120) NOT NULL DEFAULT '' AFTER name_pt;
UPDATE market_region SET name_ru=name_en, name_pt=name_en, name_es=name_en;
