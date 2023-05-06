package by.bsuir.linguaclient.configuration;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

@Component
@Data
@Slf4j
public class ResourceHolder implements DisposableBean {

    private MediaPlayerFactory mediaPlayerFactory;
    private EmbeddedMediaPlayer embeddedMediaPlayer;

    @Override
    public void destroy() {
        if (mediaPlayerFactory != null) {
            mediaPlayerFactory.release();
        }
        if (embeddedMediaPlayer != null) {
            embeddedMediaPlayer.controls().stop();
            embeddedMediaPlayer.release();
        }
        log.info("Resources released");
    }
}
