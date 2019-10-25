package me.ele.napos.evalon.gitlab

import me.ele.napos.evalon.exceptions.OperationFailedException
import me.ele.napos.evalon.gitlab.structs.GitlabBranch
import me.ele.napos.evalon.gitlab.structs.GitlabBranchQuery
import me.ele.napos.evalon.gitlab.structs.GitlabCommit
import me.ele.napos.evalon.gitlab.structs.GitlabProject
import me.ele.napos.evalon.gitlab.structs.GitlabWebHook
import me.ele.napos.evalon.http.EvalonHttpException
import me.ele.napos.evalon.http.EvalonHttpOperation
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.http.HttpResponse
import org.apache.http.util.EntityUtils
import org.apache.logging.log4j.LogManager

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.zip.GZIPInputStream

import static me.ele.napos.evalon.http.EvalonHttpUtil.*

class LegacyGitlabApi {
    private static final logger = LogManager.getLogger()

    static String MASTER = "master"

    static String DEVELOP(projectId = null) {
        return "develop"
    }

    static String GITLAB_API_URL = "https://git.elenet.me/api/v4"

    static String currentToken() {
        return "v5GBf1zUYGyzEjL98Er_"
    }

    static DEFAULT_ERROR_HANDLER = { HttpResponse response ->
        int code = response.statusLine.statusCode
        if (code != HttpURLConnection.HTTP_OK && code != HttpURLConnection.HTTP_CREATED) {
            String message = EntityUtils.toString(response.entity)
            throw new EvalonHttpException("发生Http异常: " +
                    " Code: ${code}" +
                    " Reason: ${response.statusLine.reasonPhrase} Message: ${message}")
        }
    }

    static IGNORE_ERROR_HANDLER = { HttpResponse response ->
        // ignore
    }

    static {
        setHeaders("git.elenet.me", [
                "PRIVATE-TOKEN": currentToken()
        ])
        setErrorHandler("git.elenet.me", DEFAULT_ERROR_HANDLER)
    }

    static String BRANCH_API(String branchName = "") {
        if (branchName) {
            return "/repository/branches/${URLEncoder.encode(branchName, "UTF-8")}"
        } else {
            return "/repository/branches"
        }
    }

    static String COMMITS_API(String sha, boolean diff = false) {
        if (sha) {
            if (diff) {
                return "/repository/commits/${sha}/diff"
            } else {
                return "/repository/commits/${sha}"
            }
        } else {
            return "/repository/commits"
        }
    }

    static String PROJECT_API(Integer projectId) {
        if (projectId) {
            return "/projects/${projectId}"
        } else {
            return "/projects"
        }
    }

    static String HOOK_API() {
        return "/hooks"
    }

    static String VARIABLES_API(String key) {
        if (key) {
            return "/variables/${key}"
        }
        return "/variables"
    }

    static String RUNNER_API(Integer runnerId = null) {
        if (runnerId) {
            return "/runners/${runnerId}"
        }

        return "/runners/"
    }

    static String PROJECT_RUNNER_API(int projectId) {
        return "/projects/${projectId}/runners"
    }

    static String MERGE_REQUEST_API(Integer mergeRequestId = null, boolean merge = false) {
        if (mergeRequestId) {
            if (merge) {
                return "/merge_requests/${mergeRequestId}/merge"
            } else {
                return "/merge_requests/${mergeRequestId}"
            }
        } else {
            return "/merge_requests"
        }
    }

    static String REPOSITORY_FILE_API(String filePath) {
        if (filePath) {
            return "/repository/files/${filePath}"
        }

        return "/repository/files"
    }

    static String REPOSITORY_ARCHIVE_API() {
        return "/repository/archive"
    }

    static String JOB_API(int projectId, int jobId) {
        return "/projects/${projectId}/jobs/${jobId}"
    }

    // COMMITS API
    static getDiff(int projectId, String sha) {
        return httpGet(GITLAB_API_URL + PROJECT_API(projectId) + COMMITS_API(sha, true),
                null, Object, {
            return null
        })
    }

    // USER API

    static getUser() {
        return httpGet(new EvalonHttpOperation(
                baseUrl: GITLAB_API_URL,
                urls: ["user"]
        ))
    }

    // PROJECT API

