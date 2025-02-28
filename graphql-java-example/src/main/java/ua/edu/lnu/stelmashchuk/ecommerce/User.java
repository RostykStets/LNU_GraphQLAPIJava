package ua.edu.lnu.stelmashchuk.ecommerce;

import java.util.List;

public record User(String id,
                   String name,
                   String email,
                   List<CartItem> cart) {
}
