package de.anhquan.kassesync.model;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import org.junit.Test;

import de.anhquan.kassesync.helper.OrderBuildHelper;

public class OrderTest {

	@Test
	public void testExportOrder() {

		Order order = OrderBuildHelper
				.newOrder()
				.createDateTime(new Date().toString())
				.serverData(
						new ServerData().agent("WinOrder V2.5.0.16")
								.ipAddress("192.168.1.111").referer("WinOrder"))
				.customer(
						new Customer().customerNo("-1")
								.deliveryAddress(
										new Address().firstName("Anh Quan")
												.lastName("Nguyen")
												.street("Kasino Str")
												.houseNo("11").city("Aachen")
												.zip("52066").country("49")
												.phoneNo("0421-11111111")))
				.storeData(new StoreData().storeId("1").storeName("Dai Duong"))
				.addInfo(
						new AddInfo().currencyStr("\u00e2\u201a\u00ac")
								.deliverLumpSum(0))
				.addArticle(
						new Article()
								.articleNo("31")
								.articleName("Nem Ran Ha Noi")
								.count(1)
								.price(320)
								.addSubArticle(
										new SubArticle().articleNo("SS").count(
												1)));

		System.out.println(OrderBuildHelper.toXML(order));

		try {
			FileWriter writer = new FileWriter("neworder.xml");
			OrderBuildHelper.toXML(order, writer);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
