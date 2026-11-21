package com.utp.restacontrol.service;

import com.utp.restacontrol.dto.reserva.ReservaCreateRequest;
import com.utp.restacontrol.model.Cliente;
import com.utp.restacontrol.model.Mesa;
import com.utp.restacontrol.repository.ClienteRepository;
import com.utp.restacontrol.repository.MesaRepository;
import com.utp.restacontrol.repository.ReservaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReservaCrudServiceTest {

    private ClienteRepository clienteRepository;
    private MesaRepository mesaRepository;
    private ReservaCrudService service;

    private final UUID idCliente = UUID.randomUUID();
    private final UUID idMesa = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        // Repositorios simulados con Mockito, asi la prueba no necesita base de datos
        ReservaRepository reservaRepository = mock(ReservaRepository.class);
        clienteRepository = mock(ClienteRepository.class);
        mesaRepository = mock(MesaRepository.class);
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);

        service = new ReservaCrudService(reservaRepository, clienteRepository, mesaRepository, jdbcTemplate);

        // El cliente existe
        when(clienteRepository.findById(idCliente)).thenReturn(Optional.of(new Cliente()));

        // La mesa existe y tiene capacidad para 4 personas
        Mesa mesa = new Mesa();
        mesa.setCapacidad(4);
        when(mesaRepository.findById(idMesa)).thenReturn(Optional.of(mesa));
    }

    private ReservaCreateRequest requestValido() {
        ReservaCreateRequest request = new ReservaCreateRequest();
        request.setTipo("mesa");
        request.setIdCliente(idCliente);
        request.setNombreContacto("Cliente de prueba");
        request.setIdMesa(idMesa);
        request.setFechaHora(LocalDateTime.now().plusDays(1));
        request.setCantidadPersonas(2);
        return request;
    }

    @Test
    void crearDebeRechazarReservaConFechaPasada() {
        ReservaCreateRequest request = requestValido();
        request.setFechaHora(LocalDateTime.now().minusDays(1));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> service.crear(request));
        assertEquals("La fecha y hora de la reserva no puede ser pasada", error.getMessage());
    }

    @Test
    void crearDebeRechazarMasPersonasQueLaCapacidadDeLaMesa() {
        ReservaCreateRequest request = requestValido();
        request.setCantidadPersonas(6);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> service.crear(request));
        assertEquals("La mesa solo tiene capacidad para 4 personas", error.getMessage());
    }
}
