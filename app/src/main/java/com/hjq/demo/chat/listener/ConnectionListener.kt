package com.hjq.demo.chat.listener

import com.bndg.smack.callback.IConnectionListener

/**
 * @author r
 * @date 2024/8/26
 * @description Brief description of the file content.
 */
object ConnectionListener :
    IConnectionListener {
    val myConnectionListener: MutableList<IConnectionListener> = mutableListOf()
    fun addConnectionListener(listener: IConnectionListener) {
        myConnectionListener.add(listener)
    }

    fun removeConnectionListener(listener: IConnectionListener) {
        myConnectionListener.remove(listener)
    }

    override fun onServerConnecting() {
        for (listener in myConnectionListener) {
            listener.onServerConnecting()
        }
    }

    override fun onServerConnected() {
        for (listener in myConnectionListener) {
            listener.onServerConnected()
        }
    }

    override fun onAuthenticated() {
        for (listener in myConnectionListener) {
            listener.onAuthenticated()
        }
    }

    override fun onServerConnectFailed(desc: String?) {
        for (listener in myConnectionListener) {
            listener.onServerConnectFailed(desc)
        }
    }

    override fun onLoginFailed(code: Int, desc: String?) {
        for (listener in myConnectionListener) {
            listener.onLoginFailed(code, desc)
        }
    }

    override fun onChatDataLoading() {
        for (listener in myConnectionListener) {
            listener.onChatDataLoading()
        }
    }

    override fun onChatDataLoaded() {
        for (listener in myConnectionListener) {
            listener.onChatDataLoaded()
        }
    }

    override fun onKickedOffline() {
        for (listener in myConnectionListener) {
            listener.onKickedOffline()
        }
    }
}