package com.telepathic.finder.network.ksoap.envelop;

import com.telepathic.finder.network.ksoap.message.ClientMessage;
import com.telepathic.finder.network.ksoap.message.listener.MessageSendListener;

public abstract interface Envelop
{
  public abstract boolean addEnvelop(Envelop paramEnvelop);

  public abstract <T extends ClientMessage> boolean addMessage(T paramT);

  public abstract int getAllMessageCount();

  public abstract int getMessageCount();

  public abstract MessageSendListener getMessageSendListener();

  public abstract void setMessageSendListener(MessageSendListener paramMessageSendListener);
}
