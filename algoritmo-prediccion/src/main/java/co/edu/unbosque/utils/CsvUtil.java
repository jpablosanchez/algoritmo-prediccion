package co.edu.unbosque.utils;

import org.apache.commons.csv.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

public class CsvUtil {

    public static List<List<String>> leerCSV(MultipartFile file) throws IOException {
        List<List<String>> filas = new ArrayList<>();

        try (Reader reader = new InputStreamReader(file.getInputStream());
             CSVParser csvParser = CSVFormat.DEFAULT.parse(reader)) {

            for (CSVRecord record : csvParser) {
                List<String> fila = new ArrayList<>();
                record.forEach(fila::add);
                filas.add(fila);
            }
        }

        return filas;
    }
    
    public static void guardarCSV(String ruta, List<String> columnas, List<List<String>> filas) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ruta))) {
            writer.write(String.join(",", columnas));
            writer.newLine();
            for (List<String> fila : filas) {
                writer.write(String.join(",", fila));
                writer.newLine();
            }
        }
    }

}
