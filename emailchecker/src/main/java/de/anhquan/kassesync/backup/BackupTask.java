package de.anhquan.kassesync.backup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

/**
 * 
 * @author anhquan
 *
 */
public class BackupTask extends Thread{

	String dbSourceFile;
	String backupDir;
	String backupFileName;
	static Logger log = Logger.getLogger(BackupTask.class);

	public BackupTask(String dbSourceFile, String backupDir, String backupFileName) {
		this.dbSourceFile = dbSourceFile;
		this.backupDir = backupDir;
		this.backupFileName = backupFileName;
	}

	public void run(){
		log.info("Start backup ...");
		backupDatabase();
		log.info("Backup finished!");
	}
	public void backupDatabase() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		String today = df.format(new Date());
		String newFolderName = backupDir + "/" + today;
		String newFileName = newFolderName+"/"+backupFileName;
		
		if (new File(newFileName).exists()){
			log.info("The backup file "+newFileName+" already exists. No backup required. Skip now !");
			return;
		}
			
		File newFolder = new File(newFolderName);
		if (!newFolder.exists()) {
			if (newFolder.mkdir()) {
				log.info("New folder '" + newFolderName
						+ "' is created");
				
			} else
				log.error("Cannot create folder '" + newFolderName
						+ "'");
		}
		
		try {
			copyFile(new File(dbSourceFile), new File(newFileName));
			log.info("new file "+newFileName+" is created");
		} catch (IOException e) {
			log.error(e.getMessage());
		}

	}

	private static void copyFile(File source, File dest)
			throws IOException {
		FileChannel inputChannel = null;
		FileChannel outputChannel = null;
		try {
			inputChannel = new FileInputStream(source).getChannel();
			outputChannel = new FileOutputStream(dest).getChannel();
			outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
		} finally {
			inputChannel.close();
			outputChannel.close();
		}
		
		log.info("copying file finished.");
	}

}
