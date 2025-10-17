package com.evening.dailylife.core.util

import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader

/**
 * Shell 命令执行器，支持 root 与非 root 模式。
 */
object ShellCommandExecutor {

    fun exec(cmd: String, withoutRoot: Boolean = false): String {
        val process = if (withoutRoot) {
            Runtime.getRuntime().exec("sh")
        } else {
            Runtime.getRuntime().exec("su --mount-master")
        }
        val outputStream = DataOutputStream(process.outputStream)
        outputStream.writeBytes("$cmd\n")
        outputStream.flush()
        outputStream.writeBytes("exit\n")
        outputStream.flush()

        val inputStream = BufferedReader(InputStreamReader(process.inputStream))
        val errorStream = BufferedReader(InputStreamReader(process.errorStream))
        val result = StringBuilder()
        var line: String?
        while (inputStream.readLine().also { line = it } != null) {
            result.append(line).append('\n')
        }
        while (errorStream.readLine().also { line = it } != null) {
            result.append(line).append('\n')
        }
        process.waitFor()
        process.destroy()
        return result.toString()
    }
}
