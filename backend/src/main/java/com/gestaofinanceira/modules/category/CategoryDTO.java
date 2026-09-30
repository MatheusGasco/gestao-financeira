package com.gestaofinanceira.modules.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class CategoryDTO {

    public static class Response {
        private UUID id;
        private String name;
        private CategoryType type;
        private String icon;
        private String color;
        private boolean isDefault;

        public Response() {}

        public Response(UUID id, String name, CategoryType type, String icon, String color, boolean isDefault) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.icon = icon;
            this.color = color;
            this.isDefault = isDefault;
        }

        public static Response fromEntity(Category category) {
            return new Response(
                    category.getId(),
                    category.getName(),
                    category.getType(),
                    category.getIcon(),
                    category.getColor(),
                    category.isDefault()
            );
        }

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }

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

    public static class Request {
        @NotBlank(message = "O nome da categoria é obrigatório")
        private String name;

        @NotNull(message = "O tipo da categoria é obrigatório (INCOME/EXPENSE)")
        private CategoryType type;

        private String icon;
        private String color;

        public Request() {}

        public Request(String name, CategoryType type, String icon, String color) {
            this.name = name;
            this.type = type;
            this.icon = icon;
            this.color = color;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public CategoryType getType() { return type; }
        public void setType(CategoryType type) { this.type = type; }

        public String getIcon() { return icon; }
        public void setIcon(String icon) { this.icon = icon; }

        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }
    }
}
