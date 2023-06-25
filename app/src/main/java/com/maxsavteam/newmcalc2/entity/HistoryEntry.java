package com.maxsavteam.newmcalc2.entity;

import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public final class HistoryEntry {
    private String example;
    private String answer;
    private String description;

    public HistoryEntry(String example, String answer) {
        this(example, answer, null);
    }

    public HistoryEntry(String example, String answer, @Nullable String description) {
        this.example = example;
        this.answer = answer;
        if (description == null || description.isEmpty())
            this.description = null;
        else
            this.description = description;
    }

    public String getExample() {
        return example;
    }

    public void setExample(String example) {
        this.example = example;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(@Nullable String description) {
        if (description == null || description.isEmpty()) {
            this.description = null;
        } else {
            this.description = description;
        }
    }

    public JSONObject getJSON() throws JSONException {
        return new JSONObject()
                .put("example", example)
                .put("answer", answer)
                .put("description", description);
    }

    @Override
    public String toString() {
        return "HistoryEntry{" +
                "example='" + example + '\'' +
                ", answer='" + answer + '\'' +
                ", description='" + description + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        HistoryEntry that = (HistoryEntry) o;

        if (!Objects.equals(example, that.example)) {
            return false;
        }
        if (!Objects.equals(answer, that.answer)) {
            return false;
        }
        return Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        int result = example != null ? example.hashCode() : 0;
        result = 31 * result + (answer != null ? answer.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        return result;
    }

    public static HistoryEntry create(String ex, String ans) {
        return new HistoryEntry(ex, ans);
    }
}
