package com.dhr.bot.qa

data class RandomSetuVo(
    val `data`: List<Data>,
    val error: String,
)

data class Data(
    val urls: setuBean, val pid: Int,
)

data class setuBean(val small: String)