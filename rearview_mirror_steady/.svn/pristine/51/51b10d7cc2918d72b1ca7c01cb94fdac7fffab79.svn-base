package com.txznet.feedback.dtool;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.feedback.data.Question;

public class SaxParseUtil {
	
	public static Question getQuestions(InputStream is){
		try {
			QuestionHandler handler = new QuestionHandler();
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser parser = spf.newSAXParser();
			parser.parse(is, handler);
			return handler.getQuestion();
		} catch (ParserConfigurationException e) {
			LogUtil.loge("ParserConfigurationException 解析xml出错！");
		} catch (SAXException e) {
			LogUtil.loge("SAXException 解析xml出错！");
		} catch (IOException e) {
			LogUtil.loge("IOException 解析xml出错！");
		}
		
		return null;
	}
}