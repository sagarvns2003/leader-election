package me.vidya.leader.service;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import me.alexpanov.net.FreePortFinder;

public class LeaderElectionServiceTest {

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
	}

	@Test
	void leaderChangeTest() throws Exception {
		node1.connect();
		node2.connect();
		// log.info(node1.leaderInfo());
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