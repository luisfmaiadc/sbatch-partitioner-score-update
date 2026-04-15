package com.portfolio.luisfmdc.sbatch_partitioner_score_update.config.job;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.parameters.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JobConfig {

    @Bean
    public Job scoreUpdateJob(JobRepository jobRepository, @Qualifier("scoreUpdateStepManager") Step scoreUpdateStepManager) {
        return new JobBuilder("scoreUpdateJob", jobRepository)
            .start(scoreUpdateStepManager)
            .incrementer(new RunIdIncrementer())
            .build();
    }
}