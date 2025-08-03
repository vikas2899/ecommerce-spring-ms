package com.ecommerce.product_service.dto;

import com.ecommerce.product_service.model.StockStatus;

import java.util.List;

public class ProductResponseDTO {
    private String id;
    private String name;
    private String description;
    private float price;
    private String categoryName;
    private List<String> images;
    private StockStatus stockStatus;

    private int ratingCount;
    private int reviewCount;
    private int rating1Count;
    private int rating2Count;
    private int rating3Count;
    private int rating4Count;
    private int rating5Count;

    // --- Getters and Setters ---

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public int getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(int ratingCount) {
        this.ratingCount = ratingCount;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(int reviewCount) {
        this.reviewCount = reviewCount;
    }

    public int getRating1Count() {
        return rating1Count;
    }

    public void setRating1Count(int rating1Count) {
        this.rating1Count = rating1Count;
    }

    public int getRating2Count() {
        return rating2Count;
    }

    public void setRating2Count(int rating2Count) {
        this.rating2Count = rating2Count;
    }

    public int getRating3Count() {
        return rating3Count;
    }

    public void setRating3Count(int rating3Count) {
        this.rating3Count = rating3Count;
    }

    public int getRating4Count() {
        return rating4Count;
    }

    public void setRating4Count(int rating4Count) {
        this.rating4Count = rating4Count;
    }

    public int getRating5Count() {
        return rating5Count;
    }

    public void setRating5Count(int rating5Count) {
        this.rating5Count = rating5Count;
    }

    public StockStatus getStockStatus() {
        return stockStatus;
    }

    public void setStockStatus(StockStatus stockStatus) {
        this.stockStatus = stockStatus;
    }
}
