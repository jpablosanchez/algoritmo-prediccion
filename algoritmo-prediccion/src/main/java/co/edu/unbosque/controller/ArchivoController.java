package co.edu.unbosque.controller;

import co.edu.unbosque.model.ArchivoModel;
import co.edu.unbosque.service.ArbolDecisionService;
import co.edu.unbosque.service.KMediasService;
import co.edu.unbosque.service.KModasService;
import co.edu.unbosque.service.RegresionService;
import co.edu.unbosque.utils.CsvUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.*;

@Controller
public class ArchivoController {

	private ArchivoModel objModel;

	@Autowired
	private ArbolDecisionService arbolDecisionService;
	@Autowired
	private RegresionService regresionService;
	@Autowired
	private KMediasService kMediasService;
	@Autowired
	private KModasService kModasService;

	@GetMapping("/")
	public String home() {
		return "index";
	}

	@GetMapping("/subir")
	public String mostrarFormulario() {
		return "subir";
	}

	@PostMapping("/analizar")
	public String procesarArchivo(@RequestParam("archivoCsv") MultipartFile archivo,
			@RequestParam("algoritmo") String algoritmo, Model model) {
		
	    if (!archivo.getOriginalFilename().toLowerCase().endsWith(".csv")) {
	        model.addAttribute("error", "Solo se permiten archivos CSV");
	        return "subir";
	    }

		try {
			List<List<String>> datos = CsvUtil.leerCSV(archivo);
			if (datos.isEmpty()) {
				model.addAttribute("error", "El archivo está vacío.");
				return "subir";
			}

			List<String> columnas = datos.get(0);
			List<List<String>> filas = datos.subList(1, datos.size());

			model.addAttribute("columnas", columnas);
			model.addAttribute("filas", filas);
			model.addAttribute("algoritmoSeleccionado", algoritmo);

			int columnaObjetivo = detectarColumnaObjetivo(filas, columnas.size());
			if (columnaObjetivo == -1) {
				model.addAttribute("error", "No se encontró ninguna columna con '?' para predecir.");
				return "subir";
			}

			switch (algoritmo) {
			case "arbol":
				procesarArbol(filas, columnas, columnaObjetivo, model);
				break;
			case "regresion":
				procesarRegresion(filas, columnas, columnaObjetivo, model);
				break;
			case "kmedias":
				procesarKMedias(filas, columnas, columnaObjetivo, model);
				break;
			case "kmodas":
				procesarKModas(filas, columnas, columnaObjetivo, model);
				break;
			default:
				model.addAttribute("error", "Algoritmo no reconocido.");
				return "subir";
			}

			return "subir";

		} catch (Exception e) {
			model.addAttribute("error", "Error al procesar el archivo: " + e.getMessage());
			return "subir";
		}
	}

	@GetMapping("/descargar")
	@ResponseBody
	public ResponseEntity<Resource> descargarCsv() throws IOException {
		Path archivo = Paths.get("archivo_completado.csv");
		if (!Files.exists(archivo)) {
			return ResponseEntity.notFound().build();
		}

		Resource recurso = new UrlResource(archivo.toUri());

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=archivo_completado.csv")
				.contentType(MediaType.parseMediaType("text/csv")).body(recurso);
	}

	private int detectarColumnaObjetivo(List<List<String>> filas, int numColumnas) {
		for (int j = 0; j < numColumnas; j++) {
			for (List<String> fila : filas) {
				if (j < fila.size() && fila.get(j).equals("?")) {
					return j;
				}
			}
		}
		return -1;
	}

	private void procesarArbol(List<List<String>> filas, List<String> columnas, int colObjetivo, Model model)
			throws Exception {
		Set<Integer> filasConFaltantes = identificarFilasConFaltantes(filas, colObjetivo);
		objModel = new ArchivoModel(filas, columnas, colObjetivo);
		List<String> predicciones = arbolDecisionService.entrenarYPredecir(objModel);
		List<List<String>> filasMarcadas = marcarValoresImputados(filas, colObjetivo, filasConFaltantes);
		model.addAttribute("explicacionArbol", arbolDecisionService.getExplicacionHtml());
		model.addAttribute("filas", filasMarcadas);
		CsvUtil.guardarCSV("archivo_completado.csv", columnas, filas);
	}

