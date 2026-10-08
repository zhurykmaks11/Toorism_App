-- Тестові дані, щоб після запуску одразу було що показати
INSERT INTO trips (title, description, start_date, end_date, budget, status) VALUES
    ('Карпати', 'Похід на Говерлу',     '2026-07-10', '2026-07-15', 8000.00,  'COMPLETED'),
    ('Львів',   'Кава і старе місто',   '2026-09-01', '2026-09-03', 5000.00,  'COMPLETED'),
    ('Одеса',   'Море і Привоз',        '2026-11-05', '2026-11-12', 12000.00, 'PLANNED');

INSERT INTO destinations (city, country, arrival_date, departure_date, trip_id) VALUES
    ('Ворохта',   'Україна', '2026-07-10', '2026-07-13', 1),
    ('Яремче',    'Україна', '2026-07-13', '2026-07-15', 1),
    ('Львів',     'Україна', '2026-09-01', '2026-09-03', 2),
    ('Одеса',     'Україна', '2026-11-05', '2026-11-12', 3);

INSERT INTO places (name, category, planned_date, cost, visited, rating, destination_id) VALUES
    ('Говерла',                  'NATURE',   '2026-07-11', 0,      TRUE,  5,    1),
    ('Водоспад Пробій',          'NATURE',   '2026-07-14', 50,     TRUE,  4,    2),
    ('Площа Ринок',              'LANDMARK', '2026-09-01', 0,      TRUE,  5,    3),
    ('Львівська копальня кави',  'FOOD',     '2026-09-02', 300,    TRUE,  4,    3),
    ('Оперний театр',            'LANDMARK', '2026-11-06', 600,    FALSE, NULL, 4),
    ('Привоз',                   'FOOD',     '2026-11-07', 500,    FALSE, NULL, 4);
