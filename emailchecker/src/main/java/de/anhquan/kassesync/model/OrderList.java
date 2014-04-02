package de.anhquan.kassesync.model;

import java.util.Date;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * Specification at http://download.pixelplanet.com/WinOrder/WinOrder-EShop-Spezifikation.pdf
 * @author anhquan
 *
 */
public class OrderList {

	@XStreamOmitField
	WinOrder parent;
	
	@XStreamAlias("CreateDateTime")
	private String _createDateTime;

	@XStreamAlias("Order")
	Order _order;
	

	public OrderList(){
		parent = new WinOrder();
		parent.setChild(this);
	}
	
	public void setChild(Order order) {
		this._order = order;
	}

	public OrderList createDateTime(String date) {
		this._createDateTime = date;
		return this;
	}
	
	public WinOrder getParent(){
		return parent;
	}
}
