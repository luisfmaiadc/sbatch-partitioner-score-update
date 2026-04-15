SET SESSION cte_max_recursion_depth = 1000000;

INSERT INTO TbUsers (name, score, status, last_update)
WITH RECURSIVE seq AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 1000000
)
SELECT
    CONCAT('User_', n),
    ROUND(RAND() * 1000, 2),
    CASE
        WHEN RAND() < 0.33 THEN 'BRONZE'
        WHEN RAND() < 0.66 THEN 'SILVER'
        ELSE 'GOLD'
        END,
    NOW() - INTERVAL FLOOR(RAND() * 365) DAY
FROM seq;