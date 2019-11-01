package me.ele.napos.evalon

import com.github.javaparser.JavaParser
import me.ele.napos.evalon.domain.JavaServiceDomain
import me.ele.napos.evalon.domain.types.JavaPojoType
import me.ele.napos.evalon.resolver.AbstractResolver
import me.ele.napos.evalon.structs.ProjectRegistryReport
import me.ele.napos.evalon.structs.RegistryStatus
import me.ele.napos.evalon.visitor.JavaPojoCommentVisitor
import me.ele.napos.evalon.visitor.JavaServiceCommentVisitor
import org.reflections.Reflections
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class DocResolver {
    @Autowired
    private ProjectBuilder projectBuilder

    def resolvePackage(ResolverPayload payload) {
        def projectDir = payload.projectDir.listFiles().first()

        def f = projectDir.listFiles().find {
            return it.isFile() && it.name == "gradle.properties"
        }

        if (!f) {
            return
        }

        Properties p = new Properties()

        p.load(f.newInputStream())

        payload.groupId = p.getProperty("GROUP", "")

        payload.versionId = p.getProperty("VERSION", "")
    }

    def resolveServices(ResolverPayload payload) {
        def moduleDirs = payload.moduleDirs

        def createdBranches = payload.createdBranches

        def classLoader = payload.classLoader

        def report = payload.report

        def buildSystem = payload.buildSystem

        moduleDirs.each { moduleDir ->
            List<JavaServiceDomain> services = []

            List<JavaPojoType> pojos = []

            def classDir = projectBuilder.getBuildClassPath(moduleDir, buildSystem)

            if (!isClassDirExists(classDir)) {
                report.addException("模块 ${moduleDir.name} 没有找到构建文件夹", null, null)

                return
            }

            List<AbstractResolver> resolvers = initResolvers()

            resolvers.each {
                it.pojos = pojos
            }

            classDir.eachFileRecurse { classFile ->
                if (!isClassFile(classFile)) {
                    return
                }

                String qualifiedName = getQualifiedNameFromClassFile(classFile, buildSystem)

                def clazz = loadClass(classLoader, qualifiedName, report)

                if (clazz == null) {
                    return
                }

                if (isInnerClass(clazz)) {
                    return
                }

                def service = null

                resolvers.each { r ->
                    try {
                        !service && r.match(clazz) && (service = r.resolve(clazz))
                    } catch (Exception e) {
                        report.addException("服务 ${qualifiedName.split("\\.").last()} 解析失败", null, e)
                    }
                }

                if (service) {
                    service.moduleName = moduleDir.name

                    services << service
                }
            }

            def branchDomain = createdBranches.find {
                it.moduleName == moduleDir.name
            }

            if (services.isEmpty()) {
                report.registryStatus = RegistryStatus.NO_SERVICE
            }

            branchDomain.services = services

            branchDomain.pojos = pojos
        }
    }

    String getQualifiedNameFromJavaFile(File javaFile) {
        String path = javaFile.path.split("src/main/java/").last()

        def splits = path.split("/")

        String className = className(splits.last())

        splits = splits.dropRight(1) + className

        return splits.join(".")
    }

    String getQualifiedNameFromClassFile(File classFile, BuildSystem buildSystem) {
        def path = ""

        if (buildSystem == BuildSystem.MAVEN) {
            path = classFile.path.split("target/classes/").last()
        }

        if (buildSystem == BuildSystem.GRADLE) {
            path = classFile.path.split("build/classes/java/main/").last()
        }

        if (buildSystem == BuildSystem.UNKNOWN) {
            return ""
        }

        def splits = path.split("/")

        String className = className(splits.last())

        splits = splits.dropRight(1) + className

        return splits.join(".")
    }

    String className(String fileName) {
        return fileName.replace(".java", "").replace(".class", "")
    }

    List<AbstractResolver> initResolvers() {
        Reflections reflections = new Reflections("me.ele.napos.evalon.resolver")

        def resolvers = reflections.getSubTypesOf(AbstractResolver)

        return resolvers.collect {
            it.newInstance()
        }
    }

    def resolveJavaDoc(ResolverPayload payload) {
        def moduleDirs = payload.moduleDirs

        def createdBranches = payload.createdBranches

        def report = payload.report

        moduleDirs.each { moduleDir ->
            def branch = createdBranches.find {
                it.moduleName == moduleDir.name
            }

            if (!branch) {
                return
            }

            def services = branch.services

            def pojos = branch.pojos

            moduleDir.eachFileRecurse { javaFile ->
                if (!isJavaFile(javaFile)) {
                    return
                }

                String qualifiedName = getQualifiedNameFromJavaFile(javaFile)

                def service = services.find {
                    it.serviceQualifiedName == qualifiedName
                }

                def pojo = pojos.find {
                    it.qualifiedName == qualifiedName
                }

                try {
                    service && new JavaServiceCommentVisitor().visit(JavaParser.parse(javaFile), service)
                } catch (Exception e) {
                    report.addException("读取服务 ${service.serviceName} 注释失败", null, e)
                }

                try {
                    pojo && new JavaPojoCommentVisitor().visit(JavaParser.parse(javaFile), pojo)
                } catch (Exception e) {
                    report.addException("读取结构体 ${pojo.simpleName} 注释失败", null, e)
                }
            }
        }
    }

    private boolean isInnerClass(Class clazz) {
        try {
            return clazz.isMemberClass()
        } catch (Exception ignore) {
            return false
        } catch (Error ignore) {
            return false
        }
    }

    private Class loadClass(ClassLoader classLoader, String qualifiedName, ProjectRegistryReport report) {
        try {
            return classLoader.loadClass(qualifiedName)
        } catch (Exception e) {
            report.addException("类 ${qualifiedName} 加载失败", null, e)

            return null
        } catch (Error e) {
            report.addException("类 ${qualifiedName} 加载失败", null, new Exception(e.message))

            return null
        }
    }

    private boolean isClassDirExists(File classDir) {
        return classDir && classDir.exists() && classDir.isDirectory()
    }

    private boolean isJavaFile(File javaFile) {
        return javaFile.isFile() && javaFile.name.endsWith("java")
    }

    private boolean isClassFile(File classFile) {
        return classFile.isFile() && classFile.name.endsWith("class")
    }
}
