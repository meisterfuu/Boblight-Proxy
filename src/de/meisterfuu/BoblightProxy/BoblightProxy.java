package de.meisterfuu.BoblightProxy;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class BoblightProxy {

	private final ServerSocket server;

	static ArrayList<String> Lights = new ArrayList<String>();
	static ArrayList<String> Lights_Raw = new ArrayList<String>();

	static String BOB_IP;
	static int BOB_PORT;

	static Socket bob_sock;
	static OutputStreamWriter bob_out;
	static InputStreamReader bob_in;
	static BufferedWriter bob_writer;
	static BufferedReader bob_reader;


	public BoblightProxy(int port) throws IOException {
		server = new ServerSocket(port);
	}


	private void startServing() {
		while (true) {
			Socket client = null;
			try {
				client = server.accept();
				System.out.println("New Client");
				Thread t = new Thread(new ClientConn(client));
				t.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	class ClientConn implements Runnable {

		private Socket client;


		ClientConn(Socket client) {
			this.client = client;
		}


		@Override
		public void run() {
			try {
				InputStreamReader in = new InputStreamReader(client.getInputStream());
				OutputStreamWriter out = new OutputStreamWriter(client.getOutputStream());
				BufferedReader reader = new BufferedReader(in);
				BufferedWriter writer = new BufferedWriter(out);

				String line;

				if (reader.readLine().startsWith("hello")) {
					System.out.println("New Client said hello");
					writer.write("hello");
					writer.newLine();
					writer.flush();
				} else
					throw new IOException();
				
				while ((line = reader.readLine()) != null) {

					if (line.startsWith("get version")) {
						writer.write("version 5");
						writer.newLine();
						writer.flush();
					}
					else if (line.startsWith("get lights")) {
						writer.write("lights " + Lights.size());
						writer.newLine();
						writer.flush();
						for (int i = 0;i < Lights.size(); i++){
							writer.write(Lights_Raw.get(i));
							writer.newLine();
							writer.flush();
						}
					} else {
						bob_writer.write(line);
						bob_writer.newLine();
						bob_writer.flush();
					}

				}
			} catch (IOException e) {
				try {
					client.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				e.printStackTrace();
			}
		}
	}


	public static void main(String[] args) throws IOException {
		try {


			String answer;

			int PORT = 8844;
			if (args.length == 0) {
				BOB_IP = "127.0.0.1";
				BOB_PORT = 19333;
			} else if (args.length == 1) {
				BOB_IP = args[0];
				BOB_PORT = 19333;
			} else if (args.length == 2) {
				BOB_IP = args[0];
				BOB_PORT = Integer.parseInt(args[1]);
			} else if (args.length == 3) {
				BOB_IP = args[0];
				BOB_PORT = Integer.parseInt(args[1]);
				PORT = Integer.parseInt(args[2]);
			} else {
				System.err.println("Arguments [BOBLIGHT IP] [BOBLIGHT PORT] [PORT]");
			}

			System.out.println("Connecting...");

			bob_sock = new Socket(BOB_IP, BOB_PORT);
			bob_out = new OutputStreamWriter(bob_sock.getOutputStream());
			bob_in = new InputStreamReader(bob_sock.getInputStream());
			bob_writer = new BufferedWriter(bob_out);
			bob_reader = new BufferedReader(bob_in);

			System.out.println("Say \"Hello\"");
			bob_writer.write("hello");
			bob_writer.newLine();
			bob_writer.flush();
			answer = bob_reader.readLine();
			System.out.println(answer);
			System.out.println("");

			System.out.println("get Version");
			bob_writer.write("get version");
			bob_writer.newLine();
			bob_writer.flush();
			answer = bob_reader.readLine();
			System.out.println(answer);
			System.out.println("");

			System.out.println("get lights");
			bob_writer.write("get lights");
			bob_writer.newLine();
			bob_writer.flush();
			answer = bob_reader.readLine();
			System.out.println(answer);
			int lights_amount = 2;
			try {
				lights_amount = Integer.parseInt(answer.split(" ")[1]);
				for (int i = 0; i < lights_amount; i++) {
					answer = bob_reader.readLine();
					Lights_Raw.add(answer);
					answer = answer.split(" ")[1];
					Lights.add(answer);
					System.out.println("Light " + (i + 1) + ": " + Lights.get(Lights.size() - 1));
				}
				System.out.println("Got all lights");
			} catch (NumberFormatException e) {
				System.err.println("Get lights failed.");
			}
			System.out.println("");

			System.out.println("initial priority to 128");
			bob_writer.write("set priority 128");
			bob_writer.newLine();
			bob_writer.flush();

			BoblightProxy server = null;
			server = new BoblightProxy(PORT);
			server.startServing();
			// daemonize();
		} catch (NumberFormatException e) {
			System.err.println("Invalid Port. Has to be a number.");
		} catch (Throwable e) {
			System.err.println("Startup failed");
			e.printStackTrace();
		}
	}


	public static String parseLight(String x) {
		return x;
	}


	public static void daemonize() {
		System.out.close();
		System.err.close();
	}

}