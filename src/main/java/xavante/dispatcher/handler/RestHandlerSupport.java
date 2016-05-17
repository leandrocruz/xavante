package xavante.dispatcher.handler;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpResponse;

import xavante.XavanteRequest;
import xavante.dispatcher.impl.RequestHandlerSupport;
import xingu.netty.http.HttpResponseBuilder;

public abstract class RestHandlerSupport
	extends RequestHandlerSupport
{
	public RestHandlerSupport(String path)
	{
		super(path);
	}

	@Override
	public void handle(XavanteRequest xeq)
		throws Exception
	{
		HttpResponse resp = HttpResponseBuilder
				.builder()
				.withHeader("Access-Control-Allow-Origin", "*")
				.build();
		
		String reply = null;
		try
		{
			reply = handleRequest(xeq, resp);
		}
		catch(InterruptedException e)
		{
			throw e;
		}
		catch(Throwable t)
		{
			reply = handleError(t);
		}
		finally
		{
			if(reply != null)
			{
				byte[]        bytes  = reply.getBytes();
				ChannelBuffer buffer = ChannelBuffers.wrappedBuffer(bytes);
				resp.setHeader(HttpHeaders.Names.CONTENT_LENGTH, bytes.length);
				resp.addHeader(HttpHeaders.Names.CONTENT_TYPE, "application/json");
				resp.setContent(buffer);
			}
		}
		
		xeq.write(resp).addListener(ChannelFutureListener.CLOSE);
	}

	protected abstract String handleError(Throwable t);
	

	protected abstract String handleRequest(XavanteRequest xeq, HttpResponse resp)
		throws Exception;
}
