package main;

import jdk.internal.util.xml.impl.Input;

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

		console.append("--- Java console command line interface V1.0 ---\n");


		while (true) {
			vertical.setValue(vertical.getMaximum());
		}

	}

	private static void runCommand(String command, JTextArea console, Process process) {

		if (command.equalsIgnoreCase("!help")) {
			inputThread.add("\n !changeDelayTime [long] - Changes the amount of time the program has to read the command response in ms.\n");
			inputThread.add("\n !enableConsolePrinting - enables mirroring of the GUI into the console.\n");
			inputThread.add("\n !disableConsolePrinting - disable mirroring of the GUI into the console. (Default)\n");
			inputThread.add("\n !exit - Exits the program\n");
			read();
			return;
		}

		if (command.equalsIgnoreCase("!changeDelayTime")) {

			if (command.split(" ").length < 2) {
				inputThread.add("\nError: Incorrect syntax\n");
				read();
				return;
			}

			delayTime = Long.parseLong(command.split(" ")[1]);
			read();
			return;

		}

		if (command.equalsIgnoreCase("!enableConsolePrinting")) {
			inputThread.add("\nEnabled console printing\n");
			consolePrinting = true;
			read();
			return;
		}

		if (command.equalsIgnoreCase("!disableConsolePrinting")) {
			inputThread.add("\nDisabled console printing\n");
			consolePrinting = false;
			read();
			return;
		}

		if (command.equalsIgnoreCase("!exit")) {
			System.exit(0);
			return;
		}

		try {

			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));

			writer.write(command);
			writer.newLine();
			writer.flush();

		} catch (Exception e) {
			e.printStackTrace();
		}

		read();

	}

	private static synchronized void read() {
		int size = 0;
		while (inputThread.getQueue().size() == 0 || inputThread.getQueue().size() != size) {
			size = inputThread.getQueue().size();
			try {
				Thread.sleep(delayTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		String line;
		while ((line = inputThread.nextLine()) != null) {
			console.append("\n" + line);
			if (consolePrinting) {
				System.out.println(line);
			}
		}
	}

}
