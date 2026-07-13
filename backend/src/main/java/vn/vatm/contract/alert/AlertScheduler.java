package vn.vatm.contract.alert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Periodically evaluates alert conditions (FR-016). Cron is configurable; default hourly. */
@Component
public class AlertScheduler {

  private static final Logger log = LoggerFactory.getLogger(AlertScheduler.class);

  private final AlertEvaluationService evaluationService;

  public AlertScheduler(AlertEvaluationService evaluationService) {
    this.evaluationService = evaluationService;
  }

  @Scheduled(cron = "${app.alert.cron:0 0 * * * *}")
  public void run() {
    int raised = evaluationService.evaluate();
    if (raised > 0) {
      log.info("Alert evaluation raised {} new alert(s)", raised);
    }
  }
}
