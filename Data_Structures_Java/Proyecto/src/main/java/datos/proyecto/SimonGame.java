/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package datos.proyecto;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Arc2D;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Random;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

public class SimonGame {

    private JFrame ventana;
    private PanelSimon panelSimon;
    private JLabel etiquetaEstado;
    private ArrayList<ArcoColor> arcos;
    private ArrayList<Integer> secuencia;
    private int pasoActual;
    private int nivel;

    public SimonGame() {
        ventana = new JFrame("Juego Simon");
        ventana.setSize(500, 550);
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.setLayout(new BorderLayout());
        ventana.setLocationRelativeTo(null); // Centrar ventana

        panelSimon = new PanelSimon();
        ventana.add(panelSimon, BorderLayout.CENTER);

        etiquetaEstado = new JLabel("Mira la secuencia", SwingConstants.CENTER);
        ventana.add(etiquetaEstado, BorderLayout.SOUTH);

        arcos = new ArrayList<>();
        secuencia = new ArrayList<>();

        arcos.add(new ArcoColor(Color.RED, 0));
        arcos.add(new ArcoColor(Color.BLUE, 90));
        arcos.add(new ArcoColor(Color.GREEN, 180));
        arcos.add(new ArcoColor(Color.YELLOW, 270));

        nivel = 0;

        ventana.setVisible(true);
        iniciarJuego();
    }

    public void iniciarJuego() {
        secuencia.clear();
        agregarPasoASecuencia();
    }

    public void agregarPasoASecuencia() {
        Random rand = new Random();
        secuencia.add(rand.nextInt(4));
        pasoActual = 0;
        nivel++;
        jugarSecuencia();
    }

