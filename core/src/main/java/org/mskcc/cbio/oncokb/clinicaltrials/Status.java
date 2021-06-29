package org.mskcc.cbio.oncokb.clinicaltrials;

import org.mskcc.cbio.oncokb.model.AlterationType;

public enum Status {
//    NOT_YET_RECRUITING("Not yet recruiting"),
//    RECRUITING("Recruiting"),
//    ENROLLING_BY_INVITATION("Enrolling by invitation"),
//    ACTIVE_NOT_RECRUITING("Active, not recruiting"),
//    SUSPENDED("Suspended"),
//    TERMINATED("Terminated"),
//    COMPLETED("Completed"),
//    WITHDRAWN("Withdrawn"),
//    UNKNOWN("Unknown");

    COMPLETE("Complete"),
    CLOSED_ACCURAL("Closed to Accrual"),
    ADMIN_COMPLETE("Administratively Complete"),
    ACTIVE("Active"),
    CLOSED_ACCURAL_INTERVENTION("Closed to Accrual and Intervention"),
    TEM_CLOSED_ACCURAL("Temporarily Closed to Accrual"),
    NA("NA"),
    WITHDRAWN("Withdrawn"),
    ENROLLING_BY_INVITATION("Enrolling by Invitation"),
    TEM_CLOSED_ACCURAL_INTERVENTION("Temporarily Closed to Accrual and Intervention"),
    IN_REVIEW("In Review"),
    APPROVED("Approved");


    String term;

    Status(String term) {
        this.term = term;
    }

    public String getTerm() {
        return term;
    }

    public static Status getByTerm(String term) {
        for (Status status : Status.values()) {
            if (status.getTerm().equalsIgnoreCase(term)) {
                return status;
            }
        }
        return null;
    }
}
