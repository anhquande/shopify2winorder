package de.anhquan.kassesync;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.mail.Flags;

import jodd.mail.EmailFilter;
import jodd.mail.EmailMessage;
import jodd.mail.ImapSslServer;
import jodd.mail.ReceiveMailSession;
import jodd.mail.ReceivedEmail;

import org.apache.log4j.Logger;

import de.anhquan.kassesync.backup.BackupTask;
import de.anhquan.kassesync.helper.OrderBuildHelper;
import de.anhquan.kassesync.model.Order;
import de.anhquan.kassesync.parser.OrderParser;
import de.anhquan.kassesync.parser.OrderParsingException;

/**
 * 
 * @author anhquan
 *
 */
public class MainApp extends Thread {
	ReceiveMailSession session;
	ImapSslServer imapServer;
	String emailServer;
	String emailUsername;
	String emailPassword;
	
	volatile boolean quitFlag;

	int alarmHour;
	int alarmMinute;
	boolean isAlreadyBackup;
	
	static Logger log = Logger.getLogger(MainApp.class);
	
	Properties prop;
	String outputDir = "";
	private String backupDir;
	private String backupFile;
	private String dbSourceFile;
	private String filterSender;

	public MainApp(Properties prop) {
		this.prop = prop;
		outputDir = prop.getProperty("output-dir");
		backupDir = prop.getProperty("backup-dir");
		backupFile = prop.getProperty("backup-file");
		dbSourceFile = prop.getProperty("db-source-file");
		alarmHour= Integer.parseInt(prop.getProperty("backup-hour"));
		alarmMinute=Integer.parseInt(prop.getProperty("backup-minute"));
		emailServer = prop.getProperty("email-server");
		emailUsername = prop.getProperty("email-username");
		emailPassword = prop.getProperty("email-password");
		filterSender= prop.getProperty("filter-sender");
		isAlreadyBackup = false;
	}

	public void run() {
		log.info("Start Backup Timer ...");
		
		log.info("Connecting to GMAIL IMAP Server ... ");
		quitFlag = false;
		createSession();
		log.info("Session created.");
		checkMail();
		while (true) {
			checkMail();
			
			//check backup
			Date now=new Date();
			int curHour=now.getHours();
			int curMinute = now.getMinutes();
			if ((curHour==23)&&(curMinute==59))
				isAlreadyBackup = false;
			
			if (!isAlreadyBackup)
				if ((curHour >= alarmHour) && (curMinute >= alarmMinute)){
					//time to backup
					BackupTask task = new BackupTask(dbSourceFile,backupDir, backupFile);
					task.start();					
					isAlreadyBackup = true;
				}
			
			try {
				sleep((int) (30000));
				log.info("Checkmail...");
			} catch (InterruptedException e) {
				log.info("Interrupt");
				Thread.currentThread().interrupt(); // very important
				break;
			}

		}

		closeSession();
		System.exit(0);

	}

	public void quit() {
		log.info("Quit now");
		this.interrupt(); 
	}

	public void createSession() {
		imapServer = new ImapSslServer(emailServer,
				emailUsername, emailPassword);
		session = imapServer.createSession();
		session.open();
		session.useFolder("INBOX");
	}

	public void closeSession() {
		session.close();
		log.info("IMAP Session closed. Good bye!");
	}

	public void checkMail() {
		SimpleDateFormat df = new SimpleDateFormat("dd.mm.yy hh:mm:ss");
		log.info("[" + df.format(new Date()) + "] Inbox ("
				+ session.getMessageCount() + ")");
		Flags flags = new Flags("processed");

		Flags flagsToSet = new Flags("processed");
		ReceivedEmail[] emails = session.receive(
				EmailFilter.filter().from(filterSender)
						.flags(flags, false), flagsToSet);
		if (emails != null) {
			log.info("New Message ("+emails.length + ")");
			for (ReceivedEmail email : emails) {

				// process messages
				List<EmailMessage> messages = email.getAllMessages();
				for (EmailMessage msg : messages) {
					if (msg.getMimeType().contains("TEXT/PLAIN")) {
						try {
							String emailContent = msg.getContent();
							Order order = OrderParser.parse(emailContent);
							OrderBuildHelper.toXML(order, outputDir);

						} catch (OrderParsingException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		} else {
			log.info("New Message (0)");
		}
	}
}
