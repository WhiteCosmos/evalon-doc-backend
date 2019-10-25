package me.ele.napos.evalon

import me.ele.napos.evalon.domain.BranchDomain
import me.ele.napos.evalon.domain.ProjectDomain
import me.ele.napos.evalon.exceptions.BuildSystemNotSupportedException
import me.ele.napos.evalon.exceptions.FileAlreadyFoundException
import me.ele.napos.evalon.exceptions.OperationFailedException
import me.ele.napos.evalon.gitlab.GitlabApiV3
import me.ele.napos.evalon.structs.ProjectRegistryReport
import me.ele.napos.evalon.structs.RegistryStatus
import org.springframework.stereotype.Component

import static me.ele.napos.evalon.utils.FileUtil.*
import static me.ele.napos.evalon.utils.GroovyProcessUtil.executeProcess
import static me.ele.napos.evalon.utils.GroovyProcessUtil.executeProcessWithOutput

/**
 * Build Java Project with Gradle or Maven
 */
@Component
@SuppressWarnings("GroovyAssignabilityCheck")
class ProjectBuilder {
    static final String GRADLE_BUILD_SCRIPT = "gradle clean build -x compileTestJava" // Skip Test

    static final String MAVEN_BUILD_SCRIPT = "mvn package"

    static final String GRADLE_CLASSES_PATH = "build/classes" // Gradle Class Path

    static final String MAVEN_CLASSES_PATH = "target/classes" // Maven Class Path

    static final String SPRING_BOOT_INF = "BOOT-INF" // Spring Boot libs

    static final String GRADLE_BUILD_FILE = "build.gradle" // Gradle Config File

    static final String MAVEN_BUILD_FILE = "pom.xml" // Maven Config File

    URLClassLoader buildProject(File projectDir, BuildSystem buildSystem, ProjectRegistryReport report) {
        if (buildSystem == BuildSystem.GRADLE) {
            buildByGradle(projectDir, report)

            if (report.registryStatus == RegistryStatus.BUILD_FAILED) {
                return null
            }

            return loadJars(projectDir)
        }

        if (buildSystem == BuildSystem.MAVEN) {
            buildByMaven(projectDir, report)

            if (report.registryStatus == RegistryStatus.BUILD_FAILED) {
                return null
            }

            return loadJars(projectDir)
        }

        throw new BuildSystemNotSupportedException()
    }

    File getBuildClassPath(File moduleDir, BuildSystem buildSystem) {
        if (buildSystem == BuildSystem.GRADLE) {
            return new File("${moduleDir.absolutePath}/${GRADLE_CLASSES_PATH}")
        }

        if (buildSystem == BuildSystem.MAVEN) {
            return new File("${moduleDir.absolutePath}/${MAVEN_CLASSES_PATH}")
        }

        return null
    }

    File getBuildClassPath(File projectDir, File descriptor) {
        def classesDir = null

        if (usingGradle(projectDir)) {
            classesDir = new File("${descriptor.absolutePath}/${GRADLE_CLASSES_PATH}")

            return classesDir
        }

        if (usingMaven(projectDir)) {
            classesDir = new File("${descriptor.absolutePath}/${MAVEN_CLASSES_PATH}")

            return classesDir
        }

        if (!classesDir || !classesDir.exists()) {
            return null
        }

        return classesDir
    }

    File downloadProjectArchive(projectId, branchName) {
        File workspace = new File("workspace_${UUID.randomUUID().toString()}")

        def gitlabProject = GitlabApiV3.projects(projectId)

        String projectName = gitlabProject.path

        if (!branchName) {
            branchName = gitlabProject.default_branch
        }

        String sshUrl = gitlabProject.ssh_url_to_repo

        File projectDir = new File("${workspace.absolutePath}/${projectName}")

        def p = "git clone --single-branch -b ${branchName} ${sshUrl} ${projectDir.absolutePath}".execute()

        def r = executeProcessWithOutput(p)

        if (r.hasError) {
            throw new OperationFailedException("项目 ${projectName} 克隆GIT仓库 ${sshUrl} 失败: ${r.err}")
        }

        return workspace
    }

