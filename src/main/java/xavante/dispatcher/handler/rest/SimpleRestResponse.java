package xavante.dispatcher.handler.rest;

import org.jboss.netty.handler.codec.http.HttpResponseStatus;

public class SimpleRestResponse
	implements RestResponse
{
	private final HttpResponseStatus	status;

	private String						contentType;

	private String						payload;

	public SimpleRestResponse(HttpResponseStatus status)
	{
		this.status = status;
	}

	public SimpleRestResponse(HttpResponseStatus status, String contentType, String payload)
	{
		this.status      = status;
		this.contentType = contentType;
		this.payload     = payload;
	}

	@Override
	public HttpResponseStatus getStatus()
	{
		return status;
	}

	@Override
	public byte[] getBytes()
	{
		return payload == null ? null : payload.getBytes();
	}

	@Override
	public String getContentType()
	{
		return contentType;
	}
}
