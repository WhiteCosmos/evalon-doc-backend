package me.ele.napos.evalon.gitlab

import com.alibaba.fastjson.JSON
import me.ele.napos.evalon.gitlab.exceptions.GitlabForbiddenException
import me.ele.napos.evalon.gitlab.exceptions.GitlabNotFoundException
import me.ele.napos.evalon.gitlab.exceptions.GitlabUnauthorizedException
import me.ele.napos.evalon.http.EvalonHttpClient
import org.apache.http.HttpResponse
import org.apache.http.util.EntityUtils

class GitlabApiV3 {
    static String GITLAB_URL = "Gitlab Url"

    static String GITLAB_TOKEN = "Gitlab Token"

    private static Map<Integer, Closure> GITLAB_ERROR_HANDLERS = [
            200: { HttpResponse response -> },
            201: { HttpResponse response -> },
            204: { HttpResponse response -> },
            304: { HttpResponse response ->
                if (response.statusLine.statusCode == 304) {
                    throw new GitlabUnauthorizedException(getGitlabErrorMessage(response))
                }
            },
            400: { HttpResponse response ->
                if (response.statusLine.statusCode == 400) {
                    throw new GitlabUnauthorizedException(getGitlabErrorMessage(response))
                }
            },
            401: { HttpResponse response ->
                if (response.statusLine.statusCode == 401) {
                    throw new GitlabUnauthorizedException(getGitlabErrorMessage(response))
                }
            },
            403: { HttpResponse response ->
                if (response.statusLine.statusCode == 403) {
                    throw new GitlabForbiddenException(getGitlabErrorMessage(response))
                }
            },
            404: { HttpResponse response ->
                if (response.statusLine.statusCode == 404) {
                    throw new GitlabNotFoundException(getGitlabErrorMessage(response))
                }
            },
    ]

    static String getGitlabErrorMessage(HttpResponse response) {
        return JSON.parse(EntityUtils.toString(response.entity))['message']
    }

    /*
    {
  "id": 3,
  "description": null,
  "default_branch": "master",
  "visibility": "private",
  "ssh_url_to_repo": "git@example.com:diaspora/diaspora-project-site.git",
  "http_url_to_repo": "http://example.com/diaspora/diaspora-project-site.git",
  "web_url": "http://example.com/diaspora/diaspora-project-site",
  "readme_url": "http://example.com/diaspora/diaspora-project-site/blob/master/README.md",
  "tag_list": [
    "example",
    "disapora project"
  ],
  "owner": {
    "id": 3,
    "name": "Diaspora",
    "created_at": "2013-09-30T13:46:02Z"
  },
  "name": "Diaspora Project Site",
  "name_with_namespace": "Diaspora / Diaspora Project Site",
  "path": "diaspora-project-site",
  "path_with_namespace": "diaspora/diaspora-project-site",
  "issues_enabled": true,
  "open_issues_count": 1,
  "merge_requests_enabled": true,
  "jobs_enabled": true,
  "wiki_enabled": true,
  "snippets_enabled": false,
  "resolve_outdated_diff_discussions": false,
  "container_registry_enabled": false,
  "created_at": "2013-09-30T13:46:02Z",
  "last_activity_at": "2013-09-30T13:46:02Z",
  "creator_id": 3,
  "namespace": {
    "id": 3,
    "name": "Diaspora",
    "path": "diaspora",
    "kind": "group",
    "full_path": "diaspora"
  },
  "import_status": "none",
  "import_error": null,
  "permissions": {
    "project_access": {
      "access_level": 10,
      "notification_level": 3
    },
    "group_access": {
      "access_level": 50,
      "notification_level": 3
    }
  },
  "archived": false,
  "avatar_url": "http://example.com/uploads/project/avatar/3/uploads/avatar.png",
  "shared_runners_enabled": true,
  "forks_count": 0,
  "star_count": 0,
  "runners_token": "b8bc4a7a29eb76ea83cf79e4908c2b",
  "public_jobs": true,
  "shared_with_groups": [
    {
      "group_id": 4,
      "group_name": "Twitter",
      "group_access_level": 30
    },
    {
      "group_id": 3,
      "group_name": "Gitlab Org",
      "group_access_level": 10
    }
  ],
  "repository_storage": "default",
  "only_allow_merge_if_pipeline_succeeds": false,
  "only_allow_merge_if_all_discussions_are_resolved": false,
  "printing_merge_requests_link_enabled": true,
  "request_access_enabled": false,
  "merge_method": "merge",
  "approvals_before_merge": 0,
  "statistics": {
    "commit_count": 37,
    "storage_size": 1038090,
    "repository_size": 1038090,
    "lfs_objects_size": 0,
    "job_artifacts_size": 0
  },
  "_links": {
    "self": "http://example.com/api/v4/projects",
    "issues": "http://example.com/api/v4/projects/1/issues",
    "merge_requests": "http://example.com/api/v4/projects/1/merge_requests",
    "repo_branches": "http://example.com/api/v4/projects/1/repository_branches",
    "labels": "http://example.com/api/v4/projects/1/labels",
    "events": "http://example.com/api/v4/projects/1/events",
    "members": "http://example.com/api/v4/projects/1/members"
  }
}
     */

//    static projects(String s, boolean simple, limit)

