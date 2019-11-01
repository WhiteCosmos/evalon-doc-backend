package me.ele.napos.evalon

import me.ele.napos.evalon.domain.BranchDomain
import me.ele.napos.evalon.domain.ModuleDomain
import me.ele.napos.evalon.structs.ProjectRegistryReport

class ResolverPayload {
    File projectDir

    List<File> moduleDirs = []

    ClassLoader classLoader

    BuildSystem buildSystem = BuildSystem.UNKNOWN

    String groupId

    String versionId

    ProjectRegistryReport report

    List<ModuleDomain> createdModules = []

    List<ModuleDomain> deletedModules = []

    List<BranchDomain> createdBranches = []

    List<BranchDomain> deletedBranches = []
}
