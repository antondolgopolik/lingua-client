package by.bsuir.linguaclient.subtitle;

import org.apache.commons.io.input.BOMInputStream;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;

@Component
public class SubtitleParser {

    private final DateTimeFormatter timeFormatter;

    public SubtitleParser() {
        this.timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss,SSS");
    }

    public Subtitle parse(InputStream inputStream) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new BOMInputStream(inputStream), StandardCharsets.UTF_8));
        return new Subtitle(parseSubtitleItems(reader));
    }

    private ArrayList<SubtitleItem> parseSubtitleItems(BufferedReader reader) {
        ArrayList<SubtitleItem> subtitleItems = new ArrayList<>();
        try {
            String strSeqNumber;
            while ((strSeqNumber = reader.readLine()) != null) {
                SubtitleItem subtitleItem = new SubtitleItem();
                subtitleItem.setSeqNumber(Integer.parseInt(strSeqNumber));
                String[] timeRange = reader.readLine().split(" --> ");
                subtitleItem.setStartTime(parseTime(timeRange[0]));
                subtitleItem.setEndTime(parseTime(timeRange[1]));
                subtitleItem.setFirstLangPhrase(reader.readLine());
                subtitleItem.setSecondLangPhrase(reader.readLine());
                subtitleItems.add(subtitleItem);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return subtitleItems;
    }

    private long parseTime(String strTime) {
        return LocalTime.parse(strTime, timeFormatter).get(ChronoField.MILLI_OF_DAY);
    }
}
