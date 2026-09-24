package cl.dsy1104.fonda.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class Bebida {
    private int id;
    private String nombre;
    private TipoBebida tipoBebida;
    private int volumen;
    
}