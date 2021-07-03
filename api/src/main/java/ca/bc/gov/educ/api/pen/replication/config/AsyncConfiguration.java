package ca.bc.gov.educ.api.pen.replication.config;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.val;
import org.jboss.threads.EnhancedQueueExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.time.Duration;
import java.util.concurrent.Executor;

/**
 * The type Async configuration.
 */
@Configuration
@EnableAsync
@Profile("!test")
public class AsyncConfiguration {
  /**
   * Thread pool task executor executor.
   *
   * @return the executor
   */
  @Bean(name = "subscriberExecutor")
  public Executor threadPoolTaskExecutor() {
    return new EnhancedQueueExecutor.Builder()
      .setThreadFactory(new ThreadFactoryBuilder().setNameFormat("message-subscriber-%d").build())
      .setCorePoolSize(5).setMaximumPoolSize(10).setKeepAliveTime(Duration.ofSeconds(60)).build();
  }

  /**
   * Controller task executor executor.
   *
   * @return the executor
   */
  @Bean(name = "taskExecutor")
  public Executor controllerTaskExecutor() {
    return new EnhancedQueueExecutor.Builder()
      .setThreadFactory(new ThreadFactoryBuilder().setNameFormat("async-executor-%d").build())
      .setCorePoolSize(5).setMaximumPoolSize(10).setKeepAliveTime(Duration.ofSeconds(60)).build();
  }

  /**
   * Thread pool task scheduler thread pool task scheduler.
   *
   * @return the thread pool task scheduler
   */
  @Bean
  public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
    val threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
    threadPoolTaskScheduler.setThreadNamePrefix("scheduled-task-executor");
    threadPoolTaskScheduler.setPoolSize(5);
    return threadPoolTaskScheduler;
  }
}
