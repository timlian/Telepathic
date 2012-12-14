package com.telepathic.finder.network.ksoap.sender;

import android.util.Log;
import com.telepathic.finder.network.ksoap.envelop.Envelop;
import com.telepathic.finder.network.ksoap.envelop.ParallelEnvelop;
import com.telepathic.finder.network.ksoap.envelop.SerialEnvelop;
import com.telepathic.finder.network.ksoap.message.ClientMessage;
import com.telepathic.finder.network.ksoap.message.MessageException;
import com.telepathic.finder.network.ksoap.message.listener.MessageSendListener;
import com.telepathic.finder.network.ksoap.message.server.ServerMessageContent;
import com.telepathic.finder.network.ksoap.soap.SoapSender;

import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;

public class MessageSender implements Sender
{
  private static final String TAG = "MessageSender";
  private boolean autoRetry = false;
  private Sender envelopSender;
  private boolean isCancel = false;
  private MessageSendListener listener;
  private long retryLantency = 30000L;
  private int retryTime = 3;
  private SoapSender sender;

  public MessageSender()
  {
  }

  public MessageSender(MessageSendListener paramMessageSendListener)
  {
    this.listener = paramMessageSendListener;
  }

  private MessageSendListener choseListener(ClientMessage paramClientMessage)
  {
    if (paramClientMessage.getListener() != null) {
        return paramClientMessage.getListener();
    } 
    if (this.listener != null) {
        return listener;
    }
//        for (MessageSendListener localMessageSendListener = paramClientMessage.getListener(); ; localMessageSendListener = this.listener)
//        {
//          return localMessageSendListener;
//          if (this.listener == null)
//            break;
//        }
        // Tim: do not know how to handle it
        //return null;
    //} 
    return null;
    //throw new RuntimeException("娌℃湁鍙敤鐨勭洃鍚櫒锛岃璁剧疆鐩戝惉鍣�);
  }

  private void send2(ClientMessage paramClientMessage)
  {
    Log.d("test", "message: " + paramClientMessage);
    while (true)
    {
      try
      {
        Object localObject = this.sender.send();
        Log.i("MessageSender", localObject.toString());
        if ((localObject instanceof SoapObject))
        {
          ServerMessageContent localServerMessageContent = new ServerMessageContent(paramClientMessage.getEntityClass(), (SoapObject)localObject);
          if (!this.isCancel)
          {
            choseListener(paramClientMessage).onMessageRecieved(localServerMessageContent.getServerMessages(), this, paramClientMessage);
            if (this.autoRetry)
            {
              boolean bool;
              if (!this.autoRetry) {
                  bool = true;
              }
              bool = false;
              this.autoRetry = bool;
            }
          }
        }
        else if ((localObject instanceof SoapFault))
        {
          throw new MessageException("SoapFault: " + localObject.toString());
        }
      }
      catch (Exception localException)
      {
        localException.printStackTrace();
        if (!this.isCancel)
        {
          Log.w("MessageSender", localException);
          Log.w("MessageSender", localException.getMessage());
          if (!choseListener(paramClientMessage).onError(new Exception("缃戠粶杩炴帴澶辫触锛岃閲嶈瘯"), this, paramClientMessage))
            localException.printStackTrace();
        }
        if ((this.autoRetry) && (this.retryTime > 0))
        {
          this.retryTime = (-1 + this.retryTime);
          try
          {
            //Log.i("MessageSender", paramClientMessage.getMethodName() + "杩涘叆Sender鑷姩" + this.retryLantency / 1000L + "閲嶈瘯鍙戦�璇锋眰锛�);
            Thread.sleep(this.retryLantency);
            send2(paramClientMessage);
          }
          catch (InterruptedException localInterruptedException)
          {
            localInterruptedException.printStackTrace();
          }
          //throw new MessageException("unknown exception during sending SOAP request.");
        }
      }
      return;
    }
  }

  public void cancel()
  {
    this.isCancel = true;
    if (this.envelopSender == null) {
        return ;
    } else  {
      this.envelopSender.cancel();
    }
  }

  public Sender getEnvelopSender()
  {
    return this.envelopSender;
  }

  public int getRunningSenderNumber()
  {
    if (this.envelopSender == null);
    for (int i = 0; ; i = this.envelopSender.getRunningSenderNumber())
      return i;
  }

  public MessageSender[] getRunningSenders()
  {
    if (this.envelopSender == null);
    for (MessageSender[] arrayOfMessageSender = null; ; arrayOfMessageSender = this.envelopSender.getRunningSenders())
      return arrayOfMessageSender;
  }

  public int getSendedCount()
  {
    if (this.envelopSender == null)
      throw new RuntimeException("娑堟伅娌℃湁琚彂閫侊紝璇峰厛鍙戦�娑堟伅");
    return this.envelopSender.getSendedCount();
  }

  public void send(Envelop paramEnvelop)
  {
    if (this.envelopSender == null)
    {
      if (!(paramEnvelop instanceof ParallelEnvelop)) {
          if ((paramEnvelop instanceof SerialEnvelop))
              this.envelopSender = new SerialEnvelopSender(this.listener);
      } else {
      this.envelopSender = new ParallelEnvelopSender(this.listener);
      }
    }
    this.envelopSender.send(paramEnvelop);
  }

  public void send(final ClientMessage paramClientMessage)
  {
    new Thread(new Runnable()
    {
      public void run()
      {
        MessageSender.this.sender = new SoapSender(paramClientMessage.getNamespace(), paramClientMessage.getMethodName(), paramClientMessage.getConnectionAddr());
        Log.d("test", "message.getConnectionAddr(): " + paramClientMessage.getConnectionAddr());
        MessageSender.this.sender.setParemeters(paramClientMessage.getParameters());
        MessageSender.this.send2(paramClientMessage);
      }
    }).start();
  }

  @Deprecated
  public void sendMessage(ClientMessage paramClientMessage)
  {
    send(paramClientMessage);
  }

  public void setAutoRetry(boolean paramBoolean)
  {
    this.autoRetry = paramBoolean;
  }

  public void setAutoRetryLatency(long paramLong)
  {
    this.retryLantency = paramLong;
  }

  public void setAutoRetryTime(int paramInt)
  {
    this.retryTime = paramInt;
  }

  public void setCancel(boolean paramBoolean)
  {
  }

  public void setEnvelopSender(Sender paramSender)
  {
    this.envelopSender = paramSender;
  }

  public void setListener(MessageSendListener paramMessageSendListener)
  {
    this.listener = paramMessageSendListener;
  }

}
