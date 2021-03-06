package LabJackData;

import javafx.application.Platform;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class CSVWriter {

	List<String[]> dataLines;
	private String fileName;
	private Controller controller;

	CSVWriter(Controller controller) throws Exception {
		this.controller=controller;
		fileName = new SimpleDateFormat("'data/'yyyyMMddHHmmss'.csv'").format(new Date());
		dataLines = new ArrayList<>();
		dataLines.add(new String[]
				{"Time", "Load", "Pressure 1", "Pressure 2"});
		givenDataArray_whenConvertToCSV_thenOutputCreated();
		updateLog("Writing to file: \""+fileName+"\"");
	}

	void writeLine() throws IOException {
		givenDataArray_whenConvertToCSV_thenOutputCreated();
	}

	private void givenDataArray_whenConvertToCSV_thenOutputCreated() throws IOException {
		File csvOutputFile = new File(fileName);
		try (PrintWriter pw = new PrintWriter(new FileWriter(csvOutputFile, true))) {
			dataLines.stream()
					.map(this::convertToCSV)
					.forEach(pw::println);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String convertToCSV(String[] data) {
		return Stream.of(data)
				.map(this::escapeSpecialCharacters)
				.collect(Collectors.joining(","));
	}

	private String escapeSpecialCharacters(String data) {
		String escapedData = data.replaceAll("\\R", " ");
		if (data.contains(",") || data.contains("\"") || data.contains("'")) {
			data = data.replace("\"", "\"\"");
			escapedData = "\"" + data + "\"";
		}
		return escapedData;
	}

	private void updateLog(String message) {
		Platform.runLater(() -> controller.updateLog(message));
	}
}
