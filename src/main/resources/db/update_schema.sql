-- Добавляем столбец hourly_rate в таблицу projects, если его еще нет
ALTER TABLE projects ADD COLUMN hourly_rate REAL DEFAULT 0; 