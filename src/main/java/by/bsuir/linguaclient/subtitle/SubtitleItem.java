package by.bsuir.linguaclient.subtitle;

import lombok.Data;

@Data
public class SubtitleItem {

    private int seqNumber;
    private long startTime;
    private long endTime;
    private String firstLangPhrase;
    private String secondLangPhrase;
}