    static GitlabProject getProject(Integer projectId, boolean simple = false) {
        def gProject = httpGet(new EvalonHttpOperation(
                baseUrl: GITLAB_API_URL,
                urls: [PROJECT_API(projectId)],
                parameters: [simple: simple],
                errorHandler: { HttpResponse response ->
                    int code = response.statusLine.statusCode

                    if (code == HttpURLConnection.HTTP_NOT_FOUND) {
                        return null
                    }
                }))

        return new GitlabProject(gProject)
    }

    static List<GitlabProject> queryProjects(String search, boolean simple = false) {
        def gProjects = httpGet(new EvalonHttpOperation(
                baseUrl: GITLAB_API_URL,
                urls: [PROJECT_API()],
                parameters: [search: search, simple: simple],
                errorHandler: { HttpResponse response ->
                    int code = response.statusLine.statusCode

                    if (code == HttpURLConnection.HTTP_NOT_FOUND) {
                        return null
                    }
                }))

        return gProjects.collect {
            new GitlabProject(it)
        }
    }

    static List<GitlabProject> queryProjects(String search, int offset, int limit, boolean simple = true) {
        def gProjects = httpGet(new EvalonHttpOperation(
                baseUrl: GITLAB_API_URL,
                urls: [PROJECT_API()],
                parameters: [search: search, simple: simple, page: offset, per_page: limit],
                errorHandler: { HttpResponse response ->
                    int code = response.statusLine.statusCode

                    if (code == HttpURLConnection.HTTP_NOT_FOUND) {
                        return null
                    }
                }))

        return gProjects.collect {
            new GitlabProject(it)
        }
    }

    static getVariables(int projectId) {
        return httpGet(new EvalonHttpOperation(
                baseUrl: GITLAB_API_URL,
                urls: [PROJECT_API(projectId), VARIABLES_API(null)]))
    }

    static getVariable(int projectId, String key) {
        return httpGet(new EvalonHttpOperation(
                baseUrl: GITLAB_API_URL,
                urls: [PROJECT_API(projectId), VARIABLES_API(key)],
                errorHandler: { HttpResponse response ->
                    int code = response.statusLine.statusCode
                    if (code == HttpURLConnection.HTTP_NOT_FOUND) {
                        return null
                    }
                }))
    }

    static setVariables(int project, Map pairs) {
        // ignore
    }

    static TarArchiveInputStream getProjectArchive(int projectId, String branch) {
        def stream = httpGet(new EvalonHttpOperation(
                baseUrl: GITLAB_API_URL,
                urls: [PROJECT_API(projectId), REPOSITORY_ARCHIVE_API()],
                parameters: [sha: branch],
                returnType: ByteArrayInputStream,
                errorHandler: { HttpResponse response ->
                    return null
                }))

        if (!stream) {
            return null
        }

        return new TarArchiveInputStream(new GZIPInputStream(stream))
    }

    // JOB API

    static getJob(Integer projectId, Integer jobId) {
        return httpGet(new EvalonHttpOperation(
                baseUrl: GITLAB_API_URL,
                urls: [JOB_API(projectId, jobId)]))
    }

    // BRANCH API

    static branches(int projectId) {
        return httpGet(GITLAB_API_URL + PROJECT_API(projectId) + BRANCH_API())
    }

    static branches(int projectId, GitlabBranchQuery query) {
        def branches = httpGet(GITLAB_API_URL + PROJECT_API(projectId) + BRANCH_API())

        if (query.after) {
            return branches.findAll { branch ->
                String dateStr = branch['commit']['committed_date']
                return LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_DATE_TIME) > query.after
            }
        }

