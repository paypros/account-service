# account-service

Microservicio que gestiona las cuentas de los merchants. Valida saldo y ejecuta débitos cuando `payment-service` procesa un pago.

## Stack
- Java 17 / Spring Boot 3.5.13
- PostgreSQL en producción, H2 en dev
- AWS ECS Fargate / ECR Public
- GitHub Actions (CI/CD)
- AWS Cloud Map (DNS privado `account-service.paypro.local`)
- OpenTelemetry + AWS X-Ray (trazas distribuidas)

## Endpoints
```
GET  /accounts/              → listar todas las cuentas
GET  /accounts/{merchantId}  → obtener cuenta por merchantId
POST /accounts/{merchantId}/debit → debitar monto de una cuenta
POST /accounts               → crear cuenta
```

### Request de débito
```json
{
  "amount": 100.00
}
```

### Response
```json
{
  "id": 1,
  "merchantId": "Tienda Test",
  "balance": 4900.00,
  "currency": "MXN"
}
```

## Reglas de negocio
| Condición | Resultado |
|---|---|
| Saldo suficiente | Débito ejecutado, retorna cuenta actualizada |
| Saldo insuficiente | 400 — `Insufficient funds` |
| merchantId no encontrado | 404 — `Account not found for merchant: {id}` |

## Variables de entorno (AWS)
```
SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD
SPRING_JPA_HIBERNATE_DDL_AUTO
OTEL_TRACES_EXPORTER
OTEL_EXPORTER_OTLP_ENDPOINT
```

## Observabilidad — OpenTelemetry + AWS X-Ray
Las trazas se envían al sidecar ADOT Collector en la misma Task de ECS. En el Trace Map de CloudWatch aparece como nodo conectado a `payment-service`:

```
payment-service → account-service → accountdb
```

En local, `OTEL_TRACES_EXPORTER` tiene default `none` — no requiere infraestructura de trazas para correr.

## Datos de prueba (dev)
```sql
INSERT INTO account (merchant_id, balance, currency) VALUES ('Tienda Test', 5000.00, 'MXN');
INSERT INTO account (merchant_id, balance, currency) VALUES ('Tienda B', 100.00, 'MXN');
```

---

## Decisiones de diseño

### Servicio interno — sin autenticación propia
`account-service` no tiene autenticación porque solo es accesible dentro de la VPC via Cloud Map (`account-service.paypro.local`). No tiene IP pública. La autenticación del sistema ocurre en `payment-service` que es el único punto de entrada desde internet.

### `@Transactional` en el débito
El método `debit` usa `@Transactional` para garantizar que la validación de saldo y la actualización del balance sean atómicas. Si el `save` falla después de la validación, el saldo no se modifica.

---

## Retos encontrados y soluciones

### 1. `spring.profiles.active=dev` en producción
**Problema:** La propiedad estaba activa en `application.properties` al agregar la configuración de OpenTelemetry, haciendo que en AWS cargara H2 en lugar de Postgres. El error era: `Driver org.h2.Driver claims to not accept jdbcUrl, jdbc:postgresql://...`
**Solución:** Eliminada la línea `spring.profiles.active=dev` de `application.properties`. El perfil dev se activa solo localmente cuando se lanza con `-Dspring.profiles.active=dev`.

### 2. Duplicados de merchantId en H2 al reiniciar
**Problema:** Al reiniciar el servicio varias veces en local con perfil `dev`, `data.sql` insertaba duplicados. El débito fallaba con `Query did not return a unique result: 2 results were returned`.
**Causa:** H2 con `create-drop` limpia la DB al apagar limpiamente, pero si el proceso se mata sin apagar el contexto Spring, los datos persisten y se duplican en el siguiente arranque.
**Solución:** Reiniciar completamente desde el IDE o dashboard para forzar el `create-drop`.

### 3. Primer request lento desde payment-service
**Problema:** El primer POST a `/payments` tardaba varios segundos en la llamada al account-service.
**Causa:** WebClient inicializa el pool de conexiones y resuelve el DNS de Cloud Map la primera vez.
**Solución conocida para producción:** Implementar un warm-up o health check que haga una llamada dummy al arrancar `payment-service`.
