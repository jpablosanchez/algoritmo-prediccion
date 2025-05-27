package co.edu.unbosque.service;

import org.springframework.stereotype.Service;
import co.edu.unbosque.model.ArchivoModel;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class KMediasService {

	public Map<String, Object> imputarValoresConKMeans(ArchivoModel model) {
		List<List<String>> filas = model.getDatos();
		List<String> columnas = model.getColumnas();
		int columnaObjetivo = model.getColumnaObjetivo();
		Map<String, List<Double>> grupos = new LinkedHashMap<>();
		List<List<String>> filasCompletas = new ArrayList<>();

		// Paso 1: Identificar categorías y valores
		int categIndex = detectarColumnaCateg(filas, columnaObjetivo);

		// Construir explicación HTML
		StringBuilder html = new StringBuilder();
		html.append("<div class='algorithm-explanation'>");
		html.append("<div class='explanation-header mb-4'>");
		html.append("<h3 class='text-primary'><i class='bi bi-calculator-fill me-2'></i>Proceso de K-Medias</h3>");
		html.append(
				"<p class='text-muted'>Imputación de valores numéricos faltantes usando promedios por categoría</p>");
		html.append("</div>");

		// Paso 1: Agrupación de datos
		html.append("<div class='explanation-step mb-4'>");
		html.append("<h5 class='step-title'><span class='step-number'>1</span> Agrupación por categorías</h5>");
		html.append("<div class='step-content'>");
		html.append("<p>Se agruparon los datos por la columna categórica <code>").append(columnas.get(categIndex))
				.append("</code>:</p>");
		html.append("<div class='table-responsive'>");
		html.append("<table class='table table-bordered'>");
		html.append("<thead><tr><th>Categoría</th><th>Valores</th><th>Cantidad</th></tr></thead>");
		html.append("<tbody>");

		for (List<String> fila : filas) {
			String categoria = fila.get(categIndex);
			String valorStr = fila.get(columnaObjetivo);

			if (!valorStr.equals("?")) {
				double valor = Double.parseDouble(valorStr.replace(",", "."));
				grupos.computeIfAbsent(categoria, k -> new ArrayList<>()).add(valor);
			}
		}

		// Mostrar grupos en la tabla
		for (Map.Entry<String, List<Double>> entry : grupos.entrySet()) {
			html.append("<tr><td>").append(entry.getKey()).append("</td>");
			html.append("<td>").append(entry.getValue().toString()).append("</td>");
			html.append("<td>").append(entry.getValue().size()).append("</td></tr>");
		}
		html.append("</tbody></table></div></div></div>");

		// Paso 2: Cálculo de medias
		html.append("<div class='explanation-step mb-4'>");
		html.append("<h5 class='step-title'><span class='step-number'>2</span> Cálculo de medias</h5>");
		html.append("<div class='step-content'>");
		html.append("<p>Se calculó la media para cada categoría usando la fórmula:</p>");
		html.append("<div class='math-formula'>");
		html.append("\\[ \\bar{x}_k = \\frac{1}{n_k} \\sum_{i=1}^{n_k} x_i \\]");
		html.append("<p class='mt-2 mb-0 text-muted small'>Donde:</p>");
		html.append("<ul class='small'>");
		html.append("<li><strong>k</strong>: Categoría</li>");
		html.append("<li><strong>n<sub>k</sub></strong>: Cantidad de valores en la categoría k</li>");
		html.append("<li><strong>x<sub>i</sub></strong>: Valor numérico</li>");
		html.append("</ul></div>");
		html.append("<div class='table-responsive mt-3'>");
		html.append("<table class='table table-bordered'>");
		html.append("<thead><tr><th>Categoría</th><th>Media</th><th>Cálculo</th></tr></thead>");
		html.append("<tbody>");

		Map<String, Double> medias = new HashMap<>();
		for (Map.Entry<String, List<Double>> entry : grupos.entrySet()) {
			String cat = entry.getKey();
			List<Double> valores = entry.getValue();
			double media = valores.stream().mapToDouble(d -> d).average().orElse(0.0);
			medias.put(cat, media);

			html.append("<tr><td>").append(cat).append("</td>");
			html.append("<td>").append(String.format(Locale.US, "%.2f", media)).append("</td>");
			html.append("<td>\\[ \\frac{")
					.append(valores.stream().map(v -> String.format(Locale.US, "%.2f", v))
							.collect(Collectors.joining(" + ")))
					.append("}{").append(valores.size()).append("} \\]</td></tr>");
		}
		html.append("</tbody></table></div></div></div>");

		// Paso 3: Imputación de valores
		html.append("<div class='explanation-step mb-4'>");
		html.append("<h5 class='step-title'><span class='step-number'>3</span> Imputación de valores</h5>");
		html.append("<div class='step-content'>");
		html.append("<p>Se reemplazaron los valores faltantes (<code>?</code>) con la media de su categoría:</p>");
		html.append("<ul class='list-group'>");

		int contadorImputaciones = 0;
		for (List<String> fila : filas) {
			String valor = fila.get(columnaObjetivo);
			String categoria = fila.get(categIndex);

			if (valor.equals("?")) {
				Double media = medias.get(categoria);
				String valorImputado = String.format(Locale.US, "%.2f", media);
				List<String> filaNueva = new ArrayList<>(fila);
				filaNueva.set(columnaObjetivo, valorImputado);
				filasCompletas.add(filaNueva);

				html.append("<li class='list-group-item d-flex justify-content-between align-items-center'>");
				html.append("<span>Categoría <span class='badge bg-primary'>").append(categoria).append("</span>");
				html.append(" : Fila ").append(filasCompletas.size()).append("</span>");
				html.append("<span class='badge bg-success'>").append(valorImputado).append("</span>");
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
				.append("</strong> valores faltantes en la columna <code>").append(columnas.get(columnaObjetivo))
				.append("</code>.</p>");
		html.append("<p class='mb-0'>Usando como categoría la columna <code>").append(columnas.get(categIndex))
				.append("</code>.</p>");
		html.append("</div>");

		html.append("</div>"); // Cierre del contenedor principal

		Map<String, Object> resultado = new HashMap<>();
		resultado.put("filasActualizadas", filasCompletas);
		resultado.put("explicacionHtml", html.toString());
		return resultado;
	}

	// Método auxiliar para detectar columna categórica (sin cambios)
	private int detectarColumnaCateg(List<List<String>> filas, int columnaObjetivo) {
		int numCols = filas.get(0).size();
		for (int col = 0; col < numCols; col++) {
			if (col == columnaObjetivo)
				continue;
			boolean esCateg = true;
			for (List<String> fila : filas) {
				String valor = fila.get(col);
				if (!valor.equals("?") && valor.matches("[-+]?\\d+(,\\d+)?")) {
					esCateg = false;
					break;
				}
			}
			if (esCateg)
				return col;
		}
		return 0;
	}
}