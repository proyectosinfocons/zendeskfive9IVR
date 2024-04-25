package com.inference.zendeskfive9IVR.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inference.zendeskfive9IVR.model.MessageSucces;
import com.inference.zendeskfive9IVR.model.RequestNubyx;
import com.inference.zendeskfive9IVR.service.ZendeskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@RestController
@RequestMapping("/zendeskfive9IVR")
public class ZendeskController {

    @Autowired
    private ZendeskService zendeskService;

    private static final Logger logger = LogManager.getLogger(ZendeskController.class);

    @PostMapping("/lead")
    public ResponseEntity<?> principal(
            @RequestBody RequestNubyx requestNubyx
    ) throws JsonProcessingException, ParseException {
        ResponseEntity<String> prueba = zendeskService.listZendesk(requestNubyx.getId());
        String responseBody = prueba.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        JsonNode itemsNode = jsonNode.get("items");
        List<String> createdAtValues = new ArrayList<>();
        DateTimeFormatter originalFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        DateTimeFormatter targetFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        logger.info("Info message []  " + itemsNode);
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
                        if (formattedCurrentDate.after(formattedDate)) {
                            logger.info("Info message success register after day [] " + date);
                            zendeskService.createZendesk(requestNubyx.getId(), requestNubyx.getNumber(), requestNubyx.getChannel());
                            return ResponseEntity.ok(new MessageSucces("registered", requestNubyx.getId()));
                        }
                    }
                }
            } else {
                logger.info("Error message in length id find [] " + itemsNode);
                return ResponseEntity.badRequest().body(new MessageSucces("longitud de id no valido", null));
            }
        } else {
            if (requestNubyx.getId().length() >= 8 && requestNubyx.getId().length() <= 11) {
                logger.info("Info message success register first [] " + requestNubyx.getId());
                zendeskService.createZendesk(requestNubyx.getId(), requestNubyx.getNumber(), requestNubyx.getChannel());
                return ResponseEntity.ok(new MessageSucces("registered", requestNubyx.getId()));
            } else {
                logger.info("Error message in length id first [] " + itemsNode);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageSucces("longitud de id no valido", null));
            }
        }
        logger.info("Error message in server []");
        return null;
    }
}