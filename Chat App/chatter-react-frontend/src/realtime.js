import { Client } from "@stomp/stompjs";

export function connectRealtime(jwt, onState) {
  if (!jwt) {
    onState?.("error");
    throw new Error("JWT token is required for WebSocket connection");
  }

  const wsUrl = import.meta.env.VITE_WS_URL || "ws://localhost:8080/ws";

  const client = new Client({
    brokerURL: wsUrl,

    connectHeaders: {
      Authorization: `Bearer ${jwt}`,
    },

    reconnectDelay: 3000,
    heartbeatIncoming: 10000,
    heartbeatOutgoing: 10000,

    debug: () => {},

    onConnect: () => onState?.("connected"),

    onDisconnect: () => onState?.("disconnected"),

    onWebSocketClose: () => onState?.("disconnected"),

    onWebSocketError: () => onState?.("error"),

    onStompError: (frame) => {
      console.error("STOMP error:", frame.headers?.message, frame.body);
      onState?.("error");
    },
  });

  client.activate();

  return client;
}
