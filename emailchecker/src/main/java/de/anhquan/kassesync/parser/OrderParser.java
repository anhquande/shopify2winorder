package de.anhquan.kassesync.parser;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import de.anhquan.kassesync.helper.OrderBuildHelper;
import de.anhquan.kassesync.model.AddInfo;
import de.anhquan.kassesync.model.Address;
import de.anhquan.kassesync.model.Article;
import de.anhquan.kassesync.model.Customer;
import de.anhquan.kassesync.model.Order;
import de.anhquan.kassesync.model.StoreData;
import de.anhquan.kassesync.model.SubArticle;
import de.anhquan.kassesync.model.VAT;

/**
 * 
 * @author anhquan
 *
 */
public class OrderParser {

	public static final int BEILAGE_PRICE = 180;

	public static Order parse(File file) throws OrderParsingException, IOException{
		BufferedReader br = new BufferedReader(new FileReader(file));
		return parse(br);

	}
	public static Order parse(String txt) throws OrderParsingException, IOException{
	
		BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(txt.getBytes())));
		return parse(br);
	}
	
	public static Order parse(BufferedReader br) throws OrderParsingException, IOException{
		Order order = OrderBuildHelper.newOrder();
	
		String shopifyID = br.readLine().substring(1);
		order.setShopifyId(shopifyID);
		String orderDate = br.readLine();
		order.createDateTime(orderDate);
		
		String gateway = br.readLine();
		String shippingMethod = br.readLine();
		String shippingPrice = br.readLine();
		int iShippingPrice = Integer.parseInt(shippingPrice);
		Article shippingArticle = new Article()
									.articleName("Fahrtkosten")
									.count(1)
									.articleNo("DD")
									.price(iShippingPrice);
				
		order.addArticle(shippingArticle);
		String discountCode = br.readLine();
		String discountValue = br.readLine();
		String total = br.readLine();
	
		String customerName = br.readLine();
		String company = br.readLine();
		String address1 = br.readLine();

		//TODO: make it better for other cases
		String[] str = address1.split("\\d+",2);
		String street = address1;
		String houseNo = "";		
		if (str.length >1){
			street = str[0].trim();
			String desc = address1.substring(street.length()).trim();
			String[] dd = desc.split(",",2);
			houseNo = dd[0];
		}
		
		String descriptionOfWay = br.readLine();
		String city = br.readLine();
		String province = br.readLine();
		String zip = br.readLine();
		String country = br.readLine();
		String phone = br.readLine();
		
		Customer customer = new Customer()
								.customerNo("-")
								.deliveryAddress(new Address()
												.lastName(customerName)
												.company(company)
												.street(street)
												.houseNo(houseNo)
												.city(city)
												.zip(zip)
												.setState(province)
												.phoneNo(phone)
												.country(country));
		order.storeData(new StoreData()
							.storeId("1")
							.storeName("Dai Duong"));
		AddInfo addInfo = new AddInfo()
				.currencyStr("\u00e2\u201a\u00ac");
		if ((discountValue!=null) && (discountValue.length()>0))
				addInfo.discountValue(-Integer.parseInt(discountValue));
		
		String bestellung = br.readLine();
		if ("Bestellung:".compareToIgnoreCase(bestellung) != 0)
			throw new OrderParsingException(
					"Invalid Header File in the incoming order. line = "
							+ bestellung);
		String line = br.readLine();
		int sumVAT7 = 0;
		int sumVAT19 = 0;
		while ("Notice:".compareToIgnoreCase(line) != 0) {
			String lineQuantity = line;
			String lineSKU = br.readLine();
			String lineTitle = br.readLine();
			String lineProductType = br.readLine();
			String lineBasePrice = br.readLine(); 
			int basePrice = Integer.parseInt(lineBasePrice);
			String lineEndPrice = br.readLine();
			int endPrice = Integer.parseInt(lineEndPrice);
			
			//save order info
			Article article = new Article()
								.articleNo(lineSKU)
								.count(Integer.parseInt(lineQuantity))																
								.price(basePrice);
			
			VAT productTaxType;
			if ("Getränke".compareToIgnoreCase(lineProductType)==0){
				productTaxType = VAT.DE_19;
				sumVAT19+=endPrice;
			}
			else{
				productTaxType = VAT.DE_7;
				sumVAT7+=endPrice;
			}
			
			article.tax(productTaxType.value());
			//////////////////
			String[] titles = lineTitle.split("\\s-\\s",2);
			article.articleName(titles[0]);
			if (titles.length>1){
				int i=0;
				for (String t : titles) {
					if (i>0){
						t = t.trim();
						if (t.startsWith("Gebratener Reis")){
							SubArticle sub = new SubArticle();
							sub.articleName("Gebratener Reis");
							sub.articleNo("Y");
							sub.price(BEILAGE_PRICE);
							sub.count(1);
							article.addSubArticle(sub);
							article.price(endPrice-BEILAGE_PRICE);
	
						} 
						else if (t.startsWith("Gebratene Nudeln")){
							SubArticle sub = new SubArticle();
							sub.articleName("Gebratene Nudeln");
							sub.articleNo("YY");
							sub.price(BEILAGE_PRICE);
							sub.count(1);
							article.addSubArticle(sub);
							article.price(endPrice-BEILAGE_PRICE);
						} 
						
					}
					i++;
				}
			}
			
			//////////////////////
			line = br.readLine();
			if ("Properties:".compareToIgnoreCase(line) != 0) {
				throw new OrderParsingException(
						"Invalid Format in the Order Line. Expecting 'Properties' but we found "
								+ line);
			}
			while ((line = br.readLine()) != null) {
				if (line.startsWith("+")) {
					String[] s = line.split(":", 1);
				} else
					break;
			}
			
			
			
			order.addArticle(article);
			line = br.readLine();
		}
	
		if ("paypal".compareToIgnoreCase(gateway)==0){
			if (sumVAT19>0){
				Article on19 = new Article();
				on19.articleName("Onlinebezahlt 19%").articleNo("ON19").count(1).comment("Betrag").price(-sumVAT19);
				order.addArticle(on19);
			}
			
			Article on7 = new Article();
			on7.articleName("Onlinebezahlt 7%").articleNo("ON7").count(1).comment("Betrag").price(-sumVAT7);
			order.addArticle(on7);
		}
			
		if ((discountCode != null) && (discountCode.length()>0)){
			Article discountArticle = new Article().articleNo(discountCode)					
													.articleName("Discount Code: "+discountCode)
													.count(1)
													.price(0);
			order.addArticle(discountArticle);
		}
		
		if ("Notice:".compareToIgnoreCase(line) != 0) {
			throw new OrderParsingException(
					"Invalid Format in the Order Line. Expecting 'Notice' but we found "
							+ line);
		}
		String notice;
		StringBuilder noticeBuilder = new StringBuilder();
		line = br.readLine();
		
		boolean hasNotice = ((line != null) && (!line.isEmpty())); 
		while (hasNotice) {
			noticeBuilder.append(line);
			line = br.readLine();
			hasNotice = ((line != null) && (!line.isEmpty()));
			if (hasNotice)
				noticeBuilder.append("\r\n");
		}
		notice = noticeBuilder.toString();
		
		customer.getDeliveryAddress().descriptionOfWay(descriptionOfWay);
		order.customer(customer);
		
		addInfo.comment(notice);
		order.addInfo(addInfo);
		br.close();
		
		return order;
	}
}
