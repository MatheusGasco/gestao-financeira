package com.gestaofinanceira.modules.category;

import com.gestaofinanceira.modules.user.User;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 60)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CategoryType type;

    @Column(length = 40)
    private String icon;

    @Column(length = 20)
    private String color;

    private boolean isDefault = false;

    public Category() {}

    public Category(UUID id, User user, String name, CategoryType type, String icon, String color, boolean isDefault) {
        this.id = id;
        this.user = user;
        this.name = name;
        this.type = type;
        this.icon = icon;
        this.color = color;
        this.isDefault = isDefault;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private User user;
        private String name;
        private CategoryType type;
        private String icon;
        private String color;
        private boolean isDefault = false;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder user(User user) { this.user = user; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder type(CategoryType type) { this.type = type; return this; }
        public Builder icon(String icon) { this.icon = icon; return this; }
        public Builder color(String color) { this.color = color; return this; }
        public Builder isDefault(boolean isDefault) { this.isDefault = isDefault; return this; }

        public Category build() {
            return new Category(id, user, name, type, icon, color, isDefault);
        }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public CategoryType getType() { return type; }
    public void setType(CategoryType type) { this.type = type; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean aDefault) { isDefault = aDefault; }
}
