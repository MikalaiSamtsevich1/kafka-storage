package com.modsen.uploaderfilesservice.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebFluxConfig(
    @Value("\${kafka.message-size}") val chunkSize: Int,
    @Value("\${metadata-service.url}") val metadataServiceUrl: String
) : WebFluxConfigurer {
    override fun configureHttpMessageCodecs(configurer: ServerCodecConfigurer) =
        configurer.defaultCodecs().maxInMemorySize(chunkSize)

    @Bean
    fun webClient(): WebClient = WebClient.builder()
        .baseUrl(metadataServiceUrl)
        .build()

}