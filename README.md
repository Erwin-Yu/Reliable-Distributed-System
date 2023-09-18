# Reliable_Distributed_System
CMU 18-794 project


Remaining Problem:


1. Clients may be blocked by another Clients in sending Messages to Server. ( ex. in a order by which the clients is established).
2.


Messages from LSD to Server1 (in String): "heartBeat"
Messages from Server1 to LSD (in String): "heartbeat message received"

Message from Client1 to Server1 (in String): "<clientId, replicaId, requestNum, request, newStateValue>" (In 'messageTuple' Object <int, int, int, String, String> )
Message from Server1 to Client1 (in String): "<clientId, replicaId, requestNum, reply, newStateValue>"
