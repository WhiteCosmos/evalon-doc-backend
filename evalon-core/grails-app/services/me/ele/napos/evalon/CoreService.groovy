package me.ele.napos.evalon

import me.ele.napos.evalon.domain.*
import me.ele.napos.evalon.domain.fields.JavaFieldDomain
import me.ele.napos.evalon.domain.types.*
import me.ele.napos.evalon.structs.*
import me.ele.napos.evalon.transformer.DocTransformer
import org.springframework.beans.factory.annotation.Autowired

class CoreService {
    @Autowired
    private ProjectBuilder projectBuilder
    @Autowired
    private DocResolver docResolver
    @Autowired
    private RegistryLogger registryLogger
    @Autowired
    private DocTransformer documentTransformer

    /**
     * Core service, registry project into system
     */
    def registryProject(DocPayload docPayload) {
        registryLogger.payload = docPayload

        def report = new ProjectRegistryReport()

        try {
            def projectId = docPayload.project.projectId

            def branchName = docPayload.branch.branchName

            def resolverPayload = initResolverPayload(docPayload)

            report = resolverPayload.report

            def projectDir

            def classLoader

            try {
                // Download Source Code Archive

                registryLogger.log(RegistryMessage.DOWNLOADING)

                projectDir = projectBuilder.downloadProjectArchive(projectId, branchName)

                // Build Project

                def buildSystem = projectBuilder.getProjectBuildSystem(projectDir)

                registryLogger.log(RegistryMessage.BUILDING)

                classLoader = projectBuilder.buildProject(projectDir, buildSystem, report) as URLClassLoader

                if (report.registryStatus != RegistryStatus.SUCCESS) {
                    return report
                }

                // Generate Payload

                resolverPayload.projectDir = projectDir

                resolverPayload.moduleDirs = getModuleDirs(projectDir, docPayload.modules)

                resolverPayload.classLoader = classLoader

                resolverPayload.buildSystem = buildSystem

                resolverPayload.report = report

                // Resolve

                registryLogger.log(RegistryMessage.RESOLVING_SERVICES)

                docResolver.resolveServices(resolverPayload)

                registryLogger.log(RegistryMessage.RESOLVING_JAVADOC)

                docResolver.resolveJavaDoc(resolverPayload)
            } finally {
                projectDir && projectDir.deleteDir()

                classLoader && classLoader.close()
            }

            if (noServiceFound(resolverPayload.createdBranches)) {
                report.registryStatus = RegistryStatus.NO_SERVICE

                return report
            }

            setRegisteredModules(resolverPayload, report)

            registryLogger.log(RegistryMessage.SAVING)

            save(resolverPayload, docPayload, report)

            registryLogger.clear()

        } catch (Exception e) {
            e.printStackTrace()

            report.registryStatus = RegistryStatus.SERVER_ERROR

            report.addException("服务器发生异常，生成失败", null, e)
        }

        return report
    }

    private boolean noServiceFound(List<BranchDomain> createdBranches) {
        return createdBranches.every {
            it.services.isEmpty()
        }
    }

    private save(ResolverPayload rp, DocPayload dp, ProjectRegistryReport report) {
        deleteModulesAndBranches(dp.branch.branchName, rp.deletedModules, rp.deletedBranches)

        try {
            def repositoryDomain = saveRepository(dp.repository)

            saveProject(repositoryDomain, dp.project)

            rp.createdBranches = rp.createdBranches.findAll {
                !it.services.isEmpty()
            }

            saveModulesAndBranches(rp.createdModules, rp.createdBranches)
        } catch (Exception e) {
            e.printStackTrace()

            report.registryStatus = RegistryStatus.SAVE_FAILED

            report.addException("保存文档发生异常", null, e)

            return
        }

//        try {
//        } catch (Exception e) {
//            report.addException("覆盖文档发生异常", null, e)
//        }
    }

    private saveRepository(Repository repository) {
        def repositoryDomain = RepositoryDomain.findByRepositoryName(repository.repositoryName)

        if (!repositoryDomain) {
            repositoryDomain = new RepositoryDomain(repositoryName: repository.repositoryName).save(flush: true)
        }

        return repositoryDomain
    }

