package co.edu.unbosque.service;

import org.springframework.stereotype.Service;
import co.edu.unbosque.model.ArchivoModel;
import java.util.*;

@Service
public class RegresionService {

    public Map<String, Object> realizarRegresion(ArchivoModel model) {
        List<List<String>> filas = model.getDatos();
        List<String> columnas = model.getColumnas();
        int columnaObjetivo = model.getColumnaObjetivo();
        List<double[]> datosCompletos = new ArrayList<>();
        List<Integer> filasAPredecir = new ArrayList<>();

        // Procesamiento inicial de datos
        for (int i = 0; i < filas.size(); i++) {
            List<String> fila = filas.get(i);
            if (!fila.get(columnaObjetivo).equals("?")) {
                double[] valores = new double[fila.size()];
                for (int j = 0; j < fila.size(); j++) {
                    valores[j] = Double.parseDouble(fila.get(j));
                }
                datosCompletos.add(valores);
            } else {
                filasAPredecir.add(i);
            }
        }

        Map<String, Object> resultado = new LinkedHashMap<>();
        StringBuilder html = new StringBuilder();

        // Encabezado de la explicación
        html.append("<div class='algorithm-explanation'>");
        html.append("<div class='explanation-header mb-4'>");
        html.append("<h3 class='text-primary'><i class='bi bi-graph-up-arrow me-2'></i>Proceso de Regresión Lineal</h3>");
        html.append("<p class='text-muted'>Explicación paso a paso del algoritmo aplicado</p>");
        html.append("</div>");

        if (columnas.size() - 1 == 1) {
            Map<String, Object> simpleResult = regresionSimple(datosCompletos, filas, columnaObjetivo, filasAPredecir, columnas, html);
            resultado.putAll(simpleResult);
        } else {
            Map<String, Object> multipleResult = regresionMultiple(datosCompletos, filas, columnaObjetivo, filasAPredecir, columnas, html);
            resultado.putAll(multipleResult);
        }

        html.append("</div>"); // Cierre del contenedor principal
        resultado.put("explicacionHtml", html.toString());
        return resultado;
    }

