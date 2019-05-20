package com.google.code.shardbatis.builder;

import com.google.code.shardbatis.converter.ShardConfigHandler;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;

/**
 * @author sean.he
 * @author colddew
 */
public class ShardConfigParser {

	private static final Log log = LogFactory.getLog(ShardConfigParser.class);

	/**
	 * 解析xml配置文件并构建ShardConfigFactory
	 * @param input
	 * @return
	 * @throws Exception
	 */
	public ShardConfigHolder parse(InputStream input) throws Exception {

		final ShardConfigHolder configHolder = ShardConfigHolder.getInstance();

		SAXParserFactory spf = SAXParserFactory.newInstance();
		spf.setValidating(true);
		spf.setNamespaceAware(true);
		SAXParser parser = spf.newSAXParser();
		XMLReader reader = parser.getXMLReader();

		DefaultHandler handler = new ShardConfigHandler(configHolder);
		reader.setContentHandler(handler);
		reader.setEntityResolver(handler);
		reader.setErrorHandler(handler);
		reader.parse(new InputSource(input));

		return configHolder;
	}
}
