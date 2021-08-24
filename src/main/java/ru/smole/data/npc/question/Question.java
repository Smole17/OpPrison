package ru.smole.data.npc.question;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class Question {

    private QuestionStep step;

    public enum QuestionStep {

        COMPLETING,
        ALR_COMPLETED
    }
}
