package me.ele.napos.evalon.controller

import me.ele.napos.evalon.CoreService
import me.ele.napos.evalon.DocService
import me.ele.napos.evalon.structs.DocPayload
import org.springframework.beans.factory.annotation.Autowired

class DocController implements WebResponseTrait {
    @Autowired
    private DocService docService
    @Autowired
    private CoreService coreService

    /**
     * Registry Project
     */
    def registryProject(DocPayload payload) {
        success(coreService.registryProject(payload))
    }

    /**
     * Query Project Modules Before Registry
     */
    def queryProjectModules(DocPayload payload) {
        success(docService.queryProjectModules(payload))
    }

    /**
     * Query Registry Process
     */
    def queryRegistryProcess(DocPayload payload) {
        success(docService.queryRegistryProcess(payload))
    }

    /**
     * Query All Repositories
     */
    def repositories() {
        success(docService.repositories())
    }

    /**
     * Query Branches Under Module Or App
     */
    def branches(long moduleId) {
        success(docService.branches(moduleId))
    }

    /**
     * Query Services Without Content
     */
    def documentTree(long branchId) {
        success(docService.documentTree(branchId))
    }

    /**
     * Query Service Content
     */
    def documentContent(String repositoryName, String projectName, String moduleName, String branchName, String serviceQualifiedName, String methodName) {
        if (!repositoryName || !projectName || !moduleName || !branchName || !serviceQualifiedName || !methodName) {
            success()
        }

        repositoryName = URLDecoder.decode(repositoryName, "UTF-8")

        projectName = URLDecoder.decode(projectName, "UTF-8")

        moduleName = URLDecoder.decode(moduleName, "UTF-8")

        branchName = URLDecoder.decode(branchName, "UTF-8")

        serviceQualifiedName = URLDecoder.decode(serviceQualifiedName, "UTF-8")

        methodName = URLDecoder.decode(methodName, "UTF-8")

        success(docService.documentContent(repositoryName, projectName, moduleName, branchName, serviceQualifiedName, methodName))
    }

    /**
     * Query Structs Without Content
     */
    def structTree(long branchId) {
        success(docService.structTree(branchId))
    }

    /**
     * Query Struct Content
     */
    def structContent(String repositoryName, String projectName, String moduleName, String branchName, String structQualifiedName) {
        if (!repositoryName || !projectName || !moduleName || !branchName || !structQualifiedName) {
            success()
        }

        repositoryName = URLDecoder.decode(repositoryName, "UTF-8")

        projectName = URLDecoder.decode(projectName, "UTF-8")

        moduleName = URLDecoder.decode(moduleName, "UTF-8")

        branchName = URLDecoder.decode(branchName, "UTF-8")

        structQualifiedName = URLDecoder.decode(structQualifiedName, "UTF-8")

        success(docService.structContent(repositoryName, projectName, moduleName, branchName, structQualifiedName))
    }

    def searchProjects(String projectName) {
        success(docService.searchGitlabProjects(projectName))
    }

    def searchBranches(int projectId) {
        success(docService.searchGitlabBranches(projectId))
    }

    @Deprecated
    def searchApplications(String s) {
//        success(docService.searchApplications(s))
    }

    // Favorites Api

    def favorites() {
        String tempToken = "evalon123"

        success(docService.favorites(tempToken))
    }

    def addApiToFavorites(String repositoryName, String projectName, String appName, String branchName, String serviceQualifiedName, String methodName) {
        docService.addApiToFavorites(repositoryName, projectName, appName, branchName, serviceQualifiedName, methodName)

        success()
    }

    def deleteApiFromFavorites(DocPayload payload) {

    }

    def addRequestToFavorites(DocPayload payload) {

    }

    def deleteRequestFromFavorites(DocPayload payload) {

    }
}
