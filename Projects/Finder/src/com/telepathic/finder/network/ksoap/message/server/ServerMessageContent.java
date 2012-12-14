package com.telepathic.finder.network.ksoap.message.server;

import org.ksoap2.serialization.SoapObject;

public class ServerMessageContent {
    private Class<?> entityClass;
    private SoapObject list;

    public ServerMessageContent(Class<?> paramClass, SoapObject paramSoapObject) {
        entityClass = paramClass;
        list = paramSoapObject;
    }

    public ServerMessage getServerMessages() {
        try {
            ServerMessage localServerMessage = (ServerMessage) entityClass.newInstance();
            localServerMessage.setContent(list);
            return localServerMessage;
        } catch (InstantiationException localInstantiationException) {
            throw new RuntimeException("服务端消息实体必须有无参的构造方法!");
        } catch (IllegalAccessException localIllegalAccessException) {
            throw new RuntimeException("服务端消息实体的构造方法必须是public的!");
        }
    }
}
