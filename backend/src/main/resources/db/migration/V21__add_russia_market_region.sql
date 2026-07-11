INSERT INTO market_region (
    code,
    name_zh,
    name_en,
    enabled,
    supports_app_store,
    supports_google_play,
    supports_ipad_store,
    sort_order
) VALUES
('RU', '俄罗斯', 'Russia', 1, 1, 1, 1, 500)
ON DUPLICATE KEY UPDATE
    name_zh = VALUES(name_zh),
    name_en = VALUES(name_en),
    enabled = VALUES(enabled),
    supports_app_store = VALUES(supports_app_store),
    supports_google_play = VALUES(supports_google_play),
    supports_ipad_store = VALUES(supports_ipad_store),
    sort_order = VALUES(sort_order);
