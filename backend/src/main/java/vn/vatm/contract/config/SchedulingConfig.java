package vn.vatm.contract.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Enables scheduled jobs (e.g. the alert evaluation cron). */
@Configuration
@EnableScheduling
public class SchedulingConfig {}
