package me.ele.napos.evalon.gitlab.structs

import java.time.LocalDateTime

class GitlabWebHook {
    Integer id = 0
    String url = ""
    boolean push_events = false
    boolean tag_push_events = false
    boolean merge_requests_events = false
    boolean enable_ssl_verification = false
}
