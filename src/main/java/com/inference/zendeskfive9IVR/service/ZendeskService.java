package com.inference.zendeskfive9IVR.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inference.zendeskfive9IVR.model.MessageSucces;
import com.inference.zendeskfive9IVR.model.RequestNubyx;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class ZendeskService {

    private static final Logger logger = LogManager.getLogger(ZendeskService.class);

    private static Date lastActionDate = null;
    private boolean validationExecuted = false;
    private Date lastValidationDate = null;

    private Date lastRegistrationDate = null;

    @Value("${token.access.get}")
    private String token;

    public ResponseEntity<String> listZendesk(String numeDocument) {
        RestTemplate restTemplate = new RestTemplate();
        String customerAPIUrl = "https://api.getbase.com/v2/leads?custom_fields[Nro Documento]=" + numeDocument;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(customerAPIUrl, HttpMethod.GET, entity, String.class);
    }

    public ResponseEntity<String> createZendesk(String id, String number, String channel) {
        String url = "https://api.getbase.com/v2/leads";
        String tipoDocumento = "";
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
                logger.info("Traze message service in API extern get Zendesk id error in default [] " + id);
                System.err.println("Error: Longitud de documento no válida.");

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
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> responseprueba=restTemplate.postForEntity(url, requestEntity, String.class);
        return responseprueba;
    }


    public ResponseEntity<MessageSucces> createZendesklis(RequestNubyx requestNubyx) throws JsonProcessingException, ParseException {
        ResponseEntity<String> prueba = listZendesk(requestNubyx.getId());
        String responseBody = prueba.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        JsonNode itemsNode = jsonNode.get("items");
        List<String> createdAtValues = new ArrayList<>();
        DateTimeFormatter originalFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        DateTimeFormatter targetFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        logger.info("Info message init[]  ");
        if (!itemsNode.isEmpty()) {
            logger.info("Info message success register find id [] " + itemsNode);
            for (JsonNode item : itemsNode) {
                JsonNode dataNode = item.get("data");
                String createdAt = dataNode.get("created_at").asText();
                logger.info("Info message success register find id object [] " + dataNode);
                LocalDateTime createdAtDateTime = LocalDateTime.parse(createdAt, originalFormatter);
                LocalDateTime adjustedCreatedAtDateTime = createdAtDateTime.minusHours(5); // Restar 5 horas
                String formattedCreatedAt = adjustedCreatedAtDateTime.format(targetFormatter);
              createdAtValues.add(formattedCreatedAt);
            }
            if (requestNubyx.getId().length() >= 8 && requestNubyx.getId().length() <= 11) {
                if (!requestNubyx.getId().isEmpty()) {
                    for (String date : createdAtValues) {
                        SimpleDateFormat originalDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        Date formattedDate = originalDateFormat.parse(date);
                        Date currentDate = new Date();
                        String formattedCurrentDateStr = originalDateFormat.format(currentDate);
                        Date formattedCurrentDate = originalDateFormat.parse(formattedCurrentDateStr);

                        if (formattedCurrentDate.equals(formattedDate)) {
                            logger.info("Error message in same day [] " + date);
                            return ResponseEntity.badRequest().body(new MessageSucces("unregistered", null));
                        }

                        if (formattedCurrentDate.after(formattedDate)  && !validationExecuted) {
                            // Si ya se ha registrado un lead en el día actual, devolver un mensaje de error
                            logger.info("Info message success register after day [] " + date);
                            ResponseEntity<String> zendesksecond = createZendesk(requestNubyx.getId(), requestNubyx.getNumber(), requestNubyx.getChannel());
                            String responseBodysecond = zendesksecond.getBody();
                            ObjectMapper objectMappersecond = new ObjectMapper();
                            JsonNode jsonNodesecond = objectMappersecond.readTree(responseBodysecond);
                            JsonNode dataNodesecond = jsonNodesecond.get("data");
                            String idcreatesecond = dataNodesecond.get("id").asText();
                            validationExecuted = true;
                            return ResponseEntity.ok(new MessageSucces("registered", idcreatesecond));
                        }
                    }
                }
            } else {
                logger.info("Error message in length id find [] " + itemsNode);
                return ResponseEntity.badRequest().body(new MessageSucces("longitud de id no valido", null));
            }
        } else {
            if (requestNubyx.getId().length() >= 8 && requestNubyx.getId().length() <= 11) {
                ResponseEntity<String> zendesk=  createZendesk(requestNubyx.getId(), requestNubyx.getNumber(), requestNubyx.getChannel());
                String responseBodyfirst = zendesk.getBody();
                ObjectMapper objectMapperfirst = new ObjectMapper();
                JsonNode jsonNodefirst = objectMapperfirst.readTree(responseBodyfirst);
                JsonNode dataNodefirst = jsonNodefirst.get("data");
                String idcreatefirst = dataNodefirst.get("id").asText();
                return ResponseEntity.ok(new MessageSucces("registered", idcreatefirst));
            } else {
                logger.info("Error message in length id first [] " + itemsNode);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageSucces("longitud de id no valido", null));
            }
        }
        logger.info("Error message in server []");
        return null;
    }
}