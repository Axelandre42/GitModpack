package net.axelandre42.gitmodpack.model;

public class Rule {
    public Rule() {}
    public Rule(String from, String to) {
        this.from = from;
        this.to = to;
    }
    
    public String from;
    public String to;
}
