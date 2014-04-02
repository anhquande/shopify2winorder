package de.anhquan.kassesync.backup;

import org.junit.Test;

public class BackupTaskTest {

	@Test
	public void testBackupDatabase(){
		BackupTask task = new BackupTask("D:\\sandbox\\WINORDER.FDB","D:\\sandbox", "WINORDER.FDB");
		task.backupDatabase();
	}
}
