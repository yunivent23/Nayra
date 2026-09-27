package upc.pe.nayrabackend.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import upc.pe.nayrabackend.excepciones.NayraException;
import upc.pe.nayrabackend.securities.UsuarioAutenticado;

/** Acceso al usuario autenticado de la petición en curso. */
final class SesionActual {

    private SesionActual() {
    }

    static UsuarioAutenticado usuario() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a != null && a.getPrincipal() instanceof UsuarioAutenticado u) {
            return u;
        }
        throw NayraException.noAutorizado("NO_AUTENTICADO");
    }
}
