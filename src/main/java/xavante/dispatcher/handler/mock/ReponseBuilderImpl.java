package xavante.dispatcher.handler.mock;



import xavante.XavanteRequest;
import xingu.http.client.HttpResponse;
import xingu.utils.collection.FluidMap;

public class ReponseBuilderImpl
	implements ReponseBuilder
{
	private String				path;

	private HttpResponse		response;

	private FluidMap<String>	parameters;

	public ReponseBuilderImpl(String path, FluidMap<String> parameters, HttpResponse response)
	{
		this.path       = path;
		this.response   = response;
		this.parameters = parameters;
	}

	@Override
	public String getPath()
	{
		return path;
	}

	@Override
	public HttpResponse handle(XavanteRequest req)
		throws Exception
	{
		return response;
	}

	@Override
	public FluidMap<String> getParameters()
	{
		return parameters;
	}
}