        return branches
    }

    static latestBranchs(int projectId, int days) {
        return branches(projectId, new GitlabBranchQuery(after: LocalDateTime.now().minusDays(days)))
    }

    static List<String> latestBranchNameInOneWeek(int projectId) {
        List<String> latestBranches = branches(projectId, new GitlabBranchQuery(after: LocalDateTime.now().minusDays(7))).collect {
            it['name']
        }

        if (branchExists(projectId, DEVELOP()) && !latestBranches.contains(DEVELOP())) {
            latestBranches.add(DEVELOP())
        }

        if (!latestBranches.contains(MASTER)) {
            latestBranches.add(MASTER)
        }

        return latestBranches
    }

    static GitlabBranch branch(int projectId, String branch) {
        try {
            def gBranch = httpGet(GITLAB_API_URL + PROJECT_API(projectId) + BRANCH_API(branch))

            return new GitlabBranch(
                    name: gBranch['name'],
                    merged: gBranch['merged'],
                    commit: new GitlabCommit(
                            author_email: gBranch['commit']['author_email']
                    )
            )
        } catch (EvalonHttpException ignored) {
            return null
        }
    }

    static boolean branchExists(int projectId, String branchName) {
        return httpGet(GITLAB_API_URL + PROJECT_API(projectId) + BRANCH_API(branchName), null, Object, { HttpResponse response ->
            return false
        })
    }

    static createBranch(int projectId, String sourceBranch, String targetBranch, boolean override = false) {
        if (branchExists(projectId, targetBranch)) {
            if (override) {
                deleteBranch(projectId, targetBranch)
            } else {
                return null
            }
        }

        return httpPost(new EvalonHttpOperation(
                baseUrl: GITLAB_API_URL,
                urls: [PROJECT_API(projectId), BRANCH_API()],
                parameters: [branch_name: targetBranch, ref: sourceBranch],
                errorHandler: { HttpResponse response ->
                    int code = response.statusLine.statusCode
                    if (code != HttpURLConnection.HTTP_OK && code != HttpURLConnection.HTTP_CREATED) {
                        throw new EvalonHttpException("发生Http异常: " +
                                " Code: ${code}" +
                                " Reason: ${response.statusLine.reasonPhrase}")
                    }
                }))
    }

    static deleteBranch(int projectId,
                        String branchName) {
        return httpDelete(new EvalonHttpOperation(
                baseUrl: GITLAB_API_URL,
                urls: [PROJECT_API(projectId), BRANCH_API(branchName)]))
    }

    // MERGE REQUEST API

    static MRExists(int projectId, String title) {
        def mrs = listMR(projectId)

        return mrs.any { it.title == title }
    }

    static getSingleMR(int projectId, String title) {
        def mrs = listMR(projectId)

        return mrs.find { it.title == title }
    }

    static listMR(int projectId) {
        return httpGet(GITLAB_API_URL + PROJECT_API(projectId) + MERGE_REQUEST_API(), [state: "opened"])
    }

    static createMR(int projectId,
                    String sourceBranch,
                    String targetBranch,
                    String title) {
        return httpPost(new EvalonHttpOperation(
                baseUrl: GITLAB_API_URL,
                urls: [PROJECT_API(projectId), MERGE_REQUEST_API()],
                parameters: [source_branch: sourceBranch, target_branch: targetBranch, title: title],
                errorHandler: { HttpResponse response ->
                    int code = response.statusLine.statusCode
                    if (!(code in [
                            HttpURLConnection.HTTP_OK,
                            HttpURLConnection.HTTP_CREATED,
                            HttpURLConnection.HTTP_CONFLICT,
                    ])) {
                        throw new EvalonHttpException("发生Http异常: " +
                                " Code: ${code}" +
                                " Reason: ${response.statusLine.reasonPhrase}")
                    }
                }
        ))
    }

    static closeMR(int projectId,
                   int mrId) {
        return httpPut(new EvalonHttpOperation(
                baseUrl: GITLAB_API_URL,
                urls: [PROJECT_API(projectId), MERGE_REQUEST_API(mrId)],
                parameters: [state_event: "close"]))
    }

    static acceptMR(int projectId, int mrId) {
        httpPut(GITLAB_API_URL + PROJECT_API(projectId) + MERGE_REQUEST_API(mrId, true), null, Object, { HttpResponse response ->
            int status = response.statusLine.statusCode
            String reason = response.statusLine.reasonPhrase
            switch (status) {
                case 404:
                case 405:
                case 406:
                case 409:
                    throw new OperationFailedException("合并失败: ${reason}")
            }
        })

    }

    static deleteMR(int projectId, int mrId) {
        return httpDelete(new EvalonHttpOperation(
                baseUrl: GITLAB_API_URL,
                urls: [PROJECT_API(projectId), MERGE_REQUEST_API(mrId)]))
    }

    // RUNNER API

    private static final int ZOO_RUNNER_ID = 64

    static getRunnerDetail(int runnerId) {
        return httpGet(new EvalonHttpOperation(
                baseUrl: GITLAB_API_URL,
                urls: [RUNNER_API(runnerId)],
                errorHandler: { HttpResponse response ->
                    int code = response.statusLine.statusCode

                    if (code == HttpURLConnection.HTTP_NOT_FOUND) {
                        return null
                    }

                    throw new EvalonHttpException("发生Http异常: " +
                            " Code: ${code}" +
                            " Reason: ${response.statusLine.reasonPhrase}")
                }))
    }

    static listAllOnlineRunners() {
        return httpGet(new EvalonHttpOperation(
                baseUrl: GITLAB_API_URL,
                urls: [RUNNER_API()],
                parameters: [scope: "online"]))
    }

    static listProjectRunners(int projectId) {
        return httpGet(new EvalonHttpOperation(
                baseUrl: GITLAB_API_URL,
                urls: [PROJECT_RUNNER_API(projectId)]))
    }

    static enableRunner(int projectId, int runnerId = ZOO_RUNNER_ID) {
        return httpPost(new EvalonHttpOperation(
                baseUrl: GITLAB_API_URL,
                urls: [PROJECT_API(projectId), RUNNER_API()],
                parameters: [runner_id: runnerId],
                errorHandler: { HttpResponse response ->
                    logger.info("enable runner complete code: ${response.statusLine.statusCode}")
                }))
    }

    static disableRunner(int projectId, int runnerId = ZOO_RUNNER_ID) {
        return httpDelete(new EvalonHttpOperation(
                baseUrl: GITLAB_API_URL,
                urls: [PROJECT_API(projectId), RUNNER_API(runnerId)],
                errorHandler: { HttpResponse response ->
                    logger.info("disable runner complete code: ${response.statusLine.statusCode}")
                }))
    }

    // REPOSITORY FILE API

    static boolean repositoryFileExists(int projectId, String branchName, String fileName) {
        return getRepositoryFile(projectId, branchName, fileName) != null
    }

    static getRepositoryFile(int projectId, String branchName, String filePath) {
        def file = httpGet(new EvalonHttpOperation(
                baseUrl: GITLAB_API_URL,
                urls: [PROJECT_API(projectId), REPOSITORY_FILE_API(filePath)],
                parameters: [ref: branchName],
                errorHandler: { HttpResponse response ->
                    int code = response.statusLine.statusCode
                    if (code == HttpURLConnection.HTTP_NOT_FOUND) {
                        return null
                    }
                }))

        if (!file) {
            return null
        }

        return new String(Base64.decoder.decode(file["content"] as String), "UTF-8")
    }

    static createRepositoryFile(int projectId, String branchName, String filePath, String content) {
        return httpPost(new EvalonHttpOperation(
                baseUrl: GITLAB_API_URL,
                urls: [PROJECT_API(projectId), REPOSITORY_FILE_API(filePath)],
                parameters: [branch        : branchName,
                             content       : content,
                             commit_message: "create file ${filePath}"]))
    }

    static updateRepositoryFile(int projectId, String branchName, String filePath, String content) {
        return httpPut(new EvalonHttpOperation(
                baseUrl: GITLAB_API_URL,
                urls: [PROJECT_API(projectId), REPOSITORY_FILE_API(filePath)],
                parameters: [branch        : branchName,
                             content       : content,
                             commit_message: "update file ${filePath}"]))
    }

    static deleteRepositoryFile(int projectId, String branchName, String filePath) {
        return httpDelete(new EvalonHttpOperation(
                baseUrl: GITLAB_API_URL,
                urls: [PROJECT_API(projectId), REPOSITORY_FILE_API()],
                parameters: [file_path     : filePath,
                             branch        : branchName,
                             commit_message: "update file ${filePath}"]))
    }

    //web hooks

    static List<GitlabWebHook> listWebHooks(int projectId) {
        def json = httpGet(new EvalonHttpOperation(
                baseUrl: GITLAB_API_URL,
                urls: [PROJECT_API(projectId), HOOK_API()]
        ))

        List<GitlabWebHook> list = []

        json.each {
            list.add(new GitlabWebHook(
                    id: it['id'] as int,
                    url: it['url'],
                    push_events: it['push_events'],
                    tag_push_events: it['tag_push_events'],
                    merge_requests_events: it['merge_requests_events'],
                    enable_ssl_verification: it['enable_ssl_verification']))
        }

        return list
    }

    static addWebHook(int projectId, GitlabWebHook hook) {
        httpPost(new EvalonHttpOperation(
                baseUrl: GITLAB_API_URL,
                urls: [PROJECT_API(projectId), HOOK_API()],
                parameters: [
                        url: hook.url
                ]))
    }
}
