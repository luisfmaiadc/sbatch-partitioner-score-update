package com.portfolio.luisfmdc.sbatch_partitioner_score_update.config.partitioner;


import org.springframework.batch.core.partition.Partitioner;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Component
public class ColumnRangePartitioner implements Partitioner {

    private final JdbcTemplate jdbcTemplate;

    public ColumnRangePartitioner(@Qualifier("appDataSource") DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        Long min = jdbcTemplate.queryForObject("SELECT MIN(id) FROM TbUsers", Long.class);
        Long max = jdbcTemplate.queryForObject("SELECT MAX(id) FROM TbUsers", Long.class);

        if (min == null || max == null) {
            return new HashMap<>();
        }

        long targetSize = (max - min) / gridSize + 1;
        Map<String, ExecutionContext> result = new HashMap<>();

        long number = 0;
        long start = min;
        long end = start + targetSize - 1;

        while (start <= max) {
            ExecutionContext value = new ExecutionContext();
            result.put("partition" + number, value);

            if (end >= max) {
                end = max;
            }

            value.putLong("minValue", start);
            value.putLong("maxValue", end);

            start += targetSize;
            end += targetSize;
            number++;
        }

        return result;
    }
}