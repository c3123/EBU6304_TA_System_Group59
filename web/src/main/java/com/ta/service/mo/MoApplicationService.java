package com.ta.service.mo;

import com.ta.dto.mo.MoApplicationDetailResponse;
import com.ta.dto.mo.MoApplicationListResponse;

/**
 * MO application viewing service boundary.
 *
 * Ownership: B side (applications and status tracking).
 * Rules:
 * 1) MO can only read applications for jobs owned by that MO.
 * 2) Detail read should auto-update status pending -> viewed.
 * 3) List should return active=true applications only.
 */
public class MoApplicationService {

    public MoApplicationListResponse listApplications(String moId, String jobId) {
        // TODO implement ownership filtering and active=true filtering
        throw new UnsupportedOperationException("TODO: listApplications");
    }

    public MoApplicationDetailResponse getDetailAndMarkViewed(String moId, String applicationId) {
        // TODO implement ownership check + status transition
        throw new UnsupportedOperationException("TODO: getDetailAndMarkViewed");
    }
}
