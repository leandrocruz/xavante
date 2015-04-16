package xavante;

import org.apache.commons.lang3.StringUtils;
import org.jboss.netty.channel.Channel;

import xingu.node.commons.signal.Signal;
import xingu.node.commons.signal.behavior.SignalBehavior;
import xingu.node.commons.signal.impl.SignalSupport;

public class Reverse
	extends SignalSupport
	implements SignalBehavior<Reverse>
{
	private String value;

	public String getValue(){return value;}
	public void setValue(String value){this.value = value;}
	
	@Override
	public Signal<?> perform(Reverse signal, Channel channel)
		throws Exception
	{
		this.value = StringUtils.reverse(value);
		return this;
	}
}
