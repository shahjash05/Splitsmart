package com.splitsmart.model;

import jakarta.persistence.*;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "category_name", nullable = false, length = 100)
    private String categoryName;

    @Column(length = 10)
    private String icon;

    public Category() {}

    public Long getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName; }
    public String getIcon() { return icon; }

    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public void setIcon(String icon) { this.icon = icon; }

    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private final Category c = new Category();
        public Builder categoryId(Long v) { c.categoryId = v; return this; }
        public Builder categoryName(String v) { c.categoryName = v; return this; }
        public Builder icon(String v) { c.icon = v; return this; }
        public Category build() { return c; }
    }
}
