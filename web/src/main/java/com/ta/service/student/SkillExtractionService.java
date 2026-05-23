package com.ta.service.student;

import com.ta.model.JobPosting;
import com.ta.model.StudentProfile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Rule-based fallback layer for future AI-assisted recommendation features.
 *
 * This service extracts a deterministic set of normalized skills from profile
 * and job free text before any external AI integration exists.
 */
public class SkillExtractionService {
    private static final Map<String, List<String>> SKILL_DICTIONARY = createSkillDictionary();

    public List<String> extractSkills(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new ArrayList<>();
        }

        List<String> skills = new ArrayList<>();
        Set<String> matchedAliases = new HashSet<>();
        for (Map.Entry<String, List<String>> entry : SKILL_DICTIONARY.entrySet()) {
            if (containsAnyAlias(text, entry.getValue(), matchedAliases)) {
                skills.add(entry.getKey());
            }
        }
        return skills;
    }

    public List<String> extractSkillsFromStudent(StudentProfile profile) {
        if (profile == null) {
            return new ArrayList<>();
        }
        return extractSkills(profile.getSkills());
    }

    public List<String> extractSkillsFromJob(JobPosting job) {
        if (job == null) {
            return new ArrayList<>();
        }
        return extractSkills(job.getRequirements());
    }

    private static Map<String, List<String>> createSkillDictionary() {
        Map<String, List<String>> dictionary = new LinkedHashMap<>();
        dictionary.put("Java", aliases("Java"));
        dictionary.put("Python", aliases("Python"));
        dictionary.put("C", aliases("C"));
        dictionary.put("C++", aliases("C++", "cpp"));
        dictionary.put("JavaScript", aliases("JavaScript", "js"));
        dictionary.put("HTML", aliases("HTML"));
        dictionary.put("CSS", aliases("CSS"));
        dictionary.put("SQL", aliases("SQL"));
        dictionary.put("Database", aliases("Database", "databases"));
        dictionary.put("Git", aliases("Git"));
        dictionary.put("Linux", aliases("Linux"));
        dictionary.put("Unix", aliases("Unix"));
        dictionary.put("Operating Systems", aliases("Operating Systems", "Operating System", "OS"));
        dictionary.put("Networking", aliases("Networking", "Computer Networks", "Network"));
        dictionary.put("Shell Scripting", aliases("Shell Scripting", "Bash", "Shell"));
        dictionary.put("Docker", aliases("Docker"));
        dictionary.put("Kubernetes", aliases("Kubernetes", "K8s"));
        dictionary.put("Cloud Computing", aliases("Cloud Computing", "Cloud"));
        dictionary.put("AWS", aliases("AWS", "Amazon Web Services"));
        dictionary.put("Azure", aliases("Azure"));
        dictionary.put("Google Cloud", aliases("Google Cloud", "GCP"));
        dictionary.put("Cybersecurity", aliases("Cybersecurity", "Cyber Security", "Security"));
        dictionary.put("Data Analysis", aliases("Data Analysis", "Data Analytics"));
        dictionary.put("Statistics", aliases("Statistics", "Statistical"));
        dictionary.put("Research", aliases("Research"));
        dictionary.put("Writing", aliases("Writing", "Academic Writing"));
        dictionary.put("R", aliases("R"));
        dictionary.put("MATLAB", aliases("MATLAB"));
        dictionary.put("Spring", aliases("Spring", "Spring Boot"));
        dictionary.put("React", aliases("React", "React.js", "ReactJS"));
        dictionary.put("Node.js", aliases("Node.js", "NodeJS", "Node"));
        dictionary.put("REST API", aliases("REST API", "RESTful API", "REST"));
        dictionary.put("Algorithms", aliases("Algorithms"));
        dictionary.put("Data Structures", aliases("Data Structures"));
        dictionary.put("Communication", aliases("Communication"));
        dictionary.put("Teaching", aliases("Teaching", "tutoring"));
        dictionary.put("Tutoring", aliases("Tutoring"));
        dictionary.put("Problem Solving", aliases("Problem Solving"));
        dictionary.put("Machine Learning", aliases("Machine Learning"));
        dictionary.put("TensorFlow", aliases("TensorFlow"));
        dictionary.put("PyTorch", aliases("PyTorch"));
        dictionary.put("API Development", aliases("API Development"));
        dictionary.put("Testing", aliases("Testing", "unit testing"));
        dictionary.put("Debugging", aliases("Debugging"));
        return dictionary;
    }

    private static List<String> aliases(String... values) {
        return Arrays.asList(values);
    }

    private static boolean containsAnyAlias(String text, List<String> aliases, Set<String> matchedAliases) {
        for (String alias : aliases) {
            String normalizedAlias = alias.toLowerCase();
            if (matchedAliases.contains(normalizedAlias)) {
                continue;
            }
            if (skillPattern(alias).matcher(text).find()) {
                matchedAliases.add(normalizedAlias);
                return true;
            }
        }
        return false;
    }

    private static Pattern skillPattern(String skill) {
        return Pattern.compile("(?i)(?<![A-Za-z0-9+#])" + Pattern.quote(skill) + "(?![A-Za-z0-9+#])");
    }
}