    private saveProject(RepositoryDomain repositoryDomain, Project project) {
        def projectDomain = ProjectDomain.findByProjectId(project.projectId)

        if (!projectDomain) {
            projectDomain = new ProjectDomain(repositoryId: repositoryDomain.id, projectId: project.projectId, projectName: project.projectName).save(flush: true)
        }

        return projectDomain
    }

    private saveModulesAndBranches(List<ModuleDomain> createdModules, List<BranchDomain> createdBranches) {
        createdModules.each {
            it.save(flush: true)
        }

        createdBranches.each { branch ->
            def m = createdModules.find { it.moduleName == branch.moduleName }

            branch.moduleId = m.id
        }

        createdBranches.each {
            it.save(flush: true)
        }
    }

    private deleteModulesAndBranches(String branchName, List<ModuleDomain> deletedModules, List<BranchDomain> deletedBranches) {
        deletedModules.each { m ->
            if (!m.id) {
                return
            }

            def existBranches = BranchDomain.findAllByModuleId(m.id)

            // delete module which has only one branch
            if (existBranches.size() == 1 && existBranches.first().branchName == branchName) {
                m.delete(flush: true)
            }
        }

        deleteBranches(deletedBranches)
    }

    private setRegisteredModules(ResolverPayload payload, ProjectRegistryReport report) {
        def createdBranches = payload.createdBranches

        def createdModules = payload.createdModules

        createdBranches.each { b ->
            def m = createdModules.find {
                it.moduleName == b.moduleName
            }

            report.registeredModules.add(new RegisteredModule(
                    moduleName: m.moduleName,
                    appName: m.appName,
                    serviceCount: b.services.size(),
                    methodCount: 0
            ))
        }
    }

    private getModuleDirs(File projectDir, List<Module> modules) {
        def dirs = []

        if (modules.size() > 1) {
            modules = modules.findAll { it.needRegistry }
        }

        def moduleNames = modules.collect { it.moduleName }

        if (modules.size() == 1 && modules.head().moduleName == projectDir.name) { // Only one module itself
            return [projectDir]
        }

        projectDir.eachDirRecurse {
            if (it.isDirectory() && it.name in moduleNames) {
                dirs << it
            }
        }

        return dirs
    }

    private initResolverPayload(DocPayload payload) {
        def rp = new ResolverPayload()

        def repository = payload.repository

        def project = payload.project

        def modules = payload.modules

        if (modules.size() > 1) {
            modules = modules.findAll { it.needRegistry }
        }

        def branch = payload.branch

        def report = new ProjectRegistryReport()

        report.repositoryName = repository.repositoryName

        report.projectId = project.projectId

        report.projectName = project.projectName

        report.branchName = branch.branchName

        if (payload.update) {
            def module = modules.first()

            def moduleDomain = ModuleDomain.findByProjectIdAndModuleName(project.projectId, module.moduleName)

            rp.createdModules = [moduleDomain]

            rp.deletedBranches = [BranchDomain.findByModuleIdAndBranchName(moduleDomain.id, branch.branchName)]

            rp.createdBranches = [new BranchDomain(
                    moduleId: moduleDomain.id,
                    moduleName: moduleDomain.moduleName,
                    branchName: branch.branchName
            )]
        } else {
            def result = initModules(project, modules)

            def deletedModules = result.deletedModules

            def createdModules = result.createdModules

            def deletedBranches = BranchDomain.findAllByModuleIdInListAndBranchName(
                    (deletedModules + createdModules).collect { it.id },
                    branch.branchName)

            def createdBranches = createdModules.collect {
                return initBranch(it, branch)
            }

            rp.createdModules = createdModules

            rp.createdBranches = createdBranches

            rp.deletedModules = deletedModules

            rp.deletedBranches = deletedBranches
        }

        rp.report = report

        return rp
    }

