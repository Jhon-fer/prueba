package pedido;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.sql.Timestamp;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class pedido {
    // Datos de conexión a la base de datos
    static final String DB_URL = "jdbc:mysql://localhost:3306/webcantabra";
    static final String USER = "root";
    static final String PASS = "";
    private static DefaultTableModel modeloTabla;

    public static void main(String[] args) {
        JFrame frame = new JFrame("Sistema de Pedidos - WEB-Cántabra");
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        mostrarPanelInicio(frame);
        frame.setVisible(true);
    }

    public static void mostrarPanelInicio(JFrame frame) {
        frame.getContentPane().removeAll();
        frame.setLayout(new BorderLayout());

        JPanel panelInicio = new JPanel(new BorderLayout());
        JLabel bienvenida = new JLabel("Bienvenido a WEB-Cántabra", SwingConstants.CENTER);
        bienvenida.setFont(new Font("Arial", Font.BOLD, 20));
        bienvenida.setBorder(BorderFactory.createEmptyBorder(50, 10, 20, 10));

        JPanel panelBotones = new JPanel();
        JButton botonCliente = new JButton("Gestión de Pedidos");
        JButton botonAdmin = new JButton("Menu Administrador");
        panelBotones.add(botonCliente);
        panelBotones.add(botonAdmin);

        botonCliente.addActionListener(e -> menuCliente(frame));

        botonAdmin.addActionListener(e -> menuAdmin(frame));

        panelInicio.add(bienvenida, BorderLayout.NORTH);
        panelInicio.add(panelBotones, BorderLayout.CENTER);

        frame.add(panelInicio, BorderLayout.CENTER);
        frame.revalidate();
        frame.repaint();
    }

    public static void menuCliente(JFrame frame) {
        frame.getContentPane().removeAll();
        frame.setLayout(new BorderLayout());

        // Panel de datos del cliente
        JPanel panelCliente = new JPanel(new GridLayout(5, 2, 10, 10)); // quitamos campo id_cliente porque será auto_increment
        panelCliente.setBorder(BorderFactory.createTitledBorder("Datos del Cliente"));

        JTextField tfNombre = new JTextField();
        JTextField tfCorreo = new JTextField();
        JTextField tfTelefono = new JTextField();
        JTextField tfDireccion = new JTextField();

        panelCliente.add(new JLabel("Nombre:"));
        panelCliente.add(tfNombre);
        panelCliente.add(new JLabel("Correo:"));
        panelCliente.add(tfCorreo);
        panelCliente.add(new JLabel("Teléfono:"));
        panelCliente.add(tfTelefono);
        panelCliente.add(new JLabel("Dirección:"));
        panelCliente.add(tfDireccion);

        JButton btnGuardarCliente = new JButton("Guardar Cliente");
        panelCliente.add(new JLabel()); // celda vacía para alineación
        panelCliente.add(btnGuardarCliente);

        // Tabla de productos
        DefaultTableModel modelo = new DefaultTableModel(new String[]{"ID", "Nombre", "Descripción", "Precio", "Stock"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // tabla no editable
            }
        };
        JTable tabla = new JTable(modelo);
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createTitledBorder("Seleccionar Producto"));

        // Área de mensajes
        JTextArea areaMensajes = new JTextArea(6, 30);
        areaMensajes.setEditable(false);
        JScrollPane scrollMensajes = new JScrollPane(areaMensajes);
        scrollMensajes.setBorder(BorderFactory.createTitledBorder("Proceso de Pedido"));

        JButton btnSeleccionarProducto = new JButton("Seleccionar Producto");

        JPanel panelInferior = new JPanel(new BorderLayout());
        panelInferior.add(btnSeleccionarProducto, BorderLayout.NORTH);
        panelInferior.add(scrollMensajes, BorderLayout.CENTER);

        // Guardar cliente
        btnGuardarCliente.addActionListener(e -> {
            String nombre = tfNombre.getText().trim();
            String correo = tfCorreo.getText().trim();
            String telefono = tfTelefono.getText().trim();
            String direccion = tfDireccion.getText().trim();

            if (nombre.isEmpty() || correo.isEmpty() || telefono.isEmpty() || direccion.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Por favor, complete todos los campos del cliente.");
                return;
            }

            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
                String sql = "INSERT INTO cliente (nombre, correo, telefono, direccion) VALUES (?, ?, ?, ?)";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, nombre);
                ps.setString(2, correo);
                ps.setString(3, telefono);
                ps.setString(4, direccion);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(frame, "Cliente guardado correctamente.");

                // Limpiar campos
                tfNombre.setText("");
                tfCorreo.setText("");
                tfTelefono.setText("");
                tfDireccion.setText("");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(frame, "Error al guardar cliente: " + ex.getMessage());
            }
        });

        // Cargar productos
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM producto");

            while (rs.next()) {
                Object[] fila = {
                        rs.getInt("id_producto"),
                        rs.getString("nombre"),
                        rs.getString("descripcion"),
                        rs.getDouble("precio"),
                        rs.getInt("stock")
                };
                modelo.addRow(fila);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error al cargar productos: " + e.getMessage());
        }

        // Crear pedido (sin id_cliente aquí, lo pediremos después)
        // Para simplificar, preguntamos el id_cliente con InputDialog (puedes modificar luego)
        btnSeleccionarProducto.addActionListener(e -> {
            int filaSeleccionada = tabla.getSelectedRow();
            if (filaSeleccionada == -1) {
                JOptionPane.showMessageDialog(frame, "Seleccione un producto.");
                return;
            }

            int stock = (int) modelo.getValueAt(filaSeleccionada, 4);
            if (stock <= 0) {
                areaMensajes.setText("❌ No hay comida disponible para este producto.");
                return;
            }

            String nombreProd = (String) modelo.getValueAt(filaSeleccionada, 1);
            double precio = (double) modelo.getValueAt(filaSeleccionada, 3);
            int idProducto = (int) modelo.getValueAt(filaSeleccionada, 0);

            String inputCantidad = JOptionPane.showInputDialog(frame, "Ingrese la cantidad para " + nombreProd + " (Stock disponible: " + stock + "):");
            if (inputCantidad == null) return; // Cancelar
            int cantidad;
            try {
                cantidad = Integer.parseInt(inputCantidad);
                if (cantidad <= 0 || cantidad > stock) {
                    JOptionPane.showMessageDialog(frame, "Cantidad inválida.");
                    return;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Ingrese un número válido.");
                return;
            }

            String inputIdCliente = JOptionPane.showInputDialog(frame, "Ingrese el ID del cliente para el pedido:");
            if (inputIdCliente == null) return;
            int idCliente;
            try {
                idCliente = Integer.parseInt(inputIdCliente);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "ID cliente inválido.");
                return;
            }

            // Crear pedido y obtener idPedido
            int idPedido = crearPedido(idCliente);
            if (idPedido == -1) {
                areaMensajes.setText("Error al crear pedido.");
                return;
            }

            // Guardar detalle pedido
            guardarDetallePedido(idPedido, idProducto, cantidad, precio);

            areaMensajes.setText("Pedido creado para cliente ID " + idCliente + "\nProducto: " + nombreProd + "\nCantidad: " + cantidad + "\nPrecio unitario: " + precio);

            // Preguntar si quiere agregar más productos
            int resp = JOptionPane.showConfirmDialog(frame, "¿Desea agregar otro producto?", "Ofrecer algo más", JOptionPane.YES_NO_OPTION);
            if (resp == JOptionPane.YES_OPTION) {
                areaMensajes.append("\nCliente desea agregar otro producto.");
                return;  // Permitir agregar más sin confirmar aún
            }

            // Confirmar pedido y procesar pago
            int confirmar = JOptionPane.showConfirmDialog(frame, "¿Procesar pago?", "Confirmar pedido", JOptionPane.YES_NO_CANCEL_OPTION);
            if (confirmar == JOptionPane.YES_OPTION) {
                // Validar si se realizó el pago
                int pagoRealizado = JOptionPane.showConfirmDialog(frame, "¿Se realizó el pago?", "Validar pago", JOptionPane.YES_NO_OPTION);
                if (pagoRealizado == JOptionPane.YES_OPTION) {
                    // Pago exitoso: flujo completo
                    areaMensajes.append("\n✅ Catering pagado.");
                    areaMensajes.append("\nPreparar pedido...");
                    areaMensajes.append("\nEntregar pedido...");
                    areaMensajes.append("\nEl cliente recibe el pedido.");
                    areaMensajes.append("\nFin.");

                    // Guardar registro de envío en la tabla envio
                    try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
                        // Obtenemos la dirección del cliente para el envío
                        String direccionEnvio = "";
                        String sqlDireccion = "SELECT direccion FROM cliente WHERE id_cliente = ?";
                        PreparedStatement psDir = conn.prepareStatement(sqlDireccion);
                        psDir.setInt(1, idCliente);
                        ResultSet rsDir = psDir.executeQuery();
                        if (rsDir.next()) {
                            direccionEnvio = rsDir.getString("direccion");
                        }
                        // Insertar en envio
                        String sqlEnvio = "INSERT INTO envio (id_pedido, fecha_envio, direccion_envio, estado_envio) VALUES (?, NOW(), ?, ?)";
                        PreparedStatement psEnvio = conn.prepareStatement(sqlEnvio);
                        psEnvio.setInt(1, idPedido);
                        psEnvio.setString(2, direccionEnvio);
                        psEnvio.setString(3, "En preparación");
                        psEnvio.executeUpdate();

                        areaMensajes.append("\nRegistro de envío guardado con estado 'En preparación'.");
                    } catch (SQLException ex) {
                        areaMensajes.append("\nError al guardar el registro de envío: " + ex.getMessage());
                    }

                } else {
                    // Pago no realizado, preguntar por pago alternativo
                    int pagoAlternativo = JOptionPane.showConfirmDialog(frame, "¿Es posible un pago alternativo?", "Pago alternativo", JOptionPane.YES_NO_OPTION);
                    if (pagoAlternativo == JOptionPane.YES_OPTION) {
                        areaMensajes.append("\nSolicitud de pago alternativo.");
                        areaMensajes.append("\nEsperando pago alternativo...");
                        // Aquí podrías agregar lógica para manejar el pago alternativo, como reintentos o métodos de pago distintos
                    } else {
                        areaMensajes.append("\n❌ Pedido cancelado por falta de pago.");
                        // Opcional: eliminar pedido y detalles para mantener consistencia
                        eliminarPedido(idPedido);
                        areaMensajes.append("\nPedido eliminado.");
                    }
                }
            } else if (confirmar == JOptionPane.NO_OPTION) {
                areaMensajes.append("\n❌ Pedido cancelado.\nFin.");
                // Opcional: eliminar pedido si se desea
                eliminarPedido(idPedido);
            } else {
                areaMensajes.append("\n❌ Operación cancelada.\nFin.");
            }
        });

        frame.add(panelCliente, BorderLayout.NORTH);
        frame.add(scroll, BorderLayout.CENTER);
        frame.add(panelInferior, BorderLayout.SOUTH);

        frame.revalidate();
        frame.repaint();
    }
    // Método para crear pedido y obtener id autogenerado
    public static int crearPedido(int idCliente) {
        int idPedido = -1;
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            String sql = "INSERT INTO pedido (id_cliente) VALUES (?)";
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, idCliente);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                idPedido = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return idPedido;
    }
    public static void guardarDetallePedido(int idPedido, int idProducto, int cantidad, double precioUnitario) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            String sql = "INSERT INTO detalle_pedido (id_pedido, id_producto, cantidad, precio_unitario) VALUES (?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, idPedido);
            ps.setInt(2, idProducto);
            ps.setInt(3, cantidad);
            ps.setDouble(4, precioUnitario);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(null, "Detalle del pedido guardado correctamente.");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error al guardar el detalle del pedido.");
        }
    }
    public static void eliminarPedido(int idPedido) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            // Eliminar detalles
            String sqlDetalle = "DELETE FROM detalle_pedido WHERE id_pedido = ?";
            PreparedStatement psDetalle = conn.prepareStatement(sqlDetalle);
            psDetalle.setInt(1, idPedido);
            psDetalle.executeUpdate();

            // Eliminar pedido
            String sqlPedido = "DELETE FROM pedido WHERE id_pedido = ?";
            PreparedStatement psPedido = conn.prepareStatement(sqlPedido);
            psPedido.setInt(1, idPedido);
            psPedido.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Menu admin
    public static void menuAdmin(JFrame frame) {
        // Limpiar el contenido anterior
        frame.getContentPane().removeAll();

        // Crear componentes del menú de administrador
        JLabel mensaje = new JLabel("Menú Administrador", SwingConstants.CENTER);
        mensaje.setFont(new Font("Arial", Font.BOLD, 20));

        // Botones de acción
        JButton botonGestionClientes = new JButton("Gestión de Clientes");
        JButton botonEnvios = new JButton("Mostrar y Editar Envios");
        JButton botonGenerarReportesPedido = new JButton("Generar Reportes Pedidos");
        JButton botonReporteDetalle = new JButton("Generar Detalles Pedido");
        JButton botonreportesProductos = new JButton("Generar Productos");
        JButton botonAtras = new JButton("Atrás");

        // Acciones para cada botón
        botonGestionClientes.addActionListener(e -> gestionClientes(frame));
        botonEnvios.addActionListener(e -> mostrarEditarEnvios(frame));
        botonGenerarReportesPedido.addActionListener(e -> generarReportesPedidos(frame));
        botonReporteDetalle.addActionListener(e -> reportesDetallesPedido(frame));
        botonreportesProductos.addActionListener(e -> reportesProductos(frame));
        botonAtras.addActionListener(e -> mostrarPanelInicio(frame));

        // Panel de botones
        JPanel panelBotones = new JPanel(new GridLayout(5, 1, 10, 10)); // distribución vertical
        panelBotones.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));
        panelBotones.add(botonGestionClientes);
        panelBotones.add(botonEnvios);
        panelBotones.add(botonGenerarReportesPedido);
        panelBotones.add(botonReporteDetalle);
        panelBotones.add(botonreportesProductos);
        panelBotones.add(botonAtras);

        // Panel principal del menú
        JPanel panelMenu = new JPanel(new BorderLayout());
        panelMenu.add(mensaje, BorderLayout.NORTH);
        panelMenu.add(panelBotones, BorderLayout.CENTER);

        // Agregar el panel al frame
        frame.setLayout(new BorderLayout());
        frame.add(panelMenu, BorderLayout.CENTER);
        frame.revalidate();
        frame.repaint();
    }

    public static void gestionClientes(JFrame frame) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            Statement stmt = conn.createStatement();

            ResultSet rsClientes = stmt.executeQuery("SELECT * FROM cliente");

            String[] columnas = { "ID", "Nombre", "Correo", "Teléfono", "Dirección" };
            DefaultTableModel modeloClientes = new DefaultTableModel(columnas, 0);

            while (rsClientes.next()) {
                int id = rsClientes.getInt("id_cliente");
                String nombre = rsClientes.getString("nombre");
                String correo = rsClientes.getString("correo");
                String telefono = rsClientes.getString("telefono");
                String direccion = rsClientes.getString("direccion");

                modeloClientes.addRow(new Object[]{id, nombre, correo, telefono, direccion});
            }

            JTable tablaClientes = new JTable(modeloClientes);
            JScrollPane scrollClientes = new JScrollPane(tablaClientes);

            // Panel inferior con botones
            JPanel panelBotones = new JPanel();

            JButton botonAtras = new JButton("Atrás");
            botonAtras.addActionListener(e -> menuAdmin(frame));

            JButton botonExportarPDF = new JButton("Exportar a PDF");
            botonExportarPDF.addActionListener(e -> {
                if (modeloClientes.getRowCount() == 0) {
                    JOptionPane.showMessageDialog(frame, "No hay datos para exportar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Guardar PDF");
                fileChooser.setSelectedFile(new File("clientes_reporte.pdf"));

                int userSelection = fileChooser.showSaveDialog(frame);
                if (userSelection == JFileChooser.APPROVE_OPTION) {
                    File fileToSave = fileChooser.getSelectedFile();
                    try {
                        Document document = new Document();
                        PdfWriter.getInstance(document, new FileOutputStream(fileToSave));
                        document.open();

                        PdfPTable pdfTable = new PdfPTable(modeloClientes.getColumnCount());
                        for (int i = 0; i < modeloClientes.getColumnCount(); i++) {
                            pdfTable.addCell(new PdfPCell(new Phrase(modeloClientes.getColumnName(i))));
                        }
                        for (int row = 0; row < modeloClientes.getRowCount(); row++) {
                            for (int col = 0; col < modeloClientes.getColumnCount(); col++) {
                                Object valor = modeloClientes.getValueAt(row, col);
                                pdfTable.addCell(valor != null ? valor.toString() : "");
                            }
                        }

                        document.add(pdfTable);
                        document.close();
                        JOptionPane.showMessageDialog(frame, "PDF guardado exitosamente:\n" + fileToSave.getAbsolutePath());

                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(frame, "Error al generar PDF: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });

            panelBotones.add(botonAtras);
            panelBotones.add(botonExportarPDF);

            // Configurar el frame
            frame.getContentPane().removeAll();
            frame.setLayout(new BorderLayout());
            frame.add(scrollClientes, BorderLayout.CENTER);
            frame.add(panelBotones, BorderLayout.SOUTH);
            frame.revalidate();
            frame.repaint();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error al cargar los datos: " + e.getMessage());
        }
    }

    public static void mostrarEditarEnvios(JFrame frame) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM envio");

            String[] columnas = {"id_envio", "id_pedido", "fecha_envio", "direccion_envio", "estado_envio"};
            DefaultTableModel modelo = new DefaultTableModel(columnas, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false; // Ninguna celda editable directamente
                }
            };

            while (rs.next()) {
                int idEnvio = rs.getInt("id_envio");
                int idPedido = rs.getInt("id_pedido");
                String fechaEnvio = rs.getString("fecha_envio");
                String direccion = rs.getString("direccion_envio");
                String estado = rs.getString("estado_envio");

                modelo.addRow(new Object[]{idEnvio, idPedido, fechaEnvio, direccion, estado});
            }

            JTable tabla = new JTable(modelo);
            JScrollPane scrollPane = new JScrollPane(tabla);

            // Botón para cambiar el estado del envío seleccionado
            JButton botonCambiarEstado = new JButton("Cambiar Estado del Envío");
            botonCambiarEstado.addActionListener(e -> {
                int filaSeleccionada = tabla.getSelectedRow();
                if (filaSeleccionada == -1) {
                    JOptionPane.showMessageDialog(frame, "Seleccione un envío para cambiar el estado.");
                    return;
                }

                String nuevoEstado = JOptionPane.showInputDialog(frame, "Ingrese nuevo estado:");
                if (nuevoEstado != null && !nuevoEstado.trim().isEmpty()) {
                    int idEnvio = (int) modelo.getValueAt(filaSeleccionada, 0);

                    try (Connection conn2 = DriverManager.getConnection(DB_URL, USER, PASS);
                         PreparedStatement ps = conn2.prepareStatement("UPDATE envio SET estado_envio=? WHERE id_envio=?")) {

                        ps.setString(1, nuevoEstado);
                        ps.setInt(2, idEnvio);
                        ps.executeUpdate();

                        modelo.setValueAt(nuevoEstado, filaSeleccionada, 4); // columna estado_envio
                        JOptionPane.showMessageDialog(frame, "Estado actualizado correctamente.");

                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(frame, "Error al actualizar estado: " + ex.getMessage());
                    }
                }
            });

            // Botón para exportar a PDF
            JButton botonExportarPDF = new JButton("Exportar a PDF");
            botonExportarPDF.addActionListener(e -> {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Guardar PDF");
                fileChooser.setSelectedFile(new File("envios.pdf"));

                int seleccion = fileChooser.showSaveDialog(frame);
                if (seleccion == JFileChooser.APPROVE_OPTION) {
                    File archivoPDF = fileChooser.getSelectedFile();
                    try {
                        Document document = new Document();
                        PdfWriter.getInstance(document, new FileOutputStream(archivoPDF));
                        document.open();

                        PdfPTable pdfTable = new PdfPTable(modelo.getColumnCount());
                        for (int i = 0; i < modelo.getColumnCount(); i++) {
                            pdfTable.addCell(new PdfPCell(new Phrase(modelo.getColumnName(i))));
                        }

                        for (int row = 0; row < modelo.getRowCount(); row++) {
                            for (int col = 0; col < modelo.getColumnCount(); col++) {
                                Object value = modelo.getValueAt(row, col);
                                pdfTable.addCell(value != null ? value.toString() : "");
                            }
                        }

                        document.add(pdfTable);
                        document.close();
                        JOptionPane.showMessageDialog(frame, "PDF generado correctamente:\n" + archivoPDF.getAbsolutePath());
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(frame, "Error al generar PDF: " + ex.getMessage());
                    }
                }
            });

            // Botón Atrás
            JButton botonAtras = new JButton("Atrás");
            botonAtras.addActionListener(e -> menuAdmin(frame));

            // Panel de botones
            JPanel panelBotones = new JPanel();
            panelBotones.add(botonCambiarEstado);
            panelBotones.add(botonExportarPDF);
            panelBotones.add(botonAtras);

            frame.getContentPane().removeAll();
            frame.setLayout(new BorderLayout());
            frame.add(scrollPane, BorderLayout.CENTER);
            frame.add(panelBotones, BorderLayout.SOUTH);
            frame.revalidate();
            frame.repaint();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error al cargar datos de envío: " + e.getMessage());
        }
    }

    public static void generarReportesPedidos(JFrame frame) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            Statement stmt = conn.createStatement();
            ResultSet rsPedidos = stmt.executeQuery("SELECT * FROM pedido");

            String[] columnas = {"ID Pedido", "ID Cliente", "Fecha Pedido", "Estado"};
            DefaultTableModel modelo = new DefaultTableModel(columnas, 0);

            while (rsPedidos.next()) {
                int idPedido = rsPedidos.getInt("id_pedido");
                int idCliente = rsPedidos.getInt("id_cliente");
                Timestamp fecha = rsPedidos.getTimestamp("fecha_pedido");
                String estado = rsPedidos.getString("estado");
                modelo.addRow(new Object[]{idPedido, idCliente, fecha, estado});
            }

            JTable tablaPedidos = new JTable(modelo);
            JScrollPane scroll = new JScrollPane(tablaPedidos);

            // Botón para cambiar el estado del pedido seleccionado
            JButton botonCambiarEstado = new JButton("Cambiar Estado");
            botonCambiarEstado.addActionListener(e -> {
                int filaSeleccionada = tablaPedidos.getSelectedRow();
                if (filaSeleccionada == -1) {
                    JOptionPane.showMessageDialog(frame, "Seleccione un pedido para cambiar el estado.");
                    return;
                }

                String nuevoEstado = JOptionPane.showInputDialog(frame, "Ingrese nuevo estado:");
                if (nuevoEstado != null && !nuevoEstado.trim().isEmpty()) {
                    int idPedido = (int) modelo.getValueAt(filaSeleccionada, 0);

                    // Crear una nueva conexión aquí
                    try (Connection conn2 = DriverManager.getConnection(DB_URL, USER, PASS);
                         PreparedStatement ps = conn2.prepareStatement("UPDATE pedido SET estado=? WHERE id_pedido=?")) {

                        ps.setString(1, nuevoEstado);
                        ps.setInt(2, idPedido);
                        ps.executeUpdate();

                        modelo.setValueAt(nuevoEstado, filaSeleccionada, 3);
                        JOptionPane.showMessageDialog(frame, "Estado actualizado correctamente.");

                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(frame, "Error al actualizar estado: " + ex.getMessage());
                    }
                }
            });

            // Botón para exportar a PDF
            JButton botonExportarPDF = new JButton("Exportar a PDF");
            botonExportarPDF.addActionListener(e -> {
                if (modelo.getRowCount() == 0) {
                    JOptionPane.showMessageDialog(frame, "No hay datos para exportar.");
                    return;
                }

                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Guardar PDF");
                fileChooser.setSelectedFile(new File("reporte_pedidos.pdf"));

                int opcion = fileChooser.showSaveDialog(frame);
                if (opcion == JFileChooser.APPROVE_OPTION) {
                    File fileToSave = fileChooser.getSelectedFile();
                    try {
                        Document document = new Document();
                        PdfWriter.getInstance(document, new FileOutputStream(fileToSave));
                        document.open();

                        PdfPTable pdfTable = new PdfPTable(modelo.getColumnCount());
                        for (int i = 0; i < modelo.getColumnCount(); i++) {
                            pdfTable.addCell(new PdfPCell(new Phrase(modelo.getColumnName(i))));
                        }
                        for (int fila = 0; fila < modelo.getRowCount(); fila++) {
                            for (int col = 0; col < modelo.getColumnCount(); col++) {
                                pdfTable.addCell(modelo.getValueAt(fila, col).toString());
                            }
                        }

                        document.add(pdfTable);
                        document.close();

                        JOptionPane.showMessageDialog(frame, "PDF exportado exitosamente.");
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(frame, "Error al exportar PDF: " + ex.getMessage());
                    }
                }
            });

            // Botón atrás
            JButton botonAtras = new JButton("Atrás");
            botonAtras.addActionListener(e -> menuAdmin(frame));

            // Panel de botones
            JPanel panelBotones = new JPanel();
            panelBotones.add(botonCambiarEstado);
            panelBotones.add(botonExportarPDF);
            panelBotones.add(botonAtras);

            // Actualizar el frame
            frame.getContentPane().removeAll();
            frame.setLayout(new BorderLayout());
            frame.add(scroll, BorderLayout.CENTER);
            frame.add(panelBotones, BorderLayout.SOUTH);
            frame.revalidate();
            frame.repaint();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error al cargar pedidos: " + e.getMessage());
        }
    }

    public static void reportesDetallesPedido(JFrame frame) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM detalle_pedido");

            // Columnas de la tabla
            String[] columnas = { "id_detalle", "id_pedido", "id_producto", "cantidad", "precio_unitario" };
            DefaultTableModel modelo = new DefaultTableModel(columnas, 0);

            while (rs.next()) {
                int idDetalle = rs.getInt("id_detalle");
                int idPedido = rs.getInt("id_pedido");
                int idProducto = rs.getInt("id_producto");
                int cantidad = rs.getInt("cantidad");
                double precio = rs.getDouble("precio_unitario");

                modelo.addRow(new Object[]{idDetalle, idPedido, idProducto, cantidad, precio});
            }

            JTable tabla = new JTable(modelo);
            JScrollPane scrollPane = new JScrollPane(tabla);

            // Botones
            JButton botonAtras = new JButton("Atrás");
            botonAtras.addActionListener(e -> menuAdmin(frame));

            JButton botonExportarPDF = new JButton("Exportar a PDF");
            botonExportarPDF.addActionListener(e -> {
                if (modelo.getRowCount() == 0) {
                    JOptionPane.showMessageDialog(frame, "No hay datos para exportar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Guardar PDF");
                fileChooser.setSelectedFile(new File("detalle_pedidos.pdf"));

                int seleccion = fileChooser.showSaveDialog(frame);
                if (seleccion == JFileChooser.APPROVE_OPTION) {
                    File archivoPDF = fileChooser.getSelectedFile();
                    try {
                        Document document = new Document();
                        PdfWriter.getInstance(document, new FileOutputStream(archivoPDF));
                        document.open();

                        PdfPTable pdfTable = new PdfPTable(modelo.getColumnCount());
                        for (int i = 0; i < modelo.getColumnCount(); i++) {
                            pdfTable.addCell(new PdfPCell(new Phrase(modelo.getColumnName(i))));
                        }

                        for (int row = 0; row < modelo.getRowCount(); row++) {
                            for (int col = 0; col < modelo.getColumnCount(); col++) {
                                Object valor = modelo.getValueAt(row, col);
                                pdfTable.addCell(valor != null ? valor.toString() : "");
                            }
                        }

                        document.add(pdfTable);
                        document.close();
                        JOptionPane.showMessageDialog(frame, "PDF generado exitosamente:\n" + archivoPDF.getAbsolutePath());
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(frame, "Error al generar PDF: " + ex.getMessage());
                    }
                }
            });

            // Panel de botones
            JPanel panelBotones = new JPanel();
            panelBotones.add(botonAtras);
            panelBotones.add(botonExportarPDF);

            // Mostrar en el frame
            frame.getContentPane().removeAll();
            frame.setLayout(new BorderLayout());
            frame.add(scrollPane, BorderLayout.CENTER);
            frame.add(panelBotones, BorderLayout.SOUTH);
            frame.revalidate();
            frame.repaint();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error al cargar los detalles del pedido: " + e.getMessage());
        }
    }

    public static void reportesProductos(JFrame frame) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM producto");

            // Definir columnas
            String[] columnas = { "id_producto", "nombre", "descripcion", "precio", "stock" };
            DefaultTableModel modelo = new DefaultTableModel(columnas, 0);

            while (rs.next()) {
                int id = rs.getInt("id_producto");
                String nombre = rs.getString("nombre");
                String descripcion = rs.getString("descripcion");
                double precio = rs.getDouble("precio");
                int stock = rs.getInt("stock");

                modelo.addRow(new Object[]{id, nombre, descripcion, precio, stock});
            }

            JTable tabla = new JTable(modelo);
            JScrollPane scrollPane = new JScrollPane(tabla);

            // Botones
            JButton botonAtras = new JButton("Atrás");
            botonAtras.addActionListener(e -> menuAdmin(frame));

            JButton botonExportarPDF = new JButton("Exportar a PDF");
            botonExportarPDF.addActionListener(e -> {
                if (modelo.getRowCount() == 0) {
                    JOptionPane.showMessageDialog(frame, "No hay datos para exportar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Guardar PDF");
                fileChooser.setSelectedFile(new File("productos.pdf"));

                int seleccion = fileChooser.showSaveDialog(frame);
                if (seleccion == JFileChooser.APPROVE_OPTION) {
                    File archivoPDF = fileChooser.getSelectedFile();
                    try {
                        Document document = new Document();
                        PdfWriter.getInstance(document, new FileOutputStream(archivoPDF));
                        document.open();

                        PdfPTable pdfTable = new PdfPTable(modelo.getColumnCount());
                        for (int i = 0; i < modelo.getColumnCount(); i++) {
                            pdfTable.addCell(new PdfPCell(new Phrase(modelo.getColumnName(i))));
                        }

                        for (int row = 0; row < modelo.getRowCount(); row++) {
                            for (int col = 0; col < modelo.getColumnCount(); col++) {
                                Object valor = modelo.getValueAt(row, col);
                                pdfTable.addCell(valor != null ? valor.toString() : "");
                            }
                        }

                        document.add(pdfTable);
                        document.close();
                        JOptionPane.showMessageDialog(frame, "PDF generado exitosamente:\n" + archivoPDF.getAbsolutePath());
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(frame, "Error al generar PDF: " + ex.getMessage());
                    }
                }
            });

            // Panel de botones
            JPanel panelBotones = new JPanel();
            panelBotones.add(botonAtras);
            panelBotones.add(botonExportarPDF);

            // Mostrar en el frame
            frame.getContentPane().removeAll();
            frame.setLayout(new BorderLayout());
            frame.add(scrollPane, BorderLayout.CENTER);
            frame.add(panelBotones, BorderLayout.SOUTH);
            frame.revalidate();
            frame.repaint();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error al cargar los productos: " + e.getMessage());
        }
    }
}