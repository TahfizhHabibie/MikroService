package habibi.order.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import org.springframework.cloud.client.discovery.DiscoveryClient;
import habibi.order.model.Order;
import habibi.order.repository.OrderRepository;
import habibi.order.vo.Produk;
import habibi.order.vo.ResponseTemplate;

import jakarta.transaction.Transactional;

@Service
public class OrderService {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private DiscoveryClient discoveryClient;

    @Autowired
    private RestTemplate restTemplate;

    public List<Order> getAll() {
        return orderRepository.findAll();
    }

    public Order createOrder(Order order, String username, String role) {

        order.setCreatedBy(username);
        order.setRole(role);

        Order saved = orderRepository.save(order);

        String message = "Order ID: " + saved.getId() +
                ", Total: " + saved.getTotal() +
                ", User: " + username +
                ", Role: " + role;

        rabbitTemplate.convertAndSend("orderQueue", message);

        System.out.println("ORDER OLEH: " + username + " (" + role + ")");

        return saved;
    }

    @Transactional
    public void update(Long orderId, Integer jumlah, String tanggal, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalStateException("Order tidak ada"));

        if (jumlah != null) {
            order.setJumlah(jumlah);
        }
        if (tanggal != null && tanggal.length() > 0
                && !Objects.equals(order.getTanggal(), tanggal)) {
            order.setTanggal(tanggal);
        }
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id).orElse(null);
    }

    public List<ResponseTemplate> getOrderWithProdukById(Long id) {
        List<ResponseTemplate> resoponseList = new ArrayList<>();

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Order tidak ditemukan"));

        ServiceInstance serviceInstance = discoveryClient.getInstances("PRODUK").get(0);

        Produk produk = restTemplate.getForObject(
                serviceInstance.getUri() + "/api/produk/" + order.getProdukId(),
                Produk.class);

        ResponseTemplate vo = new ResponseTemplate();
        vo.setOrder(order);
        vo.setProduk(produk);

        resoponseList.add(vo);
        return resoponseList;
    }

    public void deleteOrder(long id) {
        orderRepository.deleteById(id);
    }
}