    /**
     * 下载项目
     */
    File downloadProjectArchive(ProjectDomain projectDomain, BranchDomain context, String uuid = UUID.randomUUID().toString()) {
        File workspace = new File("workspace_${uuid}")

        if (!workspace.mkdir()) {
            throw new OperationFailedException("文件夹 ${workspace.name} 创建失败!")
        }

        File projectDir = new File("${workspace.absolutePath}/${projectDomain.projectName}")

        projectDir.mkdir()

        println "项目 ${projectDomain.projectName} 生成临时文件夹 ${workspace.name} 完成"

        def p = "git clone --single-branch -b ${context.branch} ${projectDomain.sshUrl} ${projectDir.absolutePath}".execute()

        def r = executeProcess(p)

        if (!r) {
            throw new OperationFailedException("项目 ${projectDomain.projectName} 克隆GIT仓库 ${projectDomain.sshUrl} 失败: ")
        }

        logger.log("项目 ${projectDomain.projectName} 下载完成", ["下载用时": "7.2秒"])

        return projectDir
    }

    BuildSystem getProjectBuildSystem(File projectDir) {
        if (usingGradle(projectDir)) {
            return BuildSystem.GRADLE
        }

        if (usingMaven(projectDir)) {
            return BuildSystem.MAVEN
        }

        return BuildSystem.UNKNOWN
    }

    boolean isJavaProject(File projectDir) {
        return usingGradle(projectDir) || usingMaven(projectDir)
    }

    /**
     * 使用了Vine框架项目
     */
    boolean isVineProject(File projectDir) { // app.properties
        boolean flag = false

        projectDir.eachFileRecurse {
            if (it.name == VINE_APP_PROPERTIES) {
                flag = true
            }
        }

        return flag
    }

    boolean isYMLProject(File projectDir) { // xxx_build.yml
        boolean flag = false

        projectDir.eachFileRecurse {
            if (it.name.endsWith(BUILD_YML)) {
                flag = true
            }
        }

        return flag
    }

    boolean usingGradle(File projectDir) {
        try {
            projectDir.eachFileRecurse {
                if (it.name == GRADLE_BUILD_FILE) {
                    throw new FileAlreadyFoundException()
                }
            }
        } catch (FileAlreadyFoundException ignore) {
            return true
        }

        return false
    }

    boolean isGradleModule(File dir) {
        if (!dir.isDirectory()) {
            return false
        }

        if (dir.isHidden()) {
            return false
        }

        if (!usingGradle(dir)) {
            return false
        }

        return dir.listFiles().any { f ->
            if (f.isDirectory() && usingGradle(f)) {
                return false
            }

            return true
        }
    }

    boolean usingMaven(File projectDir) {
        try {
            projectDir.eachFileRecurse {
                if (it.name == MAVEN_BUILD_FILE) {
                    throw new FileAlreadyFoundException()
                }
            }
        } catch (FileAlreadyFoundException ignore) {
            return true
        }

        return false
    }

    boolean isMavenModule(File dir) {
        if (!dir.isDirectory()) {
            return false
        }

        if (dir.isHidden()) {
            return false
        }

        if (!usingMaven(dir)) {
            return false
        }

        return dir.listFiles().any { f ->
            if (f.isDirectory() && usingMaven(f)) {
                return false
            }

            return true
        }
    }

    private buildByGradle(File projectDir, ProjectRegistryReport report) {
        def rootDir = findGradleRootDir(projectDir)

        def p = (GRADLE_BUILD_SCRIPT + " -p ${rootDir.absolutePath}").execute()

        def r = executeProcessWithOutput(p)

        report.buildLog = r.out

        if (r.hasError) {
            report.buildLog = r.err

            report.registryStatus = RegistryStatus.BUILD_FAILED
        }
    }

    private findGradleRootDir(File projectDir) {
        return findParentDirRecurse(projectDir, GRADLE_BUILD_FILE)
    }

