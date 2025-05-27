package co.edu.unbosque.service;

import weka.classifiers.trees.J48;
import weka.core.*;
import org.springframework.stereotype.Service;
import co.edu.unbosque.model.ArchivoModel;
import java.util.*;

@Service
public class ArbolDecisionService {

    private String ultimoArbolTexto;
    private String explicacionHtml;

    public String getUltimoArbolTexto() {
        return ultimoArbolTexto;
    }

    public String getExplicacionHtml() {
        return explicacionHtml;
    }

    public List<String> entrenarYPredecir(ArchivoModel model) throws Exception {
        List<List<String>> datos = model.getDatos();
        List<String> columnas = model.getColumnas();
        int columnaObjetivo = model.getColumnaObjetivo();

        StringBuilder html = new StringBuilder();
        
        // Inicio del contenedor principal
        html.append("<div class='algorithm-explanation'>");
        
        // Encabezado
        html.append("<div class='explanation-header mb-4'>");
        html.append("<h3 class='text-primary'><i class='bi bi-diagram-3-fill me-2'></i>Proceso de Árbol de Decisión</h3>");
        html.append("<p class='text-muted'>Explicación paso a paso del algoritmo aplicado</p>");
        html.append("</div>");

        // Paso 1: Construcción del conjunto de datos
        html.append("<div class='explanation-step mb-4'>");
        html.append("<h5 class='step-title'><span class='step-number'>1</span> Construcción del conjunto de datos</h5>");
        html.append("<div class='step-content'>");
        html.append("<p>Se crearon atributos nominales a partir de las columnas originales:</p>");
        html.append("<div class='table-responsive'>");
        html.append("<table class='table table-bordered'>");
        html.append("<thead><tr><th>Atributo</th><th>Valores</th></tr></thead>");
        html.append("<tbody>");

        ArrayList<Attribute> atributos = new ArrayList<>();
        for (int j = 0; j < columnas.size(); j++) {
            Set<String> valores = new LinkedHashSet<>();
            for (List<String> fila : datos) {
                String val = fila.get(j);
                if (!val.equals("?")) valores.add(val);
            }
            html.append("<tr><td><strong>").append(columnas.get(j)).append("</strong></td>")
                .append("<td>").append(valores.toString()).append("</td></tr>");
            atributos.add(new Attribute(columnas.get(j), new ArrayList<>(valores)));
        }
        html.append("</tbody></table></div></div></div>");

        Instances dataset = new Instances("Datos", atributos, datos.size());
        dataset.setClassIndex(columnaObjetivo);

        List<Instance> instanciasFaltantes = new ArrayList<>();
        List<Integer> filasFaltantes = new ArrayList<>();

        // Paso 2: Identificación de valores faltantes
        html.append("<div class='explanation-step mb-4'>");
        html.append("<h5 class='step-title'><span class='step-number'>2</span> Identificación de valores faltantes</h5>");
        html.append("<div class='step-content'>");
        html.append("<p>Se identificaron las filas con valores faltantes en la columna objetivo:</p>");
        html.append("<ul class='list-group'>");

        for (int i = 0; i < datos.size(); i++) {
            List<String> fila = datos.get(i);
            DenseInstance instancia = new DenseInstance(columnas.size());
            instancia.setDataset(dataset);

            boolean tieneFaltante = fila.get(columnaObjetivo).equals("?");

            for (int j = 0; j < columnas.size(); j++) {
                if (fila.get(j).equals("?")) {
                    instancia.setMissing(j);
                } else {
                    instancia.setValue(atributos.get(j), fila.get(j));
                }
            }

            if (tieneFaltante) {
                instanciasFaltantes.add(instancia);
                filasFaltantes.add(i);
                html.append("<li class='list-group-item'>Fila <span class='badge bg-secondary'>")
                    .append(i + 1).append("</span> tiene valor faltante en <code>")
                    .append(columnas.get(columnaObjetivo)).append("</code></li>");
            } else {
                dataset.add(instancia);
            }
        }
        html.append("</ul></div></div>");

        // Paso 3: Entrenamiento del árbol
        html.append("<div class='explanation-step mb-4'>");
        html.append("<h5 class='step-title'><span class='step-number'>3</span> Entrenamiento del árbol de decisión</h5>");
        html.append("<div class='step-content'>");
        html.append("<p>Se entrenó el algoritmo <strong>J48</strong> (implementación WEKA de C4.5) con las instancias completas.</p>");
        
        html.append("<div class='alert alert-info'>");
        html.append("<h6><i class='bi bi-info-circle-fill me-2'></i>Métrica de selección de atributos</h6>");
        html.append("<p>El algoritmo usa el índice de <strong>ganancia de información</strong> para seleccionar los mejores atributos:</p>");
        html.append("<div class='math-formula bg-light p-3 rounded'>");
        html.append("\\[ IG(S, A) = Entropía(S) - \\sum_{v \\in valores(A)} \\frac{|S_v|}{|S|} Entropía(S_v) \\]");
        html.append("<p class='mt-2 mb-0 text-muted small'>Donde:</p>");
        html.append("<ul class='small'>");
        html.append("<li><strong>IG(S, A)</strong>: Ganancia de información del atributo A sobre el conjunto S</li>");
        html.append("<li><strong>Entropía(S)</strong>: Medida de impureza del conjunto S</li>");
        html.append("<li><strong>S<sub>v</sub></strong>: Subconjunto donde A = v</li>");
        html.append("</ul></div></div>");

        J48 arbol = new J48();
        arbol.buildClassifier(dataset);
        this.ultimoArbolTexto = arbol.toString();

        html.append("<h6 class='mt-4'><i class='bi bi-code-square me-2'></i>Estructura del árbol generado:</h6>");
        html.append("<div class='tree-structure bg-dark text-light p-3 rounded'>");
        html.append("<pre><code class='text-white'>").append(this.ultimoArbolTexto).append("</code></pre>");
        html.append("</div></div></div>");

        // Paso 4: Predicción de valores
        html.append("<div class='explanation-step mb-4'>");
        html.append("<h5 class='step-title'><span class='step-number'>4</span> Predicción de valores faltantes</h5>");
        html.append("<div class='step-content'>");
        html.append("<p>Se aplicó el modelo entrenado para predecir los valores faltantes:</p>");
        html.append("<ul class='list-group'>");

        List<String> resultados = new ArrayList<>();
        for (int i = 0; i < instanciasFaltantes.size(); i++) {
            Instance inst = instanciasFaltantes.get(i);
            double pred = arbol.classifyInstance(inst);
            String predStr = dataset.classAttribute().value((int) pred);

            int fila = filasFaltantes.get(i);
            datos.get(fila).set(columnaObjetivo, predStr);
            String res = "Fila " + (fila + 1) + " → " + predStr;
            resultados.add(res);
            
            html.append("<li class='list-group-item d-flex justify-content-between align-items-center'>");
            html.append("<span>Fila <span class='badge bg-primary'>").append(fila + 1).append("</span></span>");
            html.append("<span class='badge bg-success'>").append(predStr).append("</span>");
            html.append("</li>");
        }
        html.append("</ul></div></div>");

        // Resumen final
        html.append("<div class='alert alert-success mt-4'>");
        html.append("<h5 class='alert-heading'><i class='bi bi-check-circle-fill me-2'></i>Proceso completado</h5>");
        html.append("<p>Se actualizaron <strong>").append(resultados.size())
            .append("</strong> valores faltantes en la columna <code>")
            .append(columnas.get(columnaObjetivo)).append("</code>.</p>");
        html.append("</div>");

        html.append("</div>"); // Cierre del contenedor principal

        this.explicacionHtml = html.toString();
        return resultados;
    }
}