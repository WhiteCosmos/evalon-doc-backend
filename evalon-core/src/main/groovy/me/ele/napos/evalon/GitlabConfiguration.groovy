package me.ele.napos.evalon

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "gitlab")
class GitlabConfiguration {
    String gitlabUrl

    String gitlabToken
}
