package upc.pe.nayrabackend.entities;

import java.security.PublicKey;
import java.time.Instant;

/**
 * Registro inicial asistido en curso (D-052). No es una tabla del modelo lógico: se mantiene en
 * memoria hasta que el registro finaliza y se crean USUARIOS y DISPOSITIVOS.
 *
 * MECANISMO PROVISIONAL DEL PROTOTIPO (autorizado el 2026-09-27; no es una decisión de D-052): el identificador
 * es un código aleatorio de un solo uso que el representante entrega a la persona para continuar en su propio
 * celular. Su vida de 900 s también es PROVISIONAL.
 */
public class RegistroEnCurso {

    /** Pasos del flujo D-052 cubiertos por el backend. */
    public enum Paso { INICIADO, IDENTIDAD_VALIDADA, DATOS_COMPLETOS, VOZ_ENROLADA }

    private final String codigo;
    private final String usuarioIdPrevisto;
    private final String dni;
    private final String nombres;
    private final String apellidos;
    private final Rol rol;
    private final String representanteId;
    private final Instant expira;
    private Paso paso = Paso.INICIADO;
    private String celular;
    private String pinHash;
    private PublicKey clavePublica;

    public RegistroEnCurso(String codigo, String usuarioIdPrevisto, RegistroIdentidadSimulado identidad, Rol rol,
                           String representanteId, Instant expira) {
        this.codigo = codigo;
        this.usuarioIdPrevisto = usuarioIdPrevisto;
        this.dni = identidad.dni();
        this.nombres = identidad.nombres();
        this.apellidos = identidad.apellidos();
        this.rol = rol;
        this.representanteId = representanteId;
        this.expira = expira;
    }

    public String getCodigo() { return codigo; }
    public String getUsuarioIdPrevisto() { return usuarioIdPrevisto; }
    public String getDni() { return dni; }
    public String getNombres() { return nombres; }
    public String getApellidos() { return apellidos; }
    public Rol getRol() { return rol; }
    public String getRepresentanteId() { return representanteId; }
    public Instant getExpira() { return expira; }
    public synchronized Paso getPaso() { return paso; }
    public synchronized String getCelular() { return celular; }
    public synchronized String getPinHash() { return pinHash; }
    public synchronized PublicKey getClavePublica() { return clavePublica; }

    public synchronized void validarIdentidad() { paso = Paso.IDENTIDAD_VALIDADA; }

    public synchronized void completarDatos(String celular, String pinHash, PublicKey clavePublica) {
        this.celular = celular;
        this.pinHash = pinHash;
        this.clavePublica = clavePublica;
        paso = Paso.DATOS_COMPLETOS;
    }

    public synchronized void marcarVozEnrolada() { paso = Paso.VOZ_ENROLADA; }
}