    private Map<String, Object> regresionSimple(List<double[]> datos, List<List<String>> filas, int colY,
            List<Integer> filasAPredecir, List<String> columnas, StringBuilder html) {
        int colX = (colY == 0) ? 1 : 0;

        // Paso 1: Cálculo de sumatorias
        html.append("<div class='explanation-step mb-4'>");
        html.append("<h5 class='step-title'><span class='step-number'>1</span> Cálculo de sumatorias</h5>");
        html.append("<div class='step-content'>");
        html.append("<p>Se calcularon las siguientes sumatorias para las variables:</p>");
        html.append("<ul>");
        html.append("<li><strong>Variable independiente (X):</strong> ").append(columnas.get(colX)).append("</li>");
        html.append("<li><strong>Variable dependiente (Y):</strong> ").append(columnas.get(colY)).append("</li>");
        html.append("</ul>");

        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        int n = datos.size();

        for (double[] fila : datos) {
            double x = fila[colX];
            double y = fila[colY];
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumX2 += x * x;
        }

        html.append("<div class='table-responsive'>");
        html.append("<table class='table table-bordered'>");
        html.append("<thead><tr><th>Sumatoria</th><th>Valor</th></tr></thead>");
        html.append("<tbody>");
        html.append("<tr><td>ΣX</td><td>").append(String.format("%.4f", sumX)).append("</td></tr>");
        html.append("<tr><td>ΣY</td><td>").append(String.format("%.4f", sumY)).append("</td></tr>");
        html.append("<tr><td>ΣXY</td><td>").append(String.format("%.4f", sumXY)).append("</td></tr>");
        html.append("<tr><td>ΣX²</td><td>").append(String.format("%.4f", sumX2)).append("</td></tr>");
        html.append("<tr><td>n</td><td>").append(n).append("</td></tr>");
        html.append("</tbody></table></div></div></div>");

        // Paso 2: Cálculo de parámetros
        html.append("<div class='explanation-step mb-4'>");
        html.append("<h5 class='step-title'><span class='step-number'>2</span> Cálculo de parámetros</h5>");
        html.append("<div class='step-content'>");

        double pendiente = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
        double intercepto = (sumY - pendiente * sumX) / n;

        html.append("<div class='math-formula'>");
        html.append("<h6>Fórmula de la pendiente (m):</h6>");
        html.append("\\[ m = \\frac{n \\sum XY - \\sum X \\sum Y}{n \\sum X^2 - (\\sum X)^2} \\]");
        html.append("<p class='mt-3'>Resultado:</p>");
        html.append("\\[ m = \\frac{" + n + " \\times " + String.format("%.4f", sumXY) + " - " + 
                   String.format("%.4f", sumX) + " \\times " + String.format("%.4f", sumY) + "}{" + 
                   n + " \\times " + String.format("%.4f", sumX2) + " - (" + String.format("%.4f", sumX) + ")^2} = " + 
                   String.format("%.4f", pendiente) + " \\]");
        html.append("</div>");

        html.append("<div class='math-formula mt-3'>");
        html.append("<h6>Fórmula del intercepto (b):</h6>");
        html.append("\\[ b = \\frac{\\sum Y - m \\sum X}{n} \\]");
        html.append("<p class='mt-3'>Resultado:</p>");
        html.append("\\[ b = \\frac{" + String.format("%.4f", sumY) + " - " + 
                   String.format("%.4f", pendiente) + " \\times " + String.format("%.4f", sumX) + "}{" + 
                   n + "} = " + String.format("%.4f", intercepto) + " \\]");
        html.append("</div></div></div>");

        // Paso 3: Modelo final
        html.append("<div class='explanation-step mb-4'>");
        html.append("<h5 class='step-title'><span class='step-number'>3</span> Modelo de regresión</h5>");
        html.append("<div class='step-content'>");
        html.append("<div class='alert alert-success'>");
        html.append("<h6><i class='bi bi-check-circle-fill me-2'></i>Ecuación final:</h6>");
        html.append("\\[ y = " + String.format("%.4f", pendiente) + " \\cdot x + " + 
                   String.format("%.4f", intercepto) + " \\]");
        html.append("</div></div></div>");

        // Paso 4: Predicciones
        html.append("<div class='explanation-step mb-4'>");
        html.append("<h5 class='step-title'><span class='step-number'>4</span> Predicciones realizadas</h5>");
        html.append("<div class='step-content'>");
        html.append("<p>Se aplicó el modelo para predecir los valores faltantes:</p>");
        html.append("<ul class='list-group'>");

        for (Integer i : filasAPredecir) {
            double x = Double.parseDouble(filas.get(i).get(colX));
            double y = pendiente * x + intercepto;
            filas.get(i).set(colY, String.format("%.4f", y));
            
            html.append("<li class='list-group-item d-flex justify-content-between align-items-center'>");
            html.append("<span>Fila <span class='badge bg-primary'>").append(i + 1).append("</span>");
            html.append(" (x = ").append(String.format("%.4f", x)).append(")</span>");
            html.append("<span class='badge bg-success'>y = ").append(String.format("%.4f", y)).append("</span>");
            html.append("</li>");
        }
        html.append("</ul></div></div>");

        // Resumen final
        html.append("<div class='alert alert-info mt-4'>");
        html.append("<h5 class='alert-heading'><i class='bi bi-info-circle-fill me-2'></i>Resumen</h5>");
        html.append("<p>Se completaron <strong>").append(filasAPredecir.size())
            .append("</strong> valores faltantes en la columna <code>")
            .append(columnas.get(colY)).append("</code>.</p>");
        html.append("</div>");

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("tipo", "simple");
        resultado.put("filasActualizadas", filas);
        return resultado;
    }

