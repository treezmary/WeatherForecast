package com.example.weatherforecast;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

public class WeatherDataXmlParser {
	private static final String ns = null;

	// We don't use namespaces
	public List<WeatherData> parse(InputStream in)
			throws XmlPullParserException, IOException {
		try {
			XmlPullParser parser = Xml.newPullParser();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			parser.setInput(in, null);
			parser.nextTag();
			return readWeatherData(parser);
		} finally {
			in.close();
		}
	}

	private List<WeatherData> readWeatherData(XmlPullParser parser)
			throws XmlPullParserException, IOException {
		List<WeatherData> weatherDatas = new ArrayList<WeatherData>();

		parser.require(XmlPullParser.START_TAG, ns, "weatherdata");
		while (parser.next() != XmlPullParser.END_DOCUMENT) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}

			String name = parser.getName();
			// Starts by looking for the time tag
			if (name.equals("time")) {

				WeatherData weatherdata = readEntry(parser);
				weatherDatas.add(weatherdata);

			}
		}
		return weatherDatas;
	}

	public static class WeatherData {
		public final String date;
		public final String weatherDescription;
		public final String windDescription;
		public final String temperature;

		private WeatherData(String date, String description,
				String windDescription, String temperature) {
			this.date = date;
			this.weatherDescription = description;
			this.windDescription = windDescription;
			this.temperature = temperature;
		}
	}

	private WeatherData readEntry(XmlPullParser parser)
			throws XmlPullParserException, IOException {
		String date = null;
		String weatherDescription = null;
		String windDescription = null;
		String temperature = null;

		while ((parser.getEventType() != XmlPullParser.END_DOCUMENT)) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				if ((parser.getName().equals("time"))
						&& parser.getEventType() == XmlPullParser.END_TAG) {
					break;
				} else {
					parser.nextTag();
					continue;
				}
			}

			String name = parser.getName();
			if (name.equals("time")) {
				date = readTime(parser);
			}
			if (name.equals("symbol")) {
				weatherDescription = readDescription(parser);
			}
			if (name.equals("windSpeed")) {
				windDescription = readWindDescription(parser);
			} else if (name.equals("temperature")) {
				temperature = readTemperature(parser);
			}
			parser.nextTag();
		}
		return new WeatherData(date, weatherDescription, windDescription,
				temperature);
	}

	// Processes time tags in the weatherdata.
	private String readTime(XmlPullParser parser) throws IOException,
			XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, ns, "time");
		String time = readDate(parser);
		parser.require(XmlPullParser.START_TAG, ns, "time");
		return time;
	}

	// Processes symbol tags in the weatherdata.
	private String readDescription(XmlPullParser parser) throws IOException,
			XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, ns, "symbol");
		String symbol = readName(parser);
		parser.require(XmlPullParser.END_TAG, ns, "symbol");
		return symbol;
	}

	// Processes windSpeed tags in the weatherdata.
	private String readWindDescription(XmlPullParser parser)
			throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, ns, "windSpeed");
		String windSpeed = readWindName(parser);
		parser.require(XmlPullParser.END_TAG, ns, "windSpeed");
		return windSpeed;
	}

	// Processes summary tags in the weatherdata.
	private String readTemperature(XmlPullParser parser) throws IOException,
			XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, ns, "temperature");
		String temperature = readDayTemperature(parser);
		parser.require(XmlPullParser.END_TAG, ns, "temperature");
		return temperature;
	}

	// For the tags time, symbol, windSpeed and temperature extracts their text
	// values.
	private String readDate(XmlPullParser parser) throws IOException,
			XmlPullParserException {
		String result = "";
		result = parser.getAttributeValue(null, "day");
		return result;
	}

	private String readName(XmlPullParser parser) throws IOException,
			XmlPullParserException {
		String result = "";
		result = parser.getAttributeValue(null, "name");
		parser.nextTag();
		return result;
	}

	private String readWindName(XmlPullParser parser) throws IOException,
			XmlPullParserException {
		String result = "";
		result = parser.getAttributeValue(null, "name");
		parser.nextTag();

		return result;
	}

	private String readDayTemperature(XmlPullParser parser) throws IOException,
			XmlPullParserException {
		String result = "";
		result = parser.getAttributeValue(null, "day");
		parser.nextTag();
		return result;
	}

}