    static projects(String s, boolean simple, int page = 1, int per_page = 100) {
        def client = new EvalonHttpClient(GITLAB_URL, GITLAB_ERROR_HANDLERS)

        setToken(client, GITLAB_TOKEN)

        return client.projects().get([
                search  : s,
                simple  : simple,
                page    : page,
                per_page: per_page,
        ]).asJson()
    }


    static projects(Integer projectId) {
        def client = new EvalonHttpClient(GITLAB_URL, GITLAB_ERROR_HANDLERS)

        setToken(client, GITLAB_TOKEN)

        return client.projects(projectId).get().asJson()
    }

    static namespaces() {
        def client = new EvalonHttpClient(GITLAB_URL, GITLAB_ERROR_HANDLERS)

        setToken(client, GITLAB_TOKEN)

        return client.namespaces().get().asJson()
    }

    /*
    {
  "name": "master",
  "merged": false,
  "protected": true,
  "developers_can_push": false,
  "developers_can_merge": false,
  "can_push": true,
  "commit": {
    "author_email": "john@example.com",
    "author_name": "John Smith",
    "authored_date": "2012-06-27T05:51:39-07:00",
    "committed_date": "2012-06-28T03:44:20-07:00",
    "committer_email": "john@example.com",
    "committer_name": "John Smith",
    "id": "7b5c3cc8be40ee161ae89a06bba6229da1032a0c",
    "short_id": "7b5c3cc",
    "title": "add projects API",
    "message": "add projects API",
    "parent_ids": [
      "4ad91d3c1144c406e50c7b33bae684bd6837faf8"
    ]
  }
}
     */

    static branches(Integer projectId, String branch) {
        def client = new EvalonHttpClient(GITLAB_URL, GITLAB_ERROR_HANDLERS)

        setToken(client, GITLAB_TOKEN)

        return client.projects(projectId).repository().branches(URLEncoder.encode(branch)).get().asJson()
    }

    static create_branch(Integer projectId, String branch, String ref) {
        def client = new EvalonHttpClient(GITLAB_URL, GITLAB_ERROR_HANDLERS)

        setToken(client, GITLAB_TOKEN)

        return client.projects(projectId).repository().branches().post([
                branch_name: branch,
                ref        : ref
        ]).asJson()
    }

    static delete_branch(Integer projectId, String branch) {
        def client = new EvalonHttpClient(GITLAB_URL, GITLAB_ERROR_HANDLERS)

        setToken(client, GITLAB_TOKEN)

        return client.projects(projectId).repository().branches(URLEncoder.encode(branch)).delete().asJson()
    }

