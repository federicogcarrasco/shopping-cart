package com.challenge.shoppingcart.services;

import com.challenge.shoppingcart.entities.Cart;
import com.challenge.shoppingcart.entities.CartItem;
import com.challenge.shoppingcart.entities.CartStatus;
import com.challenge.shoppingcart.repositories.CartItemRepository;
import com.challenge.shoppingcart.repositories.CartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderProcessingService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final MeterRegistry meterRegistry;

    @Async
    @Transactional
    public void process(Long cartId) {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            Cart cart = cartRepository.findById(cartId)
                    .orElseThrow(() -> new RuntimeException(
                            "Carrito no encontrado con id: " + cartId));

            List<CartItem> items = cartItemRepository.findByCartId(cartId);

            StringBuilder sb = new StringBuilder();
            sb.append("\n===== ORDEN PROCESADA =====");
            sb.append("\nUsuario: ").append(cart.getUser().getUsername());
            sb.append("\nCarrito: ").append(cart.getId());
            sb.append("\n---------------------------");
            sb.append("\nProductos:");

            double total = 0.0;

            for (CartItem item : items) {
                String productName = item.getProduct().getName();
                String categoryName = item.getProduct().getCategory().getName();
                double price = item.getProduct().getPrice();
                double discount = item.getProduct().getCategory().getDiscount();
                int quantity = item.getQuantity();

                double discountedPrice = price * (1 - discount);
                double itemTotal = discountedPrice * quantity;
                total += itemTotal;

                sb.append("\n  - ").append(productName)
                        .append(" (").append(categoryName).append(")")
                        .append(" | Precio: $").append(String.format("%.2f", price))
                        .append(" | Descuento: ").append((int)(discount * 100)).append("%")
                        .append(" | Precio final: $").append(String.format("%.2f", discountedPrice))
                        .append(" | Cantidad: ").append(quantity)
                        .append(" | Subtotal: $").append(String.format("%.2f", itemTotal));
            }

            sb.append("\n---------------------------");
            sb.append("\nTotal: $").append(String.format("%.2f", total));
            sb.append("\n===========================");

            log.info(sb.toString());

            cart.setStatus(CartStatus.PROCESSED);
            cartRepository.save(cart);
            meterRegistry.counter("carts.orders.processed").increment();

        } catch (Exception e) {
            log.error("Error procesando carrito {}: {}", cartId, e.getMessage(), e);
            meterRegistry.counter("carts.orders.failed").increment();
        } finally {
            sample.stop(meterRegistry.timer("carts.orders.processing.time"));
        }
    }
}