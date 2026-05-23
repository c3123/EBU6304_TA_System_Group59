package com.ta.service.mo;

import com.ta.testsupport.MoTestSupport;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MoApplicationStatusFilterTest extends MoTestSupport {

    @Test
    void nullFilter_matchesAll() {
        assertNull(MoApplicationService.parseStatusFilter(null));
        assertTrue(MoApplicationService.matchesStatusFilter("pending", null));
        assertTrue(MoApplicationService.matchesStatusFilter("hired", null));
    }

    @Test
    void blankFilter_matchesAll() {
        assertNull(MoApplicationService.parseStatusFilter(""));
        assertNull(MoApplicationService.parseStatusFilter("   "));
    }

    @Test
    void noneSentinel_neverMatches() {
        Set<String> tokens = MoApplicationService.parseStatusFilter(MoApplicationService.STATUS_FILTER_NONE_SENTINEL);
        assertTrue(tokens.isEmpty());
        assertFalse(MoApplicationService.matchesStatusFilter("pending", tokens));
        assertFalse(MoApplicationService.matchesStatusFilter("hired", tokens));
    }

    @Test
    void allFourTokens_parsedAsNoFilter() {
        assertNull(MoApplicationService.parseStatusFilter("pending,shortlisted,rejected,hired"));
        assertTrue(MoApplicationService.matchesStatusFilter("viewed", null));
    }

    @Test
    void pendingToken_matchesPendingAndViewed() {
        Set<String> tokens = MoApplicationService.parseStatusFilter("pending");
        assertTrue(MoApplicationService.matchesStatusFilter("pending", tokens));
        assertTrue(MoApplicationService.matchesStatusFilter("viewed", tokens));
        assertFalse(MoApplicationService.matchesStatusFilter("hired", tokens));
    }

    @Test
    void unknownToken_exactMatchOnly() {
        Set<String> tokens = MoApplicationService.parseStatusFilter("foobar");
        assertTrue(MoApplicationService.matchesStatusFilter("foobar", tokens));
        assertFalse(MoApplicationService.matchesStatusFilter("pending", tokens));
    }

    @Test
    void hiredOnly_matchesHired() {
        Set<String> tokens = MoApplicationService.parseStatusFilter("hired");
        assertTrue(MoApplicationService.matchesStatusFilter("hired", tokens));
        assertFalse(MoApplicationService.matchesStatusFilter("shortlisted", tokens));
    }
}