    static branches(Integer projectId, int page = 1, int per_page = 100) {
        def client = new EvalonHttpClient(GITLAB_URL, GITLAB_ERROR_HANDLERS)

        setToken(client, GITLAB_TOKEN)

        return client.projects(projectId).repository().branches().get([
                page    : page,
                per_page: per_page
        ]).asJson()
    }

    static commits(Integer projectId, String sha) {
        def client = new EvalonHttpClient(GITLAB_URL, GITLAB_ERROR_HANDLERS)

        setToken(client, GITLAB_TOKEN)

        return client.projects(projectId).repository().commits(sha).get().asJson()
    }

    static webhooks(Integer projectId) {
        def client = new EvalonHttpClient(GITLAB_URL, GITLAB_ERROR_HANDLERS)

        setToken(client, GITLAB_TOKEN)

        return client.projects(projectId).hooks().get().asJson()
    }

    static webhooks(Integer projectId, String webhook) {
        def client = new EvalonHttpClient(GITLAB_URL, GITLAB_ERROR_HANDLERS)

        setToken(client, GITLAB_TOKEN)

        return client.projects(projectId).hooks().post([
                url                       : webhook,
                push_events               : true,
                "tag_push_events"         : false,
                "merge_requests_events"   : false,
                "repository_update_events": false,
                "enable_ssl_verification" : true
        ]).asJson()
    }

    /*
[
  {
    "diff": "--- a/doc/update/5.4-to-6.0.md\n+++ b/doc/update/5.4-to-6.0.md\n@@ -71,6 +71,8 @@\n sudo -u git -H bundle exec rake migrate_keys RAILS_ENV=production\n sudo -u git -H bundle exec rake migrate_inline_notes RAILS_ENV=production\n \n+sudo -u git -H bundle exec rake gitlab:assets:compile RAILS_ENV=production\n+\n ```\n \n ### 6. Update config files",
    "new_path": "doc/update/5.4-to-6.0.md",
    "old_path": "doc/update/5.4-to-6.0.md",
    "a_mode": null,
    "b_mode": "100644",
    "new_file": false,
    "renamed_file": false,
    "deleted_file": false
  }
]
     */

    static diff(Integer projectId, String sha) {
        def client = new EvalonHttpClient(GITLAB_URL, GITLAB_ERROR_HANDLERS)

        setToken(client, GITLAB_TOKEN)

        return client.projects(projectId).repository().commits(sha).diff().get().asJson()
    }

    static query_commits(Integer projectId, String branch) {
        def client = new EvalonHttpClient(GITLAB_URL, GITLAB_ERROR_HANDLERS)

        setToken(client, GITLAB_TOKEN)

        return client.projects(projectId).repository().commits().get([
                ref_name: branch,
                page    : 0,
                per_page: 100
        ]).asJson()
    }

    static query_commits(Integer projectId, String branch, int page) {
        def client = new EvalonHttpClient(GITLAB_URL, GITLAB_ERROR_HANDLERS)

        setToken(client, GITLAB_TOKEN)

        return client.projects(projectId).repository().commits().get([
                ref_name: branch,
                page    : page,
                per_page: 100
        ]).asJson()
    }

    /*
    {
  "file_name": "key.rb",
  "file_path": "app/models/key.rb",
  "size": 1476,
  "encoding": "base64",
  "content": "IyA9PSBTY2hlbWEgSW5mb3...",
  "content_sha256": "4c294617b60715c1d218e61164a3abd4808a4284cbc30e6728a01ad9aada4481",
  "ref": "master",
  "blob_id": "79f7bbd25901e8334750839545a9bd021f0e4c83",
  "commit_id": "d5a3ff139356ce33e37e73add446f16869741b50",
  "last_commit_id": "570e7b2abdd848b95f2f578043fc23bd6f6fd24d"
}
     */

