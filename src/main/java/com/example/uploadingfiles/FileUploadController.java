package com.example.uploadingfiles;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.uploadingfiles.storage.StorageFileNotFoundException;
import com.example.uploadingfiles.storage.StorageService;

@Controller
public class FileUploadController {

	private final StorageService storageService;
	private final List<String> fileNames = new ArrayList<>();

	@Autowired
	public FileUploadController(StorageService storageService) {
		this.storageService = storageService;
	}

	@GetMapping("/")
	public String listUploadedFiles(Model model) throws IOException {

		model.addAttribute("files", storageService.loadAll().map(
				path -> MvcUriComponentsBuilder.fromMethodName(FileUploadController.class,
						"serveFile", path.getFileName().toString()).build().toUri().toString())
				.collect(Collectors.toList()));
		model.addAttribute("fileNames", fileNames);

		return "uploadForm";
	}

	@GetMapping("/files/{filename:.+}")
	@ResponseBody
	public ResponseEntity<Resource> serveFile(@PathVariable String filename) {

		Resource file = storageService.loadAsResource(filename);

		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
				"attachment; filename=\"" + file.getFilename() + "\"").body(file);
	}

	@PostMapping("/")
	public String handleFileUpload(@RequestParam("file") MultipartFile file,
			RedirectAttributes redirectAttributes)  {

		storageService.store(file);
		/*redirectAttributes.addFlashAttribute("message",
				"You successfully uploaded " + file.getOriginalFilename() + "!");*/
		fileNames.add(file.getOriginalFilename());

		return "redirect:/"+file.getOriginalFilename() ;
	}

	@GetMapping("/{filename}")
	public String fileContent (@PathVariable String filename, Model model) throws FileNotFoundException {

		Scanner scanner = new Scanner(new FileReader(new File(String.valueOf(storageService.load(filename)))));
		scanner.useDelimiter("\r\n");
		ArrayList<String> fileStrings = new ArrayList<>();
		ArrayList<String> tableOfContent = new ArrayList<>();
		//HashMap<Long, String> tableOfContent = new HashMap<>();
		Long id = Long.valueOf(1);
		while (scanner.hasNext()) {
			String line = scanner.nextLine();

			if (line.startsWith("#")) {
				tableOfContent.add(id.toString()+line);
				fileStrings.add(id.toString()+line);
				id++;
			}
			else {
				fileStrings.add(line);
			}

		}

		model.addAttribute("fileStrings", fileStrings);
		model.addAttribute("fileName", filename);
		model.addAttribute("tableOfContent", tableOfContent);

		return "home";

	}

	@GetMapping("/error")
	public String errorPage () {

		return "error";
	}

	@ExceptionHandler(StorageFileNotFoundException.class)
	public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
		return ResponseEntity.notFound().build();
	}

}
