package io.github.sagarvns2003.leader.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.jgroups.Message;
import org.jgroups.ObjectMessage;
import org.jgroups.Receiver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.alexpanov.net.FreePortFinder;

class LeaderElectionServiceTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(LeaderElectionServiceTest.class);

	Object receivedMessageBody;

	int port1 = FreePortFinder.findFreeLocalPort();
	int port2 = FreePortFinder.findFreeLocalPort();

	LeaderElectionService node1 = new LeaderElectionService(port1, "testChannelName", "localhost",
			List.of("localhost[" + port1 + "]", "localhost[" + port2 + "]"));

	LeaderElectionService node2 = new LeaderElectionService(port2, "testChannelName", "localhost",
			List.of("localhost[" + port1 + "]", "localhost[" + port2 + "]"));

	@AfterEach
	void cleanUp() {
		node1.disconnect();
		node2.disconnect();
		receivedMessageBody = null;
	}

	@Test
	void broadcastMessageTest() throws Exception {

		Receiver receiver = new Receiver() {
			@Override
			public void receive(Message msg) {
				receivedMessageBody = msg.getObject();
				LOGGER.info("New message: {} recieved from: {}", msg.getObject(), msg.getSrc());
			}
		};

		node1.connect(receiver);
		node2.connect(receiver);

		Message message = new ObjectMessage(null, "abc");
		node1.broadcast(message);
		
		Thread.sleep(500);
		
		assertNotNull(receivedMessageBody);
		assertEquals("abc", receivedMessageBody);
	}

	@Test
	void leaderChangeTest() throws Exception {
		node1.connect();
		node2.connect();
		assertTrue(node1.isLeader());

		node1.disconnect();
		Thread.sleep(1000);
		assertTrue(node2.isLeader());

		node1.connect();
		Thread.sleep(1000);
		assertTrue(node2.isLeader());

		node2.disconnect();
		Thread.sleep(1000);
		assertTrue(node1.isLeader());
	}

	@Test
	void leaderWhenOnlyNode1StartedTest() throws Exception {
		node1.connect();
		assertTrue(node1.isLeader());
	}

	@Test
	void leaderWhenOnlyNode2StartedTest() throws Exception {
		node2.connect();
		assertTrue(node2.isLeader());
	}
}