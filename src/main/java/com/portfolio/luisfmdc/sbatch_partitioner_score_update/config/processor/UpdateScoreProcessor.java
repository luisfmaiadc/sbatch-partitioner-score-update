package com.portfolio.luisfmdc.sbatch_partitioner_score_update.config.processor;

import com.portfolio.luisfmdc.sbatch_partitioner_score_update.domain.Status;
import com.portfolio.luisfmdc.sbatch_partitioner_score_update.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;

@Slf4j
@Component
public class UpdateScoreProcessor implements ItemProcessor<User, User> {

    @Override
    public User process(@NonNull User item) {
        Double originalScore = item.getScore();
        Status originalStatus = item.getStatus();

        penanlizarInatividade(item);
        aplicarBonusPorDesempenho(item);
        atualizarScore(item);

        boolean isScoreChanged = !Objects.equals(originalScore, item.getScore());
        boolean isStatusChanged = originalStatus != item.getStatus();

        if (!isScoreChanged && !isStatusChanged) {
            return null; 
        }

        item.setLastUpdate(LocalDateTime.now());
        return item;
    }

    private void penanlizarInatividade(User user) {
        if (user.getLastUpdate() != null && user.getLastUpdate().isBefore(LocalDateTime.now().minusDays(180))) {
            log.info("[UpdateScoreProcessor] Aplicando penalidade de 15% por inatividade para o usuário id={}", user.getId());
            user.setScore(user.getScore() * 0.85);
        }
    }

    private void aplicarBonusPorDesempenho(User user) {
        if (user.getScore() > 800) {
            log.info("[UpdateScoreProcessor] Aplicando bônus de 5% por desempenho para o usuário id={}", user.getId());
            user.setScore(user.getScore() * 1.05);
        }
    }

    private void atualizarScore(User user) {
        if (user.getScore() < 300) {
            user.setStatus(Status.BRONZE);
        } else if (user.getScore() <= 700) {
            user.setStatus(Status.SILVER);
        } else {
            user.setStatus(Status.GOLD);
        }
    }
}