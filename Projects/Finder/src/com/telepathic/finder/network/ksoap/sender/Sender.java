package com.telepathic.finder.network.ksoap.sender;


import com.telepathic.finder.network.ksoap.envelop.Envelop;
import com.telepathic.finder.network.ksoap.message.ClientMessage;

public abstract interface Sender
{
  public abstract void cancel();

  public abstract int getRunningSenderNumber();

  public abstract MessageSender[] getRunningSenders();

  public abstract int getSendedCount();

  public abstract void send(Envelop paramEnvelop);

  public abstract void send(ClientMessage paramClientMessage);
}
