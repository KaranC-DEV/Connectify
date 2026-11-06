import { Client } from "@stomp/stompjs";
export function connectRealtime(jwt, onState) {
  const client = new Client({
    brokerURL: import.meta.env.VITE_WS_URL || "ws://localhost:8080/ws",
    connectHeaders: { Authorization: `Bearer ${jwt}` },
    reconnectDelay: 3000,
    heartbeatIncoming: 10000,
    heartbeatOutgoing: 10000,
    debug: () => {},
    onConnect: () => onState("connected"),
    onDisconnect: () => onState("disconnected"),
    onWebSocketClose: () => onState("disconnected"),
    onStompError: (f) => {
      console.error(f.headers?.message, f.body);
      onState("error");
    },
  });
  client.activate();
  return client;
}
