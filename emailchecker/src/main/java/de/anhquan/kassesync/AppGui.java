package de.anhquan.kassesync;

import java.awt.Desktop;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;


public class AppGui {
	
	static Logger log = Logger.getLogger(AppGui.class);
	
	public static void main(String[] args) throws Exception {
		if (!SystemTray.isSupported()) {
			System.out.println("SystemTray is not supported");
			return;
		}
		PropertyConfigurator.configure("log4j.properties");

		final Properties properties = new Properties();
		try {
		  properties.load(new FileInputStream("app.conf"));
		} catch (IOException e) {
			log.error("Cannot load the configuration file app.conf");
		}
		final MainApp app = new MainApp(properties);
		
		SystemTray tray = SystemTray.getSystemTray();
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Image image = toolkit.getImage("logo.png");

		PopupMenu menu = new PopupMenu();

		MenuItem messageItem = new MenuItem("Show Folder EShop\\Incoming");
		log.info("Check email and save new email to "+properties.getProperty("output-dir"));
		messageItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					Desktop.getDesktop().open(new File(properties.getProperty("output-dir")));
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(null, e1.getMessage());
				}
			}
		});
		menu.add(messageItem);

		MenuItem closeItem = new MenuItem("Exit");
		closeItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				app.quit();
			}
		});
		menu.add(closeItem);
		TrayIcon icon = new TrayIcon(image,
				"Kassesync ist aktiviert", menu);
		icon.setImageAutoSize(true);

		tray.add(icon);
		
		app.start();
		
	}
}