    public void jugarSecuencia() {
        etiquetaEstado.setText("Mira la secuencia");
        for (int i = 0; i < secuencia.size(); i++) {
            int indiceFinal = i;
            Timer temporizador = new Timer(1000 * (i + 1), new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    arcos.get(secuencia.get(indiceFinal)).iluminar();
                    reproducirSonido(secuencia.get(indiceFinal) + 1); // +1 para obtener números del 1 al 4
                    panelSimon.repaint();
                    Timer temporizadorRetornoColor = new Timer(500, new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            arcos.get(secuencia.get(indiceFinal)).detenerIluminacion();
                            panelSimon.repaint();
                            if (indiceFinal == secuencia.size() - 1) {
                                etiquetaEstado.setText("Es tu turno");
                            }
                        }
                    });
                    temporizadorRetornoColor.setRepeats(false);
                    temporizadorRetornoColor.start();
                }
            });
            temporizador.setRepeats(false);
            temporizador.start();
        }
    }

    public void verificarSecuencia(ArcoColor arco) {
        reproducirSonido(arcos.indexOf(arco) + 1); // +1 para obtener números del 1 al 4
        if (arco.getColor() == arcos.get(secuencia.get(pasoActual)).getColor()) {
            pasoActual++;
            if (pasoActual == secuencia.size()) {
                agregarPasoASecuencia();
            }
        } else {
            mostrarPantallaFinal();
        }
    }

    public void mostrarPantallaFinal() {
        ventana.remove(panelSimon);
        ventana.remove(etiquetaEstado);

        JPanel panelFinal = new JPanel(new BorderLayout());
        JLabel etiquetaFinal = new JLabel("Quedaste en el nivel: ", SwingConstants.CENTER);
        panelFinal.add(etiquetaFinal, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel();
        JButton btnReintentar = new JButton("Volver a intentar");
        JButton btnSalir = new JButton("Salir");

        btnReintentar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ventana.remove(panelFinal);
                ventana.add(panelSimon, BorderLayout.CENTER);
                ventana.add(etiquetaEstado, BorderLayout.SOUTH);
                nivel = 0;
                iniciarJuego();
                ventana.revalidate();
                ventana.repaint();
            }
        });

        btnSalir.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        panelBotones.add(btnReintentar);
        panelBotones.add(btnSalir);

        panelFinal.add(panelBotones, BorderLayout.SOUTH);

        ventana.add(panelFinal);
        ventana.revalidate();
        ventana.repaint();
    }

    public void reproducirSonido(int numero) {
    try {
        FileInputStream fis = new FileInputStream(numero + ".mp3");
        Player player = new Player(fis);
        new Thread(() -> {
            try {
                player.play();
            } catch (JavaLayerException e) {
                e.printStackTrace();
            }
        }).start();
    } catch (JavaLayerException | FileNotFoundException e) {
        e.printStackTrace();
    }
}   

    public static void main(String[] args) {
        new SimonGame();
    }

    class PanelSimon extends JPanel {
        public PanelSimon() {
            setBackground(Color.WHITE);
            addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent evt) {
                    int x = evt.getX();
                    int y = evt.getY();
                    for (ArcoColor arco : arcos) {
                        if (arco.contiene(x, y)) {
                            verificarSecuencia(arco);
                        }
                    }
                }

                public void mousePressed(MouseEvent evt) {
                    int x = evt.getX();
                    int y = evt.getY();
                    for (ArcoColor arco : arcos) {
                        if (arco.contiene(x, y)) {
                            arco.setPresionado(true);
                            repaint();
                        }
                    }
                }

                public void mouseReleased(MouseEvent evt) {
                    for (ArcoColor arco : arcos) {
                        arco.setPresionado(false);
                    }
                    repaint();
                }

                public void mouseExited(MouseEvent evt) {
                    for (ArcoColor arco : arcos) {
                        arco.setResaltado(false);
                    }
                    repaint();
                }
            });

            addMouseMotionListener(new MouseMotionAdapter() {
                public void mouseMoved(MouseEvent evt) {
                    int x = evt.getX();
                    int y = evt.getY();
                    boolean estaResaltado = false;
                    for (ArcoColor arco : arcos) {
                        if (arco.contiene(x, y)) {
                            arco.setResaltado(true);
                            estaResaltado = true;
                        } else {
                            arco.setResaltado(false);
                        }
                    }
                    if (estaResaltado) {
                        repaint();
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int centroX = getWidth() / 2;
            int centroY = getHeight() / 2;
            int radio = 200;

            g2d.setColor(Color.WHITE);
            g2d.fillOval(centroX - radio, centroY - radio, 2 * radio, 2 * radio);
            g2d.setColor(Color.BLACK);
            g2d.drawOval(centroX - radio, centroY - radio, 2 * radio, 2 * radio);

            for (ArcoColor arco : arcos) {
                g2d.setColor(arco.getColorActual());
                g2d.fill(arco.getForma(centroX, centroY, radio));
                g2d.setColor(Color.BLACK); // Borde negro
                g2d.setStroke(new BasicStroke(10)); // Grosor del borde
                g2d.draw(arco.getForma(centroX, centroY, radio));
            }
        }
    }

    class ArcoColor {
        private Color color;
        private int anguloInicio;
        private boolean estaIluminado;
        private boolean estaResaltado;
        private boolean estaPresionado;

        public ArcoColor(Color color, int anguloInicio) {
            this.color = color;
            this.anguloInicio = anguloInicio;
            this.estaIluminado = false;
            this.estaResaltado = false;
            this.estaPresionado = false;
        }

        public Color getColor() {
            return color;
        }

        public Color getColorActual() {
            if (estaIluminado) return Color.BLACK;
            if (estaPresionado) return color.darker().darker();
            if (estaResaltado) return color.darker();
            return color;
        }

        public Shape getForma(int x, int y, int r) {
            return new Arc2D.Double(x - r, y - r, 2 * r, 2 * r, anguloInicio, 90, Arc2D.PIE);
        }

        public void iluminar() {
            estaIluminado = true;
        }

        public void detenerIluminacion() {
            estaIluminado = false;
        }

        public boolean contiene(int x, int y) {
            return getForma(ventana.getWidth() / 2, ventana.getHeight() / 2, 200).contains(x, y);
        }

        public void setResaltado(boolean resaltado) {
            estaResaltado = resaltado;
        }

        public void setPresionado(boolean presionado) {
            estaPresionado = presionado;
        }
    }
}
