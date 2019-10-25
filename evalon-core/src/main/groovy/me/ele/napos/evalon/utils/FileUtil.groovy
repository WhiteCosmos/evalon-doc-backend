package me.ele.napos.evalon.utils


import static GroovyProcessUtil.executeProcess
import static GroovyProcessUtil.executeProcessWithOutput

class FileUtil {
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

    static extractTarFile(File source, File target) {
        def command = "tar -xf ${source.absolutePath} -C ${target.absolutePath}"

        return executeProcess(command.execute())
    }

    static extractZipFile(File source, File target) {
        def command = "unzip -o ${source.absolutePath} -d ${target.absolutePath}"

        return executeProcess(command.execute())
    }

    static makeRandomFolderUnderParent(File file) {
        def dir = new File("${file.parentFile.absolutePath}/${UUID.randomUUID().toString()}")

        dir.mkdir()

        return dir
    }

    static isSpringJar(File file) {
        return isJarFile(file) && hasSpringBootInfo(file)
    }

    private static boolean hasSpringBootInfo(File file) {
        def process = "jar tvf ${file.absolutePath}".execute()

        def r = executeProcessWithOutput(process)

        if (r.hasError) {
            return false
        }

        return r.out.readLines().any {
            it.contains("BOOT-INF")
        }
    }
}
