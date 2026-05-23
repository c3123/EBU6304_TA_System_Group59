package com.ta.service.student;

/**
 * Partial credit link between a student skill and a required skill.
 */
public class SkillRelationHint {
    private String studentSkill;
    private String requiredSkill;
    private double credit;

    public SkillRelationHint() {
    }

    public SkillRelationHint(String studentSkill, String requiredSkill, double credit) {
        this.studentSkill = studentSkill;
        this.requiredSkill = requiredSkill;
        this.credit = credit;
    }

    public String getStudentSkill() {
        return studentSkill;
    }

    public void setStudentSkill(String studentSkill) {
        this.studentSkill = studentSkill;
    }

    public String getRequiredSkill() {
        return requiredSkill;
    }

    public void setRequiredSkill(String requiredSkill) {
        this.requiredSkill = requiredSkill;
    }

    public double getCredit() {
        return credit;
    }

    public void setCredit(double credit) {
        this.credit = credit;
    }

    /** Display form e.g. "MATLAB -> Statistics (80%)". */
    public String toDisplayLabel() {
        int pct = (int) Math.round(credit * 100);
        return studentSkill + " -> " + requiredSkill + " (" + pct + "%)";
    }
}
