package de.anhquan.kassesync.parser;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import de.anhquan.kassesync.helper.OrderBuildHelper;
import de.anhquan.kassesync.model.Order;

public class OrderParserTest {

	@Test
	public void testParse() throws IOException, OrderParsingException {
		Order order = OrderParser.parse(new File("test-incoming.txt"));
		System.out.println(OrderBuildHelper.toXML(order));
		OrderBuildHelper.toXML(order, "C:/ProgramData/PixelPlanet/WinOrder/EShop/Incoming");
	}
}
