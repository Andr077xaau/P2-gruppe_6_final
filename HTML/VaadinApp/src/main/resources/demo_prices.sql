-- demo electricity prices in dkk/kwh for each hour of the day (0 = 00:00-01:00, 17 = 17:00-18:00 osv.)
-- used to calculate price of every energy reading from the hour it was recorded
-- loaded into the electricity_prices table on every startup, so change prices here
INSERT OR REPLACE INTO electricity_prices (hour, price_per_kwh) VALUES (0, 1.45);
INSERT OR REPLACE INTO electricity_prices (hour, price_per_kwh) VALUES (1, 1.38);
INSERT OR REPLACE INTO electricity_prices (hour, price_per_kwh) VALUES (2, 1.32);
INSERT OR REPLACE INTO electricity_prices (hour, price_per_kwh) VALUES (3, 1.30);
INSERT OR REPLACE INTO electricity_prices (hour, price_per_kwh) VALUES (4, 1.33);
INSERT OR REPLACE INTO electricity_prices (hour, price_per_kwh) VALUES (5, 1.52);
INSERT OR REPLACE INTO electricity_prices (hour, price_per_kwh) VALUES (6, 2.05);
INSERT OR REPLACE INTO electricity_prices (hour, price_per_kwh) VALUES (7, 2.48);
INSERT OR REPLACE INTO electricity_prices (hour, price_per_kwh) VALUES (8, 2.55);
INSERT OR REPLACE INTO electricity_prices (hour, price_per_kwh) VALUES (9, 2.30);
INSERT OR REPLACE INTO electricity_prices (hour, price_per_kwh) VALUES (10, 2.10);
INSERT OR REPLACE INTO electricity_prices (hour, price_per_kwh) VALUES (11, 1.98);
INSERT OR REPLACE INTO electricity_prices (hour, price_per_kwh) VALUES (12, 1.90);
INSERT OR REPLACE INTO electricity_prices (hour, price_per_kwh) VALUES (13, 1.88);
INSERT OR REPLACE INTO electricity_prices (hour, price_per_kwh) VALUES (14, 1.95);
INSERT OR REPLACE INTO electricity_prices (hour, price_per_kwh) VALUES (15, 2.15);
INSERT OR REPLACE INTO electricity_prices (hour, price_per_kwh) VALUES (16, 2.60);
INSERT OR REPLACE INTO electricity_prices (hour, price_per_kwh) VALUES (17, 3.45);
INSERT OR REPLACE INTO electricity_prices (hour, price_per_kwh) VALUES (18, 3.70);
INSERT OR REPLACE INTO electricity_prices (hour, price_per_kwh) VALUES (19, 3.55);
INSERT OR REPLACE INTO electricity_prices (hour, price_per_kwh) VALUES (20, 3.10);
INSERT OR REPLACE INTO electricity_prices (hour, price_per_kwh) VALUES (21, 2.40);
INSERT OR REPLACE INTO electricity_prices (hour, price_per_kwh) VALUES (22, 1.95);
INSERT OR REPLACE INTO electricity_prices (hour, price_per_kwh) VALUES (23, 1.62);
