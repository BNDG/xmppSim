package com.hjq.demo.chat.extensions

import com.bndg.smack.extensions.base.IExtension
import com.bndg.smack.extensions.base.IExtensionProvider

/**
 * @author r
 * @date 2024/12/6
 * @description 个人名片信息
 */
class CardInfoExtension : IExtension {
    var nickName: String = ""
    var userId: String = ""

    companion object {
        @kotlin.jvm.JvmField
        val NAMESPACE: String = "urn:xmpp:sim:card-info:0"

        @kotlin.jvm.JvmField
        val ELEMENT_NAME: String = "card-info"
    }

    constructor(userId: String, nickName: String) {
        this.nickName = nickName
        this.userId = userId
    }

    override fun getNamespace(): String {
        return NAMESPACE
    }

    override fun getElementName(): String {
        return ELEMENT_NAME
    }


    override fun getExtraData(): MutableMap<String, Any> {
        val propertyMap = HashMap<String, Any>()
        propertyMap["nickName"] = nickName
        propertyMap["userId"] = userId
        return propertyMap
    }

    class Provider : IExtensionProvider<CardInfoExtension> {
        override fun getProperty(): MutableList<String> {
            val propertyList = ArrayList<String>()
            propertyList.add("nickName")
            propertyList.add("userId")
            return propertyList
        }

        override fun createExtension(extraData: MutableMap<String, String>): CardInfoExtension {
            val cardInfoExtension =
                CardInfoExtension(extraData["userId"]!!, extraData["nickName"]!!)
            return cardInfoExtension
        }

    }


}