    static files(Integer projectId, String branch, String path) {
        def client = new EvalonHttpClient(GITLAB_URL, GITLAB_ERROR_HANDLERS)

        setToken(client, GITLAB_TOKEN)

        return client.projects(projectId).repository().files().get([
                file_path: path,
                ref      : branch
        ]).asJson()
    }

    static String filesAsContent(Integer projectId, String branch, String path) {
        def files = files(projectId, branch, path)

        return new String(Base64.decoder.decode(files['content'] as String))
    }

    static create_file(Integer projectId, String branch, String path, String content) {
        def client = new EvalonHttpClient(GITLAB_URL, GITLAB_ERROR_HANDLERS)

        setToken(client, GITLAB_TOKEN)

        return client.projects(projectId).repository().files().post([
                branch_name   : branch,
                file_path     : path,
                encoding      : "base64",
                content       : Base64.encoder.encodeToString(content.bytes),
                commit_message: "Create File ${path}".toString() // 转换为正常的String类型
        ]).asJson()
    }

    static update_file(Integer projectId, String branch, String path, String content) {
        def client = new EvalonHttpClient(GITLAB_URL, GITLAB_ERROR_HANDLERS)

        setToken(client, GITLAB_TOKEN)

        return client.projects(projectId).repository().files().put([
                branch_name   : branch,
                file_path     : path,
                content       : content,
                commit_message: "Update File ${path}".toString() // 转换为正常的String类型
        ]).asJson()
    }

    static delete_file(Integer projectId, String branch, String path, String content) {
        def client = new EvalonHttpClient(GITLAB_URL, GITLAB_ERROR_HANDLERS)

        setToken(client, GITLAB_TOKEN)

        return client.projects(projectId).repository().files().delete([
                branch_name   : branch,
                file_path     : path,
                content       : content,
                commit_message: "Update File ${path}".toString() // 转换为正常的String类型
        ]).asJson()
    }

    /*
    {
  "id": "ed899a2f4b50b4370feeea94676502b42383c746",
  "short_id": "ed899a2f4b5",
  "title": "some commit message",
  "author_name": "Dmitriy Zaporozhets",
  "author_email": "dzaporozhets@sphereconsultinginc.com",
  "committer_name": "Dmitriy Zaporozhets",
  "committer_email": "dzaporozhets@sphereconsultinginc.com",
  "created_at": "2016-09-20T09:26:24.000-07:00",
  "message": "some commit message",
  "parent_ids": [
    "ae1d9fb46aa2b07ee9836d49862ec4e2c46fbbba"
  ],
  "committed_date": "2016-09-20T09:26:24.000-07:00",
  "authored_date": "2016-09-20T09:26:24.000-07:00",
  "stats": {
    "additions": 2,
    "deletions": 2,
    "total": 4
  },
  "status": null
}
     */

    static commit(Integer projectId, String branch, List<GitlabAction> actions) {
        def client = new EvalonHttpClient(GITLAB_URL, GITLAB_ERROR_HANDLERS)

        setToken(client, GITLAB_TOKEN)

        return client.projects(projectId).repository().commits().post([
                branch        : branch,
                commit_message: "commit by LegacyGitlabApi",
                actions       : actions
        ]).asJson()
    }

    static class GitlabAction {
        String action = ""
    }

    static class GitlabCreateAction extends GitlabAction {
        String action = "create"
        String file_path = ""
        String content = ""
    }

    static class GitlabDeleteAction extends GitlabAction {
        String action = "delete"
        String file_path = ""
    }

    static class GitlabMoveAction extends GitlabAction {
        String action = "move"
        String file_path = ""
        String previous_path = ""
        String content = ""
    }

    static class GitlabUpdateAction extends GitlabAction {
        String action = "update"
        String file_path = ""
        String content = ""
    }

    static setToken(EvalonHttpClient client, String token) {
        client.addHeader("PRIVATE-TOKEN", token)
    }

    static setErrorHandlers(EvalonHttpClient client) {
        client.errorHandlers = GITLAB_ERROR_HANDLERS
    }
}
