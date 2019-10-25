package me.ele.napos.evalon

import me.ele.napos.evalon.domain.RegistryLogDomain
import me.ele.napos.evalon.structs.DocPayload
import org.springframework.context.MessageSource
import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.stereotype.Component

@Component
@Scope(scopeName = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
class RegistryLogger {
    DocPayload payload

    MessageSource messageSource

    def log(RegistryMessage message) {
        try {
            new RegistryLogDomain(
                    repositoryName: payload.repository.repositoryName,
                    projectName: payload.project.projectName,
                    branchName: payload.branch.branchName,
                    message: message
            ).save(flush: true)
        } catch (Exception ignore) { // ignore any exception

        }
    }

    def log(String message) {
        try {
            new RegistryLogDomain(
                    repositoryName: payload.repository.repositoryName,
                    projectName: payload.project.projectName,
                    branchName: payload.branch.branchName,
                    message: message
            ).save(flush: true)
        } catch (Exception ignore) { // ignore any exception

        }
    }

    def clear() {
        try {
            def logs = RegistryLogDomain.findAllByRepositoryNameAndProjectNameAndBranchName(
                    payload.repository.repositoryName,
                    payload.project.projectName,
                    payload.branch.branchName)

            RegistryLogDomain.deleteAll(logs)
        } catch (Exception ignore) { // ignore any exception

        }
    }
}
