package com.txznet.feedback.data;

import java.util.ArrayList;
import java.util.List;

public class Question {
	private long qId;
	private String description;
	private List<Question> questions;

	public void setQuestionId(long id) {
		qId = id;
	}

	public long getQuestionId() {
		return qId;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public void setQuestionList(List<Question> list) {
		questions = list;
	}

	public List<Question> getQuestionList() {
		return questions;
	}

	public void addQuestion(Question question) {
		if (question == null) {
			return;
		}

		if (questions == null) {
			questions = new ArrayList<Question>();
		}
		this.questions.add(question);
	}

	@Override
	public String toString() {
		return "[" + description + "]";
	}
}