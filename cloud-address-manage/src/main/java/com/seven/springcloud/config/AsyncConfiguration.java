package com.seven.springcloud.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync  // 启用异步任务
public class AsyncConfiguration implements AsyncConfigurer {
    // 声明一个线程池(并指定线程池的名字)
    @Bean("AsyncTask")
    public ThreadPoolTaskExecutor asyncExecutor() {
        //创建线程池
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //核心线程数：线程池创建时候初始化准备就绪的线程数，
        //作用：等待来接受异步任务，一直存在不会被释放
        int corePoolSize = 10;
        executor.setCorePoolSize(corePoolSize);

       //线程池所使用的缓冲队列
        executor.setQueueCapacity(100);
        //等待任务在关机时完成--表明等待所有线程执行完
        executor.setWaitForTasksToCompleteOnShutdown(true);

        //最大线程数：线程池最大的线程数，只有在缓冲队列满了之后才会申请超过核心线程数的线程
        //作用：控制资源并发
        executor.setMaxPoolSize(corePoolSize*5);

        //允许线程的空闲时间：超过了核心线程出之外的线程，在空闲时间到达之后会被释放
        executor.setKeepAliveSeconds(30);

        //线程池名的前缀：设置好了之后可以方便我们定位处理任务所在的线程池
        executor.setThreadNamePrefix("Async-");
        executor.initialize();
        return executor;
    }
}

