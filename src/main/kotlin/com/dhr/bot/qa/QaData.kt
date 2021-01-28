package com.dhr.bot.qa

import kotlinx.serialization.Serializable
import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.value

object QaData : AutoSavePluginData("qa") {
    var questions: MutableList<Question> by value() // List、Set 或 Map 同样支持 var。但请注意这是非引用赋值（详见下文）。
}

@Serializable
class Question(
    var groupId: Long,
    var questionId: Long,
    var question: String? = null,
    var answerType: Int = 0,// 0 文字回复  1 图片回复
    var answer: String? = null // 0 文字内容 1 图片id
)