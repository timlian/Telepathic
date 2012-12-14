package com.telepathic.finder.network.ksoap.message;

import com.telepathic.finder.network.ksoap.message.listener.MessageSendListener;
import com.telepathic.finder.network.ksoap.message.server.ServerMessage;
import com.telepathic.finder.network.ksoap.sender.MessageSender;

public abstract class ClientMessage extends AbstractMessage
{
  private String connectionAddr = "http://client.10628106.com:4800/TrafficService.asmx";
  private MessageSendListener listener;
  private String namespace = "http://tempuri.org/";

  public ClientMessage()
  {
  }

  public ClientMessage(MessageSendListener paramMessageSendListener)
  {
    this.listener = paramMessageSendListener;
  }

  public String getConnectionAddr()
  {
    return this.connectionAddr;
  }

  public abstract Class<? extends ServerMessage> getEntityClass();

  public MessageSendListener getListener()
  {
    return this.listener;
  }

  public abstract String getMethodName();

  public String getNamespace()
  {
    return this.namespace;
  }

  @Deprecated
  public void send(MessageSendListener paramMessageSendListener)
  {
    new MessageSender(paramMessageSendListener).sendMessage(this);
  }

  protected void setConectionProperty(String paramString1, String paramString2)
  {
    this.namespace = paramString1;
    this.connectionAddr = paramString2;
  }

  public void setListener(MessageSendListener paramMessageSendListener)
  {
    this.listener = paramMessageSendListener;
  }
}