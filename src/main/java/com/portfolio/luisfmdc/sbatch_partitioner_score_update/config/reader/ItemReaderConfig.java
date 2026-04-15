package com.portfolio.luisfmdc.sbatch_partitioner_score_update.config.reader;

import com.portfolio.luisfmdc.sbatch_partitioner_score_update.domain.Status;
import com.portfolio.luisfmdc.sbatch_partitioner_score_update.domain.User;
import org.springframework.batch.infrastructure.item.database.JdbcPagingItemReader;
import org.springframework.batch.infrastructure.item.database.PagingQueryProvider;
import org.springframework.batch.infrastructure.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.infrastructure.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class ItemReaderConfig {

    private static final String SELECT_CLAUSE = "SELECT *";
    private static final String FROM_CLAUSE = "FROM TbUsers";
    private static final String SORT_KEY = "id";

    @Value("${app.batch.chunk-size}")
    private Integer chunkSize;

    @Bean
    @StepScope
    public JdbcPagingItemReader<User> userScoreUpdateReader(
            @Qualifier("appDataSource") DataSource appDataSource,
            PagingQueryProvider queryProvider,
            @Value("#{stepExecutionContext['minValue']}") Long minValue,
            @Value("#{stepExecutionContext['maxValue']}") Long maxValue) throws Exception {

        Map<String, Object> parameterValues = new HashMap<>();
        parameterValues.put("minValue", minValue);
        parameterValues.put("maxValue", maxValue);

        return new JdbcPagingItemReaderBuilder<User>()
                .name("userScoreUpdateReader")
                .dataSource(appDataSource)
                .queryProvider(queryProvider)
                .parameterValues(parameterValues)
                .rowMapper(userRowMapper())
                .pageSize(chunkSize)
                .build();
    }

    @Bean
    public SqlPagingQueryProviderFactoryBean queryProvider(@Qualifier("appDataSource") DataSource appDataSource) throws Exception {
        SqlPagingQueryProviderFactoryBean queryProvider = new SqlPagingQueryProviderFactoryBean();
        queryProvider.setDataSource(appDataSource);
        queryProvider.setSelectClause(SELECT_CLAUSE);
        queryProvider.setFromClause(FROM_CLAUSE);
        queryProvider.setWhereClause("WHERE id >= :minValue AND id <= :maxValue");
        queryProvider.setSortKey(SORT_KEY);
        return queryProvider;
    }

    
    private RowMapper<User> userRowMapper() {
        return (rs, _) -> {
            User user = new User();
            user.setId(rs.getLong("id"));
            user.setName(rs.getString("name"));
            user.setScore(rs.getDouble("score"));
            
            String status = rs.getString("status");
            if (status != null) {
                user.setStatus(Status.valueOf(status.toUpperCase()));
            }

            Timestamp lastUpdate = rs.getTimestamp("last_update");
            if (lastUpdate != null) {
                user.setLastUpdate(lastUpdate.toLocalDateTime());
            }

            return user;
        };
    }
}