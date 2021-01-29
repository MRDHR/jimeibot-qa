package com.dhr.bot.qa

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import net.mamoe.mirai.utils.ExternalResource.Companion.sendAsImageTo
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import okhttp3.Request
import java.io.File
import java.io.IOException
import java.net.MalformedURLException

class QaPlugin : KotlinPlugin(
    JvmPluginDescription(
        id = QaPlugin::class.java.name,
        version = "0.0.1",
        name = "问答 power by 一生的等待"
    )
) {
    override fun onEnable() {
        QaData.reload()
        initQa()
    }

    private fun initQa() {
        GlobalEventChannel.subscribeGroupMessages {
            always {
                if (message.content.startsWith("问") && (message.content.contains("\r答") || message.content.contains(
                        "\n答"
                    ))
                ) {
                    saveQa(this)
                } else if (message.content.startsWith("删除问")) {
                    deleteQa(this)
                } else if (message.content.startsWith("查询问")) {
                    searchQa(this)
                } else {
                    checkQa(this)
                }
            }
        }
    }

    private fun saveQa(messageEvent: GroupMessageEvent) {
        launch(Dispatchers.IO) {
            val message = messageEvent.message
            val split = message.content.split("问")
            val question = split[1].substring(0, split[1].lastIndexOf("答"))
                .replace("\r", "")
                .replace("\n", "")
            val split1 = split[1].split("答")
            val answer = split1[1].replace("答", "")
            var imageId: String? = null
            val image = message[Image]
            if (null != image) {
                imageId = image.imageId
            }
            val groupId = messageEvent.group.id
            val questionId = if (QaData.questions.isEmpty()) 1 else QaData.questions.last().questionId + 1
            if (imageId.isNullOrEmpty()) {
                //文字问答
                QaData.questions.add(
                    Question(groupId, questionId, question, 0, answer)
                )
            } else {
                val queryUrl = image!!.queryUrl()
                if (queryUrl.isNotEmpty()) {
                    val file = File("${dataFolder.absolutePath}/image/")
                    if (!file.exists()) {
                        file.mkdirs()
                    }
                    val imagePath = "${dataFolder.absolutePath}/image/${image.imageId}"
                    val imageRequest: Request = Request.Builder()
                        .url(queryUrl)
                        .removeHeader("User-Agent")
                        .addHeader(
                            "User-Agent",
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.102 Safari/537.36 Edg/85.0.564.51"
                        )
                        .build()
                    DownloadUtil.downloadFile(imageRequest, imagePath)
                    //图片问答
                    QaData.questions.add(
                        Question(groupId, questionId, question, 1, imagePath)
                    )
                }
            }
            messageEvent.sender.group.sendMessage("问答消息已保存")
        }
    }

    private fun deleteQa(messageEvent: GroupMessageEvent) {
        launch(Dispatchers.IO) {
            val message = messageEvent.message
            val replace = message.content.replace("删除问", "").replace(" ", "")
            val questions = QaData.questions.filter {
                (it.groupId == messageEvent.group.id) && (it.questionId == replace.toLong())
            }
            questions.forEach {
                if (1 == it.answerType) {
                    val answer = it.answer
                    if (!answer.isNullOrEmpty() && !answer.isNullOrBlank()) {
                        val file = File(answer)
                        file.delete()
                    }
                }
                QaData.questions.removeAt(QaData.questions.lastIndexOf(it))
            }
            messageEvent.sender.group.sendMessage("该问题已删除")
        }
    }

    private fun searchQa(messageEvent: GroupMessageEvent) {
        launch(Dispatchers.IO) {
            val message = messageEvent.message
            val replace = message.content.replace("查询问", "")
            val questions = QaData.questions.filter {
                it.groupId == messageEvent.group.id && it.question?.contains(Regex("^*${replace}.*")) == true
            }
            val messageArray: MutableList<Message> = mutableListOf()
            messageArray.add(PlainText("查询到的问题如下："))
            questions.forEach {
                messageArray.add(PlainText("\n问题id：${it.questionId} 问题：${it.question}\n回答："))
                if (0 == it.answerType) {
                    //文字类
                    it.answer?.let { it1 ->
                        messageArray.add(PlainText(it1))
                    }
                } else {
                    //图片类
                    messageArray.add(
                        File(it.answer!!).toExternalResource()
                            .uploadAsImage(messageEvent.group)
                    )
                }
                messageArray.add(PlainText("回复[删除问 序号] 可删除对应的问题，举个栗子：删除问 1"))
            }
            val asMessageChain = messageArray.toMessageChain()
            messageEvent.sender.group.sendMessage(asMessageChain)
        }
    }

    //查询问题
    private fun checkQa(messageEvent: GroupMessageEvent) {
        launch(Dispatchers.IO) {
            val message = messageEvent.message
            val findLast = message.findLast { it is PlainText }
            //不是图片消息
            if (null != findLast) {
                val text = findLast.toString()
                if (text.isNotEmpty() && text.isNotBlank()) {
                    if ("我要se图" == text) {
                        TODO("需要替换key为你申请的key")
                        val request: Request = Request.Builder()
                            .url("https://api.lolicon.app/setu/?apikey=key&r18=0&size1200=true&proxy=disable")
                            .build()
                        val response = DownloadUtil.getResponse(request)
                        if (!response.isNullOrEmpty()) {
                            val randomSetuVo = DownloadUtil.fromJson(response, RandomSetuVo::class.java)
                            if (null != randomSetuVo && 0 == randomSetuVo.code) {
                                val images = mutableListOf<String>()
                                val file = File("${dataFolder.absolutePath}/image/")
                                if (!file.exists()) {
                                    file.mkdirs()
                                }
                                val imagePath =
                                    "${dataFolder.absolutePath}/image/${System.currentTimeMillis()}.jpg"
                                try {
                                    val first = randomSetuVo.data.first()
                                    val pid = first.pid
                                    val imageRequest: Request = Request.Builder()
                                        .url(first.url)
                                        .addHeader("referer", "https://www.pixiv.net/artworks/$pid")
                                        .removeHeader("User-Agent")
                                        .addHeader(
                                            "User-Agent",
                                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.102 Safari/537.36 Edg/85.0.564.51"
                                        )
                                        .build()
                                    DownloadUtil.downloadFile(imageRequest, imagePath)
                                    images.add(imagePath)
                                } catch (e: MalformedURLException) {
                                    e.printStackTrace()
                                } catch (e: IOException) {
                                    e.printStackTrace()
                                }
                                val messageArray: MutableList<Message> = mutableListOf()
                                messageArray.add(PlainText("您点的se图已到货，请签收：\n"))
                                images.forEach { it2 ->
                                    messageArray.add(
                                        File(it2).toExternalResource().uploadAsImage(messageEvent.group)
                                    )
                                }
                                val asMessageChain = messageArray.toMessageChain()
                                messageEvent.sender.group.sendMessage(asMessageChain)
                            }
                        }
                    } else {
                        val questions = QaData.questions.filter {
                            it.groupId == messageEvent.group.id && it.question?.contains(Regex("^*${text}.*")) == true
                        }
                        if (questions.isNotEmpty()) {
                            val randoms = (questions.indices).random()
                            val qaVo = questions[randoms]
                            if (0 == qaVo.answerType) {
                                if (!qaVo.answer.isNullOrEmpty()) {
                                    messageEvent.sender.group.sendMessage(qaVo.answer!!)
                                }
                            } else {
                                if (!qaVo.answer.isNullOrEmpty()) {
                                    File(qaVo.answer!!).toExternalResource()
                                        .sendAsImageTo(messageEvent.group)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}