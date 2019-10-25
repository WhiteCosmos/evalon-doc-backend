package me.ele.napos.evalon

enum RegistryMessage {
    DOWNLOADING("doc.downloading"),

    BUILDING("doc.building"),

    RESOLVING_SERVICES("doc.resolving_services"),

    RESOLVING_JAVADOC("doc.resolving_javadoc"),

    SAVING("doc.saving")

    String message

    RegistryMessage(String message) {
        this.message = message
    }
}