package com.ta.service.student;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Rule-based skill matching: strict skills need exact matches; flexible skills
 * accept related tools with partial credit (explainable, no LLM).
 */
public class SkillMatchScorer {

    private static final double EXACT_CREDIT = 1.0;
    private static final double PARTIAL_THRESHOLD = 0.999;

    private static final Set<String> STRICT_SKILLS = Set.of(
            "Java", "Python", "C", "C++", "JavaScript", "HTML", "CSS", "SQL",
            "Spring", "React", "Node.js", "R", "MATLAB", "TensorFlow", "PyTorch",
            "Docker", "Kubernetes", "Git", "Linux", "Unix", "AWS", "Azure", "Google Cloud",
            "REST API", "API Development"
    );

    private static final Map<String, Map<String, Double>> RELATED_BY_REQUIRED = createRelatedMap();

    public SkillMatchOutcome score(List<String> requiredSkills, List<String> studentSkills) {
        List<String> required = requiredSkills != null ? requiredSkills : List.of();
        List<String> student = studentSkills != null ? studentSkills : List.of();
        Set<String> studentSet = new HashSet<>(student);

        List<String> matched = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        List<SkillRelationHint> related = new ArrayList<>();

        if (required.isEmpty()) {
            return new SkillMatchOutcome(0.0, matched, missing, related);
        }

        double totalCredit = 0.0;
        for (String req : required) {
            double credit = creditForRequirement(req, studentSet, student, related);
            totalCredit += credit;
            if (credit >= PARTIAL_THRESHOLD) {
                matched.add(req);
            } else if (credit <= 0.0) {
                missing.add(req);
            } else {
                // Partial only — not in matched or missing lists as full items
            }
        }

        double matchScore = totalCredit / required.size();
        return new SkillMatchOutcome(matchScore, matched, missing, related);
    }

    private double creditForRequirement(String required,
                                        Set<String> studentSet,
                                        List<String> studentList,
                                        List<SkillRelationHint> relatedOut) {
        if (studentSet.contains(required)) {
            return EXACT_CREDIT;
        }

        if (isStrict(required)) {
            return 0.0;
        }

        double best = 0.0;
        String bestStudentSkill = null;
        Map<String, Double> relatedWeights = RELATED_BY_REQUIRED.get(required);
        if (relatedWeights != null) {
            for (String st : studentList) {
                Double w = relatedWeights.get(st);
                if (w != null && w > best) {
                    best = w;
                    bestStudentSkill = st;
                }
            }
        }

        if (best > 0.0 && bestStudentSkill != null) {
            relatedOut.add(new SkillRelationHint(bestStudentSkill, required, best));
        }
        return best;
    }

    private static boolean isStrict(String skill) {
        return STRICT_SKILLS.contains(skill);
    }

    private static Map<String, Map<String, Double>> createRelatedMap() {
        Map<String, Map<String, Double>> map = new LinkedHashMap<>();

        putRelated(map, "Statistics",
                exact("Statistics"),
                strong("MATLAB", "R", "Data Analysis"),
                weak("Python", "Machine Learning"));

        putRelated(map, "Data Analysis",
                exact("Data Analysis"),
                strong("Python", "SQL", "Statistics", "MATLAB", "R"),
                weak("Java", "Machine Learning"));

        putRelated(map, "Machine Learning",
                exact("Machine Learning"),
                strong("Python", "TensorFlow", "PyTorch", "Data Analysis", "Statistics"),
                weak("Java", "R"));

        putRelated(map, "Teaching",
                exact("Teaching", "Tutoring"),
                strong("Communication"),
                weak());

        putRelated(map, "Tutoring",
                exact("Tutoring", "Teaching"),
                strong("Communication"),
                weak());

        putRelated(map, "Communication",
                exact("Communication"),
                strong("Teaching", "Tutoring"),
                weak());

        putRelated(map, "Research",
                exact("Research"),
                strong("Writing", "Data Analysis", "Statistics"),
                weak());

        putRelated(map, "Writing",
                exact("Writing"),
                strong("Research", "Communication"),
                weak());

        putRelated(map, "Cybersecurity",
                exact("Cybersecurity"),
                strong("Networking", "Linux"),
                weak("Python"));

        putRelated(map, "Database",
                exact("Database", "SQL"),
                strong("SQL", "Data Analysis"),
                weak("Java", "Python"));

        putRelated(map, "Cloud Computing",
                exact("Cloud Computing"),
                strong("AWS", "Azure", "Google Cloud", "Docker", "Kubernetes"),
                weak());

        return map;
    }

    private static void putRelated(Map<String, Map<String, Double>> map,
                                   String required,
                                   Map<String, Double> weights) {
        map.put(required, weights);
    }

    private static Map<String, Double> exact(String... skills) {
        Map<String, Double> m = new HashMap<>();
        for (String s : skills) {
            m.put(s, EXACT_CREDIT);
        }
        return m;
    }

    private static Map<String, Double> strong(String... skills) {
        Map<String, Double> m = new HashMap<>();
        for (String s : skills) {
            m.put(s, 0.8);
        }
        return m;
    }

    private static Map<String, Double> weak(String... skills) {
        Map<String, Double> m = new HashMap<>();
        for (String s : skills) {
            m.put(s, 0.5);
        }
        return m;
    }

    private static void putRelated(Map<String, Map<String, Double>> map,
                                   String required,
                                   Map<String, Double> exact,
                                   Map<String, Double> strong,
                                   Map<String, Double> weak) {
        Map<String, Double> combined = new LinkedHashMap<>();
        combined.putAll(exact);
        for (Map.Entry<String, Double> e : strong.entrySet()) {
            combined.putIfAbsent(e.getKey(), e.getValue());
        }
        for (Map.Entry<String, Double> e : weak.entrySet()) {
            combined.putIfAbsent(e.getKey(), e.getValue());
        }
        map.put(required, combined);
    }

    public static class SkillMatchOutcome {
        private final double matchScore;
        private final List<String> matchedSkills;
        private final List<String> missingSkills;
        private final List<SkillRelationHint> relatedMatches;

        public SkillMatchOutcome(double matchScore,
                                 List<String> matchedSkills,
                                 List<String> missingSkills,
                                 List<SkillRelationHint> relatedMatches) {
            this.matchScore = matchScore;
            this.matchedSkills = matchedSkills;
            this.missingSkills = missingSkills;
            this.relatedMatches = relatedMatches;
        }

        public double getMatchScore() {
            return matchScore;
        }

        public List<String> getMatchedSkills() {
            return matchedSkills;
        }

        public List<String> getMissingSkills() {
            return missingSkills;
        }

        public List<SkillRelationHint> getRelatedMatches() {
            return relatedMatches;
        }
    }
}
