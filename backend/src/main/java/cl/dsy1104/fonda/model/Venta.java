package cl.dsy1104.fonda.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@AllArgsConstructor 
@NoArgsConstructor 

public class Venta {
    private int id;
    private Bebida bebida;
    private int unidades;
    private int total;
    private EstadoVenta estado;
    private String motivo;
    private LocalDateTime fecha;
    
    
}
