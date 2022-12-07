package com.xtc_gelato.constent_classes

import android.os.Build
import android.util.Log
import java.io.*
import java.util.*

class RootUtil {

    private val TAG = RootUtil::class.java.name

    fun isDeviceRooted(): Boolean {
        return checkRootMethod1() || checkRootMethod2() || checkRootMethod3()
    }

    fun checkRootMethod1(): Boolean {
        val buildTags = Build.TAGS
        return buildTags != null && buildTags.contains("test-keys")
    }

    fun checkRootMethod2(): Boolean {
        try {
            val file = File("/system/app/Superuser.apk")
            return file.exists()
        } catch (e: Exception) {
        }
        return false
    }

    fun checkRootMethod3(): Boolean {
        return ExecShell().executeCommand(SHELL_CMD.check_su_binary) != null
    }

    enum class SHELL_CMD(var command: Array<String>) {
        check_su_binary(arrayOf("/system/xbin/which", "su"));
    }

    class ExecShell {
        fun executeCommand(shellCmd: SHELL_CMD): ArrayList<String?>? {
            var line: String? = null
            val fullResponse = ArrayList<String?>()
            var localProcess: Process
            localProcess = try {
                Runtime.getRuntime().exec(shellCmd.command)
            } catch (e: Exception) {
                return null
            }
            val out = BufferedWriter(
                OutputStreamWriter(
                    localProcess.getOutputStream()
                )
            )
            val `in` = BufferedReader(
                InputStreamReader(
                    localProcess.getInputStream()
                )
            )
            try {
                while (`in`.readLine().also { line = it } != null) {
                    Log.d("TAG", "--> Line received: $line")
                    fullResponse.add(line)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            Log.d("TAG", "--> Full response was: $fullResponse")
            return fullResponse
        }
    }
}
