package it.aulab.progetto_finale_docente.services;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import it.aulab.progetto_finale_docente.models.Article;
import it.aulab.progetto_finale_docente.models.Image;
import it.aulab.progetto_finale_docente.repositories.ImageRepository;

@Service
public class ImageServiceImpl implements ImageService {

    @Autowired
    private ImageRepository imageRepository;

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

    @Value("${supabase.bucket}")
    private String supabaseBucket;

    @Value("${supabase.image}")
    private String supabaseImage;

    @Override
    public void saveImageOnDB(String url, Article article) {
        // La traccia chiede di sostituire la parola/path di 'bucket' con quella di 'image' nell'url prima di salvare
        String finalPath = url.replace(supabaseBucket, supabaseImage);

        Image image = Image.builder()
                .path(finalPath)
                .article(article)
                .build();

        imageRepository.save(image);
    }

    @Override
    @Async // Esecuzione asincrona richiesta dalla traccia
    public CompletableFuture<String> saveImageOnCloud(MultipartFile file) throws Exception {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Il file è vuoto");
        }

        // Recuperiamo l'estensione del file originale (es. .jpg, .png)
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        // Generiamo il nome univoco con l'UUID + l'estensione corretta
        String fileName = UUID.randomUUID().toString() + extension;

        // URL per fare il POST su Supabase Storage
        String uploadUrl = supabaseUrl + supabaseBucket + fileName;

        // Prepariamo gli Header HTTP richiesti da Supabase
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + supabaseKey);
        headers.set("apikey", supabaseKey);
        headers.setContentType(MediaType.parseMediaType(file.getContentType()));

        // Inseriamo i byte del file nel corpo della richiesta
        HttpEntity<byte[]> requestEntity = new HttpEntity<>(file.getBytes(), headers);

        RestTemplate restTemplate = new RestTemplate();
        
        // Eseguiamo la chiamata POST
        ResponseEntity<String> response = restTemplate.exchange(uploadUrl, HttpMethod.POST, requestEntity, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            // Se va a buon fine, restituiamo l'URL completo del file appena caricato
            return CompletableFuture.completedFuture(uploadUrl);
        } else {
            throw new RuntimeException("Errore durante l'upload su Supabase: " + response.getBody());
        }
    }

    @Override
    @Async
    @Transactional // Richiesto esplicitamente per la cancellazione sul DB
    public void deleteImage(String imagePath) throws IOException {
        
        // Ricaviamo il nome del file dall'URL completo
        String fileName = imagePath.substring(imagePath.lastIndexOf("/") + 1);
        
        // URL per fare la DELETE su Supabase Storage
        String deleteUrl = supabaseUrl + supabaseBucket + fileName;

        // Prepariamo gli Header HTTP
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + supabaseKey);
        headers.set("apikey", supabaseKey);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();

        try {
            // 1. Chiamata DELETE a Supabase Cloud
            restTemplate.exchange(deleteUrl, HttpMethod.DELETE, requestEntity, String.class);
            
            // 2. Chiamata di cancellazione sul DB locale tramite il repository
            imageRepository.deleteByPath(imagePath);
            
        } catch (Exception e) {
            throw new IOException("Errore durante l'eliminazione dell'immagine", e);
        }
    }
}