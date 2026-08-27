package com.erp.platform.config;

import com.erp.platform.modules.agri.entity.SeasonPeriod;
import com.erp.platform.modules.agri.repository.SeasonPeriodRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Sales Period Setup and Season Periods (Production Configuration) used to write into the same
 * season_periods table with nothing distinguishing one screen's rows from the other's, so each
 * screen showed the other's records too. The new periodType column fixes that going forward; this
 * classifies rows that predate the column. Only Season Periods ever populated seasonId (Sales
 * Period Setup has no season selector at all), so that's the signal: a row with a seasonId was
 * created from Production Configuration, everything else came from Sales Period Setup.
 */
@Component
@Order(11)
@RequiredArgsConstructor
@Slf4j
public class SeasonPeriodTypeBackfillSeeder implements CommandLineRunner {

    private final SeasonPeriodRepository seasonPeriodRepository;

    @Override
    public void run(String... args) {
        List<SeasonPeriod> untyped = seasonPeriodRepository.findByPeriodTypeIsNull();
        if (untyped.isEmpty()) return;
        for (SeasonPeriod sp : untyped) {
            sp.setPeriodType(sp.getSeasonId() != null ? "PRODUCTION" : "SALES");
        }
        seasonPeriodRepository.saveAll(untyped);
        log.info("Backfilled periodType on {} pre-existing season_periods rows", untyped.size());
    }
}
