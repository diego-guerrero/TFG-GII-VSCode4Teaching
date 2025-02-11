import WebSocket from "ws";
import { v4tLogger } from "../services/LoggerService";
import { APIClientSession } from "./APIClientSession";

export class WebSocketV4TConnection {

    private ws: WebSocket | undefined;
    private wsTimeout: NodeJS.Timeout | undefined;
    private wsPingInterval: NodeJS.Timeout | undefined;

    constructor(private channel: string, private callback: ((data: any) => void)) {
        this.connect(this.channel, this.callback);
    }

    public send(data: any, cb?: (err?: Error) => void) {
        this.ws?.send(data, cb);
    }

    public close() {
        this.ws?.close();
    }

    private connect(channel: string, callback: ((data: any) => void)) {
        const authToken = APIClientSession.jwtToken;
        const wsURL = APIClientSession.baseUrl.replace("http", "ws");
        const startConnectionDate = new Date().getTime();
        if (authToken && wsURL) {
            this.ws = new WebSocket(`${ wsURL }/${ channel }?bearer=${ authToken }`);
            const wsHeartbeat = (websocket: WebSocket) => {
                v4tLogger.debug("ws ping " + this.channel + ": " + new Date(new Date().getTime() - startConnectionDate));
                if (this.wsTimeout) {
                    global.clearTimeout(this.wsTimeout);
                }
                // Delay should be equal to the interval at which your server
                // sends out pings plus a conservative assumption of the latency.
                this.wsTimeout = global.setTimeout(() => {
                    v4tLogger.warn("Timeout on websocket connection. Trying to reconnect...");
                    websocket.terminate();
                    this.connect(channel, callback);
                }, 31000);
            };
            this.ws.on("open", wsHeartbeat);
            this.ws.on("ping", wsHeartbeat);
            this.ws.on("pong", wsHeartbeat);
            this.ws.on("close", () => {
                if (this.wsTimeout) {
                    global.clearTimeout(this.wsTimeout);
                }
                if (this.wsPingInterval) {
                    global.clearInterval(this.wsPingInterval);
                }
            });
            this.ws.onmessage = (data) => {
                callback(data);
            };
            if (this.wsPingInterval) {
                global.clearInterval(this.wsPingInterval);
            }
            // Ping periodically to keep connection alive
            this.wsPingInterval = global.setInterval(() => {
                this.ws?.ping();
            }, 30000);
        } else {
            v4tLogger.error("Could not connect with websockets");
        }
    }
}
