package me.vidya.leader.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Receiver;
import org.jgroups.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.RequiredArgsConstructor;

/**
 * This LeaderElectionService class is having
 * 
 * @since 0.0.1
 * @author Vidya Sagar Gupta
 */
@RequiredArgsConstructor
public class LeaderElectionService {

	private static final Logger LOGGER = LoggerFactory.getLogger(LeaderElectionService.class);

	private final Integer communicationPort;
	private final String communicationChannelName;
	private final String currentHostname;
	private final List<String> memberHostnames;

	private JChannel channel = null;

	public boolean isConnected() {
		return Objects.nonNull(this.channel) && this.channel.isConnected();
	}

	public void connect() throws Exception {
		Receiver receiver = new Receiver() {
			@Override
			public void viewAccepted(View newView) {
				LOGGER.info("Leader election members changed now... {}", newView.getMembers());
				LOGGER.info(leaderInfo());
			}
		};
		this.connect(receiver);
	}

	/*
	 * This method is to connect to the cluster of the nodes
	 */
	public void connect(Receiver receiver) throws Exception {
		if (Objects.nonNull(channel)) {
			channel.disconnect();
		}
		// Create new JChannel
		channel = new JChannel(this.prepareConfiguration());
		channel.setName(this.currentHostname + "[" + this.communicationPort + "]");
		channel.setReceiver(receiver);
		channel.connect(this.communicationChannelName);

		// Added hook to disconnect this node when getting shutdown
		try {
			Runtime.getRuntime().addShutdownHook(new Thread(this::disconnect));
		} catch (Exception e) {
		}
	}

	public Address getLeader() {
		if (Objects.isNull(this.channel)) {
			return null;
		}
		// Get current view of connected clusters
		View view = this.channel.getView();
		if (Objects.isNull(view)) {
			return null;
		}
		// Get leader which is on first place
		return view.getMembers().get(0);
	}

	public boolean isLeader() {
		Address leader = this.getLeader();
		if (Objects.isNull(this.channel.address()) || Objects.isNull(leader)) {
			return false;
		}
		boolean isLeader = this.channel.address().equals(leader);
		return isLeader;
	}

	public String leaderInfo() {
		boolean isLeader = this.isLeader();
		Address leader = this.getLeader();
		String leaderInfo = String.format("This node is %sA Leader. Current leader: %s", (isLeader ? "" : "NOT "),
				leader);
		LOGGER.debug(leaderInfo);
		return leaderInfo;
	}

	public void disconnect() {
		LOGGER.info("Disconnecting...");
		if (Objects.nonNull(this.channel)) {
			channel.close();
			LOGGER.info("Disconnected and closed.");
		}
	}

	private InputStream prepareConfiguration() {
		try (InputStream templateInputStream = getClass()
				.getResourceAsStream("/jgroup/tcp-leader-election-config.xml")) {
			String templateData = new String(templateInputStream.readAllBytes());
			String allMemberHostnames = this.memberHostnames.stream().filter(Objects::nonNull)
					.map(hostName -> hostName.contains("[") ? hostName : hostName + "[" + this.communicationPort + "]")
					.collect(Collectors.joining(","));
			return new ByteArrayInputStream(templateData.replaceAll("###currentHostname###", this.currentHostname)
					.replaceAll("###communicationPort###", String.valueOf(this.communicationPort))
					.replaceAll("###memberHostnames###", allMemberHostnames).getBytes());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}