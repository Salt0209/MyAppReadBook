package com.example.appreadbook.Model;

public class ModelCategory {
    String categoryId, categoryName;
    long categoryTimestamp;

    public ModelCategory() {
    }

    public ModelCategory(String categoryId, String categoryName, long categoryTimestamp) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.categoryTimestamp = categoryTimestamp;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public long getCategoryTimestamp() {
        return categoryTimestamp;
    }

    public void setCategoryTimestamp(long categoryTimestamp) {
        this.categoryTimestamp = categoryTimestamp;
    }
}
