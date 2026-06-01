// src/main/java/com/example/id3web/controller/WebController.java
package com.example.id3web.controller;

import com.example.id3web.service.ModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import weka.core.Instance;
import weka.core.Instances;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RestController
public class WebController {

    @Autowired
    private ModelService modelService;

    @GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }

    @GetMapping("/api/tree/dot")
    public ResponseEntity<String> getTreeDot() {
        return ResponseEntity.ok(modelService.getTreeDot());
    }

    @GetMapping("/api/gains")
    public ResponseEntity<Map<String, Object>> getGains() {
        return ResponseEntity.ok(modelService.getGainInfo());
    }

    @GetMapping("/api/dataset")
    public ResponseEntity<Map<String, Object>> getDataset() {
        Instances data = modelService.getData();
        List<String> attributeNames = IntStream.range(0, data.numAttributes())
                .mapToObj(i -> data.attribute(i).name())
                .collect(Collectors.toList());

        List<Map<String, String>> rows = data.stream()
                .map(inst -> {
                    Map<String, String> row = new HashMap<>();
                    for (int i = 0; i < data.numAttributes(); i++) {
                        row.put(data.attribute(i).name(), inst.stringValue(data.attribute(i)));
                    }
                    return row;
                })
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("attributes", attributeNames);
        response.put("rows", rows);
        response.put("classAttribute", data.classAttribute().name());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/predict")
    public ResponseEntity<Map<String, String>> predict(
            @RequestParam String Temperatura,
            @RequestParam String Humedad,
            @RequestParam String Viento) {
        try {
            String prediction = modelService.predict(Temperatura, Humedad, Viento);
            Map<String, String> response = new HashMap<>();
            response.put("prediction", prediction);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/api/attributes")
    public ResponseEntity<Map<String, List<String>>> getAttributes() {
        Map<String, List<String>> attrMap = new HashMap<>();
        for (String attrName : modelService.getAttributeNames()) {
            attrMap.put(attrName, modelService.getAttributeValues(attrName));
        }
        return ResponseEntity.ok(attrMap);
    }
}