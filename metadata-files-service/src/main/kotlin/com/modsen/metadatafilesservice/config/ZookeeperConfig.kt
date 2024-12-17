package com.modsen.metadatafilesservice.config

import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.retry.RetryNTimes
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ZookeeperConfig(
    @Value("\${zookeeper.url}") private val zookeeperUrl: String,
    @Value("\${zookeeper.max-retries}") private val maxRetries: Int,
    @Value("\${zookeeper.sleep-ms-between-retries}") private val sleepMsBetweenRetries: Int
) {
    @Bean
    fun curatorFramework(): CuratorFramework {
        val sleepMsBetweenRetries = sleepMsBetweenRetries
        val maxRetries = maxRetries
        val retryPolicy = RetryNTimes(
            maxRetries, sleepMsBetweenRetries
        )
        val client = CuratorFrameworkFactory.newClient(zookeeperUrl, retryPolicy)
        client.start()
        return client
    }
}