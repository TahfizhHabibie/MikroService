package habibi.order.vo;



import habibi.order.model.Order;
import lombok.Data;

@Data
public class ResponseTemplate {

    Order order;
    Produk produk;
    
}
