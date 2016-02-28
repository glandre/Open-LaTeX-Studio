/* 
 * Copyright (c) 2016 Sebastian Brudzinski
 * 
 * See the file LICENSE for copying permission.
 */
package latexstudio.editor.remote;

import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;
import latexstudio.editor.ApplicationLogger;
import latexstudio.editor.DropboxRevisionsTopComponent;
import latexstudio.editor.TopComponentFactory;
import latexstudio.editor.settings.ApplicationSettings;
import latexstudio.editor.settings.SettingsService;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;

@ActionID(
        category = "Remote",
        id = "latexstudio.editor.remote.DisconnectDropbox"
)
@ActionRegistration(
        displayName = "#CTL_DisconnectDropbox"
)
@ActionReference(path = "Menu/Remote", position = 3508)
@Messages("CTL_DisconnectDropbox=Disconnect from Dropbox")
public final class DisconnectDropbox implements ActionListener {

    private final DropboxRevisionsTopComponent drtc = new TopComponentFactory<DropboxRevisionsTopComponent>()
            .getTopComponent(DropboxRevisionsTopComponent.class.getSimpleName());

    private static final ApplicationLogger LOGGER = new ApplicationLogger("Dropbox");

    @Override
    public void actionPerformed(ActionEvent e) {
        Runnable task;
        task = new Runnable() {
            @Override
            public void run() {
                final ProgressHandle progressBar = ProgressHandleFactory.createHandle("Disconnecting from Dropbox");
                progressBar.start();

                DbxClient client = DbxUtil.getDbxClient();
                progressBar.progress("Token disabled");
                if (client == null) {
                    LOGGER.log("Dropbox account already disconnected.");
                    progressBar.finish();
                    return;
                }

                String userToken = client.getAccessToken();

                if (userToken != null && !userToken.isEmpty()) {
                    try {

                        client.disableAccessToken();

                        ApplicationSettings appSettings = SettingsService.loadApplicationSettings();
                        appSettings.setDropboxToken("");
                        SettingsService.saveApplicationSettings(appSettings);

                        LOGGER.log("Successfully disconnected from Dropbox account.");

                    } catch (DbxException ex) {
                        JOptionPane.showMessageDialog(null,
                                "Invalid access token! Open LaTeX Studio has NOT been connected with Dropbox.\n Please try again and provide correct access token.",
                                "Invalid token",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    LOGGER.log("Dropbox account already disconnected.");
                }

                progressBar.finish();
            }
        };
        RequestProcessor.getDefault().post(task);

        drtc.updateRevisionsList(null);
        drtc.close();
    }
}
