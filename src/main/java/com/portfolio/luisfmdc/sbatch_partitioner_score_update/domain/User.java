package com.portfolio.luisfmdc.sbatch_partitioner_score_update.domain;

import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class User {

    private Long id;
    private String name;
    private Double score;
    private Status status;
    private LocalDateTime lastUpdate;

    public String getStatusName() {
        return this.status != null ? this.status.name() : null;
    }
}