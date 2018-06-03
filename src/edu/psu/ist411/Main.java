/*
 * Copyright 2018 Group 5.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.psu.ist411;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * Group project.
 *
 * A large number of organizations from Twitter to the National Oceanic and
 * Atmospheric Administration (NOAA) make vast quantities and varieties of
 * information available via web services. These services can be accessed by
 * extending the examples used in Lesson 1 via some of the techniques learned in
 * Lesson 3. Work with your teammates to decide on a web service and write a
 * Java application to access and display web-based data from your choice of one
 * of the many available sources. You are highly encouraged to discuss the
 * benefits (and potential pitfalls) of working with web-based data.
 *
 * @author Tyler Suehr
 * @author Win Ton
 * @author Steven Weber
 * @author David Wong
 */
public class Main {
    private final static Pattern temperatureRegex = Pattern.compile("<div class=\"today_nowcard-temp\"><span class=\"\">([0-9]+)<sup>°</sup></span></div>");
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        final String spec = "https://weather.com/weather/today/l/USPA1276:1:US";
        System.out.println("Webpage: " + spec);
        
        try {
            final URL url = new URL(spec);
            final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            HttpURLConnection.setFollowRedirects(true);
            connection.setRequestProperty("Accept-Encoding", "gzip, deflate");
            
            final int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new Exception("Invalid response code: " + responseCode);
            }
            
            final String encoding = connection.getContentEncoding();
            InputStream inputStream = connection.getInputStream();
            if (encoding != null) {
                if (encoding.equalsIgnoreCase("gzip")) {
                    inputStream = new GZIPInputStream(connection.getInputStream());
                } else if (encoding.equalsIgnoreCase("deflate")) {
                    inputStream = new InflaterInputStream(connection.getInputStream(), new Inflater(true));
                } else {
                    throw new Exception("Unknown encoding: " + encoding);
                }
            }
            
            try (final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
                final StringBuilder stringBuilder = new StringBuilder();
                String line = bufferedReader.readLine();
                while (line != null) {
                    stringBuilder.append(line);
                    line = bufferedReader.readLine();
                }
                
                final String markup = stringBuilder.toString();
                final Matcher matcher = temperatureRegex.matcher(markup);
                if (matcher.find()) {
                    System.out.println("Temperature: " + matcher.group(1) + "°");
                } else {
                    throw new Exception("Temperature not found.");
                }
            }
            
            System.out.println();
        } catch (final Exception exception) {
            exception.printStackTrace();
        }
    }
}