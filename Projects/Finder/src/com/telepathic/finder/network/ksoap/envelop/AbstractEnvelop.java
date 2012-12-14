package com.telepathic.finder.network.ksoap.envelop;

import com.telepathic.finder.network.ksoap.message.ClientMessage;
import com.telepathic.finder.network.ksoap.message.listener.MessageSendListener;
import java.util.ArrayList;
import java.util.Iterator;

public abstract class AbstractEnvelop
  implements Envelop
{
  private int count = 0;
  private MessageSendListener listener;
  private ArrayList<Object> sendContent = new ArrayList();

  private void checkAndSetListenerOnMessage(ClientMessage paramClientMessage)
  {
    if ((paramClientMessage.getListener() == null) && (this.listener != null))
      paramClientMessage.setListener(this.listener);
  }

  private void checkAndSetListenerOnSubenvelop(Envelop paramEnvelop)
  {
    if ((paramEnvelop.getMessageSendListener() == null) && (this.listener != null))
      paramEnvelop.setMessageSendListener(this.listener);
  }

  public boolean addEnvelop(Envelop paramEnvelop)
  {
    if ((paramEnvelop != null) && (!this.sendContent.contains(paramEnvelop)))
    {
      this.sendContent.add(paramEnvelop);
      checkAndSetListenerOnSubenvelop(paramEnvelop);
    }
    for (boolean bool = true; ; bool = false)
      return bool;
  }

  public <T extends ClientMessage> boolean addMessage(T paramT)
  {
    if ((this.sendContent != null) && (!this.sendContent.contains(paramT)))
    {
      this.sendContent.add(paramT);
      this.count = (1 + this.count);
      if ((paramT.getListener() == null) && (this.listener != null))
        paramT.setListener(this.listener);
    }
    for (boolean bool = true; ; bool = false)
      return bool;
  }

  public int getAllMessageCount()
  {
    int i = this.count;
    Iterator localIterator = this.sendContent.iterator();
    while (true)
    {
      if (!localIterator.hasNext())
        return i;
      Object localObject = localIterator.next();
      if ((localObject instanceof Envelop))
        i += ((Envelop)localObject).getAllMessageCount();
    }
  }

  public int getMessageCount()
  {
    return this.count;
  }

  public MessageSendListener getMessageSendListener()
  {
    return this.listener;
  }

  public ArrayList<Object> getSendContent()
  {
    return this.sendContent;
  }

  public void setMessageSendListener(MessageSendListener paramMessageSendListener)
  {
    Iterator localIterator;
    if (paramMessageSendListener != null)
    {
      this.listener = paramMessageSendListener;
      localIterator = this.sendContent.iterator();
      while (localIterator.hasNext()) {
        Object localObject = localIterator.next();
        if ((localObject instanceof ClientMessage))
        {
          checkAndSetListenerOnMessage((ClientMessage)localObject);
        }
        else
        {
          if (!(localObject instanceof Envelop))
            break;
          checkAndSetListenerOnSubenvelop((Envelop)localObject);
        }
      }
    } 
    throw new RuntimeException("数据内容错误");
}
}
