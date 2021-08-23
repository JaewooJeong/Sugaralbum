package com.sugarmount.common.model

import com.google.gson.annotations.SerializedName
import com.sugarmount.common.utils.log

class ResponseData {
    lateinit var message: Message

    class Message {
        @SerializedName("@version")
        var version: String = ""

        lateinit var result: Result

        class Text {
            var order: Int = 0
            var group: String = ""
            var text: String = ""

            constructor(order: Int, type: String, text: String) {
                this.order = order
                this.group = type
                this.text = text
            }
        }

        class Result {
            var textList: ArrayList<Text> = ArrayList()

            @SerializedName("errata_count")
            var errataCount = 0

            @SerializedName("html")
            var html: String = ""
                set(value) {
                    field = value
                    log.e("[TimeCheck] Parse start")
                    var pos = 0
                    val regexTmp = (regex).toRegex()
                    var matchResult: MatchResult? = regexTmp.find(value)

                    while(matchResult != null){
                        val list = matchResult.destructured.toList()

                        println("grp1:${list[0]}, grp2:${list[1]}")
                        textList.add(Text(pos++, list[0], list[1]))

                        matchResult = matchResult.next()
                    }
                    log.e("[TimeCheck] Parse end")
                }

            fun setHtml() {
                this.html = html
                this.notagHtml = notagHtml.replace("<br>", "\n")
            }

            @SerializedName("notag_html")
            var notagHtml: String = ""

            @SerializedName("origin_html")
            var originHtml: String = ""

            companion object {
                const val regex = "class='(.\\w+)*_text'>*(.+?)<\\/span>"
            }

        }

        fun findText(value: String, pos: Int): Int {
            return result.notagHtml.indexOf(value, pos)
        }
    }

}