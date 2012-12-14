package com.telepathic.finder.network.ksoap.sender;

import com.telepathic.finder.network.ksoap.envelop.Envelop;
import com.telepathic.finder.network.ksoap.envelop.ParallelEnvelop;
import com.telepathic.finder.network.ksoap.envelop.SerialEnvelop;
import com.telepathic.finder.network.ksoap.message.ClientMessage;
import com.telepathic.finder.network.ksoap.message.listener.MessageSendListener;
import java.util.ArrayList;
import java.util.Iterator;

public class SerialEnvelopSender
  implements Sender
{
  private boolean isSend;
  private MessageSendListener listener;
  private int sendedCount;
  private int sendingCount;

  public SerialEnvelopSender()
  {
  }

  public SerialEnvelopSender(MessageSendListener paramMessageSendListener)
  {
    this.listener = paramMessageSendListener;
  }

  private void send1(Envelop paramEnvelop)
  {
    if (!(paramEnvelop instanceof SerialEnvelop))
      throw new RuntimeException("消息集装箱不正确");
    this.isSend = true;
    Iterator localIterator = ((SerialEnvelop)paramEnvelop).getSendContent().iterator();
    while (true)
    {
      if ((!localIterator.hasNext()) || (!this.isSend))
        return;
      Object localObject = localIterator.next();
      if ((localObject instanceof SerialEnvelop))
      {
        send1((SerialEnvelop)localObject);
        this.sendingCount = 1;
      }
      else if ((localObject instanceof ParallelEnvelop))
      {
        ParallelEnvelopSender localParallelEnvelopSender = new ParallelEnvelopSender(this.listener);
        localParallelEnvelopSender.send((ParallelEnvelop)localObject);
        this.sendedCount += localParallelEnvelopSender.getSendedCount();
        this.sendingCount = localParallelEnvelopSender.getRunningSenderNumber();
      }
      else
      {
        send((ClientMessage)localObject);
        this.sendingCount = 1;
        this.sendedCount = (1 + this.sendedCount);
      }
    }
  }

  public void cancel()
  {
    this.isSend = false;
  }

  public int getRunningSenderNumber()
  {
    return this.sendingCount;
  }

  public MessageSender[] getRunningSenders()
  {
    return null;
  }

  public int getSendedCount()
  {
    return this.sendedCount;
  }

  public int getSenderCount()
  {
    return this.sendingCount;
  }

  public void send(final Envelop paramEnvelop)
  {
    final SerialEnvelop localSerialEnvelop = (SerialEnvelop)paramEnvelop;
    if (!this.isSend)
      new Thread(new Runnable()
      {
        public void run()
        {
          SerialEnvelopSender.this.send1(paramEnvelop);
          localSerialEnvelop.getSendContent().remove(paramEnvelop);
        }
      }).start();
  }

  public void send(ClientMessage paramClientMessage)
  {
    new MessageSender(this.listener).send(paramClientMessage);
  }

  public void setSenderCount(int paramInt)
  {
    this.sendingCount = paramInt;
  }
}
