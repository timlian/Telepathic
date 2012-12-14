package com.telepathic.finder.network.ksoap.message.listener;

import com.telepathic.finder.network.ksoap.message.ClientMessage;
import com.telepathic.finder.network.ksoap.message.server.ServerMessage;
import com.telepathic.finder.network.ksoap.sender.MessageSender;

public abstract interface MessageSendListener
{
  public abstract boolean onError(Exception paramException, MessageSender paramMessageSender, ClientMessage paramClientMessage);

  public abstract void onMessageRecieved(ServerMessage paramServerMessage, MessageSender paramMessageSender, ClientMessage paramClientMessage);
}
