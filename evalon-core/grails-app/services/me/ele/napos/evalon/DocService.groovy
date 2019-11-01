package me.ele.napos.evalon

import me.ele.napos.evalon.domain.*
import me.ele.napos.evalon.exceptions.OperationFailedException
import me.ele.napos.evalon.gitlab.GitlabApiV3
import me.ele.napos.evalon.structs.Branch
import me.ele.napos.evalon.structs.DocPayload
import me.ele.napos.evalon.transformer.DocTransformer
import org.springframework.beans.factory.annotation.Autowired

/**
 * The Whole Doc Service
 */
class DocService {
    @Autowired
    private ProjectBuilder projectBuilder
    @Autowired
    private DocResolver docResolver
    @Autowired
    private RegistryLogger registryLogger
    @Autowired
    private DocTransformer documentTransformer

    def queryRegistryProcess(DocPayload payload) {
        def logs = RegistryLogDomain.findAllByRepositoryNameAndProjectNameAndBranchName(
                payload.repository.repositoryName,
                payload.project.projectName,
                payload.branch.branchName)

        return logs ? logs.last().message : ""
    }

    def repositories() {
        def results = []

        def repositories = RepositoryDomain.all

        def projects = ProjectDomain.all

        projects.each { project ->
            def repo = repositories.find {
                it.id == project.repositoryId
            }

            results << [
                    repositoryName   : repo.repositoryName,
                    projectId        : project.projectId,
                    projectName      : project.projectName,
                    groupId          : project.groupId,
                    versionId        : project.versionId,
                    defaultBranchName: "master",
                    teamName         : "",
                    modules          : [],
            ]
        }

        def modules = ModuleDomain.all

        modules.each { module ->
            def r = results.find { result ->
                result.projectId == module.projectId
            }

            r.modules << [
                    moduleId  : module.id,
                    moduleName: module.moduleName,
                    appName   : module.appName,
                    branches  : []
            ]

            r.teamName = r.modules.first().teamName
        }

        return results
    }

    def branches(long moduleId) {
        def branches = BranchDomain.findAllByModuleId(moduleId)

        return branches.collect {
            [
                    branchId  : it.id,
                    branchName: it.branchName,
                    isDefault : it.isDefault,
            ]
        }
    }

    def documentTree(long branchId) {
        def branch = BranchDomain.findById(branchId)

        if (!branch) {
            return []
        }

        def services = branch.services

        return documentTransformer.transform(services, false)
    }

    def documentContent(String repositoryName,
                        String projectName,
                        String appName,
                        String branchName,
                        String serviceQualifiedName,
                        String methodName) {
        def repository = RepositoryDomain.findByRepositoryName(repositoryName)

        if (!repository) {
            return null
        }

        def project = ProjectDomain.findByRepositoryIdAndProjectName(repository.id, projectName)

        if (!project) {
            return null
        }

        def module = ModuleDomain.findByProjectIdAndAppName(project.projectId, appName)

        if (!module) {
            return null
        }

        def branch = BranchDomain.findByModuleIdAndBranchName(module.id, branchName)

        if (!branch) {
            return null
        }

        return content(branch, serviceQualifiedName, methodName)
    }

    def content(BranchDomain branch, String serviceQualifiedName, String methodName) {
        def service = branch.services.find { service ->
            service.serviceQualifiedName == serviceQualifiedName
        }

        if (!service) {
            return null
        }

        def method = service.methods.find {
            it.methodName == methodName
        }

        if (!method) {
            return null
        }

        def javaMethod = documentTransformer.transformMethod(method)

        javaMethod.serviceName = service.serviceName

        javaMethod.serviceQualifiedName = service.serviceQualifiedName

        return javaMethod
    }

    def structTree(long branchId) {
        def branch = BranchDomain.findById(branchId)

        if (!branch) {
            return null
        }

        return documentTransformer.transformPojos(branch.pojos, false)
    }

    def structContent(String repositoryName, String projectName, String appName, String branchName, String structQualifiedName) {
        def repository = RepositoryDomain.findByRepositoryName(repositoryName)

        if (!repository) {
            return null
        }

        def project = ProjectDomain.findByRepositoryIdAndProjectName(repository.id, projectName)

        if (!project) {
            return null
        }

        def module = ModuleDomain.findByProjectIdAndAppName(project.projectId, appName)

        if (!module) {
            return null
        }

        def branch = BranchDomain.findByModuleIdAndBranchName(module.id, branchName)

        if (!branch) {
            return null
        }

        def pojo = branch.pojos.find {
            it.qualifiedName == structQualifiedName
        }

        if (!pojo) {
            return null
        }

        return documentTransformer.transformPojos([pojo])
    }

