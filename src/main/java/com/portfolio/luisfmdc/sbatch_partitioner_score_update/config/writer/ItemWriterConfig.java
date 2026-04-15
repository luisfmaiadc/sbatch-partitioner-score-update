package com.portfolio.luisfmdc.sbatch_partitioner_score_update.config.writer;

import com.portfolio.luisfmdc.sbatch_partitioner_score_update.domain.User;
import org.springframework.batch.infrastructure.item.database.JdbcBatchItemWriter;
import org.springframework.batch.infrastructure.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class ItemWriterConfig {

    private static final String UPDATE_CLAUSE = "UPDATE TbUsers SET score = :score, status = :statusName, last_update = :lastUpdate WHERE id = :id";

    @Bean
    public JdbcBatchItemWriter<User> userScoreUpdateWriter(@Qualifier("appDataSource") DataSource appDataSource) {
        return  new JdbcBatchItemWriterBuilder<User>()
                .dataSource(appDataSource)
                .sql(UPDATE_CLAUSE)
                .beanMapped()
                .build();
    }
}