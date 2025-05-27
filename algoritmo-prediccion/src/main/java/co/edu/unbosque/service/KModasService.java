package co.edu.unbosque.service;

import org.springframework.stereotype.Service;
import co.edu.unbosque.model.ArchivoModel;
import java.util.*;

@Service
public class KModasService {

    public Map<String, Object> imputarValoresConKModas(ArchivoModel model) {
        List<List<String>> filas = model.getDatos();
        List<String> columnas = model.getColumnas();
        int columnaObjetivo = model.getColumnaObjetivo();
        int categIndex = detectarColumnaCateg(filas, columnaObjetivo);
        
        // Construir explicación HTML
        StringBuilder html = new StringBuilder();
        html.append("<div class='algorithm-explanation'>");
        html.append("<div class='explanation-header mb-4'>");
        html.append("<h3 class='text-primary'><i class='bi bi-pie-chart-fill me-2'></i>Proceso de K-Modas</h3>");
        html.append("<p class='text-muted'>Imputación de valores categóricos faltantes usando la moda por categoría</p>");
        html.append("</div>");

        // Paso 1: Agrupación de datos
        html.append("<div class='explanation-step mb-4'>");
        html.append("<h5 class='step-title'><span class='step-number'>1</span> Agrupación por categorías</h5>");
        html.append("<div class='step-content'>");
        html.append("<p>Se agruparon los datos por la columna categórica <code>").append(columnas.get(categIndex)).append("</code>:</p>");
        
        Map<String, List<String>> grupos = new LinkedHashMap<>();
        for (List<String> fila : filas) {
            String categoria = fila.get(categIndex);
            String valor = fila.get(columnaObjetivo);
            if (!valor.equals("?")) {
                grupos.computeIfAbsent(categoria, k -> new ArrayList<>()).add(valor);
            }
        }

        html.append("<div class='table-responsive'>");
        html.append("<table class='table table-bordered'>");
        html.append("<thead><tr><th>Categoría</th><th>Valores</th><th>Cantidad</th></tr></thead>");
        html.append("<tbody>");
        for (Map.Entry<String, List<String>> entry : grupos.entrySet()) {
            html.append("<tr><td>").append(entry.getKey()).append("</td>");
            html.append("<td>").append(entry.getValue().toString()).append("</td>");
            html.append("<td>").append(entry.getValue().size()).append("</td></tr>");
        }
        html.append("</tbody></table></div></div></div>");

        // Paso 2: Cálculo de modas
        html.append("<div class='explanation-step mb-4'>");
        html.append("<h5 class='step-title'><span class='step-number'>2</span> Cálculo de modas</h5>");
        html.append("<div class='step-content'>");
        html.append("<p>Se calculó la moda para cada categoría:</p>");
        html.append("<div class='math-formula'>");
        html.append("<p>La <strong>moda</strong> es el valor que aparece con mayor frecuencia en un conjunto de datos:</p>");
        html.append("\\[ \\mathrm{moda} = \\arg\\max_v \\mathrm{frecuencia}(v) \\]");
        html.append("</div>");
        html.append("<div class='table-responsive mt-3'>");
        html.append("<table class='table table-bordered'>");
        html.append("<thead><tr><th>Categoría</th><th>Valores</th><th>Frecuencias</th><th>Moda</th></tr></thead>");
        html.append("<tbody>");

        Map<String, String> modas = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : grupos.entrySet()) {
            String cat = entry.getKey();
            List<String> valores = entry.getValue();
            
            // Calcular frecuencias
            Map<String, Integer> frecuencias = new HashMap<>();
            for (String v : valores) {
                frecuencias.put(v, frecuencias.getOrDefault(v, 0) + 1);
            }
            
            // Encontrar moda
            String moda = null;
            int maxFreq = 0;
            for (Map.Entry<String, Integer> freqEntry : frecuencias.entrySet()) {
                if (freqEntry.getValue() > maxFreq) {
                    maxFreq = freqEntry.getValue();
                    moda = freqEntry.getKey();
                }
            }
            modas.put(cat, moda);

            html.append("<tr><td>").append(cat).append("</td>");
            html.append("<td>").append(valores.toString()).append("</td>");
            html.append("<td>").append(frecuencias.toString()).append("</td>");
            html.append("<td><span class='badge bg-success'>").append(moda).append("</span> (")
                .append(maxFreq).append(" veces)</td></tr>");
        }
        html.append("</tbody></table></div></div></div>");

        // Paso 3: Imputación de valores
        html.append("<div class='explanation-step mb-4'>");
        html.append("<h5 class='step-title'><span class='step-number'>3</span> Imputación de valores</h5>");
        html.append("<div class='step-content'>");
        html.append("<p>Se reemplazaron los valores faltantes (<code>?</code>) con la moda de su categoría:</p>");
        html.append("<ul class='list-group'>");

        List<List<String>> filasCompletas = new ArrayList<>();
        int contadorImputaciones = 0;
        for (List<String> fila : filas) {
            String valor = fila.get(columnaObjetivo);
            String categoria = fila.get(categIndex);

            if (valor.equals("?")) {
                String moda = modas.get(categoria);
                List<String> filaNueva = new ArrayList<>(fila);
                filaNueva.set(columnaObjetivo, moda);
                filasCompletas.add(filaNueva);
                
                html.append("<li class='list-group-item d-flex justify-content-between align-items-center'>");
                html.append("<span>Categoría <span class='badge bg-primary'>").append(categoria).append("</span>");
                html.append(" : Fila ").append(filasCompletas.size()).append("</span>");
                html.append("<span class='badge bg-success'>").append(moda).append("</span>");
                html.append("</li>");
                contadorImputaciones++;
            } else {
                filasCompletas.add(fila);
            }
        }
        html.append("</ul></div></div>");

        // Resumen final
        html.append("<div class='alert alert-success mt-4'>");
        html.append("<h5 class='alert-heading'><i class='bi bi-check-circle-fill me-2'></i>Proceso completado</h5>");
        html.append("<p>Se imputaron <strong>").append(contadorImputaciones)
            .append("</strong> valores faltantes en la columna <code>")
            .append(columnas.get(columnaObjetivo)).append("</code>.</p>");
        html.append("<p class='mb-0'>Usando como categoría la columna <code>")
            .append(columnas.get(categIndex)).append("</code>.</p>");
        html.append("</div>");

        html.append("</div>"); // Cierre del contenedor principal

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("filasActualizadas", filasCompletas);
        resultado.put("explicacionHtml", html.toString());
        return resultado;
    }

    // Método auxiliar para detectar columna categórica
    private int detectarColumnaCateg(List<List<String>> filas, int columnaObjetivo) {
        int numCols = filas.get(0).size();
        for (int col = 0; col < numCols; col++) {
            if (col == columnaObjetivo) continue;
            boolean esCateg = true;
            for (List<String> fila : filas) {
                String valor = fila.get(col);
                if (!valor.equals("?") && valor.matches("[-+]?\\d+(,\\d+)?")) {
                    esCateg = false;
                    break;
                }
            }
            if (esCateg) return col;
        }
        return 0;
    }
}