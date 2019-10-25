package me.ele.napos.evalon.file

class EvalonFileUtil {
    static isJavaFile(File file) {
        return file && file.exists() && !file.isDirectory() && file.name.endsWith("java")
    }

    static isJarFile(File file) {
        return file && file.exists() && !file.isDirectory() && file.name.endsWith("jar")
    }

    static isOriginJarFile(File file) {
        return file && file.exists() && !file.isDirectory() && file.name.endsWith("jar.original")
    }

    static isTarFile(File file) {
        return file && file.exists() && !file.isDirectory() && file.name.endsWith("tar")
    }

    static isZipFile(File file) {
        return file && file.exists() && !file.isDirectory() && file.name.endsWith("zip")
    }

    static isGradleDistributions(File file) {
        return file.isDirectory() &&
                file.exists() &&
                (file.absolutePath.endsWith("/build/distributions"))
    }

    static extractTarFileTo(File source, File target) {

    }

    static extractZipFileTo(File source, File target) {

    }

    static isSpringJar(File file) {
        return isJarFile(file) && hasSpringBootInfo(file)
    }

    private static boolean hasSpringBootInfo(File file) {
        def process = "jar tvf ${file.absolutePath}".execute()

//        def result = EvalonProcessUtil.executeProcess(process)

//        if (result.hasError) {
//            return false
//        }

//        return result.outputMessage.readLines().any {
//            it.contains("BOOT-INF")
//        }
    }
}
