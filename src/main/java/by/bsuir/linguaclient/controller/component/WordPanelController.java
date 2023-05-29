package by.bsuir.linguaclient.controller.component;

import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
@FxmlView("/fxml/WordPanelView.fxml")
@Slf4j
public class WordPanelController extends AbstractWordPanelController {

}
