package ru.smole.cases;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class CaseManager {

    private @Getter Map<String, Case> caseMap;

    public CaseManager() {
        caseMap = new HashMap<>();
    }
}
