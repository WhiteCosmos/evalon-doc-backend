package me.ele.napos.evalon.structs


import me.ele.napos.evalon.domain.BranchDomain

class ProjectRegistryReport {
    long reportId

    long projectId

    String repositoryName

    String projectName

    String branchName

    RegistryStatus registryStatus = RegistryStatus.SUCCESS

    String buildLog

    List<RegisteredModule> registeredModules = []

    List<RegisteredException> registeredExceptions = []

    Performance performance = new Performance()

    def addException(String exceptionName, String filePath, Exception e) {
//        if (Environment.current == DEVE)
        //TODO filePath 去重

        def ex = new RegisteredException()

        ex.exceptionName = exceptionName

        ex.filePath = filePath //TODO

        e && (ex.stacktraces = e.stackTrace.collect {
            it.toString()
        })

        registeredExceptions << ex
    }

    def addModules(List<Module> modules, Map<String, BranchDomain> branches) {

    }
}

class RegisteredModule {
    String moduleName

    String appName

    int serviceCount = 0

    int methodCount = 0
}

class RegisteredException {
    String exceptionName

    String filePath

    List<String> stacktraces = []
}

class Performance {
    String gitVersion

    String gitTiming

    String gradleVersion

    String mavenVersion

    String buildScript

    String buildTiming
}

enum RegistryStatus {
    CLONE_FAILED("项目克隆失败"), // git clone failed

    BUILD_FAILED("项目构建失败"), // project build failed

    SAVE_FAILED("文档保存失败"),

    NO_SERVICE("没有发现接口"), // project or module has no service

    SUCCESS("接入成功"),

    SERVER_ERROR("服务器异常") // Unknown error from server

    String message

    RegistryStatus(String message) {
        this.message = message
    }
}

