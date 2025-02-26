
DROP PROCEDURE IF EXISTS SP_REGISTROS_POR_DISTANCIA;

DELIMITER $$

CREATE PROCEDURE SP_REGISTROS_POR_DISTANCIA(
    IN p_lat DOUBLE,
    IN p_long DOUBLE,
    IN p_distancia DOUBLE,
    IN p_paginacao INT,
    IN p_pagina INT
)
BEGIN

    -- Cálculo do offset
    DECLARE v_offset INT;
    SET v_offset = (p_pagina - 1) * p_paginacao;

    SELECT *,
           (SQRT(POW(latitude - p_lat, 2) + POW(longitude - p_long, 2))) * 100                AS distancia,
           ROUND((SQRT(POW(latitude - p_lat, 2) + POW(longitude - p_long, 2)) * 100 / 4)) * 4 AS distancia_arredondada
    FROM REGISTRO
    WHERE (SQRT(POW(latitude - p_lat, 2) + POW(longitude - p_long, 2))) * 100 <= p_distancia
    ORDER BY distancia_arredondada ASC, dt_criacao DESC
    LIMIT p_paginacao OFFSET v_offset;
END$$

DELIMITER ;

CALL SP_REGISTROS_POR_DISTANCIA(-27.66674533163343, -48.48246553064851, 1000, 100, 1);

DROP PROCEDURE SP_REGISTROS_POR_DISTANCIA;
