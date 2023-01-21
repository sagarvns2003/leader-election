# leader-election

A light weight leader election library based on JGroups (http://www.jgroups.org/) which uses reliable TCP protocol for their node communication.

#How to use in your application...
```
Integer communicationPort = 3030;   //Node will use this port to communicate other nodes. This must not be your application port number;
String communicationChannelName = "testChannelName"; //Node will use this channel to communicate other nodes. Recommend to provide application name as channel name
String currentHostname = "qcl2009566.bk.cloud.in.abc";  //Current host name where application is going to deploy.
List<String> memberHostnames = List.of("qcl2009566.bk.cloud.in.abc", "qcl2009567.bk.cloud.in.abc", "qcl2009568.bk.cloud.in.abc");  //All host names where the same application is going to deploy

LeaderElectionService leaderElectionService = new LeaderElectionService(communicationPort, communicationChannelName, currentHostname, memberHostnames);
leaderElectionService.connect();  //Node has joined the cluster

Now you can use...
leaderElectionService.isLeader() method as per your need.

```

#Log recommendation
Redirect jgroups logs to separate file for easy tracking in your application. This package should be redirected... org.jgroups & me.vidya.leader