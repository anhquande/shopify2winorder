package de.anhquan.kassesync.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * Specification at http://download.pixelplanet.com/WinOrder/WinOrder-EShop-Spezifikation.pdf
 * @author anhquan
 *
 */
@XStreamAlias("Order")
public class Order {

	@XStreamOmitField
	private
	OrderList parent;
	
	AddInfo addInfo;
	
	@XStreamAlias("ServerData")
	private ServerData _serverData;
	
	@XStreamAlias("Customer")
	private Customer _customer;

	@XStreamAlias("StoreData")
	private StoreData _storeData;

	@XStreamAlias("AddInfo")
	private AddInfo _addInfo;

	@XStreamAlias("ArticleList")
	private List<Article> _articles;

	@XStreamOmitField
	private String shopifyId;
	
	public Order(){
		parent = new OrderList();
		parent.setChild(this);
		
		_articles = new ArrayList<Article>();
	}
	
	public ServerData getServerData() {
		return _serverData;
	}

	public void setServerData(ServerData serverData) {
		this._serverData = serverData;
	}

	public void setAddInfo(AddInfo addInfo){
		this.addInfo = addInfo;
	}

	public Order serverData(ServerData serverData) {
		this._serverData = serverData;
		return this;
	}

	public OrderList getParent() {
		return parent;
	}
	
	public Order createDateTime(String createDateTime) {
		parent.createDateTime(createDateTime);
		return this;
	}

	public Order customer(Customer customer) {
		this._customer = customer;
		return this;
	}

	public Order storeData(StoreData storeData) {
		this._storeData = storeData;
		return this;
	}

	public Order addInfo(AddInfo addInfo) {
		this._addInfo = addInfo;
		return this;
	}

	public Order addArticle(Article article) {
		this._articles.add(article);
		return this;
	}

	public String getShopifyId() {
		return shopifyId;
	}
	
	public void setShopifyId(String id){
		this.shopifyId = id;
	}
}