	private void procesarRegresion(List<List<String>> filas, List<String> columnas, int colObjetivo, Model model)
	        throws IOException {
	    Set<Integer> filasConFaltantes = identificarFilasConFaltantes(filas, colObjetivo);
	    objModel = new ArchivoModel(filas, columnas, colObjetivo);
	    Map<String, Object> resultado = regresionService.realizarRegresion(objModel);
	    String explicacionHtml = (String) resultado.get("explicacionHtml");
	    List<List<String>> filasActualizadas = (List<List<String>>) resultado.get("filasActualizadas");
	    List<List<String>> filasMarcadas = marcarValoresImputados(filasActualizadas, colObjetivo, filasConFaltantes);
	    model.addAttribute("resultadoRegresion", true);
	    model.addAttribute("explicacionRegresion", explicacionHtml);
	    model.addAttribute("filas", filasMarcadas);
	    CsvUtil.guardarCSV("archivo_completado.csv", columnas, filasActualizadas);
	}

	private void procesarKMedias(List<List<String>> filas, List<String> columnas, int colObjetivo, Model model) 
	        throws IOException {
	    Set<Integer> filasConFaltantes = identificarFilasConFaltantes(filas, colObjetivo);
	    objModel = new ArchivoModel(filas, columnas, colObjetivo);
	    Map<String, Object> resultado = kMediasService.imputarValoresConKMeans(objModel);
	    
	    List<List<String>> filasActualizadas = (List<List<String>>) resultado.get("filasActualizadas");
	    String explicacionHtml = (String) resultado.get("explicacionHtml");
	    List<List<String>> filasMarcadas = marcarValoresImputados(filasActualizadas, colObjetivo, filasConFaltantes);
	    model.addAttribute("resultadoKmedias", true);
	    model.addAttribute("explicacionKmedias", explicacionHtml);
	    model.addAttribute("filas", filasMarcadas);
	    model.addAttribute("columnas", columnas);

	    CsvUtil.guardarCSV("archivo_completado.csv", columnas, filasActualizadas);
	}

	private void procesarKModas(List<List<String>> filas, List<String> columnas, int colObjetivo, Model model) 
	        throws IOException {
	    Set<Integer> filasConFaltantes = identificarFilasConFaltantes(filas, colObjetivo);
	    objModel = new ArchivoModel(filas, columnas, colObjetivo);
	    Map<String, Object> resultado = kModasService.imputarValoresConKModas(objModel);
	    List<List<String>> filasActualizadas = (List<List<String>>) resultado.get("filasActualizadas");
	    String explicacionHtml = (String) resultado.get("explicacionHtml");
	    List<List<String>> filasMarcadas = marcarValoresImputados(filasActualizadas, colObjetivo, filasConFaltantes);
	    model.addAttribute("resultadoKmodas", true);
	    model.addAttribute("explicacionKmodas", explicacionHtml);
	    model.addAttribute("filas", filasMarcadas);
	    model.addAttribute("columnas", columnas);

	    CsvUtil.guardarCSV("archivo_completado.csv", columnas, filasActualizadas);
	}

	private Set<Integer> identificarFilasConFaltantes(List<List<String>> filas, int colObjetivo) {
		Set<Integer> indices = new HashSet<>();
		for (int i = 0; i < filas.size(); i++) {
			if ("?".equals(filas.get(i).get(colObjetivo))) {
				indices.add(i);
			}
		}
		return indices;
	}

	private List<List<String>> marcarValoresImputados(List<List<String>> filas, int colObjetivo,
			Set<Integer> imputadas) {
		List<List<String>> marcadas = new ArrayList<>();
		for (int i = 0; i < filas.size(); i++) {
			boolean imputado = imputadas.contains(i);
			List<String> fila = filas.get(i);
			List<String> nuevaFila = new ArrayList<>();
			for (int j = 0; j < fila.size(); j++) {
				String valor = fila.get(j);
				nuevaFila.add((j == colObjetivo && imputado && !"?".equals(valor))
						? "<span class='valor-imputado'>" + valor + "</span>"
						: valor);
			}
			marcadas.add(nuevaFila);
		}
		return marcadas;
	}

}
