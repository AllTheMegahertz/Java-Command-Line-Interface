package main;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by AllTheMegahertz on 8/30/2017.
 */

public class InputThread implements Runnable {

	private ArrayList<String> queue = new ArrayList<String>();
	private Scanner scanner;

	public Thread thread;

	public InputThread(Scanner scanner) {
		this.scanner = scanner;
	}

	public void run() {

		while (true) {
			String line;
			while ((line = scanner.nextLine()) != null) {
				getQueue().add(line);
			}
		}

	}

	public synchronized ArrayList<String> getQueue() {
		return queue;
	}

	public void refresh() {
		String line;
		while ((line = scanner.nextLine()) != null) {
			getQueue().add(line);
		}
	}

	public String nextLine() {

		if (getQueue().size() == 0) {
			return null;
		}

		return getQueue().remove(0);
	}

	public void start() {

		if (thread == null) {
			thread = new Thread(this);
			thread.start();
		}

	}

	public void add(String s) {
		getQueue().add(s);
	}

}
