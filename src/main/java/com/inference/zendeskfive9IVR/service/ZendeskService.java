package com.inference.zendeskfive9IVR.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ZendeskService {

    private static final Logger logger = LogManager.getLogger(ZendeskService.class);

    public ResponseEntity<String> listZendesk(String numeDocument) {
        RestTemplate restTemplate = new RestTemplate();
        String customerAPIUrl = "https://api.getbase.com/v2/leads?custom_fields[Nro Documento]=" + numeDocument;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + "18e45ee292b3becaadfe1a0c75ab5d510b724bffaf722e53bcf16a33ef004d4d");
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        logger.info("Traze message in API extern list Zendesk  [] "+ restTemplate.exchange(customerAPIUrl, HttpMethod.GET, entity, String.class));
        return restTemplate.exchange(customerAPIUrl, HttpMethod.GET, entity, String.class);
    }

    public void createZendesk(String id, String number, String channel) {
        String url = "https://api.getbase.com/v2/leads";
        String tipoDocumento;
        switch (id.length()) {
            case 8:
                tipoDocumento = "DNI";
                break;
            case 11:
                tipoDocumento = "RUC";
                break;
            case 9:
                tipoDocumento = "CARNET/PASAPORTE";
                break;
            default:
                System.err.println("Error: Longitud de documento no válida.");
                return;
        }
        String requestBody = "{ " +
                "\"data\": {" +
                "\"first_name\": \"Lead IVR\"," +
                "\"last_name\": \"Lead IVR\"," +
                "\"description\": \"Lead Ingresado por IVR\"," +
                (number.length() == 7 ?
                        "\"phone\": \"" + number + "\"," :
                        "\"mobile\": \"" + number + "\","
                ) +
                "\"status\": \"New\"," +
                "\"tags\": [\"ivr lead\"]," +
                "\"custom_fields\": {" +
                "\"Nro Documento\": \"" + id + "\"," +
                    "\"Tipo Documento\": \"" + tipoDocumento + "\"" +
                "}" +
                "}" +
                "}";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + "18e45ee292b3becaadfe1a0c75ab5d510b724bffaf722e53bcf16a33ef004d4d");
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);
        logger.info("Traze message in API extern create Zendesk lead  [] "+ restTemplate.postForEntity(url, requestEntity, String.class));
        restTemplate.postForEntity(url, requestEntity, String.class);
    }
}