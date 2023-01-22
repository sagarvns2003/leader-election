package me.vidya.leader.listener;

import org.jgroups.ChannelListener;
import org.jgroups.JChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LeaderChannelListener implements ChannelListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(LeaderChannelListener.class);

	private String clusterName;

	@Override
	public void channelConnected(JChannel channel) {
		this.clusterName = channel.getClusterName();
		LOGGER.info("Connected to {}", this.clusterName);
	}

	@Override
	public void channelDisconnected(JChannel channel) {
		LOGGER.info("Disconnected from {}", this.clusterName);
	}

	@Override
	public void channelClosed(JChannel channel) {
		LOGGER.info("Closed from {}", this.clusterName);
	}

}
