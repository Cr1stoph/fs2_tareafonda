package cl.dsy1104.fonda.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import cl.dsy1104.fonda.model.Bebida;
import cl.dsy1104.fonda.model.EstadoVenta;
import cl.dsy1104.fonda.model.TipoBebida;
import cl.dsy1104.fonda.model.Venta;
import cl.dsy1104.fonda.repository.BebidaRepository;
import cl.dsy1104.fonda.repository.VentaRepository;
import jakarta.transaction.Transactional;

@Service 
public class VentaService {

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private BebidaRepository bebidaRepository;

    // Lee automáticamente el límite desde application.properties
    @Value("${fonda.limite-unidades-por-cliente}")
    private Integer limiteUnidadesPorCliente;

    @Transactional
    public Venta registrarVenta(VentaRequestDTO request) {
        // Buscar la bebida o lanzar excepción 404 si no existe
        Bebida bebida = bebidaRepository.findById(request.getBebidaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bebida no encontrada"));

        // 1. Verificar restricción de venta
        if (Boolean.TRUE.equals(bebida.getVentaRestringida())) {
            throw new ConflictException("VENTA_RESTRINGIDA", "La venta de esta bebida se encuentra restringida.");
        }

        // 2. Verificar límite si es alcohólica
        if (bebida.getTipo() == TipoBebida.ALCOHOLICA && request.getUnidades() > limiteUnidadesPorCliente) {
            throw new ConflictException("LIMITE_EXCEDIDO", request.getUnidades() + " unidades superan el límite de " + limiteUnidadesPorCliente + " por cliente.");
        }

        // 3. Verificar stock suficiente
        if (bebida.getStock() < request.getUnidades()) {
            throw new ConflictException("STOCK_INSUFICIENTE", "El stock disponible es menor a las unidades solicitadas.");
        }

        // 4. Si pasa todo: calcular precio, descontar stock y guardar venta autorizada
        int precioUnitario = calcularPrecio(bebida);
        int total = precioUnitario * request.getUnidades();

        bebida.setStock(bebida.getStock() - request.getUnidades());
        bebidaRepository.save(bebida);

        Venta venta = new Venta();
        venta.setBebida(bebida);
        venta.setUnidades(request.getUnidades());
        venta.setTotal(total);
        venta.setEstado(EstadoVenta.AUTORIZADA);

        return ventaRepository.save(venta);
    }

    private int calcularPrecio(Bebida bebida) {
        if (bebida.getTipo() == TipoBebida.ALCOHOLICA) {
            int precioBase = 3500;
            // Si no está certificada, sube un 20%
            if (Boolean.FALSE.equals(bebida.getCertificada())) {
                return (int) (precioBase * 1.20);
            }
            return precioBase;
        } else {
            int precioBase = 2000;
            // Si el azúcar por litro supera 80, sube un 10%
            if (bebida.getAzucarPorLitro() != null && bebida.getAzucarPorLitro() > 80) {
                return (int) (precioBase * 1.10);
            }
            return precioBase;
        }
    }
}