    private initModules(Project project, List<Module> modules) {
        def projectId = project.projectId

        def oldValues = ModuleDomain.findAllByProjectId(projectId)

        def newValues = modules.collect {
            return new ModuleDomain(
                    projectId: projectId,
                    appName: it.appName ? it.appName : it.moduleName,
                    moduleName: it.moduleName)
        }

        def commonModules = oldValues.findAll {
            it.moduleName in newValues.collect { it.moduleName }
        }

        def deletedModules = oldValues.findAll {
            !(it.moduleName in commonModules.collect { it.moduleName })
        }

        def addedModules = newValues.findAll {
            !(it.moduleName in commonModules.collect { it.moduleName })
        }

        return [
                createdModules: commonModules + addedModules,

                deletedModules: deletedModules
        ]
    }

    private BranchDomain initBranch(ModuleDomain moduleDomain, Branch branch) {
        return new BranchDomain(moduleId: moduleDomain.id,
                moduleName: moduleDomain.moduleName,
                branchName: branch.branchName,
                isDefault: branch.isDefault)
    }

    // Gorm Mongo Not Support Cascade
    private deleteBranches(List<BranchDomain> deletedBranches) {
        deletedBranches.each {
            deleteBranch(it)
        }
    }

    private deleteBranch(BranchDomain deletedBranch) {
        Set<JavaPojoType> set = []

        deletedBranch.pojos.each {
            deleteJavaAbstractTypeRecursively(it, set)
        }

        deletedBranch.services.each { service ->
            service.methods.each { method ->
                method.parameters.each { p ->
                    if (p.fieldTypes) {
                        deleteJavaAbstractTypeRecursively(p.fieldTypes.first(), set)
                    }

                    p.delete()
                }

                method.responses.each { r ->
                    if (r.fieldTypes) {
                        deleteJavaAbstractTypeRecursively(r.fieldTypes.first(), set)
                    }

                    r.delete()
                }

                method.exceptions.each { e ->
                    deleteJavaAbstractTypeRecursively(e, set)
                }

                method.delete()
            }

            service.delete()
        }

        deletedBranch.delete()

        JavaAbstractType.withSession { session ->
            session.flush()

            session.clear()
        }

        JavaArgumentType.withSession { session ->
            session.flush()

            session.clear()
        }

        JavaFieldDomain.withSession { session ->
            session.flush()

            session.clear()
        }

        JavaServiceDomain.withSession { session ->
            session.flush()

            session.clear()
        }

        JavaMethodDomain.withSession { session ->
            session.flush()

            session.clear()
        }

        BranchDomain.withSession { session ->
            session.flush()

            session.clear()
        }
    }

    private deleteJavaAbstractTypeRecursively(JavaAbstractType type, Set<JavaPojoType> deleted = []) {
        if (!type) {
            return
        }

        if (type instanceof JavaPrimitiveType) {
            type.delete()

            return
        }

        if (type instanceof JavaGenericType) {
            deleteJavaAbstractTypeRecursively(type.genericType)

            type.argumentTypes.each {
                it.types.each {
                    deleteJavaAbstractTypeRecursively(it, deleted)
                }

                it.delete()
            }

            type.delete()

            return
        }

        if (type instanceof JavaListType) {
            deleteJavaAbstractTypeRecursively(type.typeArgument.first(), deleted)

            type.delete()

            return
        }

        if (type instanceof JavaSetType) {
            deleteJavaAbstractTypeRecursively(type.typeArgument.first(), deleted)

            type.delete()

            return
        }

        if (type instanceof JavaMapType) {
            if (type.keyTypeArgument) {
                deleteJavaAbstractTypeRecursively(type.keyTypeArgument.first(), deleted)
            }

            if (type.valueTypeArgument) {
                deleteJavaAbstractTypeRecursively(type.valueTypeArgument.first(), deleted)
            }

            type.delete()

            return
        }

        if (type instanceof JavaEnumType) {
            type.delete()

            return
        }

        if (type instanceof JavaPojoType) {
            if (type in deleted) {
                return
            }

            deleted << type

            type.fields.each {
                if (!it) {
                    return // ???
                }

                if (it.fieldTypes) {
                    deleteJavaAbstractTypeRecursively(it.fieldTypes.first(), deleted)
                }

                it.delete()
            }

            type.extensions.each {
                deleteJavaAbstractTypeRecursively(it, deleted)
            }

            type.delete()
        }
    }
}