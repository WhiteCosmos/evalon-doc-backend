package me.ele.napos.evalon.gitlab.structs

import java.time.LocalDateTime

class GitlabCommit {
    String author_email
    String author_name
    LocalDateTime authored_date
    LocalDateTime committed_date
    String committer_email
    String committer_name
    String id
    String short_id
    String title
    String message
    List<String> parent_ids = []
}
