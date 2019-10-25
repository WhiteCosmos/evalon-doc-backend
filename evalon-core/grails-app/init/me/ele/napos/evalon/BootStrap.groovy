package me.ele.napos.evalon

import me.ele.napos.evalon.gitlab.GitlabApiV3

class BootStrap {
    GitlabConfiguration gitlabConfiguration

    def init = { servletContext ->
        GitlabApiV3.setGITLAB_URL(gitlabConfiguration.gitlabUrl)

        GitlabApiV3.setGITLAB_TOKEN(gitlabConfiguration.gitlabToken)

//        EvalonBootStrap.initializeEvalonPrimitiveTyes()

        EvalonBootStrap.initializeLocalDateTimeSupport()
    }

    def destroy = {

    }
}
