package habibi.order.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import habibi.order.model.Order;
import habibi.order.service.OrderService;
import habibi.order.vo.ResponseTemplate;
import habibi.order.security.JwtUtil;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    private Claims validateToken(String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Token tidak ada");
        }

        String token = authHeader.substring(7);

        try {
            return JwtUtil.validateToken(token);
        } catch (ExpiredJwtException e) {
            throw new RuntimeException("Token expired, silakan login ulang");
        } catch (Exception e) {
            throw new RuntimeException("Token tidak valid");
        }
    }

    @GetMapping
    public List<Order> getAll(
            @RequestHeader("Authorization") String authHeader) {

        Claims claims = validateToken(authHeader);

        String role = claims.get("role", String.class);

        if (!role.equals("ADMIN")) {
            throw new RuntimeException("Akses ditolak, hanya ADMIN");
        }

        return orderService.getAll();
    }

    @PostMapping
    public Order createOrder(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Order order) {

        Claims claims = validateToken(authHeader);

        String username = claims.getSubject();
        String role = claims.get("role", String.class);

        System.out.println("ORDER OLEH: " + username + " (" + role + ")");

        return orderService.createOrder(order, username, role);
    }

    @GetMapping(path = "{id}")
    public Order getOrderById(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable("id") Long id) {

        validateToken(authHeader);

        return orderService.getOrderById(id);
    }

    @GetMapping(path = "/produk/{id}")
    public List<ResponseTemplate> getOrderWithProdukById(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable("id") Long id) {

        validateToken(authHeader);

        return orderService.getOrderWithProdukById(id);
    }

    @PutMapping(path = "/{id}")
    public void updateOrder(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable("id") Long id,
            @RequestParam(required = false) Integer jumlah,
            @RequestParam(required = false) String tanggal,
            @RequestParam(required = false) String status) {

        validateToken(authHeader);

        orderService.update(id, jumlah, tanggal, status);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOrder(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable long id) {

        Claims claims = validateToken(authHeader);

        String role = claims.get("role", String.class);

        if (!role.equals("ADMIN")) {
            throw new RuntimeException("Akses ditolak, hanya ADMIN");
        }

        orderService.deleteOrder(id);

        return ResponseEntity.ok().build();
    }
}