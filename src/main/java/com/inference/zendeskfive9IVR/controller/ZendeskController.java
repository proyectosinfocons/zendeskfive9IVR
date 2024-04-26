package com.inference.zendeskfive9IVR.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.inference.zendeskfive9IVR.model.MessageSucces;
import com.inference.zendeskfive9IVR.model.RequestNubyx;
import com.inference.zendeskfive9IVR.service.ZendeskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.text.ParseException;
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
        ResponseEntity<MessageSucces> response=zendeskService.createZendesklis(requestNubyx);
        return response;
    }
}