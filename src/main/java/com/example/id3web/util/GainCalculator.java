// src/main/java/com/example/id3web/util/GainCalculator.java
package com.example.id3web.util;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import java.util.*;

public class GainCalculator {
    private final Instances data;
    private final double totalEntropy;
    private final Map<String, double[]> classDistribution;

    public GainCalculator(Instances data) {
        this.data = data;
        this.classDistribution = computeClassDistribution();
        this.totalEntropy = computeEntropy(getClassCounts());
    }

    private Map<String, double[]> computeClassDistribution() {
        Map<String, Integer> classCounts = new HashMap<>();
        for (int i = 0; i < data.numClasses(); i++) {
            classCounts.put(data.classAttribute().value(i), 0);
        }
        for (Instance inst : data) {
            String className = inst.stringValue(data.classAttribute());
            classCounts.put(className, classCounts.get(className) + 1);
        }
        Map<String, double[]> dist = new HashMap<>();
        for (Map.Entry<String, Integer> entry : classCounts.entrySet()) {
            dist.put(entry.getKey(), new double[]{entry.getValue(), entry.getValue() / (double) data.numInstances()});
        }
        return dist;
    }

    private Map<String, Integer> getClassCounts() {
        Map<String, Integer> counts = new HashMap<>();
        for (Instance inst : data) {
            String className = inst.stringValue(data.classAttribute());
            counts.put(className, counts.getOrDefault(className, 0) + 1);
        }
        return counts;
    }

    private double computeEntropy(Map<String, Integer> counts) {
        int total = counts.values().stream().mapToInt(Integer::intValue).sum();
        double entropy = 0.0;
        for (int count : counts.values()) {
            if (count == 0) continue;
            double p = count / (double) total;
            entropy -= p * (Math.log(p) / Math.log(2));
        }
        return entropy;
    }

    public double getTotalEntropy() {
        return totalEntropy;
    }

    public List<Map<String, Object>> calculateGains() {
        List<Map<String, Object>> gainList = new ArrayList<>();
        for (int i = 0; i < data.numAttributes() - 1; i++) {
            Attribute attr = data.attribute(i);
            Map<String, Integer> classCountsPerValue = new HashMap<>();
            Map<String, Map<String, Integer>> valueToClassCounts = new HashMap<>();

            for (Instance inst : data) {
                String value = inst.stringValue(attr);
                String className = inst.stringValue(data.classAttribute());
                valueToClassCounts.computeIfAbsent(value, k -> new HashMap<>());
                valueToClassCounts.get(value).put(className,
                        valueToClassCounts.get(value).getOrDefault(className, 0) + 1);
            }

            double conditionalEntropy = 0.0;
            for (String value : valueToClassCounts.keySet()) {
                Map<String, Integer> countsForValue = valueToClassCounts.get(value);
                int totalForValue = countsForValue.values().stream().mapToInt(Integer::intValue).sum();
                double entropyForValue = computeEntropy(countsForValue);
                conditionalEntropy += (totalForValue / (double) data.numInstances()) * entropyForValue;
            }
            double gain = totalEntropy - conditionalEntropy;

            Map<String, Object> attrInfo = new HashMap<>();
            attrInfo.put("attribute", attr.name());
            attrInfo.put("gain", round(gain, 5));
            attrInfo.put("conditionalEntropy", round(conditionalEntropy, 5));
            attrInfo.put("values", valueToClassCounts.keySet());
            gainList.add(attrInfo);
        }
        gainList.sort((a, b) -> Double.compare((double) b.get("gain"), (double) a.get("gain")));
        return gainList;
    }

    public Map<String, Object> getClassDistribution() {
        Map<String, Object> result = new HashMap<>();
        Map<String, Integer> counts = getClassCounts();
        result.put("counts", counts);
        result.put("total", data.numInstances());
        return result;
    }

    private double round(double value, int places) {
        double scale = Math.pow(10, places);
        return Math.round(value * scale) / scale;
    }
}