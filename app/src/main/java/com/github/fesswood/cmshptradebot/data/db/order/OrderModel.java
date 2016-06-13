package com.github.fesswood.cmshptradebot.data.db.order;

import io.realm.RealmObject;

/**
 * Created by fesswood on 08.06.16.
 */
public class OrderModel extends RealmObject {

    private double price;
    private int count;


    public OrderModel() {
    }

    public OrderModel(float v, int i) {
        price =v;
        count = i;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "OrderModel{" +
                "price=" + price +
                ", count=" + count +
                '}';
    }
}
