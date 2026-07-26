package habibi.order.vo;

import lombok.Data;

@Data
public class Produk {
    private long id;
    private String nama;
    private String satuan;
    private double harga;
}