    private Map<String, Object> regresionMultiple(List<double[]> datos, List<List<String>> filas, int colY,
            List<Integer> filasAPredecir, List<String> columnas, StringBuilder html) {
        int n = datos.size();
        int m = columnas.size() - 1; // sin la dependiente

        // Paso 1: Preparación de matrices
        html.append("<div class='explanation-step mb-4'>");
        html.append("<h5 class='step-title'><span class='step-number'>1</span> Preparación de matrices</h5>");
        html.append("<div class='step-content'>");
        html.append("<p>Se construyeron las matrices para el cálculo:</p>");
        html.append("<ul>");
        html.append("<li><strong>Matriz X</strong>: Matriz de diseño con columna de unos</li>");
        html.append("<li><strong>Matriz Y</strong>: Vector de valores de la variable dependiente</li>");
        html.append("</ul></div></div>");

        double[][] X = new double[n][m + 1];
        double[][] Y = new double[n][1];

        for (int i = 0; i < n; i++) {
            X[i][0] = 1.0;
            int k = 1;
            for (int j = 0; j < columnas.size(); j++) {
                if (j != colY) {
                    X[i][k++] = datos.get(i)[j];
                } else {
                    Y[i][0] = datos.get(i)[j];
                }
            }
        }

        // Paso 2: Cálculos matriciales
        html.append("<div class='explanation-step mb-4'>");
        html.append("<h5 class='step-title'><span class='step-number'>2</span> Cálculos matriciales</h5>");
        html.append("<div class='step-content'>");
        html.append("<p>Se realizaron los siguientes cálculos:</p>");
        html.append("<ol>");
        html.append("<li>Transpuesta de X (X<sup>T</sup>)</li>");
        html.append("<li>Multiplicación X<sup>T</sup>X</li>");
        html.append("<li>Inversa de (X<sup>T</sup>X)</li>");
        html.append("<li>Multiplicación X<sup>T</sup>Y</li>");
        html.append("</ol>");
        html.append("<div class='math-formula'>");
        html.append("<h6>Fórmula general:</h6>");
        html.append("\\[ \\mathbf{B} = (\\mathbf{X}^T \\mathbf{X})^{-1} \\mathbf{X}^T \\mathbf{Y} \\]");
        html.append("</div></div></div>");

        double[][] XT = transpose(X);
        double[][] XTX = multiply(XT, X);
        double[][] invXTX = invert(XTX);
        double[][] XTY = multiply(XT, Y);
        double[][] B = multiply(invXTX, XTY);

        // Paso 3: Coeficientes obtenidos
        html.append("<div class='explanation-step mb-4'>");
        html.append("<h5 class='step-title'><span class='step-number'>3</span> Coeficientes obtenidos</h5>");
        html.append("<div class='step-content'>");
        html.append("<div class='table-responsive'>");
        html.append("<table class='table table-bordered'>");
        html.append("<thead><tr><th>Coeficiente</th><th>Valor</th></tr></thead>");
        html.append("<tbody>");

        StringBuilder ecuacion = new StringBuilder("\\[ Y = ");
        for (int i = 0; i < B.length; i++) {
            html.append("<tr><td>b<sub>").append(i).append("</sub></td>");
            html.append("<td>").append(String.format("%.4f", B[i][0])).append("</td></tr>");
            
            ecuacion.append(String.format("%.4f", B[i][0]));
            if (i > 0) {
                ecuacion.append(" \\cdot X_").append(i);
            }
            if (i < B.length - 1) {
                ecuacion.append(" + ");
            }
        }
        ecuacion.append(" \\]");
        html.append("</tbody></table></div>");
        html.append("<div class='alert alert-success mt-3'>");
        html.append("<h6><i class='bi bi-check-circle-fill me-2'></i>Ecuación final:</h6>");
        html.append(ecuacion.toString());
        html.append("</div></div></div>");

        // Paso 4: Predicciones
        html.append("<div class='explanation-step mb-4'>");
        html.append("<h5 class='step-title'><span class='step-number'>4</span> Predicciones realizadas</h5>");
        html.append("<div class='step-content'>");
        html.append("<p>Se aplicó el modelo para predecir los valores faltantes:</p>");
        html.append("<ul class='list-group'>");

        for (Integer i : filasAPredecir) {
            double pred = B[0][0]; // intercepto
            int k = 1;
            StringBuilder exp = new StringBuilder(String.format("%.4f", B[0][0]));
            
            for (int j = 0; j < columnas.size(); j++) {
                if (j != colY) {
                    double val = Double.parseDouble(filas.get(i).get(j));
                    pred += B[k][0] * val;
                    exp.append(" + ").append(String.format("%.4f", B[k][0])).append("×").append(String.format("%.4f", val));
                    k++;
                }
            }
            filas.get(i).set(colY, String.format("%.4f", pred));
            
            html.append("<li class='list-group-item'>");
            html.append("<div class='d-flex justify-content-between align-items-center'>");
            html.append("<span>Fila <span class='badge bg-primary'>").append(i + 1).append("</span></span>");
            html.append("<span class='badge bg-success'>Y = ").append(String.format("%.4f", pred)).append("</span>");
            html.append("</div>");
            html.append("<div class='text-muted small mt-1'>").append(exp.toString()).append("</div>");
            html.append("</li>");
        }
        html.append("</ul></div></div>");

        // Resumen final
        html.append("<div class='alert alert-info mt-4'>");
        html.append("<h5 class='alert-heading'><i class='bi bi-info-circle-fill me-2'></i>Resumen</h5>");
        html.append("<p>Se completaron <strong>").append(filasAPredecir.size())
            .append("</strong> valores faltantes en la columna <code>")
            .append(columnas.get(colY)).append("</code>.</p>");
        html.append("<p>Modelo con ").append(B.length - 1).append(" variables predictoras.</p>");
        html.append("</div>");

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("tipo", "multiple");
        resultado.put("filasActualizadas", filas);
        return resultado;
    }

