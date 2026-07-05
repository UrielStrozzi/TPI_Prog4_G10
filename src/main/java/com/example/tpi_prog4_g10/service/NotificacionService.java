package com.example.tpi_prog4_g10.service;

import com.example.tpi_prog4_g10.enums.EstadoNotificacion;
import com.example.tpi_prog4_g10.enums.TipoNotificacion;
import com.example.tpi_prog4_g10.model.Notificacion;
import com.example.tpi_prog4_g10.model.Subasta;
import com.example.tpi_prog4_g10.model.Usuario;
import com.example.tpi_prog4_g10.repository.NotificacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificacionService {

    @Autowired
    private NotificacionRepository notificacionRepository;

    // ── Método genérico interno ───────────────────────────────
    private void crear(Usuario destinatario, Subasta subasta,
            TipoNotificacion tipo, String titulo, String cuerpo) {
        Notificacion n = Notificacion.builder()
                .usuario(destinatario)
                .subasta(subasta)
                .tipo(tipo)
                .titulo(titulo)
                .cuerpo(cuerpo)
                .estado(EstadoNotificacion.PENDIENTE)
                .build();
        notificacionRepository.save(n);
    }

    // ── Al adjudicar: notificar ganador y vendedor ────────────
    public void notificarAdjudicacion(Subasta subasta) {
        Usuario ganador = subasta.getGanador();
        Usuario vendedor = subasta.getVendedor();

        // Al ganador
        crear(
                ganador,
                subasta,
                TipoNotificacion.SUBASTA_GANADA,
                "¡Ganaste la subasta!",
                "Ganaste la subasta de \"" + subasta.getProducto().getNombre()
                        + "\" con una oferta de $" + subasta.getPrecioFinal() + ".");

        // Al vendedor
        crear(
                vendedor,
                subasta,
                TipoNotificacion.SUBASTA_ADJUDICADA,
                "Tu subasta fue adjudicada",
                "La subasta de \"" + subasta.getProducto().getNombre()
                        + "\" fue adjudicada a " + ganador.getUsername()
                        + " por $" + subasta.getPrecioFinal() + ".");
    }

    // ── Al cancelar: notificar a todos los que pujaron ────────
    public void notificarCancelacion(Subasta subasta, List<Usuario> oferentes) {
        for (Usuario oferente : oferentes) {
            crear(
                    oferente,
                    subasta,
                    TipoNotificacion.SUBASTA_CANCELADA,
                    "Subasta cancelada",
                    "La subasta de \"" + subasta.getProducto().getNombre()
                            + "\" en la que participaste fue cancelada.");
        }
    }

    // ── Consultar notificaciones de un usuario ────────────────
    public List<Notificacion> obtenerPorUsuario(Long usuarioId) {
        return notificacionRepository.findByUsuarioIdOrderByCreatedAtDesc(usuarioId);
    }

    public void marcarComoLeida(Long id) {
        Notificacion n = notificacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notificación no encontrada."));
        n.marcarComoLeida(); // método ya existe en la entidad
        notificacionRepository.save(n);
    }
}