    def searchGitlabProjects(String projectName) {
        if (!projectName) {
            return []
        }

        def projects = GitlabApiV3.projects(projectName, true, 1, 20)

        return projects.collect {
            [
                    projectId         : it['id'] as int,
                    projectName       : it['path'],
                    repositoryName    : it['namespace']['path'],
                    projectDescription: it['description']
            ]
        }
    }

    def searchGitlabBranches(int projectId) {
        if (!projectId) {
            return []
        }

        def branches = GitlabApiV3.branches(projectId)

        println branches

        return branches.collect {
            new Branch(branchName: it['name'])
        }
    }

    def queryProjectModules(DocPayload payload) {
        def project = payload.project

        def branch = payload.branch

        def modules = []

        int projectId = project.projectId

        String branchName = branch.branchName

        File workspace = null

        try {
            workspace = projectBuilder.downloadProjectArchive(projectId, branchName)

            if (workspace.listFiles().length == 0) {
                throw new OperationFailedException("下载项目失败，项目为空")
            }

            def projectDir = workspace.listFiles().first()

            if (!projectBuilder.isJavaProject(projectDir)) {
                throw new OperationFailedException("没有发现Gradle或Maven配置文件，请确认是否为JAVA项目")
            }


            if (projectBuilder.usingGradle(projectDir)) {
                projectDir.eachDirRecurse { dir ->
                    if (projectBuilder.isGradleModule(dir)) {
                        modules << [
                                moduleName: dir.name,
                                registered: false,
                        ]
                    }
                }

                if (!modules) {
                    modules << [
                            moduleName: projectDir.name,
                            registered: false,
                    ]
                }
            }

            if (projectBuilder.usingMaven(projectDir)) {
                projectDir.eachDirRecurse { dir ->
                    if (projectBuilder.isMavenModule(dir)) {
                        modules << [
                                moduleName: dir.name,
                                registered: false,
                        ]
                    }
                }

                if (!modules) {
                    modules << [
                            moduleName: projectDir.name,
                            registered: false,
                    ]
                }
            }
        } finally {
            workspace && workspace.deleteDir()
        }

        def registeredModules = queryRegisteredModules(projectId)

        modules.each { m ->
            def rm = registeredModules.find {
                it.moduleName == m.moduleName
            }

            if (rm) {
                m.appName = rm.appName

                m.registered = true
            }
        }

        modules = modules.sort {
            it.moduleName
        }

        return modules
    }

    def queryRegisteredModules(int projectId) {
        return ModuleDomain.findAllByProjectId(projectId)
    }

    // Favorites Api

    def favorites(String token) {
        List<FavoriteApiDomain> apis = FavoriteApiDomain.findAllByToken(token)

        if (!apis) {
            return []
        }

        def results = []

        def apps = apis.groupBy {
            return [
                    repositoryName: it.repositoryName,
                    projectName   : it.projectName,
                    appName       : it.appName,
                    branches      : []
            ]
        }

        apps.each { app, appDomains ->
            def branches = appDomains.groupBy {
                return [
                        branchName: it.branchName,
                        services  : []
                ]
            }

            branches.each { branch, branchDomains ->
                def services = branchDomains.groupBy {
                    return [
                            serviceName         : it.serviceName,
                            serviceQualifiedName: it.serviceQualifiedName,
                            methods             : [],
                    ]
                }

                services.each { service, serviceDomains ->
                    serviceDomains.each { serviceDomain ->
                        service.methods << [
                                methodName   : serviceDomain.methodName,
                                methodComment: serviceDomain.methodComment,
                        ]
                    }
                }

                branch.services.addAll(services.keySet())

                app.branches << branch
            }

            results << app
        }

        return results
    }

    def addApiToFavorites(String repositoryName, String projectName, String appName, String branchName, String serviceQualifiedName, String methodName) {
        String tempToken = "evalon123456"

        def content = documentContent(repositoryName, projectName, appName, branchName, serviceQualifiedName, methodName)

        if (!content) {
            return
        }

        def api = FavoriteApiDomain
                .findByTokenAndRepositoryNameAndProjectNameAndAppNameAndBranchNameAndServiceQualifiedNameAndMethodName(
                        tempToken, repositoryName, projectName, appName, branchName, serviceQualifiedName, methodName)

        if (api) {
            return
        }

        api = new FavoriteApiDomain(
                token: tempToken,
                repositoryName: repositoryName,
                projectName: projectName,
                appName: appName,
                branchName: branchName,
                serviceQualifiedName: serviceQualifiedName,
                methodName: methodName)

        api.serviceName = serviceQualifiedName.split("\\.").last()

        api.methodComment = content.commentTitle

        api.save(flush: true)
    }
}
