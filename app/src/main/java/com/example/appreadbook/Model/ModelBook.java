package com.example.appreadbook.Model;

public class ModelBook {

    String bookId,bookTitle, bookDescription,bookCategoryId,bookUrl;
    long bookTimestamp, bookViewCount, bookDownloadCount;
    boolean bookFavourite;
    Integer bookPrice;

    public ModelBook() {
    }

    public ModelBook(String bookId, String bookTitle, String bookDescription, String bookCategoryId, String bookUrl, long bookTimestamp, long bookViewCount, long bookDownloadCount, Integer bookPrice, boolean bookFavourite) {
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.bookDescription = bookDescription;
        this.bookCategoryId = bookCategoryId;
        this.bookUrl = bookUrl;
        this.bookTimestamp = bookTimestamp;
        this.bookViewCount = bookViewCount;
        this.bookDownloadCount = bookDownloadCount;
        this.bookPrice = bookPrice;
        this.bookFavourite = bookFavourite;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getBookDescription() {
        return bookDescription;
    }

    public void setBookDescription(String bookDescription) {
        this.bookDescription = bookDescription;
    }

    public String getBookCategoryId() {
        return bookCategoryId;
    }

    public void setBookCategoryId(String bookCategoryId) {
        this.bookCategoryId = bookCategoryId;
    }

    public String getBookUrl() {
        return bookUrl;
    }

    public void setBookUrl(String bookUrl) {
        this.bookUrl = bookUrl;
    }

    public long getBookTimestamp() {
        return bookTimestamp;
    }

    public void setBookTimestamp(long bookTimestamp) {
        this.bookTimestamp = bookTimestamp;
    }

    public long getBookViewCount() {
        return bookViewCount;
    }

    public void setBookViewCount(long bookViewCount) {
        this.bookViewCount = bookViewCount;
    }

    public long getBookDownloadCount() {
        return bookDownloadCount;
    }

    public void setBookDownloadCount(long bookDownloadCount) {
        this.bookDownloadCount = bookDownloadCount;
    }

    public Integer getBookPrice() {
        return bookPrice;
    }

    public void setBookPrice(Integer bookPrice) {
        this.bookPrice = bookPrice;
    }

    public boolean isBookFavourite() {
        return bookFavourite;
    }

    public void setBookFavourite(boolean bookFavourite) {
        this.bookFavourite = bookFavourite;
    }
}
