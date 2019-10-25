package me.ele.napos.evalon.structs

import grails.validation.Validateable

/**
 * @author whitecosm0s
 */
class DocPayload implements Validateable {
    Repository repository // Gitlab Repository

    Project project // Gitlab Project

    List<Module> modules = [] // Module in Project

    Branch branch

    boolean update
}
