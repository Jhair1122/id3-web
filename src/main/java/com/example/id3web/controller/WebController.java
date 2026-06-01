package com.example.id3web.controller;

import com.example.id3web.service.ModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RestController
public class WebController {

    @Autowired
    private ModelService modelService;

    @GetMapping("/")
    public String index() { return "forward:/index.html"; }

    @GetMapping("/api/tree/dot")
    public ResponseEntity<String> getTreeDot() { return ResponseEntity.ok(modelService.getTreeDot()); }

    @GetMapping("/api/gains")
    public ResponseEntity<Map<String, Object>> getGains() { return ResponseEntity.ok(modelService.getGainInfo()); }

    @GetMapping("/api/dataset")
    public ResponseEntity<Map<String, Object>> getDataset() {
        var data = modelService.getData();
        List<String> attrs = IntStream.range(0, data.numAttributes()).mapToObj(i -> data.attribute(i).name()).collect(Collectors.toList());
        List<Map<String, String>> rows = new ArrayList<>();
        for (Instance inst : data) {
            Map<String, String> row = new HashMap<>();
            for (int i = 0; i < data.numAttributes(); i++) row.put(data.attribute(i).name(), inst.stringValue(data.attribute(i)));
            rows.add(row);
        }
        return ResponseEntity.ok(Map.of("attributes", attrs, "rows", rows, "classAttribute", data.classAttribute().name()));
    }

    @PostMapping("/api/predict")
    public ResponseEntity<Map<String, String>> predict(@RequestParam String Temperatura, @RequestParam String Humedad, @RequestParam String Viento) {
        try {
            String pred = modelService.predict(Temperatura, Humedad, Viento);
            return ResponseEntity.ok(Map.of("prediction", pred));
        } catch (Exception e) { return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); }
    }

    @GetMapping("/api/attributes")
    public ResponseEntity<Map<String, List<String>>> getAttributes() {
        Map<String, List<String>> attrMap = new HashMap<>();
        for (String attr : modelService.getAttributeNames()) attrMap.put(attr, modelService.getAttributeValues(attr));
        return ResponseEntity.ok(attrMap);
    }
}
