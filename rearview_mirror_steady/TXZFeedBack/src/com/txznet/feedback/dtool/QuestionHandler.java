package com.txznet.feedback.dtool;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.text.TextUtils;

import com.txznet.feedback.data.Question;

/**
 * <questions>
 * 		<description>音乐分类</description>
 * 		<question>
 * 			<description>声控打开音乐</description>
 * 		</question>
 * </questions>
 *
 */
public class QuestionHandler extends DefaultHandler {
	private static final String PARENT_TAG = "questions";
	private static final String ITEM_TAG = "question";
	private static final String DESCRIPTION = "description";

	private Question itemQuestion;
	private Question parentQuestion;

	private String itemTag;
	private String currentTag;
	
	public Question getQuestion() {
		return parentQuestion;
	}

	@Override
	public void startDocument() throws SAXException {
		parentQuestion = new Question();
		parentQuestion.setQuestionId(System.currentTimeMillis());
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		currentTag = localName;
		if (PARENT_TAG.equals(localName)) {
			itemTag = PARENT_TAG;
		} else if (ITEM_TAG.equals(localName)) {
			itemQuestion = new Question();
			itemQuestion.setQuestionId(System.currentTimeMillis());
			itemTag = ITEM_TAG;
		} else if(DESCRIPTION.equals(localName)){
			
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (currentTag != null) {
			if (currentTag.equals(DESCRIPTION)) {
				String value = new String(ch, start, length);
				if (TextUtils.isEmpty(value) || value.length() < 3) {
					return;
				}

				if (itemTag != null) {
					if (itemTag.equals(PARENT_TAG)) {
						parentQuestion.setDescription(value);
					} else if (itemTag.equals(ITEM_TAG)) {
						itemQuestion.setDescription(value);
					}
				}
			} else if (currentTag.equals("")) {
				
			}
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (TextUtils.isEmpty(localName)) {
			return;
		}

		if (localName.equals(PARENT_TAG)) {

		} else if (localName.equals(ITEM_TAG)) {
			parentQuestion.addQuestion(itemQuestion);
		}
	}

	@Override
	public void endDocument() throws SAXException {
	}
}