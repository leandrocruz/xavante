package xavante.comet.impl;

import java.lang.reflect.Field;
import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xavante.XavanteMessage;
import xavante.comet.CometSession;
import xavante.comet.CometHandler;
import xavante.comet.CometMessage;
import xavante.comet.SessionManager;
import xingu.codec.Codec;
import xingu.container.Inject;
import xingu.lang.NotImplementedYet;
import xingu.node.commons.identity.Identity;
import xingu.node.commons.signal.ErrorSignal;
import xingu.node.commons.signal.Signal;
import xingu.node.commons.signal.behavior.BehaviorPerformer;
import xingu.node.commons.signal.impl.ExceptionSignal;
import xingu.utils.FieldUtils;
import xingu.utils.StringUtils;

public abstract class CometHandlerSupport
	implements CometHandler
{
	@Inject("xavante")
	protected Codec				codec;

	@Inject
	protected SessionManager	sessions;

	@Inject
	protected BehaviorPerformer	performer;

	private Logger				logger	= LoggerFactory.getLogger(getClass());
	
	@Override
	public String onMessage(CometMessage msg)
		throws Exception
	{
		String cmd = msg.getCommand();
		logger.debug("Received command:{}, sequence:{}, token:{} command", cmd, msg.getSequence(), msg.getToken());
		logger.debug("{}", msg.getData());
		if("con".equalsIgnoreCase(cmd))
		{
			return connect(msg);
		}
		else if("drn".equalsIgnoreCase(cmd))
		{
			return drain(msg);
		}
		else if("snd".equalsIgnoreCase(cmd))
		{
			return send(msg);
		}
		else if("rsm".equalsIgnoreCase(cmd))
		{
			return resume(msg);
		}
		else
		{
			throw new NotImplementedYet("Can't execute '" + cmd + "'");
		}
	}

	private String connect(CometMessage msg)
		throws Exception
	{
		String       token   = msg.getToken();
		CometSession session = null;
		
		if(StringUtils.isNotEmpty(token))
		{
			session = sessions.byId(token);
			if(session == null)
			{
				//server restart
			}
		}

		if(session == null)
		{
			session = sessions.newSession();
		}
		return "{\"type\": \"OK\", \"sessionId\":\"" + session.getId() + "\"}";
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected String send(CometMessage msg)
		throws Exception
	{
		/*
		 * TODO: issue #354 not all messages should have waiters. Free
		 * threads/resources asap
		 */
		String seq   = msg.getSequence();
		String token = msg.getToken();
		String data  = msg.getData();

		Object decoded = codec.decode(data);
		if(decoded instanceof Signal)
		{
			Signal signal = (Signal) decoded;
			injectXavanteMessage(signal, msg);

			String       id      = signal.getSignalId();
			boolean      enabled = signal.isProcessEnabled();
			CometSession session = sessions.byId(token);
			Identity     owner   = session == null ? null : session.getIdentity();

			if(owner == null && !enabled)
			{
				/* All signals require an owner, except for Login */
				throw new NotImplementedYet("Owner is null on signal: '" + signal + "'");
			}

			Signal reply = null;
			signal.setOwner(owner);
			
			boolean isOwner = sessions.verifyOwnership(signal);
			if(isOwner)
			{
				try
				{
					reply = performer.performBehavior(signal, null);
				}
				catch(Throwable t)
				{
					logger.error("Error Performing Behavior for Signal " + signal.getClass().getName(), t);
					String trace = ExceptionUtils.getStackTrace(t);
					reply = new ExceptionSignal(t.getMessage(), trace);
				}
			}
			else
			{
				reply = new ErrorSignal("NOT_OWNER", "Not owner");
			}

			if(reply != null)
			{
				reply.setSignalId(id);
				String encoded = codec.encode(reply);
				return encoded;
			}
			else
			{
				return "{}";
			}
		}
		else
		{
			throw new NotImplementedYet("'" + data + "' is not a signal");
		}
	}

	protected void injectXavanteMessage(Signal<?> signal, CometMessage msg)
		throws Exception
	{
        List<Field> fields = FieldUtils.getAllFields(signal.getClass());
        for (Field field : fields)
        {
        	XavanteMessage inject = field.getAnnotation(XavanteMessage.class);
            if(inject != null)
            {
                boolean before = field.isAccessible();
                field.setAccessible(true);
                field.set(signal, msg);
                field.setAccessible(before);
            }
        }
	}

	protected String resume(CometMessage msg)
		throws Exception
	{
		String       token   = msg.getToken();
		CometSession session = sessions.byId(token);
		if(session != null)
		{
			return "{\"type\": \"OK\", \"sessionId\":\"" + session.getId() + "\"}";
		}
		else
		{
			return "{}";
		}
	}

	protected String drain(CometMessage msg)
		throws Exception
	{
		String       hash    = msg.getToken();
		CometSession session = sessions.byId(hash);
		if(session == null)
		{
			return "{\"session_error\":true, \"message\":\"session "+hash+" not found\"}";
		}

		Object[] messages = session.drain();
		if(messages == null)
		{
			// Thread interrupted
			return "{\"session_error\":true, \"message\":\"no messages\"}";
		}

		StringBuffer sb = toString(messages);
		return sb.toString();
	}

	private StringBuffer toString(Object[] messages)
		throws Exception
	{
		StringBuffer sb = new StringBuffer();
		sb.append("{\"len\":").append(messages.length).append(", \"data\": [");

		int i = 0;
		for(Object msg : messages)
		{
			i++;
			String encoded = codec.encode(msg);
			sb.append(encoded);
			if(i < messages.length)
			{
				sb.append(", ");
			}
		}
		sb.append("]}");
		return sb;
	}

	@Override
	public String onError(CometMessage msg, Throwable t)
	{
		logger.error("Error Handing Comet Message", t);
		String trace = ExceptionUtils.getStackTrace(t);
		String id = msg == null ? "" : msg.getSequence(); 
		
		try
		{
			ExceptionSignal signal = new ExceptionSignal(t.getMessage(), trace);
			signal.setSignalId(id);
			return codec.encode(signal);
		}
		catch(Exception e)
		{
			return "{\"signalId\":\""+id+"\",\"error\": true ,\"message\":\""+e.getMessage()+"\"}";
		}
	}
}