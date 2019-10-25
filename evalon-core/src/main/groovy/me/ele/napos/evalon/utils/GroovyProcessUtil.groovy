package me.ele.napos.evalon.utils
/**
 * 不使用这种方式Groovy有可能在执行时卡住
 */
class GroovyProcessUtil {
    static boolean executeProcess(Process process) {
        process.consumeProcessErrorStream(System.err)

        process.consumeProcessOutputStream(System.out)

        return process.waitFor() == 0
    }

    static executeProcessWithOutput(Process process) {
        def err = new StringBuffer()

        def out = new StringBuilder()

        process.consumeProcessErrorStream(err)

        process.consumeProcessOutputStream(out)

        process.consumeProcessErrorStream(System.err)

        process.consumeProcessOutputStream(System.out)

        def code = process.waitFor()

        return [
                hasError: (code != 0),
                err: err.toString(),
                out: out.toString()
        ]
    }
}
