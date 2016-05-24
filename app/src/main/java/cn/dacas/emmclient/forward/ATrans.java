package cn.dacas.emmclient.forward;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Set;

import cn.dacas.emmclient.util.NetworkDef;

public class ATrans {
	private static final int BUFF_SIZE = 256;

	// 本地端口和服务器的端口和地址
	private int localPort = -1;
	private String serverAddr = null;
	private int serverPort = -1;

	private ServerSocket s = null;

	private boolean isValidTrans = true;

	// 来自app的client socket对应到连接服务器的client socket
	HashMap<Socket, Socket> socketMapping = null;

	public ATrans(int localPort, String serverAddr, int serverPort) {
		this.localPort = localPort;
		this.serverAddr = serverAddr;
		this.serverPort = serverPort;
		this.socketMapping = new HashMap<Socket, Socket>();
	}

	public void execute() {
		if (this.localPort <= -1 || this.serverAddr == null
				|| this.serverPort <= -1) {
			return;
		}
		isValidTrans = true;

		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					s = new ServerSocket(localPort);
				} catch (Exception e1) {
					e1.printStackTrace();
					try {
						s.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
					return;
				}

				while (isValidTrans) {
					try {
						String ipAndPortRaw = NetworkDef
								.getAvailableForwardIp();
						if (ipAndPortRaw == null)
							continue;
						String ipAndport[] = ipAndPortRaw.split(":");
						String ip = ipAndport[0];
						int port = Integer.valueOf(ipAndport[1]);
						// 接收到来自app的socket
						Socket appClient = s.accept();

						// 新建到中转的client
						Socket toForward = new Socket(ip, port);

						socketMapping.put(appClient, toForward);

						Thread forwardThread = new ForwardingThread(appClient,
								toForward);
						forwardThread.start();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});
		thread.start();
	}

	public void stopExecution() {
		this.isValidTrans = false;

		try {
			s.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		Set<Socket> appClients = socketMapping.keySet();

		for (Socket client : appClients) {
			Socket server = socketMapping.get(client);
			socketMapping.remove(client);
			try {
				client.close();
				server.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private class ForwardingThread extends Thread {
		private Socket appClient = null;
		private InputStream appClientIn = null;
		private OutputStream appClientOut = null;

		private Socket toForward = null;
		private InputStream toForwardIn = null;
		private OutputStream toForwardOut = null;

		public ForwardingThread(Socket appClient, Socket toForward) {
			this.appClient = appClient;
			this.toForward = toForward;

			try {
				this.appClientIn = this.appClient.getInputStream();
				this.appClientOut = this.appClient.getOutputStream();

				this.toForwardIn = this.toForward.getInputStream();
				this.toForwardOut = this.toForward.getOutputStream();
			} catch (IOException e) {
				e.printStackTrace();
				clearSocket();
			}
		}

		public void run() {
			if (this.appClient.isClosed() || this.toForward.isClosed()) {
				return;
			}

			try {
				// TODO 需要进行编码变换
				this.toForwardOut.write((serverAddr + ":" + String
						.valueOf(serverPort)).getBytes());
			} catch (IOException e) {
				e.printStackTrace();
				clearSocket();
				return;
			}

			byte[] reply = new byte[8];
			int readSize = 0;

			try {
				readSize = this.toForwardIn.read(reply);
			} catch (IOException e) {
				e.printStackTrace();
				clearSocket();
				return;
			}

			if (readSize <= 0) {
				clearSocket();
				return;
			}

			// TODO 需要对reply进行编码变换
			try {
				String replyStr = new String(reply, 0, readSize, "US-ASCII");
				if (!replyStr.startsWith("OK")) {
					clearSocket();
					return;
				}
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			// 监听toForward发送过来的消息，然后通过appClient转发出去
			new Thread(new Runnable() {

				@Override
				public void run() {
					byte[] buf = null;
					while (isValidTrans) {
						buf = new byte[BUFF_SIZE];
						int readSize = 0;
						try {
							readSize = toForwardIn.read(buf);
						} catch (IOException e) {
							e.printStackTrace();
							clearSocket();
							return;
						}

						if (readSize > 0) {
							try {
								appClientOut.write(buf);
							} catch (IOException e) {
								clearSocket();
								e.printStackTrace();
							}
						}
					}
				}
			}).start();

			// 监听appClient发送过来的消息,然后通过toForward转发出去
			byte[] buf = null;
			while (isValidTrans) {
				buf = new byte[BUFF_SIZE];
				int read = 0;
				try {
					read = this.appClientIn.read(buf);
				} catch (IOException e) {
					e.printStackTrace();
					clearSocket();
					return;
				}

				if (read > 0) {
					try {
						this.toForwardOut.write(buf);
					} catch (IOException e) {
						e.printStackTrace();
						clearSocket();
						return;
					}
				}
			}
		}

		private void clearSocket() {
			socketMapping.remove(this.appClient);
			try {
				this.appClientIn.close();
				this.appClientOut.close();
				this.toForwardIn.close();
				this.toForwardOut.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			try {
				this.appClient.close();
				this.toForward.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
