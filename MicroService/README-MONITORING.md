# Microservice + Monitoring/Logging Stack

Proyek Spring Boot microservices ini sekarang sudah dilengkapi **Prometheus**, **Grafana**,
dan **ELK Stack (Elasticsearch, Logstash, Kibana + Filebeat)**, semuanya dijalankan lewat
satu `docker-compose.yml`.

## Struktur service

| Service          | Port | Peran                                   |
|-------------------|------|------------------------------------------|
| eureka            | 8761 | Service discovery (Netflix Eureka)       |
| gateway-service   | 9000 | API Gateway (Spring Cloud Gateway)        |
| authservice       | 8086 | Auth (pakai MySQL)                        |
| produk            | 8081 | Produk (H2 in-memory)                     |
| pelanggan         | 8082 | Pelanggan (H2 in-memory)                  |
| order             | 8083 | Order (H2 in-memory + RabbitMQ)           |
| produser          | 8080 | Publish pesan ke RabbitMQ                 |
| consumer          | 8085 | Consume pesan RabbitMQ + kirim email      |
| mysql             | 3306 | Database authservice                      |
| rabbitmq          | 5672 / 15672 (mgmt UI) | Message broker              |

## Stack observability yang ditambahkan

| Tool          | Port | Fungsi                                             |
|----------------|------|-----------------------------------------------------|
| Prometheus     | 9090 | Scrape metrik dari endpoint `/actuator/prometheus` tiap service |
| Grafana        | 3000 | Visualisasi metrik (datasource Prometheus sudah auto-provisioned) |
| Elasticsearch  | 9200 | Penyimpanan log                                     |
| Logstash       | 5044 | Terima log dari Filebeat, parse JSON, kirim ke ES   |
| Kibana         | 5601 | Visualisasi & pencarian log                         |
| Filebeat       | -    | Mengambil log container Docker & kirim ke Logstash  |

## Apa saja yang diubah/ditambahkan di kode

1. **`pom.xml`** tiap service (kecuali `demo`, yang tidak dipakai di arsitektur ini) ditambah:
   - `spring-boot-starter-actuator`
   - `micrometer-registry-prometheus`
   - `logstash-logback-encoder` (untuk log JSON)
2. **`application.properties` / `.yml`** tiap service ditambah konfigurasi:
   ```properties
   management.endpoints.web.exposure.include=health,info,prometheus,metrics
   management.prometheus.metrics.export.enabled=true
   ```
   Host database/RabbitMQ/Eureka juga diparameterkan lewat environment variable
   (`MYSQL_HOST`, `RABBITMQ_HOST`, `EUREKA_HOST`, dst) dengan default `localhost`,
   supaya kode yang sama tetap bisa dijalankan manual di lokal maupun lewat Docker Compose.
3. **`logback-spring.xml`** baru di tiap service — log dicetak ke stdout dalam format JSON
   (via `LogstashEncoder`) supaya gampang di-parse oleh Logstash.
4. **`Dockerfile`** baru di tiap service (multi-stage build: Maven → JRE 21 image).
5. **`docker-compose.yml`** di root — mengorkestrasi semua service + MySQL + RabbitMQ +
   Prometheus + Grafana + ELK + Filebeat dalam satu network.
6. **`monitoring/`** — semua file konfigurasi Prometheus, Grafana (datasource + 1 dashboard
   siap pakai), Logstash pipeline, dan Filebeat.
7. **`rabbitmq/definitions.json`** — supaya dua user RabbitMQ yang dipakai di kode asli
   (`ari` dan `habibi`, sama-sama password `12345`) otomatis dibuat saat container start.

> Catatan: folder `demo/` (proyek "Hello World" bawaan Spring Initializr) sengaja tidak
> diikutkan ke `docker-compose.yml` karena tidak terpakai di alur microservice ini.

## Cara menjalankan

Pastikan Docker & Docker Compose sudah terpasang, lalu dari folder root project:

```bash
docker compose up -d --build
```

Build pertama kali akan agak lama karena Maven mengunduh dependency untuk 8 service.
Tunggu sampai semua container `healthy`/`running`:

```bash
docker compose ps
```

### Akses masing-masing tool

- **Eureka dashboard**: http://localhost:8761
- **API Gateway**: http://localhost:9000
- **Prometheus**: http://localhost:9090 (cek Status → Targets, semua service harus `UP`)
- **Grafana**: http://localhost:3000 (login `admin` / `admin`) → dashboard
  **"Microservices Overview"** sudah otomatis ter-load di folder root
- **RabbitMQ Management**: http://localhost:15672 (login `ari` / `12345` atau `habibi` / `12345`)
- **Kibana**: http://localhost:5601 → buat **Data View / Index Pattern** dengan pola
  `microservices-logs-*` untuk mulai melihat log semua service
- **Elasticsearch**: http://localhost:9200

### Mematikan semuanya

```bash
docker compose down
```

Tambahkan `-v` kalau ingin sekalian menghapus data MySQL, Elasticsearch, dan Grafana:

```bash
docker compose down -v
```

## Troubleshooting singkat

- **Service gagal konek ke Eureka/MySQL/RabbitMQ**: cek urutan start — `depends_on` sudah
  diatur, tapi Spring Boot butuh beberapa detik lagi setelah container "running". Jika perlu,
  tambahkan `restart: on-failure` atau retry mechanism di kode.
- **Prometheus target `DOWN`**: pastikan endpoint `/actuator/prometheus` bisa diakses, cek
  `docker compose logs <service>` untuk error startup.
- **Kibana belum ada data**: pastikan index `microservices-logs-*` sudah muncul di
  Elasticsearch (`curl http://localhost:9200/_cat/indices`) — butuh beberapa log masuk dulu.
- **Elasticsearch gagal start (memory)**: image ini butuh minimal ~1GB RAM; naikkan resource
  Docker Desktop kalau container `elasticsearch` terus restart.
