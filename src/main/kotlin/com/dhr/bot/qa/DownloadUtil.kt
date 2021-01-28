package com.dhr.bot.qa

import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

object DownloadUtil {
    private var client = OkHttpClient()
    private var gson = Gson()

    /**
     * 转成json
     *
     * @param obj
     * @return
     */
    fun toJson(obj: Any?): String? {
        return gson.toJson(obj)
    }

    /**
     * 转成bean
     *
     * @param content
     * @param cls
     * @return
     */
    fun <T> fromJson(content: String?, cls: Class<T>?): T? {
        if (content.isNullOrEmpty()) {
            return null
        }
        return gson.fromJson(content, cls)
    }

    fun getResponse(request: Request): String? {
        val execute = client.newCall(request).execute()
        return execute.body?.string()
    }

    fun downloadFile(imageRequest: Request, path: String) {
        val execute = client.newCall(imageRequest).execute()
        //将响应数据转化为输入流数据
        val inputStream = execute.body!!.byteStream()
        val fileOutputStream = FileOutputStream(File(path))
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(1024)
        var length: Int
        while (inputStream.read(buffer).also { it2 -> length = it2 } > 0) {
            output.write(buffer, 0, length)
        }
        fileOutputStream.write(output.toByteArray())
        inputStream.close()
        fileOutputStream.close()
    }
}