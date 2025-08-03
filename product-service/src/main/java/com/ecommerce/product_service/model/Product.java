package com.ecommerce.product_service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @NotNull
    private String name;

    @NotNull
    private String description;

    @NotNull
    @Positive(message = "price must be positive")
    private float price;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @NotNull
    @Lob
    @Column(columnDefinition = "TEXT")
    private String images; // Store JSON array as a string: ["url1", "url2"]

    @Column(name = "rating_count")
    private int ratingCount;

    @Column(name = "review_count")
    private int reviewCount;

    @Column(name = "rating_1_count")
    private int rating1Count;

    @Column(name = "rating_2_count")
    private int rating2Count;

    @Column(name = "rating_3_count")
    private int rating3Count;

    @Column(name = "rating_4_count")
    private int rating4Count;

    @Column(name = "rating_5_count")
    private int rating5Count;

    // --- Getters and Setters ---

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
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

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
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
}
