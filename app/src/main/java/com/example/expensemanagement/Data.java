package com.example.expensemanagement;

public class Data {
    String item, date, id, note, itemNMonth;
    int amount, month;

    public Data() {
    }

    public Data(String item, String date, String id, String note, String itemNMonth, int amount, int month) {
        this.item = item;
        this.date = date;
        this.id = id;
        this.note = note;
        this.itemNMonth = itemNMonth;
        this.amount = amount;
        this.month = month;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getItemNMonth() {
        return itemNMonth;
    }

    public void setItemNMonth(String itemNMonth) {
        this.itemNMonth = itemNMonth;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }
}