    // Métodos auxiliares de álgebra matricial (sin cambios)
    private double[][] transpose(double[][] matrix) {
        int f = matrix.length;
        int c = matrix[0].length;
        double[][] transpuesta = new double[c][f];
        for (int i = 0; i < f; i++)
            for (int j = 0; j < c; j++)
                transpuesta[j][i] = matrix[i][j];
        return transpuesta;
    }

    private double[][] multiply(double[][] a, double[][] b) {
        int f1 = a.length, c1 = a[0].length, c2 = b[0].length;
        double[][] resultado = new double[f1][c2];
        for (int i = 0; i < f1; i++)
            for (int j = 0; j < c2; j++)
                for (int k = 0; k < c1; k++)
                    resultado[i][j] += a[i][k] * b[k][j];
        return resultado;
    }

    private double[][] invert(double[][] matriz) {
        int n = matriz.length;
        double[][] identidad = new double[n][n];
        for (int i = 0; i < n; i++)
            identidad[i][i] = 1;

        double[][] temp = new double[n][n * 2];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++) {
                temp[i][j] = matriz[i][j];
                temp[i][j + n] = identidad[i][j];
            }

        // Gauss-Jordan
        for (int i = 0; i < n; i++) {
            double diag = temp[i][i];
            for (int j = 0; j < 2 * n; j++)
                temp[i][j] /= diag;

            for (int k = 0; k < n; k++) {
                if (k == i) continue;
                double factor = temp[k][i];
                for (int j = 0; j < 2 * n; j++)
                    temp[k][j] -= factor * temp[i][j];
            }
        }

        double[][] inversa = new double[n][n];
        for (int i = 0; i < n; i++)
            System.arraycopy(temp[i], n, inversa[i], 0, n);

        return inversa;
    }
}