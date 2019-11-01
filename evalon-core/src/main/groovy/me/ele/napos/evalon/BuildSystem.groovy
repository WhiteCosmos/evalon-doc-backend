package me.ele.napos.evalon

enum BuildSystem {
    GRADLE("build.gradle"), // Build By Gradle

    MAVEN("pom.xml"), // Build By Maven

    UNKNOWN("unknown")

    String buildFile

    BuildSystem(String buildFile) {
        this.buildFile = buildFile
    }
}