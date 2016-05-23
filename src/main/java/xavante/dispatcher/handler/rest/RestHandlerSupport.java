package xavante.dispatcher.handler.rest;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

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
		final HttpRequest  request = xeq.getRequest();
		final HttpMethod   method  = request.getMethod();
		final List<String> headers = request.headers().getAll("Access-Control-Request-Headers");
		final HttpResponse resp    = HttpResponseBuilder
				.builder()
				.withHeader("Access-Control-Allow-Origin", "*")
				.withHeader("Access-Control-Allow-Headers", StringUtils.join(headers, ","))
				.build();

		if(HttpMethod.OPTIONS.equals(method))
		{
			xeq.write(resp).addListener(ChannelFutureListener.CLOSE);
			return;
		}

		RestResponse reply = null;
		try
		{
			reply = handleRequest(xeq);
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
				final HttpResponseStatus status      = reply.getStatus();
				final String             contentType = reply.getContentType();
				final byte[]             bytes       = reply.getBytes();
				final ChannelBuffer      buffer      = ChannelBuffers.wrappedBuffer(bytes);

				resp.setStatus(status);
				resp.headers().set(HttpHeaders.Names.CONTENT_LENGTH, bytes.length);
				resp.headers().set(HttpHeaders.Names.CONTENT_TYPE, contentType);
				resp.setContent(buffer);
				xeq.write(resp).addListener(ChannelFutureListener.CLOSE);
			}
		}
	}

	protected RestResponse handleError(Throwable t)
	{
		final String trace = ExceptionUtils.getStackTrace(t);
		return RestResponses.InternalServerError(trace);
	}

	protected abstract RestResponse handleRequest(XavanteRequest xeq)
		throws Exception;
}
