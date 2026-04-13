package com.challenge.shoppingcart.repositories;

import com.challenge.shoppingcart.entities.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.id = :cartId AND ci.product.id = :productId")
    Optional<CartItem> findByCartIdAndProductId(@Param("cartId") Long cartId,
                                                @Param("productId") Long productId);

    @Query("SELECT ci FROM CartItem ci JOIN FETCH ci.product p JOIN FETCH p.category WHERE ci.cart.id = :cartId")
    List<CartItem> findByCartId(@Param("cartId") Long cartId);
}