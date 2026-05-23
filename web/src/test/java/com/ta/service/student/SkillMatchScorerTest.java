package com.ta.service.student;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkillMatchScorerTest {

    private final SkillMatchScorer scorer = new SkillMatchScorer();

    @Test
    void score_exactStrictSkillsReceiveFullCredit() {
        var outcome = scorer.score(List.of("Java", "SQL"), List.of("Java", "SQL", "Python"));

        assertEquals(1.0, outcome.getMatchScore());
        assertEquals(List.of("Java", "SQL"), outcome.getMatchedSkills());
        assertTrue(outcome.getMissingSkills().isEmpty());
        assertTrue(outcome.getRelatedMatches().isEmpty());
    }

    @Test
    void score_strictSkillDoesNotAcceptRelatedPartialCredit() {
        var outcome = scorer.score(List.of("Java"), List.of("Python"));

        assertEquals(0.0, outcome.getMatchScore());
        assertEquals(List.of("Java"), outcome.getMissingSkills());
    }

    @Test
    void score_flexibleSkillUsesRelatedPartialCredit() {
        var outcome = scorer.score(List.of("Machine Learning"), List.of("Python"));

        assertEquals(0.8, outcome.getMatchScore());
        assertTrue(outcome.getMatchedSkills().isEmpty());
        assertTrue(outcome.getMissingSkills().isEmpty());
        assertEquals("Python -> Machine Learning (80%)", outcome.getRelatedMatches().get(0).toDisplayLabel());
    }

    @Test
    void score_emptyRequirementsReturnsZero() {
        var outcome = scorer.score(List.of(), List.of("Java"));

        assertEquals(0.0, outcome.getMatchScore());
        assertTrue(outcome.getMatchedSkills().isEmpty());
    }
}
