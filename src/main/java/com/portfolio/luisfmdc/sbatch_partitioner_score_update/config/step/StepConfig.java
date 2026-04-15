package com.portfolio.luisfmdc.sbatch_partitioner_score_update.config.step;

import com.portfolio.luisfmdc.sbatch_partitioner_score_update.domain.User;
import org.springframework.batch.core.partition.Partitioner;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;

@Configuration
public class StepConfig {

    @Value("${app.batch.chunk-size}")
    private Integer chunkSize;

    @Value("${app.batch.thread-usage}")
    private Integer batchThreadUsage;

    @Bean
    public Step scoreUpdateStepManager(JobRepository jobRepository,
                                       Partitioner partitioner,
                                       TaskExecutor taskExecutor,
                                       Step scoreUpdateStep) {
        return new StepBuilder("scoreUpdateStepManager", jobRepository)
                .partitioner("scoreUpdateStep.manager", partitioner)
                .taskExecutor(taskExecutor)
                .gridSize(batchThreadUsage)
                .step(scoreUpdateStep)
                .build();
    }

    @Bean
    public Step scoreUpdateStep(JobRepository jobRepository,
                                ItemReader<User> userScoreUpdateReader,
                                ItemProcessor<User, User> updateScoreProcessor,
                                ItemWriter<User> userScoreUpdateWriter) {
        return new StepBuilder("scoreUpdateStep", jobRepository)
                .<User, User>chunk(chunkSize)
                .reader(userScoreUpdateReader)
                .processor(updateScoreProcessor)
                .writer(userScoreUpdateWriter)
                .build();
    }
}