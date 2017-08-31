package main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.Scanner;

/**
 * Created by AllTheMegahertz on 8/30/2017.
 */

public class Main {

	private static String directory;

	private static JFrame frame = new JFrame("Command Line Interface");

	private static JTextArea console = new JTextArea();
	private static JScrollPane scroll = new JScrollPane(console);
	private static JScrollBar vertical = scroll.getVerticalScrollBar();

	private static Scanner scanner;
	private static Process process;
	private static InputThread inputThread;

	private static long delayTime = 10;
	private static boolean consolePrinting = false;
	private static boolean compatibilityMode = false;

	public static void main(String[] args) {

		try {
			ProcessBuilder builder = new ProcessBuilder("cmd.exe");
			builder.redirectErrorStream(true);
			process = builder.start();

			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
			InputStream inputStream = process.getInputStream();
			scanner = new Scanner(inputStream);
			inputThread = new InputThread(scanner);
			inputThread.start();
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		frame.getContentPane().removeAll();
		frame.add(Box.createVerticalStrut(25));

		console.setLineWrap(true);
		console.setWrapStyleWord(true);
		console.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
		console.setAlignmentX(Component.CENTER_ALIGNMENT);
		console.setBackground(Color.decode("#FFFFFF"));
		console.setEditable(false);
		console.setMaximumSize(new Dimension(770, 500));
		console.setMinimumSize(new Dimension(770, 500));
		console.setLayout(new BoxLayout(console, BoxLayout.Y_AXIS));

		frame.add(scroll);

		frame.add(Box.createVerticalStrut(10));

		JTextField input = new JTextField();
		input.setAlignmentX(Component.CENTER_ALIGNMENT);
		input.setBackground(Color.decode("#FFFFFF"));
		input.setMaximumSize(new Dimension(770, 25));

		input.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					runCommand(input.getText(), console, process);
					input.setText("");
				}
			}
		});

		frame.add(input);
		frame.add(Box.createVerticalStrut(10));

		frame.setSize(800, 600);
		frame.setVisible(true);
		frame.setResizable(false);
		frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		vertical.setValue(vertical.getMaximum());

		console.append("--- Java console command line interface V1.2 ---\n");


		while (true) {
			vertical.setValue(vertical.getMaximum());
			read();
		}

	}

	private static void runCommand(String command, JTextArea console, Process process) {

		if (command.equalsIgnoreCase("!help")) {
			inputThread.add("\n !changeDelayTime [long] - Changes the amount of time the program has to read the command response in ms.");
			inputThread.add("\n !enableConsolePrinting - enables mirroring of the GUI into the console.");
			inputThread.add("\n !disableConsolePrinting - disable mirroring of the GUI into the console. (Default)");
			inputThread.add("\n !enableCompatibilityMode - (!ecm) enables compatibility with systems without access to cmd.exe");
			inputThread.add("\n !disableCompatibilityMode - (!dcm) disables compatibility with systems without access to cmd.exe (Default)");
			inputThread.add("\n !exit - Exits the program\n");
			return;
		}

		if (command.equalsIgnoreCase("!changeDelayTime")) {

			if (command.split(" ").length < 2) {
				inputThread.add("\nError: Incorrect syntax");
				return;
			}

			delayTime = Long.parseLong(command.split(" ")[1]);
			return;

		}

		if (command.equalsIgnoreCase("!enableConsolePrinting")) {
			inputThread.add("\nEnabled console printing");
			consolePrinting = true;
			return;
		}

		if (command.equalsIgnoreCase("!disableConsolePrinting")) {
			inputThread.add("\nDisabled console printing");
			consolePrinting = false;
			return;
		}

		if (command.equalsIgnoreCase("!enableCompatibilityMode") || command.equalsIgnoreCase("!ecm")) {
			inputThread.add("\nEnabled compatibility mode");
			compatibilityMode = true;
			return;
		}

		if (command.equalsIgnoreCase("!disableCompatibilityMode") || command.equalsIgnoreCase("!dcm")) {
			inputThread.add("\nDisabled compatibility mode");
			compatibilityMode = false;
			return;
		}

		if (command.equalsIgnoreCase("!exit")) {
			System.exit(0);
			return;
		}

		try {

			if (compatibilityMode) {

				ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", command);
				builder.redirectErrorStream(true);

				Process cmd = builder.start();

				BufferedReader reader = new BufferedReader(new InputStreamReader(cmd.getInputStream()));

				console.append("\n\n > " + command);

				String line;
				while ((line = reader.readLine()) != null) {
					console.append("\n" + line);
				}

				reader.close();
				cmd.destroy();

				return;

			}

			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));

			writer.write(command);
			writer.newLine();
			writer.flush();

		} catch (Exception e) {
			e.printStackTrace();
		}


	}

	private static synchronized void read() {

		String line;
		while ((line = inputThread.nextLine()) != null) {
			console.append("\n" + line);
			if (consolePrinting) {
				System.out.println(line);
			}
		}

	}

}
