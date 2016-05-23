package xavante.dispatcher.handler.rest;

import org.jboss.netty.handler.codec.http.HttpResponseStatus;

public interface RestResponse
{
	HttpResponseStatus getStatus();

	byte[] getBytes();

	String getContentType();
}
