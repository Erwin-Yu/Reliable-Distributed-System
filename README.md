# Reliable_Distributed_System
CMU 18-794 project


Remaining Problem:

  <td><code class="highlighter-rouge">
1. Clients may be blocked by another Clients in sending Messages to the Server. ( ex. in a order by which the clients is established).
2. The "my_state" variable of server could be set to be any String from any client's terminal input.
</code></td>


| Messages in Directions  | String value |
| ------------- | ------------- |
| Messages from LSD to Server1 (in String)  | "heartBeat"  |
| Messages from Server1 to LSD (in String)  | "heartbeat message received"  |
| Message from Client1 to Server1 (in String)  | "<clientId, replicaId, requestNum, request, newStateValue>" (In 'messageTuple' Object <int, int, int, String, String> )  |
| Message from Server1 to Client1 (in String)  | "<clientId, replicaId, requestNum, reply, newStateValue>"  |
