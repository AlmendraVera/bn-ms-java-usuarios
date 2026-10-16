package cl.duoc.bn.inventario.config;

import cl.duoc.bn.inventario.model.Categoria;
import cl.duoc.bn.inventario.model.Producto;
import cl.duoc.bn.inventario.repository.CategoriaRepository;
import cl.duoc.bn.inventario.repository.ProductoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/** Carga categorías y productos de ejemplo al iniciar (la BD H2 en memoria parte vacía). */
@Component
public class DatosIniciales implements CommandLineRunner {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;

    public DatosIniciales(CategoriaRepository categoriaRepository, ProductoRepository productoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
    }

    @Override
    public void run(String... args) {
        if (categoriaRepository.count() > 0) {
            return;
        }
        Categoria electronica = categoriaRepository.save(new Categoria("Electrónica", "Audio, carga y accesorios"));
        Categoria hogar = categoriaRepository.save(new Categoria("Hogar", "Decoración y utensilios"));
        Categoria ropa = categoriaRepository.save(new Categoria("Ropa", "Vestuario y accesorios"));

        productoRepository.save(new Producto("AUD-001", "Audífonos Bluetooth", 19990, 50, electronica));
        productoRepository.save(new Producto("CAR-002", "Cargador USB-C 30W", 9990, 120, electronica));
        productoRepository.save(new Producto("LAM-003", "Lámpara LED de escritorio", 14990, 30, hogar));
        productoRepository.save(new Producto("POL-004", "Polerón canguro", 24990, 15, ropa));
        // Producto sin stock a propósito: sirve para demostrar el error 409 al crear una orden
        productoRepository.save(new Producto("TAZ-005", "Set de tazas cerámicas", 12990, 0, hogar));
    }
}