    private findParentDirRecurse(File dir, String fileName) {
        if (!dir.isDirectory() || dir.isHidden()) {
            return null
        }

        def target = null

        try {
            dir.listFiles().each {
                if (it.isFile() && it.name == fileName) {
                    target = it.parentFile

                    throw new FileAlreadyFoundException() // Stop recurse
                }
            }

            dir.listFiles().each {
                target = findParentDirRecurse(it, fileName)

                if (target) {
                    throw new FileAlreadyFoundException() // Stop recurse
                }
            }
        } catch (FileAlreadyFoundException ignore) {

        }

        return target
    }


    private buildByMaven(File projectDir, ProjectRegistryReport report) {
        File rootDir = findMavenRootDir(projectDir)

        def envp = getEnvForMaven()

        def p = MAVEN_BUILD_SCRIPT
                .execute(envp, rootDir) //指定脚本执行的目录

        def r = executeProcessWithOutput(p)

        report.buildLog = r.out

        if (r.hasError) {
            report.buildLog = r.err ? r.err : r.out

            report.registryStatus = RegistryStatus.BUILD_FAILED
        }
    }

    private List<String> getEnvForMaven() {
        def envp = System.getenv().collect {
            return it.key + "=" + it.value
        }

        envp.add("LANG=zh_CN.UTF-8")

        return envp
    }

    private findMavenRootDir(File projectDir) {
        return findParentDirRecurse(projectDir, MAVEN_BUILD_FILE)
    }

    private ClassLoader loadJars(File projectDir) {
        List<URL> URLs = []

        loadJarFromProject(projectDir, URLs)

        return new URLClassLoader(URLs as URL[])
    }

    private loadJarFromProject(File projectDir, List<URL> URLs) {
        def springAppJars = [] //  Spring App Jar

        def allZipFiles = [] // All Other Zip Files

        def allTarFiles = [] // All Other Tar Files

        projectDir.eachFileRecurse { f ->
            if (isSpringJar(f)) { // 加载SpringBoot包
                springAppJars << f

                return
            }

            if (isJarFile(f) || isOriginJarFile(f)) { // 加载一般Jar包，Spring需要加载Original
                URLs << f.toURI().toURL()

                return
            }

            if (isZipFile(f)) {
                allZipFiles << f

                return
            }

            if (isTarFile(f)) {
                allTarFiles << f

                return
            }
        }

        URLs.addAll(loadJarFromSpringAppJar(springAppJars))

        URLs.addAll(loadJarFromZipFiles(allZipFiles))

        URLs.addAll(loadJarFromTarFiles(allTarFiles))

        return URLs
    }

    private static final String SPRING_BOOT_INF = "BOOT-INF"

    private loadJarFromSpringAppJar(List<File> springAppJars) {
        def urls = []

        springAppJars.each { springAppJar ->
            urls << springAppJar.toURI().toURL() // 添加自身

            def dir = makeRandomFolderUnderParent(springAppJar)

            extractZipFile(springAppJar, dir)

            def BOOT_INF = new File("${dir.absolutePath}/${SPRING_BOOT_INF}")

            if (!BOOT_INF.exists()) {
                return urls
            }

            BOOT_INF.eachFileRecurse {
                if (isJarFile(it)) {
                    urls << it.toURI().toURL()
                }
            }
        }

        return urls
    }

    private loadJarFromZipFiles(List<File> allZipFiles) {
        def urls = []

        allZipFiles.each { zip ->
            def zipDir = makeRandomFolderUnderParent(zip)

            extractZipFile(zip, zipDir)

            zipDir.eachFileRecurse {
                if (isJarFile(it)) {
                    urls << it.toURI().toURL()
                }
            }
        }

        return urls
    }

    private loadJarFromTarFiles(List<File> allTarFiles) {
        def urls = []

        allTarFiles.each { tar ->
            def tarDir = makeRandomFolderUnderParent(tar)

            extractTarFile(tar, tarDir)

            tarDir.eachFileRecurse {
                if (isJarFile(it)) {
                    urls << it.toURI().toURL()
                }
            }
        }

        return urls
    }
}
