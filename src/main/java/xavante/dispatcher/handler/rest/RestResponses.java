package xavante.dispatcher.handler.rest;

import org.jboss.netty.handler.codec.http.HttpResponseStatus;

public class RestResponses
{
	public static final RestResponse InternalServerError(String payload)
	{
		return new SimpleRestResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR, "text/plain", payload);
	}

	public static final RestResponse Ok(String payload)
	{
		return new SimpleRestResponse(HttpResponseStatus.OK, "application/json", payload);
	}

	public static final RestResponse NoContent()
	{
		return new SimpleRestResponse(HttpResponseStatus.NO_CONTENT);
	}
}
