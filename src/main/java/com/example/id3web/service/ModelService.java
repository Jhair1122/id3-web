package com.example.id3web.service;

import com.example.id3web.util.GainCalculator;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import weka.classifiers.trees.Id3;
import weka.core.*;
import weka.core.converters.ArffLoader;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;

@Service
public class ModelService {
    private Instances data;
    private Id3 classifier;
    private GainCalculator gainCalculator;

    @PostConstruct
    public void init() throws Exception {
        // Load ARFF from resources
        ClassPathResource resource = new ClassPathResource("data/lluvioso.arff");
        ArffLoader loader = new ArffLoader();
        loader.setSource(resource.getFile());
        data = loader.getDataSet();
        data.setClassIndex(data.numAttributes() - 1);

        // Train ID3 classifier
        classifier = new Id3();
        classifier.buildClassifier(data);

        // Gain calculator for display
        gainCalculator = new GainCalculator(data);
    }

    public Instances getData() {
        return data;
    }

    public Id3 getClassifier() {
        return classifier;
    }

    public String getTreeDot() {
        return classifier.graph();
    }

    public Map<String, Object> getGainInfo() {
        Map<String, Object> result = new HashMap<>();
        result.put("totalEntropy", gainCalculator.getTotalEntropy());
        result.put("gains", gainCalculator.calculateGains());
        result.put("classDistribution", gainCalculator.getClassDistribution());
        return result;
    }

    public String predict(String temperatura, String humedad, String viento) throws Exception {
        // Create new instance with same structure
        Instance newInst = new DenseInstance(data.numAttributes());
        newInst.setDataset(data);
        newInst.setValue(data.attribute("Temperatura"), temperatura);
        newInst.setValue(data.attribute("Humedad"), humedad);
        newInst.setValue(data.attribute("Viento"), viento);
        double classIdx = classifier.classifyInstance(newInst);
        return data.classAttribute().value((int) classIdx);
    }

    public List<String> getAttributeValues(String attributeName) {
        Attribute attr = data.attribute(attributeName);
        if (attr != null && attr.isNominal()) {
            List<String> values = new ArrayList<>();
            Enumeration<?> enu = attr.enumerateValues();
            while (enu.hasMoreElements()) {
                values.add((String) enu.nextElement());
            }
            return values;
        }
        return new ArrayList<>();
    }

    public List<String> getAttributeNames() {
        List<String> names = new ArrayList<>();
        for (int i = 0; i < data.numAttributes() - 1; i++) {
            names.add(data.attribute(i).name());
        }
        return names;
    }
}
