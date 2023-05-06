package by.bsuir.linguaclient.subtitle;

import java.util.ArrayList;

public class Subtitle {

    private final ArrayList<SubtitleItem> subtitleItems;
    private int pos;

    public Subtitle(ArrayList<SubtitleItem> subtitleItems) {
        this.subtitleItems = subtitleItems;
    }

    public void changePosition(long time) {
        pos = searchPos(-1, subtitleItems.size(), time);
    }

    private int searchPos(int l, int r, long time) {
        while (l + 1 < r) {
            int m = (l + r) / 2;
            if (subtitleItems.get(m).getEndTime() < time) {
                l = m;
            } else {
                r = m;
            }
        }
        return r;
    }

    public void back(long time) {
        while (pos > 0 && subtitleItems.get(pos - 1).getEndTime() >= time) {
            pos--;
        }
    }

    public void forward(long time) {
        while (pos < subtitleItems.size() && subtitleItems.get(pos).getEndTime() < time) {
            pos++;
        }
    }

    public boolean showNext(long time) {
        return pos < subtitleItems.size() && subtitleItems.get(pos).getStartTime() <= time;
    }

    public SubtitleItem next() {
        return subtitleItems.get(pos++);
    }
}
