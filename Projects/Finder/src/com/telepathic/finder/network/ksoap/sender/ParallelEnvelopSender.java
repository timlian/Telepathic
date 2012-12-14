package com.telepathic.finder.network.ksoap.sender;

import com.telepathic.finder.network.ksoap.envelop.Envelop;
import com.telepathic.finder.network.ksoap.envelop.ParallelEnvelop;
import com.telepathic.finder.network.ksoap.envelop.SerialEnvelop;
import com.telepathic.finder.network.ksoap.message.ClientMessage;
import com.telepathic.finder.network.ksoap.message.listener.MessageSendListener;
import java.util.ArrayList;
import java.util.Iterator;

public class ParallelEnvelopSender implements Sender
{
  private boolean isSend;
  private MessageSendListener listener;
  private int sendedCount;
  private int sendingCount;

  public ParallelEnvelopSender()
  {
  }

  public ParallelEnvelopSender(MessageSendListener paramMessageSendListener)
  {
    this.listener = paramMessageSendListener;
  }

  private void send1(ClientMessage paramClientMessage)
  {
    new MessageSender(this.listener).send(paramClientMessage);
  }

  private void setSendedCount(int paramInt)
  {
    try
    {
      this.sendedCount = paramInt;
      return;
    } finally {
        // Tim : it's not clear, why use try finally, so comment out this code
    }
//    finally
//    {
//      localObject = finally;
//      throw localObject;
//    }
  }

  private void setSendingCount(int paramInt)
  {
    try
    {
      this.sendingCount = paramInt;
      return;
    } finally {
     // Tim : it's not clear, why use try finally, so comment out this code
    }
//    finally
//    {
//      localObject = finally;
//      throw localObject;
//    }
  }

  public void cancel()
  {
    this.isSend = false;
  }

  public MessageSendListener getListener()
  {
    return this.listener;
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

  public int getSendingCount()
  {
    return this.sendingCount;
  }

  public void send(final Envelop paramEnvelop)
  {
    this.isSend = true;
    if (!(paramEnvelop instanceof ParallelEnvelop))
      throw new RuntimeException("消息集装箱不正确");
    final ParallelEnvelop localParallelEnvelop = (ParallelEnvelop)paramEnvelop;
    Iterator localIterator = localParallelEnvelop.getSendContent().iterator();
    while (true)
    {
      if ((!localIterator.hasNext()) || (!this.isSend))
        return;
      new Thread(new Runnable()
      {
        public void run()
        {
          if ((paramEnvelop instanceof SerialEnvelop))
          {
            SerialEnvelopSender localSerialEnvelopSender = new SerialEnvelopSender(ParallelEnvelopSender.this.listener);
            localSerialEnvelopSender.send((SerialEnvelop)paramEnvelop);
            ParallelEnvelopSender.this.setSendingCount(ParallelEnvelopSender.this.sendingCount + localSerialEnvelopSender.getRunningSenderNumber());
            localParallelEnvelop.getSendContent().remove(paramEnvelop);
            return;
          }
          else
          {
            if ((paramEnvelop instanceof ParallelEnvelop))
            {
              new ParallelEnvelopSender(ParallelEnvelopSender.this.listener).send((ParallelEnvelop)paramEnvelop);
            }
            else
            {
              ParallelEnvelopSender.this.setSendingCount(1 + ParallelEnvelopSender.this.sendingCount);
              ParallelEnvelopSender.this.send1((ClientMessage)paramEnvelop);
              ParallelEnvelopSender.this.setSendingCount(-1 + ParallelEnvelopSender.this.sendingCount);
              ParallelEnvelopSender.this.setSendedCount(1 + ParallelEnvelopSender.this.sendedCount);
            }
          }
        }
      }).start();
    }
  }

  public void send(ClientMessage paramClientMessage)
  {
    new MessageSender(this.listener).send(paramClientMessage);
  }

  public void setListener(MessageSendListener paramMessageSendListener)
  {
    this.listener = paramMessageSendListener;
  }
}
